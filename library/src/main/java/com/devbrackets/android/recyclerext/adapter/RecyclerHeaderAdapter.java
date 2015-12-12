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

package com.devbrackets.android.recyclerext.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.devbrackets.android.recyclerext.R;
import com.devbrackets.android.recyclerext.adapter.header.HeaderApi;
import com.devbrackets.android.recyclerext.adapter.header.HeaderCore;

import static android.support.v7.widget.RecyclerView.ViewHolder;

/**
 * A RecyclerView adapter that adds support for dynamically placing headers in the view.
 *
 * @param <H> The Header {@link RecyclerView.ViewHolder}
 * @param <C> The Child or content {@link RecyclerView.ViewHolder}
 */
public abstract class RecyclerHeaderAdapter<H extends ViewHolder, C extends ViewHolder> extends RecyclerView.Adapter<ViewHolder> implements HeaderApi<H, C> {

    /**
     * Contains the base processing for the header adapters
     */
    protected HeaderCore core;

    /**
     * Called to display the header information with the <code>firstChildPosition</code> being the
     * position of the first child after this header.
     *
     * @param holder The ViewHolder which should be updated
     * @param firstChildPosition The position of the child immediately after this header
     */
    public abstract void onBindHeaderViewHolder(H holder, int firstChildPosition);

    /**
     * Called to display the child information with the <code>childPosition</code> being the
     * position of the child, excluding headers.
     *
     * @param holder The ViewHolder which should be updated
     * @param childPosition The position of the child
     */
    public abstract void onBindChildViewHolder(C holder, int childPosition);

    public RecyclerHeaderAdapter() {
        core = new HeaderCore(this);
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
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
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
    public void onBindViewHolder(ViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        int childPosition = determineChildPosition(position);

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
     * @param position The position to determine the view type for
     * @return The type of ViewHolder for the <code>position</code>
     */
    @Override
    public int getItemViewType(int position) {
        return core.getItemViewType(position);
    }

    /**
     * When the RecyclerView is attached a data observer is registered
     * in order to determine when to re-calculate the headers
     *
     * @param recyclerView The RecyclerView that was attached
     */
    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
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
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
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
        return getChildCount() + core.getHeaderCount();
    }

    @Override
    public int getHeaderViewType(int childPosition) {
        return 0;
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
    public long getHeaderId(int childPosition) {
        return RecyclerView.NO_ID;
    }

    @Override
    public int determineChildPosition(int viewPosition) {
        return core.determineChildPosition(viewPosition);
    }

    @Override
    public int getHeaderPosition(long headerId) {
        return core.getHeaderPosition(headerId);
    }
}