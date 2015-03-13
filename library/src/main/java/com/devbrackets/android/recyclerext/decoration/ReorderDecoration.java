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

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;

import com.devbrackets.android.recyclerext.layout.LayoutOrientation;

/**
 * An ItemDecoration that performs the functionality to show the reordering of
 * list items without any space between items.
 */
public class ReorderDecoration extends RecyclerView.ItemDecoration implements RecyclerView.OnItemTouchListener {
    public static final int NO_POSITION = -1;
    public static final int INVALID_RESOURCE_ID = 0;

    private static final float MAX_EDGE_DETECTION_THRESHOLD = 0.5f;
    private static final float DEFAULT_EDGE_SCROLL_SPEED = 0.5f;
    private static final float DEFAULT_EDGE_DETECTION_THRESHOLD = 0.01f;

    private enum DragState {
        DRAGGING,
        ENDED
    }

    private RecyclerView recyclerView;
    private DragState dragState = DragState.ENDED;

    private LayoutOrientation orientation = LayoutOrientation.VERTICAL;
    private boolean edgeScrollingEnabled = true;

    private float edgeDetectionThreshold = DEFAULT_EDGE_DETECTION_THRESHOLD;
    private float edgeScrollSpeed = DEFAULT_EDGE_SCROLL_SPEED;

    @Nullable
    private PointF fingerOffset;
    private BitmapDrawable dragItem;

    private int selectedDragItemPosition = NO_POSITION;
    private Rect floatingItemStartingBounds;
    private Rect floatingItemBounds;

    private int dragHandleId = INVALID_RESOURCE_ID;

    public ReorderDecoration(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        if (dragItem != null) {
            dragItem.draw(c);
        }
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);

        if (selectedDragItemPosition == NO_POSITION) {
            view.setVisibility(View.VISIBLE);
            return;
        }

        int itemPosition = recyclerView.getChildPosition(view);
        if (itemPosition == selectedDragItemPosition) {
            view.setVisibility(View.INVISIBLE);
            return;
        }

        //Make sure the view is visible
        view.setVisibility(View.VISIBLE);

