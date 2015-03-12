package com.devbrackets.android.recyclerextdemo.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.devbrackets.android.recyclerextdemo.R;

/**
 * A ViewHolder for the simple_text_item layout
 */
public class SimpleDragItemViewHolder extends RecyclerView.ViewHolder {
    private TextView textView;

    public SimpleDragItemViewHolder(View itemView) {
        super(itemView);

        textView = (TextView)itemView.findViewById(R.id.simple_drag_item_text_view);
    }

    public void setText(String text) {
        textView.setText(text);
    }

    public void setPosition(int position) {
        textView.setTag(position);
    }

    public void setOnClickListener(View.OnClickListener listener) {
        textView.setOnClickListener(listener);
    }

    public void setOnLongClickListener(View.OnLongClickListener listener) {
        textView.setOnLongClickListener(listener);
    }

    public void setOnTouchListener(View.OnTouchListener listener) {
        textView.setOnTouchListener(listener);
    }
}
