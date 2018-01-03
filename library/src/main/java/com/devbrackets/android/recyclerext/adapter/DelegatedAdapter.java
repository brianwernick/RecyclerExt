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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.devbrackets.android.recyclerext.adapter.delegate.DelegateApi;
import com.devbrackets.android.recyclerext.adapter.delegate.DelegateCore;
import com.devbrackets.android.recyclerext.adapter.delegate.ViewHolderBinder;

/**
 * A {@link RecyclerView.Adapter} that handles delegating the creation and binding of
 * {@link android.support.v7.widget.RecyclerView.ViewHolder}s with {@link ViewHolderBinder}s
 * to allow for dynamic lists
 *
 * TODO: how do we handle the unregister when the RV goes away?
 */
@SuppressWarnings("WeakerAccess")
public abstract class DelegatedAdapter<T> extends ActionableAdapter<RecyclerView.ViewHolder> implements DelegateApi<T> {

    @NonNull
    protected DelegateCore<RecyclerView.ViewHolder, T> delegateCore;

    public DelegatedAdapter() {
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

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder holder) {
        delegateCore.onViewRecycled(holder);
    }

    @Override
    public boolean onFailedToRecycleView(RecyclerView.ViewHolder holder) {
        return delegateCore.onFailedToRecycleView(holder);
    }

    @Override
    public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
        delegateCore.onViewAttachedToWindow(holder);
    }

    @Override
    public void onViewDetachedFromWindow(RecyclerView.ViewHolder holder) {
        delegateCore.onViewDetachedFromWindow(holder);
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
