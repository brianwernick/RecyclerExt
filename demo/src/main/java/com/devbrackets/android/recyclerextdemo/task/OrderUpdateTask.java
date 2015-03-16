package com.devbrackets.android.recyclerextdemo.task;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import com.devbrackets.android.recyclerext.adapter.ReorderableRecyclerCursorAdapter;
import com.devbrackets.android.recyclerextdemo.database.ItemDAO;

public class OrderUpdateTask extends AsyncTask<Void, Void, Cursor> {
    private SQLiteDatabase database;
    private ReorderableRecyclerCursorAdapter.ReorderItem reorderItem;
    private DBUpdateListener listener;

    /**
     * For simplicity sake I will only perform a single reorder update at a time.
     *
     * @param reorderItem The ReorderItem to update the database with
     */
    public OrderUpdateTask(SQLiteDatabase database, ReorderableRecyclerCursorAdapter.ReorderItem reorderItem, DBUpdateListener listener) {
        this.database = database;
        this.reorderItem = reorderItem;
        this.listener = listener;
    }

    @Override
    protected Cursor doInBackground(Void... params) {
        //Save off the item being changed with an invalid order (-1 in this case)
        ItemDAO item = ItemDAO.getItemByOrder(database, reorderItem.getOriginalPosition());
        if (item == null) {
            return ItemDAO.findCursorAll(database);
        }

        item.setOrder(-1);
        item.save(database);


        //Update any items that are affected by the update
        ItemDAO.updateOrdering(database, reorderItem.getOriginalPosition(), reorderItem.getNewPosition());


        //Place the item being changed in the ending order
        item.setOrder(reorderItem.getNewPosition());
        item.save(database);

        return ItemDAO.findCursorAll(database);
    }

    @Override
    protected void onPostExecute(Cursor cursor) {
        if (listener != null) {
            listener.onDBUpdated(cursor, reorderItem.getId());
        }
    }

    public interface DBUpdateListener {
        void onDBUpdated(Cursor cursor, long reorderId);
    }
}
