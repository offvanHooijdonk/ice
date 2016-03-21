package by.ingman.ice.retailerrequest.v2.local.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import by.ingman.ice.retailerrequest.v2.helpers.GsonHelper;
import by.ingman.ice.retailerrequest.v2.structure.Order;

/**
 * Created by Yahor_Fralou on 3/21/2016.
 */
public class OrderLocalDao {
    public static String TABLE = "order";

    private DBHelper dbHelper;

    public OrderLocalDao(Context context) {
        dbHelper = new DBHelper(context);
    }

    public Order getSingleRequestByRequestId(String requestId) {
        Order order = null;
        ContentValues cv = new ContentValues();
        cv.put("req_id", requestId);
        Cursor cursor = dbHelper.getReadableDatabase().query(TABLE, new String[]{"req"}, "req_id", new String[]{requestId}, null, null, null);
        if (cursor.moveToFirst()) {
            String requestString = cursor.getString(0);
            order = GsonHelper.createGson().fromJson(requestString, Order.class);
        }
        cursor.close();

        return order;
    }

    public Map<String, List<Order>> readUnsentRequests() {
        String selection = "sent=0 and is_req>0";
        Cursor c = dbHelper.getReadableDatabase().query(TABLE, null, selection, null, null, null, null);
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
        dbHelper.getWritableDatabase().update(TABLE, cv, "req_id = ?", new String[]{reqId});
    }
}
