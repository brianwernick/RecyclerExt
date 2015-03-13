package com.devbrackets.android.recyclerextdemo;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.devbrackets.android.recyclerextdemo.viewholder.SimpleTextViewHolder;

import java.util.LinkedList;
import java.util.List;


/**
 * A placeholder fragment containing a simple view.
 */
public class StartupFragment extends Fragment {

    private RecyclerView recyclerView;

    public static StartupFragment newInstance() {
        return new StartupFragment();
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
    }



















    private class ListAdapter extends RecyclerView.Adapter<SimpleTextViewHolder> implements View.OnClickListener {
        private List<String> examples;
        private LayoutInflater inflater;

        public ListAdapter(Context context) {
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            examples = new LinkedList<>();
            examples.add("Sortable List");
            examples.add("Cursor Adapter");
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
            holder.setOnClickListener(this);
        }

        @Override
        public int getItemCount() {
            return examples.size();
        }

        @Override
        public void onClick(View v) {
            //TODO change the fragment... use view.getTag()
        }
    }
}
