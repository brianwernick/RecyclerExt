/*
 * Copyright (C) 2014 Lucas Rocha (TwoWayView)
 *
 * This code is based on Android's StaggeredLayoutManager's
 * LazySpanLookup class.
 *
 * Copyright (C) 2014 The Android Open Source Project
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

package com.devbrackets.android.recyclerext.layout;

import com.devbrackets.android.recyclerext.layout.BaseLayoutManager.ItemEntry;

import java.util.Arrays;

/**
 *
 */
class ItemEntries {
    private static final int MIN_SIZE = 10;

    private ItemEntry[] entries;
    private int adapterSize;
    private boolean restoringItem;

    private int sizeForPosition(int position) {
        int len = entries.length;
        while (len <= position) {
            len *= 2;
        }

        // We don't apply any constraints while restoring
        // item entries.
        if (!restoringItem && len > adapterSize) {
            len = adapterSize;
        }

        return len;
    }

    private void ensureSize(int position) {
        if (entries == null) {
            entries = new ItemEntry[Math.max(position, MIN_SIZE) + 1];
            Arrays.fill(entries, null);
        } else if (position >= entries.length) {
            ItemEntry[] oldItemEntries = entries;
            entries = new ItemEntry[sizeForPosition(position)];
            System.arraycopy(oldItemEntries, 0, entries, 0, oldItemEntries.length);
            Arrays.fill(entries, oldItemEntries.length, entries.length, null);
        }
    }

    public ItemEntry getItemEntry(int position) {
        if (entries == null || position >= entries.length) {
            return null;
        }

        return entries[position];
    }

    public void putItemEntry(int position, ItemEntry entry) {
        ensureSize(position);
        entries[position] = entry;
    }

    public void restoreItemEntry(int position, ItemEntry entry) {
        restoringItem = true;
        putItemEntry(position, entry);
        restoringItem = false;
    }

    public int size() {
        return (entries != null ? entries.length : 0);
    }

    public void setAdapterSize(int adapterSize) {
        this.adapterSize = adapterSize;
    }

    public void invalidateItemLanesAfter(int position) {
        if (entries == null || position >= entries.length) {
            return;
        }

        for (int i = position; i < entries.length; i++) {
            final ItemEntry entry = entries[i];
            if (entry != null) {
                entry.invalidateLane();
            }
        }
    }

    public void clear() {
        if (entries != null) {
            Arrays.fill(entries, null);
        }
    }

    void offsetForRemoval(int positionStart, int itemCount) {
        if (entries == null || positionStart >= entries.length) {
            return;
        }

        ensureSize(positionStart + itemCount);

        System.arraycopy(entries, positionStart + itemCount, entries, positionStart, entries.length - positionStart - itemCount);
        Arrays.fill(entries, entries.length - itemCount, entries.length, null);
    }

    void offsetForAddition(int positionStart, int itemCount) {
        if (entries == null || positionStart >= entries.length) {
            return;
        }

        ensureSize(positionStart + itemCount);

        System.arraycopy(entries, positionStart, entries, positionStart + itemCount, entries.length - positionStart - itemCount);
        Arrays.fill(entries, positionStart, positionStart + itemCount, null);
    }
}
