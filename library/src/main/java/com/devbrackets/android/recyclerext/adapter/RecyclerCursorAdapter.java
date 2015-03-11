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

import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.FilterQueryProvider;
import android.widget.Filterable;

import com.devbrackets.android.recyclerext.filter.CursorFilter;

/**
 * A base cursor adapter for the RecyclerView
 *
 * TODO: when adding reordering support (it might not be in this file) see Jordan's comment below
 * A Markov chain of moves. Then going through that with each lookup.
 * That is the smallest space wise and has the best update time. Mainly because it handles the case of and update being affected by another move
 */
public abstract class RecyclerCursorAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> implements Filterable,
        CursorFilter.CursorFilterClient {
    protected static final String DEFAULT_ID_COLUMN_NAME = "_id";

    protected Cursor cursor;
    protected boolean isValidData;
    protected int idColumn;

    protected CursorFilter cursorFilter;
    protected ChangeObserver internalChangeObserver;
    protected DataSetObserver internalDataSetObserver;
    protected FilterQueryProvider filterQueryProvider;

    @Nullable
    private String idColumnName;

    /**
     * @param cursor The cursor from which to get the data.
     */
    public RecyclerCursorAdapter(Cursor cursor) {
        setupCursor(cursor, null);
    }

    /**
     * @param cursor The cursor from which to get the data.
     * @param idColumnName The name for the id column to use when calling {@link #getItemId(int)} [default: {@value #DEFAULT_ID_COLUMN_NAME}]
     */
    public RecyclerCursorAdapter(Cursor cursor, String idColumnName) {
        this.idColumnName = idColumnName != null ? idColumnName : DEFAULT_ID_COLUMN_NAME;
        setupCursor(cursor, idColumnName);
    }

    @Override
    public abstract VH onCreateViewHolder(ViewGroup parent, int viewType);

    @Override
    public abstract void onBindViewHolder(VH holder, int position);

    /**
     * Called when the {@link ContentObserver} on the cursor receives a change notification
     *
     * @see ContentObserver#onChange(boolean)
     */
    public abstract void onContentChanged();

    /**
     * Retrieves the amount of items in the current cursor
     *
     * @return The amount of items in the cursor
     */
    @Override
    public int getItemCount() {
        if (isValidData && cursor != null) {
            return cursor.getCount();
        }

        return 0;
    }

    /**
     * Returns the current cursor
     *
     * @return The current cursor
     */
    @Override
    @Nullable
    public Cursor getCursor() {
        return cursor;
    }

    /**
     * Get the cursor associated with the specified position in the data set.
     *
     * @param position The position of the item whose data we want within the adapter's data set.
     * @return The cursor representing the data at the specified position.
     */
    @Nullable
    public Cursor getCursor(int position) {
        if (isValidData && cursor != null) {
            cursor.moveToPosition(position);
            return cursor;
        }

        return null;
    }

    /**
     * Get the row id associated with the specified position in the list.
     *
     * @param position The position of the item within the adapter's data set whose row id we want.
     * @return The id of the item at the specified position.
     */
    public long getItemId(int position) {
        if (isValidData && cursor != null) {
            if (cursor.moveToPosition(position)) {
                return cursor.getLong(idColumn);
            } else {
                return 0;
            }
        }

        return 0;
    }

    /**
     * Change the underlying cursor to a new cursor. If there is an existing cursor it will be
     * closed.
     *
     * @param newCursor The new cursor to be used
     */
    @Override
    public void changeCursor(Cursor newCursor) {
        Cursor oldCursor = swapCursor(newCursor);
        if (oldCursor != null && !oldCursor.isClosed()) {
            oldCursor.close();
        }
    }

    /**
     * Swap in a new Cursor, returning the old Cursor.  Unlike
     * {@link #changeCursor(Cursor)}, the returned old Cursor is <em>not</em>
     * closed.
     *
     * @param newCursor The new cursor to be used.
     * @return Returns the previously set Cursor, or null if there wasa not one.
     * If the given new Cursor is the same instance is the previously set
     * Cursor, null is also returned.
     */
    @Nullable
    public Cursor swapCursor(Cursor newCursor) {
        if (newCursor == cursor) {
            return null;
        }

        Cursor oldCursor = cursor;
        if (oldCursor != null) {
            if (internalChangeObserver != null) {
                oldCursor.unregisterContentObserver(internalChangeObserver);
            }

            if (internalDataSetObserver != null) {
                oldCursor.unregisterDataSetObserver(internalDataSetObserver);
            }
        }

        setupCursor(newCursor, idColumnName);
        if (newCursor != null) {
            notifyDataSetChanged();
        }

        return oldCursor;
    }

    /**
     * <p>Converts the cursor into a CharSequence. Subclasses should override this
     * method to convert their results. The default implementation returns an
     * empty String for null values or the default String representation of
     * the value.</p>
     *
     * @param cursor the cursor to convert to a CharSequence
     * @return a CharSequence representing the value
     */
    public CharSequence convertToString(Cursor cursor) {
        return cursor == null ? "" : cursor.toString();
    }

    /**
     * Runs a query with the specified constraint. This query is requested
     * by the filter attached to this adapter.
     *
     * The query is provided by a
     * {@link android.widget.FilterQueryProvider}.
     * If no provider is specified, the current cursor is not filtered and returned.
     *
     * After this method returns the resulting cursor is passed to {@link #changeCursor(Cursor)}
     * and the previous cursor is closed.
     *
     * This method is always executed on a background thread, not on the
     * application's main thread (or UI thread.)
     *
     * Contract: when constraint is null or empty, the original results,
     * prior to any filtering, must be returned.
     *
     * @param constraint the constraint with which the query must be filtered
     *
     * @return a Cursor representing the results of the new query
     *
     * @see #getFilter()
     * @see #getFilterQueryProvider()
     * @see #setFilterQueryProvider(android.widget.FilterQueryProvider)
     */
    @Nullable
    public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
        if (filterQueryProvider != null) {
            return filterQueryProvider.runQuery(constraint);
        }

        return cursor;
    }

    public Filter getFilter() {
        if (cursorFilter == null) {
            cursorFilter = new CursorFilter(this);
        }

        return cursorFilter;
    }

    /**
     * Returns the query filter provider used for filtering. When the
     * provider is null, no filtering occurs.
     *
     * @return the current filter query provider or null if it does not exist
     *
     * @see #setFilterQueryProvider(android.widget.FilterQueryProvider)
     * @see #runQueryOnBackgroundThread(CharSequence)
     */
    @Nullable
    public FilterQueryProvider getFilterQueryProvider() {
        return filterQueryProvider;
    }

    /**
     * Sets the query filter provider used to filter the current Cursor.
     * The provider's
     * {@link android.widget.FilterQueryProvider#runQuery(CharSequence)}
     * method is invoked when filtering is requested by a client of
     * this adapter.
     *
     * @param provider the filter query provider or null to remove it
     *
     * @see #getFilterQueryProvider()
     * @see #runQueryOnBackgroundThread(CharSequence)
     */
    public void setFilterQueryProvider(@Nullable FilterQueryProvider provider) {
        this.filterQueryProvider = provider;
    }

    /**
     * Updates the global variables, and registers observers to the cursor for any
     * changes in order to notify the implementing class.
     *
     * @param cursor The cursor from which to get the data
     */
    private void setupCursor(Cursor cursor, String idColumnName) {
        this.cursor = cursor;
        isValidData = cursor != null;
        idColumn = isValidData ? cursor.getColumnIndexOrThrow(idColumnName) : -1;

        //Makes sure the observers aren't null
        if (internalChangeObserver == null) {
            internalChangeObserver = new ChangeObserver();
        }

        if (internalDataSetObserver == null) {
            internalDataSetObserver = new MyDataSetObserver();
        }

        //Registers the observers
        if (cursor != null) {
            cursor.registerContentObserver(internalChangeObserver);
            cursor.registerDataSetObserver(internalDataSetObserver);
        }
    }

    private class ChangeObserver extends ContentObserver {
        public ChangeObserver() {
            super(new Handler());
        }

        @Override
        public boolean deliverSelfNotifications() {
            return true;
        }

        @Override
        public void onChange(boolean selfChange) {
            onContentChanged();
        }
    }

    private class MyDataSetObserver extends DataSetObserver {
        @Override
        public void onChanged() {
            isValidData = true;
            notifyDataSetChanged();
        }

        @Override
        public void onInvalidated() {
            isValidData = false;
        }
    }
}
