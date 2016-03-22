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

package com.devbrackets.android.recyclerext.layoutmanager;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.IntRange;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewTreeObserver;

import com.devbrackets.android.recyclerext.decoration.SpacerDecoration;

import java.lang.ref.WeakReference;

/**
 * An extension to the {@link GridLayoutManager} that provides
 * the ability to specify the width a grid item is supposed to have instead
 * of the number of columns.  It will then determine the number of columns
 * that are possible, enforcing the size specified by adding spacing
 * between the columns to make sure the grid items width isn't resized.
 *
 * <b>NOTE:</b> Due to limitations of the {@link GridLayoutManager} the layout
 * for the grid items should have a width of "match_parent", otherwise the items
 * won't be correctly centered
 */
public class AutoColumnGridLayoutManager extends GridLayoutManager {
    public enum SpacingMethod {
        ALL,
        EDGES,
        SEPARATOR
    }

    @Nullable
    protected SpacerDecoration spacerDecoration;

    protected int rowSpacing = 0;
    protected int edgeSpacing = 0;
    protected boolean matchSpacing = false;
    protected int minColumnSpacingEdge = 0;
    protected int minColumnSpacingSeparator = 0;
    protected SpacingMethod spacingMethod = SpacingMethod.ALL;

    protected int requestedColumnWidth;
    protected int maxColumnCount = Integer.MAX_VALUE;

    protected WeakReference<RecyclerView> parent = new WeakReference<>(null);

    /**
     * Constructs the layout manager that will correctly determine the number
     * of possible columns based on the <code>gridItemWidth</code> specified.
     *
     * @param context The context to use for the layout manager
     * @param gridItemWidth The width for the items in each column
     */
    public AutoColumnGridLayoutManager(Context context, int gridItemWidth) {
        super(context, 1);
        requestedColumnWidth = gridItemWidth;
    }

    /**
     * When the layout manager is attached to the {@link RecyclerView} then
     * we will run the logic to determine the maximum number of columns
     * allowed with the specified column width.
     *
     * @param recyclerView The {@link RecyclerView} this layout manager is attached to
     */
    @Override
    public void onAttachedToWindow(RecyclerView recyclerView) {
        super.onAttachedToWindow(recyclerView);
        parent = new WeakReference<>(recyclerView);
        setColumnWidth(requestedColumnWidth);
    }

    /**
     * When the layout manager is detached from the {@link RecyclerView} then the
     * decoration that correctly spaces the grid items will be removed.
     *
     * @param recyclerView The {@link RecyclerView} that the layout manager is detaching from
     * @param recycler The {@link RecyclerView.Recycler}
     */
    @Override
    public void onDetachedFromWindow(RecyclerView recyclerView, RecyclerView.Recycler recycler) {
        super.onDetachedFromWindow(recyclerView, recycler);

        //If we have setup the decoration then remove it
        if (spacerDecoration != null) {
            recyclerView.removeItemDecoration(spacerDecoration);
            resetRecyclerPadding(recyclerView);
        }

        parent = new WeakReference<>(null);
    }

    /**
     * Specifies the stable width for the grid items.  This will then be used
     * to determine the maximum number of columns possible.
     *
     * @param gridItemWidth The width for the items in each column
     */
    public void setColumnWidth(int gridItemWidth) {
        requestedColumnWidth = gridItemWidth;
        setSpanCount(determineColumnCount(requestedColumnWidth));
    }

    /**
     * Sets the minimum amount of spacing there should be between items in the grid.  This
     * will be used when determining the number of columns possible with the gridItemWidth specified
     * with {@link #AutoColumnGridLayoutManager(Context, int)} or {@link #setColumnWidth(int)}
     *
     * @param minColumnSpacing The minimum amount of spacing between items in the grid
     */
    public void setMinColumnSpacing(int minColumnSpacing) {
        this.minColumnSpacingSeparator = minColumnSpacing;
        setSpanCount(determineColumnCount(requestedColumnWidth));
    }

    /**
     * Sets the minimum amount of spacing there should be on the sides of the grid.  This
     * will be used when determining the number of columns possible with the gridItemWidth specified
     * with {@link #AutoColumnGridLayoutManager(Context, int)} or {@link #setColumnWidth(int)}
     *
     * @param minEdgeSpacing The minimum amount of spacing that should exist at the edges of the grid
     */
    public void setMinEdgeSpacing(int minEdgeSpacing) {
        this.minColumnSpacingEdge = minEdgeSpacing;
        setSpanCount(determineColumnCount(requestedColumnWidth));
    }

    /**
     * Sets the minimum amount of spacing there should be between edges and items.  This will
     * be used when determining the number of columns possible with the gridItemWidth specified
     * with {@link #AutoColumnGridLayoutManager(Context, int)} or {@link #setColumnWidth(int)}
     *
     * @param minSpacingEdge The minimum amount of spacing between the edge of the RecyclerView and the first and last items in each row
     * @param minSpacingSeparator The minimum amount of spacing between items in each row
     */
    public void setMinColumnSpacing(int minSpacingEdge, int minSpacingSeparator) {
        this.minColumnSpacingEdge = minSpacingEdge;
        this.minColumnSpacingSeparator = minSpacingSeparator;
        setSpanCount(determineColumnCount(requestedColumnWidth));
    }

