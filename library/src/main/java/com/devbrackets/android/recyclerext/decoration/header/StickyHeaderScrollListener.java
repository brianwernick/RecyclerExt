/*
 * Copyright (C) 2018 Brian Wernick
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

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Listens to the scroll events for the RecyclerView that will have
 * sticky headers.  When a new header reaches the start it will be
 * transformed in to a sticky view and attached to the start of the
 * RecyclerView.  Additionally, when a new header is reaching the
 * start, the headers will be transitioned smoothly
 */
@SuppressWarnings("WeakerAccess")
public class StickyHeaderScrollListener extends RecyclerView.OnScrollListener {
    @NonNull
    protected UpdateListener updateListener;

    public StickyHeaderScrollListener(@NonNull UpdateListener updateListener) {
        this.updateListener = updateListener;
    }

    @Override
    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        updateListener.onUpdateStickyHeader();
    }
}
