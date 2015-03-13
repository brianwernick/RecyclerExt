package com.devbrackets.android.recyclerextdemo;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

/**
 *
 */
public class SingleFragmentActivity extends FragmentActivity {
    public static final String EXTRA_FRAGMENT_TYPE = "EXTRA_FRAGMENT_TYPE";

    public static final int FRAGMENT_TYPE_REORDER = 1;

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
            case FRAGMENT_TYPE_REORDER:
                pushReorderListFragment();
                break;
        }
    }

    private void pushReorderListFragment() {
        Fragment fragment = ReorderListFragment.newInstance();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
    }
}