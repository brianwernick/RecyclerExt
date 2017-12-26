package com.devbrackets.android.recyclerext.adapter.delegate;

public interface DelegateApi<T> {

    /**
     * Retrieves the item associated with the <code>position</code>
     *
     * @param position The position to get the item for
     * @return The item in the <code>position</code>
     */
    T getItem(int position);

    int getItemViewType(int position);
}
