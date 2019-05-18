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
package com.money.manager.ex.reports;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.money.manager.ex.R;
import com.money.manager.ex.core.UIHelper;
import com.money.manager.ex.currency.CurrencyService;
import com.money.manager.ex.database.ViewMobileData;

import androidx.cursoradapter.widget.CursorAdapter;
import info.javaperformance.money.MoneyFactory;

/**
 * Adapter for the Categories report.
 */
public class CategoriesReportAdapter
    extends CursorAdapter {

    private LayoutInflater mInflater;
    private Context mContext;

//    @SuppressWarnings("deprecation")
    public CategoriesReportAdapter(Context context, Cursor c) {
        super(context, c, false);

        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContext = context;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView txtColumn1 = (TextView) view.findViewById(R.id.textViewColumn1);
        TextView txtColumn2 = (TextView) view.findViewById(R.id.textViewColumn2);

        double total = cursor.getDouble(cursor.getColumnIndex("TOTAL"));
        String column1;
        String category = cursor.getString(cursor.getColumnIndex(ViewMobileData.Category));
        if (!TextUtils.isEmpty(category)) {
            column1 = "<b>" + category + "</b>";
            String subCategory = cursor.getString(cursor.getColumnIndex(ViewMobileData.Subcategory));
            if (!TextUtils.isEmpty(subCategory)) {
                column1 += " : " + subCategory;
            }
        } else {
            column1 = "<i>" + context.getString(R.string.empty_category);
        }
        txtColumn1.setText(UIHelper.fromHtml(column1));

        CurrencyService currencyService = new CurrencyService(mContext);

        txtColumn2.setText(currencyService.getCurrencyFormatted(currencyService.getBaseCurrencyId(), MoneyFactory.fromDouble(total)));
        UIHelper uiHelper = new UIHelper(context);
        if (total < 0) {
            txtColumn2.setTextColor(uiHelper.getColor(uiHelper.resolveAttribute(R.attr.holo_red_color_theme)));
        } else {
            txtColumn2.setTextColor(uiHelper.getColor(uiHelper.resolveAttribute(R.attr.holo_green_color_theme)));
        }

        //view.setBackgroundColor(core.resolveColorAttribute(cursor.getPosition() % 2 == 1 ? R.attr.row_dark_theme : R.attr.row_light_theme));
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup root) {
        return mInflater.inflate(R.layout.item_generic_report_2_columns, root, false);
    }
}
