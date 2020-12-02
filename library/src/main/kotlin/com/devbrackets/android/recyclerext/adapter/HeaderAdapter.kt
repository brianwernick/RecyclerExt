/*
 * Copyright (C) 2016 - 2020 Brian Wernick
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
package com.devbrackets.android.recyclerext.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.devbrackets.android.recyclerext.adapter.header.HeaderApi
import com.devbrackets.android.recyclerext.adapter.header.HeaderCore
import com.devbrackets.android.recyclerext.adapter.header.HeaderDataGenerator.HeaderData

/**
 * A RecyclerView adapter that adds support for dynamically placing headers in the view.
 *
 * @param <H> The Header [RecyclerView.ViewHolder]
 * @param <C> The Child or content [RecyclerView.ViewHolder]
 */
abstract class HeaderAdapter<H : ViewHolder, C : ViewHolder> : ActionableAdapter<ViewHolder>(), HeaderApi<H, C> {
  /**
   * Contains the base processing for the header adapters
   */
  protected lateinit var core: HeaderCore

  /**
   * Initializes the non-super components for the Adapter
   */
  protected fun init() {
    core = HeaderCore(this)
  }

  init {
    init()
  }

  /**
   * Called to display the header information with the `firstChildPosition` being the
   * position of the first child after this header.
   *
   * @param holder The ViewHolder which should be updated
   * @param firstChildPosition The position of the child immediately after this header
   */
  abstract fun onBindHeaderViewHolder(holder: H, firstChildPosition: Int)

  /**
   * Called to display the child information with the `childPosition` being the
   * position of the child, excluding headers.
   *
   * @param holder The ViewHolder which should be updated
   * @param childPosition The position of the child
   */
  abstract fun onBindChildViewHolder(holder: C, childPosition: Int)

  /**
   * This method shouldn't be used directly, instead use
   * [onCreateHeaderViewHolder] and
   * [onCreateChildViewHolder]
   *
   * @param parent The parent ViewGroup for the ViewHolder
   * @param viewType The type for the ViewHolder
   * @return The correct ViewHolder for the specified viewType
   */
  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    return core.onCreateViewHolder(parent, viewType)
  }

  /**
   * This method shouldn't be used directly, instead use
   * [onBindHeaderViewHolder] and
   * [onBindChildViewHolder]
   *
   * @param holder The ViewHolder to update
   * @param position The position to update the `holder` with
   */
  @Suppress("UNCHECKED_CAST")
  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    val viewType = getItemViewType(position)
    val childPosition = getChildPosition(position)

    if (viewType and HeaderApi.HEADER_VIEW_TYPE_MASK != 0) {
      onBindHeaderViewHolder(holder as H, childPosition)
      return
    }

    onBindChildViewHolder(holder as C, childPosition)
  }

  override var headerData: HeaderData
    get() = core.headerData
    set(headerData) {
      core.headerData = headerData
    }

  override var autoUpdateHeaders: Boolean
    get() = core.autoUpdateHeaders
    set(autoUpdateHeaders) {
      core.setAutoUpdateHeaders(this, autoUpdateHeaders)
    }

  /**
   * Retrieves the view type for the specified adapterPosition.
   *
   * @param adapterPosition The position to determine the view type for
   * @return The type of ViewHolder for the `adapterPosition`
   */
  override fun getItemViewType(adapterPosition: Int): Int {
    return core.getItemViewType(adapterPosition)
  }

  /**
   * When the RecyclerView is attached a data observer is registered
   * in order to determine when to re-calculate the headers
   *
   * @param recyclerView The RecyclerView that was attached
   */
  override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
    super.onAttachedToRecyclerView(recyclerView)
    core.registerObserver(this)
  }

  /**
   * When the RecyclerView is detached the registered data observer
   * will be unregistered.  See [.onAttachedToRecyclerView]
   * for more information
   *
   * @param recyclerView The RecyclerView that has been detached
   */
  override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
    super.onDetachedFromRecyclerView(recyclerView)
    core.unregisterObserver(this)
  }

  /**
   * Returns the total number of items in the data set hold by the adapter, this includes
   * both the Headers and the Children views
   *
   *
   * **NOTE:** [.getChildCount] should be overridden instead of this method
   *
   * @return The total number of items in this adapter.
   */
  override fun getItemCount(): Int {
    return core.itemCount
  }

  override fun getHeaderViewType(childPosition: Int): Int {
    return HeaderApi.HEADER_VIEW_TYPE_MASK
  }

  override fun getChildViewType(childPosition: Int): Int {
    return 0
  }

  override fun getChildCount(headerId: Long): Int {
    return core.getChildCount(headerId)
  }

  override fun getHeaderId(childPosition: Int): Long {
    return RecyclerView.NO_ID
  }

  override fun getChildPosition(adapterPosition: Int): Int {
    return core.getChildPosition(adapterPosition)
  }

  override fun getChildIndex(adapterPosition: Int): Int? {
    return core.getChildIndex(adapterPosition)
  }

  override fun getAdapterPositionForChild(childPosition: Int): Int {
    return core.getAdapterPositionForChild(childPosition)
  }

  override fun getHeaderPosition(headerId: Long): Int {
    return core.getHeaderPosition(headerId)
  }

  override fun showHeaderAsChild(enabled: Boolean) {
    core.showHeaderAsChild(enabled)
  }

  override val customStickyHeaderViewId: Int
    get() = 0
}