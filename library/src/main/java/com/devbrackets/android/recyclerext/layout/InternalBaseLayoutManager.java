/*
 * Copyright (C) 2015 Lucas Rocha (TwoWayView), Brian Wernick
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

package com.devbrackets.android.recyclerext.layout;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.support.v7.widget.RecyclerView.LayoutParams;
import android.support.v7.widget.RecyclerView.Recycler;
import android.support.v7.widget.RecyclerView.State;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup.MarginLayoutParams;

import com.devbrackets.android.recyclerext.R;
import com.devbrackets.android.recyclerext.util.SelectionSupport;

import java.util.List;

/**
 *
 */
abstract class InternalBaseLayoutManager extends LayoutManager {
    private RecyclerView recyclerView;

    private boolean isLayoutVertical = true;

    private SavedState pendingSavedState = null;

    private int pendingScrollPosition = RecyclerView.NO_POSITION;
    private int pendingScrollOffset = 0;

    private int layoutStart;
    private int layoutEnd;


    public InternalBaseLayoutManager(LayoutOrientation orientation) {
        isLayoutVertical = orientation == LayoutOrientation.VERTICAL;
    }

    public InternalBaseLayoutManager(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public InternalBaseLayoutManager(Context context, AttributeSet attrs, int defStyle) {
        TypedArray a =  context.obtainStyledAttributes(attrs, R.styleable.recyclerExt_LayoutManager, defStyle, 0);

        int indexCount = a.getIndexCount();
        for (int i = 0; i < indexCount; i++) {
            int attr = a.getIndex(i);

            if (attr == R.styleable.recyclerExt_LayoutManager_android_orientation) {
                int orientation = a.getInt(attr, -1);
                if (orientation >= 0) {
                    setOrientation(LayoutOrientation.values()[orientation]);
                }
            }
        }

        a.recycle();
    }

    @Override
    public int getDecoratedMeasuredWidth(View child) {
        MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
        return super.getDecoratedMeasuredWidth(child) + lp.leftMargin + lp.rightMargin;
    }

    @Override
    public int getDecoratedMeasuredHeight(View child) {
        MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
        return super.getDecoratedMeasuredHeight(child) + lp.topMargin + lp.bottomMargin;
    }

    @Override
    public int getDecoratedLeft(View child) {
        MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
        return super.getDecoratedLeft(child) - lp.leftMargin;
    }

    @Override
    public int getDecoratedTop(View child) {
        MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
        return super.getDecoratedTop(child) - lp.topMargin;
    }

    @Override
    public int getDecoratedRight(View child) {
        MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
        return super.getDecoratedRight(child) + lp.rightMargin;
    }

    @Override
    public int getDecoratedBottom(View child) {
        MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
        return super.getDecoratedBottom(child) + lp.bottomMargin;
    }

    @Override
    public void layoutDecorated(View child, int left, int top, int right, int bottom) {
        MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
        super.layoutDecorated(child, left + lp.leftMargin, top + lp.topMargin, right - lp.rightMargin, bottom - lp.bottomMargin);
    }

    @Override
    public void onAttachedToWindow(RecyclerView view) {
        super.onAttachedToWindow(view);
        recyclerView = view;
    }

    @Override
    public void onDetachedFromWindow(RecyclerView view, Recycler recycler) {
        super.onDetachedFromWindow(view, recycler);
        recyclerView = null;
    }

    @Override
    public void onAdapterChanged(RecyclerView.Adapter oldAdapter, RecyclerView.Adapter newAdapter) {
        super.onAdapterChanged(oldAdapter, newAdapter);

        SelectionSupport itemSelectionSupport = SelectionSupport.from(recyclerView);
        if (oldAdapter != null && itemSelectionSupport != null) {
            itemSelectionSupport.clearChoices();
        }
    }

    @Override
    public void onLayoutChildren(Recycler recycler, State state) {
        SelectionSupport itemSelection = SelectionSupport.from(recyclerView);
        if (itemSelection != null) {
            Bundle itemSelectionState = getPendingItemSelectionState();
            if (itemSelectionState != null) {
                itemSelection.onRestoreInstanceState(itemSelectionState);
            }

            if (state.didStructureChange()) {
                itemSelection.onAdapterDataChanged();
            }
        }

        int anchorItemPosition = getAnchorItemPosition(state);
        detachAndScrapAttachedViews(recycler);
        fillSpecific(anchorItemPosition, recycler, state);

        onLayoutScrapList(recycler, state);

        setPendingScrollPositionWithOffset(RecyclerView.NO_POSITION, 0);
        pendingSavedState = null;
    }

    @Override
    public void onItemsAdded(RecyclerView recyclerView, int positionStart, int itemCount) {
        handleUpdate();
    }

    @Override
    public void onItemsRemoved(RecyclerView recyclerView, int positionStart, int itemCount) {
        handleUpdate();
    }

    @Override
    public void onItemsUpdated(RecyclerView recyclerView, int positionStart, int itemCount) {
        handleUpdate();
    }

    @Override
    public void onItemsMoved(RecyclerView recyclerView, int from, int to, int itemCount) {
        handleUpdate();
    }

    @Override
    public void onItemsChanged(RecyclerView recyclerView) {
        handleUpdate();
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        if (isLayoutVertical) {
            return new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        } else {
            return new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
        }
    }

    @Override
    public boolean supportsPredictiveItemAnimations() {
        return true;
    }

    @Override
    public int scrollHorizontallyBy(int dx, Recycler recycler, State state) {
        if (isLayoutVertical) {
            return 0;
        }

        return scrollBy(dx, recycler, state);
    }

    @Override
    public int scrollVerticallyBy(int dy, Recycler recycler, State state) {
        if (!isLayoutVertical) {
            return 0;
        }

        return scrollBy(dy, recycler, state);
    }

    @Override
    public boolean canScrollHorizontally() {
        return !isLayoutVertical;
    }

    @Override
    public boolean canScrollVertically() {
        return isLayoutVertical;
    }

    @Override
    public void scrollToPosition(int position) {
        scrollToPositionWithOffset(position, 0);
    }

    public void scrollToPositionWithOffset(int position, int offset) {
        setPendingScrollPositionWithOffset(position, offset);
        requestLayout();
    }

    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, State state, int position) {
        LinearSmoothScroller scroller = new LinearSmoothScroller(recyclerView.getContext()) {
            @Override
            public PointF computeScrollVectorForPosition(int targetPosition) {
                if (getChildCount() == 0) {
                    return null;
                }

                int direction = targetPosition < getFirstVisiblePosition() ? -1 : 1;
                if (isLayoutVertical) {
                    return new PointF(0, direction);
                } else {
                    return new PointF(direction, 0);
                }
            }

            @Override
            protected int getVerticalSnapPreference() {
                return LinearSmoothScroller.SNAP_TO_START;
            }

            @Override
            protected int getHorizontalSnapPreference() {
                return LinearSmoothScroller.SNAP_TO_START;
            }
        };

        scroller.setTargetPosition(position);
        startSmoothScroll(scroller);
    }

