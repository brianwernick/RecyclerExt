package com.devbrackets.android.recyclerext.animation;

import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;


public class FastScrollBubbleVisibilityAnimation extends AnimationSet {
    private static final int DURATION = 100; //milliseconds
    private final boolean toVisible;

    public FastScrollBubbleVisibilityAnimation(View bubble, boolean toVisible) {
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

    /**
     * Listens to the {@link FastScrollBubbleVisibilityAnimation}
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
}