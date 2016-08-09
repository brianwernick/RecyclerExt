package com.devbrackets.android.recyclerext.animation;

import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;

public class FastScrollHandleVisibilityAnimation extends AnimationSet {
    private static final int DURATION = 333; //milliseconds
    private final boolean toVisible;

    public FastScrollHandleVisibilityAnimation(View handle, boolean toVisible) {
        super(false);

        this.toVisible = toVisible;
        setup(handle);
    }

    private void setup(View handle) {
        float startAlpha = toVisible ? 0 : 1;
        float endAlpha = toVisible ? 1 : 0;

        AlphaAnimation alphaAnimation = new AlphaAnimation(startAlpha, endAlpha);
        alphaAnimation.setDuration(DURATION);
        addAnimation(alphaAnimation);

        setAnimationListener(new HandleAnimationListener(handle, toVisible));

        //Works around the issue of the animation never starting because the view is GONE
        if (handle.getVisibility() == View.GONE) {
            handle.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Listens to the {@link FastScrollBubbleVisibilityAnimation}
     * making sure the handle has the correct visibilities at the start and end of the animation
     */
    protected static class HandleAnimationListener implements Animation.AnimationListener {
        private View handle;
        private boolean toVisible;

        public HandleAnimationListener(View handle, boolean toVisible) {
            this.handle = handle;
            this.toVisible = toVisible;
        }

        @Override
        public void onAnimationStart(Animation animation) {
            handle.setVisibility(View.VISIBLE);
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            handle.setVisibility(toVisible ? View.VISIBLE : View.GONE);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
            //Purposefully left blank
        }
    }
}
