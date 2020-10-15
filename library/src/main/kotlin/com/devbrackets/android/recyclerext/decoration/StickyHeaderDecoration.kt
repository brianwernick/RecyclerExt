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
package com.devbrackets.android.recyclerext.decoration

import android.graphics.Canvas
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.devbrackets.android.recyclerext.R
import com.devbrackets.android.recyclerext.adapter.header.HeaderApi
import com.devbrackets.android.recyclerext.decoration.header.*
import com.devbrackets.android.recyclerext.decoration.header.StickyHeaderTouchInterceptor.StickyHeaderCallback

/**
 * A RecyclerView Decoration that allows for Header views from
 * the [HeaderAdapter] to be persisted when they
 * reach the start of the RecyclerView's frame.
 */
class StickyHeaderDecoration(parent: RecyclerView) : ItemDecoration(), UpdateListener, StickyHeaderCallback {
    companion object {
        private const val TAG = "StickyHeaderDecoration"
    }

    enum class LayoutOrientation {
        VERTICAL, HORIZONTAL
    }

    protected var parent: RecyclerView?
    protected var adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>?
    protected var headerApi: HeaderApi<*, *>?
    protected var dataObserver: StickyHeaderDataObserver?
    protected var scrollListener: StickyHeaderScrollListener
    protected var touchInterceptor: StickyHeaderTouchInterceptor? = null
    protected var stickyHeader = StickyHeader()

    /**
     * Because of limitations around passing touch events to a view that isn't attached
     * to a parent, and the limitations the RecyclerView has around adding children we
     * require a [ViewGroup] to act as the host for the sticky header when the
     * user has specified that they want the header to be clickable. Having the sticky header
     * in a [ViewGroup] is advantageous anyways because it allows us to get standard
     * animations (e.g. touch animations) whereas without the [ViewGroup] we would have
     * to attempt to perform a manual draw at 60/30 fps.
     */
    protected var touchHeaderContainer: ViewGroup? = null

    /**
     * The orientation to use for edgeScrolling and position calculations
     */
    var orientation = LayoutOrientation.VERTICAL
        set (value) {
            field = value
            stickyHeader.stickyViewOffset.x = 0f
            stickyHeader.stickyViewOffset.y = 0f
        }


    protected var windowLocation = IntArray(2)
    protected var parentStart = Int.MIN_VALUE

    /**
     * Creates the ItemDecoration that performs the functionality to
     * have the current header view sticky (persisted at the start of the
     * RecyclerView).
     *
     *
     * **NOTE:** This will tightly couple to the `parent` and the
     * adapter.  If you intend to swap out adapters you will need to first call
     * [.dispose] then replace this decoration with a new one.
     *
     * @param parent The RecyclerView to couple the ItemDecoration to
     */
    init {
        if (parent.adapter == null || parent.adapter !is HeaderApi<*, *>) {
            throw ExceptionInInitializerError("The Adapter must be set before this is created and extend RecyclerHeaderAdapter, RecyclerHeaderListAdapter or implement HeaderApi")
        }

        this.parent = parent
        adapter = parent.adapter
        headerApi = adapter as HeaderApi<*, *>?
        dataObserver = StickyHeaderDataObserver(this)
        scrollListener = StickyHeaderScrollListener(this)
        adapter!!.registerAdapterDataObserver(dataObserver!!)
        parent.addOnScrollListener(scrollListener)
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val stickyView = stickyHeader.getStickyView(headerApi!!) ?: return

        // If the sticky header is a child of the container we don't need to use onDrawOver to actually draw,
        // just to update the position
        if (touchHeaderContainer != null) {
            stickyView.translationX = stickyHeader.stickyViewOffset.x
            stickyView.translationY = stickyHeader.stickyViewOffset.y
            return
        }

        c.save()
        c.translate(stickyHeader.stickyViewOffset.x, stickyHeader.stickyViewOffset.y)
        stickyView.draw(c)
        c.restore()
    }

    override val stickyView: View?
        get() = stickyHeader.getStickyView(headerApi!!)

    override fun onUpdateStickyHeader() {
        val firstView = findStartView(parent!!)
        if (firstView == null) {
            clearStickyHeader()
            return
        }

        //Retrieve the child position for the view
        val childPosition = headerApi!!.getChildPosition(parent!!.getChildAdapterPosition(firstView))
        if (childPosition < 0) {
            clearStickyHeader()
            return
        }

        //If the next header is different than the current one, perform the swap
        val headerId = getHeaderId(childPosition)
        if (headerId != stickyHeader.currentStickyId) {
            performHeaderSwap(headerId)
        }

        updateHeaderPosition(firstView, childPosition, headerId)
    }

