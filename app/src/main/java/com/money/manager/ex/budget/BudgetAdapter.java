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

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.money.manager.ex.Constants;
import com.money.manager.ex.MmexApplication;
import com.money.manager.ex.R;
import com.money.manager.ex.core.InfoKeys;
import com.money.manager.ex.core.UIHelper;
import com.money.manager.ex.currency.CurrencyService;
import com.money.manager.ex.database.QueryMobileData;
import com.money.manager.ex.datalayer.BudgetEntryRepository;
import com.money.manager.ex.domainmodel.BudgetEntry;
import com.money.manager.ex.nestedcategory.NestedCategoryEntity;
import com.money.manager.ex.nestedcategory.QueryNestedCategory;
import com.money.manager.ex.scheduled.ScheduleTransactionForecastList;
import com.money.manager.ex.scheduled.ScheduledTransactionForecastListServices;
import com.money.manager.ex.servicelayer.InfoService;
import com.money.manager.ex.settings.AppSettings;
import com.money.manager.ex.settings.BudgetSettings;
import com.money.manager.ex.utils.MmxDate;
import com.squareup.sqlbrite3.BriteDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

    private Context mContext;

    private final int mLayout;
    private String mBudgetName;
    private long mBudgetYearId;
    private HashMap<String, BudgetEntry> mBudgetEntries;

    // budget financial year
    private boolean useBudgetFinancialYear = false;
    private MmxDate dateFrom;
    private MmxDate dateTo;

    //private ScheduleTransactionForecastList mScheduleTransactionForecastList;
    private final ScheduledTransactionForecastListServices scheduledTransactionForecastListServices;
    //private CompletableFuture<ScheduleTransactionForecastList> mScheduleTransactionForecastListFuture;
    private final List<View> fieldRequestUpdate = new ArrayList<>();
    private final HashMap<Long, Double> categoryIdAmountAvailable = new HashMap<>();
    private final HashMap<Long, Double> categoryIdForecastAmount = new HashMap<>();
    private final CurrencyService currencyService;

    private final ArrayList<Integer> mVisibleColumn = new ArrayList<>();

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

        // switch to simple layout if the showSimpleView is set
        AppSettings settings = new AppSettings(getContext());
        mLayout = (settings.getBudgetSettings().getShowSimpleView())
                ? R.layout.item_budget_simple
                : R.layout.item_budget;

        currencyService = new CurrencyService(mContext);

        scheduledTransactionForecastListServices = ScheduledTransactionForecastListServices.getInstance();

        MmexApplication.getApp().iocComponent.inject(this);

        // get Budget financial
        try {
            useBudgetFinancialYear = (new AppSettings(getContext()).getBudgetSettings().getBudgetFinancialYear());
        } catch (Exception e) {
            useBudgetFinancialYear = false;
        }

        if (mLayout == R.layout.item_budget_simple) {
            addVisibleColumn(R.id.amountAvailableTextView);
        } else {
            // read from setting
            BudgetSettings setting = (new AppSettings(getContext())).getBudgetSettings();
            if (setting.getColumnVisible(R.id.frequencyTextView, true)) addVisibleColumn(R.id.frequencyTextView);
            if (setting.getColumnVisible(R.id.amountTextView, true)) addVisibleColumn(R.id.amountTextView);
            if (setting.getColumnVisible(R.id.estimatedForPeriodTextView, false)) addVisibleColumn(R.id.estimatedForPeriodTextView);
            if (setting.getColumnVisible(R.id.actualTextView, true)) addVisibleColumn(R.id.actualTextView);
            if (setting.getColumnVisible(R.id.amountAvailableTextView, false)) addVisibleColumn(R.id.amountAvailableTextView);
            if (setting.getColumnVisible(R.id.forecastRemainTextView, false)) addVisibleColumn(R.id.forecastRemainTextView);
        }
    }

    private void createForecastEntry(){
        if (!mVisibleColumn.contains(R.id.forecastRemainTextView))
            return;
        if (!scheduledTransactionForecastListServices.isReady()) {
            Toast.makeText(mContext, R.string.forecast_calculate, Toast.LENGTH_LONG).show();
            scheduledTransactionForecastListServices
                    .setDateTo(dateTo)
                    .createScheduledTransactionForecastAsync(mContext, result -> {
                        Handler mHandler = new Handler(Looper.getMainLooper());
                        mHandler.post(() -> processForecast((ScheduleTransactionForecastList) result));
                        return result;
                    });
        }

    }

    public void processForecast(ScheduleTransactionForecastList result){
        for (int i = 0; i < fieldRequestUpdate.size(); i++) {
            View view = fieldRequestUpdate.get(i);
            long categoryId = (long) view.getTag();
            double forecastRemain;
            if (categoryIdForecastAmount.containsKey( categoryId ) ) {
                // reuse from cache
                forecastRemain = categoryIdAmountAvailable.get(categoryId) - categoryIdForecastAmount.get(categoryId);
            } else {
                double totalFromSchedule = getEstimateFromRecurringTransaction(categoryId);
                double amountAvailable = categoryIdAmountAvailable.get(categoryId);
                forecastRemain = amountAvailable - totalFromSchedule;
            }
            setViewElement(view, R.id.forecastRemainTextView, forecastRemain, currencyService, forecastRemain > 0);
            view.postInvalidate();
        }
        fieldRequestUpdate.clear();
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
        setVisibleTextFieldForView(view, R.id.estimatedForPeriodTextView);
        setVisibleTextFieldForView(view, R.id.actualTextView);
        setVisibleTextFieldForView(view, R.id.amountAvailableTextView);
        setVisibleTextFieldForView(view, R.id.forecastRemainTextView);
    }

    private void setVisibleTextFieldForView(View view, int resId) {
        try {
            view.findViewById(resId).setVisibility(getVisibleColumn().contains(resId) ? View.VISIBLE : View.GONE);
        } catch (Exception e) {
            // column not visible
        }

    }

    private void setViewElement(View view, int resId, String text) {
        setViewElement(view, resId, text, false, false);
    }

    private void setViewElement(View view, int resId, String text, Boolean withColor, boolean witchColor) {
        TextView textView = view.findViewById(resId);
        if (textView != null) {
            try {
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
            } catch ( Exception e ) {
                Timber.e(e, "setViewElement");
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

        boolean useSubCategory = (new AppSettings(getContext())).getBudgetSettings().get(R.id.menu_budget_category_with_sub, false);

        setViewElement(view, R.id.categoryTextView, cursor.getString(cursor.getColumnIndex(QueryNestedCategory.CATEGNAME)));
        long categoryId = cursor.getLong(cursor.getColumnIndex(BudgetNestedQuery.CATEGID));

        // Frequency frequencyTextView
        BudgetPeriodEnum periodEnum = getBudgetPeriodFor(categoryId);
        setViewElement(view, R.id.frequencyTextView, BudgetPeriods.getPeriodTranslationForEnum(mContext, periodEnum));

        // amountTextView
        double amount = 0;
        amount = getBudgetAmountFor(categoryId);
        setViewElement(view, R.id.amountTextView, amount, currencyService);

        // Estimated estimatedForPeriodTextView
        double estimatedForPeriod = isMonthlyBudget(mBudgetName)
                ? BudgetPeriods.getMonthlyEstimate(periodEnum, amount)
                : BudgetPeriods.getYearlyEstimate(periodEnum, amount);
        if (Double.isNaN(estimatedForPeriod)) {
            // this means that we don't have estimate for this category
            // so estimate is 0
            estimatedForPeriod = 0;
        }
        if ( useSubCategory) {
            List<NestedCategoryEntity> children = (new QueryNestedCategory(context)).getChildrenNestedCategoryEntities(categoryId);
            for (NestedCategoryEntity child : children) {
                if ( child.getId() != categoryId ) { // already computed
                    BudgetPeriodEnum childPeriodEnum = getBudgetPeriodFor(child.getCategoryId());
                    double childAmount = getBudgetAmountFor(child.getCategoryId());
                    double childEstimatedForPeriod = isMonthlyBudget(mBudgetName)
                            ? BudgetPeriods.getMonthlyEstimate(childPeriodEnum, childAmount)
                            : BudgetPeriods.getYearlyEstimate(childPeriodEnum, childAmount);
                    if (Double.isNaN(childEstimatedForPeriod)) {
                        // this means that we don't have estimate for this category
                        // so estimate is 0
                        childEstimatedForPeriod = 0;
                    }
                    estimatedForPeriod += childEstimatedForPeriod;
                }
            }
        }
        setViewElement(view, R.id.estimatedForPeriodTextView, estimatedForPeriod, currencyService);

        // Actual actualTextView
        double actual = getActualAmount(useSubCategory, cursor);
        setViewElement(view, R.id.actualTextView, actual, currencyService, (int) (actual * 100) < (int) (estimatedForPeriod * 100));

        // Amount Available amountAvailableTextView
        double amountAvailable = (estimatedForPeriod - actual);
        if (Double.isInfinite(amountAvailable)) {
            setViewElement(view, R.id.amountAvailableTextView, "<setup a period>");
            amountAvailable = 0.0;
        } else {
            setViewElement(view, R.id.amountAvailableTextView, amountAvailable, currencyService);
        }
        categoryIdAmountAvailable.put(categoryId, amountAvailable);

        // forecastRemainTextView
        if (!ScheduledTransactionForecastListServices.getInstance().isReady()){
            setViewElement(view, R.id.forecastRemainTextView, "<...>");
            view.setTag(categoryId);
            fieldRequestUpdate.add(view);
        } else {
            double totalFromSchedule = getEstimateFromRecurringTransaction(categoryId);
            double forecastRemain = amountAvailable - totalFromSchedule;
            setViewElement(view, R.id.forecastRemainTextView, forecastRemain, currencyService, forecastRemain > 0);
        }
    }

    private double getEstimateFromRecurringTransaction(long categoryId) {
        if (categoryIdForecastAmount.isEmpty()) {
            Timber.i("Calculate local cache");
        }
        // Get Value for this category from recurring transaction
        if ( categoryIdForecastAmount.containsKey(categoryId) ) {
            return categoryIdForecastAmount.get(categoryId);
        }

        if ( ! scheduledTransactionForecastListServices.isReady() ) return 0;

        // budget is based on year, Year financial, or monthly
        MmxDate date = new MmxDate(dateFrom.toDate());
        Double total = 0.0;
        ScheduleTransactionForecastList list = scheduledTransactionForecastListServices.getRecurringTransactions();
        while (date.toDate().compareTo(dateTo.toDate()) < 0) {
            total += list.getForecastAmountFromCache(categoryId, date.getYear(), date.getMonth());
            date.addMonth(1);
        }
        categoryIdForecastAmount.put(categoryId, total);
        return categoryIdForecastAmount.get(categoryId);
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
        // populate forecast entry
        createForecastEntry();
    }

    private double getActualAmount(Boolean useSubCategory, Cursor cursor) {
        double actual;
        // WolfSolver since category can be nested we need to consider always category as master
        long categoryId = cursor.getLong(cursor.getColumnIndexOrThrow(BudgetNestedQuery.CATEGID));
        String categoryName = cursor.getString(cursor.getColumnIndexOrThrow(BudgetNestedQuery.CATEGNAME));
        actual = getAmountForCategory(useSubCategory, categoryId, categoryName);

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

    private double getAmountForCategory(Boolean useSubCategory, long categoryId, String categoryName) {
        String where;
        where = QueryNestedCategory.CATEGID + "=" + categoryId;
        if (useSubCategory) {
            if ( categoryName.contains("'")) {
                categoryName = categoryName.replace("\"", "\"\"");
            }
            where = "( " + where + " OR " + QueryMobileData.Category + " LIKE \"" + categoryName +":%\" )";
        }
        double total = loadTotalFor(where);
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
            // WolfSolver adapt query for nested category
            String query = prepareQuery(where);
            Cursor cursor = databaseLazy.get().query(query);
            if (cursor == null) return 0;
            // add all the categories and subcategories together.
            while (cursor.moveToNext()) {
                total += cursor.getDouble(cursor.getColumnIndexOrThrow("TOTAL"));
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
