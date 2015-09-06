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
package com.money.manager.ex.reports;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.money.manager.ex.R;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.currency.CurrencyService;
import com.money.manager.ex.database.ViewMobileData;

/**
 * Adapter for the Payee report.
 */
public class PayeeReportAdapter extends CursorAdapter {
    private LayoutInflater mInflater;

    @SuppressWarnings("deprecation")
    public PayeeReportAdapter(Context context, Cursor c) {
        super(context, c);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView txtColumn1 = (TextView) view.findViewById(R.id.textViewColumn1);
        TextView txtColumn2 = (TextView) view.findViewById(R.id.textViewColumn2);
        double total = cursor.getDouble(cursor.getColumnIndex("TOTAL"));
        if (!TextUtils.isEmpty(cursor.getString(cursor.getColumnIndex(ViewMobileData.Payee)))) {
            txtColumn1.setText(cursor.getString(cursor.getColumnIndex(ViewMobileData.Payee)));
        } else {
            txtColumn1.setText(context.getString(R.string.empty_payee));
        }

        CurrencyService currencyService = new CurrencyService(mContext);

        txtColumn2.setText(currencyService.getCurrencyFormatted(currencyService.getBaseCurrencyId(), total));
        Core core = new Core(context);
        if (total < 0) {
            txtColumn2.setTextColor(context.getResources().getColor(core.resolveIdAttribute(R.attr.holo_red_color_theme)));
        } else {
            txtColumn2.setTextColor(context.getResources().getColor(core.resolveIdAttribute(R.attr.holo_green_color_theme)));
        }

        view.setBackgroundColor(core.resolveColorAttribute(cursor.getPosition() % 2 == 1 ? R.attr.row_dark_theme : R.attr.row_light_theme));
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup root) {
        return mInflater.inflate(R.layout.item_generic_report_2_columns, root, false);
    }
}
