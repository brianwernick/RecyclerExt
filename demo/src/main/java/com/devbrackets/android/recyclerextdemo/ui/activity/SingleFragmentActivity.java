package com.devbrackets.android.recyclerextdemo.ui.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import com.devbrackets.android.recyclerextdemo.R;
import com.devbrackets.android.recyclerextdemo.data.Example;
import com.devbrackets.android.recyclerextdemo.ui.fragment.GridFragment;
import com.devbrackets.android.recyclerextdemo.ui.fragment.HeaderAsChildListFragment;
import com.devbrackets.android.recyclerextdemo.ui.fragment.HeaderListFragment;
import com.devbrackets.android.recyclerextdemo.ui.fragment.ReorderListFragment;
import com.devbrackets.android.recyclerextdemo.ui.fragment.ReorderListHorizontalFragment;

public class SingleFragmentActivity extends AppCompatActivity {
    public static final String EXTRA_FRAGMENT_TYPE = "EXTRA_FRAGMENT_TYPE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_fragment);
    }

    @Override
    protected void onResume() {
        super.onResume();

        //Retrieves the type of example to display
        Bundle extras = getIntent().getExtras();
        Example example = (Example)extras.getSerializable(EXTRA_FRAGMENT_TYPE);
        if (example == null) {
            return;
        }

        //Displays the example requested
        switch (example) {
            case REORDER_LIST_HORIZONTAL:
                pushHorizontalReorderListFragment();
                break;

            case REORDER_LIST_VERTICAL:
                pushReorderListFragment();
                break;

            case HEADER_LIST:
                pushHeaderListFragment();
                break;

            case HEADER_AS_CHILD_LIST:
                pushHeaderAsChildListFragment();
                break;

            case GRID:
                pushGridFragment();
                break;
        }
    }

    private void pushHorizontalReorderListFragment() {
        Fragment fragment = ReorderListHorizontalFragment.newInstance();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
    }

    private void pushReorderListFragment() {
        Fragment fragment = ReorderListFragment.newInstance();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
    }

    private void pushGridFragment() {
        Fragment fragment = GridFragment.newInstance();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
    }

    private void pushHeaderListFragment() {
        Fragment fragment = HeaderListFragment.newInstance();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
    }

    private void pushHeaderAsChildListFragment() {
        Fragment fragment = HeaderAsChildListFragment.newInstance();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
    }
}