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
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;

public class FastScrollHandleVisibilityAnimation extends AnimationSet {
    private static final int DURATION = 250; //milliseconds
    private final boolean toVisible;

    public FastScrollHandleVisibilityAnimation(@NonNull View handle, boolean toVisible) {
        super(false);

        this.toVisible = toVisible;
        setup(handle);
    }

    private void setup(@NonNull View handle) {
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
