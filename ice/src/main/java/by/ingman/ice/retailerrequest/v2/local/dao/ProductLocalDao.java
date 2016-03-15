package by.ingman.ice.retailerrequest.v2.local.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.List;

import by.ingman.ice.retailerrequest.v2.structure.Product;

/**
 * Created by Yahor_Fralou on 3/15/2016.
 */
public class ProductLocalDao {
    public static final String TABLE = "rests";

    private DBHelper dbHelper;

    public ProductLocalDao(Context context) {
        dbHelper = new DBHelper(context);
    }

    public void updateProducts(List<Product> products) {
        for (Product p : products) {

        }
    }

    public boolean existsProductAtStore(Product p) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        boolean exists;

        Cursor c = db.query(TABLE, null, "code =? AND store_code = ?", new String[] {p.getCode(), p.getStorehouseCode()}, null, null, null);

        exists = c.getCount() > 0;

        return exists;
    }

    public void insert(Product p, SQLiteDatabase db) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put("code", p.getCode());
        cv.put("name", p.getName());
        cv.put("store_code", p.getStorehouseCode());
        cv.put("store_packs", p.getStorehousePacks());
        cv.put("store_rest", p.getStorehouseRest());
        cv.put("price", p.getPrice());
        cv.put("num_in_pack", p.getCountInPack());
        cv.put("weight", p.getWeight());

        db.insert(TABLE, null, cv);
    }

    public void update
}
