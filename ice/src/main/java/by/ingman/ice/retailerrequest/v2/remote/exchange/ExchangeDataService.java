package by.ingman.ice.retailerrequest.v2.remote.exchange;

import android.app.Activity;
import android.app.IntentService;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.os.Parcelable;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;

import com.google.gson.Gson;

import org.apache.log4j.Logger;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import by.ingman.ice.retailerrequest.v2.ApkUpdateActivity;
import by.ingman.ice.retailerrequest.v2.helpers.AlarmHelper;
import by.ingman.ice.retailerrequest.v2.helpers.ConfigureLog4J;
import by.ingman.ice.retailerrequest.v2.helpers.DBHelper;
import by.ingman.ice.retailerrequest.v2.helpers.GsonHelper;
import by.ingman.ice.retailerrequest.v2.helpers.NotificationsUtil;
import by.ingman.ice.retailerrequest.v2.helpers.StaticFileNames;
import by.ingman.ice.retailerrequest.v2.remote.dao.RequestDao;
import by.ingman.ice.retailerrequest.v2.structure.Request;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;

/**
 * Created with IntelliJ IDEA.
 * User: Администратор
 * Date: 07.05.13
 * Time: 21:32
 * To change this template use File | Settings | File Templates.
 */
public class ExchangeDataService extends IntentService {

    static {
        ConfigureLog4J.configure();
    }

    private final Logger log = Logger.getLogger(ExchangeDataService.class);

    public static final String EXTRA_RECEIVER = "extra_receiver";

    // for test
    private static int messIndex = 0;

    ExecutorService executorService;
    NotificationManager notificationManager;
    int k = 0;
    SharedPreferences sharedPreferences;
    private Context that;

    DBHelper dbHelper;
    SQLiteDatabase db;
    private NotificationsUtil notifUtil;
    private RequestDao requestDao;

    Date debtDate = null, restsDate = null, clientsDate = null;

    private Gson gson;
    private ExchangeUtil util;
    private ResultReceiver receiver = null;
    private boolean forcedUpdate = false;

    /**
     * Creates an IntentService. Invoked by your subclass's constructor.
     *
     */
    public ExchangeDataService() {
        super("ExchangeDataService");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // FIXME this log is for test
        log.info("Test: service launched");

        Parcelable p = intent.getParcelableExtra(EXTRA_RECEIVER);/*Extras().containsKey(EXTRA_RECEIVER) ? intent
                .getExtras()
                .getParcelable(EXTRA_RECEIVER) : null;*/

        if (p == null) {
            receiver = null;
            forcedUpdate = false;
        } else {
            receiver = (ResultReceiver) p;
            forcedUpdate = true;
        }

        doExchangeData();
    }

    public void onCreate() {
        super.onCreate();
        this.that = this;

        gson = GsonHelper.createGson();
        notifUtil = new NotificationsUtil(that);
        requestDao = new RequestDao(that);
        executorService = Executors.newFixedThreadPool(1);
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        dbHelper = new DBHelper(that);
        db = dbHelper.getWritableDatabase();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(that);
        util = new ExchangeUtil(that);
    }

    private void doExchangeData() {
        boolean success = true;
        try {
            // updating public files from remote db
            updatePubFiles();

            // send unsent requests
            util.sendRequests();

            // вычитать отправленные заявки на сегодня без ответа
            ArrayList<String> sentRequestIds = readSentRequestIdsWithoutAnswer();

            // check for an answer in remote DB and show notification for those having an answer
            for (String reqId : sentRequestIds) {
                readRemoteAnswer(reqId);
            }
        } catch (Exception e) {
            log.error("Error in file updating service", e);
            success = false;
            if (isResponseToSend()) {
                receiver.send(Activity.RESULT_CANCELED, null);
            }
        } finally {
            AlarmHelper.createExchangeAlarm(this);
        }

        if (success && isResponseToSend()) {
            receiver.send(Activity.RESULT_OK, null);
        }
    }

    private boolean isResponseToSend() {
        return receiver != null;
    }

