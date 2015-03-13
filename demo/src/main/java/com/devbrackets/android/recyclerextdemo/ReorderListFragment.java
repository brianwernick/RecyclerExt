package com.devbrackets.android.recyclerextdemo;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.devbrackets.android.recyclerext.decoration.ReorderDecoration;
import com.devbrackets.android.recyclerextdemo.viewholder.SimpleDragItemViewHolder;

import java.util.LinkedList;
import java.util.List;


/**
 * A placeholder fragment containing a simple view.
 */
public class ReorderListFragment extends Fragment {
    private RecyclerView recyclerView;

    public static ReorderListFragment newInstance() {
        return new ReorderListFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.recycler_fragment, container, false);

        recyclerView = (RecyclerView)view.findViewById(R.id.recyclerext_fragment_recycler);
        setupRecyclerExt();

        return view;
    }

    private void setupRecyclerExt() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(new ListAdapter(getActivity()));

        ReorderDecoration reorderDecoration = new ReorderDecoration(recyclerView);
        reorderDecoration.setDragHandleId(R.id.simple_drag_item_handle);
        recyclerView.addItemDecoration(reorderDecoration);
        recyclerView.addOnItemTouchListener(reorderDecoration);
        recyclerView.setItemAnimator(null);
    }


















    private class ListAdapter extends RecyclerView.Adapter<SimpleDragItemViewHolder> {
        private List<String> examples;
        private LayoutInflater inflater;

        public ListAdapter(Context context) {
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            examples = new LinkedList<>();

            for (int i = 1; i < 20; i++) {
                examples.add("Reorderable Item " + i);
            }
        }

        @Override
        public SimpleDragItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = inflater.inflate(R.layout.simple_drag_item, null);

            return new SimpleDragItemViewHolder(view);
        }

        @Override
        public void onBindViewHolder(SimpleDragItemViewHolder holder, int position) {
            holder.setText(examples.get(position));
            holder.setPosition(position);
        }

        @Override
        public int getItemCount() {
            return examples.size();
        }
    }
}
