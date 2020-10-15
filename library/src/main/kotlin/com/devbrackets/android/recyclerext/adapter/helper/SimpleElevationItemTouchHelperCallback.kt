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
package com.devbrackets.android.recyclerext.adapter.helper

import android.graphics.Canvas
import android.os.Build
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

/**
 * Extends the [androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback] to provide
 * support for specifying the elevation to use when an item is active (being dragged or swiped)
 */
abstract class SimpleElevationItemTouchHelperCallback @JvmOverloads
/**
 * Creates a Callback for the given drag and swipe allowance. These values serve as
 * defaults and if you want to customize behavior per ViewHolder, you can override
 * [.getSwipeDirs]
 * and / or [.getDragDirs].
 *
 * @param dragDirs  Binary OR of direction flags in which the Views can be dragged. Must be composed of
 * [ItemTouchHelper.LEFT], [ItemTouchHelper.RIGHT],
 * [ItemTouchHelper.START], [ItemTouchHelper.END],
 * [ItemTouchHelper.UP] and [ItemTouchHelper.DOWN]
 * @param swipeDirs Binary OR of direction flags in which the Views can be swiped. Must be composed of
 * [ItemTouchHelper.LEFT], [ItemTouchHelper.RIGHT],
 * [ItemTouchHelper.START], [ItemTouchHelper.END],
 * [ItemTouchHelper.UP] and [ItemTouchHelper.DOWN]
 * @param activeElevationChange The elevation change to use when an item becomes active
 */
constructor(
        dragDirs: Int,
        swipeDirs: Int,
        protected var activeElevationChange: Float = DEFAULT_ACTIVE_ELEVATION_CHANGE
) : ItemTouchHelper.SimpleCallback(dragDirs, swipeDirs) {
    companion object {
        const val DEFAULT_ACTIVE_ELEVATION_CHANGE = 1F //NOTE: the support library implementation uses 1F as the default
    }

    protected var isElevated = false
    protected var originalElevation = 0F

    override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
        //To avoid elevation conflicts with the Lollipop+ implementation, we will always inform the super that we aren't active
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, false)
        if (isCurrentlyActive && !isElevated) {
            updateElevation(recyclerView, viewHolder, true)
        }
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)
        updateElevation(recyclerView, viewHolder, false)
    }

    /**
     * Updates the elevation for the specified `holder` by either increasing
     * or decreasing by the specified amount
     *
     * @param recyclerView The recyclerView to use when calculating the new elevation
     * @param holder The ViewHolder to increase or decrease the elevation for
     * @param elevate True if the `holder` should have it's elevation increased
     */
    protected fun updateElevation(recyclerView: RecyclerView, holder: RecyclerView.ViewHolder, elevate: Boolean) {
        if (elevate) {
            originalElevation = ViewCompat.getElevation(holder.itemView)
            val newElevation = activeElevationChange + findMaxElevation(recyclerView)
            ViewCompat.setElevation(holder.itemView, newElevation)
            isElevated = true
        } else {
            ViewCompat.setElevation(holder.itemView, originalElevation)
            originalElevation = 0f
            isElevated = false
        }
    }

    /**
     * Finds the elevation of the highest visible viewHolder to make sure the elevated view
     * from [.updateElevation] is above
     * all others.
     *
     * @param recyclerView The RecyclerView to use when determining the height of all the visible ViewHolders
     */
    protected fun findMaxElevation(recyclerView: RecyclerView): Float {
        var maxChildElevation = 0f
        for (i in 0 until recyclerView.childCount) {
            val child = recyclerView.getChildAt(i)
            val elevation = ViewCompat.getElevation(child)
            if (elevation > maxChildElevation) {
                maxChildElevation = elevation
            }
        }
        return maxChildElevation
    }
}