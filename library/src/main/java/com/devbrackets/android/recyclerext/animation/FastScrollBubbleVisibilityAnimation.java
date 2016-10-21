/*
 * Copyright (C) 2016 Brian Wernick
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.devbrackets.android.recyclerext.animation;

import android.support.annotation.NonNull;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;


public class FastScrollBubbleVisibilityAnimation extends AnimationSet {
    private static final int DURATION = 100; //milliseconds
    private final boolean toVisible;

    public FastScrollBubbleVisibilityAnimation(@NonNull View bubble, boolean toVisible) {
        super(false);

        this.toVisible = toVisible;
        setup(bubble);
    }

    private void setup(@NonNull View bubble) {
        float startAlpha = toVisible ? 0 : 1;
        float endAlpha = toVisible ? 1 : 0;

        AlphaAnimation alphaAnimation = new AlphaAnimation(startAlpha, endAlpha);
        alphaAnimation.setDuration(DURATION);
        addAnimation(alphaAnimation);

        setAnimationListener(new BubbleVisibilityAnimationListener(bubble, toVisible));

        //Works around the issue of the animation never starting because the view is GONE
        if (bubble.getVisibility() == View.GONE) {
            bubble.setVisibility(View.INVISIBLE);
        }
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