    /**
     * Sets the amount of spacing that should be between rows.  This value
     * will be overridden when {@link #setMatchRowAndColumnSpacing(boolean)} is set to true
     *
     * @param rowSpacing The amount of spacing that should be between rows [default: 0]
     */
    public void setRowSpacing(int rowSpacing) {
        this.rowSpacing = rowSpacing;
    }

    /**
     * Enables or disables the ability to match the horizontal and vertical spacing
     * between the grid items.  If set to true this will override the value set with
     * {@link #setRowSpacing(int)}
     *
     * @param matchSpacing True to keep the horizontal and vertical spacing equal [default: false]
     */
    public void setMatchRowAndColumnSpacing(boolean matchSpacing) {
        this.matchSpacing = matchSpacing;
    }

    /**
     * Sets the maximum number of columns allowed when determining
     * the count based on the width specified in the constructor or
     * with {@link #setColumnWidth(int)}
     *
     * @param maxColumns The maximum amount of columns allowed [default: {@link Integer#MAX_VALUE}]
     */
    public void setMaxColumnCount(@IntRange(from = 1, to = Integer.MAX_VALUE) int maxColumns) {
        maxColumnCount = maxColumns;
        setSpanCount(determineColumnCount(requestedColumnWidth));
    }

    /**
     * Sets the methodology to use when determining the spacing between columns.
     * <ul>
     *     <li>
     *         {@link SpacingMethod#EDGES} will only increase the size of the left-most and right-most spacing,
     *          leaving the separators with the value specified by {@link #setMinColumnSpacing(int, int)}
     *     </li>
     *     <li>
     *         {@link SpacingMethod#SEPARATOR} will only increase the size of the spacing in between
     *         columns, leaving the edges with the value specified by {@link #setMinColumnSpacing(int, int)}
     *     </li>
     *     <li>
     *         {@link SpacingMethod#ALL} will increase the size of the spacing along both the edges and
     *         the separators, adding based on the relative amounts specified by {@link #setMinColumnSpacing(int, int)}.
     *         (e.g. <code>setMinColumnSpacing(100, 50)</code> will result in the edges growing 2px for every 1px the
     *         separators grow)
     *     </li>
     * </ul>
     *
     * @param spacingMethod The method for displaying the spacing
     */
    public void setSpacingMethod(SpacingMethod spacingMethod) {
        this.spacingMethod = spacingMethod;
        setSpanCount(determineColumnCount(requestedColumnWidth));
    }

    /**
     * Determines the maximum number of columns based on the width of the items.
     * If the <code>recyclerView</code>'s width hasn't been determined yet, this
     * will register for the layout that will then perform the functionality to
     * set the number of columns.
     *
     * @param gridItemWidth The width for the items in each column
     * @return The number of allowed columns
     */
    protected int determineColumnCount(int gridItemWidth) {
        RecyclerView recyclerView = parent.get();
        if (recyclerView == null) {
            return 1;
        }

        //We need to register for the layout then update the column count
        if (recyclerView.getWidth() == 0) {
            ViewTreeObserver observer = recyclerView.getViewTreeObserver();
            observer.addOnGlobalLayoutListener(new LayoutListener(recyclerView));
            return 1;
        }

        //Updates the actual column count and spacing between items
        int columnCount = getColumnCount(recyclerView, gridItemWidth);
        resetRecyclerPadding(recyclerView);
        updateSpacing(recyclerView, gridItemWidth, columnCount);

        return columnCount;
    }

