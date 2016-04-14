package by.ingman.ice.retailerrequest.v2.remote.exchange;

import android.app.Activity;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.IBinder;
import android.os.Parcelable;
import android.os.ResultReceiver;

import org.apache.log4j.Logger;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import by.ingman.ice.retailerrequest.v2.R;
import by.ingman.ice.retailerrequest.v2.helpers.AlarmHelper;
import by.ingman.ice.retailerrequest.v2.helpers.ConfigureLog4J;
import by.ingman.ice.retailerrequest.v2.helpers.NotificationsUtil;
import by.ingman.ice.retailerrequest.v2.helpers.PreferenceHelper;
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
    public static final String EXTRA_RECEIVER = "extra_receiver";

    static {
        ConfigureLog4J.configure();
    }

    private final Logger log = Logger.getLogger(ExchangeDataService.class);
    ExecutorService executorService;
    OrderLocalDao orderLocalDao;
    private Context that;
    private NotificationsUtil notifUtil;
    private OrderDao orderDao;

    private ExchangeUtil util;
    private ResultReceiver receiver = null;

    /**
     * Creates an IntentService. Invoked by your subclass's constructor.
     */
    public ExchangeDataService() {
        super("ExchangeDataService");
    }

    public static boolean isUpdateInProgress(Context ctx) {
        return PreferenceHelper.Runtime.getUpdateInProgress(ctx);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Parcelable p = intent.getParcelableExtra(EXTRA_RECEIVER);/*Extras().containsKey(EXTRA_RECEIVER) ? intent
                .getExtras()
                .getParcelable(EXTRA_RECEIVER) : null;*/

        if (p == null) {
            receiver = null;
        } else {
            receiver = (ResultReceiver) p;
        }

        if (isNetworkConnected()) {
            doExchangeData();
        } else {
            log.warn("Internet network not accessible when trying to update data!");
        }

    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.that = this;

        notifUtil = new NotificationsUtil(that);
        orderDao = new OrderDao(that);
        executorService = Executors.newFixedThreadPool(1);
        orderLocalDao = new OrderLocalDao(that);
        util = new ExchangeUtil(that);
    }

    @Override
    public void onDestroy() {
        notifUtil.dismissAllUpdateProgressNotifications();

        super.onDestroy();
    }

    private void doExchangeData() {
        try {
            // updating public files from remote db
            if (!PreferenceHelper.Runtime.getProductsInSelect(that)) {
                updateAllData();
            }

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
            notifUtil.showErrorNotification(NotificationsUtil.NOTIF_ORDERS_ERROR_ID, that.getString(R.string.notif_error_orders_title),
                    that.getString(R.string
                            .notif_error_orders_updating), e);
            if (isResponseToSend()) {
                receiver.send(Activity.RESULT_CANCELED, null);
            }
        } finally {
            AlarmHelper.createExchangeAlarm(this);
        }

        if (isResponseToSend()) {
            receiver.send(Activity.RESULT_OK, null);
        }
    }

    private boolean isResponseToSend() {
        return receiver != null;
    }

    /**
     * update lists of clients+, rests+, debts+
     *
     * @throws Exception
     */
    private void updateAllData() throws Exception {
        notifUtil.dismissUpdateErrorNotifications();

        long timeUpdateStart = new Date().getTime();
        long timeLastUpdate = PreferenceHelper.Runtime.getLastUpdateDate(that);
        try {
            updateProducts(timeLastUpdate);

            updateContrAgents(timeLastUpdate);

            updateDebts(timeLastUpdate);

            PreferenceHelper.Runtime.setLastUpdateDate(that, timeUpdateStart);
        } catch (Exception e) {
            PreferenceHelper.Runtime.setLastUpdateDate(that, timeLastUpdate);

            log.error("Error loading data from remote", e);
            notifUtil.dismissAllUpdateProgressNotifications();
            notifUtil.showErrorNotification(NotificationsUtil.NOTIF_DATA_UPDATE_ERROR_ID, that.getString(R.string.notif_error_updating),
                    that.getString(R.string
                            .notif_error_updating)
                    , e);
        } finally {
            PreferenceHelper.Runtime.setUpdateInProgress(that, false); // do here - if update started, then block Agent till all updates finished, as changes in one table often go with changes in others
        }
    }

    private void updateProducts(long dateLastUpdate) throws Exception {
        ProductDao productDao = new ProductDao(that);
        Long lastUnloadDate = productDao.getUnloadDate();

        if (lastUnloadDate != null && lastUnloadDate > dateLastUpdate) {
            PreferenceHelper.Runtime.setUpdateInProgress(that, true); // do it here in order not to block Agent work with UI
            if (PreferenceHelper.Settings.getNotificationsEnabled(that)) {
                notifUtil.showUpdateProgressNotification(NotificationsUtil.NOTIF_UPDATE_PROGRESS_PRODUCTS_ID);
            }

            List<Product> products = productDao.getAll();
            ProductLocalDao productLocalDao = new ProductLocalDao(that);
            productLocalDao.updateProducts(products);
            notifUtil.dismissUpdateProgressNotification(NotificationsUtil.NOTIF_UPDATE_PROGRESS_PRODUCTS_ID);
        }// otherwise data might be in unload process or nothing new
    }

    private void updateContrAgents(long dateLastUpdate) throws Exception {
        ContrAgentDao contrAgentDao = new ContrAgentDao(that);

        Long lastUnloadDate = contrAgentDao.getUnloadDate();
        if (lastUnloadDate != null && lastUnloadDate > dateLastUpdate) {
            PreferenceHelper.Runtime.setUpdateInProgress(that, true); // do it here in order not to block Agent work with UI
            if (PreferenceHelper.Settings.getNotificationsEnabled(that)) {
                notifUtil.showUpdateProgressNotification(NotificationsUtil.NOTIF_UPDATE_PROGRESS_CONTR_AGENTS_ID);
            }
            List<ContrAgent> caList = contrAgentDao.getAll();
            ContrAgentLocalDao contrAgentLocalDao = new ContrAgentLocalDao(that);
            contrAgentLocalDao.updateContrAgents(caList);
            notifUtil.dismissUpdateProgressNotification(NotificationsUtil.NOTIF_UPDATE_PROGRESS_CONTR_AGENTS_ID);
        }// otherwise data might be in unload process or nothing new
    }

    private void updateDebts(long dateLastUpdate) throws Exception {
        DebtsDao debtsDao = new DebtsDao(that);
        Long lastUnloadDate = debtsDao.getUnloadDate();

        if (lastUnloadDate != null && lastUnloadDate > dateLastUpdate) {
            PreferenceHelper.Runtime.setUpdateInProgress(that, true); // do it here in order not to block Agent work with UI
            if (PreferenceHelper.Settings.getNotificationsEnabled(that)) {
                notifUtil.showUpdateProgressNotification(NotificationsUtil.NOTIF_UPDATE_PROGRESS_DEBTS_ID);
            }

            List<Debt> debts = debtsDao.getDebts();
            DebtsLocalDao debtsLocalDao = new DebtsLocalDao(that);
            debtsLocalDao.updateAll(debts);
            notifUtil.dismissUpdateProgressNotification(NotificationsUtil.NOTIF_UPDATE_PROGRESS_DEBTS_ID);
        }// otherwise data might be in unload process or nothing new
    }

    /**
     * Read orders for which we got answer (approved/not approved)
     *
     * @param orderId order id
     */
    private void readRemoteAnswer(String orderId) throws Exception {
        Answer answer = orderDao.findAnswer(orderId);
        if (answer == null) {
            return;
        }

        orderLocalDao.saveRemoteAnswer(answer);
        Order singleOrder = orderLocalDao.getSingleOrderById(answer.getOrderId());

        notifUtil.showOrderNotification(singleOrder, answer);
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }

}
