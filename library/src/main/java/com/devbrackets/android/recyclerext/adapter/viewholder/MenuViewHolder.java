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

package com.devbrackets.android.recyclerext.adapter.viewholder;

import android.support.annotation.IdRes;
import android.support.annotation.MenuRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.PopupMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

/**
 * A simple ViewHolder that extends {@link ClickableViewHolder} to provide the
 * functionality for providing and registering for popup menu selection events
 */
public abstract class MenuViewHolder extends ClickableViewHolder implements PopupMenu.OnMenuItemClickListener {

    /**
     * Used to listen for menu item selections
     */
    public interface OnMenuItemSelectedListener {
        boolean onMenuItemSelected(@NonNull MenuViewHolder viewHolder, @NonNull MenuItem menuItem);
    }

    @Nullable
    private OnMenuItemSelectedListener onMenuItemSelectedListener;

    /**
     * Retrieves the id for the view that will be used to show the
     * popup menu when clicked.
     *
     * @return The resource id for the menu view
     */
    @IdRes
    protected abstract int getMenuViewId();

    /**
     * Retrieves the id for the xml menu resource that specifies
     * the options for the popup menu.
     *
     * @return The resource id for the xml menu
     */
    @MenuRes
    protected abstract int getMenuResourceId();

    public MenuViewHolder(@NonNull View itemView) {
        super(itemView);
        initializeMenuClickListener();
    }

    public void setOnMenuItemSelectedListener(@Nullable OnMenuItemSelectedListener listener) {
        this.onMenuItemSelectedListener = listener;
    }

    @Override
    public boolean onMenuItemClick(@NonNull MenuItem item) {
        return onMenuItemSelectedListener != null && onMenuItemSelectedListener.onMenuItemSelected(this, item);
    }

    /**
     * Registers the view specified with {@link #getMenuViewId()} to
     * show the popup menu specified with {@link #getMenuResourceId()}
     */
    protected void initializeMenuClickListener() {
        View menuView = itemView.findViewById(getMenuViewId());
        if (menuView != null) {
            menuView.setOnClickListener(new MenuClickListener());
        }
    }

    /**
     * Shows the menu specified with the <code>menuResourceId</code> starting
     * at the <code>anchor</code>
     *
     * @param anchor The view to show the popup menu from
     * @param menuResourceId The resource id for the menu to show
     */
    protected void showMenu(@NonNull View anchor, @MenuRes int menuResourceId) {
        PopupMenu menu = new PopupMenu(anchor.getContext(), anchor);
        MenuInflater inflater = menu.getMenuInflater();
        inflater.inflate(menuResourceId, menu.getMenu());

        onPreparePopupMenu(menu.getMenu());
        menu.setOnMenuItemClickListener(this);
        menu.show();
    }

    /**
     * Allows the user to customize the popup menu specified with {@link #getMenuResourceId()}
     * before it is shown
     *
     * @param menu The menu to customize
     */
    protected void onPreparePopupMenu(@NonNull Menu menu) {
        //Purposefully left blank
    }

    /**
     * A simple click listener class to handle menu view clicks
     */
    protected class MenuClickListener implements View.OnClickListener {
        @Override
        public void onClick(@NonNull View view) {
            showMenu(view, getMenuResourceId());
        }
    }
}