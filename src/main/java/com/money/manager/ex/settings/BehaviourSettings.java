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

import com.money.manager.ex.R;

import info.javaperformance.money.Money;
import info.javaperformance.money.MoneyFactory;

/**
 * Settings in the General category.
 */
public class BehaviourSettings
    extends SettingsBase {

    public BehaviourSettings(Context context) {
        super(context);

    }

//    public Money getAssetAllocationDifferenceThreshold() {
//        SharedPreferences preferences = getSharedPreferences();
//        String key = mContext.getString(R.string.pref_behaviour_asset_allocation_threshold);
//        String value = preferences.getString(key, "0");
//
//        return MoneyFactory.fromString(value);
//    }

//    public void setAssetAllocationDifferenceThreshold(Money value) {
//        set(R.string.pref_behaviour_asset_allocation_threshold, value.toString());
//    }

    public boolean getNotificationRecurringTransaction() {
        SharedPreferences preferences = getSharedPreferences();
        String key = mContext.getString(R.string.pref_repeating_transaction_notifications);
        boolean notify = preferences.getBoolean(key, true);
        return notify;
    }

    public boolean getFilterInSelectors() {
        boolean result = getSharedPreferences().getBoolean(
                mContext.getString(R.string.pref_behaviour_focus_filter), true);
        return result;
    }

    /**
     * The period to use for the income/expense summary footer on Home screen.
     * @return
     */
    public String getIncomeExpensePeriod() {
        String result = getSharedPreferences().getString(
                mContext.getString(R.string.pref_income_expense_footer_period),
                mContext.getString(R.string.last_month)
        );
        return result;
    }

    public Boolean getShowTutorial() {
        return get(R.string.pref_show_tutorial, true);
    }

    public void setShowTutorial(boolean value) {
        set(R.string.pref_show_tutorial, value);
    }
}
