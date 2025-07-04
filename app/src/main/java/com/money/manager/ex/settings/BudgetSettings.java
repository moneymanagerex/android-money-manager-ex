/*
 * Copyright (C) 2012-2016 The Android Money Manager Ex Project Team
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

package com.money.manager.ex.settings;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;

import com.money.manager.ex.R;
import com.money.manager.ex.core.InfoKeys;
import com.money.manager.ex.servicelayer.InfoService;
import com.money.manager.ex.utils.MmxDate;

/**
 * Budget preferences
 */
public class BudgetSettings
    extends SettingsBase {

    public BudgetSettings(Context context) {
        super(context);
    }

    @Override
    protected SharedPreferences getPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(getContext());
    }

    public Boolean getShowSimpleView() {
        return get(R.string.pref_budget_show_simple_view, false);
    }

    public void setShowSimpleView(boolean value) {
        set(R.string.pref_budget_show_simple_view, value);
    }

    public Boolean getBudgetFinancialYear( ) {
        return get(R.string.pref_budget_financial_year, false);
    }

    public void setBudgetFinancialYear( Boolean value ) {
        set(R.string.pref_budget_financial_year, value);
    }

    public MmxDate getBudgetDateFromForYear(int year) {
        if ( getBudgetFinancialYear() ) {
            MmxDate newDate = MmxDate.newDate();
            try {
                InfoService infoService = new InfoService(getContext());
                int financialYearStartDay = Integer.valueOf(infoService.getInfoValue(InfoKeys.FINANCIAL_YEAR_START_DAY, "1"));
                int financialYearStartMonth = Integer.valueOf(infoService.getInfoValue(InfoKeys.FINANCIAL_YEAR_START_MONTH, "0")) - 1;
                newDate.setYear(year);
                newDate.setDate(financialYearStartDay);
                newDate.setMonth(financialYearStartMonth);
            } catch (Exception e) {
                newDate.setYear(year);
                newDate.setDate(1);
                newDate.setMonth(0);
            }
            return newDate;
        } else {
            return new MmxDate(year, 0, 1);
        }
    }

    public MmxDate getBudgetDateToForYear(int year) {
        MmxDate newDate = getBudgetDateFromForYear(year);
        newDate.addYear(1).minusDays(1);
        return newDate;
    }


    public boolean getColumnVisible(int id, boolean defaultValue) {
        String key = "budgetColumn_" + id;
        return get(key, defaultValue);
    }
    public void setColumnVisible(int id, boolean value) {
        String key = "budgetColumn_" + id;
        set(key, value);
    }

}
