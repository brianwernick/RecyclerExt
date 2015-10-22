package com.devbrackets.android.recyclerext.adapter.viewholder;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.HapticFeedbackConstants;
import android.view.View;

/**
 * A Simple ViewHolder that adds the ability to specify
 * listeners that have access to the ViewHolder instead
 * of a position when clicked
 */
public abstract class ClickableViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

    public interface OnClickListener {
        void onClick(ClickableViewHolder viewHolder);
    }

    public interface OnLongClickListener {
        boolean onLongClick(ClickableViewHolder viewHolder);
    }

    @Nullable
    private OnClickListener onClickListener;
    @Nullable
    private OnLongClickListener onLongClickListener;

    protected boolean performLongClickHapticFeedback = true;

    public ClickableViewHolder(View itemView) {
        super(itemView);
        initializeClickListener();
    }

    /**
     * Sets the click and long click listeners on the root
     * view (see {@link #itemView})
     */
    protected void initializeClickListener() {
        itemView.setOnClickListener(this);
        itemView.setOnLongClickListener(this);
    }

    /**
     * Enables or Disables haptic feedback on long clicks.  If a
     * long click listener has not been set with {@link #setOnLongClickListener(OnLongClickListener)}
     * then no haptic feedback will be performed.
     *
     * @param enabled True if the long click should perform a haptic feedback [default: true]
     */
    public void setHapticFeedbackEnabled(boolean enabled) {
        performLongClickHapticFeedback = enabled;
    }

    public void setOnClickListener(@Nullable OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public void setOnLongClickListener(@Nullable OnLongClickListener onLongClickListener) {
        this.onLongClickListener = onLongClickListener;
    }

    @Override
    public void onClick(View view) {
        if (onClickListener != null) {
            onClickListener.onClick(this);
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (onLongClickListener != null) {
            if (performLongClickHapticFeedback) {
                itemView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            }

            return onLongClickListener.onLongClick(this);
        }

        return false;
    }
}