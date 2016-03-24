package by.ingman.ice.retailerrequest.v2.helpers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;

import java.util.Calendar;
import java.util.Date;

import by.ingman.ice.retailerrequest.v2.remote.exchange.ExchangeIntends;

/**
 * Created by Yahor_Fralou on 7/13/2015.
 */
public class AlarmHelper {
    private static AlarmManager alarmManager;

    public static void createExchangeAlarm(Context ctx) {
        int intervalSec = PreferenceHelper.Settings.getDataUpdateInterval(ctx);
        Calendar startTime = Calendar.getInstance();
        startTime.add(Calendar.SECOND, intervalSec);

        createAlarm(ctx, startTime.getTime(), ExchangeIntends.getExchangeStartPendingIntent(ctx));
    }

    public static void createAlarm(Context ctx, Date date, PendingIntent pendingIntent) {
        getAlarmManager(ctx).set(AlarmManager.RTC_WAKEUP, date.getTime(), pendingIntent);
    }

    public static void cancelExchangeAlarm(Context ctx) {
        getAlarmManager(ctx).cancel(ExchangeIntends.getExchangeStartPendingIntent(ctx));
    }

    private static AlarmManager getAlarmManager(Context ctx) {
        if (alarmManager == null) {
            alarmManager = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        }

        return alarmManager;
    }

}
