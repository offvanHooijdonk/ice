package by.ingman.ice.retailerrequest.v2;

import android.os.Bundle;
import android.app.Activity;
import android.widget.TextView;

public class ErrorMessageActivity extends Activity {
    public static final String EXTRA_ERROR_MESSAGE = "EXTRA_ERROR_MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error_message);

        if (getActionBar() != null) {
            getActionBar().setHomeButtonEnabled(true);
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }

        TextView textView = (TextView) findViewById(R.id.textErrorMessage);

        String errorMessage = getIntent().getExtras().getString(EXTRA_ERROR_MESSAGE, null);

        if (errorMessage != null) {
            textView.setText(errorMessage);
        }
    }

}
