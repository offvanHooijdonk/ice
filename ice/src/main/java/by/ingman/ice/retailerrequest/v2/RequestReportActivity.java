package by.ingman.ice.retailerrequest.v2;

import android.app.*;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.*;
import by.ingman.ice.retailerrequest.v2.helpers.DBHelper;
import by.ingman.ice.retailerrequest.v2.structure.Request;
import by.ingman.ice.retailerrequest.v2.structure.Response;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Администратор
 * Date: 08.06.13
 * Time: 16:10
 * To change this template use File | Settings | File Templates.
 */
public class RequestReportActivity extends Activity {
    LinearLayout mainLayout;
    LinearLayout reqLayout;
    DatePicker datePicker;
    Date date;
    //TextView textView;
    SQLiteDatabase db;
    DBHelper dbHelper;
    int k = 25;
    HashMap<String, String> requests = new HashMap<String, String>();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.requestreport);

        mainLayout = (LinearLayout) findViewById(R.id.linearLayoutCAReportMain);

        reqLayout = (LinearLayout) findViewById(R.id.linearLayoutCAReportReq);
        //reqLayout = new LinearLayout(this);
        //reqLayout.setOrientation(LinearLayout.VERTICAL);
        //mainLayout.addView(reqLayout);

        datePicker = (DatePicker) findViewById(R.id.datePickerRRerport);
        final Calendar c = Calendar.getInstance();
        datePicker.init(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), getOnDateChangedListener());

        //textView = (TextView) findViewById(R.id.textViewRRreportRequests);

        dbHelper = new DBHelper(this);
        db = dbHelper.getWritableDatabase();

        date = new Date();
        //вычитываем один раз все запросы
        requests = readRequestsForDate();
        //values = (ArrayList<String>) requests.values();
        //главный метод
        showRequestsForDate(date);
    }

    public DatePicker.OnDateChangedListener getOnDateChangedListener() {
        return new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker datePicker, int i, int i2, int i3) {
                int day = datePicker.getDayOfMonth();
                int month = datePicker.getMonth();
                int year =  datePicker.getYear();

                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, day);

                date = calendar.getTime();

                requests = readRequestsForDate();
                showRequestsForDate(date);
            }
        };
    }

    private void showRequestsForDate(Date date) {
        //mainLayout.removeView(reqLayout);
        //reqLayout = new LinearLayout(this);
        //mainLayout.addView(reqLayout);
        //mainLayout = new LinearLayout(this);
        reqLayout.removeAllViews();
        if (requests.size() == 0) {
            TextView textView = new TextView(this);
            textView.setText("Нет заявок на " + Request.getDateFormat().format(date));
            reqLayout.addView(textView);
        } else {
            for (final String reqId : requests.keySet()) {
                String s = "";
                TextView textView = new TextView(RequestReportActivity.this);
                s = s.concat(Request.parseRequest(requests.get(reqId)));

                Response response = readResponseByReqId(reqId);
                if (response == null) {
                    s = s.concat("<br><br><b>ОТВЕТА НЕТ</b>");
                } else {
                    s = s.concat("<br><br><b>ОТВЕТ: </b>").concat(response.toStringForViewing());
                }
                s = s.concat("<br><br><br>");
                textView.setText(Html.fromHtml(s));
                textView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AlertDialog.Builder adb = new AlertDialog.Builder(RequestReportActivity.this);
                        adb.setTitle("Заявка " + reqId);
                        adb.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // continue with delete
                            }
                        });
                        adb.setMessage(Request.parseRequestFully(requests.get(reqId)));
                        adb.show();
                    }
                });
                reqLayout.addView(textView);
            }
        }
    }

    private void sendNotification(String str) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        Notification notif = new Notification(R.drawable.icon, "Text in status bar",
                System.currentTimeMillis());

        notif.setLatestEventInfo(this, "Notification's title", str,
                PendingIntent.getActivity(getApplicationContext(), 0, new Intent(), 0));

        notif.defaults = Notification.DEFAULT_ALL;
        notif.flags |= Notification.FLAG_AUTO_CANCEL;
        // отправляем
        notificationManager.notify(k++, notif);
    }

    private Response readResponseByReqId(String reqId) {
        Response response = null;

        String answersSelection = "req_id=\"" + reqId + "\" and is_req=0";
        Cursor answersCursor = db.query(DBHelper.TABLE_REQUESTS_NAME, new String[]{"req_id", "req"}, answersSelection, null, null, null, null);
        if (answersCursor != null) {
            if (answersCursor.moveToFirst()) {
                response = new Response();
                for (String columnName : answersCursor.getColumnNames()) {
                    if ("req".equals(columnName)) {
                        String req = answersCursor.getString(answersCursor.getColumnIndex(columnName));
                        String[] array = req.split(";");
                        response.setId(reqId);
                        response.setResCode(array[1]);
                        response.setDesc(array[2]);
                    }
                }
            }
        }
        answersCursor.close();
        return response;
    }

    private HashMap<String, String> readRequestsForDate() {
        String selection = "is_req>0 and date=\"" + Request.getDateFormat().format(date) + "\"";
        Cursor c = db.query(DBHelper.TABLE_REQUESTS_NAME, null, selection, null, null, null, null);
        String reqId = "";
        String req = "";
        HashMap<String, String> requests = new HashMap<String, String>();

        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    for (String columnName : c.getColumnNames()) {
                        if (columnName.equals("req")) {
                            req = c.getString(c.getColumnIndex(columnName));
                        }
                        if (columnName.equals("req_id")) {
                            reqId = c.getString(c.getColumnIndex(columnName));
                        }
                    }
                    if (requests.containsKey(reqId)) {
                        String banch = requests.get(reqId);
                        banch = banch.concat(";");
                        banch = banch.concat(req);
                        requests.put(reqId, banch);
                    } else {
                        requests.put(reqId, req);
                    }
                } while (c.moveToNext());
            }
            c.close();
        }

        return requests;
    }
}
