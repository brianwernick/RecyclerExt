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
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationSet

class FastScrollBubbleVisibilityAnimation(bubble: View, private val toVisible: Boolean) : AnimationSet(false) {
  companion object {
    private const val DURATION = 100 //milliseconds
  }

  init {
    setup(bubble)
  }

  private fun setup(bubble: View) {
    val startAlpha: Float = if (toVisible) 0F else 1F
    val endAlpha: Float = if (toVisible) 1F else 0F
    val alphaAnimation = AlphaAnimation(startAlpha, endAlpha)

    alphaAnimation.duration = DURATION.toLong()
    addAnimation(alphaAnimation)
    setAnimationListener(BubbleVisibilityAnimationListener(bubble, toVisible))

    //Works around the issue of the animation never starting because the view is GONE
    if (bubble.visibility == View.GONE) {
      bubble.visibility = View.INVISIBLE
    }
  }

  /**
   * Listens to the [FastScrollBubbleVisibilityAnimation]
   * making sure the bubble has the correct visibilities at the start and end of the animation
   */
  protected class BubbleVisibilityAnimationListener(private val bubble: View, private val toVisible: Boolean) : AnimationListener {
    override fun onAnimationStart(animation: Animation) {
      bubble.visibility = View.VISIBLE
    }

    override fun onAnimationEnd(animation: Animation) {
      bubble.visibility = if (toVisible) View.VISIBLE else View.GONE
    }

    override fun onAnimationRepeat(animation: Animation) {
      //Purposefully left blank
    }
  }
}