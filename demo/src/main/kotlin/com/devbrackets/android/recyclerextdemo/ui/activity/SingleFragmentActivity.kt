package com.devbrackets.android.recyclerextdemo.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.devbrackets.android.recyclerextdemo.R
import com.devbrackets.android.recyclerextdemo.data.Example
import com.devbrackets.android.recyclerextdemo.ui.fragment.*

class SingleFragmentActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_single_fragment)
    }

    override fun onResume() {
        super.onResume()

        //Retrieves the type of example to display
        val extras = intent.extras
        val example = extras!!.getSerializable(EXTRA_FRAGMENT_TYPE) as Example? ?: return

        val fragment = when (example) {
            Example.REORDER_LIST_HORIZONTAL -> ReorderListHorizontalFragment()
            Example.REORDER_LIST_VERTICAL -> ReorderListFragment()
            Example.HEADER_LIST -> HeaderListFragment()
            Example.HEADER_AS_CHILD_LIST -> HeaderAsChildListFragment()
            Example.GRID -> GridFragment()
            Example.DELEGATED -> DelegatedFragment()
            Example.DELEGATED_HEADER -> DelegatedHeaderFragment()
        }

        pushFragment(fragment)
    }

    private fun pushFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    companion object {
        const val EXTRA_FRAGMENT_TYPE = "EXTRA_FRAGMENT_TYPE"
    }
}