package com.devbrackets.android.recyclerextdemo.data.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.LinkedList;
import java.util.List;

/**
 * A simple database object
 *
 * NOTE:
 * Ideally the {@link #C_ORDER} column would have a UNIQUE constraint however since sql updates columns in
 * arbitrary order the shuffling on a reorder would hit constraint issues.  Additionally, since SQLite doesn't
 * support dropping constraints we cannot temporarily remove it.
 */
public class ItemDAO {
    private static final int NEW_ITEM_ID = -1;
    private static final int INVALID_ORDER = -1;

    public static final String TABLE_NAME = "items";
    public static final String C_ID = "_id";
    public static final String C_TEXT = "text";
    public static final String C_ORDER = "item_order";

    public static final String UPDATE_ORDER_LESS = "UPDATE " + TABLE_NAME +
            " SET " + C_ORDER + " = " + C_ORDER + " - 1 " +
            "WHERE " + C_ORDER + ">= ? AND " + C_ORDER + " <= ?";
    public static final String UPDATE_ORDER_MORE = "UPDATE " + TABLE_NAME +
            " SET " + C_ORDER + " = " + C_ORDER + " + 1 " +
            "WHERE " + C_ORDER + ">= ? AND " + C_ORDER + " <= ?";

    public static final String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    public static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "(" +
            C_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            C_TEXT + " TEXT, " +
            C_ORDER + " INTEGER " +
            ");";

    public static final String[] COLUMNS = new String[] {
            C_ID,
            C_TEXT,
            C_ORDER
    };

    private long id = NEW_ITEM_ID;
    private long order = INVALID_ORDER;
    private String text;

    public ItemDAO() {
    }

    public ItemDAO(Cursor cursor) {
        setId(cursor.getLong(cursor.getColumnIndexOrThrow(C_ID)));
        setText(cursor.getString(cursor.getColumnIndexOrThrow(C_TEXT)));
        setOrder(cursor.getLong(cursor.getColumnIndexOrThrow(C_ORDER)));
    }

    public ItemDAO(String text) {
        this.text = text;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public long getOrder() {
        return order;
    }

    public void setOrder(long order) {
        this.order = order;
    }

    public void save(SQLiteDatabase database) {
        if (database == null) {
            return;
        }

        //Determine if this is a new item or an update
        //NOTE: since this is a demo, we won't verify that the item with id actually exists before updating it
        if (id != NEW_ITEM_ID) {
            update(database);
        } else {
            create(database);
        }
    }

    @Nullable
    public static ItemDAO getItem(SQLiteDatabase database, int itemId) {
        if (database == null) {
            return null;
        }

        Cursor cursor = database.query(TABLE_NAME, COLUMNS, C_ID + "=?", new String[] { String.valueOf(itemId) }, null, null, null, null);
        if (cursor == null) {
            return null;
        }

        cursor.moveToFirst();

        ItemDAO item = new ItemDAO();
        item.setId(cursor.getLong(cursor.getColumnIndexOrThrow(C_ID)));
        item.setText(cursor.getString(cursor.getColumnIndexOrThrow(C_TEXT)));
        item.setOrder(cursor.getLong(cursor.getColumnIndexOrThrow(C_ORDER)));

        cursor.close();

        return item;
    }

    @Nullable
    public static ItemDAO getItemByOrder(SQLiteDatabase database, long order) {
        if (database == null) {
            return null;
        }

        Cursor cursor = database.query(TABLE_NAME, COLUMNS, C_ORDER + "=?", new String[] { String.valueOf(order) }, null, null, null, null);
        if (cursor == null) {
            return null;
        }

        cursor.moveToFirst();

        ItemDAO item = new ItemDAO();
        item.setId(cursor.getLong(cursor.getColumnIndexOrThrow(C_ID)));
        item.setText(cursor.getString(cursor.getColumnIndexOrThrow(C_TEXT)));
        item.setOrder(cursor.getLong(cursor.getColumnIndexOrThrow(C_ORDER)));

        cursor.close();

        return item;
    }

    public static List<ItemDAO> findAll(SQLiteDatabase database) {
        if (database == null) {
            return new LinkedList<>();
        }

        Cursor cursor = database.query(TABLE_NAME, COLUMNS, null, null, null, null, C_ORDER + " ASC", null);
        if (cursor == null) {
            return new LinkedList<>();
        }

        List<ItemDAO> items = new LinkedList<>();
        if (cursor.moveToFirst()) {
            do {
                ItemDAO item = new ItemDAO();
                item.setId(cursor.getLong(cursor.getColumnIndexOrThrow(C_ID)));
                item.setText(cursor.getString(cursor.getColumnIndexOrThrow(C_TEXT)));
                item.setOrder(cursor.getLong(cursor.getColumnIndexOrThrow(C_ORDER)));
                items.add(item);
            } while(cursor.moveToNext());
        }

        cursor.close();

        return items;
    }

    @Nullable
    public static Cursor findCursorAll(SQLiteDatabase database) {
        if (database == null) {
            return null;
        }

        return database.query(TABLE_NAME, COLUMNS, null, null, null, null, C_ORDER + " ASC", null);
    }

    /**
     * Updates the orderings between the original and new positions
     */
    public static void updateOrdering(SQLiteDatabase database, long originalPosition, long newPosition) {
        Log.d("ItemDAO", "original: " + originalPosition + ", newPosition:" + newPosition);

        if (originalPosition > newPosition) {
            database.execSQL(UPDATE_ORDER_MORE, new String[]{String.valueOf(newPosition), String.valueOf(originalPosition)});
        } else { //newPosition > originalPosition
            database.execSQL(UPDATE_ORDER_LESS, new String[]{String.valueOf(originalPosition), String.valueOf(newPosition)});
        }
    }

    private void create(@NonNull SQLiteDatabase database) {
        String orderQuery = "SELECT COUNT(" + C_ID + ") AS count FROM " + TABLE_NAME;
        Cursor c = database.rawQuery(orderQuery, null);
        int currentItemCount = 0;

        if (c != null) {
            if (c.moveToFirst()) {
                currentItemCount = c.getInt(c.getColumnIndexOrThrow("count"));
            }

            c.close();
        }

        ContentValues values = new ContentValues();
        values.put(C_TEXT, text);
        values.put(C_ORDER, currentItemCount);

        //NOTE: in a real instance you would get the generated C_ID and store it in the id field
        database.insert(TABLE_NAME, null, values);
    }

    private void update(@NonNull SQLiteDatabase database) {
        ContentValues values = new ContentValues();
        values.put(C_ID, id);
        values.put(C_TEXT, text);
        values.put(C_ORDER, order);

        database.update(TABLE_NAME, values, C_ID + "=?", new String[] {String.valueOf(id)});
    }

    @Override
    public String toString() {
        return text;
    }
}
