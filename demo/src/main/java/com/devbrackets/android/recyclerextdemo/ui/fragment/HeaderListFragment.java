package com.devbrackets.android.recyclerextdemo.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.devbrackets.android.recyclerext.adapter.HeaderListAdapter;
import com.devbrackets.android.recyclerext.decoration.StickyHeaderDecoration;
import com.devbrackets.android.recyclerext.widget.FastScroll;
import com.devbrackets.android.recyclerextdemo.R;
import com.devbrackets.android.recyclerextdemo.data.database.DBHelper;
import com.devbrackets.android.recyclerextdemo.data.database.ItemDAO;
import com.devbrackets.android.recyclerextdemo.ui.viewholder.SimpleTextViewHolder;

import java.util.List;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


/**
 * An example of the {@link HeaderAdapter}
 * and using the {@link StickyHeaderDecoration} to keep the header at the top of the screen when reached.
 */
public class HeaderListFragment extends Fragment {
    private DBHelper dbHelper;
    private RecyclerView recyclerView;
    private ViewGroup parent;
    private FastScroll fastScroll;

    public static HeaderListFragment newInstance() {
        return new HeaderListFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recycler, container, false);
        parent = view.findViewById(R.id.parent);
        recyclerView = view.findViewById(R.id.recyclerext_fragment_recycler);
        fastScroll = view.findViewById(R.id.recyclerext_fast_scroll);
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
     * Retrieves the items from the database, and sets the layout manager, adapter, and sticky decoration
     * on the RecyclerView.
     */
    private void setupRecyclerExt() {
        HeaderAdapter adapter = new HeaderAdapter(getActivity(), ItemDAO.findAll(dbHelper.getWritableDatabase()));

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        fastScroll.attach(recyclerView);

        //OPTIONAL: The StickyHeaderDecoration is used to keep the current header always visible
        StickyHeaderDecoration decoration = new StickyHeaderDecoration(recyclerView);
        decoration.enableStickyHeaderTouches(parent);
        recyclerView.addItemDecoration(decoration);
    }

    /**
     * The adapter that extends the {@link com.devbrackets.android.recyclerext.adapter.HeaderAdapter} to provide the
     * minimum number of methods to function
     */
    private class HeaderAdapter extends HeaderListAdapter<SimpleTextViewHolder, SimpleTextViewHolder, ItemDAO> implements FastScroll.PopupCallbacks {
        private LayoutInflater inflater;

        public HeaderAdapter(Context context, List<ItemDAO> items) {
            super();

            setItems(items);
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @NonNull
        @Override
        public SimpleTextViewHolder onCreateHeaderViewHolder(@NonNull ViewGroup parent, int viewType) {
            SimpleTextViewHolder holder = SimpleTextViewHolder.newInstance(inflater, parent);
            holder.setOnClickListener(viewHolder -> viewHolder.itemView.setBackgroundColor(0xff44aacc));
            return holder;
        }

        @NonNull
        @Override
        public SimpleTextViewHolder onCreateChildViewHolder(@NonNull ViewGroup parent, int viewType) {
            return SimpleTextViewHolder.newInstance(inflater, parent);
        }

        @Override
        public void onBindHeaderViewHolder(@NonNull SimpleTextViewHolder holder, int childPosition) {
            holder.setText(getHeaderId(childPosition) + "0s");
            holder.setBackgroundColor(0xFFCCCCCC);
        }

        @Override
        public void onBindChildViewHolder(@NonNull SimpleTextViewHolder holder, int childPosition) {
            holder.setText(getItems().get(childPosition).getText());
        }

        /**
         * For simplicity sake, we just return a simple mathematical id for the headers.
         * You should provide an actual id.
         */
        @Override
        public long getHeaderId(int childPosition) {
            return (getItems().get(childPosition).getOrder() + 1) / 10;
        }

        @Override
        public long getSectionId(@IntRange(from = 0) int position) {
            return (long) ((getChildPosition(position) + 1) / 10);
        }

        @NonNull
        @Override
        public String getPopupText(int position, long sectionId) {
            return sectionId + "0s";
        }
    }
}
