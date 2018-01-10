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
import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.devbrackets.android.recyclerext.R;
import com.devbrackets.android.recyclerext.adapter.HeaderAdapter;
import com.devbrackets.android.recyclerext.adapter.header.HeaderApi;

/**
 * A RecyclerView Decoration that allows for Header views from
 * the {@link HeaderAdapter} to be persisted when they
 * reach the start of the RecyclerView's frame.
 */
@SuppressWarnings("ALL")
public class StickyHeaderDecoration extends RecyclerView.ItemDecoration {
    private static final String TAG = "StickyHeaderDecoration";

    public enum LayoutOrientation {
        VERTICAL,
        HORIZONTAL
    }

    protected RecyclerView parent;
    protected RecyclerView.Adapter adapter;
    protected AdapterDataObserver dataObserver;
    protected StickyViewScrollListener scrollListener;

    @Nullable
    protected StickyViewTouchInterceptor touchInterceptor;

    @NonNull
    protected StickyHeader stickyHeader = new StickyHeader();

    @NonNull
    protected LayoutOrientation orientation = LayoutOrientation.VERTICAL;

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
        View stickyView = stickyHeader.getStickyView((HeaderApi) adapter);
        if (stickyView != null) {
            c.save();
            c.translate(stickyHeader.stickyViewOffset.x, stickyHeader.stickyViewOffset.y);
            stickyView.draw(c);
            c.restore();
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
        setAllowStickyHeaderTouches(false);

        adapter = null;
        dataObserver = null;
        parent = null;
    }

    /**
     * Clears the current sticky header from the view.
     */
    public void clearStickyHeader() {
        stickyHeader.reset();
    }

    /**
     * Sets the orientation of the current layout
     *
     * @param orientation The layouts orientation
     */
    public void setOrientation(LayoutOrientation orientation) {
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
     * Enables or disables touches from being passed to the sticky headers.
     * When enabled the touch events will be passed through to the view (ViewHolder) that
     * represents the header currently stickied.
     *
     * @param allowTouches <code>true</code> to enable sticky header touches
     */
    public void setAllowStickyHeaderTouches(boolean allowTouches) {
        if (allowTouches && touchInterceptor == null) {
            touchInterceptor = new StickyViewTouchInterceptor();
            parent.addOnItemTouchListener(touchInterceptor);
        } else if (!allowTouches && touchInterceptor != null) {
            parent.removeOnItemTouchListener(touchInterceptor);
            touchInterceptor = null;
        }
    }

    protected static class StickyHeader {
        @Nullable
        protected RecyclerView.ViewHolder stickyViewHolder;
        @Nullable
        protected View cachedStickyView;
        @NonNull
        public PointF stickyViewOffset = new PointF(0, 0);
        public long currentStickyId = RecyclerView.NO_ID;

        public void reset() {
            update(RecyclerView.NO_ID, null);

            stickyViewOffset.x = 0;
            stickyViewOffset.y = 0;
        }

        public void update(long stickyId, @Nullable RecyclerView.ViewHolder holder) {
            stickyViewHolder = holder;
            cachedStickyView = null;
            currentStickyId = stickyId;
        }

        @Nullable
        public View getStickyView(@NonNull HeaderApi headerApi) {
            if (cachedStickyView != null) {
                return cachedStickyView;
            }

            RecyclerView.ViewHolder holder = stickyViewHolder;
            if (holder == null) {
                return null;
            }

            // If we have a ViewHolder we should have a view, but just to be safe we check
            int stickyViewId = headerApi.getCustomStickyHeaderViewId();
            cachedStickyView = stickyViewId != 0 ? holder.itemView.findViewById(stickyViewId) : holder.itemView;
            return cachedStickyView;
        }
    }

    /**
     * An observer to watch the adapter for changes so that we can update the
     * current sticky header
     */
    protected class AdapterDataObserver extends RecyclerView.AdapterDataObserver {
        @Override
        public void onChanged() {
            //For now we just clear the stick header
            clearStickyHeader();
        }
    }

    /**
     * Handles intercepting touch events in the RecyclerView to handle interaction in the
     * sticky header.
     */
    protected class StickyViewTouchInterceptor implements RecyclerView.OnItemTouchListener {
        protected boolean allowInterception = true;
        protected boolean capturedTouchDown = false;

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent event) {
            // Ignores touch events we don't want to intercept
            if (event.getAction() != MotionEvent.ACTION_DOWN && !capturedTouchDown) {
                return false;
            }

            View stickyView = getInterceptView();
            if (stickyView == null) {
                capturedTouchDown = false;
                return false;
            }

            // Makes sure to un-register capturing so that we don't accidentally interfere with scrolling
            if (event.getAction() == MotionEvent.ACTION_UP) {
                capturedTouchDown = false;
            }

            // Determine if the event is boxed by the view and pass the event through
            boolean bounded = (event.getX() >= stickyView.getX() && event.getX() <= stickyView.getX() + stickyView.getMeasuredWidth()) &&
                    (event.getY() >= stickyView.getY() && event.getY() <= stickyView.getY() + stickyView.getMeasuredHeight());

            if (!bounded) {
                return false;
            }

            // Updates the filter
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                capturedTouchDown = true;
            }

            return dispatchChildTouchEvent(stickyView, event);
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent event) {
            // Makes sure to un-register capturing so that we don't accidentally interfere with scrolling
            if (event.getAction() == MotionEvent.ACTION_UP) {
                capturedTouchDown = false;
            }

            View stickyView = getInterceptView();
            if (stickyView == null) {
                return;
            }

            dispatchChildTouchEvent(stickyView, event);
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
            allowInterception = !disallowIntercept;
        }

