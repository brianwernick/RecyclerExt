package com.devbrackets.android.recyclerext.decoration.header;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;

/**
 * An observer to watch the adapter for changes so that we can update the
 * sticky header
 */
@SuppressWarnings("WeakerAccess")
public class StickyHeaderDataObserver extends RecyclerView.AdapterDataObserver {

    @NonNull
    protected UpdateListener updateListener;

    public StickyHeaderDataObserver(@NonNull UpdateListener updateListener) {
        this.updateListener = updateListener;
    }

    @Override
    public void onChanged() {
        updateListener.onUpdateStickyHeader();
    }

    public void onItemRangeChanged(int positionStart, int itemCount) {
        updateListener.onUpdateStickyHeader();
    }

    public void onItemRangeInserted(int positionStart, int itemCount) {
        updateListener.onUpdateStickyHeader();
    }

    public void onItemRangeRemoved(int positionStart, int itemCount) {
        updateListener.onUpdateStickyHeader();
    }

    public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
        updateListener.onUpdateStickyHeader();
    }
}
