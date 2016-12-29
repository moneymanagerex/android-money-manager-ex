/*
 * Copyright (C) 2012-2016 The Android Money Manager Ex Project Team
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
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.money.manager.ex.Constants;
import com.money.manager.ex.MoneyManagerApplication;
import com.money.manager.ex.R;
import com.money.manager.ex.datalayer.Select;
import com.money.manager.ex.log.ExceptionHandler;
import com.money.manager.ex.currency.CurrencyService;
import com.money.manager.ex.database.SQLDataSet;
import com.money.manager.ex.database.ViewMobileData;
import com.squareup.sqlbrite.BriteDatabase;

import javax.inject.Inject;

import butterknife.ButterKnife;
import dagger.Lazy;
import info.javaperformance.money.MoneyFactory;
import timber.log.Timber;

/**
 * Adapter for budgets.
 */
public class BudgetAdapter
    extends SimpleCursorAdapter {

    /**
     * Standard constructor.
     *
     * @param context The context where the ListView associated with this
     *                SimpleListItemFactory is running
     * @param from    A list of column names representing the data to bind to the UI.  Can be null
     *                if the cursor is not available yet.
     * @param to      The views that should display column in the "from" parameter.
     *                These should all be TextViews. The first N views in this list
     *                are given the values of the first N columns in the from
     *                parameter.  Can be null if the cursor is not available yet.
     * @param flags   Flags used to determine the behavior of the adapter,
     *                as per {@link CursorAdapter#CursorAdapter(Context, Cursor, int)}.
     */
    public BudgetAdapter(Context context, Cursor cursor, String[] from, int[] to, int flags) {
        super(context, R.layout.item_budget, cursor, from, to, flags);

        mContext = context;
        mLayout = R.layout.item_budget;

        MoneyManagerApplication.getApp().iocComponent.inject(this);
    }

    @Inject Lazy<BriteDatabase> databaseLazy;

    private int mLayout;
    private String mBudgetName;

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        final LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(mLayout, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Category

        boolean hasSubcategory = false;
        TextView categoryTextView = (TextView) view.findViewById(R.id.categoryTextView);
        if (categoryTextView != null) {
            int categoryCol = cursor.getColumnIndex(BudgetQuery.CATEGNAME);
            String category = cursor.getString(categoryCol);

            // Subcategory
            String subCategory = cursor.getString(cursor.getColumnIndex(BudgetQuery.SUBCATEGNAME));
            if (!TextUtils.isEmpty(subCategory)) {
                category += ":" + subCategory;
                hasSubcategory = true;
            }

            categoryTextView.setText(category);
        }

        // Frequency

        TextView frequencyTextView = (TextView) view.findViewById(R.id.frequencyTextView);
        if (frequencyTextView != null) {
            String text = cursor.getString(cursor.getColumnIndex(BudgetQuery.PERIOD));
            frequencyTextView.setText(text);
        }

        CurrencyService currencyService = new CurrencyService(mContext);

        // Amount

        TextView amountTextView = (TextView) view.findViewById(R.id.amountTextView);
        if (amountTextView != null) {
            double amount = cursor.getDouble(cursor.getColumnIndex(BudgetQuery.AMOUNT));
            String text = currencyService.getBaseCurrencyFormatted(MoneyFactory.fromDouble(amount));
            amountTextView.setText(text);
        }

        // Estimated
        // Actual
        // todo: colour the amount depending on whether it is above/below the budgeted amount.
        TextView actualTextView = (TextView) view.findViewById(R.id.actualTextView);
        if (actualTextView != null) {
            double actual;
            if (!hasSubcategory) {
                int categoryId = cursor.getInt(cursor.getColumnIndex(BudgetQuery.CATEGID));
                actual = getAmountForCategory(categoryId);
            } else {
                int subCategoryId = cursor.getInt(cursor.getColumnIndex(BudgetQuery.SUBCATEGID));
                actual = getAmountForSubCategory(subCategoryId);
            }

            String actualString = currencyService.getBaseCurrencyFormatted(MoneyFactory.fromDouble(actual));
            actualTextView.setText(actualString);
        }
    }

    public Context getContext() {
        return mContext;
    }

    public void setBudgetName(String budgetName) {
        mBudgetName = budgetName;
    }

    private double getAmountForCategory(int categoryId) {
        double total = loadTotalFor(ViewMobileData.CATEGID + "=" + Integer.toString(categoryId));
        return total;
    }

    private double getAmountForSubCategory(int subCategoryId) {
        double total = loadTotalFor(ViewMobileData.SubcategID + "=" + Integer.toString(subCategoryId));
        return total;
    }

    private double loadTotalFor(String where) {
        double total = 0;

        int year = getYearFromBudgetName(mBudgetName);
        where += " AND " + ViewMobileData.Year + "=" + Integer.toString(year);
        int month = getMonthFromBudgetName(mBudgetName);
        if (month != Constants.NOT_SET) {
            where += " AND " + ViewMobileData.Month + "=" + Integer.toString(month);
        }

        try {
            String query = prepareQuery(where);
            Cursor cursor = databaseLazy.get().query(query);
            if (cursor == null) return 0;
            // add all the categories and subcategories together.
            while (cursor.moveToNext()) {
                total += cursor.getDouble(cursor.getColumnIndex("TOTAL"));
            }
            cursor.close();
        } catch (IllegalStateException ise) {
            Timber.e(ise, "loading category total");
        }

        return total;
    }

    private String prepareQuery(String whereClause) {
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        ViewMobileData mobileData = new ViewMobileData(getContext());

        //data to compose builder
        String[] projectionIn = new String[]{
                "ROWID AS _id", ViewMobileData.CATEGID, ViewMobileData.Category,
                ViewMobileData.SubcategID, ViewMobileData.Subcategory,
                "SUM(" + ViewMobileData.AmountBaseConvRate + ") AS TOTAL"
        };

        String selection = ViewMobileData.Status + "<>'V' AND " +
                ViewMobileData.TransactionType + " IN ('Withdrawal', 'Deposit')";
        if (!TextUtils.isEmpty(whereClause)) {
            selection += " AND " + whereClause;
        }

        String groupBy = ViewMobileData.CATEGID + ", " + ViewMobileData.Category + ", " +
                ViewMobileData.SubcategID + ", " + ViewMobileData.Subcategory;

        String having = null;
//        if (!TextUtils.isEmpty(((CategoriesReportActivity) context).mFilter)) {
//            String filter = ((CategoriesReportActivity) context).mFilter;
//            if (TransactionTypes.valueOf(filter).equals(TransactionTypes.Withdrawal)) {
//                having = "SUM(" + ViewMobileData.AmountBaseConvRate + ") < 0";
//            } else {
//                having = "SUM(" + ViewMobileData.AmountBaseConvRate + ") > 0";
//            }
//        }

        String sortOrder = ViewMobileData.Category + ", " + ViewMobileData.Subcategory;
        String limit = null;

        builder.setTables(mobileData.getSource());

        return builder.buildQuery(projectionIn, selection, groupBy, having, sortOrder, limit);
    }

    private int getYearFromBudgetName(String budgetName) {
        String yearString = budgetName.substring(0, 4);
        int year = Integer.parseInt(yearString);
        return year;
    }

    private int getMonthFromBudgetName(String budgetName) {
        int result = Constants.NOT_SET;

        if (!budgetName.contains("-")) return result;

        int separatorLocation = budgetName.indexOf("-");
        String monthString = budgetName.substring(separatorLocation + 1, separatorLocation + 3);

        result = Integer.parseInt(monthString);
        return result;
    }
}
