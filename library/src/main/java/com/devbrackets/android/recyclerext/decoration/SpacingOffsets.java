package com.devbrackets.android.recyclerext.decoration;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;

import com.devbrackets.android.recyclerext.layout.BaseLayoutManager;
import com.devbrackets.android.recyclerext.layout.Lanes;
import com.devbrackets.android.recyclerext.layout.LayoutDirection;
import com.devbrackets.android.recyclerext.layout.SpannableGridLayoutManager;
import com.devbrackets.android.recyclerext.layout.StaggeredGridLayoutManager;

/**
 *
 */
class SpacingOffsets {
    private final int verticalSpacing;
    private final int horizontalSpacing;

    private boolean addEndSpacing;

    private final Lanes.LaneInfo tempLaneInfo = new Lanes.LaneInfo();

    public SpacingOffsets(int verticalSpacing, int horizontalSpacing) {
        if (verticalSpacing < 0 || horizontalSpacing < 0) {
            throw new IllegalArgumentException("Spacings should be equal or greater than 0");
        }

        this.verticalSpacing = verticalSpacing;
        this.horizontalSpacing = horizontalSpacing;
    }

    /**
     * Checks whether the given position is placed just after the item in the
     * first lane of the layout taking items spans into account.
     */
    private boolean isSecondLane(BaseLayoutManager layoutManager, int itemPosition, int lane) {
        if (lane == 0 || itemPosition == 0) {
            return false;
        }

        int previousLane = Lanes.NO_LANE;
        int previousPosition = itemPosition - 1;
        while (previousPosition >= 0) {
            layoutManager.getLaneForPosition(tempLaneInfo, previousPosition, LayoutDirection.END);
            previousLane = tempLaneInfo.startLane;
            if (previousLane != lane) {
                break;
            }

            previousPosition--;
        }

        final int previousLaneSpan = layoutManager.getLaneSpanForPosition(previousPosition);
        return previousLane == 0 && (lane == previousLane + previousLaneSpan);
    }

    /**
     * Checks whether the given position is placed at the start of a layout lane.
     */
    private static boolean isFirstChildInLane(BaseLayoutManager lm, int itemPosition) {
        int laneCount = lm.getLanes().getCount();
        if (itemPosition >= laneCount) {
            return false;
        }

        int count = 0;
        for (int i = 0; i < itemPosition; i++) {
            count += lm.getLaneSpanForPosition(i);
            if (count >= laneCount) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks whether the given position is placed at the end of a layout lane.
     */
    private static boolean isLastChildInLane(BaseLayoutManager lm, int itemPosition, int itemCount) {
        int laneCount = lm.getLanes().getCount();
        if (itemPosition < itemCount - laneCount) {
            return false;
        }

        // TODO: Figure out a robust way to compute this for layouts
        // that are dynamically placed and might span multiple lanes.
        return !(lm instanceof SpannableGridLayoutManager || lm instanceof StaggeredGridLayoutManager);
    }

    public void setAddSpacingAtEnd(boolean spacingAtEnd) {
        addEndSpacing = spacingAtEnd;
    }

    /**
     * Computes the offsets based on the vertical and horizontal spacing values.
     * The spacing computation has to ensure that the lane sizes are the same after
     * applying the offsets. This means we have to shift the spacing unevenly across
     * items depending on their position in the layout.
     */
    public void getItemOffsets(Rect outRect, int itemPosition, RecyclerView parent) {
        BaseLayoutManager layoutManager = (BaseLayoutManager) parent.getLayoutManager();

        layoutManager.getLaneForPosition(tempLaneInfo, itemPosition, LayoutDirection.END);
        int lane = tempLaneInfo.startLane;
        int laneSpan = layoutManager.getLaneSpanForPosition(itemPosition);
        int laneCount = layoutManager.getLanes().getCount();
        int itemCount = parent.getAdapter().getItemCount();

        boolean isVertical = layoutManager.isVertical();

        boolean firstLane = (lane == 0);
        boolean secondLane = isSecondLane(layoutManager, itemPosition, lane);

        boolean lastLane = (lane + laneSpan == laneCount);
        boolean beforeLastLane = (lane + laneSpan == laneCount - 1);

        int laneSpacing = (isVertical ? horizontalSpacing : verticalSpacing);

        int laneOffsetStart;
        int laneOffsetEnd;

        if (firstLane) {
            laneOffsetStart = 0;
        } else if (lastLane && !secondLane) {
            laneOffsetStart = (int) (laneSpacing * 0.75);
        } else if (secondLane && !lastLane) {
            laneOffsetStart = (int) (laneSpacing * 0.25);
        } else {
            laneOffsetStart = (int) (laneSpacing * 0.5);
        }

        if (lastLane) {
            laneOffsetEnd = 0;
        } else if (firstLane && !beforeLastLane) {
            laneOffsetEnd = (int) (laneSpacing * 0.75);
        } else if (beforeLastLane && !firstLane) {
            laneOffsetEnd = (int) (laneSpacing * 0.25);
        } else {
            laneOffsetEnd = (int) (laneSpacing * 0.5);
        }

        boolean isFirstInLane = isFirstChildInLane(layoutManager, itemPosition);
        boolean isLastInLane = !addEndSpacing && isLastChildInLane(layoutManager, itemPosition, itemCount);

        if (isVertical) {
            outRect.left = laneOffsetStart;
            outRect.top = (isFirstInLane ? 0 : verticalSpacing / 2);
            outRect.right = laneOffsetEnd;
            outRect.bottom = (isLastInLane ? 0 : verticalSpacing / 2);
        } else {
            outRect.left = (isFirstInLane ? 0 : horizontalSpacing / 2);
            outRect.top = laneOffsetStart;
            outRect.right = (isLastInLane ? 0 : horizontalSpacing / 2);
            outRect.bottom = laneOffsetEnd;
        }
    }
}
