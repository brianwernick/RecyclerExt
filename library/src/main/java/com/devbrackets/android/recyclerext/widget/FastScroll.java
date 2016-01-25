package com.devbrackets.android.recyclerext.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.widget.FrameLayout;

import com.devbrackets.android.recyclerext.R;
import com.devbrackets.android.recyclerext.animation.FastScrollBubbleVisibilityAnimation;

/**
 * A class that provides the functionality of a fast scroll
 * for the attached {@link android.support.v7.widget.RecyclerView}
 *
 * TODO:
 * We needs options to:
 *  * padding/margin?
 *  * rtl support?
 *  * Horizontal support (already support vertical)
 *  * Callbacks when hiding/showing the bubble and when we should hide/show the handle & track
 */
public class FastScroll extends FrameLayout {
    private static final String TAG = "FastScroll";
    private static final int TRACK_SNAP_RANGE = 5;

    public interface FastScrollPopupCallbacks {
        String getFastScrollPopupText(int position);
    }

    private PositionSupportImageView handle;
    private PositionSupportTextView bubble;

    private RecyclerView recyclerView;
    private FastScrollPopupCallbacks popupCallbacks;

    private int height;

    private Animation currentAnimation;
    private FastScrollListener scrollListener = new FastScrollListener();

    private boolean showBubble;

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
        popupCallbacks = (FastScrollPopupCallbacks)recyclerView.getAdapter();

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
     * TODO: make sure to add source modifiers for all attributes as well
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

        int textColor = typedArray.getColor(R.styleable.FastScroll_re_bubble_text_color, 0xFFFFFFFF); //TODO: leave as white?

        int textSize = getResources().getDimensionPixelSize(R.dimen.recyclerext_default_fast_scroll_bubble_text_size);
        textSize = typedArray.getDimensionPixelSize(R.styleable.FastScroll_re_bubble_text_size, textSize);

        Drawable backgroundDrawable = typedArray.getDrawable(R.styleable.FastScroll_re_bubble_background);
        int backgroundColor = typedArray.getColor(R.styleable.FastScroll_re_bubble_color, 0xFF4433FF); //TODO: accent color or fallback...

        if (backgroundDrawable == null) {
            backgroundDrawable = tint(getDrawable(R.drawable.recyclerext_fast_scroll_bubble), backgroundColor);
        }

        bubble.setTextSize(textSize);
        bubble.setTextColor(textColor);
        bubble.setBackground(backgroundDrawable);
    }

    protected void retrieveHandleAttributes(TypedArray typedArray) {
        Drawable backgroundDrawable = typedArray.getDrawable(R.styleable.FastScroll_re_handle_background);
        int backgroundColor = typedArray.getColor(R.styleable.FastScroll_re_handle_color, 0xFF4433FF); //TODO: accent color or fallback...

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
        String bubbleText = popupCallbacks.getFastScrollPopupText(targetPos);

        bubble.setText(bubbleText);
    }

    protected int getValueInRange(int min, int max, int value) {
        int minimum = Math.max(min, value);
        return Math.min(minimum, max);
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

        if (currentAnimation != null) {
            bubble.clearAnimation();
        }

        Log.d(TAG, "updating bubble visibility" + toVisible);
        currentAnimation = new FastScrollBubbleVisibilityAnimation(bubble, toVisible);
        bubble.startAnimation(currentAnimation);
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
        return getResources().getDrawable(R.drawable.recyclerext_fast_scroll_handle);
    }

    /**
     * Listens to the scroll position changes of the parent (RecyclerView)
     * so that the handle will always have the correct position
     */
    protected class FastScrollListener extends RecyclerView.OnScrollListener {
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