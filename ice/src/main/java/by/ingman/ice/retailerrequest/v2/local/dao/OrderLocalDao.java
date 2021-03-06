package by.ingman.ice.retailerrequest.v2.local.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import by.ingman.ice.retailerrequest.v2.structure.Answer;
import by.ingman.ice.retailerrequest.v2.structure.Order;
import by.ingman.ice.retailerrequest.v2.structure.Product;

/**
 * Created by Yahor_Fralou on 3/21/2016.
 */
public class OrderLocalDao {
    public static String TABLE = "orders";
    public static String TABLE_ANSWER = "answers";

    private DBHelper dbHelper;
    private Context ctx;

    public OrderLocalDao(Context context) {
        this.ctx = context;
        dbHelper = new DBHelper(context);
    }

    public Order getSingleOrderById(String orderId) {
        Order order = null;
        Cursor c = dbHelper.getReadableDatabase().query(TABLE, null, "order_id = ?", new String[]{orderId}, null, null, null);
        if (c.moveToFirst()) {
            order = fromCursor(c);
        }
        c.close();

        return order;
    }

    public void save(Order order) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        db.insert(TABLE, null, toContentValues(order));
    }

    public Map<String, List<Order>> getUnsentOrders() {
        Cursor c = dbHelper.getReadableDatabase().query(TABLE, null, "sent=?", new String[]{"0"}, null, null, null);
        Map<String, List<Order>> orders = new HashMap<>();

        while (c.moveToNext()) {
            Order order = fromCursor(c);
            if (orders.containsKey(order.getOrderId())) {
                orders.get(order.getOrderId()).add(order);
            } else {
                List<Order> orderList = new ArrayList<>();
                orderList.add(order);
                orders.put(order.getOrderId(), orderList);
            }
        }
        c.close();

        return orders;
    }

    public List<String> getSentOrdersIdsWithoutAnswer() {
        List<String> orderIds = new ArrayList<>();
        Cursor c = dbHelper.getReadableDatabase().query(true, TABLE, new String[]{"order_id"}, "processed = ?", new String[]{"0"}, null, null, null, null, null);

        while (c.moveToNext()) {
            orderIds.add(c.getString(0));
        }

        c.close();

        return orderIds;
    }

    public Map<String, List<Order>> getOrdersSince(Date dateSince, Date dateTo, Boolean answeredOnly) {
        Map<String, List<Order>> ordersMap = new HashMap<>();
        ProductLocalDao productLocalDao = new ProductLocalDao(ctx);

        Cursor c;
        if (answeredOnly == null) {
            c = dbHelper.getReadableDatabase().query(TABLE, null, "order_date >= ? AND order_date <= ?",
                    new String[]{String.valueOf(dateSince.getTime()), String.valueOf(dateTo.getTime())}, null, null, "order_id");
        } else {
            c = dbHelper.getReadableDatabase().query(TABLE, null, "order_date >= ? AND order_date <= ? AND processed = ?",
                    new String[]{String.valueOf(dateSince.getTime()), String.valueOf(dateTo.getTime()), answeredOnly ? "1" : "0"}, null, null, "order_id");
        }

        while (c.moveToNext()) {
            String orderId = c.getString(c.getColumnIndex("order_id"));
            if (!ordersMap.containsKey(orderId)) {
                ordersMap.put(orderId, new ArrayList<Order>());
            }
            Order o = fromCursor(c);
            Product p = productLocalDao.findProductInStorehouse(o.getProductCode(), o.getStorehouseCode());
            o.setProductPrice(p.getPrice());
            ordersMap.get(orderId).add(o);
        }

        c.close();

        return ordersMap;
    }

    public void markOrderSent(String orderId, boolean sent) {
        ContentValues cv = new ContentValues();
        cv.put("sent", sent ? 1 : 0);
        dbHelper.getWritableDatabase().update(TABLE, cv, "order_id = ?", new String[]{orderId});
    }

    public void saveRemoteAnswer(Answer answer) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues cvOrder = new ContentValues();
        cvOrder.put("processed", 1);
        db.update(TABLE, cvOrder, "order_id = ?", new String[]{answer.getOrderId()});

        db.delete(TABLE_ANSWER, "order_id = ?", new String[]{answer.getOrderId()});

        ContentValues cvAnswer = new ContentValues();
        cvAnswer.put("order_id", answer.getOrderId());
        cvAnswer.put("description", answer.getDescription());
        cvAnswer.put("date_unload", answer.getUnloadTime().getTime());
        db.insert(TABLE_ANSWER, null, cvAnswer);
    }

    public Answer findAnswerByOrderId(String orderId) {
        Answer answer = null;

        Cursor c = dbHelper.getReadableDatabase().query(TABLE_ANSWER, null, "order_id = ?", new String[]{orderId}, null, null, null);
        if (c.moveToFirst()) {
            answer = new Answer();
            answer.setOrderId(orderId);
            answer.setDescription(c.getString(c.getColumnIndex("description")));
            answer.setUnloadTime(new Date(c.getLong(c.getColumnIndex("date_unload"))));
        }

        c.close();

        return answer;
    }

    private ContentValues toContentValues(Order o) {
        ContentValues cv = new ContentValues();

        cv.put("order_id", o.getOrderId());
        cv.put("manager", o.getManager());
        cv.put("is_advertising", o.getIsCommercial());
        cv.put("order_date", o.getOrderDate().getTime());
        cv.put("contragent_code", o.getContrAgentCode());
        cv.put("contragent_name", o.getContrAgentName());
        cv.put("sale_point_code", o.getSalePointCode());
        cv.put("sale_point_name", o.getSalePointName());
        cv.put("storehouse_code", o.getStorehouseCode());
        cv.put("storehouse_name", o.getStorehouseName());
        cv.put("product_code", o.getProductCode());
        cv.put("product_name", o.getProductName());
        cv.put("packs_num", o.getProductPacksCount());
        cv.put("product_num", o.getProductCount());
        cv.put("comment", o.getComment());
        cv.put("sent", o.getSent());
        cv.put("processed", o.getProcessed());

        return cv;
    }

    private Order fromCursor(Cursor c) {
        return new Order(
                c.getString(c.getColumnIndex("order_id")),
                c.getString(c.getColumnIndex("manager")),
                c.getInt(c.getColumnIndex("is_advertising")),
                c.getString(c.getColumnIndex("contragent_code")),
                c.getString(c.getColumnIndex("contragent_name")),
                c.getString(c.getColumnIndex("sale_point_code")),
                c.getString(c.getColumnIndex("sale_point_name")),
                c.getString(c.getColumnIndex("storehouse_code")),
                c.getString(c.getColumnIndex("storehouse_name")),
                c.getString(c.getColumnIndex("product_code")),
                c.getString(c.getColumnIndex("product_name")),
                c.getDouble(c.getColumnIndex("packs_num")),
                c.getInt(c.getColumnIndex("product_num")),
                new Date(c.getLong(c.getColumnIndex("order_date"))),
                c.getString(c.getColumnIndex("comment")),
                c.getInt(c.getColumnIndex("sent")) == 1,
                c.getInt(c.getColumnIndex("processed")) == 1
        );
    }
}
