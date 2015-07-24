package by.ingman.ice.retailerrequest.v2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import by.ingman.ice.retailerrequest.v2.helpers.AlarmHelper;

/**
 * Created by Yahor_Fralou on 7/24/2015.
 */
public class BootBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        AlarmHelper.createExchangeAlarm(context);
    }
}
