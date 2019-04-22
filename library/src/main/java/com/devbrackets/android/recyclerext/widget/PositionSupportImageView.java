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

package com.devbrackets.android.recyclerext.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import android.util.AttributeSet;

/**
 * A basic extension to the {@link AppCompatImageView} to add backwards compatibility for
 * the getX() and getY() methods.
 */
public class PositionSupportImageView extends AppCompatImageView {
    public PositionSupportImageView(Context context) {
        super(context);
    }

    public PositionSupportImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PositionSupportImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public float getY() {
        return super.getY();
    }

    public void setY(float y) {
        super.setY(y);
    }

    public float getX() {
        return super.getX();
    }

    public void setX(float x) {
        super.setX(x);
    }

    public void setBackground(@Nullable Drawable drawable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            super.setBackground(drawable);
        } else {
            //noinspection deprecation
            setBackgroundDrawable(drawable);
        }
    }
}