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

import java.util.ArrayList;
import java.util.List;

import static android.support.v7.widget.RecyclerView.ViewHolder;

/**
 * A RecyclerView adapter that adds support for dynamically placing headers in the view.
 *
 * @param <H> The Header {@link RecyclerView.ViewHolder}
 * @param <C> The Child or content {@link RecyclerView.ViewHolder}
 */
public abstract class RecyclerHeaderAdapter<H extends ViewHolder, C extends ViewHolder> extends RecyclerView.Adapter<ViewHolder> {
    public static final int VIEW_TYPE_CHILD = 1;
    public static final int VIEW_TYPE_HEADER = 10;

    private Observer observer = new Observer();
    protected List<HeaderItem> headerItems = new ArrayList<>();

    /**
     * Called when the RecyclerView needs a new {@link H} ViewHolder
     *
     * @param parent The ViewGroup into which the new View will be added
     * @return The view type of the new View
     */
    public abstract H onCreateHeaderViewHolder(ViewGroup parent);

    /**
     * Called when the RecyclerView needs a new {@link C} ViewHolder
     *
     * @param parent The ViewGroup into which the new View will be added
     * @return The view type of the new View
     */
    public abstract C onCreateChildViewHolder(ViewGroup parent);

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

    /**
     * Returns the total number of children in the data set held by the adapter.
     *
     * @return The total number of children in this adapter.
     */
    public abstract int getChildCount();

    /**
     * This method shouldn't be used directly, instead use
     * {@link #onCreateHeaderViewHolder(ViewGroup)} and
     * {@link #onCreateChildViewHolder(ViewGroup)}
     *
     * @param parent The parent ViewGroup for the ViewHolder
     * @param viewType The type for the ViewHolder
     * @return The correct ViewHolder for the specified viewType
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_CHILD) {
            return onCreateChildViewHolder(parent);
        } else if (viewType == VIEW_TYPE_HEADER) {
            return onCreateHeaderViewHolder(parent);
        }

        return null;
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
    @SuppressWarnings("unchecked")
    public void onBindViewHolder(ViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        int childPosition = determineChildPosition(position);

        if (viewType == VIEW_TYPE_CHILD) {
            onBindChildViewHolder((C) holder, childPosition);
            holder.itemView.setTag(R.id.recyclerext_view_child_position, childPosition);
        } else if (viewType == VIEW_TYPE_HEADER) {
            onBindHeaderViewHolder((H) holder, childPosition);
            holder.itemView.setTag(R.id.recyclerext_view_child_position, childPosition);
        }
    }

    /**
     * Retrieves the view type for the specified position.
     *
     * @param position The position to determine the view type for
     * @return The type of ViewHolder for the <code>position</code>
     */
    @Override
    public int getItemViewType(int position) {
        for (HeaderItem item : headerItems) {
            if (item.getViewPosition() == position) {
                return VIEW_TYPE_HEADER;
            } else if (item.getViewPosition() > position) {
                break;
            }
        }

        return VIEW_TYPE_CHILD;
    }

    /**
     * When the RecyclerView is attached a data observer is registered
     * in order to determine when to re-calculate the headers
     *
     * @param recyclerView The RecyclerView that was attached
     */
    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        registerAdapterDataObserver(observer);
        observer.onChanged();
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
        unregisterAdapterDataObserver(observer);
        headerItems.clear();
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
        return getChildCount() + headerItems.size();
    }

    /**
     * Return the stable ID for the header at <code>childPosition</code>. The default implementation
     * of this method returns {@link RecyclerView#NO_ID}
     *
     * @param childPosition The Adapters child position
     * @return the stable ID of the header at childPosition
     */
    public long getHeaderId(int childPosition) {
        return RecyclerView.NO_ID;
    }

    /**
     * Determines the child position given the position in the RecyclerView
     *
     * @param viewPosition The position in the RecyclerView (includes Headers and Children)
     * @return The child index
     */
    public int determineChildPosition(int viewPosition) {
        int headerCount = 0;
        for (HeaderItem item : headerItems) {
            if (item.getViewPosition() < viewPosition) {
                headerCount++;
            } else {
                break;
            }
        }

        return viewPosition - headerCount;
    }

    /**
     * Determines the position for the header associated with
     * the <code>headerId</code>
     *
     * @param headerId The id to find the header for
     * @return The associated headers position or {@link RecyclerView#NO_POSITION}
     */
    public int getHeaderPosition(long headerId) {
        if (headerId == RecyclerView.NO_ID) {
            return RecyclerView.NO_POSITION;
        }

        for (HeaderItem item : headerItems) {
            if (item.getHeaderId() == headerId) {
                return item.getViewPosition();
            }
        }

        return RecyclerView.NO_POSITION;
    }

    /**
     * Used to monitor data set changes to update the {@link #headerItems} so that we can correctly
     * calculate the list item count and header indexes.
     */
    private class Observer extends RecyclerView.AdapterDataObserver {
        @Override
        public void onChanged() {
            calculateHeaderIndices();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            calculateHeaderIndices();
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            calculateHeaderIndices();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            calculateHeaderIndices();
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            calculateHeaderIndices();
        }

        /**
         * Performs a full calculation for the header indices
         */
        private void calculateHeaderIndices() {
            headerItems.clear();
            HeaderItem currentItem = null;

            for (int i = 0; i < getChildCount(); i++) {
                long id = getHeaderId(i);
                if (id != RecyclerView.NO_ID && (currentItem == null || currentItem.getHeaderId() != id)) {
                    currentItem = new HeaderItem(id, i + headerItems.size());
                    headerItems.add(currentItem);
                }
            }
        }
    }

    /**
     * An object to keep track of the locations and id's for header items
     */
    private static class HeaderItem {
        private long headerId;
        private int viewPosition;

        public HeaderItem(long headerId, int viewPosition) {
            this.headerId = headerId;
            this.viewPosition = viewPosition;
        }

        public long getHeaderId() {
            return headerId;
        }

        public int getViewPosition() {
            return viewPosition;
        }
    }
}
