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

package com.devbrackets.android.recyclerext.decoration;

import android.graphics.Canvas;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.devbrackets.android.recyclerext.R;
import com.devbrackets.android.recyclerext.adapter.RecyclerHeaderAdapter;
import com.devbrackets.android.recyclerext.adapter.header.HeaderApi;

/**
 * A RecyclerView Decoration that allows for Header views from
 * the {@link RecyclerHeaderAdapter} to be persisted when they
 * reach the start of the RecyclerView's frame.
 */
@SuppressWarnings("unused")
public class StickyHeaderDecoration extends RecyclerView.ItemDecoration {
    private static final String TAG = "StickyHeaderDecoration";

    public enum LayoutOrientation {
        VERTICAL,
        HORIZONTAL
    }

    @Nullable
    private RecyclerView.ViewHolder stickyViewHolder;

    private RecyclerView parent;
    private RecyclerView.Adapter adapter;
    private AdapterDataObserver dataObserver;
    private StickyViewScrollListener scrollListener;

    private long currentStickyId = Long.MIN_VALUE;
    private LayoutOrientation orientation = LayoutOrientation.VERTICAL;

    /**
     * Creates the ItemDecoration that performs the functionality to
     * have the current header view sticky (persisted at the start of the
     * RecyclerView).
     * <p>
     * <b>NOTE:</b> This will tightly couple to the <code>parent</code> and the
     * adapter.  If you intend to swap out adapters you will need to first call
     * {@link #dispose()} then replace this decoration with a new one.
     *
     * @param parent The RecyclerView to couple the ItemDecoration to
     */
    public StickyHeaderDecoration(@NonNull RecyclerView parent) {
        if (parent.getAdapter() == null || !(parent.getAdapter() instanceof HeaderApi)) {
            throw new ExceptionInInitializerError("The Adapter must be set before this is created and extend RecyclerHeaderAdapter, RecyclerHeaderListAdapter or implement HeaderApi");
        }

        this.parent = parent;
        adapter = parent.getAdapter();
        dataObserver = new AdapterDataObserver();
        scrollListener = new StickyViewScrollListener();

        adapter.registerAdapterDataObserver(dataObserver);
        parent.addOnScrollListener(scrollListener);
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        RecyclerView.ViewHolder holder = stickyViewHolder;
        if (holder == null) {
            return;
        }

        int stickyViewId = ((HeaderApi) adapter).getCustomStickyHeaderViewId();
        View stickyView = stickyViewId != 0 ? holder.itemView.findViewById(stickyViewId) : holder.itemView;
        if (stickyView != null) {
            stickyView.draw(c);
        }
    }

    /**
     * Decouples from the RecyclerView and the RecyclerView.Adapter,
     * clearing out any header associations.
     */
    public void dispose() {
        clearStickyHeader();

        adapter.unregisterAdapterDataObserver(dataObserver);
        parent.removeOnScrollListener(scrollListener);

        adapter = null;
        dataObserver = null;
        parent = null;
    }

    /**
     * Clears the current sticky header from the view.
     */
    public void clearStickyHeader() {
        stickyViewHolder = null;
        currentStickyId = RecyclerView.NO_ID;
    }

    /**
     * Sets the orientation of the current layout
     *
     * @param orientation The layouts orientation
     */
    public void setOrientation(LayoutOrientation orientation) {
        this.orientation = orientation;
    }

    /**
     * Retrieves the current orientation to use for edgeScrolling and position calculations.
     *
     * @return The current orientation [default: {@link LayoutOrientation#VERTICAL}]
     */
    public LayoutOrientation getOrientation() {
        return orientation;
    }

    /**
     * An observer to watch the adapter for changes so that we can update the
     * current sticky header
     */
    private class AdapterDataObserver extends RecyclerView.AdapterDataObserver {
        @Override
        public void onChanged() {
            //For now we just clear the stick header
            clearStickyHeader();
        }
    }

    /**
     * Listens to the scroll events for the RecyclerView that will have
     * sticky headers.  When a new header reaches the start it will be
     * transformed in to a sticky view and attached to the start of the
     * RecyclerView.  Additionally, when a new header is reaching the
     * start, the headers will be transitioned smoothly
     */
    private class StickyViewScrollListener extends RecyclerView.OnScrollListener {
        private int[] windowLocation = new int[2];
        private int parentStart = Integer.MIN_VALUE;

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            View firstView = findStartView(recyclerView);
            if (firstView == null) {
                clearStickyHeader();
                return;
            }

            //Retrieve the child position for the view
            Integer childPosition = (Integer) firstView.getTag(R.id.recyclerext_view_child_position);
            if (childPosition == null) {
                return;
            }

            //If the next header is different than the current one, perform the swap
            long headerId = getHeaderId(adapter, childPosition);
            if (headerId != currentStickyId) {
                performHeaderSwap(headerId);
            }
        }

        /**
         * Replaces the <code>stickyHeader</code> with the header associated with
         * the <code>headerId</code>.
         *
         * @param headerId The id for the header view
         */
        private void performHeaderSwap(long headerId) {
            //If we don't have a valid headerId then clear the current header
            if (headerId == RecyclerView.NO_ID) {
                clearStickyHeader();
                return;
            }

            //Get the position of the associated header
            int headerPosition = getHeaderPosition(adapter, headerId);
            if (headerPosition == RecyclerView.NO_POSITION) {
                return;
            }

            updateHeader(headerId, headerPosition);
        }

