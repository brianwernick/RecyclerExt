package com.devbrackets.android.recyclerextdemo.ui.viewholder;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.devbrackets.android.recyclerextdemo.R;

/**
 * A ViewHolder for the list_item_simple_text layout
 */
public class SimpleDragItemViewHolder extends RecyclerView.ViewHolder {
    private TextView textView;

    public static SimpleDragItemViewHolder newInstance(LayoutInflater inflater, ViewGroup parent) {
        View view = inflater.inflate(R.layout.list_item_simple_drag, parent, false);
        return new SimpleDragItemViewHolder(view);
    }

    public SimpleDragItemViewHolder(View itemView) {
        super(itemView);

        textView = itemView.findViewById(R.id.simple_drag_item_text_view);
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
