package com.devbrackets.android.recyclerextdemo.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.devbrackets.android.recyclerext.adapter.ListAdapter;
import com.devbrackets.android.recyclerext.layoutmanager.AutoColumnGridLayoutManager;
import com.devbrackets.android.recyclerextdemo.R;
import com.devbrackets.android.recyclerextdemo.data.database.DBHelper;
import com.devbrackets.android.recyclerextdemo.data.database.ItemDAO;
import com.devbrackets.android.recyclerextdemo.ui.viewholder.GridViewHolder;

import java.util.List;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

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
        GridAdapter adapter = new GridAdapter(ItemDAO.findAll(dbHelper.getWritableDatabase()));

        //Sets up the AutoColumnGridLayoutManager
        int width = getActivity().getResources().getDimensionPixelSize(R.dimen.grid_item_width);
        AutoColumnGridLayoutManager layoutManager = new AutoColumnGridLayoutManager(getActivity(), width);
        layoutManager.setMatchSpacing(true);
        layoutManager.setMinEdgeSpacing(120); //This is a pixel value, normally you would retrieve the size similar to the width above
        layoutManager.setMinColumnSpacing(60); //This is a pixel value, normally you would retrieve the size similar to the width above
        layoutManager.setSpacingMethod(AutoColumnGridLayoutManager.SpacingMethod.ALL);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    /**
     * The adapter that extends the {@link com.devbrackets.android.recyclerext.adapter.ListAdapter} to provide the
     * minimum number of methods to function
     */
    private class GridAdapter extends ListAdapter<GridViewHolder, ItemDAO> {

        public GridAdapter(@Nullable List<ItemDAO> itemList) {
            super(itemList);
        }

        @Override
        public GridViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return GridViewHolder.newInstance(parent);
        }

        @Override
        public void onBindViewHolder(GridViewHolder holder, int position) {
            ItemDAO item = getItems().get(position);
            holder.setText(item.getText() != null ? item.getText() : "");
        }
    }
}
