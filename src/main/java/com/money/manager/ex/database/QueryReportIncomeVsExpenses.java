/*
 * Copyright (C) 2012-2015 The Android Money Manager Ex Project Team
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
 *
 */
package com.money.manager.ex.database;

import android.content.Context;

import com.money.manager.ex.Constants;
import com.money.manager.ex.R;
import com.money.manager.ex.utils.RawFileUtils;

import org.apache.commons.lang3.StringUtils;

public class QueryReportIncomeVsExpenses
    extends Dataset {

    //field name
    public static final String Year = "Year";
    public static final String Month = "Month";
    public static final String Income = "Income";
    public static final String Expenses = "Expenses";
    public static final String Transfers = "Transfers";

    public QueryReportIncomeVsExpenses(Context context) {
        super("", DatasetType.QUERY, "report_income_vs_expenses");

        this.mContext = context.getApplicationContext();

        initialize(context, null);
    }

    private Context mContext;

    @Override
    public String[] getAllColumns() {
        return new String[]{"ROWID AS _id", Year, Month, Income, Expenses, Transfers};
    }

    /**
     * add a WHERE clause for the base (mobiledata) query
     * @param whereStatement where statement to use for filtering the underlying data
     */
     public void filterTransactionsSource(String whereStatement) {
         // add WHERE statements to the base query (mobiledata)
         initialize(mContext, whereStatement);
     }

    private void initialize(Context context, String whereStatement) {
        ViewMobileData mobileData = new ViewMobileData(context);
        // add where statement
        if(!StringUtils.isEmpty(whereStatement)) {
            mobileData.setWhere(whereStatement);
        }
        String mobileDataQuery = mobileData.getSource();

        // assemble the source statement by combining queries.
        String source = RawFileUtils.getRawAsString(context, R.raw.report_income_vs_expenses);
        source = source.replace(Constants.MOBILE_DATA_PATTERN, mobileDataQuery);

        this.setSource(source);
    }
}
