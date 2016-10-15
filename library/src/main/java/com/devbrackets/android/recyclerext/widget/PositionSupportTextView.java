package com.devbrackets.android.recyclerext.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * A basic extension to the {@link TextView} to add backwards compatibility for
 * the getX() and getY() method
 */
public class PositionSupportTextView extends TextView {
    public PositionSupportTextView(Context context) {
        super(context);
    }

    public PositionSupportTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PositionSupportTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PositionSupportTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public float getY() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ? super.getY() : getTop();
    }

    public void setY(float y) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            super.setY(y);
        } else {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) getLayoutParams();
            params.topMargin = (int)y;
            setLayoutParams(params);
        }
    }

    public float getX() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ? super.getX() : getLeft();
    }

    public void setX(float x) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            super.setX(x);
        } else {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) getLayoutParams();
            params.leftMargin = (int)x;
            setLayoutParams(params);
        }
    }

    public void setBackground(@Nullable Drawable drawable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            super.setBackground(drawable);
        } else {
            //noinspection deprecation
            setBackgroundDrawable(drawable);
        }
    }
}
