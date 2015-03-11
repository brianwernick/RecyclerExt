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
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Recycler;
import android.support.v7.widget.RecyclerView.State;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.devbrackets.android.recyclerview.R;
import com.devbrackets.android.recyclerview.RecyclerExt;
import com.devbrackets.android.recyclerview.layout.Lanes.LaneInfo;

/**
 *
 */
public class SpannableGridLayoutManager extends GridLayoutManager {
    private static final int DEFAULT_COLUMN_COUNT = 3;
    private static final int DEFAULT_ROW_COUNT = 3;

    protected static class SpannableItemEntry extends BaseLayoutManager.ItemEntry {
        private final int colSpan;
        private final int rowSpan;

        public SpannableItemEntry(int startLane, int anchorLane, int colSpan, int rowSpan) {
            super(startLane, anchorLane);
            this.colSpan = colSpan;
            this.rowSpan = rowSpan;
        }

        public SpannableItemEntry(Parcel in) {
            super(in);
            this.colSpan = in.readInt();
            this.rowSpan = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(colSpan);
            out.writeInt(rowSpan);
        }

        public static final Parcelable.Creator<SpannableItemEntry> CREATOR = new Parcelable.Creator<SpannableItemEntry>() {
            @Override
            public SpannableItemEntry createFromParcel(Parcel in) {
                return new SpannableItemEntry(in);
            }

            @Override
            public SpannableItemEntry[] newArray(int size) {
                return new SpannableItemEntry[size];
            }
        };
    }

    private boolean mMeasuring;

    public SpannableGridLayoutManager(Context context) {
        this(context, null);
    }

    public SpannableGridLayoutManager(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SpannableGridLayoutManager(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle, DEFAULT_COLUMN_COUNT, DEFAULT_ROW_COUNT);
    }

    public SpannableGridLayoutManager(LayoutOrientation orientation, int numColumns, int numRows) {
        super(orientation, numColumns, numRows);
    }

    private int getChildWidth(int colSpan) {
        return getLanes().getLaneSize() * colSpan;
    }

    private int getChildHeight(int rowSpan) {
        return getLanes().getLaneSize() * rowSpan;
    }

    private static int getLaneSpan(LayoutParams lp, boolean isVertical) {
        return (isVertical ? lp.colSpan : lp.rowSpan);
    }

    private static int getLaneSpan(SpannableItemEntry entry, boolean isVertical) {
        return (isVertical ? entry.colSpan : entry.rowSpan);
    }

    @Override
    public boolean canScrollHorizontally() {
        return super.canScrollHorizontally() && !mMeasuring;
    }

    @Override
    public boolean canScrollVertically() {
        return super.canScrollVertically() && !mMeasuring;
    }

    @Override
    public int getLaneSpanForChild(View child) {
        return getLaneSpan((LayoutParams) child.getLayoutParams(), isVertical());
    }

    @Override
    public int getLaneSpanForPosition(int position) {
        SpannableItemEntry entry = (SpannableItemEntry) getItemEntryForPosition(position);
        if (entry == null) {
            throw new IllegalStateException("Could not find span for position " + position);
        }

        return getLaneSpan(entry, isVertical());
    }

    @Override
    public void getLaneForPosition(LaneInfo outInfo, int position, LayoutDirection direction) {
        SpannableItemEntry entry = (SpannableItemEntry) getItemEntryForPosition(position);
        if (entry != null) {
            outInfo.set(entry.startLane, entry.anchorLane);
            return;
        }

        outInfo.setUndefined();
    }

    @Override
    public void getLaneForChild(LaneInfo outInfo, View child, LayoutDirection direction) {
        super.getLaneForChild(outInfo, child, direction);
        if (outInfo.isUndefined()) {
            getLanes().findLane(outInfo, getLaneSpanForChild(child), direction);
        }
    }

    private int getWidthUsed(View child) {
        LayoutParams lp = (LayoutParams) child.getLayoutParams();
        return getWidth() - getPaddingLeft() - getPaddingRight() - getChildWidth(lp.colSpan);
    }

    private int getHeightUsed(View child) {
        LayoutParams lp = (LayoutParams) child.getLayoutParams();
        return getHeight() - getPaddingTop() - getPaddingBottom() - getChildHeight(lp.rowSpan);
    }

    @Override
    protected void measureChildWithMargins(View child) {
        // XXX: This will disable scrolling while measuring this child to ensure that
        // both width and height can use MATCH_PARENT properly.
        mMeasuring = true;
        measureChildWithMargins(child, getWidthUsed(child), getHeightUsed(child));
        mMeasuring = false;
    }

