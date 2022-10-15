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
package com.money.manager.ex.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

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

    @Override
    protected SharedPreferences getPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(getContext());
    }

    public boolean getNotificationRecurringTransaction() {
        return get(PreferenceConstants.PREF_REPEATING_TRANSACTION_NOTIFICATIONS, true);
    }

    public String getNotificationTime() {
        return get(PreferenceConstants.PREF_REPEATING_TRANSACTION_CHECK, "08:00");
    }

    public void setNotificationTime(String timeString) {
        set(PreferenceConstants.PREF_REPEATING_TRANSACTION_CHECK, timeString);
    }

    public boolean getFilterInSelectors() {
        return get(R.string.pref_behaviour_focus_filter, true);
    }

    /**
     * The period to use for the income/expense summary footer on Home screen.
     * @return
     */
    public String getIncomeExpensePeriod() {
        return get(R.string.pref_income_expense_footer_period,
                getContext().getString(R.string.last_month)
        );
    }

    public boolean getBankSmsTrans() {
        return get(PreferenceConstants.PREF_SMS_AUTOMATIC_TRANSACTIONS, false);
    }

    public void setBankSmsTrans(boolean status) {
        set(PreferenceConstants.PREF_SMS_AUTOMATIC_TRANSACTIONS, status);
    }

    public boolean getSmsTransStatusNotification() {
        return get(PreferenceConstants.PREF_SMS_TRANS_STATUS_NOTIFICATION, false);
    }

    public void setSmsTransStatusNotification(boolean status) {
        set(PreferenceConstants.PREF_SMS_TRANS_STATUS_NOTIFICATION, status);
    }

    public Boolean getShowTutorial() {
        return get(R.string.pref_show_tutorial, true);
    }

    public void setShowTutorial(boolean value) {
        set(R.string.pref_show_tutorial, value);
    }
}
