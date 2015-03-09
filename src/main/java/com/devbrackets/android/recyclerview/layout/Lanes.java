/*
 * Copyright (C) 2014 Lucas Rocha (TwoWayView)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.devbrackets.android.recyclerview.layout;

import android.graphics.Rect;

/**
 *
 */
public class Lanes {
    public static final int NO_LANE = -1;

    private final boolean isVertical;
    private final Rect[] lanes;
    private final Rect[] savedLanes;
    private final int laneSize;

    private final Rect tempRect = new Rect();
    private final LaneInfo tempLaneInfo = new LaneInfo();

    private Integer innerStart;
    private Integer innerEnd;

    public static class LaneInfo {
        public int startLane;
        public int anchorLane;

        public boolean isUndefined() {
            return (startLane == NO_LANE || anchorLane == NO_LANE);
        }

        public void set(int startLane, int anchorLane) {
            this.startLane = startLane;
            this.anchorLane = anchorLane;
        }

        public void setUndefined() {
            startLane = NO_LANE;
            anchorLane = NO_LANE;
        }
    }

    public Lanes(LayoutOrientation orientation, Rect[] lanes, int laneSize) {
        isVertical = (orientation == LayoutOrientation.VERTICAL);
        this.lanes = lanes;
        this.laneSize = laneSize;

        savedLanes = new Rect[this.lanes.length];
        for (int i = 0; i < this.lanes.length; i++) {
            savedLanes[i] = new Rect();
        }
    }

    public Lanes(BaseLayoutManager layout, int laneCount) {
        isVertical = layout.isVertical();

        lanes = new Rect[laneCount];
        savedLanes = new Rect[laneCount];
        for (int i = 0; i < laneCount; i++) {
            lanes[i] = new Rect();
            savedLanes[i] = new Rect();
        }

        laneSize = calculateLaneSize(layout, laneCount);

        int paddingLeft = layout.getPaddingLeft();
        int paddingTop = layout.getPaddingTop();

        for (int i = 0; i < laneCount; i++) {
            int laneStart = i * laneSize;

            int l = paddingLeft + (isVertical ? laneStart : 0);
            int t = paddingTop + (isVertical ? 0 : laneStart);
            int r = (isVertical ? l + laneSize : l);
            int b = (isVertical ? t : t + laneSize);

            lanes[i].set(l, t, r, b);
        }
    }

    public static int calculateLaneSize(BaseLayoutManager layout, int laneCount) {
        if (layout.isVertical()) {
            int paddingLeft = layout.getPaddingLeft();
            int paddingRight = layout.getPaddingRight();
            int width = layout.getWidth() - paddingLeft - paddingRight;
            return width / laneCount;
        } else {
            int paddingTop = layout.getPaddingTop();
            int paddingBottom = layout.getPaddingBottom();
            int height = layout.getHeight() - paddingTop - paddingBottom;
            return height / laneCount;
        }
    }

    private void invalidateEdges() {
        innerStart = null;
        innerEnd = null;
    }

    public LayoutOrientation getOrientation() {
        return (isVertical ? LayoutOrientation.VERTICAL : LayoutOrientation.HORIZONTAL);
    }

    public void save() {
        for (int i = 0; i < lanes.length; i++) {
            savedLanes[i].set(lanes[i]);
        }
    }

    public void restore() {
        for (int i = 0; i < lanes.length; i++) {
            lanes[i].set(savedLanes[i]);
        }
    }

    public int getLaneSize() {
        return laneSize;
    }

    public int getCount() {
        return lanes.length;
    }

    private void offsetLane(int lane, int offset) {
        lanes[lane].offset(isVertical ? 0 : offset, isVertical ? offset : 0);
    }

    public void offset(int offset) {
        for (int i = 0; i < lanes.length; i++) {
            offset(i, offset);
        }

        invalidateEdges();
    }

    public void offset(int lane, int offset) {
        offsetLane(lane, offset);
        invalidateEdges();
    }

    public void getLane(int lane, Rect laneRect) {
        laneRect.set(lanes[lane]);
    }

    public int pushChildFrame(Rect outRect, int lane, int margin, LayoutDirection direction) {
        int delta;

        Rect laneRect = lanes[lane];
        if (isVertical) {
            if (direction == LayoutDirection.END) {
                delta = outRect.top - laneRect.bottom;
                laneRect.bottom = outRect.bottom + margin;
            } else {
                delta = outRect.bottom - laneRect.top;
                laneRect.top = outRect.top - margin;
            }
        } else {
            if (direction == LayoutDirection.END) {
                delta = outRect.left - laneRect.right;
                laneRect.right = outRect.right + margin;
            } else {
                delta = outRect.right - laneRect.left;
                laneRect.left = outRect.left - margin;
            }
        }

        invalidateEdges();

        return delta;
    }

