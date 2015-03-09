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


import android.support.v7.widget.RecyclerView;
import android.view.HapticFeedbackConstants;
import android.view.SoundEffectConstants;
import android.view.View;

import com.devbrackets.android.recyclerview.R;
import com.devbrackets.android.recyclerview.listener.OnItemClickListener;
import com.devbrackets.android.recyclerview.listener.OnItemLongClickListener;

/**
 *
 */
public class ItemClickSupport {
    private final RecyclerView recyclerView;
    private final TouchListener touchListener;

    private OnItemClickListener itemClickListener;
    private OnItemLongClickListener itemLongClickListener;

    private ItemClickSupport(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;

        touchListener = new TouchListener(recyclerView);
        recyclerView.addOnItemTouchListener(touchListener);
    }

    /**
     * Register a callback to be invoked when an item in the
     * RecyclerView has been clicked.
     *
     * @param listener The callback that will be invoked.
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        itemClickListener = listener;
    }

    /**
     * Register a callback to be invoked when an item in the
     * RecyclerView has been clicked and held.
     *
     * @param listener The callback that will be invoked.
     */
    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        if (!recyclerView.isLongClickable()) {
            recyclerView.setLongClickable(true);
        }

        itemLongClickListener = listener;
    }

    public static ItemClickSupport addTo(RecyclerView recyclerView) {
        ItemClickSupport itemClickSupport = from(recyclerView);
        if (itemClickSupport == null) {
            itemClickSupport = new ItemClickSupport(recyclerView);
            recyclerView.setTag(R.id.recyclerExt_item_click_support, itemClickSupport);
        }

        return itemClickSupport;
    }

    public static void removeFrom(RecyclerView recyclerView) {
        ItemClickSupport itemClickSupport = from(recyclerView);
        if (itemClickSupport == null) {
            return;
        }

        recyclerView.removeOnItemTouchListener(itemClickSupport.touchListener);
        recyclerView.setTag(R.id.recyclerExt_item_click_support, null);
    }

    public static ItemClickSupport from(RecyclerView recyclerView) {
        if (recyclerView == null) {
            return null;
        }

        return (ItemClickSupport) recyclerView.getTag(R.id.recyclerExt_item_click_support);
    }





    private class TouchListener extends ItemClickTouchListener {
        TouchListener(RecyclerView recyclerView) {
            super(recyclerView);
        }

        @Override
        protected boolean performItemClick(RecyclerView parent, View view, int position, long id) {
            if (itemClickListener != null) {
                view.playSoundEffect(SoundEffectConstants.CLICK);
                itemClickListener.onItemClick(parent, view, position, id);
                return true;
            }

            return false;
        }

        @Override
        protected boolean performItemLongClick(RecyclerView parent, View view, int position, long id) {
            if (itemLongClickListener != null) {
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                return itemLongClickListener.onItemLongClick(parent, view, position, id);
            }

            return false;
        }
    }
}