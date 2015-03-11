package com.devbrackets.android.recyclerextdemo;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.devbrackets.android.recyclerext.RecyclerExt;
import com.devbrackets.android.recyclerext.decoration.ReorderDecoration;
import com.devbrackets.android.recyclerext.layout.LayoutOrientation;
import com.devbrackets.android.recyclerext.layout.ListLayoutManager;
import com.devbrackets.android.recyclerextdemo.util.ReorderDetector;
import com.devbrackets.android.recyclerextdemo.viewholder.SimpleTextViewHolder;

import java.util.LinkedList;
import java.util.List;


/**
 * A placeholder fragment containing a simple view.
 */
public class ReorderListFragment extends Fragment {

    private RecyclerExt recyclerExt;
    private ReorderDecoration reorderDecoration;

    public static ReorderListFragment newInstance() {
        return new ReorderListFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.recyclerext_fragment, container, false);

        recyclerExt = (RecyclerExt)view.findViewById(R.id.recyclerext_fragment_recyclerext);
        setupRecyclerExt();

        return view;
    }

    private void setupRecyclerExt() {
        recyclerExt.setLayoutManager(new ListLayoutManager(getActivity(), LayoutOrientation.VERTICAL));
        recyclerExt.setAdapter(new ListAdapter(getActivity()));

        reorderDecoration = new ReorderDecoration(recyclerExt);
        recyclerExt.addItemDecoration(reorderDecoration);
    }


















    //TODO: as it currently sits, we need to watch the motionEvents to look for an up-event to stop reordering...
    //      Is there a way around this?
    private class ListAdapter extends RecyclerView.Adapter<SimpleTextViewHolder> implements View.OnTouchListener, ReorderDetector.ReorderDetectorListener {
        private List<String> examples;
        private LayoutInflater inflater;
        private ReorderDetector reorderDetector;

        public ListAdapter(Context context) {
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            reorderDetector = new ReorderDetector(this);

            examples = new LinkedList<>();

            for (int i = 1; i < 20; i++) {
                examples.add("Reorderable Item " + i);
            }
        }

        @Override
        public SimpleTextViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = inflater.inflate(R.layout.simple_text_item, null);

            return new SimpleTextViewHolder(view);
        }

        @Override
        public void onBindViewHolder(SimpleTextViewHolder holder, int position) {
            holder.setText(examples.get(position));
            holder.setPosition(position);
            holder.setOnTouchListener(this);
        }

        @Override
        public int getItemCount() {
            return examples.size();
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return reorderDetector.onTouch(v, event);
        }

        @Override
        public void onCancel(View view) {
            reorderDecoration.cancelReorder();
        }

        @Override
        public void onClick(View view) {
            //Purposefully left blank
        }

        @Override
        public void onDone(View view) {
            reorderStarted = false;
            int newPosition = reorderDecoration.endReorder();
        }

        @Override
        public void onLongClick(View view) {
            if (!reorderStarted) {
                reorderStarted = true;
                reorderDecoration.startReorder(view, null);
            }
        }

        private boolean reorderStarted;
        @Override
        public void onMove(View view, MotionEvent event) {

        }
    }
}