    @Override
    public void moveLayoutToPosition(int position, int offset, Recycler recycler, State state) {
        boolean isVertical = isVertical();
        Lanes lanes = getLanes();

        lanes.reset(0);

        for (int i = 0; i <= position; i++) {
            SpannableItemEntry entry = (SpannableItemEntry) getItemEntryForPosition(i);
            if (entry == null) {
                View child = recycler.getViewForPosition(i);
                entry = (SpannableItemEntry) cacheChildLaneAndSpan(child, LayoutDirection.END);
            }

            tempLaneInfo.set(entry.startLane, entry.anchorLane);

            // The lanes might have been invalidated because an added or
            // removed item. See BaseLayoutManager.invalidateItemLanes().
            if (tempLaneInfo.isUndefined()) {
                lanes.findLane(tempLaneInfo, getLaneSpanForPosition(i), LayoutDirection.END);
                entry.setLane(tempLaneInfo);
            }

            lanes.getChildFrame(tempRect, getChildWidth(entry.colSpan), getChildHeight(entry.rowSpan), tempLaneInfo, LayoutDirection.END);

            if (i != position) {
                pushChildFrame(entry, tempRect, entry.startLane, getLaneSpan(entry, isVertical), LayoutDirection.END);
            }
        }

        lanes.getLane(tempLaneInfo.startLane, tempRect);
        lanes.reset(LayoutDirection.END);
        lanes.offset(offset - (isVertical ? tempRect.bottom : tempRect.right));
    }

    @Override
    public ItemEntry cacheChildLaneAndSpan(View child, LayoutDirection direction) {
        int position = getPosition(child);

        tempLaneInfo.setUndefined();

        SpannableItemEntry entry = (SpannableItemEntry) getItemEntryForPosition(position);
        if (entry != null) {
            tempLaneInfo.set(entry.startLane, entry.anchorLane);
        }

        if (tempLaneInfo.isUndefined()) {
            getLaneForChild(tempLaneInfo, child, direction);
        }

        if (entry == null) {
            final LayoutParams lp = (LayoutParams) child.getLayoutParams();
            entry = new SpannableItemEntry(tempLaneInfo.startLane, tempLaneInfo.anchorLane, lp.colSpan, lp.rowSpan);
            setItemEntryForPosition(position, entry);
        } else {
            entry.setLane(tempLaneInfo);
        }

        return entry;
    }

    @Override
    public boolean checkLayoutParams(RecyclerView.LayoutParams lp) {
        if (lp.width != LayoutParams.MATCH_PARENT || lp.height != LayoutParams.MATCH_PARENT) {
            return false;
        }

        if (lp instanceof LayoutParams) {
            LayoutParams spannableLp = (LayoutParams) lp;

            if (isVertical()) {
                return (spannableLp.rowSpan >= 1 && spannableLp.colSpan >= 1 &&
                        spannableLp.colSpan <= getLaneCount());
            } else {
                return (spannableLp.colSpan >= 1 && spannableLp.rowSpan >= 1 &&
                        spannableLp.rowSpan <= getLaneCount());
            }
        }

        return false;
    }

    @Override
    public LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    @Override
    public LayoutParams generateLayoutParams(ViewGroup.LayoutParams lp) {
        LayoutParams spannableLp = new LayoutParams((ViewGroup.MarginLayoutParams) lp);
        spannableLp.width = LayoutParams.MATCH_PARENT;
        spannableLp.height = LayoutParams.MATCH_PARENT;

        if (lp instanceof LayoutParams) {
            LayoutParams other = (LayoutParams) lp;
            if (isVertical()) {
                spannableLp.colSpan = Math.max(1, Math.min(other.colSpan, getLaneCount()));
                spannableLp.rowSpan = Math.max(1, other.rowSpan);
            } else {
                spannableLp.colSpan = Math.max(1, other.colSpan);
                spannableLp.rowSpan = Math.max(1, Math.min(other.rowSpan, getLaneCount()));
            }
        }

        return spannableLp;
    }

    @Override
    public LayoutParams generateLayoutParams(Context c, AttributeSet attrs) {
        return new LayoutParams(c, attrs);
    }

    public static class LayoutParams extends RecyclerExt.LayoutParams {
        private static final int DEFAULT_SPAN = 1;

        public int rowSpan;
        public int colSpan;

        public LayoutParams(int width, int height) {
            super(width, height);
            rowSpan = DEFAULT_SPAN;
            colSpan = DEFAULT_SPAN;
        }

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);

            TypedArray a = c.obtainStyledAttributes(attrs, R.styleable.recyclerExt_SpannableGridViewChild);
            colSpan = Math.max(DEFAULT_SPAN, a.getInt(R.styleable.recyclerExt_SpannableGridViewChild_colSpan, -1));
            rowSpan = Math.max(DEFAULT_SPAN, a.getInt(R.styleable.recyclerExt_SpannableGridViewChild_rowSpan, -1));
            a.recycle();
        }

        public LayoutParams(ViewGroup.LayoutParams other) {
            super(other);
            init(other);
        }

        public LayoutParams(ViewGroup.MarginLayoutParams other) {
            super(other);
            init(other);
        }

        private void init(ViewGroup.LayoutParams other) {
            if (other instanceof LayoutParams) {
                LayoutParams lp = (LayoutParams) other;
                rowSpan = lp.rowSpan;
                colSpan = lp.colSpan;
            } else {
                rowSpan = DEFAULT_SPAN;
                colSpan = DEFAULT_SPAN;
            }
        }
    }
}