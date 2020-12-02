package com.devbrackets.android.recyclerextdemo.ui.viewholder

import android.view.LayoutInflater
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.devbrackets.android.recyclerextdemo.R

/**
 * A ViewHolder for the list_item_simple_text layout
 */
class SimpleDragItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val textView: TextView
    fun setText(text: String?) {
        textView.text = text
    }

    fun setPosition(position: Int) {
        textView.tag = position
    }

    fun setOnClickListener(listener: View.OnClickListener?) {
        textView.setOnClickListener(listener)
    }

    fun setOnLongClickListener(listener: View.OnLongClickListener?) {
        textView.setOnLongClickListener(listener)
    }

    fun setOnTouchListener(listener: OnTouchListener?) {
        textView.setOnTouchListener(listener)
    }

    companion object {
        fun newInstance(inflater: LayoutInflater, parent: ViewGroup?): SimpleDragItemViewHolder {
            val view = inflater.inflate(R.layout.list_item_simple_drag, parent, false)
            return SimpleDragItemViewHolder(view)
        }
    }

    init {
        textView = itemView.findViewById(R.id.simple_drag_item_text_view)
    }
}