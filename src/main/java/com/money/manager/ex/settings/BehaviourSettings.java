/*
 * Copyright (C) 2012-2015 The Android Money Manager Ex Project Team
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
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.money.manager.ex.settings;

import android.content.Context;
import android.content.SharedPreferences;

import com.money.manager.ex.R;

/**
 * Settings in the General category.
 */
public class BehaviourSettings
    extends SettingsBase {

    public BehaviourSettings(Context context) {
        super(context);

    }

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
