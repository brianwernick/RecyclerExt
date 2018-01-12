package com.devbrackets.android.recyclerext.decoration.header;

import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.devbrackets.android.recyclerext.adapter.header.HeaderApi;

@SuppressWarnings("WeakerAccess")
public class StickyHeader {
    @Nullable
    protected RecyclerView.ViewHolder stickyViewHolder;
    @Nullable
    protected View cachedStickyView;
    @NonNull
    public PointF stickyViewOffset = new PointF(0, 0);
    public long currentStickyId = RecyclerView.NO_ID;

    public void reset() {
        update(RecyclerView.NO_ID, null);

        stickyViewOffset.x = 0;
        stickyViewOffset.y = 0;
    }

    public void update(long stickyId, @Nullable RecyclerView.ViewHolder holder) {
        stickyViewHolder = holder;
        cachedStickyView = null;
        currentStickyId = stickyId;
    }

    @Nullable
    public View getStickyView(@NonNull HeaderApi headerApi) {
        if (cachedStickyView != null) {
            return cachedStickyView;
        }

        RecyclerView.ViewHolder holder = stickyViewHolder;
        if (holder == null) {
            return null;
        }

        // If we have a ViewHolder we should have a view, but just to be safe we check
        int stickyViewId = headerApi.getCustomStickyHeaderViewId();
        cachedStickyView = stickyViewId != 0 ? holder.itemView.findViewById(stickyViewId) : holder.itemView;
        return cachedStickyView;
    }
}
