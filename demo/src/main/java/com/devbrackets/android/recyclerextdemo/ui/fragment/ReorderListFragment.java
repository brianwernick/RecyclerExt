package com.devbrackets.android.recyclerextdemo.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.devbrackets.android.recyclerext.decoration.ReorderDecoration;
import com.devbrackets.android.recyclerextdemo.R;
import com.devbrackets.android.recyclerextdemo.ui.viewholder.SimpleDragItemViewHolder;


/**
 * A Fragment for demonstrating a vertical reorder adapter
 */
public class ReorderListFragment extends Fragment implements ReorderDecoration.ReorderListener {
    private RecyclerView recyclerView;
    private ListAdapter listAdapter;

    protected int orientation = LinearLayoutManager.VERTICAL;

    public static ReorderListFragment newInstance() {
        return new ReorderListFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recycler, container, false);

        recyclerView = view.findViewById(R.id.recyclerext_fragment_recycler);
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
        layoutManager.setOrientation(orientation);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(listAdapter);


        //Create the ReorderDecoration, set the drag handle, and register for notifications of reorder events
        ReorderDecoration reorderDecoration = new ReorderDecoration(recyclerView);
        reorderDecoration.setDragHandleId(R.id.simple_drag_item_handle);
        reorderDecoration.setOrientation(orientation == LinearLayoutManager.VERTICAL ? ReorderDecoration.LayoutOrientation.VERTICAL : ReorderDecoration.LayoutOrientation.HORIZONTAL);
        reorderDecoration.setReorderListener(this);


        //Register the decoration and the item touch listener to monitor during the reordering
        recyclerView.addItemDecoration(reorderDecoration);
        recyclerView.addOnItemTouchListener(reorderDecoration);
    }

    /**
     * The adapter that extends the {@link com.devbrackets.android.recyclerext.adapter.ListAdapter} to provide the
     * minimum number of methods to function.  Any adapter could be used here, but for simplicity the
     * List adapter was used
     */
    private class ListAdapter extends com.devbrackets.android.recyclerext.adapter.ListAdapter<SimpleDragItemViewHolder, String> {
        private LayoutInflater inflater;

        public ListAdapter(Context context) {
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            for (int i = 1; i < 5; i++) {
                add("Reorderable Item " + i);
            }
        }

        @Override
        public SimpleDragItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return SimpleDragItemViewHolder.newInstance(inflater, parent);
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
