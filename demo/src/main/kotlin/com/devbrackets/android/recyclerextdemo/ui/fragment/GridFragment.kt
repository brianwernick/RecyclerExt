package com.devbrackets.android.recyclerextdemo.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.devbrackets.android.recyclerext.adapter.ListAdapter
import com.devbrackets.android.recyclerext.layoutmanager.AutoColumnGridLayoutManager
import com.devbrackets.android.recyclerextdemo.R
import com.devbrackets.android.recyclerextdemo.data.database.DBHelper
import com.devbrackets.android.recyclerextdemo.data.database.ItemDAO
import com.devbrackets.android.recyclerextdemo.ui.viewholder.GridViewHolder

/**
 * An example of the [AutoColumnGridLayoutManager]
 */
class GridFragment : Fragment() {
    private var dbHelper: DBHelper? = null
    private var recyclerView: RecyclerView? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_recycler, container, false)
        recyclerView = view.findViewById(R.id.recyclerext_fragment_recycler)
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        //Makes sure the database is initialized and open for use
        dbHelper = DBHelper(activity)
        setupRecyclerExt()
    }

    /**
     * Retrieves the cursor from the database, and sets the layout manager and adapter
     * on the RecyclerView.
     */
    private fun setupRecyclerExt() {
        val adapter = GridAdapter(ItemDAO.Companion.findAll(dbHelper!!.writableDatabase))

        //Sets up the AutoColumnGridLayoutManager
        val width = activity!!.resources.getDimensionPixelSize(R.dimen.grid_item_width)
        val layoutManager = AutoColumnGridLayoutManager(activity!!, width)
        layoutManager.matchSpacing = true
        layoutManager.setMinEdgeSpacing(120) //This is a pixel value, normally you would retrieve the size similar to the width above
        layoutManager.setMinColumnSpacing(60) //This is a pixel value, normally you would retrieve the size similar to the width above
        layoutManager.spacingMethod = AutoColumnGridLayoutManager.SpacingMethod.ALL
        recyclerView!!.layoutManager = layoutManager
        recyclerView!!.adapter = adapter
    }

    /**
     * The adapter that extends the [com.devbrackets.android.recyclerext.adapter.ListAdapter] to provide the
     * minimum number of methods to function
     */
    private inner class GridAdapter(itemList: List<ItemDAO?>?) : ListAdapter<GridViewHolder, ItemDAO?>(itemList.orEmpty()) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GridViewHolder {
            return GridViewHolder.newInstance(parent)
        }

        override fun onBindViewHolder(holder: GridViewHolder, position: Int) {
            val item = items[position]!!
            holder.setText(if (item.text != null) item.text else "")
        }
    }

    companion object {
        fun newInstance(): GridFragment {
            return GridFragment()
        }
    }
}