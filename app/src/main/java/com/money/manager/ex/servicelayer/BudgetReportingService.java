package com.money.manager.ex.servicelayer;

import android.content.Context;

import com.money.manager.ex.budget.BudgetPeriodEnum;
import com.money.manager.ex.budget.models.BudgetModel;
import com.money.manager.ex.domainmodel.BudgetEntry;
import com.money.manager.ex.settings.AppSettings;
import com.money.manager.ex.settings.BudgetSettings;
import com.money.manager.ex.utils.MmxDate;

import java.util.Date;

public class BudgetReportingService extends ServiceBase {

    private final BudgetService budgetService;
    private final TransactionService transactionService;
    private final RecurringTransactionService recurringTransactionService;
    private final BudgetSettings budgetSettings;

    public BudgetReportingService(Context context) {
        this(context,
                new BudgetService(context),
                new TransactionService(context),
                new RecurringTransactionService(context),
                (new AppSettings(context).getBudgetSettings())
        );
    }

    public BudgetReportingService(Context context,
                                  BudgetService budgetService,
                                  TransactionService transactionService,
                                  RecurringTransactionService recurringTransactionService,
                                  BudgetSettings budgetSettings) {
        super(context);
        this.budgetService = budgetService;
        this.transactionService = transactionService;
        this.recurringTransactionService = recurringTransactionService;
        this.budgetSettings = budgetSettings;
    }

    public BudgetService getBudgetService() {return budgetService;}
    public TransactionService getTransactionService() {return transactionService;}
    public RecurringTransactionService getRecurringTransactionService() {return recurringTransactionService;}
    public BudgetSettings getBudgetSettings() {return  budgetSettings;}



    public void rebuildBudget(long budgetYearId, boolean useActual, boolean useSchedule) {
        BudgetModel budgetModel = budgetService.loadFullBudget(budgetYearId);
        // now we have budgetModel with all category
        // for each budgetEntry we need to calculate the amount from transactions and recurring transactions
        for (BudgetEntry entry : budgetModel.getItemsAsList()) {
            entry.setAmount(0.0); // clear old value
            // depeding on annual or montly we set frequndy
            if (budgetModel.isMonthlyBudget()) {
                entry.setPeriod(BudgetPeriodEnum.MONTHLY.getDisplayName());
            } else {
                entry.setPeriod(BudgetPeriodEnum.YEARLY.getDisplayName());
            }
            entry.setAmount(
                    (!useActual ? 0 : transactionService.getActualValueForCategoryAndPeriod(
                            entry.getCategoryId(),
                            budgetModel.getDateFrom(budgetSettings),
                            budgetModel.getDateTo(budgetSettings)
                    )) +
                            (!useSchedule ? 0 : recurringTransactionService.getForecastValueForCategoryAndPeriod(
                                    entry.getCategoryId(),
                                    budgetModel.getDateFrom(budgetSettings),
                                    budgetModel.getDateTo(budgetSettings)
                            ))
            );
        }
    }

    public boolean isOverBudget(long categoryId, Date date, Double amount) {
        return isOverBudget(categoryId, new MmxDate(date), amount);
    }

    public boolean isOverBudget(long categoryId, MmxDate date, Double amount) {
        BudgetModel budgetModel = budgetService.loadFromDate(date);
        if (budgetModel == null) return false;
        // get category entry for this budget
        Double BudgetAmount = budgetModel.getBudgetAmountForCategory(categoryId);
        if (BudgetAmount == null) return false; // no budget for this category

        Double actualAmount = transactionService.getActualValueForCategoryAndPeriod(
                categoryId,
                budgetModel.getDateFrom(budgetSettings),
                budgetModel.getDateTo(budgetSettings)
        );
        if (actualAmount == null) actualAmount = 0.0;
        // add also amount from method. this is for not saved transaction
        actualAmount += amount;

        return actualAmount > BudgetAmount;
    }
}
