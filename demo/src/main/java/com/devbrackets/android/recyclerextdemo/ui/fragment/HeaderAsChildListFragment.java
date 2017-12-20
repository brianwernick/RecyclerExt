package com.devbrackets.android.recyclerextdemo.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.devbrackets.android.recyclerext.adapter.HeaderListAdapter;
import com.devbrackets.android.recyclerext.decoration.StickyHeaderDecoration;
import com.devbrackets.android.recyclerextdemo.R;
import com.devbrackets.android.recyclerextdemo.data.database.DBHelper;
import com.devbrackets.android.recyclerextdemo.data.database.ItemDAO;
import com.devbrackets.android.recyclerextdemo.ui.viewholder.ContactsHeaderViewHolder;
import com.devbrackets.android.recyclerextdemo.ui.viewholder.SimpleTextViewHolder;

import java.util.List;


/**
 * An example of the {@link HeaderAdapter}
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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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
     * The adapter that extends the {@link com.devbrackets.android.recyclerext.adapter.HeaderAdapter} to provide the
     * minimum number of methods to function
     */
    private class HeaderAdapter extends HeaderListAdapter<ContactsHeaderViewHolder, SimpleTextViewHolder, ItemDAO> {
        private LayoutInflater inflater;

        public HeaderAdapter(Context context, List<ItemDAO> items) {
            super();

            this.items = items;
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            //This is the call that makes the adapter treat the headers position as a child
            // e.g. CHILD(position=9, getItem(9)), HEADER(position=10, getItem(10)), CHILD(position=11, getItem(11))
            // whereas normally the header doesn't interfere with the child items
            // e.g. CHILD(position=9, getItem(9)), HEADER(position=10, getItem(10)), CHILD(position=11, getItem(10))
            showHeaderAsChild(true);
        }

        @NonNull
        @Override
        public ContactsHeaderViewHolder onCreateHeaderViewHolder(@NonNull ViewGroup parent, int viewType) {
            return ContactsHeaderViewHolder.newInstance(inflater, parent);
        }

        @NonNull
        @Override
        public SimpleTextViewHolder onCreateChildViewHolder(@NonNull ViewGroup parent, int viewType) {
            return SimpleTextViewHolder.newInstance(inflater, parent);
        }

        @Override
        public void onBindHeaderViewHolder(@NonNull ContactsHeaderViewHolder holder, int childPosition) {
            ItemDAO item = items.get(childPosition);

            holder.setText(item.getText());
            holder.setRegionText(childPosition / 10 + "");
        }

        @Override
        public void onBindChildViewHolder(@NonNull SimpleTextViewHolder holder, int childPosition) {
            holder.setText(items.get(childPosition).getText());
            holder.setSpacingVisible(true);
        }

        /**
         * For simplicity sake, we just return a simple mathematical id for the headers.
         * You should provide an actual id.
         */
        @Override
        public long getHeaderId(int childPosition) {
            return items.get(childPosition).getOrder() / 10;
        }

        /**
         * Specifying this will make only the number field from the header be sticky
         */
        @Override
        public int getCustomStickyHeaderViewId() {
            return R.id.contacts_header_item_region_text_view;
        }
    }
}
