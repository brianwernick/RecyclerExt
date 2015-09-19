/*
 * Copyright (C) 2015 Brian Wernick
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

package com.devbrackets.android.recyclerext.layoutmanager;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * Extends the default LinearLayoutManager to provide support for wrapping content sizes.
 * More details about this can be found at https://code.google.com/p/android/issues/detail?id=74772
 */
public class WrapLinearLayoutManager extends LinearLayoutManager {
    private int[] measuredDimen = new int[2];

    public WrapLinearLayoutManager(Context context) {
        super(context, VERTICAL, false);
    }

    public WrapLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    public WrapLinearLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void onMeasure(RecyclerView.Recycler recycler, RecyclerView.State state, int widthSpec, int heightSpec) {
        int widthMode = View.MeasureSpec.getMode(widthSpec);
        int heightMode = View.MeasureSpec.getMode(heightSpec);
        int widthSize = View.MeasureSpec.getSize(widthSpec);
        int heightSize = View.MeasureSpec.getSize(heightSpec);

        int width = 0;
        int height = 0;

        for (int i = 0; i < getItemCount(); i++) {
            int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(i, View.MeasureSpec.UNSPECIFIED);
            int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(i, View.MeasureSpec.UNSPECIFIED);
            measureScrapChild(recycler, i, widthMeasureSpec, heightMeasureSpec, measuredDimen);

            if (getOrientation() == HORIZONTAL) {
                width = width + measuredDimen[0];
                if (i == 0) {
                    height = measuredDimen[1];
                }
            } else {
                height = height + measuredDimen[1];
                if (i == 0) {
                    width = measuredDimen[0];
                }
            }
        }

        if (widthMode == View.MeasureSpec.EXACTLY) {
            width = widthSize;
        }

        if (heightMode == View.MeasureSpec.EXACTLY) {
            height = heightSize;
        }

        setMeasuredDimension(View.MeasureSpec.makeMeasureSpec(widthSize, widthMode), height);
    }

    private void measureScrapChild(RecyclerView.Recycler recycler, int position, int widthSpec, int heightSpec, int[] measuredDimension) {
        View view = recycler.getViewForPosition(position);

        if (view != null) {
            RecyclerView.LayoutParams p = (RecyclerView.LayoutParams) view.getLayoutParams();
            int childWidthSpec = ViewGroup.getChildMeasureSpec(widthSpec, getPaddingLeft() + getPaddingRight(), p.width);
            int childHeightSpec = ViewGroup.getChildMeasureSpec(heightSpec, getPaddingTop() + getPaddingBottom(), p.height);

            view.measure(childWidthSpec, childHeightSpec);
            measuredDimension[0] = view.getMeasuredWidth() + p.leftMargin + p.rightMargin;
            measuredDimension[1] = view.getMeasuredHeight() + p.bottomMargin + p.topMargin;

            recycler.recycleView(view);
        }
    }
}