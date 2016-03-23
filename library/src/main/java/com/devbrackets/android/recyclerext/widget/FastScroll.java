package com.devbrackets.android.recyclerext.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DimenRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import com.devbrackets.android.recyclerext.R;
import com.devbrackets.android.recyclerext.animation.FastScrollBubbleVisibilityAnimation;

/**
 * A class that provides the functionality of a fast scroll
 * for the attached {@link android.support.v7.widget.RecyclerView}
 *
 * TODO:
 * We needs options to:
 *  * Option to hide on short lists, or when scrolling is inactive (not currently scrolling)
 *  * Option to specify the delay before hiding and showing the bubble (separate for hide and show)
 *  * Horizontal support?
 *  * Callbacks when hiding/showing the bubble and when we should hide/show the handle
 *  * customize visibility animation for bubble
 */
public class FastScroll extends FrameLayout {
    private static final String TAG = "FastScroll";
    protected static final int TRACK_SNAP_RANGE = 5;

    public interface FastScrollPopupCallbacks {
        String getFastScrollPopupText(int position);
    }

    @Nullable
    protected FastScrollPopupCallbacks popupCallbacks;

    protected PositionSupportImageView handle;
    protected PositionSupportTextView bubble;

    protected RecyclerView recyclerView;
    protected RecyclerScrollListener scrollListener = new RecyclerScrollListener();

    protected int height;
    protected boolean showBubble;

    public FastScroll(Context context) {
        super(context);
        init(context, null);
    }

