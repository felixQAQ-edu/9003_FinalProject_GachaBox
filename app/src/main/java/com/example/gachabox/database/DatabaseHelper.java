package com.example.gachabox.database;
import android.content.ContentValues;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "GachaBox.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_COLLECTION = "Gacha_Collection";
    public static final String COL_ID = "id";
    public static final String COL_NAME = "item_name";
    public static final String COL_RARITY = "rarity";

    private static final String CREATE_TABLE_COLLECTION =
            "CREATE TABLE " + TABLE_COLLECTION + " (" +
                    COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_NAME + " TEXT, " +
                    COL_RARITY + " TEXT)";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 执行 SQL 语句，创建表
        db.execSQL(CREATE_TABLE_COLLECTION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COLLECTION);
        onCreate(db);
    }


    public boolean addGachaItem(String itemName, String rarity) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(COL_NAME, itemName);
        cv.put(COL_RARITY, rarity);

        long insert = db.insert(TABLE_COLLECTION, null, cv);

        db.close();

        return insert != -1;
    }
}