        /**
         * Updates the current sticky header to the one with the
         * <code>headerId</code>.
         *
         * @param headerId The id for the new sticky header
         * @param headerPosition The position in the RecyclerView for the header
         */
        private void updateHeader(long headerId, int headerPosition) {
            currentStickyId = headerId;
            stickyViewHolder = getHeaderViewHolder(headerPosition);
        }

        /**
         * Finds the view that is at the start of the list.  This will either be the
         * Left or Top most visible positions.
         *
         * @return The view at the start of the <code>recyclerView</code>
         */
        @Nullable
        private View findStartView(RecyclerView recyclerView) {
            int attachedViewCount = recyclerView.getLayoutManager().getChildCount();

            //Make sure we have the start of the RecyclerView stored
            if (parentStart == Integer.MIN_VALUE) {
                recyclerView.getLocationInWindow(windowLocation);
                parentStart = orientation == LayoutOrientation.HORIZONTAL ? windowLocation[0] : windowLocation[1];
            }

            //Finds the first visible view
            for (int viewIndex = 0; viewIndex < attachedViewCount; viewIndex++) {
                View view = recyclerView.getLayoutManager().getChildAt(viewIndex);
                view.getLocationInWindow(windowLocation);

                int startLoc = orientation == LayoutOrientation.HORIZONTAL ? windowLocation[0] : windowLocation[1];
                if (startLoc <= parentStart) {
                    return view;
                }
            }

            //Under normal circumstances we should never reach this return
            return null;
        }

        /**
         * Retrieves the id for the header associated with the <code>childPosition</code> from
         * the specified <code>headerAdapter</code>
         *
         * @param headerAdapter The adapter to use when finding the header id
         * @param childPosition The child position associated with the header
         * @return The id for the header or {@link RecyclerView#NO_ID}
         */
        private long getHeaderId(RecyclerView.Adapter headerAdapter, int childPosition) {
            if (headerAdapter instanceof HeaderApi) {
                return ((HeaderApi) adapter).getHeaderId(childPosition);
            }

            return RecyclerView.NO_ID;
        }

        /**
         * Determines the position for the header associated with
         * the <code>headerId</code>
         *
         * @param headerAdapter The adapter to use when finding the header position
         * @param headerId The id to find the header for
         * @return The associated headers position or {@link RecyclerView#NO_POSITION}
         */
        private int getHeaderPosition(RecyclerView.Adapter headerAdapter, long headerId) {
            if (headerAdapter instanceof HeaderApi) {
                return ((HeaderApi) adapter).getHeaderPosition(headerId);
            }

            return RecyclerView.NO_POSITION;
        }

        private int getHeaderViewType(int headerPosition) {
            int rawType = ((HeaderApi) adapter).getHeaderViewType(headerPosition);

            //Make sure that this is treated as a header type
            return rawType | HeaderApi.HEADER_VIEW_TYPE_MASK;
        }

        private int getCustomStickyViewId() {
            return ((HeaderApi) adapter).getCustomStickyHeaderViewId();
        }

        /**
         * Retrieves the ViewHolder associated with the header at <code>headerPosition</code>.
         * If the ViewHolder returned from <code>parent</code> is null then a temporary ViewHolder
         * will be generated to represent the header.
         *
         * @param headerPosition The position to find the ViewHolder for
         * @return The ViewHolder representing the header at <code>headerPosition</code>
         */
        @Nullable
        @SuppressWarnings("unchecked")
        private RecyclerView.ViewHolder getHeaderViewHolder(int headerPosition) {
            RecyclerView.ViewHolder holder = adapter.onCreateViewHolder(parent, getHeaderViewType(headerPosition));

            //Measure it
            if (!measureViewHolder(holder)) {
                return null;
            }

            //Bind it to get the correct values
            adapter.onBindViewHolder(holder, headerPosition);
            return holder;
        }

        /**
         * Measures the specified <code>holder</code>
         *
         * @param holder The {@link RecyclerView.ViewHolder} to measure
         * @return True if the <code>holder</code> was correctly sized
         */
        private boolean measureViewHolder(RecyclerView.ViewHolder holder) {
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) holder.itemView.getLayoutParams();


            //If the parent ViewGroup wasn't specified when inflating the view (holder.itemView) then the LayoutParams will be null and
            // We may not be able to size the sticky header correctly.
            if (params != null) {
                RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
                int widthSpec = RecyclerView.LayoutManager.getChildMeasureSpec(parent.getWidth(), parent.getPaddingLeft() + parent.getPaddingRight(), params.width, layoutManager.canScrollHorizontally());
                int heightSpec = RecyclerView.LayoutManager.getChildMeasureSpec(parent.getHeight(), parent.getPaddingTop() + parent.getPaddingBottom(), params.height, layoutManager.canScrollVertically());
                holder.itemView.measure(widthSpec, heightSpec);
            } else {
                int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(parent.getWidth(), View.MeasureSpec.AT_MOST);
                int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(parent.getHeight(), View.MeasureSpec.AT_MOST);
                holder.itemView.measure(widthMeasureSpec, heightMeasureSpec);
                Log.e(TAG, "The parent ViewGroup wasn't specified when inflating the view.  This may cause the StickyHeader to be sized incorrectly.");
            }

            //Perform a layout to update the width and height properties of the view
            holder.itemView.layout(0, 0, holder.itemView.getMeasuredWidth(), holder.itemView.getMeasuredHeight());
            return holder.itemView.getWidth() > 0 && holder.itemView.getHeight() > 0;
        }
    }
}