    private Date getDateForFilename(String filename) {
        if (filename.equals(StaticFileNames.RESTS_CSV_SD)) {
            return restsDate;
        } else if (filename.equals(StaticFileNames.CLIENTS_CSV_SD)) {
            return clientsDate;
        } else if (filename.equals(StaticFileNames.DEBTS_CSV_SD)) {
            return debtDate;
        }
        return null;
    }

    public void updatePubFiles() {
        try {
            notifUtil.dismissFileErrorNotifications();
            for (String filename : StaticFileNames.getFilenamesArray()) {
                boolean doUpdate = false;
                String url = sharedPreferences.getString("androidExchangePubDirPref", "");
                if (url.charAt(url.length() - 1) != '/') {
                    url = url.concat("/");
                }
                Date remoteDate = null;
                SmbFile smbFile = new SmbFile("smb://" + url + filename);
                if (Arrays.asList(fileList()).contains(filename)) {
                    Date localDate = new Date(getFileStreamPath(filename).lastModified());
                    Date prevRemoteDate = getDateForFilename(filename);
                    remoteDate = new Date(smbFile.getDate());
                    if (prevRemoteDate != null && remoteDate.after(prevRemoteDate)) {
                        doUpdate = true;
                    }
                    if (prevRemoteDate == null && remoteDate.after(localDate)) {
                        doUpdate = true;
                    }
                }
                if (!Arrays.asList(fileList()).contains(filename)) {
                    doUpdate = true;
                }
                if (doUpdate) {
                    if (enabledNotifications()) {
                        notifUtil.showFileProgressNotification("Обновление данных", getFileDescr(filename) + " обновляется", filename);
                    }
                    //updating file
                    SmbFileInputStream is = new SmbFileInputStream(smbFile);
                    FileOutputStream fos = openFileOutput(filename + "_temp", Context.MODE_PRIVATE);
                    byte[] buffer = new byte[8192];
                    int read;
                    while ((read = is.read(buffer)) > 0)
                        fos.write(buffer, 0, read);
                    is.close();
                    fos.close();

                    if (enabledNotifications()) {
                        notifUtil.dismissFileProgressNotification(filename);
                    }
                    getFileStreamPath(filename + "_temp").renameTo(getFileStreamPath(filename));
                    deleteFile(filename + "_temp");

                    String notif = null;
                    if (filename.equals(StaticFileNames.DEBTS_CSV_SD)) {
                        debtDate = remoteDate;
                    } else if (filename.equals(StaticFileNames.RESTS_CSV_SD)) {
                        restsDate = remoteDate;
                    } else if (filename.equals(StaticFileNames.CLIENTS_CSV_SD)) {
                        clientsDate = remoteDate;
                    }
                    notif = getFileDescr(filename) + " обновлён";
                    if (enabledNotifications()) {
                        notifUtil.showFileCompletedNotification("Файл обновлён", notif, filename);
                    }
                }
            }
        } catch (Exception e) {
            // TODO do not catch exceptions here
            log.error("Error loading data files", e);
            notifUtil.showErrorNotification("Ошибка при загрузке данных", "Ошибка загрузки файла данных.");

            notifUtil.dismissFileProgressNotification(StaticFileNames.DEBTS_CSV_SD);
            notifUtil.dismissFileProgressNotification(StaticFileNames.RESTS_CSV_SD);
            notifUtil.dismissFileProgressNotification(StaticFileNames.CLIENTS_CSV_SD);
        }
    }

    private boolean enabledNotifications() {
        return sharedPreferences.getBoolean("updateNotificationsEnabled", true);
    }

    private String getFileDescr(String filename) {
        String descr = "Файл";
        if (filename.equals(StaticFileNames.DEBTS_CSV_SD)) {
            descr = "Файл задолженностей";
        } else if (filename.equals(StaticFileNames.RESTS_CSV_SD)) {
            descr = "Файл остатков";
        } else if (filename.equals(StaticFileNames.CLIENTS_CSV_SD)) {
            descr = "Файл клиентов";
        }

        return descr;
    }