        //Calculate the new offsets
        PointF middle = getFloatingItemCenter();
        setVerticalOffsets(view, itemPosition, middle, outRect);
        setHorizontalOffsets(view, itemPosition, middle, outRect);
    }

    /**
     * This will determine two things.
     *  1. If we need to handle the touch event
     *  2. If reordering needs to start due to dragHandle being clicked
     */
    @Override
    public boolean onInterceptTouchEvent(RecyclerView recyclerView, MotionEvent event) {
        if (dragState == DragState.DRAGGING) {
            return true;
        }

        if (dragHandleId == INVALID_RESOURCE_ID) {
            return false;
        }

        View itemView = recyclerView.findChildViewUnder(event.getX(), event.getY());
        if (itemView == null) {
            return false;
        }

        View handleView = itemView.findViewById(dragHandleId);
        if (handleView == null || handleView.getVisibility() != View.VISIBLE) {
            return false;
        }


        int[] handlePosition = new int[2];
        handleView.getLocationOnScreen(handlePosition);

        //Determine if the MotionEvent is inside the handle
        if ((event.getRawX() >= handlePosition[0] && event.getRawX() <= handlePosition[0] + handleView.getWidth()) &&
                (event.getRawY() >= handlePosition[1] && event.getRawY() <= handlePosition[1] + handleView.getHeight())) {
            startReorder(itemView, event);
            return true;
        }

        return false;
    }

    //TODO: optimize object creation (since this will be called quite often)
    @Override
    public void onTouchEvent(RecyclerView recyclerView, MotionEvent event) {
        if (dragState != DragState.DRAGGING) {
            return;
        }

        //Makes sure to perform the end reorder operations...
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                if (selectedDragItemPosition != NO_POSITION) {
                    int newPos = calculateNewPosition();
                    //TODO: inform the listener that the view has a new position
                }
                //Purposefully fall through

            case MotionEvent.ACTION_CANCEL:
                endReorder();
                return;
        }

        //Finds the new location
        PointF position = new PointF(event.getX(), event.getY());

        //Updates the floating views bounds
        if (dragItem != null) {
            PointF middle = getFloatingItemCenter();

            //Make sure the dragItem bounds are correct
            updateVerticalBounds(position, middle);
            updateHorizontalBounds(position, middle);
            dragItem.setBounds(floatingItemBounds);
        }

        //Perform the edge scrolling if necessary
        performVerticalEdgeScroll(position);
        performHorizontalEdgeScroll(position);
    }

    /**
     * Sets the id for the view that will act as an immediate drag handle.
     * This means that once the view has been touched that the drag will be
     * started.
     *
     * @param handleId The Resource ID for the drag handle or {@link #INVALID_RESOURCE_ID}
     */
    public void setDragHandleId(@IdRes int handleId) {
        dragHandleId = handleId;
    }

    /**
     * Sets whether the items should start scrolling once the view being reordered
     * hits the edge of the containing view.
     *
     * @param enabled True to scroll once the view being reordered hits the edge
     */
    public void setEdgeScrollingEnabled(boolean enabled) {
        edgeScrollingEnabled = enabled;
    }

    /**
     * Retrieves whether the items should start scrolling once the view being reordered
     * hits the edge of the containing view.
     *
     * @return True if edge scrolling is enabled
     */
    public boolean isEdgeScrollingEnabled() {
        return edgeScrollingEnabled;
    }

    /**
     * Sets the percent amount in relation to the size of the recyclerView
     * for the edge scrolling to use.
     *
     * @param speed The percent amount [0.0 - 1.0]
     */
    public void setEdgeScrollSpeed(float speed) {
        if (edgeScrollSpeed < 0 || edgeScrollSpeed > 1) {
            return;
        }

        edgeScrollSpeed = speed;
    }

    /**
     * Retrieves the edge scroll speed
     *
     * @return [default: {@value #DEFAULT_EDGE_SCROLL_SPEED}]
     */
    public float getEdgeScrollSpeed() {
        return edgeScrollSpeed;
    }

    /**
     * Sets the percent threshold at the edges of the recyclerView to start the
     * edge scrolling.  This threshold can be between 0 (no edge) and {@value #MAX_EDGE_DETECTION_THRESHOLD}
     * (half of the recyclerView)
     *
     * @param threshold The edge scrolling threshold [0.0 - {@value #MAX_EDGE_DETECTION_THRESHOLD}]
     */
    public void setEdgeThreshold(float threshold) {
        if (threshold < 0 || threshold > MAX_EDGE_DETECTION_THRESHOLD) {
            return;
        }

        edgeDetectionThreshold = threshold;
    }

    /**
     * Retrieves the edge threshold for the edge scrolling.
     *
     * @return The current edge threshold [0.0 - {@value #MAX_EDGE_DETECTION_THRESHOLD}] [default: {@value #DEFAULT_EDGE_DETECTION_THRESHOLD}]
     */
    public float getEdgeThreshold() {
        return edgeDetectionThreshold;
    }

    /**
     * Sets the orientation of the current layout.  This will aid in the calculations for
     * edgeScrolling {@link #setEdgeScrollingEnabled(boolean)} and determining the new position
     * in the list on {@link #endReorder()}
     *
     * @param orientation The layouts orientation
     */
    public void setOrientation(LayoutOrientation orientation) {
        this.orientation = orientation;
    }

    /**
     * Retrieves the current orientation to use for edgeScrolling and position calculations.
     *
     * @return The current orientation [default: {@link LayoutOrientation#VERTICAL}]
     */
    public LayoutOrientation getOrientation() {
        return orientation;
    }

    /**
     * Manually starts the reorder process for the specified view.  This should not be used if the {@link #setDragHandleId(int)} is
     * set and should control the reordering.
     *
     * @param view The View to start reordering
     * @param startMotionEvent The MotionEvent that starts the reorder
     */
    public void startReorder(View view, @Nullable MotionEvent startMotionEvent) {
        if (dragState == DragState.DRAGGING) {
            return;
        }

        if (startMotionEvent != null) {
            fingerOffset = new PointF(startMotionEvent.getRawX(), startMotionEvent.getRawY());

            int[] rawViewLoc = new int[2];
            view.getLocationOnScreen(rawViewLoc);
            fingerOffset.x = rawViewLoc[0] - fingerOffset.x;
            fingerOffset.y = rawViewLoc[1] - fingerOffset.y;
        }

        dragState = DragState.DRAGGING;
        dragItem = createDragBitmap(view);

        selectedDragItemPosition = recyclerView.getChildPosition(view);
    }

    /**
     * Ends the reorder process.  This should only be called if {@link #startReorder(View, MotionEvent)} has been
     * manually called.
     */
    public void endReorder() {
        if (dragState != DragState.DRAGGING) {
            return;
        }

        dragState = DragState.ENDED;
        fingerOffset = null;
        dragItem = null;

        selectedDragItemPosition = NO_POSITION;
        recyclerView.invalidateItemDecorations();
    }

    /**
     * Calculates the position the item should have when it is dropped.
     *
     * @return The new position for the item
     */
    public int calculateNewPosition() {
        int itemsOnScreen = recyclerView.getLayoutManager().getChildCount();
        PointF middle = getFloatingItemCenter();

        int before = 0;
        int after = Integer.MAX_VALUE;
        for (int n = 0; n < itemsOnScreen; n++) {

            //Grabs the view at n
            View view = recyclerView.getLayoutManager().getChildAt(n);
            if (view.getVisibility() != View.VISIBLE) {
                continue;
            }

            //Makes sure we don't compare to itself
            int itemPos = recyclerView.getChildPosition(view);
            if (itemPos == selectedDragItemPosition) {
                continue;
            }

            //Performs the Vertical position calculations
            if (orientation == LayoutOrientation.VERTICAL) {
                float viewMiddleY = (view.getTop() + view.getHeight()) / 2;
                if (middle.y > viewMiddleY && itemPos > before) {
                    before = itemPos;
                } else if (middle.y <= viewMiddleY && itemPos < after) {
                    after = itemPos;
                }
            }

            //Performs the Horizontal position calculations
            if (orientation == LayoutOrientation.HORIZONTAL) {
                float viewMiddleX = (view.getLeft() + view.getWidth()) / 2;
                if (middle.x > viewMiddleX && itemPos > before) {
                    before = itemPos;
                } else if (middle.x <= viewMiddleX && itemPos < after) {
                    after = itemPos;
                }
            }
        }

        if (after != Integer.MAX_VALUE) {
            if (after < selectedDragItemPosition) {
                after++;
            }

            return after - 1;
        } else {
            if (before < selectedDragItemPosition) {
                before++;
            }

            return before;
        }
    }

    private PointF getFloatingItemCenter() {
        PointF center = new PointF(0, 0);
        center.x = floatingItemBounds.left + (floatingItemStartingBounds.width() / 2);
        center.y = floatingItemBounds.top + (floatingItemStartingBounds.height() / 2);

        return center;
    }

    private void setVerticalOffsets(View view, int itemPosition, PointF middle, Rect outRect) {
        if (itemPosition > selectedDragItemPosition && view.getTop() < middle.y) {
            float amountUp = (middle.y - view.getTop()) / (float) view.getHeight();
            if (amountUp > 1) {
                amountUp = 1;
            }

            outRect.top = -(int) (floatingItemBounds.height() * amountUp);
            outRect.bottom = (int) (floatingItemBounds.height() * amountUp);
        }

        if ((itemPosition < selectedDragItemPosition) && (view.getBottom() > middle.y)) {
            float amountDown = ((float) view.getBottom() - middle.y) / (float) view.getHeight();
            if (amountDown > 1) {
                amountDown = 1;
            }

            outRect.top = (int) (floatingItemBounds.height() * amountDown);
            outRect.bottom = -(int) (floatingItemBounds.height() * amountDown);
        }
    }

    private void setHorizontalOffsets(View view, int itemPosition, PointF middle, Rect outRect) {
        if (itemPosition > selectedDragItemPosition && view.getRight() < middle.y) {
            float amountRight = (middle.x - view.getRight()) / (float) view.getWidth();
            if (amountRight > 1) {
                amountRight = 1;
            }

            outRect.top = -(int) (floatingItemBounds.width() * amountRight);
            outRect.bottom = (int) (floatingItemBounds.width() * amountRight);
        }

        if ((itemPosition < selectedDragItemPosition) && (view.getLeft() > middle.y)) {
            float amountLeft = ((float) view.getLeft() - middle.x) / (float) view.getWidth();
            if (amountLeft > 1) {
                amountLeft = 1;
            }

            outRect.right = (int) (floatingItemBounds.width() * amountLeft);
            outRect.left = -(int) (floatingItemBounds.width() * amountLeft);
        }
    }

    private void performVerticalEdgeScroll(PointF fingerPosition) {
        if (!edgeScrollingEnabled || orientation == LayoutOrientation.HORIZONTAL) {
            return;
        }

        float scrollAmount = 0;
        if (fingerPosition.y > (recyclerView.getHeight() * (1 - edgeDetectionThreshold))) {
            scrollAmount = (fingerPosition.y - (recyclerView.getHeight() * (1 - edgeDetectionThreshold)));
        } else if (fingerPosition.y < (recyclerView.getHeight() * edgeDetectionThreshold)) {
            scrollAmount = (fingerPosition.y - (recyclerView.getHeight() * edgeDetectionThreshold));
        }

        scrollAmount *= edgeScrollSpeed;

        recyclerView.scrollBy(0, (int) scrollAmount);
        recyclerView.invalidateItemDecorations();
    }

    private void performHorizontalEdgeScroll(PointF fingerPosition) {
        if (!edgeScrollingEnabled || orientation == LayoutOrientation.VERTICAL) {
            return;
        }

        float scrollAmount = 0;
        if (fingerPosition.x > (recyclerView.getWidth() * (1 - edgeDetectionThreshold))) {
            scrollAmount = (fingerPosition.x - (recyclerView.getWidth() * (1 - edgeDetectionThreshold)));
        } else if (fingerPosition.x < (recyclerView.getWidth() * edgeDetectionThreshold)) {
            scrollAmount = (fingerPosition.x - (recyclerView.getWidth() * edgeDetectionThreshold));
        }

        scrollAmount *= edgeScrollSpeed;

        recyclerView.scrollBy((int) scrollAmount, 0);
        recyclerView.invalidateItemDecorations();
    }

    private void updateVerticalBounds(PointF fingerPosition, PointF viewMiddle) {
        if (orientation == LayoutOrientation.HORIZONTAL) {
            return;
        }

        floatingItemBounds.top = (int)fingerPosition.y;
        if (fingerOffset != null) {
            floatingItemBounds.top += fingerOffset.y;
        }

        if (floatingItemBounds.top < -viewMiddle.y) {
            floatingItemBounds.top = -(int)viewMiddle.y;
        }

        floatingItemBounds.bottom = floatingItemBounds.top + floatingItemStartingBounds.height();
    }

    private void updateHorizontalBounds(PointF fingerPosition, PointF viewMiddle) {
        if (orientation == LayoutOrientation.VERTICAL) {
            return;
        }

        floatingItemBounds.left = (int)fingerPosition.x;
        if (fingerOffset != null) {
            floatingItemBounds.left += fingerOffset.x;
        }

        if (floatingItemBounds.left < -viewMiddle.x) {
            floatingItemBounds.left = -(int)viewMiddle.x;
        }

        floatingItemBounds.right = floatingItemBounds.left + floatingItemStartingBounds.width();
    }

    /**
     * Generates the Bitmap that will be used to represent the view being dragged across the screen
     *
     * @param view The view to create the drag bitmap from
     * @return The bitmap representing the drag view
     */
    private BitmapDrawable createDragBitmap(View view) {
        floatingItemStartingBounds = new Rect(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
        floatingItemBounds = new Rect(floatingItemStartingBounds);

        Bitmap bitmap = Bitmap.createBitmap(floatingItemStartingBounds.width(), floatingItemStartingBounds.height(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);

        BitmapDrawable retDrawable = new BitmapDrawable(view.getResources(), bitmap);
        retDrawable.setBounds(floatingItemBounds);

        return retDrawable;
    }
}
