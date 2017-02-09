package com.devbrackets.android.recyclerextdemo.ui.fragment;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.devbrackets.android.recyclerext.adapter.RecyclerCursorAdapter;
import com.devbrackets.android.recyclerext.widget.FastScroll;
import com.devbrackets.android.recyclerextdemo.R;
import com.devbrackets.android.recyclerextdemo.data.database.DBHelper;
import com.devbrackets.android.recyclerextdemo.data.database.ItemDAO;
import com.devbrackets.android.recyclerextdemo.ui.viewholder.SimpleTextViewHolder;


/**
 * An example of the RecyclerView Cursor Adapter
 */
public class CursorFragment extends Fragment {
    private DBHelper dbHelper;
    private RecyclerView recyclerView;
    private FastScroll fastScroll;

    public static CursorFragment newInstance() {
        return new CursorFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recycler, container, false);
        recyclerView = (RecyclerView)view.findViewById(R.id.recyclerext_fragment_recycler);
        fastScroll = (FastScroll)view.findViewById(R.id.recyclerext_fast_scroll);
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
        CursorAdapter cursorAdapter = new CursorAdapter(getActivity(), ItemDAO.findCursorAll(dbHelper.getWritableDatabase()));

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(cursorAdapter);
        fastScroll.attach(recyclerView);
    }

    /**
     * The adapter that extends the {@link RecyclerCursorAdapter} to provide the
     * minimum number of methods to function
     */
    private class CursorAdapter extends RecyclerCursorAdapter<SimpleTextViewHolder> implements FastScroll.PopupCallbacks {
        private LayoutInflater inflater;

        public CursorAdapter(Context context, Cursor cursor) {
            super(cursor);
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public SimpleTextViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return SimpleTextViewHolder.newInstance(inflater, parent);
        }

        @Override
        public void onBindViewHolder(@NonNull SimpleTextViewHolder holder, @NonNull Cursor cursor, int position) {
            ItemDAO item = new ItemDAO(cursor);
            holder.setText(item.getText() != null ? item.getText() : "");
            holder.setPosition(position);
        }

        @Override
        public long getSectionId(@IntRange(from = 0) int position) {
            return (long)(position / 10);
        }

        @NonNull
        @Override
        public String getPopupText(int position, long sectionId) {
            return "" + sectionId;
        }
    }
}