    /**
     * Decouples from the RecyclerView and the RecyclerView.Adapter,
     * clearing out any header associations.
     */
    fun dispose() {
        clearStickyHeader()
        adapter!!.unregisterAdapterDataObserver(dataObserver!!)
        parent!!.removeOnScrollListener(scrollListener)

        disableStickyHeaderTouches()
        adapter = null
        headerApi = null
        dataObserver = null
        parent = null
    }

    /**
     * Clears the current sticky header from the view.
     */
    fun clearStickyHeader() {
        touchHeaderContainer?.removeView(stickyView)
        stickyHeader.reset()
    }

    /**
     * Retrieves if the decoration currently allows touch events to be passed to the
     * Sticky headers.
     *
     * @return `true` if sticky header touching is allowed
     */
    val allowStickyHeaderTouches: Boolean
        get() = touchInterceptor != null

    /**
     * Allows touch events to be passed through to the view (ViewHolder) that
     * represents the header currently stickied. A [ViewGroup] is
     * required to correctly handle the click functionality due to pre-pressed state
     * handling in scrollable containers (i.e. the [RecyclerView]).
     *
     * It is expected that the `stickyHeaderContainer` should match the
     * size of the [RecyclerView] or at the very least match the width and have
     * the top match that of the [RecyclerView] in vertical lists, or have a matching
     * height and the left (or right in rtl) match that of the [RecyclerView]
     *
     * @param stickyHeaderContainer The [ViewGroup] to place the sticky header in
     */
    fun enableStickyHeaderTouches(stickyHeaderContainer: ViewGroup) {
        if (touchHeaderContainer == null || touchHeaderContainer !== stickyHeaderContainer) {
            if (touchInterceptor == null) {
                touchInterceptor = StickyHeaderTouchInterceptor(this)
                parent!!.addOnItemTouchListener(touchInterceptor!!)
            }

            touchHeaderContainer = stickyHeaderContainer
            onUpdateStickyHeader()
        }
    }

    /**
     * Disables touch events from being passed through to the view (ViewHolder) that
     * represents the headers.
     */
    fun disableStickyHeaderTouches() {
        if (touchInterceptor != null) {
            parent!!.removeOnItemTouchListener(touchInterceptor!!)
            touchInterceptor = null
        }

        if (touchHeaderContainer != null) {
            val stickyView = stickyView
            if (stickyView != null) {
                touchHeaderContainer!!.removeView(stickyView)
            }
            touchHeaderContainer = null
        }
    }

    /**
     * Replaces the `stickyHeader` with the header associated with
     * the `headerId`.
     *
     * @param headerId The id for the header view
     */
    protected fun performHeaderSwap(headerId: Long) {
        //If we don't have a valid headerId then clear the current header
        if (headerId == RecyclerView.NO_ID) {
            clearStickyHeader()
            return
        }

        //Get the position of the associated header
        val headerPosition = getHeaderPosition(headerId)
        if (headerPosition == RecyclerView.NO_POSITION) {
            return
        }

        updateHeader(headerId, headerPosition)
    }

    /**
     * Updates the current sticky header to the one with the
     * `headerId`.
     *
     * @param headerId The id for the new sticky header
     * @param headerPosition The position in the RecyclerView for the header
     */
    protected fun updateHeader(headerId: Long, headerPosition: Int) {
        val oldStickyView = stickyView
        stickyHeader.update(headerId, getHeaderViewHolder(headerPosition))
        if (touchHeaderContainer != null) {
            val view = stickyView
            if (view != null && view !== oldStickyView) {
                touchHeaderContainer!!.removeView(oldStickyView)
                touchHeaderContainer!!.addView(view)
            }
        }
    }

    /**
     * Updates the position of the sticky header to handle smoothly transitioning
     * between headers.
     *
     * @param firstView The first visible view in the `parent` [RecyclerView]
     * @param childPosition The child position (not adapter position) for the `firstView`
     * @param headerId The header id associated with the `firstView`
     */
    protected fun updateHeaderPosition(firstView: View, childPosition: Int, headerId: Long) {
        // Updates the header offset so that we smoothly transition between headers
        val isHeader = getHeaderPosition(headerId) == parent!!.getChildAdapterPosition(firstView)
        val isLastChild = getHeaderId(childPosition + 1) != headerId
        val stickyView = stickyHeader.getStickyView(headerApi!!)
        if (stickyView == null || isHeader || !isLastChild) {
            stickyHeader.stickyViewOffset.x = 0f
            stickyHeader.stickyViewOffset.y = 0f
            return
        }

        //TODO: This doesn't work correctly if the Header is larger than the children
        if (orientation == LayoutOrientation.HORIZONTAL) {
            val firstViewStart = windowLocation[0] - parentStart.toFloat()
            stickyHeader.stickyViewOffset.x = Math.min(0f, firstViewStart + firstView.measuredWidth - stickyView.measuredWidth)
            stickyHeader.stickyViewOffset.y = 0f
        } else {
            val firstViewStart = windowLocation[1] - parentStart.toFloat()
            stickyHeader.stickyViewOffset.x = 0f
            stickyHeader.stickyViewOffset.y = Math.min(0f, firstViewStart + firstView.measuredHeight - stickyView.measuredHeight)
        }
    }

