/*
 * Copyright (C) 2017 - 2018 Brian Wernick
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
import android.support.annotation.Nullable;
import android.support.v4.util.SparseArrayCompat;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles calculating the headers and offsets within an adapter
 * and generates the related {@link HeaderItem}s. This can be used
 * to asynchronously calculate the information necessary for the
 * Header adapters that can be used with the {@link android.support.v7.util.DiffUtil}
 */
public class HeaderDataGenerator {
    public interface DataSource {
        /**
         * Return the stable ID for the header at <code>childPosition</code> or {@link RecyclerView#NO_ID}
         *
         * @param childPosition The position of the item ignoring headers
         * @return the stable ID of the header at childPosition
         */
        long getHeaderId(int childPosition);

        /**
         * Returns the total number of children in the data set
         *
         * @return The total number of children
         */
        int getChildCount();
    }

    @NonNull
    protected HeaderApi headerApi;

    public HeaderDataGenerator(@NonNull HeaderApi headerApi) {
        this.headerApi = headerApi;
    }

    /**
     * Calculates the information necessary to display headers
     * using the associated {@link DataSource}
     *
     * @param dataSource The data to use when calculating the header information
     * @return The resulting {@link HeaderData}
     */
    @NonNull
    public HeaderData calculate(@NonNull DataSource dataSource) {
        return calculate(new HeaderData(), dataSource);
    }

    /**
     * Calculates the information necessary to display headers
     * using the associated {@link DataSource}
     *
     * @param reuseData A {@link HeaderData} that will be reused instead of creating a new one
     * @param dataSource The data to use when calculating the header information
     * @return The <code>reuseData</code> that has been populated with the header information
     */
    @NonNull
    public HeaderData calculate(@NonNull HeaderData reuseData, @NonNull DataSource dataSource) {
        reuseData.headerItems.clear();
        reuseData.adapterPositionItemMap.clear();

        HeaderItem currentHeader = null;

        for (int i = 0; i < dataSource.getChildCount(); i++) {
            int adapterPosition = i + (reuseData.showHeaderAsChild ? 0 : reuseData.headerItems.size());

            long headerId = dataSource.getHeaderId(i);
            if (headerId == RecyclerView.NO_ID) {
                currentHeader = null;
                reuseData.adapterPositionItemMap.put(adapterPosition, new AdapterItem(null, i, headerApi.getChildViewType(i) & ~HeaderApi.HEADER_VIEW_TYPE_MASK));
                continue;
            }

            //Adds new headers to the list when detected
            if (currentHeader == null || currentHeader.getId() != headerId) {
                currentHeader = new HeaderItem(headerId, adapterPosition);
                reuseData.headerItems.add(currentHeader);

                reuseData.adapterPositionItemMap.put(adapterPosition, new AdapterItem(currentHeader, i, headerApi.getHeaderViewType(i) | HeaderApi.HEADER_VIEW_TYPE_MASK));
                if (reuseData.showHeaderAsChild) {
                    currentHeader.setChildCount(1);
                    continue;
                }

                adapterPosition++;
            }

            currentHeader.setChildCount(currentHeader.getChildCount() + 1);
            reuseData.adapterPositionItemMap.put(adapterPosition, new AdapterItem(null, i, headerApi.getChildViewType(i) & ~HeaderApi.HEADER_VIEW_TYPE_MASK));
        }

        return reuseData;
    }

    public static class HeaderData {
        @NonNull
        public List<HeaderItem> headerItems = new ArrayList<>();
        @NonNull
        public SparseArrayCompat<AdapterItem> adapterPositionItemMap = new SparseArrayCompat<>();

        public boolean showHeaderAsChild = false;
    }

    public static class AdapterItem {
        @Nullable
        public HeaderItem headerItem;
        public int childPosition;
        public int itemViewType;

        public AdapterItem(@Nullable HeaderItem headerItem, int childPosition, int itemViewType) {
            this.headerItem = headerItem;
            this.childPosition = childPosition;
            this.itemViewType = itemViewType;
        }
    }
}
