package com.devbrackets.android.recyclerextdemo.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.List;

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
        populateItemDAOTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(ItemDAO.DROP_TABLE);
        this.onCreate(db);
    }

    private void populateItemDAOTable(SQLiteDatabase database) {
        //Only add items if we haven't already
        List<ItemDAO> items = ItemDAO.findAll(database);
        if (items.size() > 0) {
            database.close();
            return;
        }

        //create and save some dummy items...
        for (int i = 1; i <= 200; i++) {
            ItemDAO item = new ItemDAO("RecyclerExt Demo Item " + i);
            item.save(database);
        }

    }
}