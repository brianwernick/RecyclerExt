/*
 * Copyright (C) 2017 - 2018 Brian Wernick
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
import com.devbrackets.android.recyclerext.adapter.header.HeaderApi;

/**
 * A {@link RecyclerView.Adapter} that handles delegating the creation and binding of
 * {@link android.support.v7.widget.RecyclerView.ViewHolder}s with {@link ViewHolderBinder}s
 * to allow for dynamic lists
 */
public abstract class DelegatedHeaderAdapter<T> extends HeaderAdapter<RecyclerView.ViewHolder, RecyclerView.ViewHolder> implements DelegateApi<T> {

    @NonNull
    protected DelegateCore<RecyclerView.ViewHolder, T> headerDelegateCore;
    @NonNull
    protected DelegateCore<RecyclerView.ViewHolder, T> childDelegateCore;

    public DelegatedHeaderAdapter() {
        headerDelegateCore = new DelegateCore<>(new DelegateApi<T>() {
            @Override
            public T getItem(int position) {
                return DelegatedHeaderAdapter.this.getItem(position);
            }

            @Override
            public int getItemViewType(int position) {
                return getHeaderViewType(getChildPosition(position));
            }
        }, this);

        childDelegateCore = new DelegateCore<>(new DelegateApi<T>() {
            @Override
            public T getItem(int position) {
                return DelegatedHeaderAdapter.this.getItem(position);
            }

            @Override
            public int getItemViewType(int position) {
                return getChildViewType(getChildPosition(position));
            }
        }, this);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateHeaderViewHolder(@NonNull ViewGroup parent, int viewType) {
        return headerDelegateCore.onCreateViewHolder(parent, viewType);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateChildViewHolder(@NonNull ViewGroup parent, int viewType) {
        return childDelegateCore.onCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindHeaderViewHolder(@NonNull RecyclerView.ViewHolder holder, int firstChildPosition) {
        headerDelegateCore.onBindViewHolder(holder, firstChildPosition);
    }

    @Override
    public void onBindChildViewHolder(@NonNull RecyclerView.ViewHolder holder, int childPosition) {
        childDelegateCore.onBindViewHolder(holder, childPosition);
    }

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder holder) {
        DelegateCore<RecyclerView.ViewHolder, T> delegateCore = core.isHeader(holder.getAdapterPosition()) ? headerDelegateCore : childDelegateCore;
        delegateCore.onViewRecycled(holder);
    }

    @Override
    public boolean onFailedToRecycleView(RecyclerView.ViewHolder holder) {
        DelegateCore<RecyclerView.ViewHolder, T> delegateCore = core.isHeader(holder.getAdapterPosition()) ? headerDelegateCore : childDelegateCore;
        return delegateCore.onFailedToRecycleView(holder);
    }

    @Override
    public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
        DelegateCore<RecyclerView.ViewHolder, T> delegateCore = core.isHeader(holder.getAdapterPosition()) ? headerDelegateCore : childDelegateCore;
        delegateCore.onViewAttachedToWindow(holder);
    }

    @Override
    public void onViewDetachedFromWindow(RecyclerView.ViewHolder holder) {
        DelegateCore<RecyclerView.ViewHolder, T> delegateCore = core.isHeader(holder.getAdapterPosition()) ? headerDelegateCore : childDelegateCore;
        delegateCore.onViewDetachedFromWindow(holder);
    }

    /**
     * Registers the <code>binder</code> to handle creating and binding the headers of type
     * <code>viewType</code>. If a {@link ViewHolderBinder} has already been specified
     * for the <code>viewType</code> then the value will be overwritten with <code>binder</code>
     *
     * @param viewType The type of view the {@link ViewHolderBinder} handles
     * @param binder The {@link ViewHolderBinder} to handle creating and binding views
     */
    public void registerHeaderViewHolderBinder(int viewType, ViewHolderBinder binder) {
        int headerViewType = viewType | HeaderApi.HEADER_VIEW_TYPE_MASK;
        headerDelegateCore.registerViewHolderBinder(headerViewType, binder);
    }

    /**
     * Registers the <code>binder</code> to handle creating and binding the children of type
     * <code>viewType</code>. If a {@link ViewHolderBinder} has already been specified
     * for the <code>viewType</code> then the value will be overwritten with <code>binder</code>
     *
     * @param viewType The type of view the {@link ViewHolderBinder} handles
     * @param binder The {@link ViewHolderBinder} to handle creating and binding views
     */
    public void registerChildViewHolderBinder(int viewType, ViewHolderBinder binder) {
        int childViewType = viewType & ~HeaderApi.HEADER_VIEW_TYPE_MASK;
        childDelegateCore.registerViewHolderBinder(childViewType, binder);
    }

    /**
     * Registers the <code>binder</code> to handle creating and binding the views that aren't
     * handled by any binders registered with {@link #registerHeaderViewHolderBinder(int, ViewHolderBinder)}.
     * If a {@link ViewHolderBinder} has already been specified as the default then the value will be
     * overwritten with <code>binder</code>
     *
     * @param binder The {@link ViewHolderBinder} to handle creating and binding default views
     */
    public void registerDefaultHeaderViewHolderBinder(@Nullable ViewHolderBinder binder) {
        headerDelegateCore.registerDefaultViewHolderBinder(binder);
    }

    /**
     * Registers the <code>binder</code> to handle creating and binding the views that aren't
     * handled by any binders registered with {@link #registerChildViewHolderBinder(int, ViewHolderBinder)}.
     * If a {@link ViewHolderBinder} has already been specified as the default then the value will be
     * overwritten with <code>binder</code>
     *
     * @param binder The {@link ViewHolderBinder} to handle creating and binding default views
     */
    public void registerDefaultViewHolderBinder(@Nullable ViewHolderBinder binder) {
        childDelegateCore.registerDefaultViewHolderBinder(binder);
    }
}
