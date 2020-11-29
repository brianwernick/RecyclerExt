package com.devbrackets.android.recyclerextdemo.ui.fragment.shared

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.devbrackets.android.recyclerext.widget.FastScroll
import com.devbrackets.android.recyclerextdemo.R
import com.devbrackets.android.recyclerextdemo.data.database.DBHelper
import com.devbrackets.android.recyclerextdemo.data.database.ItemDAO

/**
 * A simple base fragment to handle the boilerplate of the demo fragments to help
 * focus on the use of this library.
 */
abstract class BaseFragment(

): Fragment() {
    protected lateinit var dbHelper: DBHelper
    protected lateinit var recyclerView: RecyclerView
    protected lateinit var parent: ViewGroup
    protected lateinit var fastScroll: FastScroll

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_recycler, container, false).also {
            parent = it.findViewById(R.id.parent)
            recyclerView = it.findViewById(R.id.recyclerext_fragment_recycler)
            fastScroll = it.findViewById(R.id.recyclerext_fast_scroll)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        //Makes sure the database is initialized and open for use
        dbHelper = DBHelper(activity)
        onSetupRecyclerView()
    }

    abstract fun onSetupRecyclerView()

    fun getItems(): List<ItemDAO> {
        return ItemDAO.findAll(dbHelper.writableDatabase)
    }
}