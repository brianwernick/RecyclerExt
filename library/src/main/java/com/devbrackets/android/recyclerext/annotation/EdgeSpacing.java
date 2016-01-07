package com.devbrackets.android.recyclerext.annotation;

import android.support.annotation.IntDef;

import com.devbrackets.android.recyclerext.decoration.SpacerDecoration;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef(flag = true, value = {
        SpacerDecoration.EDGE_SPACING_TOP,
        SpacerDecoration.EDGE_SPACING_RIGHT,
        SpacerDecoration.EDGE_SPACING_BOTTOM,
        SpacerDecoration.EDGE_SPACING_LEFT})
@Retention(RetentionPolicy.SOURCE)
public @interface EdgeSpacing {
    //Purposefully left blank
}
