/*
 * Copyright (C) 2016 Brian Wernick
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

import android.database.Cursor;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.SparseIntArray;

import java.util.LinkedList;
import java.util.List;

/**
 * A Cursor adapter for the RecyclerView that correctly keeps track of reorder changes made by the
 * {@link com.devbrackets.android.recyclerext.decoration.ReorderDecoration}
 */
public abstract class RecyclerReorderCursorAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerCursorAdapter<VH> {
    @NonNull
    private SparseIntArray cursorPositionMap = new SparseIntArray();
    private boolean resetOnCursorChange = true;

    /**
     * @param cursor The cursor from which to get the data.
     */
    public RecyclerReorderCursorAdapter(@Nullable Cursor cursor) {
        super(cursor);
    }

    /**
     * @param cursor The cursor from which to get the data.
     * @param idColumnName The name for the id column to use when calling {@link #getItemId(int)} [default: {@value #DEFAULT_ID_COLUMN_NAME}]
     */
    public RecyclerReorderCursorAdapter(@Nullable Cursor cursor, @Nullable String idColumnName) {
        super(cursor, idColumnName);
    }

    /**
     * Get the cursor associated with the specified position in the data set.  This will be
     * the correctly mapped cursor even when items have been moved
     *
     * @param position The position of the item whose data we want within the adapter's data set.
     * @return The cursor representing the data at the specified position.
     */
    @Nullable
    @Override
    public Cursor getCursor(int position) {
        return super.getCursor(cursorPositionMap.get(position, position));
    }

    /**
     * Get the row id associated with the specified position in the list.
     *
     * @param position The position of the item within the adapter's data set whose row id we want.
     * @return The id of the item at the specified position.
     */
    @Override
    public long getItemId(int position) {
        return super.getItemId(cursorPositionMap.get(position, position));
    }

    /**
     * Change the underlying cursor to a new cursor. If there is an existing cursor it will be
     * closed.  Additionally, if {@link #getResetMapOnCursorChange()} return true, then the
     * mapping of moved items will be cleared.
     *
     * @param newCursor The new cursor to be used
     */
    @Override
    public void changeCursor(@Nullable Cursor newCursor) {
        super.changeCursor(newCursor);

        if (resetOnCursorChange) {
            resetMap();
        }
    }

    /**
     * Informs the adapter that an item was reordered.  This will add a ReorderItem to
     * the cache used to calculate the modified positions in the cursor.  If either of
     * the positions are outside the cursor bounds, or they are the same then
     * no ReorderItem will be added to the cache
     *
     * @param originalPosition The original position of the item
     * @param newPosition      The new position for the item
     */
    public void reorderItem(int originalPosition, int newPosition) {
        //Make sure the positions aren't the same
        if (originalPosition == newPosition) {
            return;
        }

        //Make sure the positions aren't out of bounds
        if (originalPosition < 0 || newPosition < 0 || originalPosition >= getItemCount() || newPosition >= getItemCount()) {
            return;
        }

        int curFrom = cursorPositionMap.get(originalPosition, originalPosition);

        //Iterates through the items that will be effected and changes their positions
        if (originalPosition > newPosition) {
            for (int i = originalPosition; i > newPosition; i--) {
                cursorPositionMap.put(i, cursorPositionMap.get(i - 1, i - 1));
            }
        } else {
            for (int i = originalPosition; i < newPosition; i++) {
                cursorPositionMap.put(i, cursorPositionMap.get(i + 1, i + 1));
            }
        }

        //Makes sure the actual change is in place
        cursorPositionMap.put(newPosition, curFrom);

        cleanMap();
        notifyItemReordered(originalPosition, newPosition);
    }

    /**
     * Determines if the position map for the cursor will be reset
     * when the cursor is changed
     *
     * @return True if the map will be reset on cursor changes [default: true]
     */
    public boolean getResetMapOnCursorChange() {
        return resetOnCursorChange;
    }

    /**
     * Sets if the position map for the cursor will be reset when the cursor
     * is changed
     *
     * @param resetOnSwap True if the map should be reset on cursor changes
     */
    public void setResetMapOnCursorChange(boolean resetOnSwap) {
        this.resetOnCursorChange = resetOnSwap;
    }

    /**
     * Resets the position map for the cursor.  By default this will be
     * called when a cursor is changed (see {@link #getResetMapOnCursorChange()})
     */
    public void resetMap() {
        cursorPositionMap.clear();
    }

    /**
     * Retrieves the position map for the cursor.  This is organized by the
     * index being the list (visual) position and the values representing the corresponding
     * cursor positions.
     *
     * @return A SparseIntArray representing a map of list positions to cursor positions
     */
    @NonNull
    public SparseIntArray getPositionMap() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            return cursorPositionMap.clone();
        }

        return clone(cursorPositionMap);
    }

    /**
     * Notifies the adapter that one item was moved from one position to another.
     * This method can be overridden by subclasses that use {@link ItemTouchHelper} as a way of
     * interacting with the {@link RecyclerView} elements to call {@link #notifyItemMoved(int, int)}.
     * @param originalPosition The original position of the item
     * @param newPosition The new position of the item
     */
    protected void notifyItemReordered(int originalPosition, int newPosition) {
        notifyDataSetChanged();
    }

    /**
     * Goes through the {@link #cursorPositionMap} removing any mappings that
     * are unnecessary.  This will help keep the map as small as possible
     */
    private void cleanMap() {
        List<Integer> removeList = new LinkedList<>();

        //Finds all the mappings that point to themselves
        for (int i = 0; i < cursorPositionMap.size(); i++) {
            if (cursorPositionMap.keyAt(i) == cursorPositionMap.valueAt(i)) {
                removeList.add(cursorPositionMap.keyAt(i));
            }
        }

        //Actually removes the items
        for (int i : removeList) {
            cursorPositionMap.delete(i);
        }
    }

    /**
     * Clones the specified {@link SparseIntArray} using an iterator
     *
     * @param sparseIntArray The {@link SparseIntArray} to clone
     * @return A clone of the specified <code>sparseIntArray</code>
     */
    @NonNull
    private SparseIntArray clone(@NonNull SparseIntArray sparseIntArray) {
        SparseIntArray clone = new SparseIntArray();

        //Iterates through the keys, adding the value to the clone
        for (int index = 0; index < sparseIntArray.size(); index++) {
            int key = sparseIntArray.keyAt(index);
            clone.put(key, sparseIntArray.get(key));
        }

        return clone;
    }
}