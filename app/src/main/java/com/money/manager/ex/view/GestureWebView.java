/*
 * Copyright (C) 2012-2019 The Android Money Manager Ex Project Team
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

package com.money.manager.ex.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.webkit.WebView;

/**
 * WebView with gesture listener. Allows handling custom gestures like long-press, double-tap, etc.
 */
public class GestureWebView
    extends WebView {

    public GestureWebView(Context context, AttributeSet attrs) {
        super(context, attrs);

//        this.context = context;
        this.gestureDetector = initializeGestureDetector(context);
    }

    //private Context context;
    private GestureDetector gestureDetector;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    private GestureDetector initializeGestureDetector(Context context) {
        GestureDetector.SimpleOnGestureListener listener = new GestureDetector.SimpleOnGestureListener() {
            public boolean onDown(MotionEvent event) {
                return true;
            }

            public void onLongPress(MotionEvent event) {
                // todo Trigger context menu
                Log.d("test", "test");
            }
        };

        GestureDetector detector = new GestureDetector(context, listener);
        return detector;
    }
}
