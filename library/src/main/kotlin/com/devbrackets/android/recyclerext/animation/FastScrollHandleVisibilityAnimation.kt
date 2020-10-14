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
package com.devbrackets.android.recyclerext.animation

import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.TranslateAnimation

class FastScrollHandleVisibilityAnimation(handle: View, protected val toVisible: Boolean) : AnimationSet(false) {
    protected fun setup(handle: View) {
        val xDelta = handle.width.toFloat()
        val startPos = if (toVisible) xDelta else 0f
        val endPos = if (toVisible) 0f else xDelta
        val translateAnimation = TranslateAnimation(startPos, endPos, 0f, 0f)
        translateAnimation.duration = DURATION.toLong()
        addAnimation(translateAnimation)
        setAnimationListener(HandleAnimationListener(handle, toVisible))

        //Works around the issue of the animation never starting because the view is GONE
        if (handle.visibility == View.GONE) {
            handle.visibility = View.INVISIBLE
        }
    }

    /**
     * Listens to the [FastScrollBubbleVisibilityAnimation]
     * making sure the handle has the correct visibilities at the start and end of the animation
     */
    protected class HandleAnimationListener(protected var handle: View, protected var toVisible: Boolean) : AnimationListener {
        override fun onAnimationStart(animation: Animation) {
            handle.visibility = View.VISIBLE
        }

        override fun onAnimationEnd(animation: Animation) {
            handle.visibility = if (toVisible) View.VISIBLE else View.INVISIBLE
        }

        override fun onAnimationRepeat(animation: Animation) {
            //Purposefully left blank
        }
    }

    companion object {
        protected const val DURATION = 250 //milliseconds
    }

    init {
        setup(handle)
    }
}