    @Override
    public int computeHorizontalScrollOffset(State state) {
        if (getChildCount() == 0) {
            return 0;
        }

        return getFirstVisiblePosition();
    }

    @Override
    public int computeVerticalScrollOffset(State state) {
        if (getChildCount() == 0) {
            return 0;
        }

        return getFirstVisiblePosition();
    }

    @Override
    public int computeHorizontalScrollExtent(State state) {
        return getChildCount();
    }

    @Override
    public int computeVerticalScrollExtent(State state) {
        return getChildCount();
    }

    @Override
    public int computeHorizontalScrollRange(State state) {
        return state.getItemCount();
    }

    @Override
    public int computeVerticalScrollRange(State state) {
        return state.getItemCount();
    }

    @Override
    public void onMeasure(Recycler recycler, State state, int widthSpec, int heightSpec) {
        super.onMeasure(recycler, state, widthSpec, heightSpec);
    }

    @Override
    public Parcelable onSaveInstanceState() {
        SavedState state = new SavedState(SavedState.EMPTY_STATE);

        int anchorItemPosition = getPendingScrollPosition();
        if (anchorItemPosition == RecyclerView.NO_POSITION) {
            anchorItemPosition = getFirstVisiblePosition();
        }
        state.anchorItemPosition = anchorItemPosition;

        SelectionSupport itemSelection = SelectionSupport.from(recyclerView);
        if (itemSelection != null) {
            state.itemSelectionState = itemSelection.onSaveInstanceState();
        } else {
            state.itemSelectionState = Bundle.EMPTY;
        }

        return state;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        pendingSavedState = (SavedState) state;
        requestLayout();
    }

    public LayoutOrientation getOrientation() {
        return (isLayoutVertical ? LayoutOrientation.VERTICAL : LayoutOrientation.HORIZONTAL);
    }

