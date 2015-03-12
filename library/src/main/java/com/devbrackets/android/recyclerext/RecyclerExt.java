/*
 * Copyright (C) 2015 Lucas Rocha (TwoWayView), Brian Wernick
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

package com.devbrackets.android.recyclerext;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;

import com.devbrackets.android.recyclerext.layout.BaseLayoutManager;
import com.devbrackets.android.recyclerext.layout.LayoutOrientation;

import java.lang.reflect.Constructor;

/**
 *
 */
public class RecyclerExt extends RecyclerView {
    public RecyclerExt(Context context) {
        this(context, null);
    }

    public RecyclerExt(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RecyclerExt(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.recyclerExt, defStyle, 0);

        String name = a.getString(R.styleable.recyclerExt_layoutManager);
        if (!TextUtils.isEmpty(name)) {
            loadLayoutManagerFromName(context, attrs, name);
        }

        a.recycle();
    }

    private void loadLayoutManagerFromName(Context context, AttributeSet attrs, String name) {
        try {
            final int dotIndex = name.indexOf('.');
            if (dotIndex == -1) {
                name = "com.devbrackets.android.recyclerext.layout." + name;
            } else if (dotIndex == 0) {
                final String packageName = context.getPackageName();
                name = packageName + "" + name;
            }

            Class<?>[] constructorSignature = new Class[]{Context.class, AttributeSet.class};
            Object[] constructionArgs = new Object[]{context, attrs};

            Class<? extends BaseLayoutManager> clazz = context.getClassLoader().loadClass(name).asSubclass(BaseLayoutManager.class);
            Constructor<? extends BaseLayoutManager> constructor = clazz.getConstructor(constructorSignature);

            setLayoutManager(constructor.newInstance(constructionArgs));
        } catch (Exception e) {
            throw new IllegalStateException("Could not load BaseLayoutManager from class: " + name, e);
        }
    }

    @Override
    public void setLayoutManager(LayoutManager layout) {
        if (!(layout instanceof BaseLayoutManager)) {
            throw new IllegalArgumentException("RecyclerExt can only use BaseLayoutManager subclasses as its layout manager");
        }

        super.setLayoutManager(layout);
    }

    public LayoutOrientation getOrientation() {
        BaseLayoutManager layout = (BaseLayoutManager) getLayoutManager();
        return layout.getOrientation();
    }

    public void setOrientation(LayoutOrientation orientation) {
        BaseLayoutManager layout = (BaseLayoutManager) getLayoutManager();
        layout.setOrientation(orientation);
    }

    public int getFirstVisiblePosition() {
        BaseLayoutManager layout = (BaseLayoutManager) getLayoutManager();
        return layout.getFirstVisiblePosition();
    }

    public int getLastVisiblePosition() {
        BaseLayoutManager layout = (BaseLayoutManager) getLayoutManager();
        return layout.getLastVisiblePosition();
    }
}