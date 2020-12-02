package com.devbrackets.android.recyclerextdemo.data.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * A simple utility for interacting with the database
 */
class DBHelper(context: Context?) : SQLiteOpenHelper(context, database_NAME, null, database_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(ItemDAO.CREATE_TABLE)
        populateItemDAOTable(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(ItemDAO.DROP_TABLE)
        onCreate(db)
    }

    /**
     * Populates the database with dummy items to be used for any of the
     * cursor, and most of the list examples.
     *
     * @param database The database to populate
     */
    private fun populateItemDAOTable(database: SQLiteDatabase) {
        //Only add items if we haven't already
        val items: List<ItemDAO?> = ItemDAO.findAll(database)
        if (items.isNotEmpty()) {
            database.close()
            return
        }

        //create and save some dummy items...
        for (i in 1..500) {
            val item = ItemDAO("RecyclerExt Demo Item $i")
            item.save(database)
        }
    }

    companion object {
        private const val database_VERSION = 1
        private const val database_NAME = "demo"
    }
}