    /**
     * Calculates and adds the amount of spacing that needs to be between each
     * column, row, and the edges of the RecyclerView.  This pays attention to
     * the value from {@link #setSpacingMethod(SpacingMethod)}
     *
     * @param recyclerView The RecyclerView to use for determining the amount of space that needs to be added
     * @param gridItemWidth The requested width for the items
     * @param columnCount The number of columns to display
     */
    protected void updateSpacing(RecyclerView recyclerView, int gridItemWidth, int columnCount) {
        //Sets the decoration for the calculated spacing
        if (spacerDecoration == null) {
            spacerDecoration = new SpacerDecoration();
            spacerDecoration.setAllowedEdgeSpacing(SpacerDecoration.EDGE_SPACING_LEFT | SpacerDecoration.EDGE_SPACING_RIGHT);
            recyclerView.addItemDecoration(spacerDecoration);
        }

        edgeSpacing = minColumnSpacingEdge;
        int separatorSpacing = minColumnSpacingSeparator / 2;


        //Calculates the edge spacing requirements
        int padding = recyclerView.getPaddingLeft() + recyclerView.getPaddingRight();
        int usableWidth = recyclerView.getWidth() - padding;

        int separatorCount = columnCount -1;
        int spacerCount = 2 * columnCount;

        int freeSpace = usableWidth - (gridItemWidth * columnCount);
        int extraSpace = freeSpace - (2 * minColumnSpacingEdge) - (separatorCount * minColumnSpacingSeparator);

        //If we can add spacing, then we need to calculate how much and where to add it
        if (extraSpace >= spacerCount) {
            if (spacingMethod == SpacingMethod.ALL) {
                int totalMinEdges = minColumnSpacingEdge * 2;
                int totalMinSeparators = separatorCount * minColumnSpacingSeparator;

                //If the totalMinSpace is 0, then the percentage is edge count / separators + edges
                int totalMinSpace = totalMinEdges + totalMinSeparators;
                double edgeSpacePercentage = totalMinSpace == 0 ? (2 / (2 + separatorCount)) : (double)totalMinEdges / (double)totalMinSpace;
                int totalSeparatorSpace = (int)((1d - edgeSpacePercentage) * freeSpace);

                separatorSpacing = spacerCount == 0 ? 0 : totalSeparatorSpace / spacerCount;
                edgeSpacing = ((freeSpace - totalSeparatorSpace) / 2) + separatorSpacing;
            } else if (spacingMethod == SpacingMethod.EDGES) {
                edgeSpacing = (freeSpace - (separatorSpacing * spacerCount)) / 2;
            } else { //SEPARATOR
                separatorSpacing = spacerCount == 0 ? 0 : freeSpace / spacerCount;
            }

            edgeSpacing -= separatorSpacing;
        }

        //Updates the spacing using the decoration and padding
        recyclerView.setPadding(
                recyclerView.getPaddingLeft() + edgeSpacing,
                recyclerView.getPaddingTop(),
                recyclerView.getPaddingRight() + edgeSpacing,
                recyclerView.getPaddingBottom()
        );

        spacerDecoration.update(separatorSpacing, matchSpacing ? separatorSpacing : rowSpacing / 2);
    }

    /**
     * Performs the actual calculation for determining the number of possible
     * columns by using the {@link #maxColumnCount}, {@link #minColumnSpacingEdge}, and
     * {@link #minColumnSpacingSeparator} in conjunction with the requested width
     * for the items
     *
     * @param recyclerView The RecyclerView to use when determining the possible number of columns
     * @param gridItemWidth The requested width for items to be
     * @return The calculated number of possible columns
     */
    protected int getColumnCount(RecyclerView recyclerView, int gridItemWidth) {
        int padding = recyclerView.getPaddingLeft() + recyclerView.getPaddingRight();
        int usableWidth = recyclerView.getWidth() - padding;

        int columnCount = Math.min(usableWidth / gridItemWidth, maxColumnCount);
        int usedColumnWidth, minRequiredSpacing;

        //Decreases the columnCount until the specified min spacing can be achieved.
        do {
            usedColumnWidth = columnCount * gridItemWidth;
            minRequiredSpacing = (2 * minColumnSpacingEdge) + ((columnCount -1) * minColumnSpacingSeparator);

            //If the specified min spacing is reached, return the number of columns
            if (usableWidth - usedColumnWidth - minRequiredSpacing >= 0) {
                return columnCount;
            }

            columnCount--;
        } while(columnCount > 1);

        return columnCount;
    }

    /**
     * Removes the padding previously added to the <code>recyclerView</code>
     * for the edge spacing.  If no padding was previously added, then this
     * has no affect
     *
     * @param recyclerView The RecyclerView to remove the padding from
     */
    protected void resetRecyclerPadding(RecyclerView recyclerView) {
        recyclerView.setPadding(
                recyclerView.getPaddingLeft() - edgeSpacing,
                recyclerView.getPaddingTop(),
                recyclerView.getPaddingRight() - edgeSpacing,
                recyclerView.getPaddingBottom()
        );
    }

    /**
     * A Listener for the RecyclerView so that we can correctly update the number of columns
     * once the RecyclerView has been sized
     */
    protected class LayoutListener implements ViewTreeObserver.OnGlobalLayoutListener {
        private RecyclerView recyclerView;

        public LayoutListener(RecyclerView recyclerView) {
            this.recyclerView = recyclerView;
        }

        @Override
        public void onGlobalLayout() {
            removeOnGlobalLayoutListener(recyclerView, this);

            GridLayoutManager gridLayoutManager = (GridLayoutManager)recyclerView.getLayoutManager();
            gridLayoutManager.setSpanCount(determineColumnCount(requestedColumnWidth));
        }

        @SuppressWarnings("deprecation") //removeGlobalOnLayoutListener
        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        public void removeOnGlobalLayoutListener(View v, ViewTreeObserver.OnGlobalLayoutListener listener){
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                v.getViewTreeObserver().removeGlobalOnLayoutListener(listener);
            } else {
                v.getViewTreeObserver().removeOnGlobalLayoutListener(listener);
            }
        }
    }
}
