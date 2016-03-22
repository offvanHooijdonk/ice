package by.ingman.ice.retailerrequest.v2.remote.exchange;

import android.app.Activity;
import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.Parcelable;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;

import org.apache.log4j.Logger;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import by.ingman.ice.retailerrequest.v2.R;
import by.ingman.ice.retailerrequest.v2.helpers.AlarmHelper;
import by.ingman.ice.retailerrequest.v2.helpers.ConfigureLog4J;
import by.ingman.ice.retailerrequest.v2.helpers.NotificationsUtil;
import by.ingman.ice.retailerrequest.v2.helpers.StaticFileNames;
import by.ingman.ice.retailerrequest.v2.local.dao.ContrAgentLocalDao;
import by.ingman.ice.retailerrequest.v2.local.dao.DebtsLocalDao;
import by.ingman.ice.retailerrequest.v2.local.dao.OrderLocalDao;
import by.ingman.ice.retailerrequest.v2.local.dao.ProductLocalDao;
import by.ingman.ice.retailerrequest.v2.remote.dao.ContrAgentDao;
import by.ingman.ice.retailerrequest.v2.remote.dao.DebtsDao;
import by.ingman.ice.retailerrequest.v2.remote.dao.OrderDao;
import by.ingman.ice.retailerrequest.v2.remote.dao.ProductDao;
import by.ingman.ice.retailerrequest.v2.structure.Answer;
import by.ingman.ice.retailerrequest.v2.structure.ContrAgent;
import by.ingman.ice.retailerrequest.v2.structure.Debt;
import by.ingman.ice.retailerrequest.v2.structure.Order;
import by.ingman.ice.retailerrequest.v2.structure.Product;

/**
 * Created with IntelliJ IDEA.
 * User: Администратор
 * Date: 07.05.13
 * Time: 21:32
 * To change this template use File | Settings | File Templates.
 */
public class ExchangeDataService extends IntentService {
    public static final String FLAG_UPDATE_IN_PROGRESS = "FLAG_UPDATE_IN_PROGRESS";

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

    OrderLocalDao orderLocalDao;
    private NotificationsUtil notifUtil;
    private OrderDao orderDao;

    Date debtDate = null, restsDate = null, clientsDate = null;

    private ExchangeUtil util;
    private ResultReceiver receiver = null;

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

        setProgressFlag(true);

        Parcelable p = intent.getParcelableExtra(EXTRA_RECEIVER);/*Extras().containsKey(EXTRA_RECEIVER) ? intent
                .getExtras()
                .getParcelable(EXTRA_RECEIVER) : null;*/

        if (p == null) {
            receiver = null;
        } else {
            receiver = (ResultReceiver) p;
        }

        doExchangeData();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.that = this;

