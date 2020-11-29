package com.devbrackets.android.recyclerextdemo.ui.fragment

import android.view.ViewGroup
import com.devbrackets.android.recyclerext.adapter.ListAdapter
import com.devbrackets.android.recyclerext.layoutmanager.AutoColumnGridLayoutManager
import com.devbrackets.android.recyclerextdemo.R
import com.devbrackets.android.recyclerextdemo.data.database.ItemDAO
import com.devbrackets.android.recyclerextdemo.ui.fragment.shared.BaseFragment
import com.devbrackets.android.recyclerextdemo.ui.viewholder.GridViewHolder

/**
 * An example of the [AutoColumnGridLayoutManager]
 */
class GridFragment : BaseFragment() {

    override fun onSetupRecyclerView() {
        val width = requireActivity().resources.getDimensionPixelSize(R.dimen.grid_item_width)
        val layoutManager = AutoColumnGridLayoutManager(requireActivity(), width).apply {
            matchSpacing = true
            setMinEdgeSpacing(120) //This is a pixel value, normally you would retrieve the size similar to the width above
            setMinColumnSpacing(60) //This is a pixel value, normally you would retrieve the size similar to the width above
            spacingMethod = AutoColumnGridLayoutManager.SpacingMethod.ALL
        }

        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = GridAdapter(getItems())
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
            val item = items[position]
            holder.setText(item?.text ?: "")
        }
    }
}