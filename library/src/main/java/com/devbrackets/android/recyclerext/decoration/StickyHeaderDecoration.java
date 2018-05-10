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

package com.devbrackets.android.recyclerext.decoration;

import android.graphics.Canvas;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.devbrackets.android.recyclerext.adapter.HeaderAdapter;
import com.devbrackets.android.recyclerext.adapter.header.HeaderApi;
import com.devbrackets.android.recyclerext.decoration.header.StickyHeader;
import com.devbrackets.android.recyclerext.decoration.header.StickyHeaderDataObserver;
import com.devbrackets.android.recyclerext.decoration.header.StickyHeaderScrollListener;
import com.devbrackets.android.recyclerext.decoration.header.StickyHeaderTouchInterceptor;
import com.devbrackets.android.recyclerext.decoration.header.UpdateListener;

/**
 * A RecyclerView Decoration that allows for Header views from
 * the {@link HeaderAdapter} to be persisted when they
 * reach the start of the RecyclerView's frame.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class StickyHeaderDecoration extends RecyclerView.ItemDecoration implements UpdateListener, StickyHeaderTouchInterceptor.StickyHeaderCallback {
    private static final String TAG = "StickyHeaderDecoration";

    public enum LayoutOrientation {
        VERTICAL,
        HORIZONTAL
    }

    protected RecyclerView parent;
    protected RecyclerView.Adapter adapter;
    protected HeaderApi headerApi;
    protected StickyHeaderDataObserver dataObserver;
    protected StickyHeaderScrollListener scrollListener;

    @Nullable
    protected StickyHeaderTouchInterceptor touchInterceptor;

    @NonNull
    protected StickyHeader stickyHeader = new StickyHeader();

    /**
     * Because of limitations around passing touch events to a view that isn't attached
     * to a parent, and the limitations the RecyclerView has around adding children we
     * require a {@link ViewGroup} to act as the host for the sticky header when the
     * user has specified that they want the header to be clickable. Having the sticky header
     * in a {@link ViewGroup} is advantageous anyways because it allows us to get standard
     * animations (e.g. touch animations) whereas without the {@link ViewGroup} we would have
     * to attempt to perform a manual draw at 60/30 fps.
     */
    @Nullable
    protected ViewGroup touchHeaderContainer;

    @NonNull
    protected LayoutOrientation orientation = LayoutOrientation.VERTICAL;

    protected int[] windowLocation = new int[2];
    protected int parentStart = Integer.MIN_VALUE;

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
        headerApi = (HeaderApi) adapter;
        dataObserver = new StickyHeaderDataObserver(this);
        scrollListener = new StickyHeaderScrollListener(this);

        adapter.registerAdapterDataObserver(dataObserver);
        parent.addOnScrollListener(scrollListener);
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        View stickyView = stickyHeader.getStickyView(headerApi);
        if (stickyView == null) {
            return;
        }

        // If the sticky header is a child of the container we don't need to use onDrawOver to actually draw,
        // just to update the position
        if (touchHeaderContainer != null) {
            stickyView.setTranslationX(stickyHeader.stickyViewOffset.x);
            stickyView.setTranslationY(stickyHeader.stickyViewOffset.y);
            return;
        }

        c.save();
        c.translate(stickyHeader.stickyViewOffset.x, stickyHeader.stickyViewOffset.y);
        stickyView.draw(c);
        c.restore();
    }

    @Nullable
    @Override
    public View getStickyView() {
        return stickyHeader.getStickyView(headerApi);
    }

    @Override
    public void onUpdateStickyHeader() {
        View firstView = findStartView(parent);
        if (firstView == null) {
            clearStickyHeader();
            return;
        }

        //Retrieve the child position for the view
        int childPosition = headerApi.getChildPosition(parent.getChildAdapterPosition(firstView));
        if (childPosition < 0) {
            clearStickyHeader();
            return;
        }

        //If the next header is different than the current one, perform the swap
        long headerId = getHeaderId(childPosition);
        if (headerId != stickyHeader.currentStickyId) {
            performHeaderSwap(headerId);
        }

        updateHeaderPosition(firstView, childPosition, headerId);
    }

    /**
     * Decouples from the RecyclerView and the RecyclerView.Adapter,
     * clearing out any header associations.
     */
    public void dispose() {
        clearStickyHeader();

        adapter.unregisterAdapterDataObserver(dataObserver);
        parent.removeOnScrollListener(scrollListener);
        disableStickyHeaderTouches();

        adapter = null;
        headerApi = null;
        dataObserver = null;
        parent = null;
    }

    /**
     * Clears the current sticky header from the view.
     */
    public void clearStickyHeader() {
        if (touchHeaderContainer != null) {
            View view = getStickyView();
            touchHeaderContainer.removeView(view);
        }

        stickyHeader.reset();
    }

    /**
     * Sets the orientation of the current layout
     *
     * @param orientation The layouts orientation
     */
    public void setOrientation(@NonNull LayoutOrientation orientation) {
        if (orientation == this.orientation) {
            return;
        }

        this.orientation = orientation;
        stickyHeader.stickyViewOffset.x = 0;
        stickyHeader.stickyViewOffset.y = 0;
    }

    /**
     * Retrieves the current orientation to use for edgeScrolling and position calculations.
     *
     * @return The current orientation [default: {@link LayoutOrientation#VERTICAL}]
     */
    @NonNull
    public LayoutOrientation getOrientation() {
        return orientation;
    }

    /**
     * Retrieves if the decoration currently allows touch events to be passed to the
     * Sticky headers.
     *
     * @return <code>true</code> if sticky header touching is allowed
     */
    public boolean getAllowStickyHeaderTouches() {
        return touchInterceptor != null;
    }

    /**
     * Allows touch events to be passed through to the view (ViewHolder) that
     * represents the header currently stickied. A {@link ViewGroup} is
     * required to correctly handle the click functionality due to pre-pressed state
     * handling in scrollable containers (i.e. the {@link RecyclerView}).
     *
     * It is expected that the <code>stickyHeaderContainer</code> should match the
     * size of the {@link RecyclerView} or at the very least match the width and have
     * the top match that of the {@link RecyclerView} in vertical lists, or have a matching
     * height and the left (or right in rtl) match that of the {@link RecyclerView}
     *
     * @param stickyHeaderContainer The {@link ViewGroup} to place the sticky header in
     */
    public void enableStickyHeaderTouches(@NonNull ViewGroup stickyHeaderContainer) {
        if (this.touchHeaderContainer == null || this.touchHeaderContainer != stickyHeaderContainer) {
            if (touchInterceptor == null) {
                touchInterceptor = new StickyHeaderTouchInterceptor(this);
                parent.addOnItemTouchListener(touchInterceptor);
            }

            this.touchHeaderContainer = stickyHeaderContainer;
            onUpdateStickyHeader();
        }
    }

    /**
     * Disables touch events from being passed through to the view (ViewHolder) that
     * represents the headers.
     */
    public void disableStickyHeaderTouches() {
        if (touchInterceptor != null) {
            parent.removeOnItemTouchListener(touchInterceptor);
            touchInterceptor = null;
        }

        if (touchHeaderContainer != null) {
            View stickyView = getStickyView();
            if (stickyView != null) {
                touchHeaderContainer.removeView(stickyView);
            }

            touchHeaderContainer = null;
        }
    }

    /**
     * Replaces the <code>stickyHeader</code> with the header associated with
     * the <code>headerId</code>.
     *
     * @param headerId The id for the header view
     */
    protected void performHeaderSwap(long headerId) {
        //If we don't have a valid headerId then clear the current header
        if (headerId == RecyclerView.NO_ID) {
            clearStickyHeader();
            return;
        }

        //Get the position of the associated header
        int headerPosition = getHeaderPosition(headerId);
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
    protected void updateHeader(long headerId, int headerPosition) {
        View oldStickyView = getStickyView();
        stickyHeader.update(headerId, getHeaderViewHolder(headerPosition));

        if (touchHeaderContainer != null) {
            View view = getStickyView();
            if (view != null && view != oldStickyView) {
                touchHeaderContainer.removeView(oldStickyView);
                touchHeaderContainer.addView(view);
            }
        }
    }

    /**
     * Updates the position of the sticky header to handle smoothly transitioning
     * between headers.
     *
     * @param firstView The first visible view in the <code>parent</code> {@link RecyclerView}
     * @param childPosition The child position (not adapter position) for the <code>firstView</code>
     * @param headerId The header id associated with the <code>firstView</code>
     */
    protected void updateHeaderPosition(@NonNull View firstView, int childPosition, long headerId) {
        // Updates the header offset so that we smoothly transition between headers
        boolean isHeader = getHeaderPosition(headerId) == parent.getChildAdapterPosition(firstView);
        boolean isLastChild = getHeaderId(childPosition + 1) != headerId;

        View stickyView = stickyHeader.getStickyView(headerApi);
        if (stickyView == null || isHeader || !isLastChild) {
            stickyHeader.stickyViewOffset.x = 0;
            stickyHeader.stickyViewOffset.y = 0;

            return;
        }

        //TODO: This doesn't work correctly if the Header is larger than the children
        if (orientation == LayoutOrientation.HORIZONTAL) {
            float firstViewStart = windowLocation[0] - parentStart;

            stickyHeader.stickyViewOffset.x = Math.min(0, firstViewStart + firstView.getMeasuredWidth() - stickyView.getMeasuredWidth());
            stickyHeader.stickyViewOffset.y = 0;
        } else {
            float firstViewStart = windowLocation[1] - parentStart;

            stickyHeader.stickyViewOffset.x = 0;
            stickyHeader.stickyViewOffset.y = Math.min(0, firstViewStart + firstView.getMeasuredHeight() - stickyView.getMeasuredHeight());
        }
    }

    /**
     * Finds the view that is at the start of the list.  This will either be the
     * Left or Top most visible positions.
     *
     * @return The view at the start of the <code>recyclerView</code>
     */
    @Nullable
    protected View findStartView(@NonNull RecyclerView recyclerView) {
        int attachedViewCount = recyclerView.getLayoutManager().getChildCount();

        //Make sure we have the start of the RecyclerView stored
        recyclerView.getLocationInWindow(windowLocation);
        parentStart = orientation == LayoutOrientation.HORIZONTAL ? windowLocation[0] : windowLocation[1];

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
     * @param childPosition The child position associated with the header
     * @return The id for the header or {@link RecyclerView#NO_ID}
     */
    protected long getHeaderId(int childPosition) {
        if (childPosition < 0 || childPosition >= headerApi.getChildCount()) {
            return RecyclerView.NO_ID;
        }

        return headerApi.getHeaderId(childPosition);
    }

    /**
     * Determines the position for the header associated with
     * the <code>headerId</code>
     *
     * @param headerId The id to find the header for
     * @return The associated headers position or {@link RecyclerView#NO_POSITION}
     */
    protected int getHeaderPosition(long headerId) {
        return headerApi.getHeaderPosition(headerId);
    }

    protected int getHeaderViewType(int headerPosition) {
        //Make sure that this is treated as a header type
        return headerApi.getHeaderViewType(headerPosition) | HeaderApi.HEADER_VIEW_TYPE_MASK;
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
    protected RecyclerView.ViewHolder getHeaderViewHolder(int headerPosition) {
        RecyclerView.ViewHolder holder = adapter.onCreateViewHolder(parent, getHeaderViewType(headerPosition));
        adapter.onBindViewHolder(holder, headerPosition);

        //Measure it
        if (!measureViewHolder(holder)) {
            return null;
        }

        return holder;
    }

    /**
     * Measures the specified <code>holder</code>
     *
     * @param holder The {@link RecyclerView.ViewHolder} to measure
     * @return True if the <code>holder</code> was correctly sized
     */
    protected boolean measureViewHolder(@NonNull RecyclerView.ViewHolder holder) {
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