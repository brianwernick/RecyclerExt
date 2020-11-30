package com.devbrackets.android.recyclerextdemo.ui.fragment

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.IntRange
import androidx.recyclerview.widget.LinearLayoutManager
import com.devbrackets.android.recyclerext.adapter.HeaderListAdapter
import com.devbrackets.android.recyclerext.adapter.viewholder.ClickableViewHolder
import com.devbrackets.android.recyclerext.decoration.StickyHeaderDecoration
import com.devbrackets.android.recyclerext.widget.FastScroll.PopupCallbacks
import com.devbrackets.android.recyclerextdemo.data.database.ItemDAO
import com.devbrackets.android.recyclerextdemo.ui.fragment.HeaderListFragment.HeaderAdapter
import com.devbrackets.android.recyclerextdemo.ui.fragment.shared.BaseFragment
import com.devbrackets.android.recyclerextdemo.ui.viewholder.SimpleTextViewHolder

/**
 * An example of the [HeaderAdapter]
 * and using the [StickyHeaderDecoration] to keep the header at the top of the screen when reached.
 */
class HeaderListFragment : BaseFragment() {

    override fun onSetupRecyclerView() {
        val adapter = HeaderAdapter(requireContext(), getItems())
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(activity)
        fastScroll.attach(recyclerView)

        // OPTIONAL: The StickyHeaderDecoration is used to keep the current header always visible
        val decoration = StickyHeaderDecoration(recyclerView)
        decoration.enableStickyHeaderTouches(parent)
        recyclerView.addItemDecoration(decoration)
    }

    /**
     * The adapter that extends the [com.devbrackets.android.recyclerext.adapter.HeaderAdapter] to provide the
     * minimum number of methods to function
     */
    private inner class HeaderAdapter(context: Context, items: List<ItemDAO>) : HeaderListAdapter<ItemDAO, SimpleTextViewHolder, SimpleTextViewHolder>(items), PopupCallbacks {
        private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

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
            holder.setText(items[childPosition].text)
        }

        /**
         * For simplicity sake, we just return a simple mathematical id for the headers.
         * You should provide an actual id.
         */
        override fun getHeaderId(childPosition: Int): Long {
            return (items[childPosition].order + 1) / 10
        }

        override fun getSectionId(@IntRange(from = 0) position: Int): Long {
            return ((getChildPosition(position) + 1) / 10).toLong()
        }

        override fun getPopupText(position: Int, sectionId: Long): String {
            return sectionId.toString() + "0s"
        }

    }
}