/*
 * Copyright (C) 2012-2015 The Android Money Manager Ex Project Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.money.manager.ex.budget;

import android.content.Context;
import android.database.Cursor;
import android.provider.Contacts;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.money.manager.ex.R;

/**
 * Adapter for budgets.
 * Created by Alen Siljak on 5/07/2015.
 */
public class BudgetAdapter
    extends SimpleCursorAdapter {

    public BudgetAdapter(Context context, Cursor cursor, String[] from, int[] to, int flags) {
        super(context, R.layout.item_budget, cursor, from, to, flags);

        mContext = context;
        mLayout = R.layout.item_budget;
    }

    /**
     * Standard constructor.
     *
     * @param context The context where the ListView associated with this
     *                SimpleListItemFactory is running
     * @param layout  resource identifier of a layout file that defines the views
     *                for this list item. The layout file should include at least
     *                those named views defined in "to"
     * @param c       The database cursor.  Can be null if the cursor is not available yet.
     * @param from    A list of column names representing the data to bind to the UI.  Can be null
     *                if the cursor is not available yet.
     * @param to      The views that should display column in the "from" parameter.
     *                These should all be TextViews. The first N views in this list
     *                are given the values of the first N columns in the from
     *                parameter.  Can be null if the cursor is not available yet.
     * @param flags   Flags used to determine the behavior of the adapter,
     *                as per {@link CursorAdapter#CursorAdapter(Context, Cursor, int)}.
     */
//    public BudgetAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
//        super(context, layout, c, from, to, flags);
//
//        mLayout = layout;
//    }

    private Context mContext;
    private int mLayout;

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

//        Cursor c = getCursor();

        final LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(mLayout, parent, false);

        return v;
    }
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        Cursor c = cursor;
        View v = view;

        // Category

        TextView categoryTextView = (TextView) v.findViewById(R.id.categoryTextView);
        if (categoryTextView != null) {
            int categoryCol = c.getColumnIndex(BudgetQuery.CATEGNAME);
            String category = c.getString(categoryCol);

            // Subcategory
            String subCategory = c.getString(c.getColumnIndex(BudgetQuery.SUBCATEGNAME));
            if (!TextUtils.isEmpty(subCategory)) {
                category += ":" + subCategory;
            }

            categoryTextView.setText(category);
        }

        // Frequency

        TextView frequencyTextView = (TextView) v.findViewById(R.id.frequencyTextView);
        if (frequencyTextView != null) {
            String text = c.getString(c.getColumnIndex(BudgetQuery.PERIOD));
            frequencyTextView.setText(text);
        }

        // Amount

        TextView amountTextView = (TextView) v.findViewById(R.id.amountTextView);
        if (amountTextView != null) {
            String text = c.getString(c.getColumnIndex(BudgetQuery.AMOUNT));
            amountTextView.setText(text);
        }

        // Estimated
        // Actual
        // todo: try to sum all the records here? or asynchronously?
    }

}
