package by.ingman.ice.retailerrequest.v2.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import by.ingman.ice.retailerrequest.v2.structure.Request;

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
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_REQUESTS_NAME);
        db.execSQL("create table " +  TABLE_REQUESTS_NAME +  " ("
                + "_id integer primary key autoincrement,"
                + "is_req integer not null,"
                + "sent integer not null,"
                + "req_id text not null,"
                + "date text not null,"
                + "req text not null" + ");");
    }

    public Request getSingleRequestByRequestId(String requestId) {
        Request request = null;
        ContentValues cv = new ContentValues();
        cv.put("req_id", requestId);
        Cursor cursor = getReadableDatabase().query(TABLE_REQUESTS_NAME, new String[]{"req"}, "req_id", new String[]{requestId}, null, null, null);
        if (cursor.moveToFirst()) {
            String requestString = cursor.getString(0);
            request = GsonHelper.createGson().fromJson(requestString, Request.class);
        }
        cursor.close();

        return request;
    }
}
