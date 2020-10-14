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
package com.devbrackets.android.recyclerext.adapter.viewholder

import android.view.HapticFeedbackConstants
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * A Simple ViewHolder that adds the ability to specify
 * listeners that have access to the ViewHolder instead
 * of a position when clicked
 */
abstract class ClickableViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener, View.OnLongClickListener {
    interface OnClickListener {
        fun onClick(viewHolder: ClickableViewHolder)
    }

    interface OnLongClickListener {
        fun onLongClick(viewHolder: ClickableViewHolder): Boolean
    }

    private var onClickListener: OnClickListener? = null
    private var onLongClickListener: OnLongClickListener? = null
    protected var performLongClickHapticFeedback = true

    /**
     * Sets the click and long click listeners on the root
     * view (see [.itemView])
     */
    protected fun initializeClickListener() {
        itemView.setOnClickListener(this)
        itemView.setOnLongClickListener(this)
    }

    /**
     * Enables or Disables haptic feedback on long clicks.  If a
     * long click listener has not been set with [.setOnLongClickListener]
     * then no haptic feedback will be performed.
     *
     * @param enabled True if the long click should perform a haptic feedback [default: true]
     */
    fun setHapticFeedbackEnabled(enabled: Boolean) {
        performLongClickHapticFeedback = enabled
    }

    fun setOnClickListener(onClickListener: OnClickListener?) {
        this.onClickListener = onClickListener
    }

    fun setOnLongClickListener(onLongClickListener: OnLongClickListener?) {
        this.onLongClickListener = onLongClickListener
    }

    override fun onClick(view: View) {
        if (onClickListener != null) {
            onClickListener!!.onClick(this)
        }
    }

    override fun onLongClick(view: View): Boolean {
        if (onLongClickListener != null) {
            if (performLongClickHapticFeedback) {
                itemView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            }
            return onLongClickListener!!.onLongClick(this)
        }
        return false
    }

    init {
        initializeClickListener()
    }
}