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
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import androidx.core.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.money.manager.ex.Constants;
import com.money.manager.ex.MmexApplication;
import com.money.manager.ex.R;
import com.money.manager.ex.core.UIHelper;
import com.money.manager.ex.currency.CurrencyService;
import com.money.manager.ex.database.QueryCategorySubCategory;
import com.money.manager.ex.database.ViewMobileData;
import com.money.manager.ex.datalayer.BudgetEntryRepository;
import com.money.manager.ex.domainmodel.BudgetEntry;
import com.money.manager.ex.settings.AppSettings;
import com.squareup.sqlbrite.BriteDatabase;

import java.util.HashMap;

import javax.inject.Inject;

import androidx.cursoradapter.widget.SimpleCursorAdapter;
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

        // switch to simple layout if the showSimpleView is set
        AppSettings settings = new AppSettings(getContext());
        mLayout = (settings.getBudgetSettings().getShowSimpleView())
                ? R.layout.item_budget_simple
                : R.layout.item_budget;

        mContext = context;

        MmexApplication.getApp().iocComponent.inject(this);
    }

    @Inject Lazy<BriteDatabase> databaseLazy;

    private int mLayout;
    private String mBudgetName;
    private long mBudgetYearId;
    private HashMap<String, BudgetEntry> mBudgetEntries;

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        final LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(mLayout, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Category

        boolean hasSubcategory = cursor.getInt(cursor.getColumnIndex(QueryCategorySubCategory.SUBCATEGID)) != Constants.NOT_SET;

        TextView categoryTextView = (TextView) view.findViewById(R.id.categoryTextView);
        if (categoryTextView != null) {
            int categoryColumnIndex = cursor.getColumnIndex(QueryCategorySubCategory.CATEGSUBNAME);
            categoryTextView.setText(cursor.getString(categoryColumnIndex));
        }

        int categoryId    = cursor.getInt(cursor.getColumnIndex(BudgetQuery.CATEGID));
        int subCategoryId = cursor.getInt(cursor.getColumnIndex(BudgetQuery.SUBCATEGID));

        // Frequency

        BudgetPeriodEnum periodEnum = getBudgetPeriodFor(categoryId, subCategoryId);

        TextView frequencyTextView = (TextView) view.findViewById(R.id.frequencyTextView);
        if (frequencyTextView != null) {
            frequencyTextView.setText(BudgetPeriods.getPeriodTranslationForEnum(mContext, periodEnum));
        }

        CurrencyService currencyService = new CurrencyService(mContext);

        // Amount

        TextView amountTextView = (TextView) view.findViewById(R.id.amountTextView);
        double amount = getBudgetAmountFor(categoryId, subCategoryId);
        if (amountTextView != null) {
            String text = currencyService.getBaseCurrencyFormatted(MoneyFactory.fromDouble(amount));
            amountTextView.setText(text);
        }

        // Estimated
        double estimated = isMonthlyBudget(mBudgetName)
                ? BudgetPeriods.getMonthlyEstimate(periodEnum, amount)
                : BudgetPeriods.getYearlyEstimate(periodEnum, amount)
        ;

        // Actual
        TextView actualTextView = (TextView) view.findViewById(R.id.actualTextView);
        double actual = getActualAmount(hasSubcategory, cursor);
        if (actualTextView != null) {
            String actualString = currencyService.getBaseCurrencyFormatted(MoneyFactory.fromDouble(actual));
            actualTextView.setText(actualString);

            // colour the amount depending on whether it is above/below the budgeted amount to 2 decimal places
            UIHelper uiHelper = new UIHelper(context);
            if ((int) (actual * 100) < (int) (estimated * 100)) {
                actualTextView.setTextColor(
                    ContextCompat.getColor(context, uiHelper.resolveAttribute(R.attr.holo_red_color_theme))
                );
            } else {
                actualTextView.setTextColor(
                    ContextCompat.getColor(context, uiHelper.resolveAttribute(R.attr.holo_green_color_theme))
                );
            }
        }

        // Amount Available

        TextView amountAvailableTextView = (TextView) view.findViewById(R.id.amountAvailableTextView);
        if (amountAvailableTextView != null) {
            double amountAvailable = -(estimated - actual);
            String amountAvailableString = currencyService.getBaseCurrencyFormatted(MoneyFactory.fromDouble(amountAvailable));
            amountAvailableTextView.setText(amountAvailableString);

            // colour the amount depending on whether it is above/below the budgeted amount to 2 decimal places
            UIHelper uiHelper = new UIHelper(context);
            int amountAvailableInt = (int) (amountAvailable * 100);
            if (amountAvailableInt < 0) {
                amountAvailableTextView.setTextColor(
                    ContextCompat.getColor(context, uiHelper.resolveAttribute(R.attr.holo_red_color_theme))
                );
            } else if (amountAvailableInt > 0) {
                amountAvailableTextView.setTextColor(
                    ContextCompat.getColor(context, uiHelper.resolveAttribute(R.attr.holo_green_color_theme))
                );
            }
        }
    }

    public Context getContext() {
        return mContext;
    }

    public void setBudgetName(String budgetName) {
        mBudgetName = budgetName;
    }

    /**
     * As a side effect of the setter the budget entry thread cache is populated.
     * @param budgetYearId
     */
    public void setBudgetYearId(long budgetYearId) {
        this.mBudgetYearId = budgetYearId;

        if (mBudgetEntries != null) {
            mBudgetEntries.clear();
        }
        // populate thread cache HashMap
        mBudgetEntries = populateThreadCache();
    }

    private double getActualAmount(boolean hasSubcategory, Cursor cursor) {
        double actual;
        if (!hasSubcategory) {
            int categoryId = cursor.getInt(cursor.getColumnIndex(BudgetQuery.CATEGID));
            actual = getAmountForCategory(categoryId);
        } else {
            int subCategoryId = cursor.getInt(cursor.getColumnIndex(BudgetQuery.SUBCATEGID));
            actual = getAmountForSubCategory(subCategoryId);
        }
        return actual;
    }

    /**
     * Returns the budgeted amount for the category and subcategory, or zero, if there is none.
     * @param categoryId
     * @param subCategoryId
     * @return
     */
    private double getBudgetAmountFor(int categoryId, int subCategoryId) {
        String key = BudgetEntryRepository.getKeyForCategories(categoryId, subCategoryId);
        return mBudgetEntries.containsKey(key)
                ? mBudgetEntries.get(key).getContentValues().getAsDouble(BudgetQuery.AMOUNT)
                : 0;
    }

    /**
     * Returns the period of the budgeted amount or NONE if there isn't any.
     * @param categoryId
     * @param subCategoryId
     * @return
     */
    private BudgetPeriodEnum getBudgetPeriodFor(int categoryId, int subCategoryId) {
        String key = BudgetEntryRepository.getKeyForCategories(categoryId, subCategoryId);
        return mBudgetEntries.containsKey(key)
                ? BudgetPeriods.getEnum(mBudgetEntries.get(key).getContentValues().getAsString(BudgetQuery.PERIOD))
                : BudgetPeriodEnum.NONE;
    }

    /**
     * Builds a thread cache from the database for every category and subcategory present in
     * this budget.
     * @return
     */
    private HashMap<String, BudgetEntry> populateThreadCache() {
        BudgetEntryRepository repo = new BudgetEntryRepository(mContext);
        return repo.loadForYear(mBudgetYearId);
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

    private boolean isMonthlyBudget(String budgetName) {
        return budgetName.contains("-");
    }

    private int getMonthFromBudgetName(String budgetName) {
        int result = Constants.NOT_SET;

        if (!isMonthlyBudget(budgetName)) return result;

        int separatorLocation = budgetName.indexOf("-");
        String monthString = budgetName.substring(separatorLocation + 1, separatorLocation + 3);

        result = Integer.parseInt(monthString);
        return result;
    }

}
