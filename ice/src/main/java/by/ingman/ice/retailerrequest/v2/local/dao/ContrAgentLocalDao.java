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

        filter = DBHelper.addWildcards(filter).toUpperCase();
        Cursor c = db.query(true, TABLE, new String[]{"code", "name"}, "contragent_filter like ?", new String[]{filter}, null, null,
                "name, code", null);
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

        filter = DBHelper.addWildcards(filter).toUpperCase();
        Cursor c = db.query(TABLE, new String[]{"sp_code", "sp_name"}, "code=? AND sp_filter like ?", new String[]{ca.getCode(), filter},
                null, null, "sp_name, sp_code");
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

    public void insert(SQLiteDatabase db, ContrAgent ca, SalePoint sp) {
        db = dbHelper.getWritableDatabase();

        db.insert(TABLE, null, toContentValues(ca, sp));
    }

    private ContentValues toContentValues(ContrAgent ca, SalePoint sp) {
        ContentValues cv = new ContentValues();

        cv.put("code", ca.getCode());
        cv.put("name", ca.getName());
        cv.put("contragent_filter", String.format("%s %s", ca.getCode(), ca.getName().toUpperCase()));
        cv.put("sp_code", sp.getCode());
        cv.put("sp_name", sp.getName());
        cv.put("sp_filter", String.format("%s %s", sp.getCode(), sp.getName().toUpperCase()));

        return cv;
    }
}
