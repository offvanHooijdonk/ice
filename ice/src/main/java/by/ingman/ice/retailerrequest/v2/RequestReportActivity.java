package by.ingman.ice.retailerrequest.v2;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import by.ingman.ice.retailerrequest.v2.helpers.Helper;
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
    private static final int OPTION_ALL = 0;
    private static final int OPTION_NO_ANSWER = 1;
    private static final int OPTION_ANSWERED = 2;

    private RequestReportActivity that;

    private TextView textDate;
    private Spinner spOptions;
    private ListView lvReport;
    private ReportAdapter reportAdapter;

    private Calendar reportDate;
    Map<String, List<Order>> orders = new HashMap<>();
    private OrderLocalDao orderLocalDao;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.requestreport);

        that = this;

        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }

        reportDate = initTodayDate();

        lvReport = (ListView) findViewById(R.id.lvReport);
        reportAdapter = new ReportAdapter(that, orders);
        lvReport.setAdapter(reportAdapter);
        lvReport.setEmptyView(findViewById(R.id.textEmptyReport));

        textDate = (TextView) findViewById(R.id.textDate);
        spOptions = (Spinner) findViewById(R.id.spOptions);

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

        spOptions.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                showRequestsForDate(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spOptions.setSelection(OPTION_ALL);
    }

    private void showRequestsForDate(int option) {
        orderLocalDao = new OrderLocalDao(that);
        //вычитываем один раз все запросы
        Boolean answeredOnly = option == OPTION_ALL ? null : (option == OPTION_ANSWERED) ? Boolean.TRUE : Boolean.FALSE;
        orders.clear();
        orders.putAll(orderLocalDao.getOrdersSince(reportDate.getTime(), getDayEndDate(reportDate).getTime(), answeredOnly));
        reportAdapter.notifyDataSetChanged();
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

        showRequestsForDate(spOptions.getSelectedItemPosition());
    }

    private Calendar getDayEndDate(Calendar date) {
        Calendar dayEndDate = (Calendar) date.clone();
        dayEndDate.add(Calendar.DAY_OF_MONTH, 1);
        dayEndDate.add(Calendar.MILLISECOND, -1);

        return dayEndDate;
    }

    private Calendar initTodayDate() {
        Calendar c = Calendar.getInstance();
        c = Helper.roundDayToStart(c);

        return c;
    }

    private class ReportAdapter extends BaseAdapter {
        private Context ctx;
        //private List<Order> orders;
        Map<String, List<Order>> orders;

        public ReportAdapter(Context ctx, Map<String, List<Order>> orders) {
            this.ctx = ctx;

            this.orders = orders;
        }

        @Override
        public int getCount() {
            return orders.keySet().size();
        }

        @Override
        public Object getItem(int position) {
            return orders.values().toArray()[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                v = LayoutInflater.from(ctx).inflate(R.layout.item_report, parent, false);
            }

            final List<Order> list = (List<Order>) getItem(position);

            TextView textInfo = (TextView) v.findViewById(R.id.textInfo);
            String s = Order.toReportString(list.get(0));
            textInfo.setText(Html.fromHtml(s));
            textInfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder adb = new AlertDialog.Builder(RequestReportActivity.this);
                    adb.setTitle("Заявка " + list.get(0).getOrderId());
                    adb.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // continue with delete
                        }
                    });
                    adb.setMessage(Order.toReportVerboseString(list));
                    adb.show();
                }
            });

            TextView textAnswer = (TextView) v.findViewById(R.id.textAnswer);
            Answer answer = orderLocalDao.findAnswerByOrderId(list.get(0).getOrderId());
            if (answer == null) {
                textAnswer.setText(ctx.getString(R.string.order_no_answer));
            } else {
                textAnswer.setText(ctx.getString(R.string.order_answer, answer.getDescription()));
            }

            ImageButton btnCheckAnswer = (ImageButton) v.findViewById(R.id.btnCheckStatus);
            btnCheckAnswer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(ctx, list.get(0).getContrAgentName(), Toast.LENGTH_LONG).show();
                }
            });

            return v;
        }
    }
}
