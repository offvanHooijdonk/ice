package by.ingman.ice.retailerrequest.v2.helpers;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.BitmapFactory;

import by.ingman.ice.retailerrequest.v2.R;
import by.ingman.ice.retailerrequest.v2.structure.Request;

/**
 * Created by Yahor_Fralou on 7/6/2015.
 */
public class NotificationsUtil {
    private static final int NOTIF_PROGRESS_ID = 0;
    private static final int NOTIF_COMPLETED_ID = 1;
    private static final int NOTIF_ERROR_ID = 2;
    private static final int NOTIF_REQUEST_SENT = 3;

    private static final String NOTIF_ERROR_TAG = "error_notification";

    private Context ctx;
    private NotificationManager notificationManager;

    public NotificationsUtil(Context context) {
        this.ctx = context;
    }

    public void showFileProgressNotification(String title, String message, String fileName) {
        String tag = fileName;

        Notification.Builder builder = new Notification.Builder(ctx).setTicker(title).setContentTitle(title).setContentText(message)
                .setSmallIcon(android.R.drawable.stat_sys_download).setLargeIcon(BitmapFactory.decodeResource(ctx.getResources(), R.drawable.ic_launcher))
                .setOngoing(true).setAutoCancel(false).setProgress(0, 0, true).setUsesChronometer(true);

        getNotificationManager().notify(tag, NOTIF_PROGRESS_ID, builder.build());
    }

    public void dismissFileProgressNotification(String fileName) {
        String tag = fileName;
        getNotificationManager().cancel(tag, NOTIF_PROGRESS_ID);
    }

    public void showFileCompletedNotification(String title, String message, String fileName) {
        String tag = fileName;

        Notification.Builder builder = new Notification.Builder(ctx).setTicker(title).setContentTitle(title).setContentText(message)
                .setSmallIcon(android.R.drawable.stat_sys_download_done).setLargeIcon(BitmapFactory.decodeResource(ctx.getResources(), R.drawable.ic_launcher))
                .setAutoCancel(true);

        getNotificationManager().notify(tag, NOTIF_COMPLETED_ID, builder.build());
    }

    public void showErrorNotification(String title, String message) {
        Notification.Builder builder = new Notification.Builder(ctx).setTicker(title).setContentTitle(title).setContentText(message)
                .setSmallIcon(android.R.drawable.stat_notify_error).setLargeIcon(BitmapFactory.decodeResource(ctx.getResources(), R
                        .drawable.ic_launcher))
                .setAutoCancel(true);

        getNotificationManager().notify(NOTIF_ERROR_TAG, NOTIF_ERROR_ID, builder.build());
    }

    public void showRequestSentNotification(Request request) {
        String tag = request.getId();
        String title = ctx.getResources().getString(R.string.notif_request_success_title);
        String message = ctx.getResources().getString(R.string.notif_request_success_message, request.getContrAgentName());
        Notification.Builder builder = new Notification.Builder(ctx).setTicker(title).setContentTitle(title).setContentText(message)
                .setSmallIcon(R.drawable.ic_action_accept).setLargeIcon(BitmapFactory.decodeResource(ctx.getResources(), R.drawable.ic_launcher))
                .setAutoCancel(true);

        getNotificationManager().notify(tag, NOTIF_REQUEST_SENT, builder.build());
    }

    public void dismissFileErrorNotifications() {
        getNotificationManager().cancel(NOTIF_PROGRESS_ID);
    }

    private NotificationManager getNotificationManager() {
        if (notificationManager == null) {
            notificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        }

        return notificationManager;
    }
}
