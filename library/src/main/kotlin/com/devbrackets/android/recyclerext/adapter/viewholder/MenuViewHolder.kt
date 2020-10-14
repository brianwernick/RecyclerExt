/*
 * Copyright (C) 2016 Brian Wernick
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.devbrackets.android.recyclerext.adapter.viewholder

import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.annotation.IdRes
import androidx.annotation.MenuRes
import androidx.appcompat.widget.PopupMenu

/**
 * A simple ViewHolder that extends [ClickableViewHolder] to provide the
 * functionality for providing and registering for popup menu selection events
 */
abstract class MenuViewHolder(itemView: View) : ClickableViewHolder(itemView), PopupMenu.OnMenuItemClickListener {
    /**
     * Used to listen for menu item selections
     */
    interface OnMenuItemSelectedListener {
        fun onMenuItemSelected(viewHolder: MenuViewHolder, menuItem: MenuItem): Boolean
    }

    private var onMenuItemSelectedListener: OnMenuItemSelectedListener? = null

    /**
     * Retrieves the id for the view that will be used to show the
     * popup menu when clicked.
     *
     * @return The resource id for the menu view
     */
    @get:IdRes
    protected abstract val menuViewId: Int

    /**
     * Retrieves the id for the xml menu resource that specifies
     * the options for the popup menu.
     *
     * @return The resource id for the xml menu
     */
    @get:MenuRes
    protected abstract val menuResourceId: Int
    fun setOnMenuItemSelectedListener(listener: OnMenuItemSelectedListener?) {
        onMenuItemSelectedListener = listener
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        return onMenuItemSelectedListener != null && onMenuItemSelectedListener!!.onMenuItemSelected(this, item)
    }

    /**
     * Registers the view specified with [.getMenuViewId] to
     * show the popup menu specified with [.getMenuResourceId]
     */
    protected fun initializeMenuClickListener() {
        val menuView = itemView.findViewById<View>(menuViewId)
        menuView?.setOnClickListener(MenuClickListener())
    }

    /**
     * Shows the menu specified with the `menuResourceId` starting
     * at the `anchor`
     *
     * @param anchor The view to show the popup menu from
     * @param menuResourceId The resource id for the menu to show
     */
    protected fun showMenu(anchor: View, @MenuRes menuResourceId: Int) {
        val menu = PopupMenu(anchor.context, anchor)
        val inflater = menu.menuInflater
        inflater.inflate(menuResourceId, menu.menu)
        onPreparePopupMenu(menu.menu)
        menu.setOnMenuItemClickListener(this)
        menu.show()
    }

    /**
     * Allows the user to customize the popup menu specified with [.getMenuResourceId]
     * before it is shown
     *
     * @param menu The menu to customize
     */
    protected fun onPreparePopupMenu(menu: Menu) {
        //Purposefully left blank
    }

    /**
     * A simple click listener class to handle menu view clicks
     */
    protected inner class MenuClickListener : View.OnClickListener {
        override fun onClick(view: View) {
            showMenu(view, menuResourceId)
        }
    }

    init {
        initializeMenuClickListener()
    }
}