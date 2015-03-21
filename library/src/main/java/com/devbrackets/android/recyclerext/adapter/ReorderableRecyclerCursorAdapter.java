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

import android.database.Cursor;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;

import java.util.LinkedList;
import java.util.Queue;

/**
 * A Cursor adapter for the RecyclerView that correctly keeps track of reorder changes made by the
 * {@link com.devbrackets.android.recyclerext.decoration.ReorderDecoration}
 */
public abstract class ReorderableRecyclerCursorAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerCursorAdapter<VH> {
    private Queue<ReorderItem> reorderQueue = new LinkedList<>();

    public ReorderableRecyclerCursorAdapter(Cursor cursor) {
        super(cursor);
    }

    public ReorderableRecyclerCursorAdapter(Cursor cursor, String idColumnName) {
        super(cursor, idColumnName);
    }

    @Nullable
    @Override
    public Cursor getCursor(int position) {
        return super.getCursor(calculateCursorPosition(position));
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(calculateCursorPosition(position));
    }

    /**
     * Informs the adapter that an item was reordered.  This will add a ReorderItem to
     * the cache used to calculate the modified positions in the cursor.  If either of
     * the positions are outside the cursor bounds, or they are the same then
     * no ReorderItem will be added to the cache
     *
     * @param originalPosition The original position of the item
     * @param newPosition The new position for the item
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

        ReorderItem item = new ReorderItem(originalPosition, newPosition);
        reorderQueue.add(item);

        notifyDataSetChanged();
    }

    /**
     * Removes the oldest ReorderItem from the cache so that
     * the index change is no longer accounted for when calculating the
     * modified indices for the cursor.
     */
    public void removeOldestReorderItem() {
        if (reorderQueue.size() > 0) {
            reorderQueue.remove();
        }
    }

    /**
     * Retrieves the oldest ReorderItem from the cache in order to determine
     * what changes need to be persisted in the database.
     *
     * @return The oldest ReorderItem or null
     */
    @Nullable
    public ReorderItem getOldestReorderItem() {
        return reorderQueue.peek();
    }

    /**
     * Using the {@link #reorderQueue} the modified cursor position is retrieved
     * so that the RecyclerView items can still be used and modified while the order
     * changes are being persisted to the database.
     *
     * TODO: make sure to correctly handle chained moves (e.g. [0 -> 1] then [1 -> 2])
     *
     * @param viewPosition The position in the visual list to retrieve
     * @return The modified cursor position for the actual item to retrieve
     */
    private int calculateCursorPosition(int viewPosition) {
        if (reorderQueue.size() == 0) {
            return viewPosition;
        }

        int cursorPositionDelta = 0;
        for (ReorderItem item: reorderQueue) {

            //If the reordered item is this item, then make sure to move the cursor the appropriate amount
            if (item.getNewPosition() == viewPosition) {
                cursorPositionDelta = item.getOriginalPosition() - viewPosition;
            } else if (item.getOriginalPosition() < item.getNewPosition() && viewPosition >= item.getOriginalPosition() && viewPosition < item.getNewPosition()) {
                cursorPositionDelta++;
            } else if (item.getOriginalPosition() > item.getNewPosition() && viewPosition <= item.getOriginalPosition() && viewPosition > item.getNewPosition()) {
                cursorPositionDelta--;
            }
        }

        return viewPosition + cursorPositionDelta;
    }

    /**
     * An object to represent an index change with an id so that the cursor can be updated
     * for single items without forgetting reorders that occurred between the start of the
     * database update and the cursor retrieval.
     */
    public static class ReorderItem {
        private final int originalPosition;
        private final int newPosition;

        public ReorderItem(int originalPosition, int newPosition) {
            this.originalPosition = originalPosition;
            this.newPosition = newPosition;
        }

        public int getOriginalPosition() {
            return originalPosition;
        }

        public int getNewPosition() {
            return newPosition;
        }
    }
}