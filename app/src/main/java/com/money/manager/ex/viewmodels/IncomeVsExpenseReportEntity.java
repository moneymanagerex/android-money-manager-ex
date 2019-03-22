/*
 * Copyright (C) 2012-2019 The Android Money Manager Ex Project Team
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
package com.money.manager.ex.viewmodels;

import android.database.Cursor;
import android.database.DatabaseUtils;

import com.money.manager.ex.domainmodel.EntityBase;

import info.javaperformance.money.Money;

/**
 * A record/row in the Income/Expense report.
 */
public class IncomeVsExpenseReportEntity
    extends EntityBase {

    public static final String YEAR = "Year";
    public static final String Month = "Month";
    public static final String Income = "Income";
    public static final String Expenses = "Expenses";
    public static final String Transfers = "Transfers";

    public static IncomeVsExpenseReportEntity from(Cursor c) {
        IncomeVsExpenseReportEntity entity = new IncomeVsExpenseReportEntity();
        entity.loadFromCursor(c);

        return entity;
    }

    @Override
    public void loadFromCursor(Cursor c) {
        super.loadFromCursor(c);

        DatabaseUtils.cursorDoubleToContentValuesIfPresent(c, contentValues, Income);
        DatabaseUtils.cursorDoubleToContentValuesIfPresent(c, contentValues, Expenses);
        DatabaseUtils.cursorDoubleToContentValuesIfPresent(c, contentValues, Transfers);
    }

    public int getYear() {
        return getInt(YEAR);
    }

    public int getMonth() {
        return getInt(Month);
    }

    public Money getIncome() {
        return getMoney(Income);
    }

    public Money getExpenses() {
        return getMoney(Expenses);
    }

    public Money getTransfers() {
        return getMoney(Transfers);
    }
}
