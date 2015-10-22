package com.devbrackets.android.recyclerext.adapter.viewholder;

import android.support.annotation.IdRes;
import android.support.annotation.MenuRes;
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

    public interface OnMenuItemSelectedListener {
        boolean onMenuItemSelected(MenuViewHolder viewHolder, MenuItem menuItem);
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

    public MenuViewHolder(View itemView) {
        super(itemView);
        initializeMenuClickListener();
    }

    public void setOnMenuItemSelectedListener(@Nullable OnMenuItemSelectedListener listener) {
        this.onMenuItemSelectedListener = listener;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
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
    protected void showMenu(View anchor, @MenuRes int menuResourceId) {
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
    protected void onPreparePopupMenu(Menu menu) {
        //Purposefully left blank
    }

    /**
     * A simple click listener class to handle menu view clicks
     */
    protected class MenuClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            showMenu(view, getMenuResourceId());
        }
    }
}