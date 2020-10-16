package com.devbrackets.android.recyclerextdemo.ui.viewholder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.devbrackets.android.recyclerextdemo.R

class GridViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val textView: TextView
    fun setText(text: CharSequence?) {
        textView.text = text
    }

    companion object {
        fun newInstance(parent: ViewGroup): GridViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.grid_item_simple, parent, false)
            return GridViewHolder(view)
        }
    }

    init {
        textView = itemView.findViewById(R.id.text)
    }
}