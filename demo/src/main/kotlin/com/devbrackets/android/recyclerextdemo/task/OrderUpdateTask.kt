package com.devbrackets.android.recyclerextdemo.task

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.AsyncTask
import android.util.SparseIntArray
import com.devbrackets.android.recyclerextdemo.data.database.ItemDAO

/**
 * An example task for updating the ordering in the database
 */
class OrderUpdateTask(
        private val database: SQLiteDatabase,
        private val positionMap: SparseIntArray,
        private val listener: DBUpdateListener?
) : AsyncTask<Void?, Void?, Cursor?>() {
    override fun doInBackground(vararg params: Void?): Cursor? {
        database.beginTransaction()
        saveUpdates()
        database.setTransactionSuccessful()
        database.endTransaction()
        return ItemDAO.Companion.findCursorAll(database)
    }

    override fun onPostExecute(cursor: Cursor?) {
        listener?.onDBUpdated(cursor)
    }

    /**
     * Iterate through all the items, updating the order column to the new
     * position.
     */
    private fun saveUpdates() {
        val items: List<ItemDAO?> = ItemDAO.Companion.findAll(database)

        //Iterates through the changed indexes
        for (i in items.indices) {
            val originalPosition = positionMap[i, -1]

            //If the item was moved then the value is the original position, and the key the new position
            if (originalPosition != -1) {
                val item = items[originalPosition]
                if (item != null) {
                    item.order = i.toLong()
                    item.save(database)
                }
            }
        }
    }

    interface DBUpdateListener {
        fun onDBUpdated(cursor: Cursor?)
    }
}