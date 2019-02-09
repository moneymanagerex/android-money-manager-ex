/*
 * Copyright (C) 2012-2018 The Android Money Manager Ex Project Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.money.manager.ex.view.behaviors;

import android.content.Context;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;

import android.util.AttributeSet;
import android.view.View;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * Behaviour for the FAB from Design support library.
 */

public class ScrollAwareFABBehavior
    extends CoordinatorLayout.Behavior<FloatingActionButton>  {
    // FloatingActionButton.Behavior

    public ScrollAwareFABBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);

//        this.toolbarHeight = AppUtil.getToolbarHeight(context);
    }

//    private int toolbarHeight;

    @Override
    public boolean onStartNestedScroll(final CoordinatorLayout coordinatorLayout,
                                       final FloatingActionButton child,
                                       final View directTargetChild, final View target, final int nestedScrollAxes) {
        // Ensure we react to vertical scrolling
        boolean result = nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL
                || super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target, nestedScrollAxes);
        return result;
    }

    @Override
    public void onNestedScroll(CoordinatorLayout coordinatorLayout, FloatingActionButton child,
                               View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed,
                dyUnconsumed);

        if (dyConsumed > 0 && child.getVisibility() == View.VISIBLE) {
            child.hide();
        } else if (dyConsumed < 0 && child.getVisibility() != View.VISIBLE) {
            child.show();
        }
    }

//    @Override
//    public boolean layoutDependsOn(CoordinatorLayout parent, FloatingActionButton child, View dependency) {
////        return dependency instanceof Snackbar.SnackbarLayout;
//        return dependency instanceof AppBarLayout;
//    }

//    @Override
//    public boolean onDependentViewChanged(CoordinatorLayout parent, FloatingActionButton child, View dependency) {
//        float translationY = getFabTranslationYForSnackbar(parent, child);
//        float percentComplete = -translationY / dependency.getHeight();
//        float scaleFactor = 1 - percentComplete;
//
//        child.setScaleX(scaleFactor);
//        child.setScaleY(scaleFactor);
//        return false;
//    }
}
