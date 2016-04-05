package by.ingman.ice.retailerrequest.v2.local.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

import by.ingman.ice.retailerrequest.v2.structure.Product;
import by.ingman.ice.retailerrequest.v2.structure.Storehouse;

/**
 * Created by Yahor_Fralou on 3/15/2016.
 */
public class ProductLocalDao {
    public static final String TABLE = "rests";
    private final Logger log = Logger.getLogger(ProductLocalDao.class);

    private static final String UNIQUE_CONDITION = "code =? AND store_code = ?";
    private DBHelper dbHelper;

    public ProductLocalDao(Context context) {
        dbHelper = new DBHelper(context);
    }

    public List<Product> getAllInStorehouse(String storehouseCode, String filter, boolean searchCodes) {
        List<Product> products = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        filter = DBHelper.addWildcards(filter).toUpperCase();
        Cursor c = db.query(TABLE, null, "store_code = ? AND " + (searchCodes ? "code" : "product_filter") + " like ?", new String[]{storehouseCode, filter}, null, null,
                "name, code");

        while (c.moveToNext()) {
            products.add(fromCursor(c));
        }

        c.close();

        return products;
    }

    public List<Storehouse> getStorehouses() {
        List<Storehouse> storehouses = new ArrayList<>();

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor c = db.query(true, TABLE, new String[]{"store_code", "store_name"}, null, null, null, null, "store_name, store_code", null);
        while (c.moveToNext()) {
            storehouses.add(new Storehouse(
                    c.getString(c.getColumnIndex("store_code")),
                    c.getString(c.getColumnIndex("store_name"))));
        }
        c.close();

        return storehouses;
    }

    public Storehouse getStorehouseById(String storehouseId) {
        Storehouse s = null;
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor c = db.query(true, TABLE, new String[]{"store_code", "store_name"}, "store_code = ?", new String[]{storehouseId}, null, null, "store_name, store_code", null);
        if (c.moveToNext()) {
            s = new Storehouse(
                    c.getString(c.getColumnIndex("store_code")),
                    c.getString(c.getColumnIndex("store_name")));
        }
        c.close();

        return s;
    }

    public Product findProductInStorehouse(String productCode, String storehouseCode) {
        Product product = null;
        Cursor c = dbHelper.getReadableDatabase().query(TABLE, null, UNIQUE_CONDITION, new String[]{productCode, storehouseCode}, null, null, null);
        if (c.moveToFirst()) {
            product = fromCursor(c);
        }
        c.close();

        return product;
    }

    public void updateProducts(List<Product> products) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            db.beginTransaction();
            db.delete(TABLE, null, null);

            for (Product p : products) {
                insert(db, p);
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            log.error("Error updating rests in local DB.", e);
        } finally {
            db.endTransaction();
        }
    }

    private Product fromCursor(Cursor c) {
        return new Product(
                c.getString(c.getColumnIndex("code")),
                c.getString(c.getColumnIndex("name")),
                c.getString(c.getColumnIndex("store_packs")),
                c.getString(c.getColumnIndex("store_rest")),
                c.getDouble(c.getColumnIndex("price")),
                c.getInt(c.getColumnIndex("num_in_pack")),
                c.getDouble(c.getColumnIndex("weight")),
                new Storehouse(
                        c.getString(c.getColumnIndex("store_code")),
                        c.getString(c.getColumnIndex("store_name"))
                )
        );
    }

    public void insert(SQLiteDatabase db, Product p) {
        db = dbHelper.getWritableDatabase();

        ContentValues cv = makeContentValues(p);

        db.insert(TABLE, null, cv);
    }

    public void update(Product p) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues cv = makeContentValues(p);

        db.update(TABLE, cv, UNIQUE_CONDITION, new String[]{p.getCode(), p.getStorehouse().getCode()});
    }

    private ContentValues makeContentValues(Product p) {
        ContentValues cv = new ContentValues();

        cv.put("code", p.getCode());
        cv.put("name", p.getName());
        cv.put("product_filter", String.format("%s %s", p.getCode(), p.getName().toUpperCase()));
        cv.put("store_code", p.getStorehouse().getCode());
        cv.put("store_name", p.getStorehouse().getName());
        cv.put("store_packs", p.getStorehousePacks());
        cv.put("store_rest", p.getStorehouseRest());
        cv.put("price", p.getPrice());
        cv.put("num_in_pack", p.getCountInPack());
        cv.put("weight", p.getWeight());

        return cv;
    }
}
