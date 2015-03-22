package com.devbrackets.android.recyclerext.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import static android.support.v7.widget.RecyclerView.ViewHolder;

/**
 * A RecyclerView adapter that adds support for dynamically placing headers in the view.
 *
 * @param <H> The Header {@link RecyclerView.ViewHolder}
 * @param <C> The Child or content {@link RecyclerView.ViewHolder}
 */
public abstract class HeaderAdapter<H extends ViewHolder, C extends ViewHolder> extends RecyclerView.Adapter<ViewHolder> {
    public static final int VIEW_TYPE_CHILD = 1;
    public static final int VIEW_TYPE_HEADER = 10;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_CHILD) {
            return onCreateChildViewHolder(parent);
        } else if (viewType == VIEW_TYPE_HEADER) {
            return onCreateHeaderViewHolder(parent);
        }

        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onBindViewHolder(ViewHolder holder, int position) {
        int viewType = getItemViewType(position);

        if (viewType == VIEW_TYPE_CHILD) {
            onBindChildViewHolder((C)holder, determineChildPosition(position));
        } else if (viewType == VIEW_TYPE_HEADER) {
            onBindHeaderViewHolder((H)holder, determineChildPosition(position));
        }
    }

    @Override
    public int getItemViewType(int position) {
        //TODO: figure out how to switch between the header and child views.
        return VIEW_TYPE_CHILD;
    }

    /**
     * Returns the total number of items in the data set hold by the adapter.
     * <br />
     * <b>NOTE:</b> {@link #getChildCount()} should be overridden instead of this method
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return getChildCount() + determineHeaderCount();
    }

    /**
     * Return the stable ID for the header at <code>childPosition</code>. The default implementation
     * of this method returns {@link RecyclerView#NO_ID}
     *
     * @param childPosition The Adapters child position
     * @return the stable ID of the header at childPosition
     */
    public long getHeaderId(int childPosition) {
        return RecyclerView.NO_ID;
    }

    public abstract H onCreateHeaderViewHolder(ViewGroup parent);
    public abstract C onCreateChildViewHolder(ViewGroup parent);

    public abstract void onBindHeaderViewHolder(H holder, int childPosition);
    public abstract void onBindChildViewHolder(C holder, int childPosition);

    /**
     * Returns the total number of children in the data set held by the adapter.
     *
     * @return The total number of children in this adapter.
     */
    public abstract int getChildCount();





    private int determineChildPosition(int position) {
        //TODO: Determine the position for the child, based on the raw recyclerView position that
        // includes the headers.

        return position;
    }

    private int determineHeaderCount() {
        //TODO:
        return 0;
    }
}
