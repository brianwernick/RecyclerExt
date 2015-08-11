package com.devbrackets.android.recyclerextdemo;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.devbrackets.android.recyclerext.adapter.RecyclerListAdapter;
import com.devbrackets.android.recyclerext.decoration.ReorderDecoration;
import com.devbrackets.android.recyclerextdemo.viewholder.SimpleDragItemViewHolder;


/**
 * A Fragment for demonstrating a reorderable horizontal scrolling view
 */
public class ReorderListFragmentHorizontal extends Fragment implements ReorderDecoration.ReorderListener {
    private RecyclerView recyclerView;
    private ListAdapter listAdapter;

    public static ReorderListFragmentHorizontal newInstance() {
        return new ReorderListFragmentHorizontal();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.recycler_fragment, container, false);

        recyclerView = (RecyclerView)view.findViewById(R.id.recyclerext_fragment_recycler);
        setupRecyclerExt();

        return view;
    }

    @Override
    public void onItemReordered(int originalPosition, int newPosition) {
        //This is called when the item has been dropped at the new location.  Since the ReorderDecoration only takes care
        // of the visual aspect and calculating the new position, we will need to inform the adapter ourselves.

        //onItemReordered can still be called if the user drops the item in the same position (It won't be called if the reorder was canceled)
        if (originalPosition == newPosition) {
            return;
        }

        //Inform the adapter that the data changed
        listAdapter.reorderItem(originalPosition, newPosition);
    }

    @Override
    public void onItemPostReordered(int originalPosition, int newPosition) {
        // nothing
    }

    private void setupRecyclerExt() {
        //Setup the standard Layout and Adapter
        listAdapter = new ListAdapter(getActivity());

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(listAdapter);


        //Create the ReorderDecoration, set the drag handle, and register for notifications of reorder events
        ReorderDecoration reorderDecoration = new ReorderDecoration(recyclerView);
        reorderDecoration.setOrientation(ReorderDecoration.LayoutOrientation.HORIZONTAL);
        reorderDecoration.setDragHandleId(R.id.simple_drag_item_handle);
        reorderDecoration.setReorderListener(this);


        //Register the decoration and the item touch listener to monitor during the reordering
        recyclerView.addItemDecoration(reorderDecoration);
        recyclerView.addOnItemTouchListener(reorderDecoration);
    }


















    private class ListAdapter extends RecyclerListAdapter<SimpleDragItemViewHolder, String> {
        private LayoutInflater inflater;

        public ListAdapter(Context context) {
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            for (int i = 1; i < 5; i++) {
                add("R. Item " + i);
            }
        }

        @Override
        public SimpleDragItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = inflater.inflate(R.layout.simple_drag_item, parent, false);

            return new SimpleDragItemViewHolder(view);
        }

        @Override
        public void onBindViewHolder(SimpleDragItemViewHolder holder, int position) {
            holder.setText(getItem(position));
            holder.setPosition(position);
        }

        public void reorderItem(int originalPosition, int newPosition) {
            //Make sure the positions aren't the same
            if (originalPosition == newPosition) {
                return;
            }

            //Make sure the positions aren't out of bounds
            if (originalPosition < 0 || newPosition < 0 || originalPosition >= getItemCount() || newPosition >= getItemCount()) {
                return;
            }

            //Perform the update
            String temp = getItem(originalPosition);
            remove(originalPosition);
            add(newPosition, temp);

            //Make sure the view reflects this change
            notifyDataSetChanged();
        }
    }
}
