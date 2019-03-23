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

package com.money.manager.ex.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.money.manager.ex.R;
import com.money.manager.ex.view.RobotoTextView;

/**
 * View Holder for subcategory list item.
 */
public class CategoryListItemViewHolderChild {
    public CategoryListItemViewHolderChild(View view) {
//        textContainer = (ViewGroup) view.findViewById(R.id.textContainer);
        text1 = (RobotoTextView) view.findViewById(android.R.id.text1);
        text2 = (TextView) view.findViewById(android.R.id.text2);
        selector = (LinearLayout) view.findViewById(R.id.selector);
        indent = (ViewGroup) view.findViewById(R.id.indent);
    }

//    public ViewGroup textContainer;
    public RobotoTextView text1;
    public TextView text2;
    public LinearLayout selector;
    public ViewGroup indent;
}
