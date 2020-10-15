/*
 * Copyright (C) 2016 Brian Wernick
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.devbrackets.android.recyclerext.decoration

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.devbrackets.android.recyclerext.annotation.EdgeSpacing
import java.lang.ref.WeakReference

/**
 * A simple ItemDecoration for adding space between items in lists
 * or grids
 */
class SpacerDecoration @JvmOverloads constructor(horizontalSpace: Int = 0, verticalSpace: Int = 0) : ItemDecoration() {
    companion object {
        const val EDGE_SPACING_TOP = 1
        const val EDGE_SPACING_RIGHT = 1 shl 1
        const val EDGE_SPACING_BOTTOM = 1 shl 2
        const val EDGE_SPACING_LEFT = 1 shl 3
    }

    protected var horizontalSpace = 0
    protected var verticalSpace = 0
    protected var spanLookup: SpanLookup? = null

    /**
     * Edges are:
     *  - The left side of the left-most items
     *  - The right side of the right-most items
     *  - The top side of the top-most items
     *  - The bottom side of the bottom-most items
     */
    @EdgeSpacing
    var allowedEdgeSpacing = 0



    init {
        update(horizontalSpace, verticalSpace)
    }

    fun update(horizontalSpace: Int, verticalSpace: Int) {
        this.horizontalSpace = horizontalSpace
        this.verticalSpace = verticalSpace
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = (view.layoutParams as RecyclerView.LayoutParams).viewAdapterPosition
        val childCount = parent.adapter!!.itemCount
        if (spanLookup == null) {
            spanLookup = SpanLookup(parent)
        }

        outRect.left = if (allowedEdgeSpacing and EDGE_SPACING_LEFT == 0 && isLeftEdge(spanLookup!!, position)) 0 else horizontalSpace
        outRect.right = if (allowedEdgeSpacing and EDGE_SPACING_RIGHT == 0 && isRightEdge(spanLookup!!, position)) 0 else horizontalSpace
        outRect.top = if (allowedEdgeSpacing and EDGE_SPACING_TOP == 0 && isTopEdge(spanLookup!!, position, childCount)) 0 else verticalSpace
        outRect.bottom = if (allowedEdgeSpacing and EDGE_SPACING_BOTTOM == 0 && isBottomEdge(spanLookup!!, position, childCount)) 0 else verticalSpace
    }

    /**
     * Determines if the item at `position` is a top-most view
     *
     * @param spanLookup The SpanLookup related to the parent RecyclerView
     * @param position The position to determine if the view is a top-most view
     * @param childCount The number of children in the RecyclerView (adapter)
     * @return True if the view at `position` is a top-most view
     */
    protected fun isTopEdge(spanLookup: SpanLookup, position: Int, childCount: Int): Boolean {
        var latestCheckedPosition = 0
        while (latestCheckedPosition < childCount) {
            val spanEndIndex = spanLookup.getIndex(latestCheckedPosition) + spanLookup.getSpanSize(latestCheckedPosition) - 1
            if (spanEndIndex == spanLookup.spanCount - 1) {
                break
            }
            latestCheckedPosition++
        }

        return position <= latestCheckedPosition
    }

    /**
     * Determines if the item at `position` is a right-most view
     *
     * @param spanLookup The SpanLookup related to the parent RecyclerView
     * @param position The position to determine if the view is a right-most view
     * @return True if the view at `position` is a right-most view
     */
    protected fun isRightEdge(spanLookup: SpanLookup, position: Int): Boolean {
        val spanIndex = spanLookup.getIndex(position)
        return spanIndex + spanLookup.getSpanSize(position) == spanLookup.spanCount
    }

    /**
     * Determines if the item at `position` is a bottom-most view
     *
     * @param spanLookup The SpanLookup related to the parent RecyclerView
     * @param position The position to determine if the view is a bottom-most view
     * @param childCount The number of children in the RecyclerView (adapter)
     * @return True if the view at `position` is a bottom-most view
     */
    protected fun isBottomEdge(spanLookup: SpanLookup, position: Int, childCount: Int): Boolean {
        var latestCheckedPosition = childCount - 1
        while (latestCheckedPosition >= 0) {
            val spanIndex = spanLookup.getIndex(latestCheckedPosition)
            if (spanIndex == 0) {
                break
            }
            latestCheckedPosition--
        }

        return position >= latestCheckedPosition
    }

    /**
     * Determines if the item at `position` is a left-most view
     *
     * @param spanLookup The SpanLookup related to the parent RecyclerView
     * @param position The position to determine if the view is a left-most view
     * @return True if the view at `position` is a left-most view
     */
    protected fun isLeftEdge(spanLookup: SpanLookup, position: Int): Boolean {
        val spanIndex = spanLookup.getIndex(position)
        return spanIndex == 0
    }

    /**
     * A helper class to abstract the lookup from different LayoutManagers
     */
    protected inner class SpanLookup(recyclerView: RecyclerView) {
        var gridLayoutManager = WeakReference<GridLayoutManager?>(null)
        val spanCount: Int
            get() {
                val layoutManager = gridLayoutManager.get()
                return layoutManager?.spanCount ?: 1
            }

        fun getIndex(position: Int): Int {
            val layoutManager = gridLayoutManager.get()
            return layoutManager?.spanSizeLookup?.getSpanIndex(position, spanCount) ?: 0
        }

        fun getSpanSize(position: Int): Int {
            val layoutManager = gridLayoutManager.get()
            return layoutManager?.spanSizeLookup?.getSpanSize(position) ?: 1
        }

        init {
            val layoutManager = recyclerView.layoutManager
            if (layoutManager is GridLayoutManager) {
                gridLayoutManager = WeakReference(layoutManager as GridLayoutManager?)
            }
        }
    }
}