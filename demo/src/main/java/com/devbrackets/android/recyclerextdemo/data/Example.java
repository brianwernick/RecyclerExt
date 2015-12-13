package com.devbrackets.android.recyclerextdemo.data;

/**
 * An enum for all the examples presented in the
 * {@link com.devbrackets.android.recyclerextdemo.ui.activity.MainActivity}
 */
public enum Example {
    CURSOR("Cursor"),
    HEADER_LIST("Header List"),
    HEADER_AS_CHILD_LIST("Contacts Header List"),
    REORDER_LIST_HORIZONTAL("Reorder List Horizontal"),
    REORDER_LIST_VERTICAL("Reorder List Vertical"),
    REORDER_CURSOR_VERTICAL("Reorder Cursor Vertical");

    private final String title;

    Example(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
