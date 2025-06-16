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

package com.money.manager.ex.domainmodel;

import android.content.ContentValues;
import android.database.Cursor;

import com.money.manager.ex.budget.BudgetPeriodEnum;

/**
 * A Budget Entry, part of the budget.
 */
public class BudgetEntry
    extends EntityBase {

    public static final String BUDGETENTRYID = "BUDGETENTRYID";
    public static final String BUDGETYEARID = "BUDGETYEARID";
    public static final String CATEGID = "CATEGID";
    public static final String PERIOD = "PERIOD";
    public static final String AMOUNT = "AMOUNT";
    public static final String NOTES = "NOTES";
    public static final String ACTIVE = "ACTIVE";
    
    private BudgetPeriodEnum periodEnum;

    // Default constructor setting ACTIVE to 1L
    public BudgetEntry() {
        super();
        setPeriod(BudgetPeriodEnum.NONE.getDisplayName());
        setLong(BudgetEntry.ACTIVE, 1L);  // ACTIVE is true by default (1L)
    }

    // Constructor accepting ContentValues for initialization
    public BudgetEntry(ContentValues contentValues) {
        super(contentValues);
        setPeriodEnum();
    }

    @Override
    public void loadFromCursor(Cursor cursor) {
        super.loadFromCursor(cursor);
        setPeriodEnum();
    }

    // Getter and setter for BudgetEntryId
    public Long getBudgetEntryId() {
        return getLong(BudgetEntry.BUDGETENTRYID);
    }

    public void setBudgetEntryId(Long value) {
        setLong(BudgetEntry.BUDGETENTRYID, value);
    }

    // Getter and setter for BudgetYearId
    public Long getBudgetYearId() {
        return getLong(BudgetEntry.BUDGETYEARID);
    }

    public void setBudgetYearId(Long value) {
        setLong(BudgetEntry.BUDGETYEARID, value);
    }

    // Getter and setter for CategId
    public Long getCategoryId() {
        return getLong(BudgetEntry.CATEGID);
    }

    public void setCategoryId(Long value) {
        setLong(BudgetEntry.CATEGID, value);
    }

    // Getter and setter for Period
    public String getPeriod() {
        String value = getString(BudgetEntry.PERIOD);
        return value == null ? BudgetPeriodEnum.NONE.getDisplayName() : value;
    }

    public void setPeriod(String value) {
        setString(BudgetEntry.PERIOD, value);
        setPeriodEnum();
    }

    private void setPeriodEnum() {
        periodEnum = BudgetPeriodEnum.fromString(getPeriod());
    }


    // Getter and setter for Amount
    public double getAmount() {
        Double amount = getDouble(BudgetEntry.AMOUNT);
        return amount == null ? 0.0 : amount;
    }

    public void setAmount(Double value) {
        if (value == null) {
            value = 0.0;
        }
        setDouble(BudgetEntry.AMOUNT, value);
    }

    // Getter and setter for Notes
    public String getNotes() {
        return getString(BudgetEntry.NOTES);
    }

    public void setNotes(String value) {
        setString(BudgetEntry.NOTES, value);
    }

    // Getter and setter for Active
    public Boolean getActive() {
        return getLong(ACTIVE) == null || getLong(ACTIVE) != 0L;
    }

    public void setActive(Boolean value) {
        setLong(ACTIVE, value ? 1L : 0L);
    }

    @Override
    public String getPrimaryKeyColumn() {
        return BudgetEntry.BUDGETENTRYID;  // This returns the column name specific to Report
    }
    
    // getter for period Amount via BudgetPeriod

    public BudgetPeriodEnum getPeriodEnum() {
        if (periodEnum == null) {
            // try to set default
            setPeriodEnum();
        }
        return  periodEnum ;
    }

    public double getYearlyAmount() {
//        return BudgetPeriods.getYearlyEstimate(getPeriodEnum(), getAmount());
        return getAmount() * periodEnum.getOccursTimes() ;
    }

    public double getMonthlyAmount() {
//        return BudgetPeriods.getMonthlyEstimate(getPeriodEnum(), getAmount());
        return getYearlyAmount() / 12 ;
    }
}
