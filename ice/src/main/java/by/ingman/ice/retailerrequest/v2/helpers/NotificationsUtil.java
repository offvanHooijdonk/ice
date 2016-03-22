package by.ingman.ice.retailerrequest.v2.helpers;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.BitmapFactory;

import by.ingman.ice.retailerrequest.v2.R;
import by.ingman.ice.retailerrequest.v2.structure.Answer;
import by.ingman.ice.retailerrequest.v2.structure.Order;

/**
 * Created by Yahor_Fralou on 7/6/2015.
 */
public class NotificationsUtil {
    private static final int NOTIF_DATA_UPDATE_PROGRESS_ID = 0;
    private static final int NOTIF_DATA_UPDATE_ERROR_ID = 2;
    private static final int NOTIF_ORDER_SENT_ID = 3;
    private static final int NOTIF_ORDER_ACCEPT_ID = 4;

    private static final String NOTIF_ERROR_TAG = "error_notification";

    private Context ctx;
    private NotificationManager notificationManager;

    public NotificationsUtil(Context context) {
        this.ctx = context;
    }

    public void showUpdateProgressNotification(String dataName) {
        String tag = dataName;

        String title = ctx.getResources().getString(R.string.notif_update_data_title);
        String message = ctx.getResources().getString(R.string.notif_update_data_message, dataName);

        Notification.Builder builder = setupCommonNotification(title, message, false)
                .setSmallIcon(android.R.drawable.stat_sys_download).setOngoing(true)
                .setProgress(0, 0, true).setUsesChronometer(true);

        getNotificationManager().notify(tag, NOTIF_DATA_UPDATE_PROGRESS_ID, builder.build());
    }

    public void dismissUpdateProgressNotification(String dataName) {
        String tag = dataName;
        getNotificationManager().cancel(tag, NOTIF_DATA_UPDATE_PROGRESS_ID);
    }

    public void dismissAllUpdateProgressNotifications() {
        getNotificationManager().cancel(NOTIF_DATA_UPDATE_PROGRESS_ID);
    }

    public void showErrorNotification(String title, String message) {
        Notification.Builder builder = setupCommonNotification(title, message, true)
                .setSmallIcon(android.R.drawable.stat_notify_error);

        getNotificationManager().notify(NOTIF_ERROR_TAG, NOTIF_DATA_UPDATE_ERROR_ID, builder.build());
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
                .setLargeIcon(BitmapFactory.decodeResource(ctx.getResources(), R.drawable.ic_launcher)).setAutoCancel(autoCancel);
    }

    public void dismissFileErrorNotifications() {
        getNotificationManager().cancel(NOTIF_DATA_UPDATE_PROGRESS_ID);
    }

    private NotificationManager getNotificationManager() {
        if (notificationManager == null) {
            notificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        }

        return notificationManager;
    }
}
