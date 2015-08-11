package com.devbrackets.android.recyclerext.adapter;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
    private final Object lock = new Object();

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

    /**
     * Retrieves the item with the specified position in the adapter.  If the position
     * is not in the list of items then null will be returned.
     *
     * @param position The items position in the list
     * @return The item at the specified position in the list or null
     */
    @Nullable
    public T getItem(int position) {
        if (items == null || position < 0 || position >= getItemCount()) {
            return null;
        }

        return items.get(position);
    }

    /**
     * Searches the items for the specified object and returns the index of the
     * first occurrence.
     *
     * @param item The object to search for
     * @return The index of the first occurrence of the object or -1 if the object was not found
     */
    public int getPosition(T item) {
        if (items == null || items.isEmpty()) {
            return -1;
        }

        return items.indexOf(item);
    }

    /**
     * Clears all the items from the list
     */
    public void clear() {
        synchronized (lock) {
            if (items == null) {
                return;
            }

            items.clear();
        }

        if (notifyOnChange) {
            notifyDataSetChanged();
        }
    }

    /**
     * Adds the specified item to the end of the list
     *
     * @param item The item to add to the list
     */
    public void add(T item) {
        synchronized (lock) {
            if (items == null) {
                items = new ArrayList<>();
            }

            items.add(item);
        }

        if (notifyOnChange) {
            notifyDataSetChanged();
        }
    }

    /**
     * Adds all the specified items to the list
     *
     * @param itemList The list of items to add
     */
    public void addAll(List<T> itemList) {
        synchronized (lock) {
            if (items == null) {
                items = new ArrayList<>();
            }

            items.addAll(itemList);
        }

        if (notifyOnChange) {
            notifyDataSetChanged();
        }
    }

    /**
     * Removes the specified item from the list
     *
     * @param item The item to remove from the list
     */
    public void remove(T item) {
        synchronized (lock) {
            if (items == null) {
                return;
            }

            items.remove(item);
        }

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

    public void sort(Comparator<? super T> comparator) {
        synchronized (lock) {
            if (items != null) {
                Collections.sort(items, comparator);
            }
        }

        if (notifyOnChange) {
            notifyDataSetChanged();
        }
    }
}
