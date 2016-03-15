package by.ingman.ice.retailerrequest.v2.local.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import by.ingman.ice.retailerrequest.v2.helpers.GsonHelper;
import by.ingman.ice.retailerrequest.v2.structure.Order;

/**
 * Created with IntelliJ IDEA.
 * User: Администратор
 * Date: 03.06.13
 * Time: 0:24
 * To change this template use File | Settings | File Templates.
 */
public class DBHelper extends SQLiteOpenHelper {

    public static String DATABASE_NAME = "iceDB";
    public static String TABLE_REQUESTS_NAME = "request";

    private static final int DATABASE_VERSION = 7;


    public DBHelper(Context context) {
        // конструктор суперкласса
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // создаем таблицу с полями
        db.execSQL("create table " + TABLE_REQUESTS_NAME + " ("
                + "_id integer primary key autoincrement,"
                + "is_req integer not null,"
                + "sent integer not null,"
                + "req_id text not null,"
                + "date text not null,"
                + "req text not null" + ");");
        db.execSQL("create table " + ProductLocalDao.TABLE + " ("
                + "_id integer primary key autoincrement,"
                + "code text not null,"
                + "name text not null,"
                + "store_code text not null,"
                + "store_packs text not null,"
                + "store_rest text not null,"
                + "price real not null,"
                + "num_in_pack integer not null,"
                + "weight real not null" + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_REQUESTS_NAME);
        onCreate(db);
    }

    public Order getSingleRequestByRequestId(String requestId) {
        Order order = null;
        ContentValues cv = new ContentValues();
        cv.put("req_id", requestId);
        Cursor cursor = getReadableDatabase().query(TABLE_REQUESTS_NAME, new String[]{"req"}, "req_id", new String[]{requestId}, null, null, null);
        if (cursor.moveToFirst()) {
            String requestString = cursor.getString(0);
            order = GsonHelper.createGson().fromJson(requestString, Order.class);
        }
        cursor.close();

        return order;
    }

    public Map<String, List<Order>> readUnsentRequests() {
        String selection = "sent=0 and is_req>0";
        Cursor c = getReadableDatabase().query(DBHelper.TABLE_REQUESTS_NAME, null, selection, null, null, null, null);
        String reqId = "";
        String req = "";
        Map<String, List<Order>> requests = new HashMap<String, List<Order>>();

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
                    Order order = GsonHelper.createGson().fromJson(req, Order.class);
                    if (requests.containsKey(reqId)) {
                        requests.get(reqId).add(order);
                    } else {
                        List<Order> reqList = new ArrayList<>();
                        reqList.add(order);
                        requests.put(reqId, reqList);
                    }
                } while (c.moveToNext());
            }
            c.close();
        }

        return requests;
    }

    public void markRequestSent(String reqId) {
        ContentValues cv = new ContentValues();
        cv.put("sent", 1);
        getWritableDatabase().update(DBHelper.TABLE_REQUESTS_NAME, cv, "req_id = ?", new String[]{reqId});
    }
}
