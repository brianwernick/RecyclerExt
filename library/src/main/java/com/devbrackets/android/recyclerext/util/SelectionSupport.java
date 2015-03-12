/*
 * Copyright (C) 2015 Lucas Rocha (TwoWayView), Brian Wernick
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

package com.devbrackets.android.recyclerext.util;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.util.LongSparseArray;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.Checkable;

import com.devbrackets.android.recyclerext.R;


/**
 *
 */
public class SelectionSupport {
    private static final String STATE_KEY_CHOICE_MODE = "choiceMode";
    private static final String STATE_KEY_CHECKED_STATES = "checkedStates";
    private static final String STATE_KEY_CHECKED_ID_STATES = "checkedIdStates";
    private static final String STATE_KEY_CHECKED_COUNT = "checkedCount";

    private static final int CHECK_POSITION_SEARCH_DISTANCE = 20;
    public static final int INVALID_POSITION = -1;


    public static enum ChoiceMode {
        NONE,
        SINGLE,
        MULTIPLE
    }

    private final RecyclerView recyclerView;
    private final TouchListener touchListener;

    private ChoiceMode choiceMode = ChoiceMode.NONE;
    private CheckedStates checkedStates;
    private CheckedIdStates checkedIdStates;
    private int checkedCount;

    private SelectionSupport(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;

        touchListener = new TouchListener(recyclerView);
        recyclerView.addOnItemTouchListener(touchListener);
    }

