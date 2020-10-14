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
package com.devbrackets.android.recyclerext.adapter.header

import android.view.ViewGroup
import androidx.annotation.IntRange
import androidx.recyclerview.widget.RecyclerView
import com.devbrackets.android.recyclerext.adapter.header.HeaderDataGenerator.HeaderData
import java.util.*

class HeaderCore(protected var headerApi: HeaderApi<*, *>) {
    var headerData = HeaderData()
    protected var observer: HeaderAdapterDataObserver
    protected var registeredObserver = false
    var autoUpdateHeaders = true
        protected set

    fun showHeaderAsChild(enabled: Boolean) {
        headerData.showHeaderAsChild = enabled
        if (autoUpdateHeaders) {
            observer.onChanged()
        }
    }

    fun setAutoUpdateHeaders(adapter: RecyclerView.Adapter<*>, autoUpdateHeaders: Boolean) {
        if (autoUpdateHeaders == this.autoUpdateHeaders) {
            return
        }
        this.autoUpdateHeaders = autoUpdateHeaders
        if (autoUpdateHeaders) {
            registerObserver(adapter)
        } else {
            unregisterObserver(adapter)
        }
    }

    /**
     * Returns the total number of views that are associated with the specified
     * header id.  If the headerId doesn't exist then 0 will be returned.
     *
     * @param headerId The headerId to find the number of children for
     * @return The number of children views associated with the given `headerId`
     */
    fun getChildCount(headerId: Long): Int {
        if (headerId == RecyclerView.NO_ID) {
            return 0
        }
        val headerItem = headerData.headerItems[headerId]
        return headerItem?.childCount ?: 0
    }

    /**
     * Returns the total number of headers in the list
     *
     * @return The number of headers for the list
     */
    val headerCount: Int
        get() = headerData.headerItems.size()

    /**
     * Returns a list containing the adapter positions for the
     * headers
     *
     * @return A list of the adapter positions for headers
     */
    val headerPositions: List<Int>
        get() {
            val positions: MutableList<Int> = ArrayList()
            for (i in 0 until headerData.headerItems.size()) {
                positions.add(headerData.headerItems[headerData.headerItems.keyAt(i)].getAdapterPosition())
            }
            return positions
        }

    /**
     * Returns the total number of items in the adapter including
     * headers and children.
     *
     * @return The number of items to display in the adapter
     */
    val itemCount: Int
        get() = headerData.adapterPositionItemMap.size()

    /**
     * Determines if the item at the `adapterPosition` is a Header
     * view.
     *
     * @param adapterPosition The raw adapterPosition in the RecyclerView
     * @return True if the item at `adapterPosition` is a Header
     */
    fun isHeader(adapterPosition: Int): Boolean {
        val item = headerData.adapterPositionItemMap[adapterPosition]
        return item != null && item.headerItem != null
    }

    /**
     * @param parent The parent ViewGroup for the ViewHolder
     * @param viewType The type for the ViewHolder
     * @return The correct ViewHolder for the specified viewType
     */
    fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType and HeaderApi.Companion.HEADER_VIEW_TYPE_MASK != 0) {
            headerApi.onCreateHeaderViewHolder(parent, viewType)!!
        } else headerApi.onCreateChildViewHolder(parent, viewType)!!
    }

    /**
     * Retrieves the view type for the specified position.
     *
     * @param adapterPosition The position to determine the view type for
     * @return The type of ViewHolder for the `position`
     */
    fun getItemViewType(adapterPosition: Int): Int {
        return headerData.adapterPositionItemMap[adapterPosition]!!.itemViewType
    }

    /**
     * When the RecyclerView is attached a data observer is registered
     * in order to determine when to re-calculate the headers
     *
     * @param adapter The RecyclerView.Adapter that the observer will be registered for
     */
    fun registerObserver(adapter: RecyclerView.Adapter<*>) {
        if (autoUpdateHeaders) {
            adapter.registerAdapterDataObserver(observer)
            observer.onChanged()
            registeredObserver = true
        }
    }

    /**
     * When the RecyclerView is detached the registered data observer
     * will be unregistered.  See [.registerObserver]
     * for more information
     *
     * @param adapter The RecyclerView.Adapter that the observer needs to be unregistered for
     */
    fun unregisterObserver(adapter: RecyclerView.Adapter<*>) {
        if (registeredObserver) {
            adapter.unregisterAdapterDataObserver(observer)
            registeredObserver = false
        }
        if (autoUpdateHeaders) {
            headerData.headerItems.clear()
        }
    }

    /**
     * Determines the child position given the position in the RecyclerView
     *
     * @param adapterPosition The position in the RecyclerView (includes Headers and Children)
     * @return The child index
     */
    fun getChildPosition(adapterPosition: Int): Int {
        val item = headerData.adapterPositionItemMap[adapterPosition]
        return item?.childPosition ?: adapterPosition
    }

    /**
     * Determines the adapter position given the child position in
     * the RecyclerView
     *
     * @param childPosition The child position
     * @return The adapter position
     */
    fun getAdapterPositionForChild(childPosition: Int): Int {
        if (headerData.showHeaderAsChild) {
            return childPosition
        }
        for (i in 0 until headerData.adapterPositionItemMap.size()) {
            val adapterPosition = headerData.adapterPositionItemMap.keyAt(i)
            if (headerData.adapterPositionItemMap[adapterPosition]!!.childPosition == childPosition) {
                return adapterPosition
            }
        }
        return childPosition
    }

    /**
     * Determines the adapter position for the header associated with
     * the `headerId`
     *
     * @param headerId The id to find the header for
     * @return The associated headers position or [RecyclerView.NO_POSITION]
     */
    fun getHeaderPosition(headerId: Long): Int {
        if (headerId == RecyclerView.NO_ID) {
            return RecyclerView.NO_POSITION
        }
        val headerItem = headerData.headerItems[headerId]
        return headerItem?.adapterPosition ?: RecyclerView.NO_POSITION
    }

    fun getHeaderForAdapterPosition(@IntRange(from = 0) adapterPosition: Int): HeaderItem? {
        val item = headerData.adapterPositionItemMap[adapterPosition]
        return item?.headerItem
    }

    init {
        observer = HeaderAdapterDataObserver(this, headerApi)
    }
}