    private ArrayList<String> readSentRequestIdsWithoutAnswer() {
        String selection = "sent>0 and is_req>0";
        Cursor c = db.query(DBHelper.TABLE_REQUESTS_NAME, null, selection, null, null, null, null);
        ArrayList<String> requestIds = new ArrayList<String>();

        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    for (String columnName : c.getColumnNames()) {
                        if (columnName.equals("req_id")) {
                            String reqId = c.getString(c.getColumnIndex(columnName));
                            //проверить, есть ли уже ответы в базе
                            String answersSelection = "req_id=\"" + reqId + "\" and is_req=0";
                            Cursor answersCursor = db.query(DBHelper.TABLE_REQUESTS_NAME, new String[]{"req_id"}, answersSelection, null, null, null, null);
                            if (answersCursor != null && answersCursor.getCount() == 0) {
                                if (!requestIds.contains(reqId)) {
                                    requestIds.add(reqId);
                                }
                            }
                            answersCursor.close();
                        }
                    }
                } while (c.moveToNext());
            }
            c.close();
        }
        return requestIds;
    }

    private void markRequestSent(String reqId) {
        ContentValues cv = new ContentValues();
        cv.put("sent", 1);
        db.update(DBHelper.TABLE_REQUESTS_NAME, cv, "req_id = ?", new String[]{reqId});
    }

    private void readRemoteAnswer(String requestId) {
        String filename = requestId;
        try {
            String url = sharedPreferences.getString("androidExchangeOutDirPref", "");
            if (url.charAt(url.length() - 1) != '/') {
                url = url.concat("/");
            }
            SmbFile smbFile = new SmbFile("smb://" + url + filename + ".csv");
            if (!smbFile.exists()) {
                return;
            }
            SmbFileInputStream is = new SmbFileInputStream(smbFile);
            StringBuffer fileContent = new StringBuffer("");
            byte[] buffer = new byte[8192];
            while (is.read(buffer) > 0)
                fileContent.append(new String(buffer, "windows-1251"));
            is.close();

            Request requestSingle = dbHelper.getSingleRequestByRequestId(requestId);

            ContentValues cv = new ContentValues();
            cv.put("is_req", 0);
            cv.put("req_id", filename);
            cv.put("date", Request.getDateFormat().format(new Date(smbFile.lastModified())));
            cv.put("req", fileContent.toString());
            cv.put("sent", 0);
            db.insert(DBHelper.TABLE_REQUESTS_NAME, null, cv);

            notifUtil.showResponseNotification(requestSingle);
        } catch (Exception e) {
            log.error("Error reading remote answer.", e);
        }

    }

    /*class FilesUpdateThread implements Runnable {

        public FilesUpdateThread() {
        }

        public void run() {
            while (true) {
                try {
                    //updating public files from remote db
                    updatePubFiles();
                    Map<String, List<Request>> requests = readUnsentRequests();

                    //sending unsent requests and marking as sent
                    for (String reqId : requests.keySet()) {
                        List<Request> list = requests.get(reqId);
                        //createRemoteRequest(reqId, requests.get(reqId));
                        boolean success = requestDao.batchAddRequests(list);
                        if (success) {
                            markRequestSent(reqId);

                            if (list.size() > 0) { // just make sure, though else case should not be possible
                                Request reqInfo = list.get(0);
                                notifUtil.showRequestSentNotification(reqInfo);
                            }
                        }
                    }

                    // send unsent requests
                    util.sendRequests();

                    // вычитать отправленные заявки на сегодня без ответа
                    ArrayList<String> sentRequestIds = readSentRequestIdsWithoutAnswer();
                    // для каждой поискать ответ в удалённой бд
                    // и если есть, записать его в базу и вывести оповещение
                    for (String reqId : sentRequestIds) {
                        readRemoteAnswer(reqId);
                    }

                    TimeUnit.SECONDS.sleep(30);


                } catch (Exception e) {
                    log.error("Error in file updating thread", e);
                    try {
                        TimeUnit.SECONDS.sleep(30);
                    } catch (InterruptedException e1) {
                        log.error("Error while putting service thread to sleep", e1);
                    }
                }
            }
        }

    }*/

}
