package com.devbrackets.android.recyclerext.decoration.header;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;

/**
 * Listens to the scroll events for the RecyclerView that will have
 * sticky headers.  When a new header reaches the start it will be
 * transformed in to a sticky view and attached to the start of the
 * RecyclerView.  Additionally, when a new header is reaching the
 * start, the headers will be transitioned smoothly
 */
@SuppressWarnings("WeakerAccess")
public class StickyHeaderScrollListener extends RecyclerView.OnScrollListener {
    @NonNull
    protected UpdateListener updateListener;

    public StickyHeaderScrollListener(@NonNull UpdateListener updateListener) {
        this.updateListener = updateListener;
    }

    @Override
    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        updateListener.onUpdateStickyHeader();
    }
}
