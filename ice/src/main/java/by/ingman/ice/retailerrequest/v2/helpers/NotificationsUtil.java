package by.ingman.ice.retailerrequest.v2.helpers;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.BitmapFactory;

import by.ingman.ice.retailerrequest.v2.R;
import by.ingman.ice.retailerrequest.v2.structure.Order;

/**
 * Created by Yahor_Fralou on 7/6/2015.
 */
public class NotificationsUtil {
    private static final int NOTIF_FILE_UPLOAD_PROGRESS_ID = 0;
    private static final int NOTIF_FILE_UPLOAD_COMPLETED_ID = 1;
    private static final int NOTIF_FILE_UPLOAD_ERROR_ID = 2;
    private static final int NOTIF_REQUEST_SENT_ID = 3;
    private static final int NOTIF_RESPONSE_ACCEPT_ID = 4;

    private static final String NOTIF_ERROR_TAG = "error_notification";

    private Context ctx;
    private NotificationManager notificationManager;

    public NotificationsUtil(Context context) {
        this.ctx = context;
    }

    public void showFileProgressNotification(String title, String message, String fileName) {
        String tag = fileName;

        Notification.Builder builder = setupCommonNotification(title, message, false)
                .setSmallIcon(android.R.drawable.stat_sys_download).setOngoing(true)
                .setProgress(0, 0, true).setUsesChronometer(true);

        getNotificationManager().notify(tag, NOTIF_FILE_UPLOAD_PROGRESS_ID, builder.build());
    }

    public void dismissFileProgressNotification(String fileName) {
        String tag = fileName;
        getNotificationManager().cancel(tag, NOTIF_FILE_UPLOAD_PROGRESS_ID);
    }

    public void dismissAllFileProgressNotifications() {
        getNotificationManager().cancel(NOTIF_FILE_UPLOAD_PROGRESS_ID);
    }

    public void showFileCompletedNotification(String title, String message, String fileName) {
        String tag = fileName;

        Notification.Builder builder = setupCommonNotification(title, message, true)
                .setSmallIcon(android.R.drawable.stat_sys_download_done);

        getNotificationManager().notify(tag, NOTIF_FILE_UPLOAD_COMPLETED_ID, builder.build());
    }

    public void showErrorNotification(String title, String message) {
        Notification.Builder builder = setupCommonNotification(title, message, true)
                .setSmallIcon(android.R.drawable.stat_notify_error);

        getNotificationManager().notify(NOTIF_ERROR_TAG, NOTIF_FILE_UPLOAD_ERROR_ID, builder.build());
    }

    public void showRequestSentNotification(Order order) {
        String tag = order.getId();
        String title = ctx.getResources().getString(R.string.notif_request_success_title);
        String message = ctx.getResources().getString(R.string.notif_request_success_message, order.getContrAgentName());

        Notification.BigTextStyle bigTextStyle = new Notification.BigTextStyle().setBigContentTitle(title).bigText(message);

        Notification.Builder builder = setupCommonNotification(title, message, true).setStyle(bigTextStyle)
                .setSmallIcon(android.R.drawable.stat_sys_upload_done);

        getNotificationManager().notify(tag, NOTIF_REQUEST_SENT_ID, builder.build());
    }

    public void showResponseNotification(Order order) {
        String tag = order.getId();
        String title = ctx.getResources().getString(R.string.notif_response_title);
        String message = ctx.getResources().getString(R.string.notif_response_text, order.getContrAgentName());

        Notification.BigTextStyle bigTextStyle = new Notification.BigTextStyle().setBigContentTitle(title).bigText(message);

        Notification.Builder builder = setupCommonNotification(title, message, true).setStyle(bigTextStyle)
                .setSmallIcon(R.drawable.ic_action_accept);

        getNotificationManager().notify(tag, NOTIF_RESPONSE_ACCEPT_ID, builder.build());
    }

    private Notification.Builder setupCommonNotification(String title, String message, boolean autoCancel) {
        return new Notification.Builder(ctx).setTicker(title).setContentTitle(title).setContentText(message)
                .setLargeIcon(BitmapFactory.decodeResource(ctx.getResources(), R.drawable.ic_launcher)).setAutoCancel(autoCancel);
    }

    public void dismissFileErrorNotifications() {
        getNotificationManager().cancel(NOTIF_FILE_UPLOAD_PROGRESS_ID);
    }

    private NotificationManager getNotificationManager() {
        if (notificationManager == null) {
            notificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        }

        return notificationManager;
    }
}
