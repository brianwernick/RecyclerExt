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

package com.devbrackets.android.recyclerext.adapter.header;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

/**
 * The standardized API for the Header Adapters
 */
public interface HeaderApi<H extends RecyclerView.ViewHolder, C extends RecyclerView.ViewHolder> {
    int HEADER_VIEW_TYPE_MASK = 0x80000000;

    /**
     * Called when the RecyclerView needs a new {@link H} ViewHolder
     *
     * @param parent The ViewGroup into which the new View will be added
     * @param viewType The type for the header view
     * @return The view type of the new View
     */
    H onCreateHeaderViewHolder(ViewGroup parent, int viewType);

    /**
     * Called when the RecyclerView needs a new {@link C} ViewHolder
     *
     * @param parent The ViewGroup into which the new View will be added
     * @param viewType The type for the child view
     * @return The view type of the new View
     */
    C onCreateChildViewHolder(ViewGroup parent, int viewType);

    /**
     * Retrieves the view type for the header whos first child view
     * has the <code>childPosition</code>.  This value will be |'d with
     * the {@link #HEADER_VIEW_TYPE_MASK} to make sure the header and child
     * view types don't overlap
     *
     * @param childPosition The position for the fist child underneath the header
     * @return The view type for the header view
     */
    int getHeaderViewType(int childPosition);

    /**
     * Retrieves the view type for the child view at the specified
     * <code>childPosition</code>.  This value will be &amp;'ed with the
     * inverse of {@link #HEADER_VIEW_TYPE_MASK} to make sure the header
     * and child view types don't overlap.
     *
     * @param childPosition The position of the child to get the type for
     * @return The view type for the child view
     */
    int getChildViewType(int childPosition);

    /**
     * Return the stable ID for the header at <code>childPosition</code>. The default implementation
     * of this method returns {@link RecyclerView#NO_ID}
     *
     * @param childPosition The Adapters child position
     * @return the stable ID of the header at childPosition
     */
    long getHeaderId(int childPosition);

    /**
     * Returns the total number of children in the data set held by the adapter.
     *
     * @return The total number of children in this adapter.
     */
    int getChildCount();

    /**
     * Returns the total number of views that are associated with the specified
     * header id.  If the headerId doesn't exist then 0 will be returned.
     *
     * @param headerId The headerId to find the number of children for
     * @return The number of children views associated with the given <code>headerId</code>
     */
    int getChildCount(long headerId);

    /**
     * Determines the child position given the position in the RecyclerView
     *
     * @param viewPosition The position in the RecyclerView (includes Headers and Children)
     * @return The child index
     */
    int determineChildPosition(int viewPosition);

    /**
     * Determines the position for the header associated with
     * the <code>headerId</code>
     *
     * @param headerId The id to find the header for
     * @return The associated headers position or {@link RecyclerView#NO_POSITION}
     */
    int getHeaderPosition(long headerId);
}