    /**
     * Finds the view that is at the start of the list.  This will either be the
     * Left or Top most visible positions.
     *
     * @return The view at the start of the `recyclerView`
     */
    protected fun findStartView(recyclerView: RecyclerView): View? {
        val attachedViewCount = recyclerView.layoutManager!!.childCount

        //Make sure we have the start of the RecyclerView stored
        recyclerView.getLocationInWindow(windowLocation)
        parentStart = if (orientation == LayoutOrientation.HORIZONTAL) windowLocation[0] else windowLocation[1]

        //Finds the first visible view
        for (viewIndex in 0 until attachedViewCount) {
            val view = recyclerView.layoutManager!!.getChildAt(viewIndex)
            view!!.getLocationInWindow(windowLocation)
            val startLoc = if (orientation == LayoutOrientation.HORIZONTAL) windowLocation[0] else windowLocation[1]
            if (startLoc <= parentStart) {
                return view
            }
        }

        //Under normal circumstances we should never reach this return
        return null
    }

    /**
     * Retrieves the id for the header associated with the `childPosition` from
     * the specified `headerAdapter`
     *
     * @param childPosition The child position associated with the header
     * @return The id for the header or [RecyclerView.NO_ID]
     */
    protected fun getHeaderId(childPosition: Int): Long {
        return if (childPosition < 0 || childPosition >= headerApi!!.childCount) {
            RecyclerView.NO_ID
        } else headerApi!!.getHeaderId(childPosition)
    }

    /**
     * Determines the position for the header associated with
     * the `headerId`
     *
     * @param headerId The id to find the header for
     * @return The associated headers position or [RecyclerView.NO_POSITION]
     */
    protected fun getHeaderPosition(headerId: Long): Int {
        return headerApi!!.getHeaderPosition(headerId)
    }

    protected fun getHeaderViewType(headerPosition: Int): Int {
        //Make sure that this is treated as a header type
        return headerApi!!.getHeaderViewType(headerPosition) or HeaderApi.HEADER_VIEW_TYPE_MASK
    }

    /**
     * Retrieves the ViewHolder associated with the header at `headerPosition`.
     * If the ViewHolder returned from `parent` is null then a temporary ViewHolder
     * will be generated to represent the header.
     *
     * @param headerPosition The position to find the ViewHolder for
     * @return The ViewHolder representing the header at `headerPosition`
     */
    protected fun getHeaderViewHolder(headerPosition: Int): RecyclerView.ViewHolder? {
        val holder = adapter!!.onCreateViewHolder(parent!!, getHeaderViewType(headerPosition))
        holder.itemView.setTag(R.id.recyclerext_sticky_header_position_tag_key, headerPosition)
        adapter?.onBindViewHolder(holder, headerPosition)

        //Measure it
        if (!measureViewHolder(holder)) {
            return null
        }

        return holder
    }

    /**
     * Measures the specified `holder`
     *
     * @param holder The [RecyclerView.ViewHolder] to measure
     * @return True if the `holder` was correctly sized
     */
    protected fun measureViewHolder(holder: RecyclerView.ViewHolder): Boolean {
        val params = holder.itemView.layoutParams as RecyclerView.LayoutParams?


        //If the parent ViewGroup wasn't specified when inflating the view (holder.itemView) then the LayoutParams will be null and
        // We may not be able to size the sticky header correctly.
        if (params != null) {
            val layoutManager = parent!!.layoutManager
            val widthSpec = RecyclerView.LayoutManager.getChildMeasureSpec(parent!!.width, layoutManager!!.widthMode, parent!!.paddingLeft + parent!!.paddingRight, params.width, layoutManager.canScrollHorizontally())
            val heightSpec = RecyclerView.LayoutManager.getChildMeasureSpec(parent!!.height, layoutManager.heightMode, parent!!.paddingTop + parent!!.paddingBottom, params.height, layoutManager.canScrollVertically())
            holder.itemView.measure(widthSpec, heightSpec)
        } else {
            val widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(parent!!.width, View.MeasureSpec.AT_MOST)
            val heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(parent!!.height, View.MeasureSpec.AT_MOST)
            holder.itemView.measure(widthMeasureSpec, heightMeasureSpec)
            Log.e(TAG, "The parent ViewGroup wasn't specified when inflating the view.  This may cause the StickyHeader to be sized incorrectly.")
        }

        //Perform a layout to update the width and height properties of the view
        holder.itemView.layout(0, 0, holder.itemView.measuredWidth, holder.itemView.measuredHeight)
        return holder.itemView.width > 0 && holder.itemView.height > 0
    }
}