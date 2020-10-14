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
package com.devbrackets.android.recyclerext.adapter.header

import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver

/**
 * Used to monitor data set changes to update the [HeaderCore.headerData] so that we can correctly
 * calculate the list item count and header indexes.
 */
class HeaderAdapterDataObserver(protected var headerCore: HeaderCore, protected var headerApi: HeaderApi<*, *>) : AdapterDataObserver() {
    protected var generator: HeaderDataGenerator
    override fun onChanged() {
        generator.calculate(headerApi.headerData, headerApi)
    }

    override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
        // Only recalculates the headers if there was a change
        for (i in positionStart until positionStart + itemCount) {
            val headerItem = headerCore.getHeaderForAdapterPosition(i)
            val newHeaderId = headerApi.getHeaderId(headerApi.getChildPosition(i))
            if (headerItem != null && headerItem.id != newHeaderId) {
                generator.calculate(headerApi.headerData, headerApi)
                break
            }
        }
    }

    override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
        // Unlike the onItemRangeChanged insertions cause a few things that need to be handled
        //     1. HeaderItem indexes below the insertion point need to be updated
        //     2. If added to an existing header, the item count needs to be updated
        //     3. If creating a new header, the HeaderItem(s) need to be created and inserted
        // This is made more complex because insertions can occur at the start, middle, or end
        // of an existing region (header children). Due to this we will just recalculate the
        // headers on insertions for now
        generator.calculate(headerApi.headerData, headerApi)
    }

    override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
        // Unlike the onItemRangeChanged deletions cause a few things that need to be handled
        //     1. HeaderItem indexes below the deletion point need to be updated
        //     2. If removed from an existing header, the item count needs to be updated or the header removed
        // This is made more complex because deletions can occur at the start, middle, or end
        // of an existing region (header children). Due to this we will just recalculate the
        // headers on deletions for now
        generator.calculate(headerApi.headerData, headerApi)
    }

    override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
        generator.calculate(headerApi.headerData, headerApi)
    }

    init {
        generator = HeaderDataGenerator(headerApi)
    }
}