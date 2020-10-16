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
        when (example) {
            Example.REORDER_LIST_HORIZONTAL -> pushHorizontalReorderListFragment()
            Example.REORDER_LIST_VERTICAL -> pushReorderListFragment()
            Example.HEADER_LIST -> pushHeaderListFragment()
            Example.HEADER_AS_CHILD_LIST -> pushHeaderAsChildListFragment()
            Example.GRID -> pushGridFragment()
        }
    }

    private fun pushHorizontalReorderListFragment() {
        val fragment: Fragment = ReorderListHorizontalFragment.Companion.newInstance()
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).commit()
    }

    private fun pushReorderListFragment() {
        val fragment: Fragment = ReorderListFragment.Companion.newInstance()
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).commit()
    }

    private fun pushGridFragment() {
        val fragment: Fragment = GridFragment.Companion.newInstance()
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).commit()
    }

    private fun pushHeaderListFragment() {
        val fragment: Fragment = HeaderListFragment.Companion.newInstance()
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).commit()
    }

    private fun pushHeaderAsChildListFragment() {
        val fragment: Fragment = HeaderAsChildListFragment.Companion.newInstance()
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).commit()
    }

    companion object {
        const val EXTRA_FRAGMENT_TYPE = "EXTRA_FRAGMENT_TYPE"
    }
}