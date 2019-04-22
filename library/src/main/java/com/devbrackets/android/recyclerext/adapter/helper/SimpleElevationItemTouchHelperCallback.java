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

package com.devbrackets.android.recyclerext.adapter.helper;

import android.graphics.Canvas;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.view.View;

/**
 * Extends the {@link androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback} to provide
 * support for specifying the elevation to use when an item is active (being dragged or swiped)
 */
@SuppressWarnings("unused")
public abstract class SimpleElevationItemTouchHelperCallback extends ItemTouchHelper.SimpleCallback {
    public static final float DEFAULT_ACTIVE_ELEVATION_CHANGE = 1f; //NOTE: the support library implementation uses 1f as the default

    protected boolean isElevated = false;
    protected float originalElevation = 0;
    protected float activeElevationChange = DEFAULT_ACTIVE_ELEVATION_CHANGE;

    /**
     * Creates a Callback for the given drag and swipe allowance. These values serve as
     * defaults and if you want to customize behavior per ViewHolder, you can override
     * {@link #getSwipeDirs(RecyclerView, RecyclerView.ViewHolder)}
     * and / or {@link #getDragDirs(RecyclerView, RecyclerView.ViewHolder)}.
     *
     * @param dragDirs  Binary OR of direction flags in which the Views can be dragged. Must be composed of
     *                  {@link ItemTouchHelper#LEFT}, {@link ItemTouchHelper#RIGHT},
     *                  {@link ItemTouchHelper#START}, {@link ItemTouchHelper#END},
     *                  {@link ItemTouchHelper#UP} and {@link ItemTouchHelper#DOWN}
     * @param swipeDirs Binary OR of direction flags in which the Views can be swiped. Must be composed of
     *                  {@link ItemTouchHelper#LEFT}, {@link ItemTouchHelper#RIGHT},
     *                  {@link ItemTouchHelper#START}, {@link ItemTouchHelper#END},
     *                  {@link ItemTouchHelper#UP} and {@link ItemTouchHelper#DOWN}
     */
    public SimpleElevationItemTouchHelperCallback(int dragDirs, int swipeDirs) {
        this(dragDirs, swipeDirs, DEFAULT_ACTIVE_ELEVATION_CHANGE);
    }

    /**
     * Creates a Callback for the given drag and swipe allowance. These values serve as
     * defaults and if you want to customize behavior per ViewHolder, you can override
     * {@link #getSwipeDirs(RecyclerView, RecyclerView.ViewHolder)}
     * and / or {@link #getDragDirs(RecyclerView, RecyclerView.ViewHolder)}.
     *
     * @param dragDirs  Binary OR of direction flags in which the Views can be dragged. Must be composed of
     *                  {@link ItemTouchHelper#LEFT}, {@link ItemTouchHelper#RIGHT},
     *                  {@link ItemTouchHelper#START}, {@link ItemTouchHelper#END},
     *                  {@link ItemTouchHelper#UP} and {@link ItemTouchHelper#DOWN}
     * @param swipeDirs Binary OR of direction flags in which the Views can be swiped. Must be composed of
     *                  {@link ItemTouchHelper#LEFT}, {@link ItemTouchHelper#RIGHT},
     *                  {@link ItemTouchHelper#START}, {@link ItemTouchHelper#END},
     *                  {@link ItemTouchHelper#UP} and {@link ItemTouchHelper#DOWN}
     * @param activeElevationChange The elevation change to use when an item becomes active
     */
    public SimpleElevationItemTouchHelperCallback(int dragDirs, int swipeDirs, float activeElevationChange) {
        super(dragDirs, swipeDirs);
        this.activeElevationChange = activeElevationChange;
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            return;
        }

        //To avoid elevation conflicts with the Lollipop+ implementation, we will always inform the super that we aren't active
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, false);
        if (isCurrentlyActive && !isElevated ) {
            updateElevation(recyclerView, viewHolder, true);
        }
    }

    @Override
    public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);
        updateElevation(recyclerView, viewHolder, false);
    }

    /**
     * Updates the elevation for the specified <code>holder</code> by either increasing
     * or decreasing by the specified amount
     *
     * @param recyclerView The recyclerView to use when calculating the new elevation
     * @param holder The ViewHolder to increase or decrease the elevation for
     * @param elevate True if the <code>holder</code> should have it's elevation increased
     */
    protected void updateElevation(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder holder, boolean elevate) {
        if (elevate) {
            originalElevation = ViewCompat.getElevation(holder.itemView);
            float newElevation = activeElevationChange + findMaxElevation(recyclerView);
            ViewCompat.setElevation(holder.itemView, newElevation);
            isElevated = true;
        } else {
            ViewCompat.setElevation(holder.itemView, originalElevation);
            originalElevation = 0;
            isElevated = false;
        }
    }

    /**
     * Finds the elevation of the highest visible viewHolder to make sure the elevated view
     * from {@link #updateElevation(RecyclerView, RecyclerView.ViewHolder, boolean)} is above
     * all others.
     *
     * @param recyclerView The RecyclerView to use when determining the height of all the visible ViewHolders
     */
    protected float findMaxElevation(@NonNull RecyclerView recyclerView) {
        float maxChildElevation = 0;

        for (int i = 0; i < recyclerView.getChildCount(); i++) {
            View child = recyclerView.getChildAt(i);
            float elevation = ViewCompat.getElevation(child);

            if (elevation > maxChildElevation) {
                maxChildElevation = elevation;
            }
        }

        return maxChildElevation;
    }
}
