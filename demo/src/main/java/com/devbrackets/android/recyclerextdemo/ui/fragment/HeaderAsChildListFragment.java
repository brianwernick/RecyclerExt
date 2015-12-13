package com.devbrackets.android.recyclerextdemo.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.devbrackets.android.recyclerext.adapter.RecyclerHeaderAdapter;
import com.devbrackets.android.recyclerext.decoration.StickyHeaderDecoration;
import com.devbrackets.android.recyclerextdemo.R;
import com.devbrackets.android.recyclerextdemo.data.database.DBHelper;
import com.devbrackets.android.recyclerextdemo.data.database.ItemDAO;
import com.devbrackets.android.recyclerextdemo.ui.viewholder.SimpleTextViewHolder;

import java.util.List;


/**
 * An example of the {@link HeaderAsChildListFragment.HeaderAdapter}
 * that has the display style of the Lollipop and Marshmallow Contacts app
 * using the {@link StickyHeaderDecoration} to keep the header at the top of the screen when reached.
 */
public class HeaderAsChildListFragment extends Fragment {
    private DBHelper dbHelper;
    private RecyclerView recyclerView;

    public static HeaderAsChildListFragment newInstance() {
        return new HeaderAsChildListFragment();
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

        //OPTIONAL: The StickyHeaderDecoration is used to keep the current header always visible
        recyclerView.addItemDecoration(new StickyHeaderDecoration(recyclerView));
    }

    /**
     * The adapter that extends the {@link RecyclerHeaderAdapter} to provide the
     * minimum number of methods to function
     */
    private class HeaderAdapter extends RecyclerHeaderAdapter<SimpleTextViewHolder, SimpleTextViewHolder> {
        private LayoutInflater inflater;
        private List<ItemDAO> items;

        public HeaderAdapter(Context context, List<ItemDAO> items) {
            this.items = items;
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            //This is the call that makes the adapter treat the headers position as a child
            // e.g. CHILD(position=9, getItem(9)), HEADER(position=10, getItem(10)), CHILD(position=11, getItem(11))
            // whereas normally the header doesn't interfere with the child items
            // e.g. CHILD(position=9, getItem(9)), HEADER(position=10, getItem(10)), CHILD(position=11, getItem(10))
            showHeaderAsChild(true);
        }

        @Override
        public SimpleTextViewHolder onCreateHeaderViewHolder(ViewGroup parent, int viewType) {
            View view = inflater.inflate(R.layout.simple_text_item, parent, false);
            return new SimpleTextViewHolder(view);
        }

        @Override
        public SimpleTextViewHolder onCreateChildViewHolder(ViewGroup parent, int viewType) {
            View view = inflater.inflate(R.layout.simple_text_item, parent, false);
            return new SimpleTextViewHolder(view);
        }

        @Override
        public void onBindHeaderViewHolder(SimpleTextViewHolder holder, int childPosition) {
            holder.setText(items.get(childPosition).getText());
            holder.setBackgroundColor(0xFF33FF77);
        }

        @Override
        public void onBindChildViewHolder(SimpleTextViewHolder holder, int childPosition) {
            holder.setText(items.get(childPosition).getText());
        }

        @Override
        public int getChildCount() {
            return items.size();
        }

        /**
         * For simplicity sake, we just return a simple mathematical id for the headers.
         * You should provide an actual id.
         */
        @Override
        public long getHeaderId(int childPosition) {
            return items.get(childPosition).getOrder() / 10;
        }
    }
}
