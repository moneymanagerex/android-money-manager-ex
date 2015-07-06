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
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Typeface;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.money.manager.ex.R;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.currency.CurrencyUtils;
import com.money.manager.ex.database.QueryReportIncomeVsExpenses;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Adapter
 * Created by Alen Siljak on 6/07/2015.
 */
public class IncomeVsExpensesAdapter
        extends CursorAdapter {

    private LayoutInflater mInflater;

    @SuppressWarnings("deprecation")
    public IncomeVsExpensesAdapter(Context context, Cursor c) {
        super(context, c);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView txtYear = (TextView) view.findViewById(R.id.textViewYear);
        TextView txtMonth = (TextView) view.findViewById(R.id.textViewMonth);
        TextView txtIncome = (TextView) view.findViewById(R.id.textViewIncome);
        TextView txtExpenses = (TextView) view.findViewById(R.id.textViewExpenses);
        TextView txtDifference = (TextView) view.findViewById(R.id.textViewDifference);
        // take data
        int year, month;
        year = cursor.getInt(cursor.getColumnIndex(QueryReportIncomeVsExpenses.Year));
        month = cursor.getInt(cursor.getColumnIndex(QueryReportIncomeVsExpenses.Month));
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, 1);
        double income = 0, expenses = 0;
        expenses = cursor.getDouble(cursor.getColumnIndex(QueryReportIncomeVsExpenses.Expenses));
        income = cursor.getDouble(cursor.getColumnIndex(QueryReportIncomeVsExpenses.Income));
        // attach data
        txtYear.setText(Integer.toString(year));
        //txtMonth.setText(new SimpleDateFormat("MMMM").format(new Date(year, month - 1, 1)));
        String formatMonth = context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ? "MMM" : "MMMM";

        if (month != IncomeVsExpensesActivity.SUBTOTAL_MONTH) {
            txtMonth.setText(new SimpleDateFormat(formatMonth).format(calendar.getTime()));
        } else {
            txtMonth.setText(null);
        }
        CurrencyUtils currencyUtils = new CurrencyUtils(mContext);

        txtIncome.setText(currencyUtils.getCurrencyFormatted(currencyUtils.getBaseCurrencyId(), income));
        txtExpenses.setText(currencyUtils.getCurrencyFormatted(currencyUtils.getBaseCurrencyId(), Math.abs(expenses)));
        txtDifference.setText(currencyUtils.getCurrencyFormatted(currencyUtils.getBaseCurrencyId(), income - Math.abs(expenses)));

        Core core = new Core(context);
        if (income - Math.abs(expenses) < 0) {
            txtDifference.setTextColor(context.getResources().getColor(core.resolveIdAttribute(R.attr.holo_red_color_theme)));
        } else {
            txtDifference.setTextColor(context.getResources().getColor(core.resolveIdAttribute(R.attr.holo_green_color_theme)));
        }
        //view.setBackgroundColor(core.resolveColorAttribute(cursor.getPosition() % 2 == 1 ? R.attr.row_dark_theme : R.attr.row_light_theme));
        // check if subtotal
        int typefaceStyle = month == IncomeVsExpensesActivity.SUBTOTAL_MONTH ? Typeface.BOLD : Typeface.NORMAL;

        txtDifference.setTypeface(null, typefaceStyle);
        txtExpenses.setTypeface(null, typefaceStyle);
        txtIncome.setTypeface(null, typefaceStyle);
        txtMonth.setTypeface(null, typefaceStyle);
        txtYear.setTypeface(null, typefaceStyle);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return mInflater.inflate(R.layout.tablerow_income_vs_expenses, parent, false);
    }
}
