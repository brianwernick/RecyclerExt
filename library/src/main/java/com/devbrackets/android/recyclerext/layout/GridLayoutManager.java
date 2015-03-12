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
import android.support.v7.widget.RecyclerView.Recycler;
import android.support.v7.widget.RecyclerView.State;
import android.util.AttributeSet;
import android.view.View;

import com.devbrackets.android.recyclerext.R;
/**
 *
 */
public class GridLayoutManager extends BaseLayoutManager {
    private static final int DEFAULT_COLUMN_COUNT = 2;
    private static final int DEFAULT_ROW_COUNT = 2;

    private int columnCount;
    private int rowCount;

    public GridLayoutManager(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GridLayoutManager(Context context, AttributeSet attrs, int defStyle) {
        this(context, attrs, defStyle, DEFAULT_COLUMN_COUNT, DEFAULT_ROW_COUNT);
    }

    protected GridLayoutManager(Context context, AttributeSet attrs, int defStyle, int defaultNumColumns, int defaultNumRows) {
        super(context, attrs, defStyle);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.recyclerExt_GridLayoutManager, defStyle, 0);
        columnCount = Math.max(1, a.getInt(R.styleable.recyclerExt_GridLayoutManager_numColumns, defaultNumColumns));
        rowCount = Math.max(1, a.getInt(R.styleable.recyclerExt_GridLayoutManager_numRows, defaultNumRows));
        a.recycle();
    }

    public GridLayoutManager(LayoutOrientation orientation, int numColumns, int numRows) {
        super(orientation);
        columnCount = numColumns;
        rowCount = numRows;

        if (columnCount < 1) {
            throw new IllegalArgumentException("GridLayoutManager must have at least 1 column");
        }

        if (rowCount < 1) {
            throw new IllegalArgumentException("GridLayoutManager must have at least 1 row");
        }
    }

    @Override
    public int getLaneCount() {
        return (isVertical() ? columnCount : rowCount);
    }

    @Override
    public void getLaneForPosition(Lanes.LaneInfo outInfo, int position, LayoutDirection direction) {
        int lane = (position % getLaneCount());
        outInfo.set(lane, lane);
    }

    @Override
    public void moveLayoutToPosition(int position, int offset, Recycler recycler, State state) {
        Lanes lanes = getLanes();
        lanes.reset(offset);

        getLaneForPosition(tempLaneInfo, position, LayoutDirection.END);
        int lane = tempLaneInfo.startLane;
        if (lane == 0) {
            return;
        }

        View child = recycler.getViewForPosition(position);
        measureChild(child, LayoutDirection.END);

        int dimension = isVertical() ? getDecoratedMeasuredHeight(child) : getDecoratedMeasuredWidth(child);

        for (int i = lane - 1; i >= 0; i--) {
            lanes.offset(i, dimension);
        }
    }

    public int getNumColumns() {
        return columnCount;
    }

    public void setNumColumns(int numColumns) {
        if (columnCount == numColumns) {
            return;
        }

        columnCount = numColumns;
        if (isVertical()) {
            requestLayout();
        }
    }

    public int getNumRows() {
        return rowCount;
    }

    public void setNumRows(int numRows) {
        if (rowCount == numRows) {
            return;
        }

        rowCount = numRows;
        if (!isVertical()) {
            requestLayout();
        }
    }
}