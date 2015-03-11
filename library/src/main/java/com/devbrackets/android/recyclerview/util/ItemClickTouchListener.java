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

package com.devbrackets.android.recyclerview.util;

import android.content.Context;
import android.os.Build;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnItemTouchListener;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;

/**
 *
 */
public abstract class ItemClickTouchListener implements OnItemTouchListener {
    private final GestureDetectorCompat gestureDetector;

    ItemClickTouchListener(RecyclerView hostView) {
        gestureDetector = new ItemClickGestureDetector(hostView.getContext(), new ItemClickGestureListener(hostView));
    }

    private boolean isAttachedToWindow(RecyclerView hostView) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return hostView.isAttachedToWindow();
        } else {
            return hostView.getHandler() != null;
        }
    }

    private boolean hasAdapter(RecyclerView hostView) {
        return hostView.getAdapter() != null;
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView recyclerView, MotionEvent event) {
        if (!isAttachedToWindow(recyclerView) || !hasAdapter(recyclerView)) {
            return false;
        }

        gestureDetector.onTouchEvent(event);
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView recyclerView, MotionEvent event) {
        // We can silently track tap and and long presses by silently
        // intercepting touch events in the host RecyclerView.
    }

    protected abstract boolean performItemClick(RecyclerView parent, View view, int position, long id);
    protected abstract boolean performItemLongClick(RecyclerView parent, View view, int position, long id);





    private class ItemClickGestureDetector extends GestureDetectorCompat {
        private final ItemClickGestureListener mGestureListener;

        public ItemClickGestureDetector(Context context, ItemClickGestureListener listener) {
            super(context, listener);
            mGestureListener = listener;
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            boolean handled = super.onTouchEvent(event);

            int action = event.getAction() & MotionEventCompat.ACTION_MASK;
            if (action == MotionEvent.ACTION_UP) {
                mGestureListener.dispatchSingleTapUpIfNeeded(event);
            }

            return handled;
        }
    }

    private class ItemClickGestureListener extends SimpleOnGestureListener {
        private final RecyclerView hostView;
        private View targetView;

        public ItemClickGestureListener(RecyclerView hostView) {
            this.hostView = hostView;
        }

        public void dispatchSingleTapUpIfNeeded(MotionEvent event) {
            // When the long press hook is called but the long press listener
            // returns false, the target child will be left around to be
            // handled later. In this case, we should still treat the gesture
            // as potential item click.
            if (targetView != null) {
                onSingleTapUp(event);
            }
        }

        @Override
        public boolean onDown(MotionEvent event) {
            int x = (int) event.getX();
            int y = (int) event.getY();

            targetView = hostView.findChildViewUnder(x, y);
            return targetView != null;
        }

        @Override
        public void onShowPress(MotionEvent event) {
            if (targetView != null) {
                targetView.setPressed(true);
            }
        }

        @Override
        public boolean onSingleTapUp(MotionEvent event) {
            boolean handled = false;

            if (targetView != null) {
                targetView.setPressed(false);

                int position = hostView.getChildPosition(targetView);
                long id = hostView.getAdapter().getItemId(position);
                handled = performItemClick(hostView, targetView, position, id);

                targetView = null;
            }

            return handled;
        }

        @Override
        public boolean onScroll(MotionEvent event, MotionEvent event2, float v, float v2) {
            if (targetView != null) {
                targetView.setPressed(false);
                targetView = null;

                return true;
            }

            return false;
        }

        @Override
        public void onLongPress(MotionEvent event) {
            if (targetView == null) {
                return;
            }

            int position = hostView.getChildPosition(targetView);
            long id = hostView.getAdapter().getItemId(position);
            boolean handled = performItemLongClick(hostView, targetView, position, id);

            if (handled) {
                targetView.setPressed(false);
                targetView = null;
            }
        }
    }
}