    public void setOrientation(LayoutOrientation orientation) {
        boolean isVertical = (orientation == LayoutOrientation.VERTICAL);
        if (this.isLayoutVertical == isVertical) {
            return;
        }

        this.isLayoutVertical = isVertical;
        requestLayout();
    }

    public int getFirstVisiblePosition() {
        if (getChildCount() == 0) {
            return 0;
        }

        return getPosition(getChildAt(0));
    }

    public int getLastVisiblePosition() {
        int childCount = getChildCount();
        if (childCount == 0) {
            return 0;
        }

        return getPosition(getChildAt(childCount - 1));
    }

    protected abstract void measureChild(View child, LayoutDirection direction);
    protected abstract void layoutChild(View child, LayoutDirection direction);
    protected abstract boolean canAddMoreViews(LayoutDirection direction, int limit);

    protected void onLayoutScrapList(Recycler recycler, State state) {
        int childCount = getChildCount();
        if (childCount == 0 || state.isPreLayout() || !supportsPredictiveItemAnimations()) {
            return;
        }

        List<ViewHolder> scrapList = recycler.getScrapList();
        fillFromScrapList(scrapList, LayoutDirection.START);
        fillFromScrapList(scrapList, LayoutDirection.END);
    }

    protected void detachChild(View child, LayoutDirection direction) {
        // Do nothing by default.
    }

    protected int getStartWithPadding() {
        return (isLayoutVertical ? getPaddingTop() : getPaddingLeft());
    }

    protected int getEndWithPadding() {
        if (isLayoutVertical) {
            return (getHeight() - getPaddingBottom());
        } else {
            return (getWidth() - getPaddingRight());
        }
    }

    protected int getChildStart(View child) {
        return (isLayoutVertical ? getDecoratedTop(child) : getDecoratedLeft(child));
    }

    protected int getChildEnd(View child) {
        return (isLayoutVertical ?  getDecoratedBottom(child) : getDecoratedRight(child));
    }

    protected RecyclerView.Adapter getAdapter() {
        return (recyclerView != null ? recyclerView.getAdapter() : null);
    }

    protected int getExtraLayoutSpace(State state) {
        if (state.hasTargetScrollPosition()) {
            return getTotalSpace();
        } else {
            return 0;
        }
    }

    protected void setPendingScrollPositionWithOffset(int position, int offset) {
        pendingScrollPosition = position;
        pendingScrollOffset = offset;
    }

    protected int getPendingScrollPosition() {
        if (pendingSavedState != null) {
            return pendingSavedState.anchorItemPosition;
        }

        return pendingScrollPosition;
    }

    protected int getPendingScrollOffset() {
        if (pendingSavedState != null) {
            return 0;
        }

        return pendingScrollOffset;
    }

    protected int getAnchorItemPosition(State state) {
        int itemCount = state.getItemCount();

        int pendingPosition = getPendingScrollPosition();
        if (pendingPosition != RecyclerView.NO_POSITION) {
            if (pendingPosition < 0 || pendingPosition >= itemCount) {
                pendingPosition = RecyclerView.NO_POSITION;
            }
        }

        if (pendingPosition != RecyclerView.NO_POSITION) {
            return pendingPosition;
        } else if (getChildCount() > 0) {
            return findFirstValidChildPosition(itemCount);
        } else {
            return 0;
        }
    }


    private int getTotalSpace() {
        if (isLayoutVertical) {
            return getHeight() - getPaddingBottom() - getPaddingTop();
        } else {
            return getWidth() - getPaddingRight() - getPaddingLeft();
        }
    }

    private void offsetChildren(int offset) {
        if (isLayoutVertical) {
            offsetChildrenVertical(offset);
        } else {
            offsetChildrenHorizontal(offset);
        }

        layoutStart += offset;
        layoutEnd += offset;
    }

    private void recycleChildrenOutOfBounds(LayoutDirection direction, Recycler recycler) {
        if (direction == LayoutDirection.END) {
            recycleChildrenFromStart(direction, recycler);
        } else {
            recycleChildrenFromEnd(direction, recycler);
        }
    }

    private void recycleChildrenFromStart(LayoutDirection direction, Recycler recycler) {
        int childCount = getChildCount();
        int childrenStart = getStartWithPadding();

        int detachedCount = 0;
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            int childEnd = getChildEnd(child);

            if (childEnd >= childrenStart) {
                break;
            }

            detachedCount++;

            detachChild(child, direction);
        }

