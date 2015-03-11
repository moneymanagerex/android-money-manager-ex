package com.money.manager.ex.utils;

import com.money.manager.ex.database.TableCurrencyFormats;

import java.util.Comparator;

/**
 * Compare two Currencies by Name.
 * Created by Alen Siljak on 11/03/2015.
 */
public class CurrencyNameComparator implements Comparator<TableCurrencyFormats> {
    @Override
    public int compare(TableCurrencyFormats o1, TableCurrencyFormats o2) {
        return o1.getCurrencyName().compareTo(o2.getCurrencyName());
    }
}
