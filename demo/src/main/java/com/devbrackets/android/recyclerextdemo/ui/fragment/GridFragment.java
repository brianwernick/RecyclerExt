package com.devbrackets.android.recyclerextdemo.ui.fragment;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.devbrackets.android.recyclerext.adapter.RecyclerCursorAdapter;
import com.devbrackets.android.recyclerext.layoutmanager.AutoColumnGridLayoutManager;
import com.devbrackets.android.recyclerextdemo.R;
import com.devbrackets.android.recyclerextdemo.data.database.DBHelper;
import com.devbrackets.android.recyclerextdemo.data.database.ItemDAO;
import com.devbrackets.android.recyclerextdemo.ui.viewholder.GridViewHolder;

/**
 * An example of the {@link AutoColumnGridLayoutManager}
 */
public class GridFragment extends Fragment {
    private DBHelper dbHelper;
    private RecyclerView recyclerView;

    public static GridFragment newInstance() {
        return new GridFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recycler, container, false);
        recyclerView = (RecyclerView)view.findViewById(R.id.recyclerext_fragment_recycler);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //Makes sure the database is initialized and open for use
        dbHelper = new DBHelper(getActivity());
        setupRecyclerExt();
    }

    /**
     * Retrieves the cursor from the database, and sets the layout manager and adapter
     * on the RecyclerView.
     */
    private void setupRecyclerExt() {
        CursorAdapter cursorAdapter = new CursorAdapter(ItemDAO.findCursorAll(dbHelper.getWritableDatabase()));

        //Sets up the AutoColumnGridLayoutManager
        int width = getActivity().getResources().getDimensionPixelSize(R.dimen.grid_item_width);
        AutoColumnGridLayoutManager layoutManager = new AutoColumnGridLayoutManager(getActivity(), width);
        layoutManager.setMatchRowAndColumnSpacing(true);
        layoutManager.setMinEdgeSpacing(140); //This is a pixel value, normally you would retrieve the size similar to the width above
        layoutManager.setMinColumnSpacing(80); //This is a pixel value, normally you would retrieve the size similar to the width above
        layoutManager.setSpacingMethod(AutoColumnGridLayoutManager.SpacingMethod.ALL);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(cursorAdapter);
    }

    /**
     * The adapter that extends the {@link RecyclerCursorAdapter} to provide the
     * minimum number of methods to function
     */
    private class CursorAdapter extends RecyclerCursorAdapter<GridViewHolder> {
        public CursorAdapter(Cursor cursor) {
            super(cursor);
        }

        @Override
        public GridViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return GridViewHolder.newInstance(parent);
        }

        @Override
        public void onBindViewHolder(@NonNull GridViewHolder holder, @NonNull Cursor cursor, int position) {
            ItemDAO item = new ItemDAO(cursor);
            holder.setText(item.getText() != null ? item.getText() : "");
        }
    }
}
