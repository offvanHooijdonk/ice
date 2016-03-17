package by.ingman.ice.retailerrequest.v2.local.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import by.ingman.ice.retailerrequest.v2.structure.Product;

/**
 * Created by Yahor_Fralou on 3/15/2016.
 */
public class ProductLocalDao {
    public static final String TABLE = "rests";

    private static final String UNIQUE_CONDITION = "code =? AND store_code = ?";

    private DBHelper dbHelper;

    public ProductLocalDao(Context context) {
        dbHelper = new DBHelper(context);
    }

    public List<Product> getAll() {
        List<Product> products = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor c = db.query(TABLE, null, null, null, null, null, "code");

        while (c.moveToNext()) {
            products.add(new Product(
                    c.getString("code_s"),
                    c.getString("code_p"),
                    c.getString("name_p"),
                    c.getString("packs"),
                    c.getString("amount"),
                    c.getDouble("price"),
                    c.getInt("amt_in_pack"),
                    c.getDouble("gross_weight")
            ));
        }

        c.close();

        return products;
    }

    public void updateProducts(List<Product> products) {
        for (Product p : products) {
            if (existsProductAtStore(p)) {
                update(p);
            } else {
                insert(p);
            }
        }
    }

    public boolean existsProductAtStore(Product p) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        boolean exists;

        Cursor c = db.query(TABLE, null, UNIQUE_CONDITION, new String[] {p.getCode(), p.getStorehouseCode()}, null, null, null);

        exists = c.getCount() > 0;

        c.close();

        return exists;
    }

    public void insert(Product p) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues cv = makeContentValues(p);

        db.insert(TABLE, null, cv);
    }

    public void update (Product p) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues cv = makeContentValues(p);

        db.update(TABLE, cv, UNIQUE_CONDITION, new String[]{p.getCode(), p.getStorehouseCode()});
    }

    private ContentValues makeContentValues(Product p) {
        ContentValues cv = new ContentValues();

        cv.put("code", p.getCode());
        cv.put("name", p.getName());
        cv.put("store_code", p.getStorehouseCode());
        cv.put("store_packs", p.getStorehousePacks());
        cv.put("store_rest", p.getStorehouseRest());
        cv.put("price", p.getPrice());
        cv.put("num_in_pack", p.getCountInPack());
        cv.put("weight", p.getWeight());

        return cv;
    }
}
