package com.devbrackets.android.recyclerextdemo.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devbrackets.android.recyclerext.adapter.viewholder.ClickableViewHolder
import com.devbrackets.android.recyclerext.decoration.ReorderDecoration
import com.devbrackets.android.recyclerextdemo.R
import com.devbrackets.android.recyclerextdemo.data.Example
import com.devbrackets.android.recyclerextdemo.ui.viewholder.SimpleTextViewHolder

/**
 * An activity that lists the example items.
 */
class MainActivity : AppCompatActivity() {
    private var recyclerView: RecyclerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        recyclerView = findViewById<View>(R.id.main_activity_recycler) as RecyclerView
        setupRecyclerExt()
    }

    private fun setupRecyclerExt() {
        recyclerView!!.layoutManager = LinearLayoutManager(this)
        recyclerView!!.adapter = ListAdapter(this)

        val reorderDecoration = ReorderDecoration(recyclerView!!)
        reorderDecoration.setDragHandleId(R.id.simple_drag_item_handle)

        recyclerView!!.addItemDecoration(reorderDecoration)
        recyclerView!!.addOnItemTouchListener(reorderDecoration)
        recyclerView!!.itemAnimator = null
    }

    private fun startFragmentActivity(fragmentType: Example?) {
        val intent = Intent(this, SingleFragmentActivity::class.java)
        intent.putExtra(SingleFragmentActivity.Companion.EXTRA_FRAGMENT_TYPE, fragmentType)
        startActivity(intent)
    }

    /**
     * A simple [com.devbrackets.android.recyclerext.adapter.ListAdapter] to display the options for the examples
     */
    private inner class ListAdapter(context: Context) : com.devbrackets.android.recyclerext.adapter.ListAdapter<SimpleTextViewHolder, Example?>(), ClickableViewHolder.OnClickListener {
        private val inflater: LayoutInflater
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleTextViewHolder {
            return SimpleTextViewHolder.newInstance(inflater, parent)
        }

        override fun onBindViewHolder(holder: SimpleTextViewHolder, position: Int) {
            holder.setText(getItem(position)?.title)
            holder.setOnClickListener(this)
        }

        override fun onClick(holder: ClickableViewHolder) {
            startFragmentActivity(getItem(holder.adapterPosition))
        }

        init {
            inflater = context.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater

            //Adds all the items from the Example enum
            for (example in Example.values()) {
                add(example)
            }
        }
    }
}