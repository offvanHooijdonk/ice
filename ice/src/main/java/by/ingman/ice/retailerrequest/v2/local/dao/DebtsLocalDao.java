package by.ingman.ice.retailerrequest.v2.local.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

import by.ingman.ice.retailerrequest.v2.structure.ContrAgent;
import by.ingman.ice.retailerrequest.v2.structure.Debt;

/**
 * Created by Yahor_Fralou on 3/21/2016.
 */
public class DebtsLocalDao {
    private final Logger log = Logger.getLogger(DebtsLocalDao.class);

    public static final String TABLE = "debts";

    private DBHelper dbHelper;

    public DebtsLocalDao(Context context) {
        dbHelper = new DBHelper(context);
    }

    public void updateAll(List<Debt> debts) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        db.beginTransaction();

        try {
            db.delete(TABLE, null, null);
            for (Debt d : debts) {
                ContentValues cv = toContentValues(d);
                db.insert(TABLE, null, cv);
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            log.error("Error saving Debts to local DB.", e);
        } finally {
            db.endTransaction();
        }
    }

    public List<Debt> getDebtsForContrAgent(ContrAgent ca) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<Debt> debts = new ArrayList<>();

        Cursor c = db.query(TABLE, null, "code_k = ?", new String[]{ca.getCode()}, null, null, null);

        while (c.moveToNext()) {
            debts.add(new Debt(
                    c.getString(c.getColumnIndex("code_k")),
                    c.getString(c.getColumnIndex("rating")),
                    c.getString(c.getColumnIndex("debt")),
                    c.getString(c.getColumnIndex("overdue"))
            ));
        }

        c.close();

        return debts;
    }

    private ContentValues toContentValues(Debt d) {
        ContentValues cv = new ContentValues();

        cv.put("code_k", d.getContrAgentCode());
        cv.put("rating", d.getText());
        cv.put("debt", d.getDebt());
        cv.put("overdue", d.getOverdueDebt());

        return cv;
    }
}
