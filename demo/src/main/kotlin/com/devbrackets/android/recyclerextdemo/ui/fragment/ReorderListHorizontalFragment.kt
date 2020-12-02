package com.devbrackets.android.recyclerextdemo.ui.fragment

import androidx.recyclerview.widget.LinearLayoutManager

/**
 * A Fragment for demonstrating a horizontal reorder adapter
 */
class ReorderListHorizontalFragment : ReorderListFragment() {
    override fun onSetupRecyclerView() {
        orientation = LinearLayoutManager.HORIZONTAL
        super.onSetupRecyclerView()
    }
}