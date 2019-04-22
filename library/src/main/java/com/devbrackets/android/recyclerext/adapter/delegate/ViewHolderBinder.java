/*
 * Copyright (C) 2017 Brian Wernick
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

package com.devbrackets.android.recyclerext.adapter.delegate;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.ViewGroup;

import com.devbrackets.android.recyclerext.adapter.DelegatedAdapter;

/**
 * A delegated handler for the {@link RecyclerView.ViewHolder}s
 * that the {@link DelegatedAdapter} uses to create and bind each view type
 */
public abstract class ViewHolderBinder<VH extends RecyclerView.ViewHolder, T> {
    @NonNull
    public abstract VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType);
    public abstract void onBindViewHolder(@NonNull VH holder, @NonNull T item, int position);

    public void onAttachedToAdapter(@NonNull RecyclerView.Adapter<VH> adapter) {
        // Purposefully left blank
    }

    public void onDetachedFromAdapter(@NonNull RecyclerView.Adapter<VH> adapter) {
        // Purposefully left blank
    }

    public void onViewRecycled(VH holder) {
        // Purposefully left blank
    }

    public boolean onFailedToRecycleView(VH holder) {
        return false;
    }

    public void onViewAttachedToWindow(VH holder) {
        // Purposefully left blank
    }

    public void onViewDetachedFromWindow(VH holder) {
        // Purposefully left blank
    }
}
