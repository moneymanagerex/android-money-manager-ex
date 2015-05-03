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
package com.money.manager.ex.database;

import android.content.Context;

import com.money.manager.ex.R;
import com.money.manager.ex.utils.RawFileUtils;

public class QueryReportIncomeVsExpenses extends Dataset {
    //field name
    public static final String Year = "Year";
    public static final String Month = "Month";
    public static final String Income = "Income";
    public static final String Expenses = "Expenses";
    public static final String Transfers = "Transfers";

    public QueryReportIncomeVsExpenses(Context context) {
        super(RawFileUtils.getRawAsString(context, R.raw.report_income_vs_expenses), DatasetType.QUERY, "report_income_vs_expenses");
    }

    @Override
    public String[] getAllColumns() {
        return new String[]{"ROWID AS _id", Year, Month, Income, Expenses, Transfers};
    }
}
