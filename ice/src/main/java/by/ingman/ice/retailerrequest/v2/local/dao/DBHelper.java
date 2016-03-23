package by.ingman.ice.retailerrequest.v2.local.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created with IntelliJ IDEA.
 * User: Администратор
 * Date: 03.06.13
 * Time: 0:24
 * To change this template use File | Settings | File Templates.
 */
public class DBHelper extends SQLiteOpenHelper {
    public static String DATABASE_NAME = "iceDB";

    private static final int DATABASE_VERSION = 10;


    public DBHelper(Context context) {
        // конструктор суперкласса
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // создаем таблицу с полями
        db.execSQL("create table " + OrderLocalDao.TABLE + " ("
                + "_id integer primary key autoincrement,"
                + "order_id text not null,"
                + "manager text not null,"
                + "is_advertising integer not null,"
                + "order_date integer not null,"
                + "contragent_code text not null,"
                + "contragent_name text not null,"
                + "sale_point_code text not null,"
                + "sale_point_name text not null,"
                + "storehouse_code text not null,"
                + "storehouse_name text not null,"
                + "product_code text not null,"
                + "product_name text not null,"
                + "packs_num real not null,"
                + "product_num integer not null,"
                + "comment text,"
                + "processed integer not null,"
                + "sent integer not null);");

        db.execSQL("create table " + OrderLocalDao.TABLE_ANSWER + " ("
                + "_id integer primary key autoincrement,"
                + "order_id text not null,"
                + "description text not null,"
                + "date_unload integer not null);");

        db.execSQL("create table " + ProductLocalDao.TABLE + " ("
                + "_id integer primary key autoincrement,"
                + "code text not null,"
                + "name text not null,"
                + "store_code text not null,"
                + "store_name text not null,"
                + "store_packs text not null,"
                + "store_rest text not null,"
                + "price real not null,"
                + "num_in_pack integer not null,"
                + "weight real not null" + ");");

        db.execSQL("create table " + DebtsLocalDao.TABLE + " ("
                + "_id integer primary key autoincrement,"
                + "code_k text not null,"
                + "rating text not null,"
                + "debt text not null,"
                + "overdue text not null" + ");");

        db.execSQL("create table " + ContrAgentLocalDao.TABLE + " ("
                + "_id integer primary key autoincrement,"
                + "code text not null,"
                + "name text not null,"
                + "sp_code text not null,"
                + "sp_name text not null" + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + OrderLocalDao.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + ProductLocalDao.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + DebtsLocalDao.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + ContrAgentLocalDao.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + OrderLocalDao.TABLE_ANSWER);
        db.execSQL("DROP TABLE IF EXISTS request");
        onCreate(db);
    }


}
