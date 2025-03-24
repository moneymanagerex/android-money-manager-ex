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

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;

import androidx.core.content.ContextCompat;

import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.money.manager.ex.Constants;
import com.money.manager.ex.MmexApplication;
import com.money.manager.ex.R;
import com.money.manager.ex.core.InfoKeys;
import com.money.manager.ex.core.UIHelper;
import com.money.manager.ex.currency.CurrencyService;
import com.money.manager.ex.database.QueryMobileData;
import com.money.manager.ex.datalayer.BudgetEntryRepository;
import com.money.manager.ex.domainmodel.Budget;
import com.money.manager.ex.domainmodel.BudgetEntry;
import com.money.manager.ex.nestedcategory.QueryNestedCategory;
import com.money.manager.ex.servicelayer.InfoService;
import com.money.manager.ex.settings.AppSettings;
import com.money.manager.ex.settings.BudgetSettings;
import com.money.manager.ex.utils.MmxDate;
import com.squareup.sqlbrite3.BriteDatabase;

import java.util.ArrayList;
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

    @Inject
    Lazy<BriteDatabase> databaseLazy;
    private final int mLayout;
    private String mBudgetName;
    private long mBudgetYearId;
    private HashMap<String, BudgetEntry> mBudgetEntries;

    // budget financial year
    private boolean useBudgetFinancialYear = false;
    private MmxDate dateFrom;
    private MmxDate dateTo;

    private ArrayList<Integer> mVisibleColumn = new ArrayList<>();

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

        // get Budget financial
        try {
            useBudgetFinancialYear = (new AppSettings(getContext()).getBudgetSettings().getBudgetFinancialYear());
        } catch (Exception e) {
        }

        if (mLayout == R.layout.item_budget_simple) {
            addVisibleColumn(R.id.amountAvailableTextView);
        } else {
            // read from setting
            BudgetSettings setting = (new AppSettings(getContext())).getBudgetSettings();
            if (setting.getColumnVisible(R.id.frequencyTextView, true)) addVisibleColumn(R.id.frequencyTextView);
            if (setting.getColumnVisible(R.id.amountTextView, true)) addVisibleColumn(R.id.amountTextView);
            if (setting.getColumnVisible(R.id.estimatedAnnualTextView, false)) addVisibleColumn(R.id.estimatedAnnualTextView);
            if (setting.getColumnVisible(R.id.actualTextView, true)) addVisibleColumn(R.id.actualTextView);
            if (setting.getColumnVisible(R.id.amountAvailableTextView, false)) addVisibleColumn(R.id.amountAvailableTextView);
            if (setting.getColumnVisible(R.id.forecastRemainTextView, false)) addVisibleColumn(R.id.forecastRemainTextView);
        }
    }

    public void addVisibleColumn(int column) {
        if (!mVisibleColumn.contains(column))
            mVisibleColumn.add(column);
    }

    public void removeVisibleColumn(int column) {
        if (mVisibleColumn.contains(column))
            mVisibleColumn.remove(column);
    }

    public MmxDate getDateFrom() { return dateFrom ;}
    public MmxDate getDateTo() { return dateTo ;}

    ArrayList<Integer> getVisibleColumn() {
        return mVisibleColumn;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        final LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(mLayout, parent, false);
    }

    public void setVisibleTextFieldsForView(View view) {
        setVisibleTextFieldForView(view, R.id.frequencyTextView);
        setVisibleTextFieldForView(view, R.id.amountTextView);
        setVisibleTextFieldForView(view, R.id.estimatedAnnualTextView);
        setVisibleTextFieldForView(view, R.id.actualTextView);
        setVisibleTextFieldForView(view, R.id.amountAvailableTextView);
        setVisibleTextFieldForView(view, R.id.forecastRemainTextView);
    }

    private void setVisibleTextFieldForView(View view, int resid) {
        try {
            view.findViewById(resid).setVisibility(getVisibleColumn().contains(resid) ? View.VISIBLE : View.GONE);
        } catch (Exception e) {
            // colummn not visible
        }

    }

    private void setViewElement(View view, int resId, String text) {
        setViewElement(view, resId, text, false, false);
    }

    private void setViewElement(View view, int resId, String text, Boolean withColor, boolean witchColor) {
        TextView textView = view.findViewById(resId);
        if (textView != null) {
            textView.setText(text);
            if (withColor) {
                UIHelper uiHelper = new UIHelper(mContext);
                if (witchColor) {
                    textView.setTextColor(
                            ContextCompat.getColor(mContext, uiHelper.resolveAttribute(R.attr.holo_red_color_theme))
                    );
                } else {
                    textView.setTextColor(
                            ContextCompat.getColor(mContext, uiHelper.resolveAttribute(R.attr.holo_green_color_theme))
                    );
                }
            }
        }
    }

    private void setViewElement(View view, int resId, double amount, CurrencyService currencyService, Boolean withColor) {
        setViewElement(view, resId, currencyService.getBaseCurrencyFormatted(MoneyFactory.fromDouble(amount)), true, withColor);
    }

    private void setViewElement(View view, int resId, double amount, CurrencyService currencyService) {
        setViewElement(view, resId, currencyService.getBaseCurrencyFormatted(MoneyFactory.fromDouble(amount)), false, false);
    }


    @SuppressLint("Range")
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Category categoryTextView

        setVisibleTextFieldsForView(view);

        boolean hasSubcategory = false;
        setViewElement(view, R.id.categoryTextView, cursor.getString(cursor.getColumnIndex(QueryNestedCategory.CATEGNAME)));
        long categoryId = cursor.getLong(cursor.getColumnIndex(BudgetNestedQuery.CATEGID));

        // Frequency frequencyTextView
        BudgetPeriodEnum periodEnum = getBudgetPeriodFor(categoryId);
        setViewElement(view, R.id.frequencyTextView, BudgetPeriods.getPeriodTranslationForEnum(mContext, periodEnum));

        CurrencyService currencyService = new CurrencyService(mContext);

        // amountTextView
        double amount = getBudgetAmountFor(categoryId);
        setViewElement(view, R.id.amountTextView, amount, currencyService);

        // Estimated estimatedAnnualTextView
        double estimatedAnnual = isMonthlyBudget(mBudgetName)
                ? BudgetPeriods.getMonthlyEstimate(periodEnum, amount)
                : BudgetPeriods.getYearlyEstimate(periodEnum, amount);
        if (Double.isNaN(estimatedAnnual)) {
            // this means that we don't have estimate for this category
            // so estimate is 0
            estimatedAnnual = 0;
        }

        setViewElement(view, R.id.estimatedAnnualTextView, estimatedAnnual, currencyService);

        // Actual actualTextView
        double actual = getActualAmount(hasSubcategory, cursor);
        setViewElement(view, R.id.actualTextView, actual, currencyService, (int) (actual * 100) < (int) (estimatedAnnual * 100));

        // Amount Available amountAvailableTextView
        double amountAvailable = -(estimatedAnnual - actual);
        if (Double.isInfinite(amountAvailable)) {
            setViewElement(view, R.id.amountAvailableTextView, "<setup a period>");
        } else {
            setViewElement(view, R.id.amountAvailableTextView, amountAvailable, currencyService, amountAvailable < 0);
        }


        // forecastRemainTextView
        double forecastRemain = getEstimateFromRecurringTransaction(cursor);
        setViewElement(view, R.id.forecastRemainTextView, forecastRemain, currencyService, forecastRemain < 0);

    }

    private double getEstimateFromRecurringTransaction(Cursor cursor) {
        // TODO Get Value for this category from recurring transaction
        return 0;
    }

    public Context getContext() {
        return mContext;
    }

    public void setBudgetName(String budgetName) {
        mBudgetName = budgetName;
        long year = getYearFromBudgetName(mBudgetName);
        long month = getMonthFromBudgetName(mBudgetName);
        if ( month != Constants.NOT_SET ) {
            month--;
            // monthly budget
            dateFrom = new MmxDate((int) year, (int) month, 1);
            dateTo = new MmxDate((int) year, (int) month + 1, 1).minusDays(1);
        } else if ( useBudgetFinancialYear ){
            // year financial budget
            dateFrom = getStartDateForFinancialYear(mBudgetName);
            dateTo = new MmxDate(dateFrom.toDate());
            dateTo.addYear(1).minusDays(1);
        } else {
            // year budget
            dateFrom = new MmxDate((int) year, 0, 1);
            dateTo = new MmxDate((int) year, 11, 31);
        }
    }

    /**
     * As a side effect of the setter the budget entry thread cache is populated.
     *
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
        // wolfsolver since category can be neested we need to consider always category as master
        long categoryId = cursor.getLong(cursor.getColumnIndex(BudgetNestedQuery.CATEGID));
        actual = getAmountForCategory(categoryId);

        return actual;
    }

    /**
     * Returns the budgeted amount for the category and subcategory, or zero, if there is none.
     *
     * @param categoryId
     * @return
     */
    private double getBudgetAmountFor(long categoryId) {
        String key = BudgetEntryRepository.getKeyForCategories(categoryId);
        return mBudgetEntries.containsKey(key)
                ? mBudgetEntries.get(key).getDouble(BudgetQuery.AMOUNT)
                : 0;
    }

    /**
     * Returns the period of the budgeted amount or NONE if there isn't any.
     *
     * @param categoryId
     * @return
     */
    private BudgetPeriodEnum getBudgetPeriodFor(long categoryId) {
        String key = BudgetEntryRepository.getKeyForCategories(categoryId);
        return mBudgetEntries.containsKey(key)
                ? BudgetPeriods.getEnum(mBudgetEntries.get(key).getString(BudgetQuery.PERIOD))
                : BudgetPeriodEnum.NONE;
    }

    /**
     * Builds a thread cache from the database for every category and subcategory present in
     * this budget.
     *
     * @return
     */
    private HashMap<String, BudgetEntry> populateThreadCache() {
        BudgetEntryRepository repo = new BudgetEntryRepository(mContext);
        return repo.loadForYear(mBudgetYearId);
    }

    private double getAmountForCategory(long categoryId) {
        double total = loadTotalFor(QueryMobileData.CATEGID + "=" + categoryId);
        return total;
    }

    private double loadTotalFor(String where) {
        double total = 0;

        // if month use month budget
        // if year check if financial or calendar
        long year = getYearFromBudgetName(mBudgetName);
        long month = getMonthFromBudgetName(mBudgetName);
        if (month != Constants.NOT_SET) {
            // month
            where += " AND " + QueryMobileData.Month + "=" + month;
            where += " AND " + QueryMobileData.Year + "=" + year;
        } else if (!useBudgetFinancialYear || dateFrom == null || dateTo == null) {
            // annual
            where += " AND " + QueryMobileData.Year + "=" + year;
        } else {
            // financial
            where += " AND " + QueryMobileData.Date + " BETWEEN '" + dateFrom.toIsoDateString() + "' AND '" + dateTo.toIsoDateString() + "'";
        }

        try {
            // wolfsolver adapt query for nested category
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
        QueryMobileData mobileData = new QueryMobileData(getContext());

        //data to compose builder
        String[] projectionIn = new String[]{
                "ID AS _id", QueryMobileData.CATEGID, QueryMobileData.Category,
                "SUM(" + QueryMobileData.AmountBaseConvRate + ") AS TOTAL"
        };

        String selection = QueryMobileData.Status + "<>'V' AND " +
                QueryMobileData.TransactionType + " IN ('Withdrawal', 'Deposit')";
        if (!TextUtils.isEmpty(whereClause)) {
            selection += " AND " + whereClause;
        }

        String groupBy = QueryMobileData.CATEGID + ", " + QueryMobileData.Category;

        String having = null;
//        if (!TextUtils.isEmpty(((CategoriesReportActivity) context).mFilter)) {
//            String filter = ((CategoriesReportActivity) context).mFilter;
//            if (TransactionTypes.valueOf(filter).equals(TransactionTypes.Withdrawal)) {
//                having = "SUM(" + QueryMobileData.AmountBaseConvRate + ") < 0";
//            } else {
//                having = "SUM(" + QueryMobileData.AmountBaseConvRate + ") > 0";
//            }
//        }

        String sortOrder = QueryMobileData.Category;
        String limit = null;

        builder.setTables(mobileData.getSource());

        return builder.buildQuery(projectionIn, selection, groupBy, having, sortOrder, limit);
    }

    private long getYearFromBudgetName(String budgetName) {
        String yearString = budgetName.substring(0, 4);
        long year = Integer.parseInt(yearString);
        return year;
    }

    private MmxDate getStartDateForFinancialYear(String budgetName) {
        MmxDate newDate = MmxDate.newDate();
        try {
            InfoService infoService = new InfoService(getContext());
            int financialYearStartDay = Integer.valueOf(infoService.getInfoValue(InfoKeys.FINANCIAL_YEAR_START_DAY, "1"));
            int financialYearStartMonth = Integer.valueOf(infoService.getInfoValue(InfoKeys.FINANCIAL_YEAR_START_MONTH, "0")) - 1;
            newDate.setYear((int) getYearFromBudgetName(budgetName));
            newDate.setDate(financialYearStartDay);
            newDate.setMonth(financialYearStartMonth);
        } catch (Exception e) {
        }
        return newDate;
    }

    private boolean isMonthlyBudget(String budgetName) {
        return budgetName.contains("-");
    }

    private long getMonthFromBudgetName(String budgetName) {
        long result = Constants.NOT_SET;

        if (!isMonthlyBudget(budgetName)) return result;

        int separatorLocation = budgetName.indexOf("-");
        String monthString = budgetName.substring(separatorLocation + 1, separatorLocation + 3);

        result = Integer.parseInt(monthString);
        return result;
    }

}
