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


package com.devbrackets.android.recyclerext.decoration;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ItemDecoration;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup.MarginLayoutParams;

import com.devbrackets.android.recyclerext.R;
import com.devbrackets.android.recyclerext.layout.BaseLayoutManager;


/**
 * An ItemDecoration where the spacing between items can be specified with drawables
 */
public class DividerDecoration extends ItemDecoration {
    private final SpacingOffsets spacingOffsets;

    @Nullable
    private final Drawable verticalDivider;
    @Nullable
    private final Drawable horizontalDivider;

    public DividerDecoration(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DividerDecoration(Context context, AttributeSet attrs, int defStyle) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.recyclerExt_DividerDecoration, defStyle, 0);
        verticalDivider = a.getDrawable(R.styleable.recyclerExt_DividerDecoration_verticalDivider);
        horizontalDivider = a.getDrawable(R.styleable.recyclerExt_DividerDecoration_horizontalDivider);
        a.recycle();

        spacingOffsets = createSpacing(verticalDivider, horizontalDivider);
    }

    public DividerDecoration(@Nullable Drawable verticalDivider, @Nullable Drawable horizontalDivider) {
        this.verticalDivider = verticalDivider;
        this.horizontalDivider = horizontalDivider;
        spacingOffsets = createSpacing(verticalDivider, horizontalDivider);
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        final BaseLayoutManager lm = (BaseLayoutManager) parent.getLayoutManager();

        int rightWithPadding = parent.getWidth() - parent.getPaddingRight();
        int bottomWithPadding = parent.getHeight() - parent.getPaddingBottom();

        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);

            int childLeft = lm.getDecoratedLeft(child);
            int childTop = lm.getDecoratedTop(child);
            int childRight = lm.getDecoratedRight(child);
            int childBottom = lm.getDecoratedBottom(child);

            drawHorizontalDivider(c, child, childBottom, childLeft, childRight, bottomWithPadding);
            drawVerticalDivider(c, child, childBottom, childTop, childRight, rightWithPadding);
        }
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        spacingOffsets.getItemOffsets(outRect, parent.getChildPosition(view), parent);
    }

    private void drawHorizontalDivider(Canvas c, View childView, int childBottom, int childLeft, int childRight, int bottomWithPadding) {
        if (horizontalDivider == null || childView == null) {
            return;
        }

        MarginLayoutParams params = (MarginLayoutParams) childView.getLayoutParams();
        int bottomOffset = childBottom - childView.getBottom() - params.bottomMargin;
        if (bottomOffset > 0 && childBottom < bottomWithPadding) {
            int top = childBottom - bottomOffset;
            int bottom = top + horizontalDivider.getIntrinsicHeight();

            horizontalDivider.setBounds(childLeft, top, childRight, bottom);
            horizontalDivider.draw(c);
        }
    }

    private void drawVerticalDivider(Canvas c, View childView, int childBottom, int childTop, int childRight, int rightWithPadding) {
        if (verticalDivider == null || childView == null) {
            return;
        }

        MarginLayoutParams params = (MarginLayoutParams) childView.getLayoutParams();
        int rightOffset = childRight - childView.getRight() - params.rightMargin;
        if (rightOffset > 0 && childRight < rightWithPadding) {
            int left = childRight - rightOffset;
            int right = left + verticalDivider.getIntrinsicWidth();

            verticalDivider.setBounds(left, childTop, right, childBottom);
            verticalDivider.draw(c);
        }
    }

    private SpacingOffsets createSpacing(Drawable verticalDivider,  Drawable horizontalDivider) {
        int verticalSpacing = horizontalDivider != null ? horizontalDivider.getIntrinsicHeight() : 0;
        int horizontalSpacing = verticalDivider != null ? verticalDivider.getIntrinsicWidth() : 0;

        final SpacingOffsets spacing = new SpacingOffsets(verticalSpacing, horizontalSpacing);
        spacing.setAddSpacingAtEnd(true);

        return spacing;
    }
}
