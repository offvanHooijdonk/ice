package by.ingman.ice.retailerrequest.v2.helpers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;

import by.ingman.ice.retailerrequest.v2.ErrorMessageActivity;
import by.ingman.ice.retailerrequest.v2.MainActivity;
import by.ingman.ice.retailerrequest.v2.R;
import by.ingman.ice.retailerrequest.v2.structure.Answer;
import by.ingman.ice.retailerrequest.v2.structure.Order;

/**
 * Created by Yahor_Fralou on 7/6/2015.
 */
public class NotificationsUtil {
    public static final int NOTIF_UPDATE_PROGRESS_CONTR_AGENTS_ID = 10;
    public static final int NOTIF_UPDATE_PROGRESS_PRODUCTS_ID = 11;
    public static final int NOTIF_UPDATE_PROGRESS_DEBTS_ID = 12;
    public static final int NOTIF_UPDATE_PROGRESS_ORDERS_ID = 13;
    private static final int NOTIF_DATA_UPDATE_ERROR_ID = 2;
    private static final int NOTIF_ORDER_SENT_ID = 3;
    private static final int NOTIF_ORDER_ACCEPT_ID = 4;

    private Context ctx;
    private NotificationManager notificationManager;

    public NotificationsUtil(Context context) {
        this.ctx = context;
    }

    public void showUpdateProgressNotification(int id) {
        String dataName = "";
        switch (id) {
            case NOTIF_UPDATE_PROGRESS_CONTR_AGENTS_ID : dataName = ctx.getString(R.string.notif_data_contragents);
                break;
            case NOTIF_UPDATE_PROGRESS_PRODUCTS_ID : dataName = ctx.getString(R.string.notif_data_products);
                break;
            case NOTIF_UPDATE_PROGRESS_DEBTS_ID : dataName = ctx.getString(R.string.notif_data_debts);
                break;
            case NOTIF_UPDATE_PROGRESS_ORDERS_ID : dataName = ctx.getString(R.string.notif_data_orders);
                break;
        }
        String title = ctx.getResources().getString(R.string.notif_update_data_title);
        String message = ctx.getResources().getString(R.string.notif_update_data_message, dataName);

        Notification.Builder builder = setupCommonNotification(title, message, false)
                .setSmallIcon(android.R.drawable.stat_sys_download).setOngoing(true)
                .setProgress(0, 0, true).setUsesChronometer(true);

        getNotificationManager().notify(id, builder.build());
    }

    public void dismissUpdateProgressNotification(int id) {
        getNotificationManager().cancel(id);
    }

    public void dismissAllUpdateProgressNotifications() {
        getNotificationManager().cancel(NOTIF_UPDATE_PROGRESS_CONTR_AGENTS_ID);
        getNotificationManager().cancel(NOTIF_UPDATE_PROGRESS_PRODUCTS_ID);
        getNotificationManager().cancel(NOTIF_UPDATE_PROGRESS_DEBTS_ID);
        getNotificationManager().cancel(NOTIF_UPDATE_PROGRESS_ORDERS_ID);
    }

    // TODO set different ID to error notification for data and orders
    public void showErrorNotification(String title, String message, Throwable th) {
        Notification.Builder builder = setupCommonNotification(title, message, true)
                .setSmallIcon(android.R.drawable.stat_notify_error);

        if (th != null) {
            builder.setContentIntent(createErrorIntent(th));
        }

        getNotificationManager().notify(NOTIF_DATA_UPDATE_ERROR_ID, builder.build());
    }

    public void showOrderSentNotification(Order order) {
        String tag = order.getOrderId();
        String title = ctx.getResources().getString(R.string.notif_request_success_title);
        String message = ctx.getResources().getString(R.string.notif_request_success_message, order.getContrAgentName());

        Notification.BigTextStyle bigTextStyle = new Notification.BigTextStyle().setBigContentTitle(title).bigText(message);

        Notification.Builder builder = setupCommonNotification(title, message, true).setStyle(bigTextStyle)
                .setSmallIcon(android.R.drawable.stat_sys_upload_done);

        getNotificationManager().notify(tag, NOTIF_ORDER_SENT_ID, builder.build());
    }

    public void showOrderNotification(Order order, Answer answer) {
        String tag = order.getOrderId();
        String title = ctx.getResources().getString(R.string.notif_response_title, order.getContrAgentName());
        String message = ctx.getResources().getString(R.string.notif_response_text, order.getContrAgentName(), answer.getDescription());

        Notification.BigTextStyle bigTextStyle = new Notification.BigTextStyle().setBigContentTitle(title).bigText(message);

        Notification.Builder builder = setupCommonNotification(title, message, true).setStyle(bigTextStyle)
                .setSmallIcon(R.drawable.ic_action_accept);

        getNotificationManager().notify(tag, NOTIF_ORDER_ACCEPT_ID, builder.build());
    }

    private Notification.Builder setupCommonNotification(String title, String message, boolean autoCancel) {
        return new Notification.Builder(ctx).setTicker(title).setContentTitle(title).setContentText(message)
                .setContentIntent(createMainIntent())
                .setLargeIcon(BitmapFactory.decodeResource(ctx.getResources(), R.drawable.ic_launcher)).setAutoCancel(autoCancel);
    }

    public void dismissUpdateErrorNotifications() {
        getNotificationManager().cancel(NOTIF_DATA_UPDATE_ERROR_ID);
    }

    private NotificationManager getNotificationManager() {
        if (notificationManager == null) {
            notificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        }

        return notificationManager;
    }

    private PendingIntent createMainIntent() {
        return PendingIntent.getActivity(ctx, 0, new Intent(ctx, MainActivity.class), 0);
    }

    private PendingIntent createErrorIntent(Throwable th) {
        Intent intent = new Intent(ctx, ErrorMessageActivity.class);
        StringBuilder str = new StringBuilder(th.toString()).append("\n");

        for (StackTraceElement el : th.getStackTrace()) {
            str.append(el.toString()).append("\n");
        }
        intent.putExtra(ErrorMessageActivity.EXTRA_ERROR_MESSAGE, str.toString());

        return PendingIntent.getActivity(ctx, 1, intent, 0);
    }
}
