package by.ingman.ice.retailerrequest.v2;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.NotActiveException;
import java.util.Arrays;

import by.ingman.ice.retailerrequest.v2.helpers.StaticFileNames;
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
    ClearTask clearTask;
    ProgressDialog progressDialog;


    SharedPreferences sharedPreferences;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.apkupdate);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        apkUpdateButton = (Button) findViewById(R.id.apkUpdateButton);
        apkUpdateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    checkNetworkConnected();

                    String url = sharedPreferences.getString("androidExchangeApkDirPref", "");

                    SmbFile smbFile = new SmbFile("smb://" + url + "ice.apk");

                    SmbFileInputStream is = new SmbFileInputStream(smbFile);

                    //file.createNewFile();
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
            @Override
            public void onClick(View view) {
                try {

                    checkNetworkConnected();

                    displayProgressDialog();

                    stopService(new Intent(getApplicationContext(), FilesUpdateService.class));

                    for (String filename : StaticFileNames.getFilenamesArray()) {
                        if (Arrays.asList(fileList()).contains(filename)) {
                            getFileStreamPath(filename).delete();
                        }
                    }

                    Toast.makeText(getApplicationContext(), "Подготовка прошла успешно, файлы будут обновлены в течении нескольких минут", Toast.LENGTH_SHORT).show();

                    clearTask = new ClearTask();
                    clearTask.execute();

                } catch (NotActiveException e) {
                    Toast.makeText(getApplicationContext(), "Нет связи с интернетом", Toast.LENGTH_SHORT).show();
                    closeProgressDialog();
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "Невозможно обновить", Toast.LENGTH_SHORT).show();
                    closeProgressDialog();
                }
            }
        });
    }

    class ClearTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... integers) {
            startService(new Intent(getApplicationContext(), FilesUpdateService.class));

            boolean allFilesUpdated = false;
            while (!allFilesUpdated) {
                int i = 0;
                for (String filename : StaticFileNames.getFilenamesArray()) {
                    if (Arrays.asList(fileList()).contains(filename)) {
                        i++;
                    }
                }
                if (i >= StaticFileNames.getFilenamesArray().length) {
                    allFilesUpdated = true;
                }
            }
            return null;
        }

        protected void onPostExecute(Void result) {
            progressBar.setVisibility(View.GONE);
            closeProgressDialog();
            Toast.makeText(getApplicationContext(), "Файлы обновлены успешно", Toast.LENGTH_SHORT).show();
        }

        protected void onPreExecute() {

        }
    };


    private void displayProgressDialog() {
        progressDialog = new ProgressDialog(ApkUpdateActivity.this);
        progressDialog.setTitle("Данные обновляются");
        progressDialog.setMessage("Дождитесь обновления файлов");
        progressDialog.setCancelable(true);
        progressDialog.show();
        progressBar.setVisibility(View.VISIBLE);
    }

    private void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.cancel();
        }
        progressBar.setVisibility(View.GONE);
    }

    private void checkNetworkConnected() throws NotActiveException {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni == null) {
            // There are no active networks.
            throw new NotActiveException("No network");
        }
    }
}