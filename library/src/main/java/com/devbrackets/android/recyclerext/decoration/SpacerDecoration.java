/*
 * Copyright (C) 2016 Brian Wernick
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

import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.devbrackets.android.recyclerext.annotation.EdgeSpacing;

import java.lang.ref.WeakReference;

/**
 * A simple ItemDecoration for adding space between items in lists
 * or grids
 */
public class SpacerDecoration extends RecyclerView.ItemDecoration {
    public static final int EDGE_SPACING_TOP = 1;
    public static final int EDGE_SPACING_RIGHT = 1 << 1;
    public static final int EDGE_SPACING_BOTTOM = 1 << 2;
    public static final int EDGE_SPACING_LEFT = 1 << 3;

    protected int horizontalSpace;
    protected int verticalSpace;

    @EdgeSpacing
    protected int allowedEdgeSpacing;

    protected SpanLookup spanLookup;

    public SpacerDecoration() {
        this(0, 0);
    }

    public SpacerDecoration(int horizontalSpace) {
        this(horizontalSpace, 0);
    }

    public SpacerDecoration(int horizontalSpace, int verticalSpace) {
        update(horizontalSpace, verticalSpace);
    }

    public void update(int horizontalSpace, int verticalSpace) {
        this.horizontalSpace = horizontalSpace;
        this.verticalSpace = verticalSpace;
    }

    /**
     * Sets the edges that are allowed to add spacing.  Edges are
     * the left side of the left-most items, the right side of the right-most items,
     * the top side of the top-most items, and the bottom side of the bottom-most items.
     *
     * @param edgeSpacingFlags The flags for which edges to add padding to
     */
    public void setAllowedEdgeSpacing(@EdgeSpacing int edgeSpacingFlags) {
        allowedEdgeSpacing = edgeSpacingFlags;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @Nullable RecyclerView.State state) {
        int position = ((RecyclerView.LayoutParams) view.getLayoutParams()).getViewAdapterPosition();
        int childCount = parent.getAdapter().getItemCount();
        if (spanLookup == null) {
            spanLookup = new SpanLookup(parent);
        }

        outRect.left = (allowedEdgeSpacing & EDGE_SPACING_LEFT) == 0 && isLeftEdge(spanLookup, position) ? 0 : horizontalSpace;
        outRect.right = (allowedEdgeSpacing & EDGE_SPACING_RIGHT) == 0 && isRightEdge(spanLookup, position) ? 0 : horizontalSpace;

        outRect.top = (allowedEdgeSpacing & EDGE_SPACING_TOP) == 0 && isTopEdge(spanLookup, position, childCount) ? 0 : verticalSpace;
        outRect.bottom = (allowedEdgeSpacing & EDGE_SPACING_BOTTOM) == 0 && isBottomEdge(spanLookup, position, childCount) ? 0 : verticalSpace;
    }

    /**
     * Determines if the item at <code>position</code> is a top-most view
     *
     * @param spanLookup The SpanLookup related to the parent RecyclerView
     * @param position The position to determine if the view is a top-most view
     * @param childCount The number of children in the RecyclerView (adapter)
     * @return True if the view at <code>position</code> is a top-most view
     */
    protected boolean isTopEdge(@NonNull SpanLookup spanLookup, int position, int childCount) {
        int latestCheckedPosition = 0;
        for (; latestCheckedPosition < childCount; latestCheckedPosition++) {
            int spanEndIndex = spanLookup.getIndex(latestCheckedPosition) + spanLookup.getSpanSize(latestCheckedPosition) - 1;
            if (spanEndIndex == spanLookup.getSpanCount() - 1) {
                break;
            }
        }

        return position <= latestCheckedPosition;
    }

    /**
     * Determines if the item at <code>position</code> is a right-most view
     *
     * @param spanLookup The SpanLookup related to the parent RecyclerView
     * @param position The position to determine if the view is a right-most view
     * @return True if the view at <code>position</code> is a right-most view
     */
    protected boolean isRightEdge(@NonNull SpanLookup spanLookup, int position) {
        int spanIndex = spanLookup.getIndex(position);
        return (spanIndex + spanLookup.getSpanSize(position)) == spanLookup.getSpanCount();
    }

    /**
     * Determines if the item at <code>position</code> is a bottom-most view
     *
     * @param spanLookup The SpanLookup related to the parent RecyclerView
     * @param position The position to determine if the view is a bottom-most view
     * @param childCount The number of children in the RecyclerView (adapter)
     * @return True if the view at <code>position</code> is a bottom-most view
     */
    protected boolean isBottomEdge(@NonNull SpanLookup spanLookup, int position, int childCount) {
        int latestCheckedPosition = childCount -1;
        for (; latestCheckedPosition >= 0; latestCheckedPosition--) {
            int spanIndex = spanLookup.getIndex(latestCheckedPosition);
            if (spanIndex == 0) {
                break;
            }
        }

        return position >= latestCheckedPosition;
    }

    /**
     * Determines if the item at <code>position</code> is a left-most view
     *
     * @param spanLookup The SpanLookup related to the parent RecyclerView
     * @param position The position to determine if the view is a left-most view
     * @return True if the view at <code>position</code> is a left-most view
     */
    protected boolean isLeftEdge(@NonNull SpanLookup spanLookup, int position) {
        int spanIndex = spanLookup.getIndex(position);
        return spanIndex == 0;
    }

    /**
     * A helper class to abstract the lookup from different LayoutManagers
     */
    protected class SpanLookup {
        public WeakReference<GridLayoutManager> gridLayoutManager = new WeakReference<>(null);

        public SpanLookup(RecyclerView recyclerView) {
            RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
            if (layoutManager instanceof GridLayoutManager) {
                gridLayoutManager = new WeakReference<>((GridLayoutManager)layoutManager);
            }
        }

        public int getSpanCount() {
            GridLayoutManager layoutManager = gridLayoutManager.get();
            return layoutManager == null ? 1 : layoutManager.getSpanCount();
        }

        public int getIndex(int position) {
            GridLayoutManager layoutManager = gridLayoutManager.get();
            return layoutManager == null ? 0 : layoutManager.getSpanSizeLookup().getSpanIndex(position, getSpanCount());
        }

        public int getSpanSize(int position) {
            GridLayoutManager layoutManager = gridLayoutManager.get();
            return layoutManager == null ? 1 : layoutManager.getSpanSizeLookup().getSpanSize(position);
        }
    }
}