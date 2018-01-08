/*
 * Copyright (C) 2016 - 2018 Brian Wernick
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

/**
 * Used to monitor data set changes to update the {@link HeaderCore#headerData} so that we can correctly
 * calculate the list item count and header indexes.
 */
public class HeaderAdapterDataObserver extends RecyclerView.AdapterDataObserver {
    @NonNull
    protected HeaderCore headerCore;
    @NonNull
    protected HeaderApi headerApi;

    @NonNull
    protected HeaderDataGenerator generator;

    public HeaderAdapterDataObserver(@NonNull HeaderCore headerCore, @NonNull HeaderApi headerApi) {
        this.headerCore = headerCore;
        this.headerApi = headerApi;
        this.generator = new HeaderDataGenerator(headerApi);
    }

    @Override
    public void onChanged() {
        generator.calculate(headerApi.getHeaderData(), headerApi);
    }

    @Override
    public void onItemRangeChanged(int positionStart, int itemCount) {
        // Only recalculates the headers if there was a change
        for (int i = positionStart; i < positionStart + itemCount; i++) {
            HeaderItem headerItem = headerCore.getHeaderForAdapterPosition(i);
            long newHeaderId = headerApi.getHeaderId(headerApi.getChildPosition(i));

            if (headerItem != null && headerItem.getId() != newHeaderId) {
                generator.calculate(headerApi.getHeaderData(), headerApi);
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

        generator.calculate(headerApi.getHeaderData(), headerApi);
    }

    @Override
    public void onItemRangeRemoved(int positionStart, int itemCount) {
        // Unlike the onItemRangeChanged deletions cause a few things that need to be handled
        //     1. HeaderItem indexes below the deletion point need to be updated
        //     2. If removed from an existing header, the item count needs to be updated or the header removed
        // This is made more complex because deletions can occur at the start, middle, or end
        // of an existing region (header children). Due to this we will just recalculate the
        // headers on deletions for now

        generator.calculate(headerApi.getHeaderData(), headerApi);
    }

    @Override
    public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
        generator.calculate(headerApi.getHeaderData(), headerApi);
    }
}
