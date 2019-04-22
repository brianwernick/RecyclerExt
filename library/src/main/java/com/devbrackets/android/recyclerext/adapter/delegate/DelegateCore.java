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


package com.devbrackets.android.recyclerext.adapter.delegate;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.SparseArrayCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.ViewGroup;

import com.devbrackets.android.recyclerext.R;

import java.util.LinkedList;
import java.util.List;

public class DelegateCore<VH extends RecyclerView.ViewHolder, T> {
    @NonNull
    protected DelegateApi<T> delegateApi;
    @NonNull
    protected RecyclerView.Adapter adapter;
    @NonNull
    protected SparseArrayCompat<ViewHolderBinder<VH, T>> binders = new SparseArrayCompat<>();

    @Nullable
    protected ViewHolderBinder<VH, T> defaultBinder;

    public DelegateCore(@NonNull DelegateApi<T> delegateApi, @NonNull RecyclerView.Adapter adapter) {
        this.delegateApi = delegateApi;
        this.adapter = adapter;
    }

    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return getBinderOrThrow(viewType).onCreateViewHolder(parent, viewType);
    }

    public void onBindViewHolder(@NonNull VH holder, int position) {
        int itemViewType = delegateApi.getItemViewType(holder.getAdapterPosition());
        getBinderOrThrow(itemViewType).onBindViewHolder(holder, delegateApi.getItem(position), position);
        holder.itemView.setTag(R.id.recyclerext_view_type, itemViewType);
    }

    /**
     * @see RecyclerView.Adapter#onViewRecycled(RecyclerView.ViewHolder)
     */
    public void onViewRecycled(RecyclerView.ViewHolder holder) {
        invokeBinderMethod(holder, binder -> {
            //noinspection unchecked
            binder.onViewRecycled((VH) holder);
            return true;
        });
    }

    /**
     * @see RecyclerView.Adapter#onFailedToRecycleView(RecyclerView.ViewHolder)
     */
    public boolean onFailedToRecycleView(RecyclerView.ViewHolder holder) {
        return invokeBinderMethod(holder, binder -> {
            //noinspection unchecked
            return binder.onFailedToRecycleView((VH) holder);
        });
    }

    /**
     * @see RecyclerView.Adapter#onViewAttachedToWindow(RecyclerView.ViewHolder)
     */
    public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
        invokeBinderMethod(holder, binder -> {
            //noinspection unchecked
            binder.onViewAttachedToWindow((VH) holder);
            return true;
        });
    }

    /**
     * @see RecyclerView.Adapter#onViewDetachedFromWindow(RecyclerView.ViewHolder)
     */
    public void onViewDetachedFromWindow(RecyclerView.ViewHolder holder) {
        invokeBinderMethod(holder, binder -> {
            //noinspection unchecked
            binder.onViewDetachedFromWindow((VH) holder);
            return true;
        });
    }

    /**
     * Registers the <code>binder</code> to handle creating and binding the views of type
     * <code>viewType</code>. If a {@link ViewHolderBinder} has already been specified
     * for the <code>viewType</code> then the value will be overwritten with <code>binder</code>
     *
     * @param viewType The type of view the {@link ViewHolderBinder} handles
     * @param binder The {@link ViewHolderBinder} to handle creating and binding views
     */
    public void registerViewHolderBinder(int viewType, @NonNull ViewHolderBinder<VH, T> binder) {
        ViewHolderBinder<VH, T> oldBinder = binders.get(viewType);
        if (oldBinder != null && oldBinder == binder) {
            return;
        }

        if (oldBinder != null) {
            oldBinder.onDetachedFromAdapter(adapter);
        }

        binders.put(viewType, binder);
        binder.onAttachedToAdapter(adapter);
    }

    /**
     * Registers the <code>binder</code> to handle creating and binding the views that aren't
     * handled by any binders registered with {@link #registerViewHolderBinder(int, ViewHolderBinder)}.
     * If a {@link ViewHolderBinder} has already been specified as the default then the value will be
     * overwritten with <code>binder</code>
     *
     * @param binder The {@link ViewHolderBinder} to handle creating and binding default views
     */
    public void registerDefaultViewHolderBinder(@Nullable ViewHolderBinder<VH, T> binder) {
        if (defaultBinder == binder) {
            return;
        }

        if (defaultBinder != null) {
            defaultBinder.onDetachedFromAdapter(adapter);
        }

        defaultBinder = binder;
        binder.onAttachedToAdapter(adapter);
    }

    public List<ViewHolderBinder<VH, T>> getViewHolderBinders() {
        List<ViewHolderBinder<VH, T>> binderList = new LinkedList<>();
        for (int i = 0; i < binders.size(); i++) {
            binderList.add(binders.get(binders.keyAt(i)));
        }

        return binderList;
    }

    @Nullable
    public ViewHolderBinder<VH, T> getDefaultViewHolderBinder() {
        return defaultBinder;
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
        ViewHolderBinder<VH, T> binder = getBinder(viewType);
        if (binder != null) {
            return binder;
        }

        throw new IllegalStateException("Unable to create or bind ViewHolders of viewType " + viewType + " because no ViewHolderBinder has been registered for that viewType");
    }

    /**
     * Retrieves the {@link ViewHolderBinder} associated with the <code>viewType</code>
     *
     * @param viewType The type of the view to retrieve the {@link ViewHolderBinder} for
     * @return The {@link ViewHolderBinder} that handles the <code>viewType</code>
     */
    @Nullable
    protected ViewHolderBinder<VH, T> getBinder(int viewType) {
        ViewHolderBinder<VH, T> binder = binders.get(viewType);
        if (binder != null) {
            return binder;
        }

        return defaultBinder;
    }

    protected interface BinderMethodInvoker<VH extends RecyclerView.ViewHolder, T> {
        boolean binderMethod(ViewHolderBinder<VH, T> binder);
    }

    /**
     * Handles invoking a method on a {@link ViewHolderBinder} if it handles the specified <code>holder</code>,
     * catching any exceptions thrown.
     *
     * @param holder The holder used to determine the correct {@link ViewHolderBinder}
     * @param binderMethodInvoker The method to run on the {@link ViewHolderBinder}
     * @return The result of <code>binderMethodInvoker</code> or <code>false</code> if an exception is thrown or no binder handles the <code>holder</code>
     */
    protected boolean invokeBinderMethod(RecyclerView.ViewHolder holder, BinderMethodInvoker<VH, T> binderMethodInvoker) {
        Integer itemViewType = (Integer) holder.itemView.getTag(R.id.recyclerext_view_type);
        if (itemViewType == null) {
            return false;
        }

        ViewHolderBinder<VH, T> binder = getBinder(itemViewType);
        if (binder != null) {
            try {
                return binderMethodInvoker.binderMethod(binder);
            } catch (Exception e) {
                // Purposefully left blank
            }
        }

        return false;
    }
}
