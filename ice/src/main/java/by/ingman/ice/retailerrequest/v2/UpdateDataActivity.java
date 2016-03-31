package by.ingman.ice.retailerrequest.v2;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.ResultReceiver;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import by.ingman.ice.retailerrequest.v2.helpers.AlarmHelper;
import by.ingman.ice.retailerrequest.v2.helpers.Helper;
import by.ingman.ice.retailerrequest.v2.helpers.PreferenceHelper;
import by.ingman.ice.retailerrequest.v2.remote.exchange.ExchangeDataService;

/**
 * Created with IntelliJ IDEA.
 * User: Администратор
 * Date: 26.06.13
 * Time: 0:44
 * To change this template use File | Settings | File Templates.
 */
public class UpdateDataActivity extends Activity {
    private final Logger log = Logger.getLogger(UpdateDataActivity.class);

    private Button pubFilesUpdateButton;
    private TextView textSuccess;
    private TextView textFail;
    ProgressDialog progressDialog;
    private Button btnUpdateApp;

    private File apkFile;

    private Context ctx;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.apkupdate);

        this.ctx = this;

        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
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

        btnUpdateApp = (Button) findViewById(R.id.apkUpdateButton);
        btnUpdateApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ctx);

                builder.setMessage(getString(R.string.update_apk_dialog_disclaimer))
                        .setPositiveButton(R.string.dialog_button_positive, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                updateAPK();
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
            PreferenceHelper.Runtime.setLastUpdateDate(ctx, 0);

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
            progressDialog = new ProgressDialog(UpdateDataActivity.this);
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

    private void displayApkProgressDialog(boolean display) {
        if (display) {
            progressDialog = new ProgressDialog(UpdateDataActivity.this);
            progressDialog.setTitle(ctx.getString(R.string.progress_apk_dialog_title));
            progressDialog.setMessage(ctx.getString(R.string.progress_apk_dialog_message));
            progressDialog.setMax(0);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setCancelable(false);
            progressDialog.show();
        } else {
            if (progressDialog != null) {
                progressDialog.cancel();
            }
        }
    }

    private void updateAPK() {
        displayApkProgressDialog(true);

        UpdateAPKTask updateAPKTask = new UpdateAPKTask();
        updateAPKTask.execute();
    }

    private void startUpdateIntent() {
        if (apkFile != null && apkFile.exists()) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
            startActivity(intent);
        } else {
            Toast.makeText(ctx, "Файл APK не найден!", Toast.LENGTH_LONG).show();
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

    private class UpdateAPKTask extends AsyncTask<Void, Long, Void> {
        private static final int batchSize = 4096;
        private static final int progressSizeDivider = 1000;

        private String errorMessage = null;

        @Override
        protected Void doInBackground(Void... params) {

            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            try {
                URL url = new URL(PreferenceHelper.Settings.getApkUpdateUrl(ctx));
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                // expect HTTP 200 OK, so we don't mistakenly save error report
                // instead of the file
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    log.error(String.format("Server returned HTTP %d %s", connection.getResponseCode(), connection.getResponseMessage()));
                    errorMessage = String.format("Ошибка обновления. Код %s : %s", connection.getResponseCode(), connection.getResponseMessage());

                    return null;
                }

                // this will be useful to display download percentage
                // might be -1: server did not report the length
                int fileLength = connection.getContentLength();

                // download the file
                input = connection.getInputStream();
                apkFile = new File(String.format("%s/%s/ice.apk", Environment.getExternalStorageDirectory(), Helper.APP_FOLDER));
                if (apkFile.exists()) {
                    apkFile.delete();
                }
                apkFile.createNewFile();
                output = new FileOutputStream(apkFile);

                byte data[] = new byte[batchSize];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    // allow canceling with back button
                    if (isCancelled()) {
                        input.close();
                        return null;
                    }
                    total += count;
                    // publishing the progress....
                    if (fileLength > 0) {// only if total length is known
                        publishProgress(total, (long) fileLength);
                    }
                    output.write(data, 0, count);
                }
            } catch (Exception e) {
                log.error("Error reading APK file from server", e);
                errorMessage =  "Ошибка обновления " + e.toString();
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (IOException ignored) {
                }

                if (connection != null)
                    connection.disconnect();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Long... values) {
            int max = (int) (values[1] / progressSizeDivider);
            int progress = (int) (values[0] / progressSizeDivider);
            if (progressDialog.getMax() == 0) {
                progressDialog.setMax(max);
            }
            progress = progress > progressDialog.getMax() ? progressDialog.getMax() : progress;
            progressDialog.setProgress(progress);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            progressDialog.cancel();

            if (errorMessage != null) {
                Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT).show();
                errorMessage = null;
            } else {
                Toast.makeText(getApplicationContext(), "Обновление загружено", Toast.LENGTH_SHORT).show();
                startUpdateIntent();
            }
        }
    }
}