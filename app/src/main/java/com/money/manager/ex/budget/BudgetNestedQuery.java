package com.money.manager.ex.budget;

import android.content.Context;

import com.money.manager.ex.R;
import com.money.manager.ex.database.Dataset;
import com.money.manager.ex.database.DatasetType;
import com.money.manager.ex.utils.MmxFileUtils;

public class BudgetNestedQuery
            extends Dataset {

    public BudgetNestedQuery(Context context) {
            super(MmxFileUtils.getRawAsString(context, R.raw.query_budgets_nestedcategory), DatasetType.QUERY,
                    BudgetNestedQuery.class.getSimpleName());

            this.mContext = context;
        }

        public static String BUDGETENTRYID = "BUDGETENTRYID";
        public static String BUDGETYEARID = "BUDGETYEARID";
        public static String CATEGID = "CATEGID";
        public static String CATEGNAME = "CATEGNAME";
        public static String PERIOD = "PERIOD";
        public static String AMOUNT = "AMOUNT";

        private final Context mContext;

        // get all columns
        @Override
        public String[] getAllColumns() {
            return new String[]{ BUDGETENTRYID + " AS _id",
                    BUDGETENTRYID,
                    BUDGETYEARID,
                    CATEGID,
                    CATEGNAME,
                    PERIOD,
                    AMOUNT
            };
        }

    }
