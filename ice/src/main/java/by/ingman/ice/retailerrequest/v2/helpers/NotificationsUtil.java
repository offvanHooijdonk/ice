package by.ingman.ice.retailerrequest.v2.helpers;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.BitmapFactory;

import by.ingman.ice.retailerrequest.v2.R;

/**
 * Created by Yahor_Fralou on 7/6/2015.
 */
public class NotificationsUtil {
    private static final int NOTIF_PROGRESS_ID = 0;
    private static final int NOTIF_COMPLETED_ID = 1;

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

    private NotificationManager getNotificationManager() {
        if (notificationManager == null) {
            notificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        }

        return notificationManager;
    }
}
