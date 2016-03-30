package by.ingman.ice.retailerrequest.v2;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import by.ingman.ice.retailerrequest.v2.helpers.ConfigureLog4J;
import by.ingman.ice.retailerrequest.v2.helpers.Helper;
import by.ingman.ice.retailerrequest.v2.helpers.PreferenceHelper;
import by.ingman.ice.retailerrequest.v2.local.dao.ContrAgentLocalDao;
import by.ingman.ice.retailerrequest.v2.local.dao.DebtsLocalDao;
import by.ingman.ice.retailerrequest.v2.local.dao.OrderLocalDao;
import by.ingman.ice.retailerrequest.v2.local.dao.ProductLocalDao;
import by.ingman.ice.retailerrequest.v2.remote.exchange.ExchangeDataService;
import by.ingman.ice.retailerrequest.v2.structure.ContrAgent;
import by.ingman.ice.retailerrequest.v2.structure.Debt;
import by.ingman.ice.retailerrequest.v2.structure.Order;
import by.ingman.ice.retailerrequest.v2.structure.Product;
import by.ingman.ice.retailerrequest.v2.structure.SalePoint;
import by.ingman.ice.retailerrequest.v2.structure.Storehouse;

public class MainActivity extends Activity implements DatePickerDialog.OnDateSetListener {

    private MainActivity that;

    TextView textDate;
    CheckBox checkBoxCommercial;
    EditText sortClientEditText;
    TextView usernameText;
    Button salesPointsButton;
    TextView contrAgentTextView;
    EditText sortSalespointEditText;
    TextView salePointTextView;
    TextView storehouseTextView;
    TextView textViewContrAgentRelationship;
    TextView textViewFinal;
    Button buttonAddProduct;
    EditText sortProductEditText;
    CheckBox checkBoxProductFilterType;
    EditText commentEditText;
    private Calendar orderDate = null;


    final int DIALOG_CONTRAGENTS = 1;
    final int DIALOG_SALESPOINTS = 2;
    final int DIALOG_STOREHOUSES = 3;
    final int DIALOG_PRODUCTS = 4;


    String username;
    ContrAgent contrAgent = null;
    SalePoint salePoint = null;
    Storehouse storehouse = null;
    List<Product> products;
    HashMap<Integer, Product> selectedProducts = new HashMap<>();

    private ProductLocalDao productLocalDao;
    private ContrAgentLocalDao contrAgentLocalDao;
    private DebtsLocalDao debtsLocalDao;
    private OrderLocalDao orderLocalDao;

    int productCountIds = 0;

    int currentProductId = 0;

    InputMethodManager imm;

    private String lastValueProductFilter;

    static {
        ConfigureLog4J.configure();
    }

    private final Logger log = Logger.getLogger(MainActivity.class);

    /**
     * Called when the activity is first created.
     */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        that = this;

        PreferenceManager.setDefaultValues(that, R.xml.pref, false);

        productLocalDao = new ProductLocalDao(that);
        contrAgentLocalDao = new ContrAgentLocalDao(that);
        debtsLocalDao = new DebtsLocalDao(that);
        orderLocalDao = new OrderLocalDao(that);

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        //запускаем сервис обновления
        startService(new Intent(this, ExchangeDataService.class));

        //products = productLocalDao.get

        //вычитываем при старте