        @Nullable
        protected View getInterceptView() {
            if (!allowInterception) {
                return null;
            }

            return stickyHeader.getStickyView((HeaderApi) adapter);
        }

        protected boolean dispatchChildTouchEvent(@NonNull View child, @NonNull MotionEvent event) {
            //TODO: this doesn't work because the sticky ViewHolder hasn't been attached to a parent
            // And because the RecyclerView requires children to have a layoutPosition and we would have other issues
            // So we can attach it to the parent.getParent() however that depends on what layout the
            // RecyclerView is contained in (a relative layout or constraint layout would be fine, but a linear
            // layout, etc. wouldn't).
            // Other sticky header libraries allow just a single touch target for the entire header
            // so we have 2 options that I can see:
            // 1. Have a single touch target for the sticky header
            // 2. When sticky header touch is enabled we need to (includes animations, etc.)
            //      a. require the RV is inside a RelativeLayout
            //      b. require the user to pass in a view that can contain the sticky header
            // NOTE: if we do #2 then we don't need to perform an onDrawOver custom draw

            // Pass the event through
            boolean handledEvent = child.dispatchTouchEvent(event);
            if (handledEvent) {
                parent.postInvalidate();
            }

            return handledEvent;
        }
    }

    /**
     * Listens to the scroll events for the RecyclerView that will have
     * sticky headers.  When a new header reaches the start it will be
     * transformed in to a sticky view and attached to the start of the
     * RecyclerView.  Additionally, when a new header is reaching the
     * start, the headers will be transitioned smoothly
     */
    protected class StickyViewScrollListener extends RecyclerView.OnScrollListener {
        protected int[] windowLocation = new int[2];
        protected int parentStart = Integer.MIN_VALUE;

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
                clearStickyHeader();
                return;
            }

            //If the next header is different than the current one, perform the swap
            long headerId = getHeaderId(adapter, childPosition);
            if (headerId != stickyHeader.currentStickyId) {
                performHeaderSwap(headerId);
            }

            updateHeaderPosition(firstView, childPosition, headerId);
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
        protected void updateHeader(long headerId, int headerPosition) {
            stickyHeader.update(headerId, getHeaderViewHolder(headerPosition));
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
            boolean isHeader = getHeaderPosition(adapter, headerId) == parent.getChildAdapterPosition(firstView);
            boolean isLastChild = getHeaderId(adapter, childPosition + 1) != headerId;

            View stickyView = stickyHeader.getStickyView((HeaderApi) adapter);
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
        protected View findStartView(RecyclerView recyclerView) {
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
        protected long getHeaderId(RecyclerView.Adapter headerAdapter, int childPosition) {
            if (!(headerAdapter instanceof HeaderApi)) {
                return RecyclerView.NO_ID;
            }

            HeaderApi headerApi = (HeaderApi) headerAdapter;
            if (childPosition < 0 || childPosition >= headerApi.getChildCount()) {
                return RecyclerView.NO_ID;
            }

            return headerApi.getHeaderId(childPosition);
        }

        /**
         * Determines the position for the header associated with
         * the <code>headerId</code>
         *
         * @param headerAdapter The adapter to use when finding the header position
         * @param headerId The id to find the header for
         * @return The associated headers position or {@link RecyclerView#NO_POSITION}
         */
        protected int getHeaderPosition(RecyclerView.Adapter headerAdapter, long headerId) {
            if (headerAdapter instanceof HeaderApi) {
                return ((HeaderApi) adapter).getHeaderPosition(headerId);
            }

            return RecyclerView.NO_POSITION;
        }

        protected int getHeaderViewType(int headerPosition) {
            int rawType = ((HeaderApi) adapter).getHeaderViewType(headerPosition);

            //Make sure that this is treated as a header type
            return rawType | HeaderApi.HEADER_VIEW_TYPE_MASK;
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
        protected boolean measureViewHolder(RecyclerView.ViewHolder holder) {
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
