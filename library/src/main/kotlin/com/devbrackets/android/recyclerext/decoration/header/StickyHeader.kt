/*
 * Copyright (C) 2016 - 2020 Brian Wernick
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

import android.graphics.PointF
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.devbrackets.android.recyclerext.adapter.header.HeaderApi

class StickyHeader {
    protected var stickyViewHolder: RecyclerView.ViewHolder? = null
    protected var cachedStickyView: View? = null

    var stickyViewOffset = PointF(0F, 0F)
    var currentStickyId = RecyclerView.NO_ID

    fun reset() {
        update(RecyclerView.NO_ID, null)
        stickyViewOffset.x = 0f
        stickyViewOffset.y = 0f
    }

    fun update(stickyId: Long, holder: RecyclerView.ViewHolder?) {
        stickyViewHolder = holder
        cachedStickyView = null
        currentStickyId = stickyId
    }

    fun getStickyView(headerApi: HeaderApi<*, *>): View? {
        if (cachedStickyView != null) {
            return cachedStickyView
        }

        val holder = stickyViewHolder ?: return null

        // If we have a ViewHolder we should have a view, but just to be safe we check
        val stickyViewId = headerApi.customStickyHeaderViewId
        cachedStickyView = if (stickyViewId != 0) holder.itemView.findViewById(stickyViewId) else holder.itemView
        return cachedStickyView
    }
}