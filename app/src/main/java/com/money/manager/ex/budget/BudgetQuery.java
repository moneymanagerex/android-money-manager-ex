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
package com.money.manager.ex.budget;

import android.content.Context;

import com.money.manager.ex.R;
import com.money.manager.ex.database.Dataset;
import com.money.manager.ex.database.DatasetType;
import com.money.manager.ex.utils.MmxFileUtils;

/**
 * Entity for displaying budget details list.
 * Created by Alen Siljak on 5/07/2015.
 */
public class BudgetQuery
        extends Dataset {

    public BudgetQuery(Context context) {
        super(MmxFileUtils.getRawAsString(context, R.raw.query_budgets), DatasetType.QUERY,
                BudgetQuery.class.getSimpleName());

        this.mContext = context;
    }

    public static String BUDGETENTRYID = "BUDGETENTRYID";
    public static String BUDGETYEARID = "BUDGETYEARID";
    public static String CATEGID = "CATEGID";
    public static String CATEGNAME = "CATEGNAME";
    public static String SUBCATEGID = "SUBCATEGID";
    public static String SUBCATEGNAME = "SUBCATEGNAME";
    public static String PERIOD = "PERIOD";
    public static String AMOUNT = "AMOUNT";

    private Context mContext;

    // get all columns
    @Override
    public String[] getAllColumns() {
        return new String[]{ BUDGETENTRYID + " AS _id",
                BUDGETENTRYID,
                BUDGETYEARID,
                CATEGID,
                CATEGNAME,
                SUBCATEGID,
                SUBCATEGNAME,
                PERIOD,
                AMOUNT
        };
    }

}
