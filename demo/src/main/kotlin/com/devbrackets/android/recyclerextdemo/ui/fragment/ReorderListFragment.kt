package com.devbrackets.android.recyclerextdemo.ui.fragment

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devbrackets.android.recyclerext.decoration.ReorderDecoration
import com.devbrackets.android.recyclerext.decoration.ReorderDecoration.ReorderListener
import com.devbrackets.android.recyclerextdemo.R
import com.devbrackets.android.recyclerextdemo.ui.fragment.shared.BaseFragment
import com.devbrackets.android.recyclerextdemo.ui.viewholder.SimpleDragItemViewHolder

/**
 * A Fragment for demonstrating a vertical reorder adapter
 */
open class ReorderListFragment: BaseFragment(), ReorderListener {
    private var listAdapter: ListAdapter? = null
    protected var orientation = RecyclerView.VERTICAL

    override fun onSetupRecyclerView() {
        //Setup the standard Layout and Adapter
        listAdapter = ListAdapter(requireContext())
        val layoutManager = LinearLayoutManager(activity)
        layoutManager.orientation = orientation
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = listAdapter


        //Create the ReorderDecoration, set the drag handle, and register for notifications of reorder events
        val reorderDecoration = ReorderDecoration(recyclerView)
        reorderDecoration.setDragHandleId(R.id.simple_drag_item_handle)
        reorderDecoration.orientation = if (orientation == LinearLayoutManager.VERTICAL) ReorderDecoration.LayoutOrientation.VERTICAL else ReorderDecoration.LayoutOrientation.HORIZONTAL
        reorderDecoration.setReorderListener(this)


        //Register the decoration and the item touch listener to monitor during the reordering
        recyclerView.addItemDecoration(reorderDecoration)
        recyclerView.addOnItemTouchListener(reorderDecoration)
    }


    override fun onItemReordered(originalPosition: Int, newPosition: Int) {
        //This is called when the item has been dropped at the new location.  Since the ReorderDecoration only takes care
        // of the visual aspect and calculating the new position, we will need to inform the adapter ourselves.

        //onItemReordered can still be called if the user drops the item in the same position (It won't be called if the reorder was canceled)
        if (originalPosition == newPosition) {
            return
        }

        //Inform the adapter that the data changed
        listAdapter!!.reorderItem(originalPosition, newPosition)
    }

    override fun onItemPostReordered(originalPosition: Int, newPosition: Int) {
        // nothing
    }

    /**
     * The adapter that extends the [com.devbrackets.android.recyclerext.adapter.ListAdapter] to provide the
     * minimum number of methods to function.  Any adapter could be used here, but for simplicity the
     * List adapter was used
     */
    private inner class ListAdapter(context: Context) : com.devbrackets.android.recyclerext.adapter.ListAdapter<String?, SimpleDragItemViewHolder>() {
        private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleDragItemViewHolder {
            return SimpleDragItemViewHolder.newInstance(inflater, parent)
        }

        override fun onBindViewHolder(holder: SimpleDragItemViewHolder, position: Int) {
            holder.setText(getItem(position))
            holder.position = position
        }

        fun reorderItem(originalPosition: Int, newPosition: Int) {
            //Make sure the positions aren't the same
            if (originalPosition == newPosition) {
                return
            }

            //Make sure the positions aren't out of bounds
            if (originalPosition < 0 || newPosition < 0 || originalPosition >= itemCount || newPosition >= itemCount) {
                return
            }

            //Perform the update
            val temp = getItem(originalPosition)
            remove(originalPosition)
            add(newPosition, temp)

            //Make sure the view reflects this change
            notifyDataSetChanged()
        }

        init {
            for (i in 1..4) {
                add("Reorderable Item $i")
            }
        }
    }
}