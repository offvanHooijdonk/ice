package by.ingman.ice.retailerrequest.v2.remote.exchange;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Yahor_Fralou on 7/13/2015.
 */
public class ExchangeIntends {
    private static PendingIntent exchangeBR = null;

    public static PendingIntent getExchangeStartPendingIntent(Context ctx) {
        if (exchangeBR == null) {
            Intent intent = new Intent(ctx, ExchangeDataService.class);
            exchangeBR = PendingIntent.getService(ctx, 0 , intent,0);
        }

        return  exchangeBR;
    }
}
