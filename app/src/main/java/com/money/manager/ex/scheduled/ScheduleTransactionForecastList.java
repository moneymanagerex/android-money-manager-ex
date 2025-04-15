package com.money.manager.ex.scheduled;

import androidx.annotation.NonNull;

import com.money.manager.ex.domainmodel.RecurringTransaction;
import com.money.manager.ex.utils.MmxDate;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ScheduleTransactionForecastList
        extends ArrayList<RecurringTransaction> {

    public static class ScheduleTransactionCacheKey  {
        private final long categoryId;
        private final int year;
        private final int month;
        public ScheduleTransactionCacheKey(long categoryId, int year, int month) {
            this.categoryId = categoryId;
            this.year = year;
            this.month = month;
        }

        public ScheduleTransactionCacheKey(RecurringTransaction recurringTransaction) {
            this.categoryId = recurringTransaction.getCategoryId();
            this.year = recurringTransaction.getPaymentDateAsMmxDate().getYear();
            this.month = recurringTransaction.getPaymentDateAsMmxDate().getMonth();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ScheduleTransactionCacheKey tripleKey = (ScheduleTransactionCacheKey) o;
            return Objects.equals(categoryId, tripleKey.categoryId) && Objects.equals(year, tripleKey.year) && Objects.equals(month, tripleKey.month);
        }

        @Override
        public int hashCode() {
            return Objects.hash(categoryId, year, month);
        }

        @NonNull
        @Override
        public String toString() {
            return "{ category:" + categoryId + ", year:" + year + ", month:" + month + "}";
        }

    }

    private final HashMap<ScheduleTransactionCacheKey, Double> cacheForecastAmount = new HashMap<>();

    ScheduleTransactionForecastList() {
    }

    ScheduleTransactionForecastList(ArrayList<RecurringTransaction> recurringTransactionsList) {
        this.addAll(recurringTransactionsList);
    }

    public ArrayList<RecurringTransaction> getRecurringTransactionsList() {
        return this;
    }

    public ScheduleTransactionForecastList getRecurringTransactions(MmxDate start, MmxDate end) {
        return getRecurringTransactions(recurringTransaction -> recurringTransaction.getPaymentDate().compareTo(start.toDate()) >= 0 &&
                recurringTransaction.getPaymentDate().compareTo(end.toDate()) <= 0);
    }

    public ScheduleTransactionForecastList getRecurringTransactions(long categoryId) {
        return getRecurringTransactions(recurringTransaction -> recurringTransaction.getCategoryId() == categoryId);
    }

    public ScheduleTransactionForecastList getRecurringTransactions(long categoryId, MmxDate start, MmxDate end) {
        return getRecurringTransactions(recurringTransaction -> recurringTransaction.getPaymentDate().compareTo(start.toDate()) >= 0 &&
                recurringTransaction.getPaymentDate().compareTo(end.toDate()) <= 0 &&
                recurringTransaction.getCategoryId() == categoryId);
    }

    public ScheduleTransactionForecastList getRecurringTransactions(Predicate<RecurringTransaction> predicate) {
//        List list = this.stream().filter(predicate).collect(Collectors.toList());
        List<RecurringTransaction> list = this.parallelStream().filter(predicate).collect(Collectors.toList());
        return new ScheduleTransactionForecastList((ArrayList<RecurringTransaction>) list);
    }

    public void orderByDateAscending() {
        this.sort(Comparator.comparing(RecurringTransaction::getPaymentDate));
    }

    public ScheduleTransactionForecastList populateCacheForCategory() {
        cacheForecastAmount.clear();
        for (RecurringTransaction recurringTransaction : this ) {
            ScheduleTransactionCacheKey key = new ScheduleTransactionCacheKey(recurringTransaction);
            if( cacheForecastAmount.containsKey(key)) {
                cacheForecastAmount.put(key,
                        cacheForecastAmount.get(key)
                                + recurringTransaction.getRealSignedAmount().toDouble() );
            } else {
                cacheForecastAmount.put(key, recurringTransaction.getRealSignedAmount().toDouble());
            }
        }
        return this;
    }

    public Double getForecastAmountFromCache(long categoryId, int year, int month) {
        return cacheForecastAmount.getOrDefault(new ScheduleTransactionCacheKey(categoryId, year, month), 0.0);
    }

    public Double getTotalAmount() {
        if (this.size() == 0) return 0.0;

        // todo handle currency
        double total = 0.0;
        for ( RecurringTransaction recurringTransaction : this ) {
            total += recurringTransaction.getRealSignedAmount().toDouble();
        }
        return total;
    }

}
