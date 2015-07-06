package com.devbrackets.android.recyclerextdemo;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

/**
 *
 */
public class SingleFragmentActivity extends FragmentActivity {
    public static final String EXTRA_FRAGMENT_TYPE = "EXTRA_FRAGMENT_TYPE";

    public static final int FRAGMENT_TYPE_REORDER_HORIZONTAL    = 1;
    public static final int FRAGMENT_TYPE_REORDER               = 2;
    public static final int FRAGMENT_TYPE_CURSOR                = 3;
    public static final int FRAGMENT_TYPE_REORDER_CURSOR        = 4;
    public static final int FRAGMENT_TYPE_HEADER_LIST           = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.single_fragment_activity);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Bundle extras = getIntent().getExtras();
        int fragmentType = extras.getInt(EXTRA_FRAGMENT_TYPE, -1);

        switch (fragmentType) {
            case FRAGMENT_TYPE_REORDER_HORIZONTAL:
                pushHorizontalReorderListFragment();
                break;

            case FRAGMENT_TYPE_REORDER:
                pushReorderListFragment();
                break;

            case FRAGMENT_TYPE_CURSOR:
                pushCursorFragment();
                break;

            case FRAGMENT_TYPE_REORDER_CURSOR:
                pushReorderCursorFragment();
                break;

            case FRAGMENT_TYPE_HEADER_LIST:
                pushHeaderListFragment();
                break;
        }
    }

    private void pushHorizontalReorderListFragment() {
        Fragment fragment = ReorderListFragmentHorizontal.newInstance();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
    }

    private void pushReorderListFragment() {
        Fragment fragment = ReorderListFragment.newInstance();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
    }

    private void pushCursorFragment() {
        Fragment fragment = CursorFragment.newInstance();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
    }

    private void pushReorderCursorFragment() {
        Fragment fragment = ReorderCursorFragment.newInstance();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
    }

    private void pushHeaderListFragment() {
        Fragment fragment = HeaderListFragment.newInstance();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
    }
}