        //sect--------------------------------------
        //Секция инициализации вьюх
        {
            // request date init
            orderDate = Calendar.getInstance();
            textDate = (TextView) findViewById(R.id.textDate);
            textDate.setText(Order.getDateFormat().format(orderDate.getTime()));

            textDate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DatePickerDialog dateDialog = new DatePickerDialog(that, that,
                            orderDate.get(Calendar.YEAR),
                            orderDate.get(Calendar.MONTH),
                            orderDate.get(Calendar.DAY_OF_MONTH));
                    Calendar now = Calendar.getInstance();
                    now = Helper.roundDayToStart(now);
                    Calendar maxDate = (Calendar) now.clone();
                    maxDate.add(Calendar.DAY_OF_MONTH, PreferenceHelper.Settings.getOrderDaysAhead(that));
                    dateDialog.getDatePicker().setMinDate(now.getTimeInMillis());
                    dateDialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());
                    dateDialog.show();
                }
            });

            commentEditText = (EditText) findViewById(R.id.commentEditText);

            sortClientEditText = (EditText) findViewById(R.id.editTextContrAgentFilter);
            sortClientEditText.setOnKeyListener(new View.OnKeyListener() {
                public boolean onKey(View view, int keyCode, KeyEvent event) {
                    if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                            (keyCode == KeyEvent.KEYCODE_ENTER)) {
                        showDialog(DIALOG_CONTRAGENTS);
                        return true;
                    }
                    return false;
                }
            });


            sortSalespointEditText = (EditText) findViewById(R.id.editTextSalespointsFilter);
            sortSalespointEditText.setOnKeyListener(new View.OnKeyListener() {
                public boolean onKey(View view, int keyCode, KeyEvent event) {
                    if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                            (keyCode == KeyEvent.KEYCODE_ENTER)) {
                        showDialog(DIALOG_SALESPOINTS);
                        return true;
                    }
                    return false;
                }
            });

            checkBoxCommercial = (CheckBox) findViewById(R.id.checkBoxCommercial);
            checkBoxCommercial.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    refreshFinalView();
                }
            });


            usernameText = (TextView) findViewById(R.id.textViewUsername);
            username = PreferenceHelper.Settings.getManagerName(that);
            usernameText.setText(username);

            salesPointsButton = (Button) findViewById(R.id.buttonSalespoints);
            salesPointsButton.setEnabled(false);

            contrAgentTextView = (TextView) findViewById(R.id.textViewContrAgent);

            salePointTextView = (TextView) findViewById(R.id.textViewSalePoint);

            textViewContrAgentRelationship = (TextView) findViewById(R.id.textViewContrAgentRelationship);

            textViewFinal = (TextView) findViewById(R.id.textViewFinal);

            storehouseTextView = (TextView) findViewById(R.id.textViewStorehouse);

            buttonAddProduct = (Button) findViewById(R.id.buttonAddProduct);

            sortProductEditText = (EditText) findViewById(R.id.editTextProductFilter);
            sortProductEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
            sortProductEditText.setOnKeyListener(new View.OnKeyListener() {
                public boolean onKey(View view, int keyCode, KeyEvent event) {
                    if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                            (keyCode == KeyEvent.KEYCODE_ENTER)) {
                        addProduct();
                        return true;

                    }
                    return false;
                }
            });

            checkBoxProductFilterType = (CheckBox) findViewById(R.id.checkBoxProductFilterType);
            checkBoxProductFilterType.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                    if (checked) {
                        sortProductEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
                        sortProductEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
                    } else {
                        sortProductEditText.setInputType(InputType.TYPE_CLASS_TEXT);
                    }
                }
            });

        }

        //устанавливаем склад по умолчанию из настроек
        setDefaultStorehouse();

        //обновляем отображение заявки
        refreshFinalView();
    }

    private void setDefaultStorehouse() {
        String storehouseDefaultCode = PreferenceHelper.Settings.getDefaultStoreHouseCode(that);
        if (!storehouseDefaultCode.equals("")) {
            storehouse = productLocalDao.getStorehouseById(storehouseDefaultCode);
            if (storehouse != null) {
                storehouseTextView.setText(storehouse.toString());
            }

            if (storehouse == null) {
                storehouseTextView.setText("");
                buttonAddProduct.setEnabled(false);
            }
        }
    }

    private void refreshFinalView() {
        boolean requestIsReady = true;
        StringBuilder sb = new StringBuilder();

        if (username == null || username.equals("")) {
            sb.append(" Установите имя пользователя в настройках программы\n\r\n\r");
            requestIsReady = false;
        } else {
            sb.append(" Менеджер: ").append(username).append("\n\r\n\r");
        }

        int day = orderDate.get(Calendar.DAY_OF_MONTH);
        int month = orderDate.get(Calendar.MONTH); // month starts with 0 in Calendar
        int year = orderDate.get(Calendar.YEAR);
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
        Date date = calendar.getTime();
        sb.append("Дата: ").append(Order.getDateFormat().format(date)).append("\n\r\n\r");
        textViewFinal.setText(sb.toString());


        if (checkBoxCommercial.isChecked()) {
            sb.append("Рекламное предложение\n\r\n\r");
        } else {
            sb.append("Не является рекламным предложением\n\r\n\r");
        }
        textViewFinal.setText(sb.toString());

        //склад
        if (storehouse == null) {
            sb.append("Склад не выбран\n\r\n\r");
            requestIsReady = false;
        } else {
            sb.append("Склад: ").append(storehouse.getCode()).append(" ").append(storehouse.getName()).append("\n\r\n\r");
        }
        //контрагент
        if (contrAgent == null) {
            sb.append("Контрагент не выбран\n\r\n\r");
            requestIsReady = false;
        } else {
            sb.append("Контрагент: ").append(contrAgent.getCode()).append(" ").append(contrAgent.getName()).append("\n\r\n\r");
        }
        //разгрузка
        if (salePoint == null) {
            sb.append("Точка разгрузки не выбрана\n\r\n\r");
            requestIsReady = false;
        } else {
            sb.append("Разгрузка: ").append(salePoint.getCode()).append(" ").append(salePoint.getName()).append("\n\r\n\r");
        }


        int totalCount = 0;
        double totalPacks = 0;
        double totalWeight = 0;

        //товары
        Double totalSumm = 0.0;
        if (selectedProducts.size() == 0) {
            sb.append("Товар не выбран\n\r\n\r");
            requestIsReady = false;
        } else {
            List<Integer> keys = new ArrayList<>(selectedProducts.keySet());
            Collections.sort(keys);
            for (Integer k : keys) {
                Product p = selectedProducts.get(k);
                sb.append(p.getCode()).append(" ").append(p.getName()).append("\n\r");
                sb.append("\tПо цене ").append(Helper.formatMoney(p.getPrice())).append("\n\r");
                sb.append("\tУпаковок: ").append(Helper.formatPacks(p.getPacks())).append("\n\r");
                sb.append("\tШтук: ").append(p.getRest()).append("\n\r");
                double prodSumm = p.getRest() * p.getPrice();
                sb.append("\tСумма: ").append(Helper.formatMoney(prodSumm)).append("\n\r");
                totalSumm += prodSumm;

                if (Integer.parseInt(p.getStorehouseRest()) < p.getRest()) {
                    sb.append("\tПРОВЕРЬТЕ КОЛИЧЕСТВО\n\r");
                    requestIsReady = false;
                }

                totalCount += p.getRest();
                totalPacks += p.getPacks();
                totalWeight += p.getWeight() * p.getRest();
            }
        }

        sb.append("\n\n\tВСЕГО: ").append(Helper.formatPacks(totalPacks)).append(" уп., ").append(totalCount).append(" шт., ").append(Helper.formatPacks(totalWeight)).append(" кг")
                .append("\n\tСУММА: ").append(Helper.formatMoney(totalSumm));

        //финальное отображение
        textViewFinal.setText(sb.toString());

        //кнопка отправки заявки доступна, если все параметры введены
        findViewById(R.id.buttonSendRequest).setEnabled(requestIsReady);
    }

    protected void onResume() {
        username = PreferenceHelper.Settings.getManagerName(that);
        usernameText.setText(username);
        refreshFinalView();
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.report: {
                Intent intent = new Intent(this, RequestReportActivity.class);
                startActivity(intent);
            }
            break;
            case R.id.refresh: {
                Intent intent = new Intent(this, UpdateDataActivity.class);
                startActivity(intent);
            }
            break;
            case R.id.settings: {
                Intent intent = new Intent(this, PrefActivity.class);
                startActivity(intent);
            }
            break;
        }
        return true;
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        if (orderDate == null) {
            orderDate = Calendar.getInstance();
        }
        orderDate.set(Calendar.YEAR, year);
        orderDate.set(Calendar.MONTH, monthOfYear);
        orderDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        textDate.setText(Order.getDateFormat().format(orderDate.getTime()));

        refreshFinalView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void onclick(View v) {
        switch (v.getId()) {
            case R.id.buttonContrAgentsDialog:
                if (checkUpdateInProgress()) {
                    return;
                }
                showDialog(DIALOG_CONTRAGENTS);
                break;
            case R.id.buttonSalespoints:
                if (checkUpdateInProgress()) {
                    return;
                }
                showDialog(DIALOG_SALESPOINTS);
                break;
            case R.id.buttonStorehouses:
                if (checkUpdateInProgress()) {
                    return;
                }
                showDialog(DIALOG_STOREHOUSES);
                break;
            case R.id.buttonAddProduct:
                if (checkUpdateInProgress()) {
                    return;
                }
                addProduct();
                break;
            case R.id.buttonSendRequest:
                storeOrder();
            default:
                break;
        }
    }

    private void storeOrder() {
        int day = orderDate.get(Calendar.DAY_OF_MONTH);
        int month = orderDate.get(Calendar.MONTH); // month starts with 0 in Calendar
        int year = orderDate.get(Calendar.YEAR);
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
        Date date = calendar.getTime();

        String comment = commentEditText.getText().toString();

        String orderId = Order.generateNewId();
        for (Product product : selectedProducts.values()) {
            Order order = new Order(
                    orderId,
                    username,
                    checkBoxCommercial.isChecked(),
                    contrAgent.getCode(),
                    contrAgent.getName(),
                    salePoint.getCode(),
                    salePoint.getName(),
                    storehouse.getCode(),
                    storehouse.getName(),
                    product.getCode(),
                    product.getName(),
                    product.getPacks(),
                    product.getRest(),
                    date,
                    comment,
                    false,
                    false);
            orderLocalDao.save(order);

            updateRestsLocal();

            clearForm();
        }
    }

    private void updateRestsLocal() {

        for (Product p : selectedProducts.values()) {
            Product productStored = productLocalDao.findProductInStorehouse(p.getCode(), storehouse.getCode());
            if (productStored == null) {
                if (checkUpdateInProgress()) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (that != null) {
                                that.updateRestsLocal();
                            }
                        }
                    }, 1000);
                    return;
                } else {
                    log.error(String.format("Product %s not found in local DB", p.getTextViewText()));
                    Toast.makeText(that, String.format("Товар '%s' не найден в локальной базе.", p.getName()), Toast.LENGTH_LONG).show();
                }
            } else {

                double packsNew = productStored.getPacks() - p.getPacks();
                int restNew = productStored.getRest() - p.getRest();
                productStored.setPacks(packsNew >= 0.0 ? packsNew : 0.0);
                productStored.setRest(restNew >= 0 ? restNew : 0);

                productLocalDao.update(productStored);
            }
        }

    }

    private void clearForm() {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    private void addProduct() {
        //int width = (findViewById(R.id.buttonStorehouses)).getWidth();

        //полный лайоут товара (для его удаления, если что)
        LinearLayout layout = (LinearLayout) findViewById(R.id.layoutProducts);
        LinearLayout productLayout = new LinearLayout(this);
        productLayout.setOrientation(LinearLayout.VERTICAL);
        productLayout.setId(productCountIds++); //0
        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout
                .LayoutParams.MATCH_PARENT);
        llp.setMargins(0, 0, 0, 40);
        productLayout.setLayoutParams(llp);
        int paddingPx = that.getResources().getDimensionPixelSize(R.dimen.product_layout_padding);
        productLayout.setPadding(paddingPx, 0, paddingPx, 0);
        productLayout.setBackgroundColor(that.getResources().getColor(R.color.productBckg));
        layout.addView(productLayout);


        //горизонтальный лайоут для кнопок выбора товаров и удаления
        LinearLayout layoutProductButton = new LinearLayout(this);
        layoutProductButton.setOrientation(LinearLayout.HORIZONTAL);
        productLayout.addView(layoutProductButton);

        //кнопка "Товар"
        Button productButton = new Button(this);
        productButton.setText("Товар");
        productButton.setOnClickListener(getOnClickShowProductsDialog(productButton));
        productButton.setId(2000 + productCountIds++); //1
        layoutProductButton.addView(productButton);
        //setting width for button
        productButton.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, 3));

        //кнопка "Удалить товар"
        Button deleteProductButton = new Button(this);
        deleteProductButton.setText("Удалить");
        deleteProductButton.setId(2000 + productCountIds++); //2
        deleteProductButton.setOnClickListener(getOnClickDeleteProductDialog(deleteProductButton));
        layoutProductButton.addView(deleteProductButton);
        //setting width for button
        deleteProductButton.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, 2));

        //textView с информацией по товару
        TextView textView = new TextView(this);
        textView.setId(2000 + productCountIds++);  //3
        productLayout.addView(textView);
        textView.setTextColor(that.getResources().getColor(android.R.color.primary_text_light));

        //горизонтальный лайоут для количества товаров
        LinearLayout layoutProductCount = new LinearLayout(this);
        layoutProductCount.setOrientation(LinearLayout.HORIZONTAL);
        productLayout.addView(layoutProductCount);


        //editText количество упаковок
        EditText editTextProductPacksCount = new EditText(this);
        editTextProductPacksCount.setInputType(InputType.TYPE_CLASS_NUMBER);
        editTextProductPacksCount.setId(2000 + productCountIds++); //4
        editTextProductPacksCount.setFocusable(true);
        editTextProductPacksCount.setFocusableInTouchMode(true);
        layoutProductCount.addView(editTextProductPacksCount);
        //setting width
        editTextProductPacksCount.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        editTextProductPacksCount.setEnabled(false);
        editTextProductPacksCount.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View view, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    Editable editable = ((EditText) view).getText();
                    if (editable == null || editable.toString().equals("")) {
                        return false;
                    }
                    double packs;
                    try {
                        packs = Double.valueOf(editable.toString());
                    } catch (NumberFormatException e) {
                        log.error("Error converting packs count to number", e);
                        return false;
                    }
                    Product p = selectedProducts.get(view.getId() - 4);
                    EditText editTextRest = (EditText) findViewById(view.getId() + 1);
                    int count = (int) packs * p.getCountInPack();

                    editTextRest.setText(String.valueOf(count));
                    p.setPacks(packs);
                    p.setRest(count);
                    selectedProducts.put(view.getId() - 4, p);

                    TextView textView = (TextView) findViewById(view.getId() - 1);
                    textView.setText(p.getTextViewText());

                    if (Integer.parseInt(p.getStorehouseRest()) < count) {
                        ((EditText) view).setTextColor(that.getResources().getColor(R.color.warnTextColor));
                        editTextRest.setTextColor(that.getResources().getColor(R.color.warnTextColor));
                    } else {
                        ((EditText) view).setTextColor(that.getResources().getColor(android.R.color.primary_text_light));
                        editTextRest.setTextColor(that.getResources().getColor(android.R.color.primary_text_light));
                    }

                    refreshFinalView();
                    return true;
                }
                return false;
            }
        });


        //editText количество штук
        EditText editTextProductCount = new EditText(this);
        editTextProductCount.setInputType(InputType.TYPE_CLASS_NUMBER);
        editTextProductCount.setId(2000 + productCountIds++); //5
        editTextProductCount.setFocusable(true);
        editTextProductCount.setFocusableInTouchMode(true);
        layoutProductCount.addView(editTextProductCount);
        //setting width
        editTextProductCount.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        editTextProductCount.setEnabled(false);
        editTextProductCount.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View view, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button

                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    Editable editable = ((EditText) view).getText();
                    if (editable == null || editable.toString().equals("")) {
                        return false;
                    }
                    int count = Integer.parseInt(editable.toString());
                    Product p = selectedProducts.get(view.getId() - 5);
                    EditText editTextPacks = (EditText) findViewById(view.getId() - 1);
                    double packs = (double) count / p.getCountInPack();
                    //editTextPacks.setText(String.valueOf(packs));
                    String packsString = Helper.formatPacks(packs);
                    editTextPacks.setText(packsString);

                    p.setPacks(Helper.parsePacksLocaleString(packsString));
                    p.setRest(count);
                    selectedProducts.put(view.getId() - 5, p);


                    TextView textView = (TextView) findViewById(view.getId() - 2);
                    textView.setText(p.getTextViewText());
                    if (Integer.parseInt(p.getStorehouseRest()) < count) {
                        ((EditText) view).setTextColor(that.getResources().getColor(R.color.warnTextColor));
                        editTextPacks.setTextColor(that.getResources().getColor(R.color.warnTextColor));
                    } else {
                        ((EditText) view).setTextColor(that.getResources().getColor(android.R.color.primary_text_light));
                        editTextPacks.setTextColor(that.getResources().getColor(android.R.color.primary_text_light));
                    }

                    refreshFinalView();
                    return true;
                }
                return false;
            }
        });

        //сохраняем значение фильтра (он будет очищен)
        lastValueProductFilter = sortProductEditText.getText().toString();

        if (products != null && products.size() == 0) {
            deleteProductButton.performClick();
            Toast.makeText(getApplicationContext(),
                    "Товар не подобран по фильтру \"" + lastValueProductFilter + "\"",
                    Toast.LENGTH_SHORT).show();
            sortProductEditText.setText("");
            return;
        }
        //и вызываем диалог
        productButton.performClick();

        // прыгающий фильтр товаров
        layout.removeView((View) sortProductEditText.getParent());
        layout.addView((View) sortProductEditText.getParent());
        //очищаем фильтр
        sortProductEditText.setText("");
        //... и прыгающая кнопка добавления товаров
        layout.removeView(buttonAddProduct);
        layout.addView(buttonAddProduct);

    }


    //----------------------------------------------------------------------------------
    //-------OnCreateDialog------------------------------------------------------------
    //----------------------------------------------------------------------------------


    protected Dialog onCreateDialog(int id) {
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        switch (id) {
            case DIALOG_CONTRAGENTS: {
                adb.setTitle(R.string.contrAgents);
                adb.setSingleChoiceItems(new String[]{}, -1, null);
                adb.setPositiveButton(R.string.ok, contrAgentsDialogOkClickListener);
                break;
            }
            case DIALOG_SALESPOINTS: {
                adb.setTitle(R.string.salesPoints);
                adb.setPositiveButton(R.string.ok, salesPointsDialogOkClickListener);
                adb.setSingleChoiceItems(new String[]{}, -1, null);
                break;
            }
            case DIALOG_STOREHOUSES: {
                adb.setTitle(R.string.storehouses);
                adb.setPositiveButton(R.string.ok, storehousesDialogOkClickListener);
                adb.setSingleChoiceItems(new String[]{}, -1, null);
                break;
            }
            case DIALOG_PRODUCTS: {
                adb.setTitle(R.string.products);
                adb.setPositiveButton(R.string.ok, productsDialogOkClickListener);
                adb.setSingleChoiceItems(new String[]{}, -1, null);
                break;
            }


        }
        return adb.create();
    }


    //sect----------------------------------------------------------------------------------
    //-------OnPrepareDialog------------------------------------------------------------
    //----------------------------------------------------------------------------------


    protected void onPrepareDialog(int id, Dialog dialog) {
        super.onPrepareDialog(id, dialog);
        if (id == DIALOG_CONTRAGENTS) {
            List<ContrAgent> contrAgents = contrAgentLocalDao.getContrAgents(sortClientEditText.getText().toString());

            ListAdapter mergeAdapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.select_dialog_singlechoice,
                    contrAgents);
            ((AlertDialog) dialog).getListView().setAdapter(mergeAdapter);

            if (contrAgents.size() == 1) {
                ((AlertDialog) dialog).getListView().setItemChecked(0, true);
                ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE).performClick();
            }
        }
        if (id == DIALOG_SALESPOINTS) {
            if (null != contrAgent) {
                List<SalePoint> salePoints = contrAgentLocalDao.getSalePointsByContrAgent(contrAgent, sortSalespointEditText.getText().toString());

                ListAdapter mergeAdapter = new ArrayAdapter<>(
                        this,
                        android.R.layout.select_dialog_singlechoice,
                        salePoints
                );
                ((AlertDialog) dialog).getListView().setAdapter(mergeAdapter);

                if (salePoints.size() == 1) {
                    ((AlertDialog) dialog).getListView().setItemChecked(0, true);
                    ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE).performClick();
                }
            }
        }
        if (id == DIALOG_STOREHOUSES) {
            ListAdapter mergeAdapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.select_dialog_singlechoice,
                    productLocalDao.getStorehouses()
            );
            ((AlertDialog) dialog).getListView().setAdapter(mergeAdapter);
        }
        if (id == DIALOG_PRODUCTS) {
            products = productLocalDao.getAllInStorehouse(storehouse.getCode(), lastValueProductFilter);
            if (products.size() == 0) {
                return;
            }

            ListAdapter mergeAdapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.select_dialog_singlechoice,
                    products
            );
            ((AlertDialog) dialog).getListView().setAdapter(mergeAdapter);

            if (products.size() == 1) {
                ((AlertDialog) dialog).getListView().setItemChecked(0, true);
                ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE).performClick();
            }
        }
    }

    //!sect----------------------------------------------------------------------------------
    //!-------OnClickListeners------------------------------------------------------------
    //-------------------------------------------------------------------------------------

    //показать продукты
    View.OnClickListener getOnClickShowProductsDialog(final Button button) {
        return new View.OnClickListener() {
            public void onClick(View v) {
                showDialog(DIALOG_PRODUCTS);
                currentProductId = button.getId() - 1;
            }
        };
    }

    //удалить секцию "продукты"
    View.OnClickListener getOnClickDeleteProductDialog(final Button button) {
        return new View.OnClickListener() {
            public void onClick(View v) {
                int selectedProductId = button.getId() - 2;
                if (selectedProducts.containsKey(selectedProductId)) {
                    selectedProducts.keySet().remove(selectedProductId);
                }
                //remove from main view
                LinearLayout productLayout = (LinearLayout) button.getParent().getParent();
                ((LinearLayout) productLayout.getParent()).removeView(productLayout);

                refreshFinalView();
            }
        };
    }


    //выбрать контрагента
    OnClickListener contrAgentsDialogOkClickListener = new OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            ListView lv = ((AlertDialog) dialog).getListView();
            if (which == Dialog.BUTTON_POSITIVE) {
                if (lv.getCheckedItemPosition() >= 0) {
                    contrAgent = (ContrAgent) lv.getAdapter().getItem(lv.getCheckedItemPosition()); //contrAgentHelper.getSelectedContrAgent(lv.getCheckedItemPosition());
                }
            }
            if (contrAgent != null) {
                salesPointsButton.setEnabled(true);
                contrAgentTextView.setText(contrAgent.getName());
                //проверка разгрузки
                if (salePoint != null && !contrAgent.getCode().equals(salePoint.getContrAgentCode())) {
                    salePoint = null;
                    salePointTextView.setText("");
                }
            }
            //показываем задолженность
            if (contrAgent != null) {
                List<Debt> debts = debtsLocalDao.getDebtsForContrAgent(contrAgent);
                if (debts.isEmpty()) {
                    textViewContrAgentRelationship.setText("");
                } else {
                    boolean hasOverdue = false;
                    StringBuilder str = new StringBuilder("");
                    for (Debt d : debts) {
                        str.append(String.format("%s: %s / %s \n", d.getText(), Helper.formatMoney(Double.valueOf(d.getDebt())),
                                Helper.formatMoney(Double.valueOf(d.getOverdueDebt()))));
                        if (Double.valueOf(d.getOverdueDebt()) > 0.0) {
                            hasOverdue = true;
                        }
                    }
                    textViewContrAgentRelationship.setText(str.toString());
                    if (hasOverdue) {
                        textViewContrAgentRelationship.setTextColor(that.getResources().getColor(R.color.warnTextColor));
                    } else {
                        textViewContrAgentRelationship.setTextColor(that.getResources().getColor(R.color.infoTextColor));
                    }
                }
            } else {
                textViewContrAgentRelationship.setText("");
            }
            refreshFinalView();
        }
    };

    //выбрать разгрузку
    OnClickListener salesPointsDialogOkClickListener = new OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            ListView lv = ((AlertDialog) dialog).getListView();
            if (which == Dialog.BUTTON_POSITIVE) {
                if (lv.getCheckedItemPosition() >= 0) {
                    salePoint = (SalePoint) lv.getAdapter().getItem(lv.getCheckedItemPosition());
                }
            }
            if (salePoint != null) {
                salePointTextView.setText(salePoint.getName());
            }
            refreshFinalView();
        }
    };

    //выбрать склад
    OnClickListener storehousesDialogOkClickListener = new OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            ListView lv = ((AlertDialog) dialog).getListView();
            if (which == Dialog.BUTTON_POSITIVE) {
                if (lv.getCheckedItemPosition() >= 0) {
                    storehouse = (Storehouse) lv.getAdapter().getItem(lv.getCheckedItemPosition());
                }
            }
            if (storehouse != null) {
                storehouseTextView.setText(String.format("%s %s", storehouse.getCode(), storehouse.getName()));
                buttonAddProduct.setEnabled(true);
            }
            refreshFinalView();
        }
    };

    //выбрать продукт
    OnClickListener productsDialogOkClickListener = new OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            ListView lv = ((AlertDialog) dialog).getListView();
            if (which == Dialog.BUTTON_POSITIVE) {
                if (lv.getCheckedItemPosition() >= 0) {
                    Product product;
                    //находим строку-фильтр продуктов
                    //отбираем продукты только текущего склада
                    List<Product> storehouseProducts = productLocalDao.getAllInStorehouse(storehouse.getCode(), lastValueProductFilter);
                    //находим выбранный продукт

                    product = (Product) lv.getAdapter().getItem(lv.getCheckedItemPosition());
                    selectedProducts.put(currentProductId, product);

                    TextView textView = (TextView) findViewById(currentProductId + 3);
                    textView.setText(product.getTextViewText());

                    EditText editTextPacks = (EditText) findViewById(currentProductId + 4);
                    editTextPacks.setEnabled(true);
                    editTextPacks.setText(String.valueOf(product.getPacks()));

                    EditText editTextRest = (EditText) findViewById(currentProductId + 5);
                    editTextRest.setEnabled(true);
                    editTextRest.setText(String.valueOf(product.getRest()));

                    Button productButton = (Button) findViewById(currentProductId + 1);
                    productButton.setEnabled(false);

                    editTextPacks.setText("");
                    editTextPacks.requestFocus();
                    final TextView textViewFinal = (TextView) findViewById(editTextPacks.getId());
                    editTextPacks.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            imm.showSoftInput(textViewFinal, 0);
                        }
                    }, 200);
                }
            }
            refreshFinalView();
        }
    };

    private boolean checkUpdateInProgress() {
        boolean inProgress = ExchangeDataService.isUpdateInProgress(that);
        if (inProgress) {
            Toast.makeText(that, R.string.update_in_progress, Toast.LENGTH_LONG).show();
        }

        return inProgress;
    }

}