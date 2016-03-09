package by.ingman.ice.retailerrequest.v2;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import by.ingman.ice.retailerrequest.v2.helpers.DBHelper;
import by.ingman.ice.retailerrequest.v2.helpers.GsonHelper;
import by.ingman.ice.retailerrequest.v2.structure.Order;
import by.ingman.ice.retailerrequest.v2.structure.Answer;

/**
 * Created with IntelliJ IDEA.
 * User: Администратор
 * Date: 08.06.13
 * Time: 16:10
 * To change this template use File | Settings | File Templates.
 */
public class RequestReportActivity extends Activity implements DatePickerDialog.OnDateSetListener {
    private RequestReportActivity that;

    LinearLayout mainLayout;
    LinearLayout reqLayout;
    SQLiteDatabase db;
    DBHelper dbHelper;
    int k = 25;
    Map<String, List<Order>> requests = new HashMap<>();
    private TextView textDate;
    private Calendar reportDate;
    private Gson gson;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.requestreport);

        that = this;

        getActionBar().setDisplayHomeAsUpEnabled(true);

        gson = GsonHelper.createGson();

        mainLayout = (LinearLayout) findViewById(R.id.linearLayoutCAReportMain);
        reqLayout = (LinearLayout) findViewById(R.id.linearLayoutCAReportReq);

        textDate = (TextView) findViewById(R.id.textDate);
        reportDate = Calendar.getInstance();
        textDate.setText(Order.getDateFormat().format(reportDate.getTime()));
        textDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog dateDialog = new DatePickerDialog(that, that,
                        reportDate.get(Calendar.YEAR),
                        reportDate.get(Calendar.MONTH),
                        reportDate.get(Calendar.DAY_OF_MONTH));
                dateDialog.show();
            }
        });

        dbHelper = new DBHelper(this);
        db = dbHelper.getWritableDatabase();

        //вычитываем один раз все запросы
        requests = readRequestsForDate(reportDate.getTime());
        //главный метод
        showRequestsForDate(reportDate.getTime());
    }

    private void showRequestsForDate(Date date) {
        reqLayout.removeAllViews();
        if (requests.size() == 0) {
            TextView textView = new TextView(this);
            textView.setText("Нет заявок на " + Order.getDateFormat().format(date));
            reqLayout.addView(textView);
        } else {
            for (final String reqId : requests.keySet()) {
                String s = "";
                TextView textView = new TextView(RequestReportActivity.this);

                s = s.concat(Order.toReportString(requests.get(reqId).get(0)));

                Answer answer = readResponseByReqId(reqId);
                if (answer == null) {
                    s = s.concat("<br><br><b>ОТВЕТА НЕТ</b>");
                } else {
                    s = s.concat("<br><br><b>ОТВЕТ: </b>").concat(answer.toStringForViewing());
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
                        adb.setMessage(Order.toReportVerboseString(requests.get(reqId)));
                        adb.show();
                    }
                });
                reqLayout.addView(textView);
            }
        }
    }

    private Answer readResponseByReqId(String reqId) {
        Answer answer = null;

        String answersSelection = "req_id=\"" + reqId + "\" and is_req=0";
        Cursor answersCursor = db.query(DBHelper.TABLE_REQUESTS_NAME, new String[]{"req_id", "req"}, answersSelection, null, null, null, null);
        if (answersCursor != null) {
            if (answersCursor.moveToFirst()) {
                answer = new Answer();
                for (String columnName : answersCursor.getColumnNames()) {
                    if ("req".equals(columnName)) {
                        String req = answersCursor.getString(answersCursor.getColumnIndex(columnName));
                        String[] array = req.split(";");
                        answer.setId(reqId);
                        answer.setResCode(array[1]);
                        answer.setDesc(array[2]);
                    }
                }
            }
            answersCursor.close();
        }
        return answer;
    }

    private Map<String, List<Order>> readRequestsForDate(Date date) {
        String selection = "is_req>0 and date=\"" + Order.getDateFormat().format(date) + "\"";
        Cursor c = db.query(DBHelper.TABLE_REQUESTS_NAME, null, selection, null, null, null, null);
        String reqId = "";
        String req = "";
        Map<String, List<Order>> requests = new HashMap<>();

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
                    Order order = gson.fromJson(req, Order.class);
                    if (!requests.containsKey(reqId)) {
                        requests.put(reqId, new ArrayList<Order>());
                    }
                    requests.get(reqId).add(order);
                } while (c.moveToNext());
            }
            c.close();
        }

        return requests;
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        if (reportDate == null) {
            reportDate = Calendar.getInstance();
        }
        reportDate.set(Calendar.YEAR, year);
        reportDate.set(Calendar.MONTH, monthOfYear);
        reportDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        textDate.setText(Order.getDateFormat().format(reportDate.getTime()));

        requests = readRequestsForDate(reportDate.getTime());
        showRequestsForDate(reportDate.getTime());
    }
}
