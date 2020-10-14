/*
 * Copyright (C) 2017 Brian Wernick
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
package com.devbrackets.android.recyclerext.adapter

import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * A [RecyclerView.Adapter] that handles the process of notifying
 * [RecyclerView.ViewHolder]s of action mode
 * changes (enter, exit) so they can perform the appropriate animations.
 */
abstract class ActionableAdapter<VH : RecyclerView.ViewHolder?> : RecyclerView.Adapter<VH>() {
    interface ActionableView {
        fun onActionModeChange(actionModeEnabled: Boolean)
    }

    protected var boundRecyclerView: RecyclerView? = null
    protected var inActionMode = false
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        boundRecyclerView = recyclerView
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        boundRecyclerView = null
    }

    var actionModeEnabled: Boolean
        get() = inActionMode
        set(enabled) {
            if (enabled == inActionMode) {
                return
            }
            inActionMode = enabled
            updateVisibleViewHolders()
        }

    fun updateVisibleViewHolders() {
        val recyclerView = boundRecyclerView
        if (recyclerView == null) {
            Log.d(TAG, "Ignoring updateVisibleViewHolders() when no RecyclerView is bound")
            return
        }
        val layoutManager = recyclerView.layoutManager
        if (layoutManager == null) {
            Log.d(TAG, "Ignoring updateVisibleViewHolders() when no LayoutManager is bound")
            return
        }
        var startPosition = 0
        startPosition = if (layoutManager is LinearLayoutManager) {
            layoutManager.findFirstVisibleItemPosition()
        } else {
            Log.e(TAG, "updateVisibleViewHolders() currently only supports LinearLayoutManager and it's subclasses")
            return
        }
        var i = startPosition
        var holder = recyclerView.findViewHolderForAdapterPosition(startPosition)
        while (holder != null && i < itemCount) {
            holder = recyclerView.findViewHolderForAdapterPosition(i)
            if (holder != null && holder is ActionableView) {
                (holder as ActionableView).onActionModeChange(inActionMode)
            }
            i++
        }
    }

    companion object {
        private const val TAG = "ActionableAdapter"
    }
}