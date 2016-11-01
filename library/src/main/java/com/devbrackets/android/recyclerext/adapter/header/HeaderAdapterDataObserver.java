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
import android.support.v4.util.LongSparseArray;
import android.support.v7.widget.RecyclerView;

import java.util.List;

/**
 * Used to monitor data set changes to update the {@link HeaderCore#headerItems} so that we can correctly
 * calculate the list item count and header indexes.
 */
public class HeaderAdapterDataObserver extends RecyclerView.AdapterDataObserver {
    @NonNull
    protected HeaderCore headerCore;    
    @NonNull
    protected HeaderApi headerApi;

    public HeaderAdapterDataObserver(@NonNull HeaderCore headerCore, @NonNull HeaderApi headerApi) {
        this.headerCore = headerCore;
        this.headerApi = headerApi;
    }

    @Override
    public void onChanged() {
        calculateHeaderIndices();
    }

    @Override
    public void onItemRangeChanged(int positionStart, int itemCount) {
        // Only recalculates the headers if there was a change
        for (int i = positionStart; i < positionStart + itemCount; i++) {
            HeaderItem headerItem = headerCore.getHeaderForAdapterPosition(i);
            long newHeaderId = headerApi.getHeaderId(headerApi.getChildPosition(i));

            if (headerItem != null && headerItem.getId() != newHeaderId) {
                calculateHeaderIndices();
                break;
            }
        }
    }

    @Override
    public void onItemRangeInserted(int positionStart, int itemCount) {
        // Unlike the onItemRangeChanged insertions cause a few things that need to be handled
        //     1. HeaderItem indexes below the insertion point need to be updated
        //     2. If added to an existing header, the item count needs to be updated
        //     3. If creating a new header, the HeaderItem(s) need to be created and inserted
        // This is made more complex because insertions can occur at the start, middle, or end
        // of an existing region (header children). Due to this we will just recalculate the
        // headers on insertions for now

        calculateHeaderIndices();
    }

    @Override
    public void onItemRangeRemoved(int positionStart, int itemCount) {
        // Unlike the onItemRangeChanged deletions cause a few things that need to be handled
        //     1. HeaderItem indexes below the deletion point need to be updated
        //     2. If removed from an existing header, the item count needs to be updated or the header removed
        // This is made more complex because deletions can occur at the start, middle, or end
        // of an existing region (header children). Due to this we will just recalculate the
        // headers on deletions for now

        calculateHeaderIndices();
    }

    @Override
    public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
        calculateHeaderIndices();
    }

    /**
     * Performs a full calculation for the header indices
     */
    protected void calculateHeaderIndices() {
        List<HeaderItem> items = headerCore.getHeaderItems();
        LongSparseArray<Integer> counts = headerCore.getHeaderChildCountMap();
        
        items.clear();
        counts.clear();
        HeaderItem currentItem = null;

        for (int i = 0; i < headerApi.getChildCount(); i++) {
            long id = headerApi.getHeaderId(i);
            if (id == RecyclerView.NO_ID) {
                continue;
            }

            //Updates the child count for the headerId
            Integer childCount = counts.get(id);
            childCount = (childCount == null) ? 1 : childCount +1;
            counts.put(id, childCount);

            //Adds new headers to the list when detected
            if (currentItem == null || currentItem.getId() != id) {
                int position = i + (headerCore.showHeaderAsChild ? 0 : items.size());
                currentItem = new HeaderItem(id, position);
                items.add(currentItem);
            }
        }
    }
}
