package com.devbrackets.android.recyclerextdemo.ui.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IntRange
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devbrackets.android.recyclerext.adapter.HeaderListAdapter
import com.devbrackets.android.recyclerext.adapter.viewholder.ClickableViewHolder
import com.devbrackets.android.recyclerext.decoration.StickyHeaderDecoration
import com.devbrackets.android.recyclerext.widget.FastScroll
import com.devbrackets.android.recyclerext.widget.FastScroll.PopupCallbacks
import com.devbrackets.android.recyclerextdemo.R
import com.devbrackets.android.recyclerextdemo.data.database.DBHelper
import com.devbrackets.android.recyclerextdemo.data.database.ItemDAO
import com.devbrackets.android.recyclerextdemo.ui.fragment.HeaderListFragment.HeaderAdapter
import com.devbrackets.android.recyclerextdemo.ui.viewholder.SimpleTextViewHolder

/**
 * An example of the [HeaderAdapter]
 * and using the [StickyHeaderDecoration] to keep the header at the top of the screen when reached.
 */
class HeaderListFragment : Fragment() {
    private var dbHelper: DBHelper? = null
    private var recyclerView: RecyclerView? = null
    private var parent: ViewGroup? = null
    private var fastScroll: FastScroll? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_recycler, container, false)
        parent = view.findViewById(R.id.parent)
        recyclerView = view.findViewById(R.id.recyclerext_fragment_recycler)
        fastScroll = view.findViewById(R.id.recyclerext_fast_scroll)
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        //Makes sure the database is initialized and open for use
        dbHelper = DBHelper(activity)
        setupRecyclerExt()
    }

    /**
     * Retrieves the items from the database, and sets the layout manager, adapter, and sticky decoration
     * on the RecyclerView.
     */
    private fun setupRecyclerExt() {
        val adapter = HeaderAdapter(requireContext(), ItemDAO.findAll(dbHelper!!.writableDatabase))
        recyclerView!!.adapter = adapter
        recyclerView!!.layoutManager = LinearLayoutManager(activity)
        fastScroll!!.attach(recyclerView!!)

        //OPTIONAL: The StickyHeaderDecoration is used to keep the current header always visible
        val decoration = StickyHeaderDecoration(recyclerView!!)
        decoration.enableStickyHeaderTouches(parent!!)
        recyclerView!!.addItemDecoration(decoration)
    }

    /**
     * The adapter that extends the [com.devbrackets.android.recyclerext.adapter.HeaderAdapter] to provide the
     * minimum number of methods to function
     */
    private inner class HeaderAdapter(context: Context, items: List<ItemDAO?>) : HeaderListAdapter<SimpleTextViewHolder, SimpleTextViewHolder, ItemDAO?>(items), PopupCallbacks {
        private val inflater: LayoutInflater
        override fun onCreateHeaderViewHolder(parent: ViewGroup, viewType: Int): SimpleTextViewHolder {
            val holder: SimpleTextViewHolder = SimpleTextViewHolder.newInstance(inflater, parent)
            holder.setOnClickListener { viewHolder: ClickableViewHolder -> viewHolder.itemView.setBackgroundColor(-0xbb5534) }
            return holder
        }

        override fun onCreateChildViewHolder(parent: ViewGroup, viewType: Int): SimpleTextViewHolder {
            return SimpleTextViewHolder.newInstance(inflater, parent)
        }

        override fun onBindHeaderViewHolder(holder: SimpleTextViewHolder, firstChildPosition: Int) {
            holder.setText(getHeaderId(firstChildPosition).toString() + "0s")
            holder.setBackgroundColor(-0x333334)
        }

        override fun onBindChildViewHolder(holder: SimpleTextViewHolder, childPosition: Int) {
            holder.setText(items[childPosition]?.text)
        }

        /**
         * For simplicity sake, we just return a simple mathematical id for the headers.
         * You should provide an actual id.
         */
        override fun getHeaderId(childPosition: Int): Long {
            return (items[childPosition]?.order ?: 0 + 1) / 10
        }

        override fun getSectionId(@IntRange(from = 0) position: Int): Long {
            return ((getChildPosition(position) + 1) / 10).toLong()
        }

        override fun getPopupText(position: Int, sectionId: Long): String {
            return sectionId.toString() + "0s"
        }

        init {
            inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        }
    }

    companion object {
        fun newInstance(): HeaderListFragment {
            return HeaderListFragment()
        }
    }
}