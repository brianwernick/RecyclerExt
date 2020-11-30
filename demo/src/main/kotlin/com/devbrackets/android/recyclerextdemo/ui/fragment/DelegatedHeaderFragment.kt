package com.devbrackets.android.recyclerextdemo.ui.fragment

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devbrackets.android.recyclerext.adapter.DelegatedAdapter
import com.devbrackets.android.recyclerext.adapter.DelegatedHeaderAdapter
import com.devbrackets.android.recyclerext.adapter.delegate.ViewHolderBinder
import com.devbrackets.android.recyclerextdemo.data.database.ItemDAO
import com.devbrackets.android.recyclerextdemo.ui.fragment.shared.BaseFragment
import com.devbrackets.android.recyclerextdemo.ui.viewholder.SimpleTextViewHolder

/**
 * An example of the [DelegatedAdapter]
 */
class DelegatedHeaderFragment : BaseFragment() {

    private fun setupAdapter(): RecyclerView.Adapter<*> {
        return Adapter(getItems()).apply {
            // Headers
            registerDefaultHeaderViewHolderBinder(HeaderBinder())

            // Children
            registerChildViewHolderBinder(1, PlainBinder())
            registerDefaultViewHolderBinder(ColorBinder()) // Default, will handle the 2 type
        }
    }

    override fun onSetupRecyclerView() {
        recyclerView.adapter = setupAdapter()
        recyclerView.layoutManager = LinearLayoutManager(activity)

        // NOTE: For sticky headers see HeaderListFragment
    }

    private inner class Adapter(val items: List<ItemDAO>): DelegatedHeaderAdapter<ItemDAO>() {
        override val childCount = items.size

        override fun getItem(position: Int): ItemDAO {
            return items[position]
        }

        override fun getChildViewType(childPosition: Int): Int {
            return (childPosition % 2) + 1 // 1 or 2
        }

        override fun getHeaderId(childPosition: Int): Long {
            return (items[childPosition].order + 1) / 50
        }
    }

    private inner class HeaderBinder: ViewHolderBinder<ItemDAO, SimpleTextViewHolder>() {
        private val inflater = requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleTextViewHolder {
            return SimpleTextViewHolder.newInstance(inflater, parent).apply {
                itemView.setBackgroundColor(-0x9933aa) // Green
            }
        }

        override fun onBindViewHolder(holder: SimpleTextViewHolder, item: ItemDAO, position: Int) {
            val headerId = (item.order + 1) / 50
            holder.setText("Section ${headerId +1}")
        }
    }

    /**
     * Binder 1
     *  - Plain text
     */
    private inner class PlainBinder: ViewHolderBinder<ItemDAO, SimpleTextViewHolder>() {
        private val inflater = requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleTextViewHolder {
            return SimpleTextViewHolder.newInstance(inflater, parent)
        }

        override fun onBindViewHolder(holder: SimpleTextViewHolder, item: ItemDAO, position: Int) {
            holder.setText(item.text)
        }
    }

    /**
     * Binder 2
     *  - Plain Text
     *  - Colored Background
     */
    private inner class ColorBinder: ViewHolderBinder<ItemDAO, SimpleTextViewHolder>() {
        private val inflater = requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleTextViewHolder {
            return SimpleTextViewHolder.newInstance(inflater, parent).apply {
                setBackgroundColor(-0x333333) // Gray
            }
        }

        override fun onBindViewHolder(holder: SimpleTextViewHolder, item: ItemDAO, position: Int) {
            holder.setText(item.text)
        }
    }
}