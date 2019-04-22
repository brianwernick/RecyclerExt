/*
 * Copyright (C) 2016 - 2018 Brian Wernick
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

import android.graphics.PointF;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

import com.devbrackets.android.recyclerext.adapter.header.HeaderApi;

@SuppressWarnings("WeakerAccess")
public class StickyHeader {
    @Nullable
    protected RecyclerView.ViewHolder stickyViewHolder;
    @Nullable
    protected View cachedStickyView;
    @NonNull
    public PointF stickyViewOffset = new PointF(0, 0);
    public long currentStickyId = RecyclerView.NO_ID;

    public void reset() {
        update(RecyclerView.NO_ID, null);

        stickyViewOffset.x = 0;
        stickyViewOffset.y = 0;
    }

    public void update(long stickyId, @Nullable RecyclerView.ViewHolder holder) {
        stickyViewHolder = holder;
        cachedStickyView = null;
        currentStickyId = stickyId;
    }

    @Nullable
    public View getStickyView(@NonNull HeaderApi headerApi) {
        if (cachedStickyView != null) {
            return cachedStickyView;
        }

        RecyclerView.ViewHolder holder = stickyViewHolder;
        if (holder == null) {
            return null;
        }

        // If we have a ViewHolder we should have a view, but just to be safe we check
        int stickyViewId = headerApi.getCustomStickyHeaderViewId();
        cachedStickyView = stickyViewId != 0 ? holder.itemView.findViewById(stickyViewId) : holder.itemView;
        return cachedStickyView;
    }
}
