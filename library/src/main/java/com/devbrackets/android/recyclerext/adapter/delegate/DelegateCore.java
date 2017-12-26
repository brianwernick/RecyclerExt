package com.devbrackets.android.recyclerext.adapter.delegate;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.SparseArrayCompat;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

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
        getBinderOrThrow(delegateApi.getItemViewType(position)).onBindViewHolder(holder, delegateApi.getItem(position), position);
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
