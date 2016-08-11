package com.devbrackets.android.recyclerext.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DimenRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.widget.FrameLayout;

import com.devbrackets.android.recyclerext.R;
import com.devbrackets.android.recyclerext.animation.FastScrollBubbleVisibilityAnimation;
import com.devbrackets.android.recyclerext.animation.FastScrollHandleVisibilityAnimation;

/**
 * A class that provides the functionality of a fast scroll
 * for the attached {@link android.support.v7.widget.RecyclerView}
 *
 * TODO: Option to hide on short lists
 */
@SuppressWarnings("unused")
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
    protected FastSmoothScroller fastSmoothScroller;

    @NonNull
    protected RecyclerScrollListener scrollListener = new RecyclerScrollListener();
    @NonNull
    protected Handler delayHandler = new Handler();

    @NonNull
    protected HandleHideRunnable handleHideRunnable = new HandleHideRunnable();
    @NonNull
    protected BubbleHideRunnable bubbleHideRunnable = new BubbleHideRunnable();

    @Nullable
    protected AnimationProvider animationProvider;

    protected int height;
    protected boolean showBubble;
    @NonNull
    protected BubbleAlignment bubbleAlignment = BubbleAlignment.TOP;

    protected boolean hideHandleAllowed = true;
    protected boolean draggingHandle = false;
    protected boolean trackClicksAllowed = false;

    protected long handleHideDelay = 1_000; //Milliseconds
    protected long bubbleHideDelay = 0; //Milliseconds

    protected Boolean requestedHandleVisibility;

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
        //Filters out touch events we don't need to handle
        if (!draggingHandle && event.getAction() != MotionEvent.ACTION_DOWN) {
            return super.onTouchEvent(event);
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //Verifies the event is within the allowed coordinates
                if (ignoreTouchDown(event.getX(), event.getY())) {
                    return false;
                }

                if (bubble.getVisibility() != VISIBLE) {
                    updateBubbleVisibility(true);
                }

                draggingHandle = true;
                handle.setSelected(true);
                //Purposefully falls through

            case MotionEvent.ACTION_MOVE:
                setBubbleAndHandlePosition(event.getY());
                setRecyclerViewPosition(event.getY());
                return true;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                draggingHandle = false;
                handle.setSelected(false);
                hideBubbleDelayed();
                hideHandleDelayed();
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

    /**
     * Specifies if clicks on the track should scroll to that position.
     *
     * @param allowed {@code true} to allow clicking on the track [default: {@code false}]
     */
    public void setTrackClicksAllowed(boolean allowed) {
        this.trackClicksAllowed = allowed;
    }

    public void setHideHandleAllowed(boolean allowed) {
        this.hideHandleAllowed = allowed;
    }

    public void setHandleHideDelay(long delayMilliseconds) {
        this.handleHideDelay = delayMilliseconds;
    }

    public long getHandleHideDelay() {
        return handleHideDelay;
    }

    public void setBubbleHideDelay(long delayMilliseconds) {
        this.bubbleHideDelay = delayMilliseconds;
    }

    public long getBubbleHideDelay() {
        return bubbleHideDelay;
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

    public void setAnimationProvider(@Nullable AnimationProvider animationProvider) {
        this.animationProvider = animationProvider;
    }

    public void setBubbleAlignment(@NonNull BubbleAlignment gravity) {
        bubbleAlignment = gravity;
    }

    protected void init(Context context, AttributeSet attrs) {
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.recyclerext_fast_scroll, this, true);

        bubble = (PositionSupportTextView) findViewById(R.id.recyclerext_fast_scroll_bubble);
        handle = (PositionSupportImageView) findViewById(R.id.recyclerext_fast_scroll_handle);

        bubble.setVisibility(View.GONE);

        readAttributes(context, attrs);
        fastSmoothScroller = new FastSmoothScroller(context);
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
        bubbleAlignment = BubbleAlignment.get(typedArray.getInt(R.styleable.FastScroll_re_bubble_alignment, 3));

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

    protected boolean ignoreTouchDown(float xPos, float yPos) {
        //Verifies the event is within the allowed X coordinates
        if (ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_LTR) {
            if (xPos < handle.getX() - ViewCompat.getPaddingStart(handle)) {
                return true;
            }
        } else {
            if (xPos > handle.getX() + handle.getWidth() + ViewCompat.getPaddingStart(handle)) {
                return true;
            }
        }

        if (!trackClicksAllowed) {
            //Enforces selection to only occur on the handle
            if (yPos < handle.getY() - handle.getPaddingTop() || yPos > handle.getY() + handle.getHeight() + handle.getPaddingBottom()) {
                return true;
            }
        }

        return false;
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

        int position = getValueInRange(0, itemCount - 1, (int) (proportion * (float) itemCount));
        smoothScrollToPosition(position);

        if (showBubble && popupCallbacks != null) {
            String bubbleText = popupCallbacks.getFastScrollPopupText(position);
            bubble.setText(bubbleText);
        }
    }

    protected void smoothScrollToPosition(int position) {
        fastSmoothScroller.setTargetPosition(position);
        recyclerView.getLayoutManager().startSmoothScroll(fastSmoothScroller);
    }

    protected void setBubbleAndHandlePosition(float y) {
        int handleHeight = handle.getHeight();
        float handleY = getValueInRange(0, height - handleHeight, (int) (y - handleHeight / 2));
        handle.setY(handleY);

        if (showBubble) {
            setBubblePosition(handleY, y);
        }
    }

    protected void setBubblePosition(float handleY, float requestedY) {
        int maxY = height - bubble.getHeight();

        float handleCenter = handleY + (handle.getHeight() / 2);
        float handleBottom = handleY + handle.getHeight();

        switch (bubbleAlignment) {
            case TOP: //TOP_TO_TOP
                bubble.setY(getValueInRange(0, maxY, (int)handleY));
                break;
            case CENTER: //CENTER_TO_CENTER
                bubble.setY(getValueInRange(0, maxY, (int)(handleCenter - (bubble.getHeight() / 2))));
                break;
            case BOTTOM: //BOTTOM_TO_BOTTOM
                bubble.setY(getValueInRange(0, maxY, (int)(handleBottom - bubble.getHeight())));
                break;
            case BOTTOM_TO_TOP: //Bubble bottom to handle top
                bubble.setY(getValueInRange(0, maxY, (int)(handleY - bubble.getHeight())));
                break;
            case TOP_TO_BOTTOM: //Bubble top to handle bottom
                bubble.setY(getValueInRange(0, maxY, (int)handleBottom));
                break;
            case BOTTOM_TO_CENTER: //Bubble bottom to handle center
                bubble.setY(getValueInRange(0, maxY, (int)(handleCenter - bubble.getHeight())));
                break;
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

        Log.d(TAG, "updating bubble visibility " + toVisible);
        bubble.startAnimation(getBubbleAnimation(bubble, toVisible));
    }

    protected void updateHandleVisibility(boolean toVisible) {
        if (requestedHandleVisibility != null && requestedHandleVisibility == toVisible) {
            return;
        }

        requestedHandleVisibility = toVisible;
        handle.clearAnimation();

        Log.d(TAG, "updating handle visibility " + toVisible);
        handle.startAnimation(getHandleAnimation(handle, toVisible));
    }

    /**
     * Handles the functionality to delay the hiding of the
     * bubble if the bubble is shown
     */
    protected void hideBubbleDelayed() {
        delayHandler.removeCallbacks(bubbleHideRunnable);
        if (showBubble && !draggingHandle) {
            delayHandler.postDelayed(bubbleHideRunnable, bubbleHideDelay);
        }
    }

    /**
     * Handles the functionality to delay the hiding of the
     * handle if allowed
     */
    protected void hideHandleDelayed() {
        delayHandler.removeCallbacks(handleHideRunnable);
        if (hideHandleAllowed && !draggingHandle) {
            delayHandler.postDelayed(handleHideRunnable, handleHideDelay);
        }
    }

    protected Animation getBubbleAnimation(@NonNull View bubble, final boolean toVisible) {
        Animation animation = animationProvider != null ? animationProvider.getBubbleAnimation(bubble, toVisible) : null;
        if (animation == null) {
            animation = new FastScrollBubbleVisibilityAnimation(bubble, toVisible);
        }

        return animation;
    }

    protected Animation getHandleAnimation(@NonNull View handle, final boolean toVisible) {
        Animation animation = animationProvider != null ? animationProvider.getHandleAnimation(handle, toVisible) : null;
        if (animation == null) {
            animation = new FastScrollHandleVisibilityAnimation(handle, toVisible);
        }

        return animation;
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
            //Makes sure the handle is shown when scrolling
            updateHandleVisibility(true);
            delayHandler.removeCallbacks(handleHideRunnable);

            if (handle.isSelected()) {
                return;
            }

            hideHandleDelayed();

            int verticalScrollOffset = recyclerView.computeVerticalScrollOffset();
            int verticalScrollRange = recyclerView.computeVerticalScrollRange();
            float proportion = (float) verticalScrollOffset / ((float) verticalScrollRange - height);
            setBubbleAndHandlePosition(height * proportion);
        }
    }

    protected class FastSmoothScroller extends LinearSmoothScroller {
        protected static final int DEFAULT_TOTAL_SCROLL_TIME = 50; //Milliseconds
        protected int totalScrollTime = DEFAULT_TOTAL_SCROLL_TIME;

        public FastSmoothScroller(Context context) {
            super(context);
        }

        public FastSmoothScroller(Context context, int totalScrollTime) {
            this(context);
            this.totalScrollTime = totalScrollTime;
        }

        @Override
        public PointF computeScrollVectorForPosition(int targetPosition) {
            return ((LinearLayoutManager)recyclerView.getLayoutManager()).computeScrollVectorForPosition(targetPosition);
        }

        @Override
        protected int calculateTimeForScrolling(int dx) {
            return totalScrollTime;
        }
    }

    protected class HandleHideRunnable implements Runnable {
        @Override
        public void run() {
            updateHandleVisibility(false);
        }
    }

    protected class BubbleHideRunnable implements Runnable {
        @Override
        public void run() {
            updateBubbleVisibility(false);
        }
    }

    public interface AnimationProvider {
        @Nullable
        Animation getBubbleAnimation(@NonNull View bubble, boolean toVisible);
        @Nullable
        Animation getHandleAnimation(@NonNull View handle, boolean toVisible);
    }

    public enum BubbleAlignment {
        TOP,
        CENTER,
        BOTTOM,
        BOTTOM_TO_TOP,
        TOP_TO_BOTTOM,
        BOTTOM_TO_CENTER;

        private static BubbleAlignment get(int index) {
            return BubbleAlignment.values()[index];
        }
    }
}