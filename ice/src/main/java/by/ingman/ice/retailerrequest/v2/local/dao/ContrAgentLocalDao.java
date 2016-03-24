package by.ingman.ice.retailerrequest.v2.local.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

import by.ingman.ice.retailerrequest.v2.structure.ContrAgent;
import by.ingman.ice.retailerrequest.v2.structure.SalePoint;

/**
 * Created by Yahor_Fralou on 3/21/2016.
 */
public class ContrAgentLocalDao {
    private final Logger log = Logger.getLogger(ContrAgentLocalDao.class);
    public static final String TABLE = "contragents";

    private DBHelper dbHelper;

    public ContrAgentLocalDao(Context context) {
        dbHelper = new DBHelper(context);
    }

    public List<ContrAgent> getContrAgents(String filter) {
        List<ContrAgent> caList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        filter = DBHelper.addWildcards(filter);
        Cursor c = db.query(true, TABLE, new String[]{"code", "name"}, "name like ?", new String[]{filter}, null, null, "name, code", null);
        while (c.moveToNext()) {
            caList.add(new ContrAgent(
                    c.getString(c.getColumnIndex("code")),
                    c.getString(c.getColumnIndex("name"))
            ));
        }
        c.close();

        return caList;
    }

    public List<SalePoint> getSalePointsByContrAgent(ContrAgent ca, String filter) {
        List<SalePoint> salePoints = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        filter = TextUtils.isEmpty(filter) ? "" : filter;
        filter = DBHelper.addWildcards(filter);
        Cursor c = db.query(TABLE, new String[]{"sp_code", "sp_name"}, "code=? AND sp_name like ?", new String[]{ca.getCode(), filter}, null, null, "sp_name, sp_code");
        while (c.moveToNext()) {
            salePoints.add(new SalePoint(
                    ca.getCode(),
                    c.getString(c.getColumnIndex("sp_code")),
                    c.getString(c.getColumnIndex("sp_name"))
            ));
        }
        c.close();

        return salePoints;
    }

    public void updateContrAgents(List<ContrAgent> caList) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        try {
            db.beginTransaction();
            db.delete(TABLE, null, null);

            for (ContrAgent ca : caList) {
                for (SalePoint sp : ca.getSalePoints()) {
                    insert(db, ca, sp);
                }
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            log.error("Error updating contrAgents in local DB.", e);
        } finally {
            db.endTransaction();
        }
    }

    /*public boolean exists(ContrAgent ca, SalePoint sp) {
        boolean exists;
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor c = db.query(TABLE, null, "code, sp_code", new String[]{ca.getCode(), sp.getCode()}, null, null, null);
        exists = c.getCount() > 0;
        c.close();

        return exists;
    }

    public void update(ContrAgent ca, SalePoint sp) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        db.update(TABLE, toContentValues(ca, sp), "code = ? AND sp_code = ?", new String[]{ca.getCode(), sp.getCode()});
    }*/

    public void insert(SQLiteDatabase db, ContrAgent ca, SalePoint sp) {
        db = dbHelper.getWritableDatabase();

        db.insert(TABLE, null, toContentValues(ca, sp));
    }

    private ContentValues toContentValues(ContrAgent ca, SalePoint sp) {
        ContentValues cv = new ContentValues();

        cv.put("code", ca.getCode());
        cv.put("name", ca.getName());
        cv.put("sp_code", sp.getCode());
        cv.put("sp_name", sp.getName());

        return cv;
    }
}
