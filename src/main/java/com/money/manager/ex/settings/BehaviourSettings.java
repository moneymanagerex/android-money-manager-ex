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
import com.money.manager.ex.core.InfoKeys;
import com.money.manager.ex.servicelayer.InfoService;

import org.apache.commons.lang3.StringUtils;

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

    /**
     * Threshold percentage at which the difference in actual asset allocation vs set asset
     * allocation will be painted. Green, if the current allocation is higher than the set allocation,
     * and red if it is smaller for the set percentage of the original value.
     * I.e. 20 represents 20% difference compared to the set asset allocation value.
     * @return A number that represents the percentage value.
     */
    public Money getAssetAllocationDifferenceThreshold() {
        InfoService service = new InfoService(getContext());
        String value = service.getInfoValue(InfoKeys.ASSET_ALLOCATION_DIFF_THRESHOLD);

        if (StringUtils.isEmpty(value)) {
            value = "-1";
        }
        return MoneyFactory.fromString(value);
    }

    public void setAssetAllocationDifferenceThreshold(Money value) {
        InfoService service = new InfoService(getContext());
        service.setInfoValue(InfoKeys.ASSET_ALLOCATION_DIFF_THRESHOLD, value.toString());
    }

    public boolean getNotificationRecurringTransaction() {
        SharedPreferences preferences = getSharedPreferences();
        String key = getContext().getString(PreferenceConstants.PREF_REPEATING_TRANSACTION_NOTIFICATIONS);
        boolean notify = preferences.getBoolean(key, true);
        return notify;
    }

    public String getNotificationTime() {
        return get(PreferenceConstants.PREF_REPEATING_TRANSACTION_CHECK, "08:00");
    }

    public void setNotificationTime(String timeString) {
        set(PreferenceConstants.PREF_REPEATING_TRANSACTION_CHECK, timeString);
    }

    public boolean getFilterInSelectors() {
        boolean result = getSharedPreferences().getBoolean(
                getContext().getString(R.string.pref_behaviour_focus_filter), true);
        return result;
    }

    /**
     * The period to use for the income/expense summary footer on Home screen.
     * @return
     */
    public String getIncomeExpensePeriod() {
        String result = getSharedPreferences().getString(
                getContext().getString(R.string.pref_income_expense_footer_period),
                getContext().getString(R.string.last_month)
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
