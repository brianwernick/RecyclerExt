package com.devbrackets.android.recyclerext;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * A class that provides the functionality of a fast scroll
 * for the attached {@link android.support.v7.widget.RecyclerView}
 */
public class FastScroll extends FrameLayout {
    private static final String TAG = "FastScroll";
    private static final int TRACK_SNAP_RANGE = 5;

    public interface FastScrollPopupCallbacks {
        String getFastScrollPopupText(int position);
    }

    private SupportImageView handle;
    private TextView bubble;

    private RecyclerView recyclerView;
    private FastScrollPopupCallbacks popupCallbacks;

    private int height;

    private Animation currentAnimation;
    private FastScrollListener scrollListener = new FastScrollListener();

    public FastScroll(Context context) {
        super(context);
        init(context);
    }

    public FastScroll(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public FastScroll(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public FastScroll(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (recyclerView != null) {
            recyclerView.removeOnScrollListener(scrollListener);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
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
        if (!(recyclerView.getAdapter() instanceof FastScrollPopupCallbacks)) {
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

    protected void init(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.recyclerext_fast_scroll, this, true);

        bubble = (TextView) findViewById(R.id.recyclerext_fast_scroll_bubble);
        handle = (SupportImageView) findViewById(R.id.recyclerext_fast_scroll_handle);

        bubble.setVisibility(View.GONE);
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
            updateBubbleY(getValueInRange(0, height - bubbleHeight - handleHeight / 2, (int) (y - bubbleHeight)));
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
        if (currentAnimation != null) {
            bubble.clearAnimation();
        }

        Log.d(TAG, "updating bubble visibility" + toVisible);
        currentAnimation = new BubbleVisibilityAnimation(bubble, toVisible);
        bubble.startAnimation(currentAnimation);
    }

    protected void updateBubbleY(float y) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            bubble.setY(y);
        } else {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) bubble.getLayoutParams();
            params.topMargin = (int)y;
            bubble.setLayoutParams(params);
        }
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

            Log.d(TAG, "proportion: " + proportion + ", verticalOffset: " + verticalScrollOffset);
        }
    }

    /**
     * Updates the visibility of the <code>bubble</code>
     */
    protected static class BubbleVisibilityAnimation extends AnimationSet {
        private static final int DURATION = 100; //milliseconds
        private final boolean toVisible;

        public BubbleVisibilityAnimation(View bubble, boolean toVisible) {
            super(false);

            this.toVisible = toVisible;
            setup(bubble);
        }

        private void setup(View bubble) {
            float startAlpha = toVisible ? 0 : 1;
            float endAlpha = toVisible ? 1 : 0;

            AlphaAnimation alphaAnimation = new AlphaAnimation(startAlpha, endAlpha);
            alphaAnimation.setDuration(DURATION);
            addAnimation(alphaAnimation);

            setAnimationListener(new BubbleVisibilityAnimationListener(bubble, toVisible));
        }
    }

    /**
     * Listens to the {@link com.devbrackets.android.recyclerext.FastScroll.BubbleVisibilityAnimation}
     * making sure the bubble has the correct visibilities at the start and end of the animation
     */
    protected static class BubbleVisibilityAnimationListener implements Animation.AnimationListener {
        private View bubble;
        private boolean toVisible;

        public BubbleVisibilityAnimationListener(View bubble, boolean toVisible) {
            this.bubble = bubble;
            this.toVisible = toVisible;
        }

        @Override
        public void onAnimationStart(Animation animation) {
            bubble.setVisibility(View.VISIBLE);
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            bubble.setVisibility(toVisible ? View.VISIBLE : View.GONE);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
            //Purposefully left blank
        }
    }

    /**
     * A basic extension to the {@link ImageView} to add backwards compatibility for
     * the getX() and getY() methods.
     */
    public static class SupportImageView extends ImageView {
        public SupportImageView(Context context) {
            super(context);
        }

        public SupportImageView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public SupportImageView(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        public SupportImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
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
    }
}