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

    /**
     * Links this widget to the {@code recyclerView}. This is necessary for the
     * FastScroll to function.
     *
     * @param recyclerView The {@link RecyclerView} to attach to
     */
    public void attach(@NonNull final RecyclerView recyclerView) {
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

    /**
     * Specifies if the popup bubble should be shown when the handle is
     * being dragged.
     *
     * @param showBubble {@code true} if the popup bubble should be shown
     */
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

    /**
     * The base initialization method (called from constructors) that
     * inflates and configures the widget.
     *
     * @param context The context of the widget
     * @param attrs The attributes associated with the widget
     */
    protected void init(Context context, @Nullable AttributeSet attrs) {
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

    /**
     * Retrieves the xml attributes associated with the popup bubble.
     * This includes the drawable, tint color, alignment, font options, etc.
     *
     * @param typedArray The array of attributes to use
     */
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

    /**
     * Retrieves the xml attributes associated with the drag handle.
     * This includes the drawable and tint color
     *
     * @param typedArray The array of attributes to use
     */
    protected void retrieveHandleAttributes(TypedArray typedArray) {
        Drawable backgroundDrawable = typedArray.getDrawable(R.styleable.FastScroll_re_handle_background);
        int backgroundColor = getColor(R.color.recyclerext_fast_scroll_handle_color_default);
        backgroundColor = typedArray.getColor(R.styleable.FastScroll_re_handle_color, backgroundColor);

        if (backgroundDrawable == null) {
            backgroundDrawable = tint(getDrawable(R.drawable.recyclerext_fast_scroll_handle), backgroundColor);
        }

        handle.setBackground(backgroundDrawable);
    }

    /**
     * Determines if the {@link MotionEvent#ACTION_DOWN} event should be ignored.
     * This occurs when the event position is outside the bounds of the drag handle or
     * the track (of the drag handle) when disabled (see {@link #setTrackClicksAllowed(boolean)}
     *
     * @param xPos The x coordinate of the event
     * @param yPos The y coordinate of the event
     * @return {@code true} if the event should be ignored
     */
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

    /**
     * Updates the scroll position of the {@link #recyclerView}
     * by determining the adapter position proportionally related to
     * the {@code y} position
     *
     * @param y The y coordinate to find the adapter position for
     */
    protected void setRecyclerViewPosition(float y) {
        float proportion;
        int itemCount = recyclerView.getAdapter().getItemCount();

        //Boxes the percent
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

    /**
     * Informs the {@link #recyclerView} that we need to smoothly scroll
     * to the requested position.
     *
     * @param position The adapter position to scroll to
     */
    protected void smoothScrollToPosition(int position) {
        fastSmoothScroller.setTargetPosition(position);
        recyclerView.getLayoutManager().startSmoothScroll(fastSmoothScroller);
    }

    /**
     * Updates the position both the drag handle and popup bubble
     * have in relation to y (the users finger)
     *
     * @param y The position to place the drag handle at
     */
    protected void setBubbleAndHandlePosition(float y) {
        int handleHeight = handle.getHeight();
        float handleY = getValueInRange(0, height - handleHeight, (int) (y - handleHeight / 2));
        handle.setY(handleY);

        if (showBubble) {
            setBubblePosition(handleY);
        }
    }

    /**
     * Updates the position of the popup bubble in relation to the
     * drag handle. This depends on the value of {@link #bubbleAlignment}
     *
     * @param handleY The position the drag handle has for relational alignment
     */
    protected void setBubblePosition(float handleY) {
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
     * @param toVisible {@code true} if the bubble should be visible at the end of the animation
     */
    protected void updateBubbleVisibility(boolean toVisible) {
        if (!showBubble) {
            return;
        }

        bubble.clearAnimation();

        Log.d(TAG, "updating bubble visibility " + toVisible);
        bubble.startAnimation(getBubbleAnimation(bubble, toVisible));
    }

    /**
     * Updates the visibility of the drag handle, storing the requested state
     * so that we aren't continuously requesting visibility animations.
     *
     * @param toVisible {@code true} if the drag handle should be visible at the end of the change
     */
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
     * handle
     */
    protected void hideHandleDelayed() {
        delayHandler.removeCallbacks(handleHideRunnable);
        if (hideHandleAllowed && !draggingHandle) {
            delayHandler.postDelayed(handleHideRunnable, handleHideDelay);
        }
    }

    /**
     * Retrieves the animation for hiding or showing the popup bubble
     *
     * @param bubble The view representing the popup bubble to animate
     * @param toVisible {@code true} if the animation should show the bubble
     * @return The animation for hiding or showing the bubble
     */
    @NonNull
    protected Animation getBubbleAnimation(@NonNull View bubble, final boolean toVisible) {
        Animation animation = animationProvider != null ? animationProvider.getBubbleAnimation(bubble, toVisible) : null;
        if (animation == null) {
            animation = new FastScrollBubbleVisibilityAnimation(bubble, toVisible);
        }

        return animation;
    }

    /**
     * Retrieves the animation for hiding or showing the drag handle
     *
     * @param handle The view representing the handle to animate
     * @param toVisible {@code true} if the animation should show the handle
     * @return The animation for hiding or showing the handle
     */
    @NonNull
    protected Animation getHandleAnimation(@NonNull View handle, final boolean toVisible) {
        Animation animation = animationProvider != null ? animationProvider.getHandleAnimation(handle, toVisible) : null;
        if (animation == null) {
            animation = new FastScrollHandleVisibilityAnimation(handle, toVisible);
        }

        return animation;
    }

    /**
     * Tints the {@code drawable} with the {@code color}
     *
     * @param drawable The drawable to ting
     * @param color The color to tint the {@code drawable} with
     * @return The tinted {@code drawable}
     */
    protected Drawable tint(@Nullable Drawable drawable, @ColorInt int color) {
        if (drawable != null) {
            drawable.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY));
        }

        return drawable;
    }

    /**
     * A utility method to retrieve a drawable that correctly abides by the
     * theme in Lollpop (API 23) +
     *
     * @param res The resource id for the drawable
     * @return The drawable associated with {@code res}
     */
    protected Drawable getDrawable(@DrawableRes int res) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return getResources().getDrawable(res, getContext().getTheme());
        }

        //noinspection deprecation
        return getResources().getDrawable(res);
    }

    /**
     * A utility method to retrieve a color that correctly abides by the
     * theme in Marshmallow (API 23) +
     *
     * @param res The resource id associated with the requested color
     * @return The integer representing the color associated with {@code res}
     */
    @ColorInt
    protected int getColor(@ColorRes int res) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return getResources().getColor(res, getContext().getTheme());
        }

        //noinspection deprecation
        return getResources().getColor(res);
    }

    /**
     * Enforces the restrictions on range provided by {@code min} and {@code max}
     * on {@code value}. If {@code value} is greater than {@code max} then the result will
     * be {@code max}. Likewise if {@code value} is less than {@code min} then the result
     * will be {@code min}.
     *
     * @param min The minimum amount {@code value} can represent
     * @param max The maximum amount {@code value} can represent
     * @param value The amount to constrain
     * @return {@code value}, {@code min} or {@code max} when constrained
     */
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

    /**
     * Handles the calculations and overall functionality of smoothly scrolling
     * between the current position and the requested position when dragging
     * the handle. By default this will use {@value DEFAULT_TOTAL_SCROLL_TIME}
     * as the time it takes to scroll between the two points.
     */
    protected class FastSmoothScroller extends LinearSmoothScroller {
        protected static final int DEFAULT_TOTAL_SCROLL_TIME = 50; //Milliseconds
        protected int totalScrollTime = DEFAULT_TOTAL_SCROLL_TIME;

        public FastSmoothScroller(Context context) {
            super(context);
        }

        /**
         * Sets the time it takes to scroll between the current position and
         * the requested position when dragging the handle
         *
         * @param milliseconds The time in milliseconds to scroll [default: {@value DEFAULT_TOTAL_SCROLL_TIME}]
         */
        public void setTotalScrollTime(int milliseconds) {
            this.totalScrollTime = milliseconds;
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

    /**
     * Runnable used to delay the hiding of the drag handle
     */
    protected class HandleHideRunnable implements Runnable {
        @Override
        public void run() {
            updateHandleVisibility(false);
        }
    }

    /**
     * Runnable used to delay the hiding of the popup bubble
     */
    protected class BubbleHideRunnable implements Runnable {
        @Override
        public void run() {
            updateBubbleVisibility(false);
        }
    }

    /**
     * A contract that allows the user to provide particular Fast Scroll
     * animations for hiding and showing the drag handle and popup bubble
     */
    public interface AnimationProvider {
        /**
         * Retrieves the animation to use for showing or hiding the popup bubble.
         * By default this uses the simple alpha animation {@link FastScrollBubbleVisibilityAnimation}
         *
         * @param bubble The view that represents the popup bubble
         * @param toVisible {@code true} if the returned animation should handle showing the bubble
         * @return The custom animation for hiding or showing the popup bubble, null to use the default
         */
        @Nullable
        Animation getBubbleAnimation(@NonNull View bubble, boolean toVisible);

        /**
         * Retrieves the animation to use for showing or hiding the drag handle.
         * By default this uses the simple alpha animation {@link FastScrollHandleVisibilityAnimation}
         *
         * @param handle The view that represents the drag handle
         * @param toVisible {@code true} if the returned animation should handle showing the drag handle
         * @return The custom animation for hiding or showing the drag handle, null to use the default
         */
        @Nullable
        Animation getHandleAnimation(@NonNull View handle, boolean toVisible);
    }

    /**
     * Callback used to request the title for the fast scroll bubble
     * when enabled.
     */
    public interface FastScrollPopupCallbacks {
        String getFastScrollPopupText(int position);
    }

    /**
     * Alignment types associated with the popup bubble (see {@link #setShowBubble(boolean)}
     */
    public enum BubbleAlignment {
        /**
         * The top of the popup bubble is even with the top of the drag handle
         */
        TOP,

        /**
         * The center (y) of the popup bubble is even with the center (y) of the drag handle
         */
        CENTER,

        /**
         * The bottom of the popup bubble is even with the bottom of the drag handle
         */
        BOTTOM,

        /**
         * The bottom of the popup bubble is even with the top of the drag handle
         */
        BOTTOM_TO_TOP,

        /**
         * The top of the popup bubble is even with the bottom of the drag handle
         */
        TOP_TO_BOTTOM,

        /**
         * The bottom of the popup bubble is even with the center (y) of the drag handle
         */
        BOTTOM_TO_CENTER;

        private static BubbleAlignment get(int index) {
            return BubbleAlignment.values()[index];
        }
    }
}