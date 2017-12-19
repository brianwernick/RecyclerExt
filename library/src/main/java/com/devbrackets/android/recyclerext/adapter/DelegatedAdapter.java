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
import android.support.v4.util.SparseArrayCompat;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.devbrackets.android.recyclerext.adapter.delegate.ViewHolderBinder;

/**
 * A {@link RecyclerView.Adapter} that handles delegating the creation and binding of
 * {@link android.support.v7.widget.RecyclerView.ViewHolder}s with {@link ViewHolderBinder}s
 * to allow for dynamic lists
 */
@SuppressWarnings("WeakerAccess")
public abstract class DelegatedAdapter<VH extends RecyclerView.ViewHolder, T> extends ActionableAdapter<VH> {

    protected SparseArrayCompat<ViewHolderBinder<VH, T>> binders = new SparseArrayCompat<>();

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        return getBinderOrThrow(viewType).onCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(VH holder, int position) {
        getBinderOrThrow(getItemViewType(position)).onBindViewHolder(holder, getItem(position), position);
    }

    /**
     * Retrieves the item associated with the <code>position</code>
     *
     * @param position The position to get the item for
     * @return The item in the <code>position</code>
     */
    public abstract T getItem(int position);

    /**
     * Registers the <code>binder</code> to handle creating and binding the views of type
     * <code>viewType</code>. If a {@link ViewHolderBinder} has already been specified
     * for the <code>viewType</code> then the value will be overwritten with <code>binder</code>
     *
     * @param viewType The type of view the {@link ViewHolderBinder} handles
     * @param binder The {@link ViewHolderBinder} to handle creating and binding views
     */
    public void registerViewHolderBinder(int viewType, ViewHolderBinder<VH, T> binder) {
        binders.put(viewType, binder);
    }

    /**
     * Retrieves the {@link ViewHolderBinder} associated with the <code>viewType</code> or
     * throws an {@link IllegalStateException} informing the user that they forgot to register
     * a {@link ViewHolderBinder} that handles <code>viewType</code>
     *
     * @param viewType The type of the view to retrieve the {@link ViewHolderBinder} for
     * @return The {@link ViewHolderBinder} that handles the <code>viewType</code>
     */
    @NonNull
    protected ViewHolderBinder<VH, T> getBinderOrThrow(int viewType) {
        ViewHolderBinder<VH, T> binder = binders.get(viewType);
        if (binder == null) {
            throw new IllegalStateException("Unable to create or bind ViewHolders of viewType " + viewType + " because no ViewHolderBinder has been registered for that viewType");
        }

        return binder;
    }
}
