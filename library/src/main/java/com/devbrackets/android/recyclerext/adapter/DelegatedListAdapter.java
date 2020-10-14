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
package com.devbrackets.android.recyclerext.adapter;

import android.view.ViewGroup;

import com.devbrackets.android.recyclerext.adapter.delegate.DelegateApi;
import com.devbrackets.android.recyclerext.adapter.delegate.DelegateCore;
import com.devbrackets.android.recyclerext.adapter.delegate.ViewHolderBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

/**
 * A {@link RecyclerView.Adapter} that handles delegating the creation and binding of
 * {@link RecyclerView.ViewHolder}s with {@link ViewHolderBinder}s
 * to allow for dynamic lists
 *
 * TODO: We still require the itemViewType... is there a different way to handle that so they don't have to extend the adapter? (otherwise this doesn't really help)
 */
@SuppressWarnings("WeakerAccess")
public abstract class DelegatedListAdapter<T> extends ListAdapter<RecyclerView.ViewHolder, T> implements DelegateApi<T> {

    @NonNull
    protected DelegateCore<RecyclerView.ViewHolder, T> delegateCore;

    public DelegatedListAdapter() {
        delegateCore = new DelegateCore<>(this, this);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return delegateCore.onCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        delegateCore.onBindViewHolder(holder, position);
    }

    /**
     * Registers the <code>binder</code> to handle creating and binding the views of type
     * <code>viewType</code>. If a {@link ViewHolderBinder} has already been specified
     * for the <code>viewType</code> then the value will be overwritten with <code>binder</code>
     *
     * @param viewType The type of view the {@link ViewHolderBinder} handles
     * @param binder The {@link ViewHolderBinder} to handle creating and binding views
     */
    public void registerViewHolderBinder(int viewType, ViewHolderBinder binder) {
        delegateCore.registerViewHolderBinder(viewType, binder);
    }

    /**
     * Registers the <code>binder</code> to handle creating and binding the views that aren't
     * handled by any binders registered with {@link #registerViewHolderBinder(int, ViewHolderBinder)}.
     * If a {@link ViewHolderBinder} has already been specified as the default then the value will be
     * overwritten with <code>binder</code>
     *
     * @param binder The {@link ViewHolderBinder} to handle creating and binding default views
     */
    public void registerDefaultViewHolderBinder(@Nullable ViewHolderBinder binder) {
        delegateCore.registerDefaultViewHolderBinder(binder);
    }
}
