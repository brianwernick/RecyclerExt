package com.devbrackets.android.recyclerextdemo.ui.fragment

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devbrackets.android.recyclerext.adapter.DelegatedAdapter
import com.devbrackets.android.recyclerext.adapter.delegate.ViewHolderBinder
import com.devbrackets.android.recyclerextdemo.data.database.ItemDAO
import com.devbrackets.android.recyclerextdemo.ui.fragment.shared.BaseFragment
import com.devbrackets.android.recyclerextdemo.ui.viewholder.SimpleTextViewHolder

/**
 * An example of the [DelegatedAdapter]
 */
class DelegatedFragment : BaseFragment() {

    private fun setupAdapter(): RecyclerView.Adapter<*> {
        return Adapter(getItems()).apply {
            registerViewHolderBinder(1, PlainBinder())
            registerViewHolderBinder(2, ColorBinder())
        }
    }

    override fun onSetupRecyclerView() {
        recyclerView.adapter = setupAdapter()
        recyclerView.layoutManager = LinearLayoutManager(activity)
    }

    private inner class Adapter(val items: List<ItemDAO>): DelegatedAdapter<ItemDAO>() {
        override fun getItemCount(): Int {
            return items.size
        }

        override fun getItem(position: Int): ItemDAO {
            return items[position]
        }

        override fun getItemViewType(position: Int): Int {
            return (position % 2) + 1 // 1 or 2
        }
    }

    /**
     * Binder 1
     *  - Plain text
     */
    private inner class PlainBinder: ViewHolderBinder<SimpleTextViewHolder, ItemDAO>() {
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
    private inner class ColorBinder: ViewHolderBinder<SimpleTextViewHolder, ItemDAO>() {
        private val inflater = requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleTextViewHolder {
            return SimpleTextViewHolder.newInstance(inflater, parent).apply {
                setBackgroundColor(-0x333334)
            }
        }

        override fun onBindViewHolder(holder: SimpleTextViewHolder, item: ItemDAO, position: Int) {
            holder.setText(item.text)
        }
    }
}