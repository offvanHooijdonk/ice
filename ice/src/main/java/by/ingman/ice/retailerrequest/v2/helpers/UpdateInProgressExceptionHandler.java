package by.ingman.ice.retailerrequest.v2.helpers;

import android.content.Context;
import android.widget.Toast;

import by.ingman.ice.retailerrequest.v2.remote.exchange.UpdateInProgressException;

/**
 * Created by Yahor_Fralou on 3/22/2016.
 */
public class UpdateInProgressExceptionHandler implements Thread.UncaughtExceptionHandler {
    private Context ctx;
    private final Thread.UncaughtExceptionHandler rootHandler;

    public UpdateInProgressExceptionHandler(Context context) {
        this.ctx = context;

        rootHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        if (ex instanceof UpdateInProgressException) {
            Toast.makeText(ctx, ex.getMessage(), Toast.LENGTH_LONG).show();
        } else {
            rootHandler.uncaughtException(thread, ex);
        }
    }
}
