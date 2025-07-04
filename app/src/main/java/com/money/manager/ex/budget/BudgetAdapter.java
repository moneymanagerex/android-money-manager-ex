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

import androidx.core.content.ContextCompat;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.money.manager.ex.MmexApplication;
import com.money.manager.ex.R;
import com.money.manager.ex.core.UIHelper;
import com.money.manager.ex.currency.CurrencyService;
import com.money.manager.ex.datalayer.BudgetEntryRepository;
import com.money.manager.ex.datalayer.BudgetRepository;
import com.money.manager.ex.domainmodel.Budget;
import com.money.manager.ex.domainmodel.BudgetEntry;
import com.money.manager.ex.nestedcategory.NestedCategoryEntity;
import com.money.manager.ex.nestedcategory.QueryNestedCategory;
import com.money.manager.ex.scheduled.ScheduleTransactionForecastList;
import com.money.manager.ex.scheduled.ScheduledTransactionForecastListServices;
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

    private final Context mContext;

    private final int mLayout;
    private Budget mBudget;
    private HashMap<String, BudgetEntry> mBudgetEntries;

    // budget financial year
    private BudgetSettings budgetSettings;

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

        // get Budget financial
        budgetSettings = (new AppSettings(getContext())).getBudgetSettings();

        // switch to simple layout if the showSimpleView is set
        mLayout = (budgetSettings.getShowSimpleView())
                ? R.layout.item_budget_simple
                : R.layout.item_budget;

        currencyService = new CurrencyService(mContext);

        scheduledTransactionForecastListServices = ScheduledTransactionForecastListServices.getInstance();

        if (mLayout == R.layout.item_budget_simple) {
            addVisibleColumn(R.id.amountAvailableTextView);
        } else {
            // read from setting
            if (budgetSettings.getColumnVisible(R.id.frequencyTextView, true)) addVisibleColumn(R.id.frequencyTextView);
            if (budgetSettings.getColumnVisible(R.id.amountTextView, true)) addVisibleColumn(R.id.amountTextView);
            if (budgetSettings.getColumnVisible(R.id.estimatedForPeriodTextView, false)) addVisibleColumn(R.id.estimatedForPeriodTextView);
            if (budgetSettings.getColumnVisible(R.id.actualTextView, true)) addVisibleColumn(R.id.actualTextView);
            if (budgetSettings.getColumnVisible(R.id.amountAvailableTextView, false)) addVisibleColumn(R.id.amountAvailableTextView);
            if (budgetSettings.getColumnVisible(R.id.forecastRemainTextView, false)) addVisibleColumn(R.id.forecastRemainTextView);
        }
    }

    private void createForecastEntry(){
        if (!mVisibleColumn.contains(R.id.forecastRemainTextView))
            return;
        if (!scheduledTransactionForecastListServices.isReady()) {
            Toast.makeText(mContext, R.string.forecast_calculate, Toast.LENGTH_LONG).show();
            scheduledTransactionForecastListServices
                    .setDateTo(budgetSettings.getBudgetDateToForYear(mBudget.getYear()))
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

    public MmxDate getDateFrom() { return budgetSettings.getBudgetDateFromForYear(mBudget.getYear()) ;}
    public MmxDate getDateTo() { return budgetSettings.getBudgetDateToForYear(mBudget.getYear());}

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

        boolean useSubCategory = budgetSettings.get(R.id.menu_budget_category_with_sub, false);

        setViewElement(view, R.id.categoryTextView, cursor.getString(cursor.getColumnIndexOrThrow(QueryNestedCategory.CATEGNAME)));
        long categoryId = cursor.getLong(cursor.getColumnIndexOrThrow(BudgetNestedQuery.CATEGID));

        // get BudgetEntry
        BudgetEntry budgetEntry = getBudgetEntry(categoryId);
        if (budgetEntry == null) {
            // create fake default entry
            budgetEntry = new BudgetEntry();
            budgetEntry.setActive(false);
        }

        // Frequency frequencyTextView
        setViewElement(view, R.id.frequencyTextView, BudgetPeriods.getPeriodTranslationForEnum(mContext, budgetEntry.getPeriodEnum()));

        // amountTextView
        double amount ;
        amount = budgetEntry.getAmount();
        setViewElement(view, R.id.amountTextView, amount, currencyService);

        // Estimated estimatedForPeriodTextView
        double estimatedForPeriod = mBudget.isMonthlyBudget()
                ? budgetEntry.getMonthlyAmount()
                : budgetEntry.getYearlyAmount();

        if ( useSubCategory) {
            List<NestedCategoryEntity> children = (new QueryNestedCategory(context)).getChildrenNestedCategoryEntities(categoryId);
            for (NestedCategoryEntity child : children) {
                if ( child.getId() != categoryId ) { // already computed
                    BudgetEntry childBudgetEntry = getBudgetEntry(child.getCategoryId());
                    double childEstimatedForPeriod = 0;
                    if ( childBudgetEntry != null ) {
                        childEstimatedForPeriod = mBudget.isMonthlyBudget()
                                ? childBudgetEntry.getMonthlyAmount()
                                : childBudgetEntry.getYearlyAmount();
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
        MmxDate date = new MmxDate(getDateTo().toDate());
        Double total = 0.0;
        ScheduleTransactionForecastList list = scheduledTransactionForecastListServices.getRecurringTransactions();
        while (date.toDate().compareTo(getDateTo().toDate()) < 0) {
            total += list.getForecastAmountFromCache(categoryId, date.getYear(), date.getMonth());
            date.addMonth(1);
        }
        categoryIdForecastAmount.put(categoryId, total);
        return categoryIdForecastAmount.get(categoryId);
    }

    public Context getContext() {
        return mContext;
    }

    /**
     * As a side effect of the setter the budget entry thread cache is populated.
     *
     * @param budgetYearId The budget year id
     */
    public void setBudgetYearId(long budgetYearId) {
        BudgetRepository repo = new BudgetRepository(getContext());
        mBudget = repo.load(budgetYearId);

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
     * @param categoryId Category id
     * @return Budget entry for category
     */
    private BudgetEntry getBudgetEntry(long categoryId) {
        String key = BudgetEntryRepository.getKeyForCategories(categoryId);
        return mBudgetEntries.getOrDefault(key, null);
    }

    /**
     * Builds a thread cache from the database for every category and subcategory present in
     * this budget.
     *
     * @return HashMap of budget entries
     */
    private HashMap<String, BudgetEntry> populateThreadCache() {
        BudgetEntryRepository repo = new BudgetEntryRepository(mContext);
        return repo.loadForYear(mBudget.getId());
    }

    private double getAmountForCategory(Boolean useSubCategory, long categoryId, String categoryName) {
        BudgetService service = new BudgetService(getContext());
        if (useSubCategory) {
            return service.getActualValueForCategoryAndChildrenAndPeriod(categoryId, categoryName, mBudget.getYear(), mBudget.getMonth());
        }
        return service.getActualValueForCategoryAndChildrenAndPeriod(categoryId, null, mBudget.getYear(), mBudget.getMonth());
    }

}
