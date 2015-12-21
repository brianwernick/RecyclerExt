/*
 * Copyright (C) 2015 Brian Wernick
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

package com.devbrackets.android.recyclerext.adapter.viewholder;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.HapticFeedbackConstants;
import android.view.View;

/**
 * A Simple ViewHolder that adds the ability to specify
 * listeners that have access to the ViewHolder instead
 * of a position when clicked
 */
public abstract class ClickableViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

    public interface OnClickListener {
        void onClick(ClickableViewHolder viewHolder);
    }

    public interface OnLongClickListener {
        boolean onLongClick(ClickableViewHolder viewHolder);
    }

    @Nullable
    private OnClickListener onClickListener;
    @Nullable
    private OnLongClickListener onLongClickListener;

    protected boolean performLongClickHapticFeedback = true;

    public ClickableViewHolder(View itemView) {
        super(itemView);
        initializeClickListener();
    }

    /**
     * Sets the click and long click listeners on the root
     * view (see {@link #itemView})
     */
    protected void initializeClickListener() {
        itemView.setOnClickListener(this);
        itemView.setOnLongClickListener(this);
    }

    /**
     * Enables or Disables haptic feedback on long clicks.  If a
     * long click listener has not been set with {@link #setOnLongClickListener(OnLongClickListener)}
     * then no haptic feedback will be performed.
     *
     * @param enabled True if the long click should perform a haptic feedback [default: true]
     */
    public void setHapticFeedbackEnabled(boolean enabled) {
        performLongClickHapticFeedback = enabled;
    }

    public void setOnClickListener(@Nullable OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public void setOnLongClickListener(@Nullable OnLongClickListener onLongClickListener) {
        this.onLongClickListener = onLongClickListener;
    }

    @Override
    public void onClick(View view) {
        if (onClickListener != null) {
            onClickListener.onClick(this);
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (onLongClickListener != null) {
            if (performLongClickHapticFeedback) {
                itemView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            }

            return onLongClickListener.onLongClick(this);
        }

        return false;
    }
}