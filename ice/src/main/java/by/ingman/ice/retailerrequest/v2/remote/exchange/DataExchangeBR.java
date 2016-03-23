package by.ingman.ice.retailerrequest.v2.remote.exchange;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import by.ingman.ice.retailerrequest.v2.R;
import by.ingman.ice.retailerrequest.v2.helpers.AlarmHelper;
import by.ingman.ice.retailerrequest.v2.helpers.NotificationsUtil;

/**
 * Created by Yahor_Fralou on 7/13/2015.
 */
public class DataExchangeBR extends BroadcastReceiver {
    public static final String ACTION_START_EXCHANGE = "action_start_exchange";

    @Override
    public void onReceive(Context ctx, Intent intent) {
        String action = intent.getAction();
        if (ACTION_START_EXCHANGE.equalsIgnoreCase(action)) {
            try {
                ExchangeUtil util = new ExchangeUtil(ctx);

                try {
                    util.sendRequests();
                } catch (Exception e) {
                    NotificationsUtil notificationsUtil = new NotificationsUtil(ctx);
                    notificationsUtil.showErrorNotification(ctx.getString(R.string.notif_error_orders_title), ctx.getString(R.string.notif_error_orders_updating), e);
                }
            } finally {
                AlarmHelper.createExchangeAlarm(ctx);
            }
        }
    }

    public static IntentFilter getIntentFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(DataExchangeBR.ACTION_START_EXCHANGE);

        return filter;
    }
}
