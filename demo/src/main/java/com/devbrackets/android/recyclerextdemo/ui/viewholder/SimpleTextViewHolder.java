package com.devbrackets.android.recyclerextdemo.ui.viewholder;

import android.support.annotation.ColorInt;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.devbrackets.android.recyclerextdemo.R;

/**
 * A ViewHolder for the simple_text_item layout
 */
public class SimpleTextViewHolder extends RecyclerView.ViewHolder {
    private TextView textView;

    public SimpleTextViewHolder(View itemView) {
        super(itemView);

        textView = (TextView)itemView.findViewById(R.id.simple_text_text_view);
    }

    public void setText(String text) {
        textView.setText(text);
    }

    public void setBackgroundColor(@ColorInt int color) {
        textView.setBackgroundColor(color);
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
