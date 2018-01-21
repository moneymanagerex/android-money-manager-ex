/*
 * Copyright (C) 2012-2018 The Android Money Manager Ex Project Team
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
package com.money.manager.ex.database;

import android.content.Context;
import android.text.TextUtils;

import com.money.manager.ex.Constants;
import com.money.manager.ex.R;
import com.money.manager.ex.utils.MmxFileUtils;
import com.money.manager.ex.viewmodels.IncomeVsExpenseReportEntity;

public class QueryReportIncomeVsExpenses
    extends Dataset {

    public QueryReportIncomeVsExpenses(Context context) {
        super("", DatasetType.QUERY, "report_income_vs_expenses");

        initialize(context, null);
    }

    @Override
    public String[] getAllColumns() {
        return new String[]{"ROWID AS _id",
            IncomeVsExpenseReportEntity.YEAR,
            IncomeVsExpenseReportEntity.Month,
            IncomeVsExpenseReportEntity.Income,
            IncomeVsExpenseReportEntity.Expenses,
            IncomeVsExpenseReportEntity.Transfers};
    }

    private void initialize(Context context, String whereStatement) {
        ViewMobileData mobileData = new ViewMobileData(context);
        // add where statement
        if(!TextUtils.isEmpty(whereStatement)) {
            mobileData.setWhere(whereStatement);
        }
        String mobileDataQuery = mobileData.getSource();

        // assemble the source statement by combining queries.
        String source = MmxFileUtils.getRawAsString(context, R.raw.report_income_vs_expenses);
        source = source.replace(Constants.MOBILE_DATA_PATTERN, mobileDataQuery);

        this.setSource(source);
    }
}
