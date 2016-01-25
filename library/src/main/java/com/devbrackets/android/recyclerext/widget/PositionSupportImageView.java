package com.devbrackets.android.recyclerext.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;

/**
 * A basic extension to the {@link ImageView} to add backwards compatibility for
 * the getX() and getY() methods.
 */
public class PositionSupportImageView extends ImageView {
    public PositionSupportImageView(Context context) {
        super(context);
    }

    public PositionSupportImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PositionSupportImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PositionSupportImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
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

    public void setBackground(Drawable drawable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            super.setBackground(drawable);
        } else {
            //noinspection deprecation
            setBackgroundDrawable(drawable);
        }
    }
}