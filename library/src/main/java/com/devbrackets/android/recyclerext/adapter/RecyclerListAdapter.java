/*
 * Copyright (C) 2015 Brian Wernick
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
 * @param <T>  The object type for the list
 */
public abstract class RecyclerListAdapter<VH extends RecyclerView.ViewHolder, T> extends RecyclerView.Adapter<VH> {
    @Nullable
    protected List<T> items;
    protected boolean notifyOnChange = true;
    private final Object lock = new Object();

    /**
     * Creates an adapter with no initial items
     */
    public RecyclerListAdapter() {
        //Purposefully left blank
    }

    /**
     * Creates an adapter with the specified items
     *
     * @param itemList The list of initial items for the adapter
     */
    public RecyclerListAdapter(@Nullable List<T> itemList) {
        this.items = itemList;
    }

    /**
     * Retrieves the number of items for the adapter.  By default this
     * will use the number of items in the list.
     *
     * @return The number of items in the adapter
     */
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
            notifyItemInserted(items.size());
        }
    }

    /**
     * Adds the specified item to the list with the specified position
     *
     * @param position The position to insert the item at
     * @param item The item to add to the list
     */
    public void add(int position, T item) {
        synchronized (lock) {
            if (items == null) {
                items = new ArrayList<>();
            }

            items.add(position, item);
        }

        if (notifyOnChange) {
            notifyItemInserted(position);
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
            notifyItemRangeChanged(items.size() - itemList.size(), itemList.size());
        }
    }

    /**
     * Removes the specified item from the list
     *
     * @param item The item to remove from the list
     */
    public void remove(T item) {
        int removeIndex;

        synchronized (lock) {
            if (items == null) {
                return;
            }

            removeIndex = items.indexOf(item);
            if (removeIndex != -1) {
                items.remove(removeIndex);
            }
        }

        if (notifyOnChange && removeIndex != -1) {
            notifyItemRemoved(removeIndex);
        }
    }

    /**
     * Removes the item with the specified position from the list
     *
     * @param position The position for the item to remove
     */
    public void remove(int position) {
        synchronized (lock) {
            if (items == null || position < 0 || position > getItemCount()) {
                return;
            }

            items.remove(position);
        }

        if (notifyOnChange) {
            notifyItemRemoved(position);
        }
    }

    /**
     * Swaps the items with the specified positions in the list.  If either
     * position is out of bounds then no action will be performed.
     *
     * @param positionOne The position of the first item to swap
     * @param positionTwo The position of the second item to swap
     */
    public void swap(int positionOne, int positionTwo) {
        synchronized (lock) {
            if (items == null || positionOne == positionTwo ||
                    positionOne < 0 || positionOne >= getItemCount() ||
                    positionTwo < 0 || positionTwo >= getItemCount()) {
                return;
            }

            T temp = items.get(positionOne);
            items.set(positionOne, items.get(positionTwo));
            items.set(positionTwo, temp);
        }

        if (notifyOnChange) {
            notifyItemsSwapped(positionOne, positionTwo);
        }
    }

    /**
     * Moves the item at the <code>originalPosition</code> to the <code>endPosition</code>.
     * If the <code>endPosition</code> is greater than the number of items, it will be added to the
     * end of the list instead.
     *
     * @param originalPosition The position the object is in that needs to be moved
     * @param endPosition The end position for the item being moved
     */
    public void move(int originalPosition, int endPosition) {
        synchronized (lock) {
            if (items == null || originalPosition < 0 || endPosition < 0 || originalPosition >= getItemCount()) {
                return;
            }

            if (endPosition >= getItemCount()) {
                endPosition = getItemCount();
            }

            if (originalPosition == endPosition) {
                return;
            }

            T temp = items.get(originalPosition);
            items.remove(originalPosition);
            items.add(endPosition, temp);
        }

        if (notifyOnChange) {
            notifyItemMoved(originalPosition, endPosition);
        }
    }

    /**
     * Notify any registered observers that the item reflected at <code>positionOne</code>
     * has been moved to <code>positionTwo</code> and vice-versa.
     * <p>
     * This is a structural change event. Representations of other existing items in the
     * data set are still considered up to date and will not be rebound, though their
     * positions may be altered.
     *
     * @param positionOne The position of the first item moved
     * @param positionTwo The position of the second item moved
     */
    public void notifyItemsSwapped(int positionOne, int positionTwo) {
        int lowerPosition = positionOne < positionTwo ? positionOne : positionTwo;
        int upperPosition = positionOne > positionTwo ? positionOne : positionTwo;

        notifyItemMoved(lowerPosition, upperPosition);
        notifyItemMoved(upperPosition -1, lowerPosition);
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

    /**
     * Sorts the items in the adapter using the specified comparator
     *
     * @param comparator The comparator to sort the list with
     */
    public void sort(Comparator<? super T> comparator) {
        synchronized (lock) {
            if (items == null) {
                return;
            }

            Collections.sort(items, comparator);
        }

        if (notifyOnChange) {
            notifyItemRangeChanged(0, items.size());
        }
    }
}
