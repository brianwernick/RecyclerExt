package com.devbrackets.android.recyclerextdemo.ui.viewholder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.ColorInt
import com.devbrackets.android.recyclerext.adapter.viewholder.ClickableViewHolder
import com.devbrackets.android.recyclerextdemo.R

/**
 * A ViewHolder for the list_item_simple_text layout
 */
class SimpleTextViewHolder(itemView: View) : ClickableViewHolder(itemView) {
    private val textView: TextView
    private val spacing: View
    fun setText(text: String?) {
        textView.text = text
    }

    fun setBackgroundColor(@ColorInt color: Int) {
        itemView.setBackgroundColor(color)
    }

    fun setSpacingVisible(visible: Boolean) {
        spacing.visibility = if (visible) View.VISIBLE else View.GONE
    }

    companion object {
        fun newInstance(inflater: LayoutInflater, parent: ViewGroup?): SimpleTextViewHolder {
            val view = inflater.inflate(R.layout.list_item_simple_text, parent, false)
            return SimpleTextViewHolder(view)
        }
    }

    init {
        textView = itemView.findViewById(R.id.simple_text_text_view)
        spacing = itemView.findViewById(R.id.simple_text_spacing)
    }
}