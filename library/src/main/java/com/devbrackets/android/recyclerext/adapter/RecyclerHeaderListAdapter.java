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

package com.devbrackets.android.recyclerext.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.devbrackets.android.recyclerext.R;
import com.devbrackets.android.recyclerext.adapter.header.HeaderApi;
import com.devbrackets.android.recyclerext.adapter.header.HeaderCore;

import static android.support.v7.widget.RecyclerView.ViewHolder;

/**
 * A RecyclerView adapter that adds support for dynamically placing headers in the view.
 *
 * @param <H> The Header {@link ViewHolder}
 * @param <C> The Child or content {@link ViewHolder}
 */
public abstract class RecyclerHeaderListAdapter<H extends ViewHolder, C extends ViewHolder, T> extends RecyclerListAdapter<ViewHolder, T> implements HeaderApi<H, C> {

    /**
     * Contains the base processing for the header adapters
     */
    @NonNull
    @SuppressWarnings("NullableProblems")
    protected HeaderCore core;

    /**
     * Called to display the header information with the <code>firstChildPosition</code> being the
     * position of the first child after this header.
     *
     * @param holder The ViewHolder which should be updated
     * @param firstChildPosition The position of the child immediately after this header
     */
    public abstract void onBindHeaderViewHolder(@NonNull H holder, int firstChildPosition);

    /**
     * Called to display the child information with the <code>childPosition</code> being the
     * position of the child, excluding headers.
     *
     * @param holder The ViewHolder which should be updated
     * @param childPosition The position of the child
     */
    public abstract void onBindChildViewHolder(@NonNull C holder, int childPosition);

    public RecyclerHeaderListAdapter() {
        init();
    }

    /**
     * This method shouldn't be used directly, instead use
     * {@link #onCreateHeaderViewHolder(ViewGroup, int)} and
     * {@link #onCreateChildViewHolder(ViewGroup, int)}
     *
     * @param parent The parent ViewGroup for the ViewHolder
     * @param viewType The type for the ViewHolder
     * @return The correct ViewHolder for the specified viewType
     */
    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return core.onCreateViewHolder(parent, viewType);
    }

    /**
     * This method shouldn't be used directly, instead use
     * {@link #onBindHeaderViewHolder(ViewHolder, int)} and
     * {@link #onBindChildViewHolder(ViewHolder, int)}
     *
     * @param holder The ViewHolder to update
     * @param position The position to update the <code>holder</code> with
     */
    @Override
    @SuppressWarnings("unchecked") //Unchecked cast
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        int childPosition = getChildPosition(position);

        if ((viewType & HEADER_VIEW_TYPE_MASK) != 0) {
            onBindHeaderViewHolder((H) holder, childPosition);
            holder.itemView.setTag(R.id.recyclerext_view_child_position, childPosition);
            return;
        }

        onBindChildViewHolder((C) holder, childPosition);
        holder.itemView.setTag(R.id.recyclerext_view_child_position, childPosition);
    }

    /**
     * Retrieves the view type for the specified position.
     *
     * @param adapterPosition The position to determine the view type for
     * @return The type of ViewHolder for the <code>adapterPosition</code>
     */
    @Override
    public int getItemViewType(int adapterPosition) {
        return core.getItemViewType(adapterPosition);
    }

    /**
     * When the RecyclerView is attached a data observer is registered
     * in order to determine when to re-calculate the headers
     *
     * @param recyclerView The RecyclerView that was attached
     */
    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        core.registerObserver(this);
    }

    /**
     * When the RecyclerView is detached the registered data observer
     * will be unregistered.  See {@link #onAttachedToRecyclerView(RecyclerView)}
     * for more information
     *
     * @param recyclerView The RecyclerView that has been detached
     */
    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        core.unregisterObserver(this);
    }

    /**
     * Returns the total number of items in the data set hold by the adapter, this includes
     * both the Headers and the Children views
     * <p>
     * <b>NOTE:</b> {@link #getChildCount()} should be overridden instead of this method
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return core.getItemCount();
    }

    @Override
    public int getHeaderViewType(int childPosition) {
        return HEADER_VIEW_TYPE_MASK;
    }

    @Override
    public int getChildViewType(int childPosition) {
        return 0;
    }

    @Override
    public int getChildCount(long headerId) {
        return core.getChildCount(headerId);
    }

    @Override
    public int getChildCount() {
        return super.getItemCount();
    }

    @Override
    public long getHeaderId(int childPosition) {
        return RecyclerView.NO_ID;
    }

    @Override
    public int getChildPosition(int adapterPosition) {
        return core.getChildPosition(adapterPosition);
    }

    @Override
    public int getAdapterPositionForChild(int childPosition) {
        return core.getAdapterPositionForChild(childPosition);
    }

    @Override
    public int getHeaderPosition(long headerId) {
        return core.getHeaderPosition(headerId);
    }

    @Override
    public void showHeaderAsChild(boolean enabled) {
        core.showHeaderAsChild(enabled);
    }

    @Override
    public int getCustomStickyHeaderViewId() {
        return 0;
    }

    /**
     * Initializes the non-super components for the Adapter
     */
    protected void init() {
        core = new HeaderCore(this);
    }
}