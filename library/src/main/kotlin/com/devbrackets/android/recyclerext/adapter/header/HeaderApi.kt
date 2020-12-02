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
package com.devbrackets.android.recyclerext.adapter.header

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.devbrackets.android.recyclerext.adapter.header.HeaderDataGenerator.HeaderData

/**
 * The standardized API for the Header Adapters
 */
interface HeaderApi<H : RecyclerView.ViewHolder?, C : RecyclerView.ViewHolder?> : HeaderDataGenerator.DataSource {
  /**
   * Called when the RecyclerView needs a new [H] ViewHolder
   *
   * @param parent The ViewGroup into which the new View will be added
   * @param viewType The type for the header view
   * @return The view type of the new View
   */
  fun onCreateHeaderViewHolder(parent: ViewGroup, viewType: Int): H

  /**
   * Called when the RecyclerView needs a new [C] ViewHolder
   *
   * @param parent The ViewGroup into which the new View will be added
   * @param viewType The type for the child view
   * @return The view type of the new View
   */
  fun onCreateChildViewHolder(parent: ViewGroup, viewType: Int): C

  /**
   * Sets the [HeaderDataGenerator.HeaderData] without informing any
   * listeners (data observers, etc.) of the change. Normally this should only
   * be used in conjunction with [.setAutoUpdateHeaders] to
   * handle asynchronously updating the headers in cases such as using the
   * [androidx.recyclerview.widget.DiffUtil]
   */
  var headerData: HeaderData

  /**
   * Specifies if the headers should automatically be calculated on any
   * adapter change (notified by the [HeaderAdapterDataObserver]).
   */
  var autoUpdateHeaders: Boolean

  /**
   * Retrieves the view type for the header whose first child view
   * has the `childPosition`.  This value will be |'d with
   * the [.HEADER_VIEW_TYPE_MASK] to make sure the header and child
   * view types don't overlap
   *
   * @param childPosition The position for the fist child underneath the header
   * @return The view type for the header view
   */
  fun getHeaderViewType(childPosition: Int): Int

  /**
   * Retrieves the view type for the child view at the specified
   * `childPosition`.  This value will be &amp;'ed with the
   * inverse of [.HEADER_VIEW_TYPE_MASK] to make sure the header
   * and child view types don't overlap.
   *
   * @param childPosition The position of the child to get the type for
   * @return The view type for the child view
   */
  fun getChildViewType(childPosition: Int): Int

  /**
   * Returns the total number of views that are associated with the specified
   * header id.  If the headerId doesn't exist then 0 will be returned.
   *
   * @param headerId The headerId to find the number of children for
   * @return The number of children views associated with the given `headerId`
   */
  fun getChildCount(headerId: Long): Int

  /**
   * Determines the child position given the adapter position in the RecyclerView.
   * This represents the position of the item before the headers were calculated.
   *
   * @param adapterPosition The adapter position
   * @return The position associated with the adapter data set
   */
  fun getChildPosition(adapterPosition: Int): Int

  /**
   * Determines the index of the child under it's header. This value is 0-indexed and will
   * be `null` if the `adapterPosition` represents an item that's not under a header, or if
   * the item is a header when [showHeaderAsChild] is disabled.
   *
   * @param adapterPosition The adapter position to determine the child index for
   * @return The index of the child under the header or `null` if the item isn't a child or
   * is a header.
   */
  fun getChildIndex(adapterPosition: Int): Int?

  /**
   * Determines the adapter position given the child position in
   * the RecyclerView
   *
   * @param childPosition The child position
   * @return The adapter position
   */
  fun getAdapterPositionForChild(childPosition: Int): Int

  /**
   * Determines the position for the header associated with
   * the `headerId`
   *
   * @param headerId The id to find the header for
   * @return The associated headers position or [RecyclerView.NO_POSITION]
   */
  fun getHeaderPosition(headerId: Long): Int

  /**
   * When enabled the headers will not be counted separately
   * from the children. This should be used when the headers have
   * a slightly different display type from the other children
   * instead of the abruptly different view.  This is useful when
   * mimicking the sticky alphabetical headers seen in the contacts
   * app for Lollipop and Marshmallow
   *
   * @param enabled True if the header should be treated as a child
   */
  fun showHeaderAsChild(enabled: Boolean)

  /**
   * Retrieves the resource id for the view in the header
   * view holder to make sticky.  By default this returns
   * the invalid resource id (0) and will use the entire
   * header view.  Only use this if only a specific view
   * should remain sticky.
   *
   *
   * **NOTE:** This will only be used when a
   * [com.devbrackets.android.recyclerext.decoration.StickyHeaderDecoration]
   * has been specified
   *
   * @return The resource id for the view that will be sticky
   */
  val customStickyHeaderViewId: Int

  companion object {
    const val HEADER_VIEW_TYPE_MASK = -0x80000000
  }
}