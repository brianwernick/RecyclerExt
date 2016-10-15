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
    @NonNull
    H onCreateHeaderViewHolder(@NonNull ViewGroup parent, int viewType);

    /**
     * Called when the RecyclerView needs a new {@link C} ViewHolder
     *
     * @param parent The ViewGroup into which the new View will be added
     * @param viewType The type for the child view
     * @return The view type of the new View
     */
    @NonNull
    C onCreateChildViewHolder(@NonNull ViewGroup parent, int viewType);

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
     * Determines the child position given the adapter position in the RecyclerView
     *
     * @param adapterPosition The adapter position
     * @return The child index
     */
    int getChildPosition(int adapterPosition);

    /**
     * Determines the adapter position given the child position in
     * the RecyclerView
     *
     * @param childPosition The child position
     * @return The adapter position
     */
    int getAdapterPositionForChild(int childPosition);

    /**
     * Determines the position for the header associated with
     * the <code>headerId</code>
     *
     * @param headerId The id to find the header for
     * @return The associated headers position or {@link RecyclerView#NO_POSITION}
     */
    int getHeaderPosition(long headerId);

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
    void showHeaderAsChild(boolean enabled);

    /**
     * Retrieves the resource id for the view in the header
     * view holder to make sticky.  By default this returns
     * the invalid resource id (0) and will use the entire
     * header view.  Only use this if only a specific view
     * should remain sticky.
     * <p>
     * <b>NOTE:</b> This will only be used when a
     * {@link com.devbrackets.android.recyclerext.decoration.StickyHeaderDecoration}
     * has been specified
     *
     * @return The resource id for the view that will be sticky
     */
    int getCustomStickyHeaderViewId();
}
