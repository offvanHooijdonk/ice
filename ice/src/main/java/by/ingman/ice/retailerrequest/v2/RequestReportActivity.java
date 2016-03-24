package by.ingman.ice.retailerrequest.v2;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import by.ingman.ice.retailerrequest.v2.local.dao.OrderLocalDao;
import by.ingman.ice.retailerrequest.v2.structure.Answer;
import by.ingman.ice.retailerrequest.v2.structure.Order;

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
    private OrderLocalDao orderLocalDao;
    Map<String, List<Order>> orders = new HashMap<>();
    private TextView textDate;
    private Calendar reportDate;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.requestreport);

        that = this;

        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }

        reportDate = initTodayDate();

        mainLayout = (LinearLayout) findViewById(R.id.linearLayoutCAReportMain);
        reqLayout = (LinearLayout) findViewById(R.id.linearLayoutCAReportReq);

        textDate = (TextView) findViewById(R.id.textDate);

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

        orderLocalDao = new OrderLocalDao(that);

        //вычитываем один раз все запросы
        orders = orderLocalDao.getOrdersSince(reportDate.getTime(), getDayEndDate(reportDate).getTime());
        //главный метод
        showRequestsForDate(reportDate.getTime());
    }

    private void showRequestsForDate(Date date) {
        reqLayout.removeAllViews();
        if (orders.size() == 0) {
            TextView textView = new TextView(this);
            textView.setText(String.format("Нет заявок на %s", Order.getDateFormat().format(date)));
            reqLayout.addView(textView);
        } else {
            for (final String orderId : orders.keySet()) {
                String s = "";
                TextView textView = new TextView(RequestReportActivity.this);

                s = s.concat(Order.toReportString(orders.get(orderId).get(0)));

                Answer answer = orderLocalDao.findAnswerByOrderId(orderId);
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
                        adb.setTitle("Заявка " + orderId);
                        adb.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // continue with delete
                            }
                        });
                        adb.setMessage(Order.toReportVerboseString(orders.get(orderId)));
                        adb.show();
                    }
                });
                reqLayout.addView(textView);
            }
        }
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        if (reportDate == null) {
            reportDate = initTodayDate();
        }
        reportDate.set(Calendar.YEAR, year);
        reportDate.set(Calendar.MONTH, monthOfYear);
        reportDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        textDate.setText(Order.getDateFormat().format(reportDate.getTime()));

        orders = orderLocalDao.getOrdersSince(reportDate.getTime(), getDayEndDate(reportDate).getTime());
        showRequestsForDate(reportDate.getTime());
    }

    private Calendar getDayEndDate(Calendar date) {
        Calendar dayEndDate = (Calendar) date.clone();
        dayEndDate.add(Calendar.DAY_OF_MONTH, 1);
        dayEndDate.add(Calendar.MILLISECOND, -1);

        return dayEndDate;
    }

    private Calendar initTodayDate() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        return c;
    }
}
