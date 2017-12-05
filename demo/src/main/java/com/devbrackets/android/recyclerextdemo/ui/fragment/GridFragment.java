package com.devbrackets.android.recyclerextdemo.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.devbrackets.android.recyclerext.adapter.RecyclerListAdapter;
import com.devbrackets.android.recyclerext.layoutmanager.AutoColumnGridLayoutManager;
import com.devbrackets.android.recyclerextdemo.R;
import com.devbrackets.android.recyclerextdemo.data.database.DBHelper;
import com.devbrackets.android.recyclerextdemo.data.database.ItemDAO;
import com.devbrackets.android.recyclerextdemo.ui.viewholder.GridViewHolder;

import java.util.List;

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
        recyclerView = view.findViewById(R.id.recyclerext_fragment_recycler);
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
        ListAdapter adapter = new ListAdapter(ItemDAO.findAll(dbHelper.getWritableDatabase()));

        //Sets up the AutoColumnGridLayoutManager
        int width = getActivity().getResources().getDimensionPixelSize(R.dimen.grid_item_width);
        AutoColumnGridLayoutManager layoutManager = new AutoColumnGridLayoutManager(getActivity(), width);
        layoutManager.setMatchRowAndColumnSpacing(true);
        layoutManager.setMinEdgeSpacing(140); //This is a pixel value, normally you would retrieve the size similar to the width above
        layoutManager.setMinColumnSpacing(80); //This is a pixel value, normally you would retrieve the size similar to the width above
        layoutManager.setSpacingMethod(AutoColumnGridLayoutManager.SpacingMethod.ALL);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    /**
     * The adapter that extends the {@link RecyclerListAdapter} to provide the
     * minimum number of methods to function
     */
    private class ListAdapter extends RecyclerListAdapter<GridViewHolder, ItemDAO> {

        public ListAdapter(@Nullable List<ItemDAO> itemList) {
            super(itemList);
        }

        @Override
        public GridViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return GridViewHolder.newInstance(parent);
        }

        @Override
        public void onBindViewHolder(GridViewHolder holder, int position) {
            ItemDAO item = items.get(position);
            holder.setText(item.getText() != null ? item.getText() : "");
        }
    }
}
