package by.ingman.ice.retailerrequest.v2;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.widget.Toast;

import by.ingman.ice.retailerrequest.v2.helpers.DBHelper;
import by.ingman.ice.retailerrequest.v2.helpers.NotificationsUtil;
import by.ingman.ice.retailerrequest.v2.helpers.StaticFileNames;
import by.ingman.ice.retailerrequest.v2.structure.Request;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;
import jcifs.smb.SmbFileOutputStream;

import java.io.*;
import java.net.MalformedURLException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: Администратор
 * Date: 07.05.13
 * Time: 21:32
 * To change this template use File | Settings | File Templates.
 */
public class FilesUpdateService extends Service {

    ExecutorService executorService;
    NotificationManager notificationManager;
    int k = 0;
    SharedPreferences sharedPreferences;
    private Context that;

    DBHelper dbHelper;
    SQLiteDatabase db;
    private NotificationsUtil notifUtil;

    Date debtDate = null, restsDate = null, clientsDate = null;

    @Override
    public IBinder onBind(Intent intent) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void onCreate() {
        super.onCreate();
        this.that = this;
        notifUtil = new NotificationsUtil(that);
        executorService = Executors.newFixedThreadPool(1);
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        dbHelper = new DBHelper(this);
        db = dbHelper.getWritableDatabase();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        FilesUpdateThread filesUpdateThread = new FilesUpdateThread();
        executorService.execute(filesUpdateThread);
        return super.onStartCommand(intent, flags, startId);
    }


    private Date getDateForFilename(String filename) {
        if (filename.equals(StaticFileNames.RESTS_CSV_SD)){
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
            for (String filename : StaticFileNames.getFilenamesArray()) {
                boolean doUpdate = false;
                String url = sharedPreferences.getString("androidExchangePubDirPref", "");
                if (url.charAt(url.length()-1) != '/') {
                    url = url.concat("/");
                }
                Date remoteDate = null;
                SmbFile smbFile = new SmbFile("smb://" + url +  filename);
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
            notifUtil.showErrorNotification("Ошибка при загрузке данных", "Ошибка загрузки файла данных.");

            //Toast.makeText(that, "Ошибка при загрузке данных. " + e.toString(), Toast.LENGTH_LONG).show();
            if (enabledNotifications()) {
                notifUtil.dismissFileProgressNotification(StaticFileNames.DEBTS_CSV_SD);
                notifUtil.dismissFileProgressNotification(StaticFileNames.RESTS_CSV_SD);
                notifUtil.dismissFileProgressNotification(StaticFileNames.CLIENTS_CSV_SD);
            }
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

        return  descr;
    }

    private void sendNotification(String title, String str) {
        // TODO move all notifications to NotificationsUtil
        Notification notif = new Notification(R.drawable.ic_launcher, "\"Ингман\", оповещение",
                System.currentTimeMillis());

        notif.setLatestEventInfo(this, title, str,
                PendingIntent.getActivity(that, 0, new Intent(), 0));

        notif.defaults = Notification.DEFAULT_ALL;
        notif.flags |= Notification.FLAG_AUTO_CANCEL;
        // отправляем
        notificationManager.notify(k++, notif);
    }

    private HashMap<String, String> readUnsentRequests() {
        String selection = "sent=0 and is_req>0";
        Cursor c = db.query(DBHelper.TABLE_REQUESTS_NAME, null, selection, null, null, null, null);
        String reqId = "";
        String req = "";
        HashMap<String, String> requests = new HashMap<String, String>();

        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    for (String columnName : c.getColumnNames()) {
                        if (columnName.equals("req")) {
                            req = c.getString(c.getColumnIndex(columnName));
                        }
                        if (columnName.equals("req_id")) {
                            reqId = c.getString(c.getColumnIndex(columnName));
                        }
                    }
                    if (requests.containsKey(reqId)) {
                        String banch = requests.get(reqId);
                        banch = banch.concat("\n");
                        banch = banch.concat(req);
                        requests.put(reqId, banch);
                    } else {
                        requests.put(reqId, req);
                    }
                } while (c.moveToNext());
            }
            c.close();
        }

