package com.devbrackets.android.recyclerextdemo.ui.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.devbrackets.android.recyclerextdemo.R;

/**
 * A ViewHolder for the list_item_contacts_header layout
 */
public class ContactsHeaderViewHolder extends RecyclerView.ViewHolder {
    private TextView textView;
    private TextView regionTextView;

    public static ContactsHeaderViewHolder newInstance(LayoutInflater inflater, ViewGroup parent) {
        View view = inflater.inflate(R.layout.list_item_contacts_header, parent, false);
        return new ContactsHeaderViewHolder(view);
    }

    public ContactsHeaderViewHolder(View itemView) {
        super(itemView);

        textView = itemView.findViewById(R.id.contacts_header_item_text_view);
        regionTextView = itemView.findViewById(R.id.contacts_header_item_region_text_view);
    }

    public void setText(String text) {
        textView.setText(text);
    }

    public void setRegionText(String text) {
        regionTextView.setText(text);
    }
}