        notifUtil = new NotificationsUtil(that);
        orderDao = new OrderDao(that);
        executorService = Executors.newFixedThreadPool(1);
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        orderLocalDao = new OrderLocalDao(that);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(that);
        util = new ExchangeUtil(that);
    }

    @Override
    public void onDestroy() {
        notifUtil.dismissAllUpdateProgressNotifications();

        super.onDestroy();
    }

    private void doExchangeData() {
        boolean success = true;
        try {
            // updating public files from remote db
            updateAllData();

            // send unsent requests
            util.sendRequests();

            // read from localDB today unanswered requests
            List<String> sentOrderIds = orderLocalDao.getSentOrdersIdsWithoutAnswer();

            // check for an answer in remote DB and show notification for those having an answer
            for (String orderId : sentOrderIds) {
                readRemoteAnswer(orderId);
            }
        } catch (Exception e) {
            log.error("Error in file updating service", e);
            success = false;
            if (isResponseToSend()) {
                receiver.send(Activity.RESULT_CANCELED, null);
            }
        } finally {
            setProgressFlag(false);
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
        switch (filename) {
            case StaticFileNames.RESTS_CSV_SD:
                return restsDate;
            case StaticFileNames.CLIENTS_CSV_SD:
                return clientsDate;
            case StaticFileNames.DEBTS_CSV_SD:
                return debtDate;
        }
        return null;
    }

    /**
     * update lists of clients+, rests+, debts+
     * @throws Exception
     */
    private void updateAllData() throws Exception {
        notifUtil.dismissFileErrorNotifications();

        long timeUpdateStart = new Date().getTime();
        long timeLastUpdate = sharedPreferences.getLong("lastUpdatedDate", 0);
        Date dateLastUpdate = new Date(timeLastUpdate);
        try {
            updateProducts(dateLastUpdate);

            updateContrAgents(dateLastUpdate);

            updateDebts(dateLastUpdate);

            sharedPreferences.edit().putLong("lastUpdateDate", timeUpdateStart).apply();
        } catch (Exception e) {
            sharedPreferences.edit().putLong("lastUpdateDate", timeLastUpdate).apply();

            log.error("Error loading data from remote", e);
            notifUtil.showErrorNotification("Ошибка загрузке данных", "Ошибка загрузки данных.");
            notifUtil.dismissAllUpdateProgressNotifications();

            throw new Exception(e);
        }
    }

    private void updateProducts(Date dateLastUpdate) {
        ProductDao productDao = new ProductDao(that);
        Long lastUnloadDate = productDao.getUnloadDate();

        if (lastUnloadDate != null && lastUnloadDate > dateLastUpdate.getTime()) {
            if (enabledNotifications()) {
                notifUtil.showUpdateProgressNotification(that.getString(R.string.notif_data_products));
            }

            List<Product> products = productDao.getAll();
            ProductLocalDao productLocalDao = new ProductLocalDao(that);
            productLocalDao.updateProducts(products);
            notifUtil.dismissUpdateProgressNotification(that.getString(R.string.notif_data_products));
        }// otherwise data might be in unload process or nothing new
    }

    private void updateContrAgents(Date dateLastUpdate) {
        ContrAgentDao contrAgentDao = new ContrAgentDao(that);

        Long lastUnloadDate = contrAgentDao.getUnloadDate();
        if (lastUnloadDate != null && lastUnloadDate > dateLastUpdate.getTime()) {
            if (enabledNotifications()) {
                notifUtil.showUpdateProgressNotification(that.getString(R.string.notif_data_contragents));
            }
            List<ContrAgent> caList = contrAgentDao.getAll();
            ContrAgentLocalDao contrAgentLocalDao = new ContrAgentLocalDao(that);
            contrAgentLocalDao.updateContrAgents(caList);
            notifUtil.dismissUpdateProgressNotification(that.getString(R.string.notif_data_contragents));
        }// otherwise data might be in unload process or nothing new
    }

    private void updateDebts(Date dateLastUpdate) {
        DebtsDao debtsDao = new DebtsDao(that);
        Long lastUnloadDate = debtsDao.getUnloadDate();

        if (lastUnloadDate != null && lastUnloadDate > dateLastUpdate.getTime()) {
            if (enabledNotifications()) {
                notifUtil.showUpdateProgressNotification(that.getString(R.string.notif_data_debts));
            }

            List<Debt> debts = debtsDao.getDebts();
            DebtsLocalDao debtsLocalDao = new DebtsLocalDao(that);
            debtsLocalDao.updateAll(debts);
            notifUtil.dismissUpdateProgressNotification(that.getString(R.string.notif_data_debts));
        }// otherwise data might be in unload process or nothing new
    }

    public static boolean isUpdateInProgress(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx).getBoolean(FLAG_UPDATE_IN_PROGRESS, false);
    }

    public static void checkUpdateInProgress(Context ctx) throws UpdateInProgressException {
        if (isUpdateInProgress(ctx)) {
            throw new UpdateInProgressException(ctx);
        }
    }

    private void setProgressFlag(boolean inProgress) {
        sharedPreferences.edit().putBoolean(FLAG_UPDATE_IN_PROGRESS, inProgress);
    }

    private boolean enabledNotifications() {
        return sharedPreferences.getBoolean("updateNotificationsEnabled", true);
    }

    /**
     * Read orders for which we got answer (approved/not approved)
     * @param orderId
     */
    private void readRemoteAnswer(String orderId) {
        try {
            Answer answer = orderDao.findAnswer(orderId);
            if (answer == null) {
                return;
            }

            orderLocalDao.saveRemoteAnswer(answer);
            Order singleOrder = orderLocalDao.getOrderById(answer.getOrderId());

            notifUtil.showOrderNotification(singleOrder, answer);
        } catch (Exception e) {
            log.error("Error reading remote answer.", e);
        }

    }

}
