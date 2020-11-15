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

      // Adds an item without a header
      if (headerId == RecyclerView.NO_ID) {
        currentHeader = null
        val item = AdapterItem(null, i, null, headerApi.getChildViewType(i) and HeaderApi.HEADER_VIEW_TYPE_MASK.inv())
        reuseData.adapterPositionItemMap.put(adapterPosition, item)
        continue
      }

      //Adds new headers to the list when detected
      if (currentHeader == null || currentHeader.id != headerId) {
        currentHeader = HeaderItem(headerId, adapterPosition)
        reuseData.headerItems.put(headerId, currentHeader)

        // Adds the header
        val childIndex = if (reuseData.showHeaderAsChild) 0 else null
        val item = AdapterItem(currentHeader, i, childIndex, headerApi.getHeaderViewType(i) or HeaderApi.HEADER_VIEW_TYPE_MASK)
        reuseData.adapterPositionItemMap.put(adapterPosition, item)

        // If the header is also a child then we are done with this index
        if (reuseData.showHeaderAsChild) {
          currentHeader.childCount = 1
          continue
        }

        // Make sure that the child item below will have the correct adapterPosition
        adapterPosition++
      }

      // Adds the child item
      val item = AdapterItem(null, i, currentHeader.childCount, headerApi.getChildViewType(i) and HeaderApi.HEADER_VIEW_TYPE_MASK.inv())
      reuseData.adapterPositionItemMap.put(adapterPosition, item)
      currentHeader.childCount = currentHeader.childCount + 1
    }

    return reuseData
  }

  /**
   * Class that holds the state data associated with mapping adapter positions to items, children,
   * and headers.
   */
  data class HeaderData(
      var headerItems: LongSparseArray<HeaderItem> = LongSparseArray<HeaderItem>(),
      var adapterPositionItemMap: SparseArrayCompat<AdapterItem> = SparseArrayCompat<AdapterItem>(),
      var showHeaderAsChild: Boolean = false
  )

  /**
   * The metadata associated with items, children, and headers in the adapter.
   */
  class AdapterItem(
      /**
       * The item that represents a header.
       * This will be `null` when the [AdapterItem] represents a regular item or a child
       */
      val headerItem: HeaderItem?,

      /**
       * The position in the adapters data set, this is generally the adapter position
       * subtracting the number of headers above.
       */
      val childPosition: Int,

      /**
       * The index of the child under the defined header. For the first item under a header
       * this value will be `0`; for headers and items (non-children) this will be `null`. If
       * [HeaderData.showHeaderAsChild] is `true` then because the header and the first item
       * are the same, this value will be `0`
       */
      val childIndex: Int?,

      /**
       * The ViewType for this item
       */
      val itemViewType: Int
  )
}