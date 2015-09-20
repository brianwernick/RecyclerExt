package com.devbrackets.android.recyclerextdemo.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.devbrackets.android.recyclerext.adapter.RecyclerListAdapter;
import com.devbrackets.android.recyclerext.decoration.ReorderDecoration;
import com.devbrackets.android.recyclerextdemo.R;
import com.devbrackets.android.recyclerextdemo.data.Example;
import com.devbrackets.android.recyclerextdemo.ui.viewholder.SimpleTextViewHolder;

/**
 * An activity that lists the example items.
 */
public class MainActivity extends Activity {
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        recyclerView = (RecyclerView) findViewById(R.id.main_activity_recycler);
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

    private void startFragmentActivity(Example fragmentType) {
        Intent intent = new Intent(this, SingleFragmentActivity.class);
        intent.putExtra(SingleFragmentActivity.EXTRA_FRAGMENT_TYPE, fragmentType);
        startActivity(intent);
    }

    /**
     * A simple {@link RecyclerListAdapter} to display the options for the examples
     */
    private class ListAdapter extends RecyclerListAdapter<SimpleTextViewHolder, Example> implements View.OnClickListener {
        private LayoutInflater inflater;

        public ListAdapter(Context context) {
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            //Adds all the items from the Example enum
            for (Example example : Example.values()) {
                add(example);
            }
        }

        @Override
        public SimpleTextViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = inflater.inflate(R.layout.simple_text_item, null);
            return new SimpleTextViewHolder(view);
        }

        @Override
        public void onBindViewHolder(SimpleTextViewHolder holder, int position) {
            //noinspection ConstantConditions - getItem won't be null when called from the onBindViewHolder when using 'position'
            holder.setText(getItem(position).getTitle());
            holder.setPosition(position);
            holder.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = (Integer)view.getTag();
            startFragmentActivity(getItem(position));
        }
    }
}
