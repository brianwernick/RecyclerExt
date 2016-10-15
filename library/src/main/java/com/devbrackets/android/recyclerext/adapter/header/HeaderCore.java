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

package com.devbrackets.android.recyclerext.adapter.header;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class HeaderCore {
    @NonNull
    protected HeaderApi headerApi;
    @NonNull
    protected Observer observer = new Observer();
    protected boolean showHeaderAsChild = false;

    /**
     * Stores the number of child items associated with each header id.
     * (Key: HeaderId, Value: childCount)
     */
    @NonNull
    protected Map<Long, Integer> headerChildCountMap = new HashMap<>();
    @NonNull
    protected List<HeaderItem> headerItems = new ArrayList<>();

    public HeaderCore(@NonNull HeaderApi api) {
        this.headerApi = api;
    }

    /**
     * Returns the total number of views that are associated with the specified
     * header id.  If the headerId doesn't exist then 0 will be returned.
     *
     * @param headerId The headerId to find the number of children for
     * @return The number of children views associated with the given <code>headerId</code>
     */
    public int getChildCount(long headerId) {
        if (headerId == RecyclerView.NO_ID || !headerChildCountMap.containsKey(headerId)) {
            return 0;
        }

        return headerChildCountMap.get(headerId);
    }

    /**
     * Returns the total number of headers in the list
     *
     * @return The number of headers for the list
     */
    public int getHeaderCount() {
        return headerItems.size();
    }

    /**
     * Returns a list containing the adapter positions for the
     * headers
     *
     * @return A list of the adapter positions for headers
     */
    @NonNull
    public List<Integer> getHeaderPositions() {
        List<Integer> positions = new ArrayList<>();

        for (HeaderItem item : headerItems) {
            positions.add(item.getViewPosition());
        }

        return positions;
    }

    /**
     * Returns the total number of items in the adapter including
     * headers and children.
     *
     * @return The number of items to display in the adapter
     */
    public int getItemCount() {
        if (showHeaderAsChild) {
            return headerApi.getChildCount();
        }

        return headerApi.getChildCount() + getHeaderCount();
    }

    /**
     * Determines if the item at the <code>position</code> is a Header
     * view.
     *
     * @param position The raw position in the RecyclerView
     * @return True if the item at <code>position</code> is a Header
     */
    public boolean isHeader(int position) {
        for (HeaderItem item : headerItems) {
            if (item.getViewPosition() == position) {
                return true;
            }

            //The header items are ordered, so don't go past the position
            if (item.getViewPosition() > position) {
                break;
            }
        }

        return false;
    }

    /**
     * @param parent The parent ViewGroup for the ViewHolder
     * @param viewType The type for the ViewHolder
     * @return The correct ViewHolder for the specified viewType
     */
    @NonNull
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if ((viewType & HeaderApi.HEADER_VIEW_TYPE_MASK) != 0) {
            return headerApi.onCreateHeaderViewHolder(parent, viewType);
        }

        return headerApi.onCreateChildViewHolder(parent, viewType);
    }

    /**
     * Retrieves the view type for the specified position.
     *
     * @param position The position to determine the view type for
     * @return The type of ViewHolder for the <code>position</code>
     */
    public int getItemViewType(int position) {
        int childPosition = getChildPosition(position);

        if (isHeader(position)) {
            return headerApi.getHeaderViewType(childPosition) | HeaderApi.HEADER_VIEW_TYPE_MASK;
        }

        return headerApi.getChildViewType(childPosition) & ~HeaderApi.HEADER_VIEW_TYPE_MASK;
    }

    /**
     * When the RecyclerView is attached a data observer is registered
     * in order to determine when to re-calculate the headers
     *
     * @param adapter The RecyclerView.Adapter that the observer will be registered for
     */
    public void registerObserver(@NonNull RecyclerView.Adapter adapter) {
        adapter.registerAdapterDataObserver(observer);
        observer.onChanged();
    }

    /**
     * When the RecyclerView is detached the registered data observer
     * will be unregistered.  See {@link #registerObserver(RecyclerView.Adapter)}
     * for more information
     *
     * @param adapter The RecyclerView.Adapter that the observer needs to be unregistered for
     */
    public void unregisterObserver(@NonNull RecyclerView.Adapter adapter) {
        adapter.unregisterAdapterDataObserver(observer);
        headerItems.clear();
    }

    /**
     * Determines the child position given the position in the RecyclerView
     *
     * @param adapterPosition The position in the RecyclerView (includes Headers and Children)
     * @return The child index
     */
    public int getChildPosition(int adapterPosition) {
        if (showHeaderAsChild) {
            return adapterPosition;
        }

        int headerCount = 0;
        for (HeaderItem item : headerItems) {
            if (item.getViewPosition() < adapterPosition) {
                headerCount++;
            } else {
                break;
            }
        }

        return adapterPosition - headerCount;
    }

    /**
     * Determines the adapter position given the child position in
     * the RecyclerView
     *
     * @param childPosition The child position
     * @return The adapter position
     */
    public int getAdapterPositionForChild(int childPosition) {
        if (showHeaderAsChild) {
            return childPosition;
        }

        for (HeaderItem item : headerItems) {
            if (item.getViewPosition() <= childPosition) {
                childPosition++;
            } else {
                break;
            }
        }

        return childPosition;
    }

    /**
     * Determines the adapter position for the header associated with
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

    public void showHeaderAsChild(boolean enabled) {
        showHeaderAsChild = enabled;
        observer.onChanged();
    }

    /**
     * Used to monitor data set changes to update the {@link #headerItems} so that we can correctly
     * calculate the list item count and header indexes.
     */
    protected class Observer extends RecyclerView.AdapterDataObserver {
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
            headerChildCountMap.clear();
            HeaderItem currentItem = null;

            for (int i = 0; i < headerApi.getChildCount(); i++) {
                long id = headerApi.getHeaderId(i);
                if (id == RecyclerView.NO_ID) {
                    continue;
                }

                //Updates the child count for the headerId
                Integer childCount = headerChildCountMap.get(id);
                childCount = (childCount == null) ? 1 : childCount +1;
                headerChildCountMap.put(id, childCount);

                //Adds new headers to the list when detected
                if (currentItem == null || currentItem.getHeaderId() != id) {
                    int position = i + (showHeaderAsChild ? 0 : headerItems.size());
                    currentItem = new HeaderItem(id, position);
                    headerItems.add(currentItem);
                }
            }
        }
    }
}
