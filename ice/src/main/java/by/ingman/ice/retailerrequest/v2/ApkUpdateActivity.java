package by.ingman.ice.retailerrequest.v2;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.NotActiveException;
import java.util.Arrays;

import by.ingman.ice.retailerrequest.v2.helpers.AlarmHelper;
import by.ingman.ice.retailerrequest.v2.helpers.StaticFileNames;
import by.ingman.ice.retailerrequest.v2.remote.exchange.ExchangeDataService;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;

/**
 * Created with IntelliJ IDEA.
 * User: Администратор
 * Date: 26.06.13
 * Time: 0:44
 * To change this template use File | Settings | File Templates.
 */
public class ApkUpdateActivity extends Activity {
    Button apkUpdateButton;
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

        getActionBar().setDisplayHomeAsUpEnabled(true);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        textSuccess = (TextView) findViewById(R.id.textSuccess);
        textFail = (TextView) findViewById(R.id.textFail);

        textSuccess.setVisibility(View.GONE);
        textFail.setVisibility(View.GONE);

        apkUpdateButton = (Button) findViewById(R.id.apkUpdateButton);
        apkUpdateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    checkNetworkConnected();

                    String url = sharedPreferences.getString("androidExchangeApkDirPref", "");

                    SmbFile smbFile = new SmbFile("smb://" + url + "ice.apk");

                    SmbFileInputStream is = new SmbFileInputStream(smbFile);

                    FileOutputStream fos = openFileOutput("ice.apk", Context.MODE_WORLD_READABLE);//new FileOutputStream(getApplicationContext().getFilesDir().getPath() + "/ice.apk");

                    byte buffer[] = new byte[8192];
                    int read;
                    while ((read = is.read(buffer)) > 0)
                        fos.write(buffer, 0, read);
                    is.close();
                    fos.flush();
                    fos.close();

                    File file = getFileStreamPath("ice.apk");

                    Toast.makeText(getApplicationContext(), "Обновление загружено", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
                    startActivity(intent);

                } catch (NotActiveException e) {
                    Toast.makeText(getApplicationContext(), "Нет связи с интернетом", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "Невозможно загрузить обновление, " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
           }
        });

        pubFilesUpdateButton = (Button) findViewById(R.id.pubFilesUpdateButton);
        pubFilesUpdateButton.setOnClickListener(new View.OnClickListener() {
            /**
             * Removes all curernt DB data and uploads it from scratch
             * @param view
             */
            @Override
            public void onClick(View view) {
                try {
                    checkNetworkConnected();

                    displayFailureMessage(false);
                    displaySuccessMessage(false);
                    displayProgressDialog(true);

                    stopService(new Intent(getApplicationContext(), ExchangeDataService.class));
                    // cancel alarm for the next service call
                    AlarmHelper.cancelExchangeAlarm(ctx);

                    // TODO move all prepare/run logic to separate class
                    for (String filename : StaticFileNames.getFilenamesArray()) {
                        if (Arrays.asList(fileList()).contains(filename)) {
                            getFileStreamPath(filename).delete();
                        }
                    }

                    Toast.makeText(getApplicationContext(), "Подготовка прошла успешно, файлы будут обновлены в течении нескольких минут", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(getApplicationContext(), ExchangeDataService.class);
                    ServiceResultReceiver receiver = new ServiceResultReceiver();
                    intent.putExtra(ExchangeDataService.EXTRA_RECEIVER, receiver);
                    startService(intent);
                } catch (NotActiveException e) {
                    Toast.makeText(getApplicationContext(), "Нет связи с интернетом", Toast.LENGTH_SHORT).show();
                    displayFailureMessage(true);
                    displayProgressDialog(false);
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "Невозможно обновить", Toast.LENGTH_SHORT).show();
                    displayFailureMessage(true);
                    displayProgressDialog(false);
                }
            }
        });
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

    private void checkNetworkConnected() throws NotActiveException {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni == null) {
            // There are no active networks.
            throw new NotActiveException("No network");
        }
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