/*
 * Copyright (C) 2017 - 2018 Brian Wernick
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

package com.devbrackets.android.recyclerext.decoration.header;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;

/**
 * Handles intercepting touch events in the RecyclerView to handle interaction in the
 * sticky header.
 */
@SuppressWarnings("WeakerAccess")
public class StickyHeaderTouchInterceptor implements RecyclerView.OnItemTouchListener {
    public interface StickyHeaderCallback {
        @Nullable
        View getStickyView();
    }

    @NonNull
    protected StickyHeaderCallback stickyHeaderCallback;

    protected boolean allowInterception = true;
    protected boolean capturedTouchDown = false;

    public StickyHeaderTouchInterceptor(@NonNull StickyHeaderCallback stickyHeaderCallback) {
        this.stickyHeaderCallback = stickyHeaderCallback;
    }

    @Override
    public boolean onInterceptTouchEvent(@NonNull RecyclerView recyclerView, @NonNull MotionEvent event) {
        // Ignores touch events we don't want to intercept
        if (event.getAction() != MotionEvent.ACTION_DOWN && !capturedTouchDown) {
            return false;
        }

        View stickyView = getInterceptView();
        if (stickyView == null) {
            capturedTouchDown = false;
            return false;
        }

        // Makes sure to un-register capturing so that we don't accidentally interfere with scrolling
        if (event.getAction() == MotionEvent.ACTION_UP) {
            capturedTouchDown = false;
        }

        // Determine if the event is boxed by the view and pass the event through
        boolean bounded = (event.getX() >= stickyView.getX() && event.getX() <= stickyView.getX() + stickyView.getMeasuredWidth()) &&
                (event.getY() >= stickyView.getY() && event.getY() <= stickyView.getY() + stickyView.getMeasuredHeight());

        if (!bounded) {
            return false;
        }

        // Updates the filter
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            capturedTouchDown = true;
        }

        return dispatchChildTouchEvent(recyclerView, stickyView, event);
    }

    @Override
    public void onTouchEvent(@NonNull RecyclerView recyclerView, @NonNull MotionEvent event) {
        // Makes sure to un-register capturing so that we don't accidentally interfere with scrolling
        if (event.getAction() == MotionEvent.ACTION_UP) {
            capturedTouchDown = false;
        }

        View stickyView = getInterceptView();
        if (stickyView == null) {
            return;
        }

        dispatchChildTouchEvent(recyclerView, stickyView, event);
    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        allowInterception = !disallowIntercept;
    }

    @Nullable
    protected View getInterceptView() {
        if (!allowInterception) {
            return null;
        }

        return stickyHeaderCallback.getStickyView();
    }

    protected boolean dispatchChildTouchEvent(@NonNull RecyclerView recyclerView, @NonNull View child, @NonNull MotionEvent event) {
        //TODO: this doesn't work because the sticky ViewHolder hasn't been attached to a parent
        // And because the RecyclerView requires children to have a layoutPosition and we would have other issues
        // So we can attach it to the parent.getParent() however that depends on what layout the
        // RecyclerView is contained in (a relative layout or constraint layout would be fine, but a linear
        // layout, etc. wouldn't).
        // Other sticky header libraries allow just a single touch target for the entire header
        // so we have 2 options that I can see:
        // 1. Have a single touch target for the sticky header
        // 2. When sticky header touch is enabled we need to (includes animations, etc.)
        //      a. require the RV is inside a RelativeLayout
        //      b. require the user to pass in a view that can contain the sticky header
        // NOTE: if we do #2 then we don't need to perform an onDrawOver custom draw

        // Pass the event through
        boolean handledEvent = child.dispatchTouchEvent(event);
        if (handledEvent) {
            recyclerView.postInvalidate();
        }

        return handledEvent;
    }
}
