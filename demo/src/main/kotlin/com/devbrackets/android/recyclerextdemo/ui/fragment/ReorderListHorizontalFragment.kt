package com.devbrackets.android.recyclerextdemo.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager

/**
 * A Fragment for demonstrating a horizontal reorder adapter
 */
class ReorderListHorizontalFragment : ReorderListFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        orientation = LinearLayoutManager.HORIZONTAL
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    companion object {
        fun newInstance(): ReorderListHorizontalFragment {
            return ReorderListHorizontalFragment()
        }
    }
}