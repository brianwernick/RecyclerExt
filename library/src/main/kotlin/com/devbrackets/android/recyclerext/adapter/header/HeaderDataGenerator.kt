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
package com.devbrackets.android.recyclerext.adapter.header

import androidx.collection.LongSparseArray
import androidx.collection.SparseArrayCompat
import androidx.recyclerview.widget.RecyclerView
import com.devbrackets.android.recyclerext.adapter.header.HeaderDataGenerator.DataSource
import com.devbrackets.android.recyclerext.adapter.header.HeaderDataGenerator.HeaderData

/**
 * Handles calculating the headers and offsets within an adapter
 * and generates the related [HeaderItem]s. This can be used
 * to asynchronously calculate the information necessary for the
 * Header adapters that can be used with the [androidx.recyclerview.widget.DiffUtil]
 */
class HeaderDataGenerator(protected var headerApi: HeaderApi<*, *>) {
    interface DataSource {
        /**
         * Return the stable ID for the header at `childPosition` or [RecyclerView.NO_ID]
         *
         * @param childPosition The position of the item ignoring headers
         * @return the stable ID of the header at childPosition
         */
        fun getHeaderId(childPosition: Int): Long

        /**
         * Returns the total number of children in the data set
         *
         * @return The total number of children
         */
        val childCount: Int
    }

    /**
     * Calculates the information necessary to display headers
     * using the associated [DataSource]
     *
     * @param dataSource The data to use when calculating the header information
     * @return The resulting [HeaderData]
     */
    fun calculate(dataSource: DataSource): HeaderData {
        return calculate(HeaderData(), dataSource)
    }

    /**
     * Calculates the information necessary to display headers
     * using the associated [DataSource]
     *
     * @param reuseData A [HeaderData] that will be reused instead of creating a new one
     * @param dataSource The data to use when calculating the header information
     * @return The `reuseData` that has been populated with the header information
     */
    fun calculate(reuseData: HeaderData, dataSource: DataSource): HeaderData {
        reuseData.headerItems.clear()
        reuseData.adapterPositionItemMap.clear()
        var currentHeader: HeaderItem? = null
        for (i in 0 until dataSource.childCount) {
            var adapterPosition = i + if (reuseData.showHeaderAsChild) 0 else reuseData.headerItems.size()
            val headerId = dataSource.getHeaderId(i)
            if (headerId == RecyclerView.NO_ID) {
                currentHeader = null
                reuseData.adapterPositionItemMap.put(adapterPosition, AdapterItem(null, i, headerApi.getChildViewType(i) and HeaderApi.Companion.HEADER_VIEW_TYPE_MASK.inv()))
                continue
            }

            //Adds new headers to the list when detected
            if (currentHeader == null || currentHeader.id != headerId) {
                currentHeader = HeaderItem(headerId, adapterPosition)
                reuseData.headerItems.put(headerId, currentHeader)
                reuseData.adapterPositionItemMap.put(adapterPosition, AdapterItem(currentHeader, i, headerApi.getHeaderViewType(i) or HeaderApi.Companion.HEADER_VIEW_TYPE_MASK))
                if (reuseData.showHeaderAsChild) {
                    currentHeader.childCount = 1
                    continue
                }
                adapterPosition++
            }

            currentHeader.childCount = currentHeader.childCount + 1
            reuseData.adapterPositionItemMap.put(adapterPosition, AdapterItem(null, i, headerApi.getChildViewType(i) and HeaderApi.Companion.HEADER_VIEW_TYPE_MASK.inv()))
        }

        return reuseData
    }

    data class HeaderData(
            var headerItems: LongSparseArray<HeaderItem> = LongSparseArray<HeaderItem>(),
            var adapterPositionItemMap: SparseArrayCompat<AdapterItem> = SparseArrayCompat<AdapterItem>(),
            var showHeaderAsChild: Boolean = false
    )

    class AdapterItem(var headerItem: HeaderItem?, var childPosition: Int, var itemViewType: Int)
}