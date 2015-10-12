package com.devbrackets.android.recyclerext.layoutmanager;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewTreeObserver;

/**
 * A simple extension to the {@link GridLayoutManager} that provides
 * the ability to specify the width a grid item is supposed to have instead
 * of the number of columns.  It will then determine the number of columns
 * that are possible, enforcing the size specified by adding spacing
 * between the columns to make sure the grid items width isn't resized.
 */
public class AutoColumnGridLayoutManager extends GridLayoutManager {

    @Nullable
    private SpacerDecoration spacerDecoration;

    private int requestedColumnWidth;

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

        setColumnWidth(requestedColumnWidth, recyclerView);
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
        }
    }

    /**
     * Specifies the stable width for the grid items.  This will then be used
     * to determine the maximum number of columns possible.
     *
     * @param gridItemWidth The width for the items in each column
     * @param recyclerView The {@link RecyclerView} to use for determining the number of columns
     */
    public void setColumnWidth(int gridItemWidth, RecyclerView recyclerView) {
        setSpanCount(determineColumnCount(gridItemWidth, recyclerView));
    }

    /**
     * Determines the maximum number of columns based on the width of the items.
     * If the <code>recyclerView</code>'s width hasn't been determined yet, this
     * will register for the layout that will then perform the functionality to
     * set the number of columns.
     *
     * @param gridItemWidth The width for the items in each column
     * @param recyclerView The {@link RecyclerView} to use for determining the usable width
     * @return The number of allowed columns
     */
    private int determineColumnCount(int gridItemWidth, RecyclerView recyclerView) {
        //We need to register for the layout then update the column count
        if (recyclerView.getWidth() == 0) {
            ViewTreeObserver observer = recyclerView.getViewTreeObserver();
            observer.addOnGlobalLayoutListener(new LayoutListener(recyclerView));
            return 1;
        }

        //If the RecyclerView has been sized then calculate the column width and attach the spacing decoration
        int padding = recyclerView.getPaddingLeft() + recyclerView.getPaddingRight();
        int count = (recyclerView.getWidth() - padding) / gridItemWidth;

        if (spacerDecoration != null) {
            spacerDecoration.update(recyclerView.getWidth(), gridItemWidth, count);
        } else {
            spacerDecoration = new SpacerDecoration(recyclerView.getWidth(), gridItemWidth, count);
            recyclerView.addItemDecoration(spacerDecoration);
        }

        return count;
    }

    /**
     * A Listener for the RecyclerView so that we can correctly update the number of columns
     * once the RecyclerView has been sized
     */
    private class LayoutListener implements ViewTreeObserver.OnGlobalLayoutListener {
        private RecyclerView recyclerView;

        public LayoutListener(RecyclerView recyclerView) {
            this.recyclerView = recyclerView;
        }

        @Override
        public void onGlobalLayout() {
            removeOnGlobalLayoutListener(recyclerView, this);

            GridLayoutManager gridLayoutManager = (GridLayoutManager)recyclerView.getLayoutManager();
            gridLayoutManager.setSpanCount(determineColumnCount(requestedColumnWidth, recyclerView));
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

    /**
     * A decoration to correctly space the items to keep the requested sizes
     */
    private class SpacerDecoration extends RecyclerView.ItemDecoration {
        private int space;

        public SpacerDecoration(int recyclerWidth, int gridItemWidth, int columnCount) {
            update(recyclerWidth, gridItemWidth, columnCount);
        }

        public void update(int recyclerWidth, int gridItemWidth, int columnCount) {
            int extraSpace = recyclerWidth - (gridItemWidth * columnCount);
            int spacerCount = 2 * columnCount;

            //If we are going to have partial pixel spacing, then allow the grid items to grow by ~1px
            if (extraSpace < spacerCount) {
                space = 0;
                return;
            }

            //we want the spacing between items to be split between the left and right
            space = extraSpace / spacerCount;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            outRect.left = space;
            outRect.right = space;
        }
    }
}
