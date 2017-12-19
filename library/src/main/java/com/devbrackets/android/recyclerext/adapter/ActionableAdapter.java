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

package com.devbrackets.android.recyclerext.adapter;

import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

/**
 * A {@link RecyclerView.Adapter} that handles the process of notifying
 * {@link android.support.v7.widget.RecyclerView.ViewHolder}s of action mode
 * changes (enter, exit) so they can perform the appropriate animations.
 */
public abstract class ActionableAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {
    private static final String TAG = "ActionableAdapter";

    interface ActionableView {
        void onActionModeChange(boolean actionModeEnabled);
    }

    @Nullable
    protected RecyclerView boundRecyclerView;
    protected boolean inActionMode;

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        boundRecyclerView = recyclerView;
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        boundRecyclerView = null;
    }

    public void setActionModeEnabled(boolean enabled) {
        if (enabled == inActionMode) {
            return;
        }

        inActionMode = enabled;
        updateVisibleViewHolders();
    }

    public boolean getActionModeEnabled() {
        return inActionMode;
    }

    public void updateVisibleViewHolders() {
        RecyclerView recyclerView = boundRecyclerView;
        if (recyclerView == null) {
            Log.d(TAG, "Ignoring updateVisibleViewHolders() when no RecyclerView is bound");
            return;
        }

        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager == null) {
            Log.d(TAG, "Ignoring updateVisibleViewHolders() when no LayoutManager is bound");
            return;
        }

        int startPosition = 0;
        if (layoutManager instanceof LinearLayoutManager) {
            startPosition = ((LinearLayoutManager)layoutManager).findFirstVisibleItemPosition();
        } else {
            //todo fallback to something different
            Log.e(TAG, "updateVisibleViewHolders() currently only supports LinearLayoutManager and it's subclasses");
        }

        int i = startPosition + 1;
        RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(startPosition);

        while (holder != null && i < getItemCount()) {
            holder = recyclerView.findViewHolderForAdapterPosition(i);
            if (holder != null && holder instanceof ActionableView) {
                ((ActionableView) holder).onActionModeChange(inActionMode);
            }
            i++;
        }
    }
}