    public FastScroll(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public FastScroll(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public FastScroll(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (recyclerView != null) {
            recyclerView.removeOnScrollListener(scrollListener);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);
        height = h;
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (event.getX() < handle.getX() - ViewCompat.getPaddingStart(handle)) {
                    return false;
                }

                if (bubble.getVisibility() != VISIBLE) {
                    updateBubbleVisibility(true);
                }

                handle.setSelected(true);
                //Purposefully falls through

            case MotionEvent.ACTION_MOVE:
                setBubbleAndHandlePosition(event.getY());
                setRecyclerViewPosition(event.getY());
                return true;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                handle.setSelected(false);
                updateBubbleVisibility(false);
                return true;
        }

        return super.onTouchEvent(event);
    }

    public void attach(final RecyclerView recyclerView) {
        if (showBubble && !(recyclerView.getAdapter() instanceof FastScrollPopupCallbacks)) {
            Log.e(TAG, "The RecyclerView Adapter specified needs to implement " + FastScrollPopupCallbacks.class.getSimpleName());
            return;
        }

        this.recyclerView = recyclerView;

        if (showBubble) {
            popupCallbacks = (FastScrollPopupCallbacks) recyclerView.getAdapter();
        }

        recyclerView.addOnScrollListener(scrollListener);
        recyclerView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                recyclerView.getViewTreeObserver().removeOnPreDrawListener(this);
                scrollListener.onScrolled(recyclerView, 0, 0);

                return true;
            }
        });
    }

    public void setShowBubble(boolean showBubble) {
        if (this.showBubble == showBubble) {
            return;
        }

        this.showBubble = showBubble;
        if (recyclerView == null || !showBubble) {
            return;
        }

        if (!(recyclerView.getAdapter() instanceof FastScrollPopupCallbacks)) {
            Log.e(TAG, "The RecyclerView Adapter specified needs to implement " + FastScrollPopupCallbacks.class.getSimpleName());
            return;
        }

        popupCallbacks = (FastScrollPopupCallbacks) recyclerView.getAdapter();
    }

    public void setTextColorRes(@ColorRes int colorRes) {
        setTextColor(getColor(colorRes));
    }

    public void setTextColor(@ColorInt int color) {
        bubble.setTextColor(color);
    }

    public void setTextSize(@DimenRes int dimenRes) {
        bubble.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(dimenRes));
    }

    public void setTextSize(float size) {
        bubble.setTextSize(size);
    }

    public void setBubbleColorRes(@ColorRes int colorRes) {
        setBubbleColor(getColor(colorRes));
    }

    public void setBubbleColor(@ColorInt int color) {
        bubble.setBackground(tint(getDrawable(R.drawable.recyclerext_fast_scroll_bubble), color));
    }

    public void setBubbleDrawable(Drawable drawable) {
        bubble.setBackground(drawable);
    }

    public void setHandleColorRes(@ColorRes int colorRes) {
        setHandleColor(getColor(colorRes));
    }

    public void setHandleColor(@ColorInt int color) {
        handle.setBackground(tint(getDrawable(R.drawable.recyclerext_fast_scroll_handle), color));
    }

    public void setHandleDrawable(Drawable drawable) {
        handle.setBackground(drawable);
    }

    protected void init(Context context, AttributeSet attrs) {
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.recyclerext_fast_scroll, this, true);

        bubble = (PositionSupportTextView) findViewById(R.id.recyclerext_fast_scroll_bubble);
        handle = (PositionSupportImageView) findViewById(R.id.recyclerext_fast_scroll_handle);

        bubble.setVisibility(View.GONE);

        readAttributes(context, attrs);
    }

    /**
     * Reads the attributes associated with this view, setting any values found
     *
     * @param context The context to retrieve the styled attributes with
     * @param attrs The {@link AttributeSet} to retrieve the values from
     */
    protected void readAttributes(Context context, @Nullable AttributeSet attrs) {
        if (attrs == null || isInEditMode()) {
            return;
        }

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.FastScroll);
        if (typedArray == null) {
            return;
        }

        retrieveBubbleAttributes(typedArray);
        retrieveHandleAttributes(typedArray);

        typedArray.recycle();
    }

    protected void retrieveBubbleAttributes(TypedArray typedArray) {
        showBubble = typedArray.getBoolean(R.styleable.FastScroll_re_show_bubble, true);

        int textColor = getColor(R.color.recyclerext_fast_scroll_bubble_text_color_default);
        textColor = typedArray.getColor(R.styleable.FastScroll_re_bubble_text_color, textColor);

        int textSize = getResources().getDimensionPixelSize(R.dimen.recyclerext_fast_scroll_bubble_text_size_default);
        textSize = typedArray.getDimensionPixelSize(R.styleable.FastScroll_re_bubble_text_size, textSize);

        Drawable backgroundDrawable = typedArray.getDrawable(R.styleable.FastScroll_re_bubble_background);
        int backgroundColor = getColor(R.color.recyclerext_fast_scroll_bubble_color_default);
        backgroundColor = typedArray.getColor(R.styleable.FastScroll_re_bubble_color, backgroundColor);

        if (backgroundDrawable == null) {
            backgroundDrawable = tint(getDrawable(R.drawable.recyclerext_fast_scroll_bubble), backgroundColor);
        }

        bubble.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        bubble.setTextColor(textColor);
        bubble.setBackground(backgroundDrawable);
    }

    protected void retrieveHandleAttributes(TypedArray typedArray) {
        Drawable backgroundDrawable = typedArray.getDrawable(R.styleable.FastScroll_re_handle_background);
        int backgroundColor = getColor(R.color.recyclerext_fast_scroll_handle_color_default);
        backgroundColor = typedArray.getColor(R.styleable.FastScroll_re_handle_color, backgroundColor);

        if (backgroundDrawable == null) {
            backgroundDrawable = tint(getDrawable(R.drawable.recyclerext_fast_scroll_handle), backgroundColor);
        }

        handle.setBackground(backgroundDrawable);
    }

    protected void setRecyclerViewPosition(float y) {
        float proportion;
        int itemCount = recyclerView.getAdapter().getItemCount();

        if (handle.getY() == 0) {
            proportion = 0f;
        } else if (handle.getY() + handle.getHeight() >= height - TRACK_SNAP_RANGE) {
            proportion = 1f;
        } else {
            proportion = y / (float) height;
        }

        int targetPos = getValueInRange(0, itemCount - 1, (int) (proportion * (float) itemCount));
        ((LinearLayoutManager) recyclerView.getLayoutManager()).scrollToPositionWithOffset(targetPos, 0);

        if (showBubble && popupCallbacks != null) {
            String bubbleText = popupCallbacks.getFastScrollPopupText(targetPos);
            bubble.setText(bubbleText);
        }
    }

    protected void setBubbleAndHandlePosition(float y) {
        int handleHeight = handle.getHeight();
        handle.setY(getValueInRange(0, height - handleHeight, (int) (y - handleHeight / 2)));

        if (bubble != null) {
            int bubbleHeight = bubble.getHeight();
            bubble.setY(getValueInRange(0, height - bubbleHeight - handleHeight / 2, (int) (y - bubbleHeight)));
        }
    }

    /**
     * Updates the visibility of the bubble representing the current location.
     * Typically this bubble will contain the first letter of the section that
     * is at the top of the RecyclerView.
     *
     * @param toVisible True if the bubble should be visible at the end of the animation
     */
    protected void updateBubbleVisibility(boolean toVisible) {
        if (!showBubble) {
            return;
        }

        bubble.clearAnimation();

        Log.d(TAG, "updating bubble visibility" + toVisible);
        FastScrollBubbleVisibilityAnimation animation = new FastScrollBubbleVisibilityAnimation(bubble, toVisible);
        bubble.startAnimation(animation);
    }

    protected Drawable tint(@Nullable Drawable drawable, @ColorInt int color) {
        if (drawable != null) {
            drawable.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY));
        }

        return drawable;
    }

    protected Drawable getDrawable(@DrawableRes int res) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return getResources().getDrawable(res, getContext().getTheme());
        }

        //noinspection deprecation
        return getResources().getDrawable(res);
    }

    @ColorInt
    protected int getColor(@ColorRes int res) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return getResources().getColor(res, getContext().getTheme());
        }

        //noinspection deprecation
        return getResources().getColor(res);
    }

    protected int getValueInRange(int min, int max, int value) {
        int minimum = Math.max(min, value);
        return Math.min(minimum, max);
    }

    /**
     * Listens to the scroll position changes of the parent (RecyclerView)
     * so that the handle will always have the correct position
     */
    protected class RecyclerScrollListener extends RecyclerView.OnScrollListener {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            if (handle.isSelected()) {
                return;
            }

            int verticalScrollOffset = recyclerView.computeVerticalScrollOffset();
            int verticalScrollRange = recyclerView.computeVerticalScrollRange();
            float proportion = (float) verticalScrollOffset / ((float) verticalScrollRange - height);
            setBubbleAndHandlePosition(height * proportion);
        }
    }
}