package com.devbrackets.android.recyclerextdemo.ui.viewholder;

import android.support.annotation.ColorInt;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.devbrackets.android.recyclerextdemo.R;

/**
 * A ViewHolder for the list_item_simple_text layout
 */
public class SimpleTextViewHolder extends RecyclerView.ViewHolder {
    private TextView textView;
    private View spacing;

    public static SimpleTextViewHolder newInstance(LayoutInflater inflater, ViewGroup parent) {
        View view = inflater.inflate(R.layout.list_item_simple_text, parent, false);
        return new SimpleTextViewHolder(view);
    }

    public SimpleTextViewHolder(View itemView) {
        super(itemView);

        textView = (TextView)itemView.findViewById(R.id.simple_text_text_view);
        spacing = itemView.findViewById(R.id.simple_text_spacing);
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

    public void setSpacingVisible(boolean visible) {
        spacing.setVisibility(visible ? View.VISIBLE : View.GONE);
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
