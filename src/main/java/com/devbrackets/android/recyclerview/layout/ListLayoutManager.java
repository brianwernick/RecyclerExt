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
import android.support.v7.widget.RecyclerView.Recycler;
import android.support.v7.widget.RecyclerView.State;
import android.util.AttributeSet;

import com.devbrackets.android.recyclerview.layout.Lanes.LaneInfo;

/**
 *
 */
public class ListLayoutManager extends BaseLayoutManager {
    public ListLayoutManager(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ListLayoutManager(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public ListLayoutManager(Context context, LayoutOrientation orientation) {
        super(orientation);
    }

    @Override
    public int getLaneCount() {
        return 1;
    }

    @Override
    public void getLaneForPosition(LaneInfo outInfo, int position, LayoutDirection direction) {
        outInfo.set(0, 0);
    }

    @Override
    public void moveLayoutToPosition(int position, int offset, Recycler recycler, State state) {
        getLanes().reset(offset);
    }
}
