package com.devbrackets.android.recyclerextdemo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.devbrackets.android.recyclerext.decoration.ReorderDecoration;
import com.devbrackets.android.recyclerextdemo.viewholder.SimpleTextViewHolder;

import java.util.LinkedList;
import java.util.List;

public class MainActivity extends Activity {
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        recyclerView = (RecyclerView)findViewById(R.id.main_activity_recycler);
        setupRecyclerExt();
    }

    private void setupRecyclerExt() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new ListAdapter(this));

        ReorderDecoration reorderDecoration = new ReorderDecoration(recyclerView);
        reorderDecoration.setDragHandleId(R.id.simple_drag_item_handle);
        recyclerView.addItemDecoration(reorderDecoration);
        recyclerView.addOnItemTouchListener(reorderDecoration);
        recyclerView.setItemAnimator(null);
    }

    private void startFragmentActivity(int fragmentType) {
        Intent intent = new Intent(this, SingleFragmentActivity.class);
        intent.putExtra(SingleFragmentActivity.EXTRA_FRAGMENT_TYPE, fragmentType);
        startActivity(intent);
    }
















    private class ListAdapter extends RecyclerView.Adapter<SimpleTextViewHolder> implements View.OnClickListener {
        private List<String> examples;
        private LayoutInflater inflater;

        public ListAdapter(Context context) {
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            examples = new LinkedList<>();
            examples.add("Reorderable List Adapter");
            examples.add("Cursor Adapter");
            examples.add("Reorderable Cursor Adapter");
            examples.add("Header List Adapter");
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
            switch ((Integer)v.getTag()) {
                case 0:
                    startFragmentActivity(SingleFragmentActivity.FRAGMENT_TYPE_REORDER);
                    break;

                case 1:
                    startFragmentActivity(SingleFragmentActivity.FRAGMENT_TYPE_CURSOR);
                    break;

                case 2:
                    startFragmentActivity(SingleFragmentActivity.FRAGMENT_TYPE_REORDER_CURSOR);
                    break;

                case 3:
                    startFragmentActivity(SingleFragmentActivity.FRAGMENT_TYPE_HEADER_LIST);
                    break;
            }
        }
    }
}
