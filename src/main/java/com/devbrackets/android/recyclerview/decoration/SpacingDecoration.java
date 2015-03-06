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

package com.devbrackets.android.recyclerview.decoration;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ItemDecoration;
import android.util.AttributeSet;
import android.view.View;

import com.devbrackets.android.recyclerview.R;

/**
 *
 */
public class SpacingDecoration extends ItemDecoration {
    private final SpacingOffsets spacingOffsets;

    public SpacingDecoration(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SpacingDecoration(Context context, AttributeSet attrs, int defStyle) {
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.recyclerExt_SpacingDecoration, defStyle, 0);
        final int verticalSpacing = Math.max(0, a.getInt(R.styleable.recyclerExt_SpacingDecoration_android_verticalSpacing, 0));
        final int horizontalSpacing = Math.max(0, a.getInt(R.styleable.recyclerExt_SpacingDecoration_android_horizontalSpacing, 0));
        a.recycle();

        spacingOffsets = new SpacingOffsets(verticalSpacing, horizontalSpacing);
    }

    public SpacingDecoration(int verticalSpacing, int horizontalSpacing) {
        spacingOffsets = new SpacingOffsets(verticalSpacing, horizontalSpacing);
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        spacingOffsets.getItemOffsets(outRect, parent.getChildPosition(view), parent);
    }
}