    public void popChildFrame(Rect outRect, int lane, int margin, LayoutDirection direction) {
        Rect laneRect = lanes[lane];
        if (isVertical) {
            if (direction == LayoutDirection.END) {
                laneRect.top = outRect.bottom - margin;
            } else {
                laneRect.bottom = outRect.top + margin;
            }
        } else {
            if (direction == LayoutDirection.END) {
                laneRect.left = outRect.right - margin;
            } else {
                laneRect.right = outRect.left + margin;
            }
        }

        invalidateEdges();
    }

    public void getChildFrame(Rect outRect, int childWidth, int childHeight, LaneInfo laneInfo, LayoutDirection direction) {
        Rect startRect = lanes[laneInfo.startLane];

        // The anchor lane only applies when we're get child frame in the direction
        // of the forward scroll. We'll need to rethink this once we start working on
        // RTL support.
        int anchorLane = direction == LayoutDirection.END ? laneInfo.anchorLane : laneInfo.startLane;
        Rect anchorRect = lanes[anchorLane];

        if (isVertical) {
            outRect.left = startRect.left;
            outRect.top = direction == LayoutDirection.END ? anchorRect.bottom : anchorRect.top - childHeight;
        } else {
            outRect.top = startRect.top;
            outRect.left = direction == LayoutDirection.END ? anchorRect.right : anchorRect.left - childWidth;
        }

        outRect.right = outRect.left + childWidth;
        outRect.bottom = outRect.top + childHeight;
    }

    private boolean intersects(int start, int count, Rect r) {
        for (int l = start; l < start + count; l++) {
            if (Rect.intersects(lanes[l], r)) {
                return true;
            }
        }

        return false;
    }

    private int findLaneThatFitsSpan(int anchorLane, int laneSpan, LayoutDirection direction) {
        int findStart = Math.max(0, anchorLane - laneSpan + 1);
        int findEnd = Math.min(findStart + laneSpan, lanes.length - laneSpan + 1);
        for (int l = findStart; l < findEnd; l++) {
            tempLaneInfo.set(l, anchorLane);

            getChildFrame(tempRect, isVertical ? laneSpan * laneSize : 1, isVertical ? 1 : laneSpan * laneSize, tempLaneInfo, direction);

            if (!intersects(l, laneSpan, tempRect)) {
                return l;
            }
        }

        return Lanes.NO_LANE;
    }

    public void findLane(LaneInfo outInfo, int laneSpan, LayoutDirection direction) {
        outInfo.setUndefined();

        int targetEdge = (direction == LayoutDirection.END ? Integer.MAX_VALUE : Integer.MIN_VALUE);
        for (int l = 0; l < lanes.length; l++) {
            int laneEdge;
            if (isVertical) {
                laneEdge = (direction == LayoutDirection.END ? lanes[l].bottom : lanes[l].top);
            } else {
                laneEdge = (direction == LayoutDirection.END ? lanes[l].right : lanes[l].left);
            }

            if ((direction == LayoutDirection.END && laneEdge < targetEdge) || (direction == LayoutDirection.START && laneEdge > targetEdge)) {
                int targetLane = findLaneThatFitsSpan(l, laneSpan, direction);
                if (targetLane != NO_LANE) {
                    targetEdge = laneEdge;
                    outInfo.set(targetLane, l);
                }
            }
        }
    }

    public void reset(LayoutDirection direction) {
        for (Rect laneRect : lanes) {
            if (isVertical) {
                if (direction == LayoutDirection.START) {
                    laneRect.bottom = laneRect.top;
                } else {
                    laneRect.top = laneRect.bottom;
                }
            } else {
                if (direction == LayoutDirection.START) {
                    laneRect.right = laneRect.left;
                } else {
                    laneRect.left = laneRect.right;
                }
            }
        }

        invalidateEdges();
    }

    public void reset(int offset) {
        for (Rect laneRect : lanes) {
            laneRect.offsetTo(isVertical ? laneRect.left : offset,
                    isVertical ? offset : laneRect.top);

            if (isVertical) {
                laneRect.bottom = laneRect.top;
            } else {
                laneRect.right = laneRect.left;
            }
        }

        invalidateEdges();
    }

    public int getInnerStart() {
        if (innerStart != null) {
            return innerStart;
        }

        innerStart = Integer.MIN_VALUE;
        for (Rect laneRect : lanes) {
            innerStart = Math.max(innerStart, isVertical ? laneRect.top : laneRect.left);
        }

        return innerStart;
    }

    public int getInnerEnd() {
        if (innerEnd != null) {
            return innerEnd;
        }

        innerEnd = Integer.MAX_VALUE;
        for (Rect laneRect : lanes) {
            innerEnd = Math.min(innerEnd, isVertical ? laneRect.bottom : laneRect.right);
        }

        return innerEnd;
    }
}
