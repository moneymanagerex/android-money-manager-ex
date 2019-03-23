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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.money.manager.ex.R;

/**
 * Used to display dates. Allows quickly moving up/down using side arrows.
 */
public class DateDisplay extends LinearLayout {
    public DateDisplay(Context context) {
        super(context);

        initialize(context);
    }

//    public DateDisplay(Context context, AttributeSet attrs, int defStyle) {
//        super(context, attrs, defStyle);
//
//        initialize(context);
//    }

    public DateDisplay(Context context, AttributeSet attrs) {
        super(context, attrs);

//        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Options, 0, 0);
//        String titleText = a.getString(R.styleable.Options_titleText);
//        int valueColor = a.getColor(R.styleable.Options_valueColor, android.R.color.holo_blue_light);
//        a.recycle();

        initialize(context);
    }

    private void initialize(Context context) {
        setOrientation(LinearLayout.HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.control_datedisplay, this, true);
    }
}