        return requests;
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
                            if (answersCursor!= null && answersCursor.getCount() == 0) {
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

    private boolean createRemoteRequest(String reqId, String req) throws MalformedURLException {
        String url = sharedPreferences.getString("androidExchangeNewDirPref", "");
        if (url.charAt(url.length()-1) != '/') {
            url = url.concat("/");
        }

        SmbFile smbFile = new SmbFile("smb://" + url +  reqId + ".tmp");
        SmbFile csvFile = new SmbFile("smb://" + url +  reqId + ".csv");


        try {
            if (!smbFile.exists()) {
                smbFile.createNewFile();
            }
            SmbFileOutputStream smbfos = new SmbFileOutputStream(smbFile);
            smbfos.write(req.getBytes("windows-1251"));

            smbfos.close();
            smbFile.copyTo(csvFile);
            if (smbFile.getContentLength() == 0 || csvFile.getContentLength() != smbFile.getContentLength()) {
                throw new IOException("file creating error");
            }
            smbFile.delete();
        } catch (Exception e) {
            try {
                smbFile.delete();
                csvFile.delete();
            } catch (SmbException se) {
            }
            return false;
        }
        /*try {
            SmbFile smbFile = new SmbFile(reqId + ".csv");
            if (!smbFile.exists()) {
                smbFile.createNewFile();
            }
            SmbFileOutputStream smbfos = new SmbFileOutputStream(smbFile);
            smbfos.write(req.getBytes("windows-1251"));

            smbfos.close();

            SmbFile newSmbFile = new SmbFile("smb://" + url +  reqId + ".csv");
            newSmbFile.createNewFile();
            smbFile.copyTo(newSmbFile);
            smbFile.delete();

        } catch (Exception e) {
            //sendNotification("Отправка невозможна");
            return false;
        }*/
        sendNotification("Заявка отправлена", "");
        return true;
    }

    private void readRemoteAnswer(String filename) {
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

            ContentValues cv = new ContentValues();
            cv.put("is_req", 0);
            cv.put("req_id", filename);
            cv.put("date", Request.getDateFormat().format(new Date(smbFile.lastModified())));
            cv.put("req", fileContent.toString());
            cv.put("sent", 0);
            db.insert(DBHelper.TABLE_REQUESTS_NAME, null, cv);
            sendNotification("Получен ответ на заявку", "");
        } catch (Exception e) {
            //writeFileSD("errorService", new Date() + "\n\r" + e.toString());
        }

    }

    class FilesUpdateThread implements Runnable {

        public FilesUpdateThread() {
        }

        public void run() {
            while (true) {
                try {
                    //updating public files from remote db
                    updatePubFiles();
                    ///sendNotification("check pub");


                    //reading unsent requests
                    HashMap<String, String> requests = readUnsentRequests();
                    /*if (requests.size() > 0) {
                        sendNotification(requests.size() + " readed");
                    }*/

                    //sending unsent requests and marking as sent
                    for (String reqId : requests.keySet()) {
                        boolean result = createRemoteRequest(reqId, requests.get(reqId));
                        if (result) {
                            markRequestSent(reqId);
                        }
                    }

                    //!todo read answers
                    // вычитать отправленные заявки на сегодня без ответа
                    ArrayList<String> sentRequestIds = readSentRequestIdsWithoutAnswer();
                    //writeFileSD("ssstest","swa " + sentRequestIds.size() + sentRequestIds.get(0) + "    " + sentRequestIds.get(1));
                    // для каждой поискать ответ в удалённой бд
                    // и если есть, записать его в базу и вывести оповещение
                    for (String reqId : sentRequestIds) {
                        readRemoteAnswer(reqId);
                    }

                    //int secSleep = sharedPreferences.getInt("androidUpdateSecPref", 30);
                    TimeUnit.SECONDS.sleep(30);


                } catch (Exception e) {
                    //writeFileSD("error", e.toString());
                    try {
                        //int secSleep = sharedPreferences.getInt("androidUpdateSecPref", 30);
                        TimeUnit.SECONDS.sleep(30);
                    } catch (InterruptedException e1) {
                        //writeFileSD("errorService", new Date() + "\n\r" + e.toString());
                    }
                }
            }
        }


        void stop() {
        }
    }

    private void writeFileSD(String name, String data) {
        // проверяем доступность SD
        if (!Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            return;
        }
        // получаем путь к SD
        File sdPath = Environment.getExternalStorageDirectory();
        // добавляем свой каталог к пути
        sdPath = new File(sdPath.getAbsolutePath() + "/");// + DIR_SD);
        // формируем объект File, который содержит путь к файлу
        File sdFile = new File(sdPath, name + ".csv");
        try {
            if (!sdFile.exists()) {
                sdFile.createNewFile();
            }
            InputStreamReader isr = new InputStreamReader(new FileInputStream(sdFile), "CP-1251");
            BufferedReader br = new BufferedReader(isr);
            String string;
            String res = "";
            // читаем содержимое
            while ((string = br.readLine()) != null) {
                res = res.concat(string);
            }
            isr.close();
            br.close();
            // открываем поток для записи
            BufferedWriter bw = new BufferedWriter(new FileWriter(sdFile));
            // пишем данные
            bw.write(res);
            bw.newLine();
            bw.write(data);
            // закрываем поток
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
