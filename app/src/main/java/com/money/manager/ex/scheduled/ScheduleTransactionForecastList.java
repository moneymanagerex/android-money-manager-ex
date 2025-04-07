package com.money.manager.ex.scheduled;

import com.money.manager.ex.core.TransactionTypes;
import com.money.manager.ex.domainmodel.RecurringTransaction;
import com.money.manager.ex.utils.MmxDate;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ScheduleTransactionForecastList
    extends ArrayList<RecurringTransaction> {
//    private ArrayList<RecurringTransaction> mRecurringTransactions;

    ScheduleTransactionForecastList() {
//        mRecurringTransactions = new ArrayList<>();
    }

    ScheduleTransactionForecastList(ArrayList<RecurringTransaction> recurringTransactionsList) {
//        mRecurringTransactions = recurringTransactionsList;
        this.addAll(recurringTransactionsList);
    }

    public ArrayList<RecurringTransaction> getRecurringTransactionsList() {
        return this;
    }

    public ScheduleTransactionForecastList getRecurringTransactions(MmxDate start, MmxDate end) {
        return getRecurringTransactions(recurringTransaction -> recurringTransaction.getPaymentDate().compareTo(start.toDate()) >= 0 &&
                recurringTransaction.getPaymentDate().compareTo(end.toDate()) <= 0 );
    }

    public ScheduleTransactionForecastList getRecurringTransactions(long categoryId) {
        return getRecurringTransactions(recurringTransaction -> recurringTransaction.getCategoryId() == categoryId );
    }

    public ScheduleTransactionForecastList getRecurringTransactions(long categoryId, MmxDate start, MmxDate end) {
        return getRecurringTransactions(recurringTransaction -> recurringTransaction.getPaymentDate().compareTo(start.toDate()) >= 0 &&
                recurringTransaction.getPaymentDate().compareTo(end.toDate()) <= 0 &&
                recurringTransaction.getCategoryId() == categoryId);
    }

    public ScheduleTransactionForecastList getRecurringTransactions(Predicate<RecurringTransaction> predicate) {
        List list = this.stream().filter(predicate).collect(Collectors.toList());
        return new ScheduleTransactionForecastList((ArrayList<RecurringTransaction>) list);
    }

    public void orderByDateAscending() {
        this.sort((RecurringTransaction uno, RecurringTransaction due) -> uno.getPaymentDate().compareTo(due.getPaymentDate()));
    }

    public Double getTotalAmount() {
        if (this.size() == 0) return 0.0;

        // todo handle currency
        Double total = 0.0;
        for (RecurringTransaction recurringTransaction : this) {
            // ignore transfert
            if (recurringTransaction.getTransactionType().equals(TransactionTypes.Transfer)) continue;

            // todo check split

            // if deposit add to total
            if (recurringTransaction.getTransactionType().equals(TransactionTypes.Deposit)) {
                total += recurringTransaction.getAmount().toDouble();
            }
            // if withdrawal subtract from total
            if (recurringTransaction.getTransactionType().equals(TransactionTypes.Withdrawal)) {
                total -= recurringTransaction.getAmount().toDouble();
            }
        }
        return total;
    }


}
