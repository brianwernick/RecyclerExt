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

package com.devbrackets.android.recyclerview.layout;

import android.content.Context;
import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.LayoutParams;
import android.support.v7.widget.RecyclerView.Recycler;
import android.support.v7.widget.RecyclerView.State;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;

import com.devbrackets.android.recyclerview.layout.Lanes.LaneInfo;

/**
 *
 */
public abstract class BaseLayoutManager extends InternalBaseLayoutManager {
    private enum UpdateOp {
        ADD,
        REMOVE,
        UPDATE,
        MOVE
    }

    private Lanes lanes;
    private Lanes lanesToRestore;

    private ItemEntries itemEntries;
    private ItemEntries itemEntriesToRestore;

    protected final Rect childFrame = new Rect();
    protected final Rect tempRect = new Rect();
    protected final LaneInfo tempLaneInfo = new LaneInfo();

    public BaseLayoutManager(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BaseLayoutManager(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public BaseLayoutManager(LayoutOrientation orientation) {
        super(orientation);
    }

    protected void pushChildFrame(ItemEntry entry, Rect childFrame, int lane, int laneSpan, LayoutDirection direction) {
        boolean shouldSetMargins = (direction == LayoutDirection.END && entry != null && !entry.hasSpanMargins());

        for (int i = lane; i < lane + laneSpan; i++) {
            int spanMargin;
            if (entry != null && direction != LayoutDirection.END) {
                spanMargin = entry.getSpanMargin(i - lane);
            } else {
                spanMargin = 0;
            }

            int margin = lanes.pushChildFrame(childFrame, i, spanMargin, direction);
            if (laneSpan > 1 && shouldSetMargins) {
                entry.setSpanMargin(i - lane, margin, laneSpan);
            }
        }
    }

    private void popChildFrame(ItemEntry entry, Rect childFrame, int lane, int laneSpan, LayoutDirection direction) {
        for (int i = lane; i < lane + laneSpan; i++) {
            int spanMargin;
            if (entry != null && direction != LayoutDirection.END) {
                spanMargin = entry.getSpanMargin(i - lane);
            } else {
                spanMargin = 0;
            }

            lanes.popChildFrame(childFrame, i, spanMargin, direction);
        }
    }

    public void getDecoratedChildFrame(View child, Rect childFrame) {
        childFrame.left = getDecoratedLeft(child);
        childFrame.top = getDecoratedTop(child);
        childFrame.right = getDecoratedRight(child);
        childFrame.bottom = getDecoratedBottom(child);
    }

    public boolean isVertical() {
        return (getOrientation() == LayoutOrientation.VERTICAL);
    }

    public Lanes getLanes() {
        return lanes;
    }

    public void setItemEntryForPosition(int position, ItemEntry entry) {
        if (itemEntries != null) {
            itemEntries.putItemEntry(position, entry);
        }
    }

    public ItemEntry getItemEntryForPosition(int position) {
        return itemEntries != null ? itemEntries.getItemEntry(position) : null;
    }

    public void clearItemEntries() {
        if (itemEntries != null) {
            itemEntries.clear();
        }
    }

    public void invalidateItemLanesAfter(int position) {
        if (itemEntries != null) {
            itemEntries.invalidateItemLanesAfter(position);
        }
    }

    public void offsetForAddition(int positionStart, int itemCount) {
        if (itemEntries != null) {
            itemEntries.offsetForAddition(positionStart, itemCount);
        }
    }

    public void offsetForRemoval(int positionStart, int itemCount) {
        if (itemEntries != null) {
            itemEntries.offsetForRemoval(positionStart, itemCount);
        }
    }

    private void requestMoveLayout() {
        if (getPendingScrollPosition() != RecyclerView.NO_POSITION) {
            return;
        }

        int position = getFirstVisiblePosition();
        View firstChild = findViewByPosition(position);
        int offset = (firstChild != null ? getChildStart(firstChild) : 0);

        setPendingScrollPositionWithOffset(position, offset);
    }

    private boolean canUseLanes(Lanes lanes) {
        if (lanes == null) {
            return false;
        }

        int laneCount = getLaneCount();
        int laneSize = Lanes.calculateLaneSize(this, laneCount);

        return lanes.getOrientation() == getOrientation() && lanes.getCount() == laneCount && lanes.getLaneSize() == laneSize;
    }

    private boolean ensureLayoutState() {
        int laneCount = getLaneCount();
        if (laneCount == 0 || getWidth() == 0 || getHeight() == 0 || canUseLanes(lanes)) {
            return false;
        }

        Lanes oldLanes = lanes;
        lanes = new Lanes(this, laneCount);

        requestMoveLayout();

        if (itemEntries == null) {
            itemEntries = new ItemEntries();
        }

        if (oldLanes != null && oldLanes.getOrientation() == lanes.getOrientation() && oldLanes.getLaneSize() == lanes.getLaneSize()) {
            invalidateItemLanesAfter(0);
        } else {
            itemEntries.clear();
        }

        return true;
    }

    private void handleUpdate(int positionStart, int itemCountOrToPosition, UpdateOp cmd) {
        invalidateItemLanesAfter(positionStart);

        switch (cmd) {
            case ADD:
                offsetForAddition(positionStart, itemCountOrToPosition);
                break;

            case REMOVE:
                offsetForRemoval(positionStart, itemCountOrToPosition);
                break;

            case MOVE:
                offsetForRemoval(positionStart, 1);
                offsetForAddition(itemCountOrToPosition, 1);
                break;
        }

        if (positionStart + itemCountOrToPosition <= getFirstVisiblePosition()) {
            return;
        }

        if (positionStart <= getLastVisiblePosition()) {
            requestLayout();
        }
    }

    @Override
    public void offsetChildrenHorizontal(int offset) {
        if (!isVertical()) {
            lanes.offset(offset);
        }

        super.offsetChildrenHorizontal(offset);
    }

    @Override
    public void offsetChildrenVertical(int offset) {
        super.offsetChildrenVertical(offset);

        if (isVertical()) {
            lanes.offset(offset);
        }
    }

    @Override
    public void onLayoutChildren(Recycler recycler, State state) {
        boolean restoringLanes = (lanesToRestore != null);
        if (restoringLanes) {
            lanes = lanesToRestore;
            itemEntries = itemEntriesToRestore;

            lanesToRestore = null;
            itemEntriesToRestore = null;
        }

        boolean refreshingLanes = ensureLayoutState();

        // Still not able to create lanes, nothing we can do here,
        // just bail for now.
        if (lanes == null) {
            return;
        }

        int itemCount = state.getItemCount();

        if (itemEntries != null) {
            itemEntries.setAdapterSize(itemCount);
        }

        int anchorItemPosition = getAnchorItemPosition(state);

        // Only move layout if we're not restoring a layout state.
        if (anchorItemPosition > 0 && (refreshingLanes || !restoringLanes)) {
            moveLayoutToPosition(anchorItemPosition, getPendingScrollOffset(), recycler, state);
        }

        lanes.reset(LayoutDirection.START);

        super.onLayoutChildren(recycler, state);
    }

    @Override
    protected void onLayoutScrapList(Recycler recycler, State state) {
        lanes.save();
        super.onLayoutScrapList(recycler, state);
        lanes.restore();
    }

    @Override
    public void onItemsAdded(RecyclerView recyclerView, int positionStart, int itemCount) {
        handleUpdate(positionStart, itemCount, UpdateOp.ADD);
        super.onItemsAdded(recyclerView, positionStart, itemCount);
    }

    @Override
    public void onItemsRemoved(RecyclerView recyclerView, int positionStart, int itemCount) {
        handleUpdate(positionStart, itemCount, UpdateOp.REMOVE);
        super.onItemsRemoved(recyclerView, positionStart, itemCount);
    }

    @Override
    public void onItemsUpdated(RecyclerView recyclerView, int positionStart, int itemCount) {
        handleUpdate(positionStart, itemCount, UpdateOp.UPDATE);
        super.onItemsUpdated(recyclerView, positionStart, itemCount);
    }

    @Override
    public void onItemsMoved(RecyclerView recyclerView, int from, int to, int itemCount) {
        handleUpdate(from, to, UpdateOp.MOVE);
        super.onItemsMoved(recyclerView, from, to, itemCount);
    }

    @Override
    public void onItemsChanged(RecyclerView recyclerView) {
        clearItemEntries();
        super.onItemsChanged(recyclerView);
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        LanedSavedState state = new LanedSavedState(superState);

        int laneCount = (lanes != null ? lanes.getCount() : 0);
        state.lanes = new Rect[laneCount];
        for (int i = 0; i < laneCount; i++) {
            Rect laneRect = new Rect();
            lanes.getLane(i, laneRect);
            state.lanes[i] = laneRect;
        }

        state.orientation = getOrientation();
        state.laneSize = (lanes != null ? lanes.getLaneSize() : 0);
        state.itemEntries = itemEntries;

        return state;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        LanedSavedState ss = (LanedSavedState) state;

        if (ss.lanes != null && ss.laneSize > 0) {
            lanesToRestore = new Lanes(ss.orientation, ss.lanes, ss.laneSize);
            itemEntriesToRestore = ss.itemEntries;
        }

        super.onRestoreInstanceState(ss.getSuperState());
    }

    @Override
    protected boolean canAddMoreViews(LayoutDirection direction, int limit) {
        if (direction == LayoutDirection.START) {
            return (lanes.getInnerStart() > limit);
        } else {
            return (lanes.getInnerEnd() < limit);
        }
    }

    private int getWidthUsed(View child) {
        if (!isVertical()) {
            return 0;
        }

        int size = getLanes().getLaneSize() * getLaneSpanForChild(child);
        return getWidth() - getPaddingLeft() - getPaddingRight() - size;
    }

    private int getHeightUsed(View child) {
        if (isVertical()) {
            return 0;
        }

        int size = getLanes().getLaneSize() * getLaneSpanForChild(child);
        return getHeight() - getPaddingTop() - getPaddingBottom() - size;
    }

    void measureChildWithMargins(View child) {
        measureChildWithMargins(child, getWidthUsed(child), getHeightUsed(child));
    }

    @Override
    protected void measureChild(View child, LayoutDirection direction) {
        cacheChildLaneAndSpan(child, direction);
        measureChildWithMargins(child);
    }

    @Override
    protected void layoutChild(View child, LayoutDirection direction) {
        getLaneForChild(tempLaneInfo, child, direction);

        lanes.getChildFrame(childFrame, getDecoratedMeasuredWidth(child), getDecoratedMeasuredHeight(child), tempLaneInfo, direction);
        ItemEntry entry = cacheChildFrame(child, childFrame);

        layoutDecorated(child, childFrame.left, childFrame.top, childFrame.right, childFrame.bottom);

        final LayoutParams lp = (LayoutParams) child.getLayoutParams();
        if (!lp.isItemRemoved()) {
            pushChildFrame(entry, childFrame, tempLaneInfo.startLane, getLaneSpanForChild(child), direction);
        }
    }

    @Override
    protected void detachChild(View child, LayoutDirection direction) {
        int position = getPosition(child);
        getLaneForPosition(tempLaneInfo, position, direction);
        getDecoratedChildFrame(child, childFrame);

        popChildFrame(getItemEntryForPosition(position), childFrame, tempLaneInfo.startLane, getLaneSpanForChild(child), direction);
    }

    public void getLaneForChild(LaneInfo outInfo, View child, LayoutDirection direction) {
        getLaneForPosition(outInfo, getPosition(child), direction);
    }

    public int getLaneSpanForChild(View child) {
        return 1;
    }

    public int getLaneSpanForPosition(int position) {
        return 1;
    }

    public ItemEntry cacheChildLaneAndSpan(View child, LayoutDirection direction) {
        // Do nothing by default.
        return null;
    }

    public ItemEntry cacheChildFrame(View child, Rect childFrame) {
        // Do nothing by default.
        return null;
    }

    @Override
    public boolean checkLayoutParams(LayoutParams lp) {
        if (isVertical()) {
            return (lp.width == LayoutParams.MATCH_PARENT);
        } else {
            return (lp.height == LayoutParams.MATCH_PARENT);
        }
    }

    @Override
    public LayoutParams generateDefaultLayoutParams() {
        if (isVertical()) {
            return new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        } else {
            return new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
        }
    }

    @Override
    public LayoutParams generateLayoutParams(ViewGroup.LayoutParams lp) {
        final LayoutParams lanedLp = new LayoutParams((MarginLayoutParams) lp);
        if (isVertical()) {
            lanedLp.width = LayoutParams.MATCH_PARENT;
            lanedLp.height = lp.height;
        } else {
            lanedLp.width = lp.width;
            lanedLp.height = LayoutParams.MATCH_PARENT;
        }

        return lanedLp;
    }

    @Override
    public LayoutParams generateLayoutParams(Context c, AttributeSet attrs) {
        return new LayoutParams(c, attrs);
    }

    public abstract int getLaneCount();
    public abstract void getLaneForPosition(LaneInfo outInfo, int position, LayoutDirection direction);
    public abstract void moveLayoutToPosition(int position, int offset, Recycler recycler, State state);





    protected static class LanedSavedState extends SavedState {
        private LayoutOrientation orientation;
        private Rect[] lanes;
        private int laneSize;
        private ItemEntries itemEntries;

        protected LanedSavedState(Parcelable superState) {
            super(superState);
        }

        private LanedSavedState(Parcel in) {
            super(in);

            orientation = LayoutOrientation.values()[in.readInt()];
            laneSize = in.readInt();

            int laneCount = in.readInt();
            if (laneCount > 0) {
                lanes = new Rect[laneCount];
                for (int i = 0; i < laneCount; i++) {
                    final Rect lane = new Rect();
                    lane.readFromParcel(in);
                    lanes[i] = lane;
                }
            }

            int itemEntriesCount = in.readInt();
            if (itemEntriesCount > 0) {
                itemEntries = new ItemEntries();
                for (int i = 0; i < itemEntriesCount; i++) {
                    final ItemEntry entry = in.readParcelable(getClass().getClassLoader());
                    itemEntries.restoreItemEntry(i, entry);
                }
            }
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);

            out.writeInt(orientation.ordinal());
            out.writeInt(laneSize);

            int laneCount = (lanes != null ? lanes.length : 0);
            out.writeInt(laneCount);

            for (int i = 0; i < laneCount; i++) {
                lanes[i].writeToParcel(out, Rect.PARCELABLE_WRITE_RETURN_VALUE);
            }

            int itemEntriesCount = (itemEntries != null ? itemEntries.size() : 0);
            out.writeInt(itemEntriesCount);

            for (int i = 0; i < itemEntriesCount; i++) {
                out.writeParcelable(itemEntries.getItemEntry(i), flags);
            }
        }

        public static final Parcelable.Creator<LanedSavedState> CREATOR = new Parcelable.Creator<LanedSavedState>() {
            @Override
            public LanedSavedState createFromParcel(Parcel in) {
                return new LanedSavedState(in);
            }

            @Override
            public LanedSavedState[] newArray(int size) {
                return new LanedSavedState[size];
            }
        };
    }

    protected static class ItemEntry implements Parcelable {
        public int startLane;
        public int anchorLane;

        private int[] spanMargins;

        public ItemEntry(int startLane, int anchorLane) {
            this.startLane = startLane;
            this.anchorLane = anchorLane;
        }

        public ItemEntry(Parcel in) {
            startLane = in.readInt();
            anchorLane = in.readInt();

            int marginCount = in.readInt();
            if (marginCount > 0) {
                spanMargins = new int[marginCount];
                for (int i = 0; i < marginCount; i++) {
                    spanMargins[i] = in.readInt();
                }
            }
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            out.writeInt(startLane);
            out.writeInt(anchorLane);

            int marginCount = (spanMargins != null ? spanMargins.length : 0);
            out.writeInt(marginCount);

            for (int i = 0; i < marginCount; i++) {
                out.writeInt(spanMargins[i]);
            }
        }

        void setLane(LaneInfo laneInfo) {
            startLane = laneInfo.startLane;
            anchorLane = laneInfo.anchorLane;
        }

        void invalidateLane() {
            startLane = Lanes.NO_LANE;
            anchorLane = Lanes.NO_LANE;
            spanMargins = null;
        }

        private boolean hasSpanMargins() {
            return (spanMargins != null);
        }

        private int getSpanMargin(int index) {
            if (spanMargins == null) {
                return 0;
            }

            return spanMargins[index];
        }

        private void setSpanMargin(int index, int margin, int span) {
            if (spanMargins == null) {
                spanMargins = new int[span];
            }

            spanMargins[index] = margin;
        }

        public static final Creator<ItemEntry> CREATOR = new Creator<ItemEntry>() {
            @Override
            public ItemEntry createFromParcel(Parcel in) {
                return new ItemEntry(in);
            }

            @Override
            public ItemEntry[] newArray(int size) {
                return new ItemEntry[size];
            }
        };
    }
}
