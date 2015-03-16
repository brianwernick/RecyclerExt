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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * A Cursor adapter for the RecyclerView that correctly keeps track of reorder changes made by the
 * {@link com.devbrackets.android.recyclerext.decoration.ReorderDecoration}
 */
public abstract class ReorderableRecyclerCursorAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerCursorAdapter<VH> {
    public static final long INVALID_REORDER_ITEM_ID = -1;
    private long currentReorderItemId = 1;
    private List<ReorderItem> reorderedItems = new LinkedList<>();

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
     * {@link #INVALID_REORDER_ITEM_ID} will be returned instead of the cached id.
     *
     * @param originalPosition The original position of the item
     * @param newPosition The new position for the item
     * @return The ReorderItems id to use when notifying the Adapter that the change has been persisted to the database or {@link #INVALID_REORDER_ITEM_ID}
     */
    public long reorderItem(int originalPosition, int newPosition) {
        //Make sure the positions aren't the same
        if (originalPosition == newPosition) {
            return INVALID_REORDER_ITEM_ID;
        }

        //Make sure the positions aren't out of bounds
        if (originalPosition < 0 || newPosition < 0 || originalPosition >= getItemCount() || newPosition >= getItemCount()) {
            return INVALID_REORDER_ITEM_ID;
        }

        ReorderItem item = new ReorderItem(currentReorderItemId, originalPosition, newPosition);
        reorderedItems.add(item);

        currentReorderItemId++;
        notifyDataSetChanged();

        return item.getId();
    }

    /**
     * Removes the ReorderItem with the specified id from the cache so that
     * the index change is no longer accounted for when calculating the
     * modified indices for the cursor.
     *
     * @param id The ReorderItem id to remove from the cache
     */
    public void removeReorderItem(long id) {
        ReorderItem item;
        Iterator<ReorderItem> iterator = reorderedItems.iterator();
        while (iterator.hasNext()) {
            item = iterator.next();
            if (item.getId() == id) {
                iterator.remove();
                return;
            }
        }
    }

    /**
     * Removes the ReorderItems with the specified ids from the cache so that
     * the index changes are no longer accounted for when calculating the
     * modified indices for the cursor.
     *
     * @param ids The ReorderItem ids to remove from the cache
     */
    public void removeReorderItems(@NonNull List<Long> ids) {
        ReorderItem item;
        Iterator<ReorderItem> iterator = reorderedItems.iterator();
        while (iterator.hasNext()) {
            item = iterator.next();
            for (long id : ids) {
                if (item.getId() == id) {
                    iterator.remove();
                    break;
                }
            }
        }
    }

    /**
     * Retrieves the ReorderItems that are currently cached in order to determine
     * what changes need to be persisted in the database.
     *
     * @return A List of ReorderItems
     */
    @NonNull
    public List<ReorderItem> getReorderItems() {
        return reorderedItems;
    }

    /**
     * Retrieves the ReorderItem that is associated with the passed id.
     *
     * @param id The ReorderItems id
     * @return The ReorderItem
     */
    @Nullable
    public ReorderItem getReorderItem(long id) {
        for (ReorderItem item : reorderedItems) {
            if (item.getId() == id) {
                return item;
            }
        }

        return null;
    }

    /**
     * Using the {@link #reorderedItems} the modified cursor position is retrieved
     * so that the RecyclerView items can still be used and modified while the order
     * changes are being persisted to the database.
     *
     * TODO: make sure to correctly handle chained moves (e.g. [0 -> 1] then [1 -> 2])
     *
     * @param viewPosition The position in the visual list to retrieve
     * @return The modified cursor position for the actual item to retrieve
     */
    private int calculateCursorPosition(int viewPosition) {
        if (reorderedItems.size() == 0) {
            return viewPosition;
        }

        int cursorPositionDelta = 0;
        for (ReorderItem item: reorderedItems) {

            //If the reordered item is this item, then make sure to move the cursor the appropriate amount
            if (item.getNewPosition() == viewPosition) {
                cursorPositionDelta += (item.getOriginalPosition() - viewPosition);
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
        private final long id;
        private final int originalPosition;
        private final int newPosition;

        public ReorderItem(long id, int originalPosition, int newPosition) {
            this.id = id;
            this.originalPosition = originalPosition;
            this.newPosition = newPosition;
        }

        public long getId() {
            return id;
        }

        public int getOriginalPosition() {
            return originalPosition;
        }

        public int getNewPosition() {
            return newPosition;
        }
    }
}