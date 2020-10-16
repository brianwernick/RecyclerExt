package com.devbrackets.android.recyclerextdemo.ui.viewholder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.devbrackets.android.recyclerextdemo.R

/**
 * A ViewHolder for the list_item_contacts_header layout
 */
class ContactsHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val textView: TextView
    private val regionTextView: TextView
    fun setText(text: String?) {
        textView.text = text
    }

    fun setRegionText(text: String?) {
        regionTextView.text = text
    }

    companion object {
        fun newInstance(inflater: LayoutInflater, parent: ViewGroup?): ContactsHeaderViewHolder {
            val view = inflater.inflate(R.layout.list_item_contacts_header, parent, false)
            return ContactsHeaderViewHolder(view)
        }
    }

    init {
        textView = itemView.findViewById(R.id.contacts_header_item_text_view)
        regionTextView = itemView.findViewById(R.id.contacts_header_item_region_text_view)
    }
}