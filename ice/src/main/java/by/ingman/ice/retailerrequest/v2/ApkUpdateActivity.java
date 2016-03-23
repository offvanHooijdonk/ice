package by.ingman.ice.retailerrequest.v2;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.log4j.Logger;

import by.ingman.ice.retailerrequest.v2.helpers.AlarmHelper;
import by.ingman.ice.retailerrequest.v2.remote.exchange.ExchangeDataService;

/**
 * Created with IntelliJ IDEA.
 * User: Администратор
 * Date: 26.06.13
 * Time: 0:44
 * To change this template use File | Settings | File Templates.
 */
public class ApkUpdateActivity extends Activity {
    private final Logger log = Logger.getLogger(ApkUpdateActivity.class);

    Button pubFilesUpdateButton;
    ProgressBar progressBar;
    private TextView textSuccess;
    private TextView textFail;
    ProgressDialog progressDialog;
    private Context ctx;
    private SharedPreferences sharedPreferences;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.apkupdate);

        this.ctx = this;

        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        textSuccess = (TextView) findViewById(R.id.textSuccess);
        textFail = (TextView) findViewById(R.id.textFail);

        textSuccess.setVisibility(View.GONE);
        textFail.setVisibility(View.GONE);

        pubFilesUpdateButton = (Button) findViewById(R.id.pubFilesUpdateButton);
        pubFilesUpdateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ctx);

                builder.setMessage(getString(R.string.update_data_files_dialog_disclaimer))
                        .setPositiveButton(R.string.dialog_button_positive, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                startForceUpdate();
                            }
                        })
                        .setNegativeButton(R.string.dialog_button_negative, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        }).show();
            }
        });
    }

    private void startForceUpdate() {
        try {
            sharedPreferences.edit().putLong("lastUpdateDate", 0).apply();

            displayFailureMessage(false);
            displaySuccessMessage(false);
            displayProgressDialog(true);

            stopService(new Intent(getApplicationContext(), ExchangeDataService.class));
            // cancel alarm for the next service call
            AlarmHelper.cancelExchangeAlarm(ctx);

            Intent intent = new Intent(getApplicationContext(), ExchangeDataService.class);
            ServiceResultReceiver receiver = new ServiceResultReceiver();
            intent.putExtra(ExchangeDataService.EXTRA_RECEIVER, receiver);
            startService(intent);
        } catch (Exception e) {
            log.error("Error force updating DB", e);
            Toast.makeText(getApplicationContext(), "Невозможно обновить", Toast.LENGTH_SHORT).show();
            displayFailureMessage(true);
            displayProgressDialog(false);
        }
    }

    private void displayProgressDialog(boolean display) {
        if (display) {
            progressDialog = new ProgressDialog(ApkUpdateActivity.this);
            progressDialog.setTitle(ctx.getString(R.string.progress_dialog_title));
            progressDialog.setMessage(ctx.getString(R.string.progress_dialog_message));
            progressDialog.setCancelable(false);
            progressDialog.show();
        } else {
            if (progressDialog != null) {
                progressDialog.cancel();
            }
        }
    }

    private void displaySuccessMessage(boolean display) {
        textSuccess.setVisibility(display ? View.VISIBLE : View.GONE);
    }

    private void displayFailureMessage(boolean display) {
        textFail.setVisibility(display ? View.VISIBLE : View.GONE);
    }

    public class ServiceResultReceiver extends ResultReceiver {

        public ServiceResultReceiver() {
            super(new Handler());
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);

            if (resultCode == Activity.RESULT_OK) {
                displaySuccessMessage(true);
            } else {
                displayFailureMessage(true);
            }

            displayProgressDialog(false);
        }
    }
}