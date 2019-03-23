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

package com.money.manager.ex.core;

import android.content.Context;
import android.database.Cursor;
import androidx.core.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.money.manager.ex.R;

/**
 * Custom adapter for the account list in the transaction list toolbar.
 */
public class ToolbarSpinnerAdapter
    extends SimpleCursorAdapter {
    public ToolbarSpinnerAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
        super(context, layout, c, from, to, flags);

        mContext = context;
    }

    private Context mContext;

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent){
        View view = super.getDropDownView(position, convertView, parent);

        TextView textView = (TextView) view.findViewById(android.R.id.text1);

//        int textColor = getContext().getResources().getColor(R.color.material_grey_900);
        int textColor = ContextCompat.getColor(getContext(), R.color.material_grey_900);
//        int textColorId = core.getColourFromThemeAttribute(R.attr.toolbar_spinner_item_text_color);
//        int textColorId = core.getColourFromAttribute(R.attr.toolbar_spinner_item_text_color);
//        int textColor = mContext.getResources().getColor(textColorId);
        if (new UIHelper(getContext()).isUsingDarkTheme()) {
            textColor = ContextCompat.getColor(getContext(), R.color.material_grey_50);
//            textColor = mContext.getResources().getColor(R.color.material_grey_50);
        }

        textView.setTextColor(textColor);

        return view;
    }

    public Context getContext() {
        return mContext;
    }
}
