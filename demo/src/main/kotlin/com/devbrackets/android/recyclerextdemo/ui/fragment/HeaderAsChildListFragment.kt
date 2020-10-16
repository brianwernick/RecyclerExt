package com.devbrackets.android.recyclerextdemo.ui.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devbrackets.android.recyclerext.adapter.HeaderListAdapter
import com.devbrackets.android.recyclerext.decoration.StickyHeaderDecoration
import com.devbrackets.android.recyclerextdemo.R
import com.devbrackets.android.recyclerextdemo.data.database.DBHelper
import com.devbrackets.android.recyclerextdemo.data.database.ItemDAO
import com.devbrackets.android.recyclerextdemo.ui.fragment.HeaderAsChildListFragment.HeaderAdapter
import com.devbrackets.android.recyclerextdemo.ui.viewholder.ContactsHeaderViewHolder
import com.devbrackets.android.recyclerextdemo.ui.viewholder.SimpleTextViewHolder

/**
 * An example of the [HeaderAdapter]
 * that has the display style of the Lollipop and Marshmallow Contacts app
 * using the [StickyHeaderDecoration] to keep the header at the top of the screen when reached.
 */
class HeaderAsChildListFragment : Fragment() {
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
     * Retrieves the items from the database, and sets the layout manager, adapter, and sticky decoration
     * on the RecyclerView.
     */
    private fun setupRecyclerExt() {
        val adapter = HeaderAdapter(requireContext(), ItemDAO.findAll(dbHelper!!.writableDatabase))
        recyclerView!!.adapter = adapter
        recyclerView!!.layoutManager = LinearLayoutManager(activity)

        //OPTIONAL: The StickyHeaderDecoration is used to keep the current header always visible
        recyclerView!!.addItemDecoration(StickyHeaderDecoration(recyclerView!!))
    }

    /**
     * The adapter that extends the [com.devbrackets.android.recyclerext.adapter.HeaderAdapter] to provide the
     * minimum number of methods to function
     */
    private inner class HeaderAdapter(context: Context, items: List<ItemDAO?>) : HeaderListAdapter<ContactsHeaderViewHolder, SimpleTextViewHolder, ItemDAO?>(items) {
        private val inflater: LayoutInflater
        override fun onCreateHeaderViewHolder(parent: ViewGroup, viewType: Int): ContactsHeaderViewHolder {
            return ContactsHeaderViewHolder.newInstance(inflater, parent)
        }

        override fun onCreateChildViewHolder(parent: ViewGroup, viewType: Int): SimpleTextViewHolder {
            return SimpleTextViewHolder.newInstance(inflater, parent)
        }

        override fun onBindHeaderViewHolder(holder: ContactsHeaderViewHolder, firstChildPosition: Int) {
            val item = getItem(firstChildPosition)
            holder.setText(item?.text)
            holder.setRegionText(getHeaderId(firstChildPosition).toString() + "")
        }

        override fun onBindChildViewHolder(holder: SimpleTextViewHolder, childPosition: Int) {
            holder.setText(items[childPosition]?.text)
            holder.setSpacingVisible(true)
        }

        /**
         * For simplicity sake, we just return a simple mathematical id for the headers.
         * You should provide an actual id.
         */
        override fun getHeaderId(childPosition: Int): Long {
            return (getItem(childPosition)?.order ?: 0 + 1) / 10
        }

        /**
         * Specifying this will make only the number field from the header be sticky
         */
        override val customStickyHeaderViewId: Int
            get() = R.id.contacts_header_item_region_text_view

        init {
            inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

            //This is the call that makes the adapter treat the headers position as a child
            // e.g. CHILD(position=9, getItem(9)), HEADER(position=10, getItem(10)), CHILD(position=11, getItem(11))
            // whereas normally the header doesn't interfere with the child items
            // e.g. CHILD(position=9, getItem(9)), HEADER(position=10, getItem(10)), CHILD(position=11, getItem(10))
            showHeaderAsChild(true)
        }
    }

    companion object {
        fun newInstance(): HeaderAsChildListFragment {
            return HeaderAsChildListFragment()
        }
    }
}