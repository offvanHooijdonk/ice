package by.ingman.ice.retailerrequest.v2.remote.exchange;

import android.content.Context;

import by.ingman.ice.retailerrequest.v2.R;

/**
 * Created by Yahor_Fralou on 3/22/2016.
 */
public class UpdateInProgressException extends RuntimeException {
    private Context ctx;

    public UpdateInProgressException(Context context) {
        ctx = context;
    }

    @Override
    public String getMessage() {
        return ctx.getString(R.string.progress_exception_message);
    }
}
