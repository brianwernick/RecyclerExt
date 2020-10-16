package com.devbrackets.android.recyclerextdemo.data.database

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import java.util.*

/**
 * A simple database object
 *
 * NOTE:
 * Ideally the [.C_ORDER] column would have a UNIQUE constraint however since sql updates columns in
 * arbitrary order the shuffling on a reorder would hit constraint issues.  Additionally, since SQLite doesn't
 * support dropping constraints we cannot temporarily remove it.
 */
class ItemDAO {
    var id = NEW_ITEM_ID.toLong()
    var order = INVALID_ORDER.toLong()
    var text: String? = null

    constructor() {}
    constructor(cursor: Cursor) {
        id = cursor.getLong(cursor.getColumnIndexOrThrow(C_ID))
        text = cursor.getString(cursor.getColumnIndexOrThrow(C_TEXT))
        order = cursor.getLong(cursor.getColumnIndexOrThrow(C_ORDER))
    }

    constructor(text: String?) {
        this.text = text
    }

    fun save(database: SQLiteDatabase?) {
        if (database == null) {
            return
        }

        //Determine if this is a new item or an update
        //NOTE: since this is a demo, we won't verify that the item with id actually exists before updating it
        if (id != NEW_ITEM_ID.toLong()) {
            update(database)
        } else {
            create(database)
        }
    }

    private fun create(database: SQLiteDatabase) {
        val orderQuery = "SELECT COUNT(" + C_ID + ") AS count FROM " + TABLE_NAME
        val c = database.rawQuery(orderQuery, null)
        var currentItemCount = 0
        if (c != null) {
            if (c.moveToFirst()) {
                currentItemCount = c.getInt(c.getColumnIndexOrThrow("count"))
            }
            c.close()
        }
        val values = ContentValues()
        values.put(C_TEXT, text)
        values.put(C_ORDER, currentItemCount)

        //NOTE: in a real instance you would get the generated C_ID and store it in the id field
        database.insert(TABLE_NAME, null, values)
    }

    private fun update(database: SQLiteDatabase) {
        val values = ContentValues()
        values.put(C_ID, id)
        values.put(C_TEXT, text)
        values.put(C_ORDER, order)
        database.update(TABLE_NAME, values, C_ID + "=?", arrayOf(id.toString()))
    }

    override fun toString(): String {
        return text!!
    }

    companion object {
        private const val NEW_ITEM_ID = -1
        private const val INVALID_ORDER = -1
        const val TABLE_NAME = "items"
        const val C_ID = "_id"
        const val C_TEXT = "text"
        const val C_ORDER = "item_order"
        const val UPDATE_ORDER_LESS = "UPDATE " + TABLE_NAME +
                " SET " + C_ORDER + " = " + C_ORDER + " - 1 " +
                "WHERE " + C_ORDER + ">= ? AND " + C_ORDER + " <= ?"
        const val UPDATE_ORDER_MORE = "UPDATE " + TABLE_NAME +
                " SET " + C_ORDER + " = " + C_ORDER + " + 1 " +
                "WHERE " + C_ORDER + ">= ? AND " + C_ORDER + " <= ?"
        const val DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME
        const val CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "(" +
                C_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                C_TEXT + " TEXT, " +
                C_ORDER + " INTEGER " +
                ");"
        val COLUMNS = arrayOf(
                C_ID,
                C_TEXT,
                C_ORDER
        )

        fun getItem(database: SQLiteDatabase?, itemId: Int): ItemDAO? {
            if (database == null) {
                return null
            }
            val cursor = database.query(TABLE_NAME, COLUMNS, C_ID + "=?", arrayOf(itemId.toString()), null, null, null, null)
                    ?: return null
            cursor.moveToFirst()
            val item = ItemDAO()
            item.id = cursor.getLong(cursor.getColumnIndexOrThrow(C_ID))
            item.text = cursor.getString(cursor.getColumnIndexOrThrow(C_TEXT))
            item.order = cursor.getLong(cursor.getColumnIndexOrThrow(C_ORDER))
            cursor.close()
            return item
        }

        fun getItemByOrder(database: SQLiteDatabase?, order: Long): ItemDAO? {
            if (database == null) {
                return null
            }
            val cursor = database.query(TABLE_NAME, COLUMNS, C_ORDER + "=?", arrayOf(order.toString()), null, null, null, null)
                    ?: return null
            cursor.moveToFirst()
            val item = ItemDAO()
            item.id = cursor.getLong(cursor.getColumnIndexOrThrow(C_ID))
            item.text = cursor.getString(cursor.getColumnIndexOrThrow(C_TEXT))
            item.order = cursor.getLong(cursor.getColumnIndexOrThrow(C_ORDER))
            cursor.close()
            return item
        }

        fun findAll(database: SQLiteDatabase?): List<ItemDAO?> {
            if (database == null) {
                return LinkedList()
            }
            val cursor = database.query(TABLE_NAME, COLUMNS, null, null, null, null, C_ORDER + " ASC", null)
                    ?: return LinkedList()
            val items: MutableList<ItemDAO?> = LinkedList()
            if (cursor.moveToFirst()) {
                do {
                    val item = ItemDAO()
                    item.id = cursor.getLong(cursor.getColumnIndexOrThrow(C_ID))
                    item.text = cursor.getString(cursor.getColumnIndexOrThrow(C_TEXT))
                    item.order = cursor.getLong(cursor.getColumnIndexOrThrow(C_ORDER))
                    items.add(item)
                } while (cursor.moveToNext())
            }
            cursor.close()
            return items
        }

        fun findCursorAll(database: SQLiteDatabase?): Cursor? {
            return database?.query(TABLE_NAME, COLUMNS, null, null, null, null, C_ORDER + " ASC", null)
        }

        /**
         * Updates the orderings between the original and new positions
         */
        fun updateOrdering(database: SQLiteDatabase, originalPosition: Long, newPosition: Long) {
            Log.d("ItemDAO", "original: $originalPosition, newPosition:$newPosition")
            if (originalPosition > newPosition) {
                database.execSQL(UPDATE_ORDER_MORE, arrayOf(newPosition.toString(), originalPosition.toString()))
            } else { //newPosition > originalPosition
                database.execSQL(UPDATE_ORDER_LESS, arrayOf(originalPosition.toString(), newPosition.toString()))
            }
        }
    }
}