        while (--detachedCount >= 0) {
            View child = getChildAt(0);
            removeAndRecycleView(child, recycler);
            updateLayoutEdgesFromRemovedChild(child, direction);
        }
    }

    private void recycleChildrenFromEnd(LayoutDirection direction, Recycler recycler) {
        int childrenEnd = getEndWithPadding();
        int childCount = getChildCount();

        int firstDetachedPos = 0;
        int detachedCount = 0;
        for (int i = childCount - 1; i >= 0; i--) {
            View child = getChildAt(i);
            int childStart = getChildStart(child);

            if (childStart <= childrenEnd) {
                break;
            }

            firstDetachedPos = i;
            detachedCount++;

            detachChild(child, direction);
        }

        while (--detachedCount >= 0) {
            View child = getChildAt(firstDetachedPos);
            removeAndRecycleViewAt(firstDetachedPos, recycler);
            updateLayoutEdgesFromRemovedChild(child, direction);
        }
    }

    private int scrollBy(int delta, Recycler recycler, State state) {
        int childCount = getChildCount();
        if (childCount == 0 || delta == 0) {
            return 0;
        }

        int start = getStartWithPadding();
        int end = getEndWithPadding();
        int firstPosition = getFirstVisiblePosition();

        int totalSpace = getTotalSpace();
        if (delta < 0) {
            delta = Math.max(-(totalSpace - 1), delta);
        } else {
            delta = Math.min(totalSpace - 1, delta);
        }

        boolean cannotScrollBackward = firstPosition == 0 && layoutStart >= start && delta <= 0;
        boolean cannotScrollForward = (firstPosition + childCount == state.getItemCount() && layoutEnd <= end && delta >= 0);

        if (cannotScrollForward || cannotScrollBackward) {
            return 0;
        }

        offsetChildren(-delta);

        LayoutDirection direction = (delta > 0 ? LayoutDirection.END : LayoutDirection.START);
        recycleChildrenOutOfBounds(direction, recycler);

        int absDelta = Math.abs(delta);
        if (canAddMoreViews(LayoutDirection.START, start - absDelta) ||
                canAddMoreViews(LayoutDirection.END, end + absDelta)) {
            fillGap(direction, recycler, state);
        }

        return delta;
    }

    private void fillGap(LayoutDirection direction, Recycler recycler, State state) {
        int childCount = getChildCount();
        int extraSpace = getExtraLayoutSpace(state);
        int firstPosition = getFirstVisiblePosition();

        if (direction == LayoutDirection.END) {
            fillAfter(firstPosition + childCount, recycler, state, extraSpace);
            correctTooHigh(childCount, recycler, state);
        } else {
            fillBefore(firstPosition - 1, recycler, extraSpace);
            correctTooLow(childCount, recycler, state);
        }
    }

    private void fillBefore(int pos, Recycler recycler) {
        fillBefore(pos, recycler, 0);
    }

    private void fillBefore(int position, Recycler recycler, int extraSpace) {
        int limit = getStartWithPadding() - extraSpace;

        while (canAddMoreViews(LayoutDirection.START, limit) && position >= 0) {
            makeAndAddView(position, LayoutDirection.START, recycler);
            position--;
        }
    }

    private void fillAfter(int pos, Recycler recycler, State state) {
        fillAfter(pos, recycler, state, 0);
    }

    private void fillAfter(int position, Recycler recycler, State state, int extraSpace) {
        int limit = getEndWithPadding() + extraSpace;

        int itemCount = state.getItemCount();
        while (canAddMoreViews(LayoutDirection.END, limit) && position < itemCount) {
            makeAndAddView(position, LayoutDirection.END, recycler);
            position++;
        }
    }

    private void fillSpecific(int position, Recycler recycler, State state) {
        if (state.getItemCount() == 0) {
            return;
        }

        makeAndAddView(position, LayoutDirection.END, recycler);

        int extraSpaceBefore;
        int extraSpaceAfter;

        int extraSpace = getExtraLayoutSpace(state);
        if (state.getTargetScrollPosition() < position) {
            extraSpaceAfter = 0;
            extraSpaceBefore = extraSpace;
        } else {
            extraSpaceAfter = extraSpace;
            extraSpaceBefore = 0;
        }

        fillBefore(position - 1, recycler, extraSpaceBefore);

        // This will correct for the top of the first view not
        // touching the top of the parent.
        adjustViewsStartOrEnd();

        fillAfter(position + 1, recycler, state, extraSpaceAfter);
        correctTooHigh(getChildCount(), recycler, state);
    }

    private void correctTooHigh(int childCount, Recycler recycler, State state) {
        // First see if the last item is visible. If it is not, it is OK for the
        // top of the list to be pushed up.
        int lastPosition = getLastVisiblePosition();
        if (lastPosition != state.getItemCount() - 1 || childCount == 0) {
            return;
        }

        // This is bottom of our drawable area.
        int start = getStartWithPadding();
        int end = getEndWithPadding();
        int firstPosition = getFirstVisiblePosition();

        // This is how far the end edge of the last view is from the end of the
        // drawable area.
        int endOffset = end - layoutEnd;

        // Make sure we are 1) Too high, and 2) Either there are more rows above the
        // first row or the first row is scrolled off the top of the drawable area
        if (endOffset > 0 && (firstPosition > 0 || layoutStart < start))  {
            if (firstPosition == 0) {
                // Don't pull the top too far down.
                endOffset = Math.min(endOffset, start - layoutStart);
            }

            // Move everything down
            offsetChildren(endOffset);

            if (firstPosition > 0) {
                // Fill the gap that was opened above first position with more
                // children, if possible.
                fillBefore(firstPosition - 1, recycler);

                // Close up the remaining gap.
                adjustViewsStartOrEnd();
            }
        }
    }

    private void correctTooLow(int childCount, Recycler recycler, State state) {
        // First see if the first item is visible. If it is not, it is OK for the
        // end of the list to be pushed forward.
        int firstPosition = getFirstVisiblePosition();
        if (firstPosition != 0 || childCount == 0) {
            return;
        }

        int start = getStartWithPadding();
        int end = getEndWithPadding();
        int itemCount = state.getItemCount();
        int lastPosition = getLastVisiblePosition();

        // This is how far the start edge of the first view is from the start of the
        // drawable area.
        int startOffset = layoutStart - start;

        // Make sure we are 1) Too low, and 2) Either there are more columns/rows below the
        // last column/row or the last column/row is scrolled off the end of the
        // drawable area.
        if (startOffset > 0) {
            if (lastPosition < itemCount - 1 || layoutEnd > end)  {
                if (lastPosition == itemCount - 1) {
                    // Don't pull the bottom too far up.
                    startOffset = Math.min(startOffset, layoutEnd - end);
                }

                // Move everything up.
                offsetChildren(-startOffset);

                if (lastPosition < itemCount - 1) {
                    // Fill the gap that was opened below the last position with more
                    // children, if possible.
                    fillAfter(lastPosition + 1, recycler, state);

                    // Close up the remaining gap.
                    adjustViewsStartOrEnd();
                }
            } else if (lastPosition == itemCount - 1) {
                adjustViewsStartOrEnd();
            }
        }
    }

    private void adjustViewsStartOrEnd() {
        if (getChildCount() == 0) {
            return;
        }

        int delta = layoutStart - getStartWithPadding();
        if (delta < 0) {
            // We only are looking to see if we are too low, not too high
            delta = 0;
        }

        if (delta != 0) {
            offsetChildren(-delta);
        }
    }

    private static View findNextScrapView(List<ViewHolder> scrapList, LayoutDirection direction, int position) {
        int scrapCount = scrapList.size();

        ViewHolder closest = null;
        int closestDistance = Integer.MAX_VALUE;

        for (int i = 0; i < scrapCount; i++) {
            ViewHolder holder = scrapList.get(i);

            int distance = holder.getPosition() - position;
            if ((distance < 0 && direction == LayoutDirection.END) ||
                    (distance > 0 && direction == LayoutDirection.START)) {
                continue;
            }

            int absDistance = Math.abs(distance);
            if (absDistance < closestDistance) {
                closest = holder;
                closestDistance = absDistance;

                if (distance == 0) {
                    break;
                }
            }
        }

        if (closest != null) {
            return closest.itemView;
        }

        return null;
    }

    private void fillFromScrapList(List<ViewHolder> scrapList, LayoutDirection direction) {
        int firstPosition = getFirstVisiblePosition();

        int position;
        if (direction == LayoutDirection.END) {
            position = firstPosition + getChildCount();
        } else {
            position = firstPosition - 1;
        }

        View scrapChild;
        while ((scrapChild = findNextScrapView(scrapList, direction, position)) != null) {
            setupChild(scrapChild, direction);
            position += (direction == LayoutDirection.END ? 1 : -1);
        }
    }

    private void setupChild(View child, LayoutDirection direction) {
        SelectionSupport itemSelection = SelectionSupport.from(recyclerView);
        if (itemSelection != null) {
            int position = getPosition(child);
            itemSelection.setViewChecked(child, itemSelection.isItemChecked(position));
        }

        measureChild(child, direction);
        layoutChild(child, direction);
    }

    private View makeAndAddView(int position, LayoutDirection direction, Recycler recycler) {
        View child = recycler.getViewForPosition(position);
        boolean isItemRemoved = ((LayoutParams) child.getLayoutParams()).isItemRemoved();

        if (!isItemRemoved) {
            addView(child, (direction == LayoutDirection.END ? -1 : 0));
        }

        setupChild(child, direction);

        if (!isItemRemoved) {
            updateLayoutEdgesFromNewChild(child);
        }

        return child;
    }

    private void handleUpdate() {
        // Refresh state by requesting layout without changing the
        // first visible position. This will ensure the layout will
        // sync with the adapter changes.
        int firstPosition = getFirstVisiblePosition();
        View firstChild = findViewByPosition(firstPosition);
        if (firstChild != null) {
            setPendingScrollPositionWithOffset(firstPosition, getChildStart(firstChild));
        } else {
            setPendingScrollPositionWithOffset(RecyclerView.NO_POSITION, 0);
        }
    }

    private void updateLayoutEdgesFromNewChild(View newChild) {
        int childStart = getChildStart(newChild);
        if (childStart < layoutStart) {
            layoutStart = childStart;
        }

        int childEnd = getChildEnd(newChild);
        if (childEnd > layoutEnd) {
            layoutEnd = childEnd;
        }
    }

    private void updateLayoutEdgesFromRemovedChild(View removedChild, LayoutDirection direction) {
        int childCount = getChildCount();
        if (childCount == 0) {
            resetLayoutEdges();
            return;
        }

        int removedChildStart = getChildStart(removedChild);
        int removedChildEnd = getChildEnd(removedChild);

        if (removedChildStart > layoutStart && removedChildEnd < layoutEnd) {
            return;
        }

        int index;
        int limit;
        if (direction == LayoutDirection.END) {
            // Scrolling towards the end of the layout, child view being
            // removed from the start.
            layoutStart = Integer.MAX_VALUE;
            index = 0;
            limit = removedChildEnd;
        } else {
            // Scrolling towards the start of the layout, child view being
            // removed from the end.
            layoutEnd = Integer.MIN_VALUE;
            index = childCount - 1;
            limit = removedChildStart;
        }

        while (index >= 0 && index <= childCount - 1) {
            View child = getChildAt(index);

            if (direction == LayoutDirection.END) {
                int childStart = getChildStart(child);
                if (childStart < layoutStart) {
                    layoutStart = childStart;
                }

                // Checked enough child views to update the minimum
                // layout start edge, stop.
                if (childStart >= limit) {
                    break;
                }

                index++;
            } else {
                int childEnd = getChildEnd(child);
                if (childEnd > layoutEnd) {
                    layoutEnd = childEnd;
                }

                // Checked enough child views to update the minimum
                // layout end edge, stop.
                if (childEnd <= limit) {
                    break;
                }

                index--;
            }
        }
    }

    private void resetLayoutEdges() {
        layoutStart = getStartWithPadding();
        layoutEnd = layoutStart;
    }

    private Bundle getPendingItemSelectionState() {
        if (pendingSavedState != null) {
            return pendingSavedState.itemSelectionState;
        }

        return null;
    }

    private int findFirstValidChildPosition(int itemCount) {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = getChildAt(i);
            int position = getPosition(view);
            if (position >= 0 && position < itemCount) {
                return position;
            }
        }

        return 0;
    }





    protected static class SavedState implements Parcelable {
        protected static final SavedState EMPTY_STATE = new SavedState();

        private final Parcelable superState;
        private int anchorItemPosition;
        private Bundle itemSelectionState;

        private SavedState() {
            superState = null;
        }

        protected SavedState(Parcelable superState) {
            if (superState == null) {
                throw new IllegalArgumentException("superState must not be null");
            }

            this.superState = (superState != EMPTY_STATE ? superState : null);
        }

        protected SavedState(Parcel in) {
            this.superState = EMPTY_STATE;
            anchorItemPosition = in.readInt();
            itemSelectionState = in.readParcelable(getClass().getClassLoader());
        }

        public Parcelable getSuperState() {
            return superState;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            out.writeInt(anchorItemPosition);
            out.writeParcelable(itemSelectionState, flags);
        }

        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
}