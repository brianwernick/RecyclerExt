package com.devbrackets.android.recyclerextdemo.task;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.SparseIntArray;

import com.devbrackets.android.recyclerextdemo.database.ItemDAO;

import java.util.List;

public class OrderUpdateTask extends AsyncTask<Void, Void, Cursor> {
    private SQLiteDatabase database;
    private DBUpdateListener listener;
    private SparseIntArray positionMap;

    /**
     * For simplicity sake I will only perform a single reorder update at a time.
     */
    public OrderUpdateTask(SQLiteDatabase database, SparseIntArray positionMap, DBUpdateListener listener) {
        this.database = database;
        this.positionMap = positionMap;
        this.listener = listener;
    }

    @Override
    protected Cursor doInBackground(Void... params) {
        database.beginTransaction();
        saveUpdates();
        database.setTransactionSuccessful();
        database.endTransaction();

        return ItemDAO.findCursorAll(database);
    }

    @Override
    protected void onPostExecute(Cursor cursor) {
        if (listener != null) {
            listener.onDBUpdated(cursor);
        }
    }

    private void saveUpdates() {
        List<ItemDAO> items = ItemDAO.findAll(database);

        //Iterates through the changed indexes
        for (int i = 0; i < items.size(); i++) {
            int originalPosition = positionMap.get(i, -1);

            //If the item was moved then the value is the original position, and the key the new position
            if (originalPosition != -1) {
                ItemDAO item = items.get(originalPosition);
                if (item != null) {
                    item.setOrder(i);
                    item.save(database);
                }
            }
        }
    }

    public interface DBUpdateListener {
        void onDBUpdated(Cursor cursor);
    }
}
