/*
 * Copyright (C) 2016 - 2018 Brian Wernick
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
package com.devbrackets.android.recyclerext.adapter

import androidx.recyclerview.widget.RecyclerView.ViewHolder
import java.util.*

/**
 * A basic adapter with support for lists to simplify integrations.
 *
 * @param <VH> The ViewHolder to use
 * @param <T>  The object type for the list
 */
abstract class ListAdapter<VH : ViewHolder, T>(
        items: List<T> = emptyList()
) : ActionableAdapter<VH>() {
    protected var items: MutableList<T> = items.toMutableList()

    protected val lock = Any()

    /**
     * Control whether methods that change the list automatically call notifyDataSetChanged().
     * If set to false, caller must manually call notifyDataSetChanged() to have the changes reflected
     * in the attached view. The default is true, and calling notifyDataSetChanged() resets the flag to true.
     */
    var notifyOnChange = true

    /**
     * Retrieves the number of items for the adapter.  By default this
     * will use the number of items in the list.
     *
     * @return The number of items in the adapter
     */
    override fun getItemCount(): Int {
        return items.size
    }

    /**
     * Retrieves the item with the specified position in the adapter.  If the position
     * is not in the list of items then null will be returned.
     *
     * @param position The items position in the list
     * @return The item at the specified position in the list or null
     */
    fun getItem(position: Int): T? {
        return items.getOrNull(position)
    }

    /**
     * Searches the items for the specified object and returns the index of the
     * first occurrence.
     *
     * @param item The object to search for
     * @return The index of the first occurrence of the object or -1 if the object was not found
     */
    fun getPosition(item: T): Int {
        return if (items.isEmpty()) {
            -1
        } else items.indexOf(item)
    }

    /**
     * Clears all the items from the list
     */
    fun clear() {
        synchronized(lock) {
            items.clear()
        }

        if (notifyOnChange) {
            notifyDataSetChanged()
        }
    }

    /**
     * Clears all the current items in the list and
     * adds all the specified items to the list
     *
     * @param itemList the list of items to add
     */
    fun set(itemList: List<T>) {
        synchronized(lock) {
            items.clear()
            items.addAll(itemList)
        }

        if (notifyOnChange) {
            notifyDataSetChanged()
        }
    }

    /**
     * Adds the specified item to the end of the list
     *
     * @param item The item to add to the list
     */
    fun add(item: T) {
        synchronized(lock) {
            items.add(item)
        }

        if (notifyOnChange) {
            notifyItemInserted(items.size)
        }
    }

    /**
     * Adds the specified item to the list with the specified position
     *
     * @param position The position to insert the item at
     * @param item The item to add to the list
     */
    fun add(position: Int, item: T) {
        synchronized(lock) {
            items.add(position, item)
        }

        if (notifyOnChange) {
            notifyItemInserted(position)
        }
    }

    /**
     * Adds all the specified items to the list
     *
     * @param itemList The list of items to add
     */
    fun addAll(itemList: List<T>) {
        synchronized(lock) {
            items.addAll(itemList)
        }

        if (notifyOnChange) {
            if (items.size - itemList.size != 0) {
                notifyItemRangeChanged(items.size - itemList.size, itemList.size)
            } else {
                notifyDataSetChanged()
            }
        }
    }

    /**
     * Removes the specified item from the list
     *
     * @param item The item to remove from the list
     */
    fun remove(item: T) {
        var removeIndex: Int
        synchronized(lock) {
            removeIndex = items.indexOf(item)
            if (removeIndex != -1) {
                items.removeAt(removeIndex)
            }
        }

        if (notifyOnChange && removeIndex != -1) {
            notifyItemRemoved(removeIndex)
        }
    }

    /**
     * Removes the item with the specified position from the list
     *
     * @param position The position for the item to remove
     */
    fun remove(position: Int) {
        synchronized(lock) {
            if (position < 0 || position > items.size) {
                return
            }

            items.removeAt(position)
        }

        if (notifyOnChange) {
            notifyItemRemoved(position)
        }
    }

    /**
     * Swaps the items with the specified positions in the list.  If either
     * position is out of bounds then no action will be performed.
     *
     * @param positionOne The position of the first item to swap
     * @param positionTwo The position of the second item to swap
     */
    fun swap(positionOne: Int, positionTwo: Int) {
        synchronized(lock) {
            if (positionOne == positionTwo || positionOne < 0 || positionOne >= items.size || positionTwo < 0 || positionTwo >= items.size) {
                return
            }

            val temp = items[positionOne]
            items[positionOne] = items[positionTwo]
            items.set(positionTwo, temp)
        }

        if (notifyOnChange) {
            notifyItemsSwapped(positionOne, positionTwo)
        }
    }

    /**
     * Moves the item at the `originalPosition` to the `endPosition`.
     * If the `endPosition` is greater than the number of items, it will be added to the
     * end of the list instead.
     *
     * @param originalPosition The position the object is in that needs to be moved
     * @param endPosition The end position for the item being moved
     */
    fun move(originalPosition: Int, endPosition: Int) {
        var destinationPosition = endPosition
        synchronized(lock) {
            if (originalPosition < 0 || destinationPosition < 0 || originalPosition >= items.size) {
                return
            }

            if (destinationPosition >= items.size) {
                destinationPosition = items.size
            }

            if (originalPosition == destinationPosition) {
                return
            }

            val temp = items[originalPosition]
            items.removeAt(originalPosition)
            items.add(destinationPosition, temp)
        }

        if (notifyOnChange) {
            notifyItemMoved(originalPosition, destinationPosition)
        }
    }

    /**
     * Notify any registered observers that the item reflected at `positionOne`
     * has been moved to `positionTwo` and vice-versa.
     *
     *
     * This is a structural change event. Representations of other existing items in the
     * data set are still considered up to date and will not be rebound, though their
     * positions may be altered.
     *
     * @param positionOne The position of the first item moved
     * @param positionTwo The position of the second item moved
     */
    fun notifyItemsSwapped(positionOne: Int, positionTwo: Int) {
        val lowerPosition = if (positionOne < positionTwo) positionOne else positionTwo
        val upperPosition = if (positionOne > positionTwo) positionOne else positionTwo

        notifyItemMoved(lowerPosition, upperPosition)
        notifyItemMoved(upperPosition - 1, lowerPosition)
    }

    /**
     * Sorts the items in the adapter using the specified comparator
     *
     * @param comparator The comparator to sort the list with
     */
    fun sort(comparator: Comparator<in T>) {
        synchronized(lock) {
            Collections.sort(items, comparator)
        }

        if (notifyOnChange) {
            notifyDataSetChanged()
        }
    }
}