    private void updateOnScreenCheckedViews() {
        int count = recyclerView.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = recyclerView.getChildAt(i);
            int position = recyclerView.getChildPosition(child);
            setViewChecked(child, checkedStates.get(position));
        }
    }

    /**
     * Returns the number of items currently selected. This will only be valid
     * if the choice mode is not {@link ChoiceMode#NONE} (default).
     *
     * <p>To determine the specific items that are currently selected, use one of
     * the <code>getChecked*</code> methods.
     *
     * @return The number of items currently selected
     *
     * @see #getCheckedItemPosition()
     * @see #getCheckedItemPositions()
     * @see #getCheckedItemIds()
     */
    public int getCheckedItemCount() {
        return checkedCount;
    }

    /**
     * Returns the checked state of the specified position. The result is only
     * valid if the choice mode has been set to {@link ChoiceMode#SINGLE}
     * or {@link ChoiceMode#MULTIPLE}.
     *
     * @param position The item whose checked state to return
     * @return The item's checked state or <code>false</code> if choice mode
     *         is invalid
     *
     * @see #setChoiceMode(ChoiceMode)
     */
    public boolean isItemChecked(int position) {
        return choiceMode != ChoiceMode.NONE && checkedStates != null && checkedStates.get(position);
    }

    /**
     * Returns the currently checked item. The result is only valid if the choice
     * mode has been set to {@link ChoiceMode#SINGLE}.
     *
     * @return The position of the currently checked item or
     *         {@link #INVALID_POSITION} if nothing is selected
     *
     * @see #setChoiceMode(ChoiceMode)
     */
    public int getCheckedItemPosition() {
        if (choiceMode == ChoiceMode.SINGLE && checkedStates != null && checkedStates.size() == 1) {
            return checkedStates.keyAt(0);
        }

        return INVALID_POSITION;
    }

    /**
     * Returns the set of checked items in the list. The result is only valid if
     * the choice mode has not been set to {@link ChoiceMode#NONE}.
     *
     * @return  A SparseBooleanArray which will return true for each call to
     *          get(int position) where position is a position in the list,
     *          or <code>null</code> if the choice mode is set to
     *          {@link ChoiceMode#NONE}.
     */
    public SparseBooleanArray getCheckedItemPositions() {
        if (choiceMode != ChoiceMode.NONE) {
            return checkedStates;
        }

        return null;
    }

    /**
     * Returns the set of checked items ids. The result is only valid if the
     * choice mode has not been set to {@link ChoiceMode#NONE} and the adapter
     * has stable IDs.
     *
     * @return A new array which contains the id of each checked item in the
     *         list.
     *
     * @see android.support.v7.widget.RecyclerView.Adapter#hasStableIds()
     */
    public long[] getCheckedItemIds() {
        if (choiceMode == ChoiceMode.NONE || checkedIdStates == null || recyclerView.getAdapter() == null) {
            return new long[0];
        }

        int count = checkedIdStates.size();
        long[] ids = new long[count];

        for (int i = 0; i < count; i++) {
            ids[i] = checkedIdStates.keyAt(i);
        }

        return ids;
    }

    /**
     * Sets the checked state of the specified position. The is only valid if
     * the choice mode has been set to {@link ChoiceMode#SINGLE} or
     * {@link ChoiceMode#MULTIPLE}.
     *
     * @param position The item whose checked state is to be checked
     * @param checked The new checked state for the item
     */
    public void setItemChecked(int position, boolean checked) {
        if (choiceMode == ChoiceMode.NONE) {
            return;
        }

        Adapter adapter = recyclerView.getAdapter();

        if (choiceMode == ChoiceMode.MULTIPLE) {
            boolean oldValue = checkedStates.get(position);
            checkedStates.put(position, checked);

            if (checkedIdStates != null && adapter.hasStableIds()) {
                if (checked) {
                    checkedIdStates.put(adapter.getItemId(position), position);
                } else {
                    checkedIdStates.delete(adapter.getItemId(position));
                }
            }

            if (oldValue != checked) {
                if (checked) {
                    checkedCount++;
                } else {
                    checkedCount--;
                }
            }
        } else {
            boolean updateIds = checkedIdStates != null && adapter.hasStableIds();

            // Clear all values if we're checking something, or unchecking the currently
            // selected item
            if (checked || isItemChecked(position)) {
                checkedStates.clear();

                if (updateIds) {
                    checkedIdStates.clear();
                }
            }

            // This may end up selecting the checked we just cleared but this way
            // we ensure length of mCheckStates is 1, a fact getCheckedItemPosition relies on
            if (checked) {
                checkedStates.put(position, true);

                if (updateIds) {
                    checkedIdStates.put(adapter.getItemId(position), position);
                }

                checkedCount = 1;
            } else if (checkedStates.size() == 0 || !checkedStates.valueAt(0)) {
                checkedCount = 0;
            }
        }

        updateOnScreenCheckedViews();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void setViewChecked(View view, boolean checked) {
        if (view instanceof Checkable) {
            ((Checkable) view).setChecked(checked);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            view.setActivated(checked);
        }
    }

    /**
     * Clears any choices previously set.
     */
    public void clearChoices() {
        if (checkedStates != null) {
            checkedStates.clear();
        }

        if (checkedIdStates != null) {
            checkedIdStates.clear();
        }

        checkedCount = 0;
        updateOnScreenCheckedViews();
    }

    /**
     * Returns the current choice mode.
     *
     * @see #setChoiceMode(ChoiceMode)
     */
    public ChoiceMode getChoiceMode() {
        return choiceMode;
    }

    /**
     * Defines the choice behavior for the List. By default, Lists do not have any choice behavior
     * ({@link ChoiceMode#NONE}). By setting the choiceMode to {@link ChoiceMode#SINGLE}, the
     * List allows up to one item to  be in a chosen state. By setting the choiceMode to
     * {@link ChoiceMode#MULTIPLE}, the list allows any number of items to be chosen.
     *
     * @param choiceMode One of {@link ChoiceMode#NONE}, {@link ChoiceMode#SINGLE}, or
     * {@link ChoiceMode#MULTIPLE}
     */
    public void setChoiceMode(ChoiceMode choiceMode) {
        if (this.choiceMode == choiceMode) {
            return;
        }

        this.choiceMode = choiceMode;

        if (this.choiceMode != ChoiceMode.NONE) {
            if (checkedStates == null) {
                checkedStates = new CheckedStates();
            }

            Adapter adapter = recyclerView.getAdapter();
            if (checkedIdStates == null && adapter != null && adapter.hasStableIds()) {
                checkedIdStates = new CheckedIdStates();
            }
        }
    }

    public void onAdapterDataChanged() {
        Adapter adapter = recyclerView.getAdapter();
        if (choiceMode == ChoiceMode.NONE || adapter == null || !adapter.hasStableIds()) {
            return;
        }

        int itemCount = adapter.getItemCount();

        // Clear out the positional check states, we'll rebuild it below from IDs.
        checkedStates.clear();

        for (int checkedIndex = 0; checkedIndex < checkedIdStates.size(); checkedIndex++) {
            long currentId = checkedIdStates.keyAt(checkedIndex);
            int currentPosition = checkedIdStates.valueAt(checkedIndex);

            long newPositionId = adapter.getItemId(currentPosition);
            if (currentId != newPositionId) {
                // Look around to see if the ID is nearby. If not, uncheck it.
                int start = Math.max(0, currentPosition - CHECK_POSITION_SEARCH_DISTANCE);
                int end = Math.min(currentPosition + CHECK_POSITION_SEARCH_DISTANCE, itemCount);

                boolean found = false;
                for (int searchPos = start; searchPos < end; searchPos++) {
                    long searchId = adapter.getItemId(searchPos);
                    if (currentId == searchId) {
                        found = true;
                        checkedStates.put(searchPos, true);
                        checkedIdStates.setValueAt(checkedIndex, searchPos);
                        break;
                    }
                }

                if (!found) {
                    checkedIdStates.delete(currentId);
                    checkedCount--;
                    checkedIndex--;
                }
            } else {
                checkedStates.put(currentPosition, true);
            }
        }
    }

    public Bundle onSaveInstanceState() {
        Bundle state = new Bundle();

        state.putInt(STATE_KEY_CHOICE_MODE, choiceMode.ordinal());
        state.putParcelable(STATE_KEY_CHECKED_STATES, checkedStates);
        state.putParcelable(STATE_KEY_CHECKED_ID_STATES, checkedIdStates);
        state.putInt(STATE_KEY_CHECKED_COUNT, checkedCount);

        return state;
    }

    public void onRestoreInstanceState(Bundle state) {
        choiceMode = ChoiceMode.values()[state.getInt(STATE_KEY_CHOICE_MODE)];
        checkedStates = state.getParcelable(STATE_KEY_CHECKED_STATES);
        checkedIdStates = state.getParcelable(STATE_KEY_CHECKED_ID_STATES);
        checkedCount = state.getInt(STATE_KEY_CHECKED_COUNT);

        // TODO confirm ids here
    }

    public static SelectionSupport addTo(RecyclerView recyclerView) {
        SelectionSupport itemSelectionSupport = from(recyclerView);
        if (itemSelectionSupport == null) {
            itemSelectionSupport = new SelectionSupport(recyclerView);
            recyclerView.setTag(R.id.recyclerExt_item_selection_support, itemSelectionSupport);
        }

        return itemSelectionSupport;
    }

    public static void removeFrom(RecyclerView recyclerView) {
        SelectionSupport itemSelection = from(recyclerView);
        if (itemSelection == null) {
            return;
        }

        itemSelection.clearChoices();

        recyclerView.removeOnItemTouchListener(itemSelection.touchListener);
        recyclerView.setTag(R.id.recyclerExt_item_selection_support, null);
    }

    public static SelectionSupport from(RecyclerView recyclerView) {
        if (recyclerView == null) {
            return null;
        }

        return (SelectionSupport) recyclerView.getTag(R.id.recyclerExt_item_selection_support);
    }

    private static class CheckedStates extends SparseBooleanArray implements Parcelable {
        private static final int FALSE = 0;
        private static final int TRUE = 1;

        public CheckedStates() {
            super();
        }

        private CheckedStates(Parcel in) {
            int size = in.readInt();
            if (size > 0) {
                for (int i = 0; i < size; i++) {
                    int key = in.readInt();
                    boolean value = (in.readInt() == TRUE);
                    put(key, value);
                }
            }
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int flags) {
            int size = size();
            parcel.writeInt(size);

            for (int i = 0; i < size; i++) {
                parcel.writeInt(keyAt(i));
                parcel.writeInt(valueAt(i) ? TRUE : FALSE);
            }
        }

        public static final Parcelable.Creator<CheckedStates> CREATOR = new Parcelable.Creator<CheckedStates>() {
            @Override
            public CheckedStates createFromParcel(Parcel in) {
                return new CheckedStates(in);
            }

            @Override
            public CheckedStates[] newArray(int size) {
                return new CheckedStates[size];
            }
        };
    }

    private static class CheckedIdStates extends LongSparseArray<Integer> implements Parcelable {
        public CheckedIdStates() {
            super();
        }

        private CheckedIdStates(Parcel in) {
            int size = in.readInt();
            if (size > 0) {
                for (int i = 0; i < size; i++) {
                    long key = in.readLong();
                    int value = in.readInt();
                    put(key, value);
                }
            }
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int flags) {
            int size = size();
            parcel.writeInt(size);

            for (int i = 0; i < size; i++) {
                parcel.writeLong(keyAt(i));
                parcel.writeInt(valueAt(i));
            }
        }

        public static final Creator<CheckedIdStates> CREATOR = new Creator<CheckedIdStates>() {
            @Override
            public CheckedIdStates createFromParcel(Parcel in) {
                return new CheckedIdStates(in);
            }

            @Override
            public CheckedIdStates[] newArray(int size) {
                return new CheckedIdStates[size];
            }
        };
    }

    private class TouchListener extends ItemClickTouchListener {
        TouchListener(RecyclerView recyclerView) {
            super(recyclerView);
        }

        @Override
        protected boolean performItemClick(RecyclerView parent, View view, int position, long id) {
            Adapter adapter = recyclerView.getAdapter();
            boolean checkedStateChanged = false;

            if (choiceMode == ChoiceMode.MULTIPLE) {
                boolean checked = !checkedStates.get(position, false);
                checkedStates.put(position, checked);

                if (checkedIdStates != null && adapter.hasStableIds()) {
                    if (checked) {
                        checkedIdStates.put(adapter.getItemId(position), position);
                    } else {
                        checkedIdStates.delete(adapter.getItemId(position));
                    }
                }

                if (checked) {
                    checkedCount++;
                } else {
                    checkedCount--;
                }

                checkedStateChanged = true;
            } else if (choiceMode == ChoiceMode.SINGLE) {
                boolean checked = !checkedStates.get(position, false);
                if (checked) {
                    checkedStates.clear();
                    checkedStates.put(position, true);

                    if (checkedIdStates != null && adapter.hasStableIds()) {
                        checkedIdStates.clear();
                        checkedIdStates.put(adapter.getItemId(position), position);
                    }

                    checkedCount = 1;
                } else if (checkedStates.size() == 0 || !checkedStates.valueAt(0)) {
                    checkedCount = 0;
                }

                checkedStateChanged = true;
            }

            if (checkedStateChanged) {
                updateOnScreenCheckedViews();
            }

            return false;
        }

        @Override
        protected boolean performItemLongClick(RecyclerView parent, View view, int position, long id) {
            return true;
        }
    }
}