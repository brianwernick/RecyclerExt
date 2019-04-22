package com.devbrackets.android.recyclerextdemo.ui.viewholder;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.devbrackets.android.recyclerextdemo.R;

public class GridViewHolder extends RecyclerView.ViewHolder {

    private TextView textView;

    public static GridViewHolder newInstance(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_item_simple, parent, false);
        return new GridViewHolder(view);
    }

    public GridViewHolder(View itemView) {
        super(itemView);
        textView = itemView.findViewById(R.id.text);
    }

    public void setText(@Nullable CharSequence text) {
        textView.setText(text);
    }
}
