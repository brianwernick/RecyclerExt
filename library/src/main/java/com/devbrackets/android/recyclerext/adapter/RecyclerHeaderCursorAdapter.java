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

import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.devbrackets.android.recyclerext.R;

import java.util.ArrayList;
import java.util.List;

import static android.support.v7.widget.RecyclerView.ViewHolder;

/**
 * A RecyclerView adapter that adds support for dynamically placing headers in the view
 * using a cursor.
 *
 * @param <H> The Header {@link ViewHolder}
 * @param <C> The Child or content {@link ViewHolder}
 */
public abstract class RecyclerHeaderCursorAdapter<H extends ViewHolder, C extends ViewHolder> extends RecyclerCursorAdapter<ViewHolder> {
    public static final int VIEW_TYPE_CHILD = 1;
    public static final int VIEW_TYPE_HEADER = 10;

    private Observer observer = new Observer();
    protected List<HeaderItem> headerItems = new ArrayList<>();

    /**
     * @param cursor The cursor from which to get the data.
     */
    public RecyclerHeaderCursorAdapter(Cursor cursor) {
        super(cursor);
    }

    /**
     * @param cursor The cursor from which to get the data.
     * @param idColumnName The name for the id column to use when calling {@link #getItemId(int)} [default: {@value #DEFAULT_ID_COLUMN_NAME}]
     */
    public RecyclerHeaderCursorAdapter(Cursor cursor, String idColumnName) {
        super(cursor, idColumnName);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_CHILD) {
            return onCreateChildViewHolder(parent);
        } else if (viewType == VIEW_TYPE_HEADER) {
            return onCreateHeaderViewHolder(parent);
        }

        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onBindViewHolder(ViewHolder holder, Cursor cursor, int position) {
        int viewType = getItemViewType(position);
        int childPosition = determineChildPosition(position);

        Cursor c = getCursor(childPosition);
        if (viewType == VIEW_TYPE_CHILD) {
            onBindChildViewHolder((C)holder, c, childPosition);
        } else if (viewType == VIEW_TYPE_HEADER) {
            onBindHeaderViewHolder((H) holder, c, childPosition);
            holder.itemView.setTag(R.id.sticky_view_header_id, getHeaderId(childPosition));
        }

        holder.itemView.setTag(R.id.sticky_view_type_tag, viewType);
    }

    @Override
    public int getItemViewType(int position) {
        for (HeaderItem item: headerItems) {
            if (item.getViewPosition() == position) {
                return VIEW_TYPE_HEADER;
            } else if (item.getViewPosition() > position) {
                break;
            }
        }

        return VIEW_TYPE_CHILD;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        registerAdapterDataObserver(observer);
        observer.onChanged();
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        unregisterAdapterDataObserver(observer);
        headerItems.clear();
    }

    /**
     * Returns the total number of items in the data set hold by the adapter.
     * <br />
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
    public abstract void onBindHeaderViewHolder(H holder, Cursor cursor, int firstChildPosition);

    /**
     * Called to display the child information with the <code>childPosition</code> being the
     * position of the child, excluding headers.
     *
     * @param holder The ViewHolder which should be updated
     * @param childPosition The position of the child
     */
    public abstract void onBindChildViewHolder(C holder, Cursor cursor, int childPosition);

    /**
     * Returns the total number of children in the data set held by the adapter.
     *
     * @return The total number of children in this adapter.
     */
    public int getChildCount() {
        if (isValidData && cursor != null) {
            return cursor.getCount();
        }

        return 0;
    }

    /**
     * Determines the child position given the position in the RecyclerView
     *
     * @param viewPosition The position in the RecyclerView (includes Headers and Children)
     * @return The child index
     */
    private int determineChildPosition(int viewPosition) {
        int headerCount = 0;
        for (HeaderItem item: headerItems) {
            if (item.getViewPosition() < viewPosition) {
                headerCount++;
            } else {
                break;
            }
        }

        return viewPosition - headerCount;
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
