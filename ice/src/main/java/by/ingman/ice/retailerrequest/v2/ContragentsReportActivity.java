package by.ingman.ice.retailerrequest.v2;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.*;
import by.ingman.ice.retailerrequest.v2.helpers.StaticFileNames;
import by.ingman.ice.retailerrequest.v2.structure.ContrAgent;
import by.ingman.ice.retailerrequest.v2.structure.ContrAgentList;
import by.ingman.ice.retailerrequest.v2.structure.Debt;
import by.ingman.ice.retailerrequest.v2.structure.SalePoint;

import java.io.*;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * Created with IntelliJ IDEA.
 * User: Администратор
 * Date: 27.05.13
 * Time: 1:44
 * To change this template use File | Settings | File Templates.
 */
public class ContragentsReportActivity extends Activity {
    ContrAgentList contrAgentList = new ContrAgentList();
    LinkedList<SalePoint> salePoints = new LinkedList<SalePoint>();
    HashMap<String, Debt> debts = new HashMap<String, Debt>();

    SharedPreferences sharedPreferences;

    String username;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contragentsreport);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        username = (sharedPreferences.getString("username", ""));

        readContrAgentsFromSD();
        readDebtsFromSD();

    }

    public void onclick(View v) {
        switch (v.getId()) {
            case R.id.buttonCAReportShow:
                show();
                break;
            default:
                break;
        }
    }

    private void show() {
        /*TableLayout tl = (TableLayout) findViewById(R.id.tableLayoutCAReport);
        LinearLayout parent = (LinearLayout) tl.getParent();
        parent.removeView(tl);*/

        /*TableLayout tl = new TableLayout(this);
        LinearLayout parent = (LinearLayout) findViewById(R.id.linearLayoutCAReportMain);
        parent.addView(tl);*/

        String  filter = ((EditText) findViewById(R.id.editTextCAReportFilter)).getText().toString();

        TextView tv = (TextView) findViewById(R.id.textViewCAReport);
        tv.setText("test11");


        StringBuilder sb = new StringBuilder();
        for (ContrAgent ca : contrAgentList.getContrAgentsCollection(filter)) {
            //TableRow tr = new TableRow(this);
            Debt debt = debts.get(ca.getCode());
            sb.append(ca.getCode()).append(" ").append(ca.getName()).append(" ");
            if (debt != null) {
                sb.append("Долг: ").append(debt.getOverdueDebt());
            }
            sb.append("\n\r");
            //tr.addView(tv);
            //tl.addView(tr);
        }
        tv.setText(sb.toString());

    }

    private void readContrAgentsFromSD() {
        // проверяем доступность SD
        if (!Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            return;
        }
        // получаем путь к SD
        File sdPath = Environment.getExternalStorageDirectory();
        // добавляем свой каталог к пути
        sdPath = new File(sdPath.getAbsolutePath() + "/");
        // формируем объект File, который содержит путь к файлу
        File sdFile = new File(sdPath, StaticFileNames.CLIENTS_CSV_SD);

        try {
            // открываем поток для чтения
            InputStreamReader isr = new InputStreamReader(new FileInputStream(sdFile), "CP-1251");
            BufferedReader br = new BufferedReader(isr);
            String string;
            String stringArray[];
            // читаем содержимое
            while ((string = br.readLine()) != null) {
                stringArray = string.split(";");
                ContrAgent ca = new ContrAgent(stringArray[0], stringArray[1]);
                contrAgentList.addContrAgent(ca);
                SalePoint sp = new SalePoint(stringArray[0], stringArray[4], stringArray[5]);
                salePoints.add(sp);
            }
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }
    }

    private void readDebtsFromSD() {
        // проверяем доступность SD
        if (!Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            return;
        }
        // получаем путь к SD
        File sdPath = Environment.getExternalStorageDirectory();
        // добавляем свой каталог к пути
        sdPath = new File(sdPath.getAbsolutePath() + "/");
        // формируем объект File, который содержит путь к файлу
        File sdFile = new File(sdPath, StaticFileNames.DEBTS_CSV_SD);

        try {
            // открываем поток для чтения
            InputStreamReader isr = new InputStreamReader(new FileInputStream(sdFile), "CP-1251");
            BufferedReader br = new BufferedReader(isr);
            String string;
            String stringArray[];
            // читаем содержимое
            while ((string = br.readLine()) != null) {
                stringArray = string.split(";");
                Debt debt = new Debt(stringArray[0], stringArray[3], stringArray[4], stringArray[5]);
                debts.put(stringArray[0], debt);
            }
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }
    }
}