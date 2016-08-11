package com.devbrackets.android.recyclerext.animation;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;

public class FastScrollHandleVisibilityAnimation extends AnimationSet {
    private static final int DURATION = 250; //milliseconds
    private final boolean toVisible;

    public FastScrollHandleVisibilityAnimation(View handle, boolean toVisible) {
        super(false);

        this.toVisible = toVisible;
        setup(handle);
    }

    private void setup(View handle) {
        float xDelta = handle.getWidth();

        float startPos = toVisible ? xDelta : 0F;
        float endPos = toVisible ? 0F : xDelta;

        TranslateAnimation translateAnimation = new TranslateAnimation(startPos, endPos, 0F, 0F);
        translateAnimation.setDuration(DURATION);
        addAnimation(translateAnimation);

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
