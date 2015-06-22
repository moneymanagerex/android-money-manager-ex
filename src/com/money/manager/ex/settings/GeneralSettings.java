/*
 * Copyright (C) 2012-2015 The Android Money Manager Ex Project Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
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
import android.preference.PreferenceManager;

import com.money.manager.ex.R;

/**
 * Settings in the General category.
 */
public class GeneralSettings {
    public GeneralSettings(AppSettings settings) {
        this.mSettings = settings;
        this.mContext = settings.getContext();
    }

    private final Context mContext;
    private AppSettings mSettings;

    public String getApplicationLocale() {
        String result = PreferenceManager.getDefaultSharedPreferences(mContext.getApplicationContext())
                .getString(mContext.getString(PreferenceConstants.PREF_LOCALE), "");
        return result;
    }

//    public int getBaseCurrency() {
//        SharedPreferences preferences = getSharedPreferences();
//
//        String key = mContext.getString(PreferenceConstants.PREF_BASE_CURRENCY);
////        String key2 = mContext.getString(R.string.pref_base_currency);
//
//        int currencyId = preferences.getInt(key, -1);
//
//        return currencyId;
//    }

    public boolean getNotificationRecurringTransaction() {
        SharedPreferences preferences = getSharedPreferences();
        String key = mContext.getString(R.string.pref_repeating_transaction_notifications);
        boolean notify = preferences.getBoolean(key, true);
        return notify;
    }

    private SharedPreferences getSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(mContext.getApplicationContext());
    }

    public String getDefaultAccount() {
        String key = mContext.getString(PreferenceConstants.PREF_DEFAULT_ACCOUNT);
        String result = getSharedPreferences().getString(key, "");
        return result;
    }

//    public boolean setBaseCurrency(int currencyId) {
//        String key = mContext.getString(PreferenceConstants.PREF_BASE_CURRENCY);
//        return mSettings.set(key, currencyId);
//    }
}
