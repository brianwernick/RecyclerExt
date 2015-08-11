package com.devbrackets.android.recyclerext.adapter;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * A basic adapter with support for lists to simplify integrations.
 *
 * @param <VH> The ViewHolder to use
 * @param <T> The object type for the list
 */
public abstract class RecyclerListAdapter<VH extends RecyclerView.ViewHolder, T> extends RecyclerView.Adapter<VH> {

    @Nullable
    protected List<T> items;
    protected boolean notifyOnChange = true;

    public RecyclerListAdapter() {
        //Purposefully left blank
    }

    public RecyclerListAdapter(@Nullable List<T> itemList) {
        this.items = itemList;
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    @Nullable
    public T getItem(int position) {
        if (items == null || position < 0 || position >= getItemCount()) {
            return null;
        }

        return items.get(position);
    }

    public void clear() {
        if (items == null) {
            return;
        }

        items.clear();

        if (notifyOnChange) {
            notifyDataSetChanged();
        }
    }

    public void add(T item) {
        if (items == null) {
            items = new ArrayList<>();
        }

        items.add(item);

        if (notifyOnChange) {
            notifyDataSetChanged();
        }
    }

    public void addAll(List<T> itemList) {
        if (items == null) {
            items = new ArrayList<>();
        }

        items.addAll(itemList);

        if (notifyOnChange) {
            notifyDataSetChanged();
        }
    }

    public void remove(T screen) {
        if (items == null) {
            return;
        }

        items.remove(screen);

        if (notifyOnChange) {
            notifyDataSetChanged();
        }
    }

    /**
     * Control whether methods that change the list automatically call notifyDataSetChanged().
     * If set to false, caller must manually call notifyDataSetChanged() to have the changes reflected
     * in the attached view. The default is true, and calling notifyDataSetChanged() resets the flag to true.
     *
     * @param notifyOnChange if true, modifications to the list will automatically call notifyDataSetChanged()
     */
    public void setNotifyOnChange(boolean notifyOnChange) {
        this.notifyOnChange = notifyOnChange;
    }
}
