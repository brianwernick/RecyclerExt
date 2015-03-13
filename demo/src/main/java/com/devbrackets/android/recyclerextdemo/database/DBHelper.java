package com.devbrackets.android.recyclerextdemo.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 *
 */
public class DBHelper extends SQLiteOpenHelper {
    private static final int database_VERSION = 1;
    private static final String database_NAME = "demo";

    public DBHelper(Context context) {
        super(context, database_NAME, null, database_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(ItemDAO.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(ItemDAO.DROP_TABLE);
        this.onCreate(db);
    }
}