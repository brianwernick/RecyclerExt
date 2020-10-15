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
package com.devbrackets.android.recyclerext.decoration.header

import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnItemTouchListener

/**
 * Handles intercepting touch events in the RecyclerView to handle interaction in the
 * sticky header.
 */
class StickyHeaderTouchInterceptor(protected var stickyHeaderCallback: StickyHeaderCallback) : OnItemTouchListener {
    interface StickyHeaderCallback {
        val stickyView: View?
    }

    protected var allowInterception = true
    protected var capturedTouchDown = false

    protected val interceptView: View?
        get() = if (!allowInterception) {
            null
        } else stickyHeaderCallback.stickyView

    override fun onInterceptTouchEvent(recyclerView: RecyclerView, event: MotionEvent): Boolean {
        // Ignores touch events we don't want to intercept
        if (event.action != MotionEvent.ACTION_DOWN && !capturedTouchDown) {
            return false
        }

        val stickyView = interceptView
        if (stickyView == null) {
            capturedTouchDown = false
            return false
        }

        // Makes sure to un-register capturing so that we don't accidentally interfere with scrolling
        if (event.action == MotionEvent.ACTION_UP) {
            capturedTouchDown = false
        }

        // Determine if the event is boxed by the view and pass the event through
        val bounded = event.x >= stickyView.x &&
                event.x <= stickyView.x + stickyView.measuredWidth &&
                event.y >= stickyView.y &&
                event.y <= stickyView.y + stickyView.measuredHeight
        if (!bounded) {
            return false
        }

        // Updates the filter
        if (event.action == MotionEvent.ACTION_DOWN) {
            capturedTouchDown = true
        }

        return dispatchChildTouchEvent(recyclerView, stickyView, event)
    }

    override fun onTouchEvent(recyclerView: RecyclerView, event: MotionEvent) {
        // Makes sure to un-register capturing so that we don't accidentally interfere with scrolling
        if (event.action == MotionEvent.ACTION_UP) {
            capturedTouchDown = false
        }

        val stickyView = interceptView ?: return
        dispatchChildTouchEvent(recyclerView, stickyView, event)
    }

    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
        allowInterception = !disallowIntercept
    }

    protected fun dispatchChildTouchEvent(recyclerView: RecyclerView, child: View, event: MotionEvent): Boolean {
        // Pass the event through
        val handledEvent = child.dispatchTouchEvent(event)
        if (handledEvent) {
            recyclerView.postInvalidate()
        }

        return handledEvent
    }
}