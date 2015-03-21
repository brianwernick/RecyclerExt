package com.devbrackets.android.recyclerextdemo;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.devbrackets.android.recyclerext.adapter.ReorderableRecyclerCursorAdapter;
import com.devbrackets.android.recyclerext.decoration.ReorderDecoration;
import com.devbrackets.android.recyclerextdemo.database.DBHelper;
import com.devbrackets.android.recyclerextdemo.database.ItemDAO;
import com.devbrackets.android.recyclerextdemo.task.OrderUpdateTask;
import com.devbrackets.android.recyclerextdemo.viewholder.SimpleDragItemViewHolder;


/**
 * An example for using the ReorderRecyclerCursorAdapter
 */
public class ReorderCursorFragment extends Fragment implements ReorderDecoration.ReorderListener, OrderUpdateTask.DBUpdateListener {
    private DBHelper dbHelper;
    private RRCursorAdapter cursorAdapter;
    private RecyclerView recyclerView;

    private boolean isUpdateRunning = false; //A simple way to enforce only 1 DB update is running at a time

    public static ReorderCursorFragment newInstance() {
        return new ReorderCursorFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.recycler_fragment, container, false);
        recyclerView = (RecyclerView)view.findViewById(R.id.recyclerext_fragment_recycler);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        dbHelper = new DBHelper(getActivity());
        setupRecyclerExt();
    }

    //Called from the ReorderListener
    @Override
    public void onItemReordered(int originalPosition, int newPosition) {
        //This is called when the item has been dropped at the new location.  Since the ReorderDecoration only takes care
        // of the visual aspect and calculating the new position, we will need to inform the adapter ourselves.

        //onItemReordered can still be called if the user drops the item in the same position (It won't be called if the reorder was canceled)
        if (originalPosition == newPosition) {
            return;
        }

        //Inform the adapter that the data changed
        cursorAdapter.reorderItem(originalPosition, newPosition);
        //performDatabaseOrderUpdate();
    }

    //Called from the DBUpdateListener
    @Override
    public void onDBUpdated(Cursor cursor) {
        isUpdateRunning = false;
        cursorAdapter.changeCursor(cursor);
        cursorAdapter.removeOldestReorderItem();
        performDatabaseOrderUpdate();
    }

    private void setupRecyclerExt() {
        cursorAdapter = new RRCursorAdapter(getActivity(), ItemDAO.findCursorAll(dbHelper.getWritableDatabase()));

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(cursorAdapter);


        //Create the ReorderDecoration, set the drag handle, and register for notifications of reorder events
        ReorderDecoration reorderDecoration = new ReorderDecoration(recyclerView);
        reorderDecoration.setDragHandleId(R.id.simple_drag_item_handle);
        reorderDecoration.setReorderListener(this);

        //Register the decoration and the item touch listener to monitor during the reordering
        recyclerView.addItemDecoration(reorderDecoration);
        recyclerView.addOnItemTouchListener(reorderDecoration);
    }

    private void performDatabaseOrderUpdate() {
        //Saves the new order to the database (so add a new column for order)
        ReorderableRecyclerCursorAdapter.ReorderItem item = cursorAdapter.getOldestReorderItem();

        if (!isUpdateRunning && item != null) {
            isUpdateRunning = true;
            new OrderUpdateTask(dbHelper.getWritableDatabase(), item, this).execute();
        }
    }

















    private class RRCursorAdapter extends ReorderableRecyclerCursorAdapter<SimpleDragItemViewHolder> {
        private LayoutInflater inflater;

        public RRCursorAdapter(Context context, Cursor cursor) {
            super(cursor);
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public SimpleDragItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = inflater.inflate(R.layout.simple_drag_item, null);
            return new SimpleDragItemViewHolder(view);
        }

        @Override
        public void onBindViewHolder(SimpleDragItemViewHolder holder, Cursor cursor, int position) {
            ItemDAO item = new ItemDAO(cursor);
            holder.setText(item.getText() != null ? item.getText() : "");
            holder.setPosition(position);
        }

        @Override
        public void onContentChanged() {
            //Purposefully left blank
        }
    }
}
