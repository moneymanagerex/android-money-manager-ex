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
import android.text.TextUtils;

import com.money.manager.ex.Constants;
import com.money.manager.ex.R;
import com.money.manager.ex.core.InfoKeys;
import com.money.manager.ex.servicelayer.InfoService;
import com.money.manager.ex.core.DefinedDateRange;
import com.money.manager.ex.core.DefinedDateRangeName;
import com.money.manager.ex.core.DefinedDateRanges;

import timber.log.Timber;

/**
 * Look & Feel preferences
 */
public class LookAndFeelSettings
    extends SettingsBase {

    public LookAndFeelSettings(Context context) {
        super(context);

    }

    @Override
    protected SharedPreferences getPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(getContext());
    }

    public boolean getHideReconciledAmounts() {
        String key = getSettingsKey(R.string.pref_transaction_hide_reconciled_amounts);
        return getBooleanSetting(key, false);
    }

    public DefinedDateRangeName getShowTransactions() {
        DefinedDateRangeName defaultValue = DefinedDateRangeName.LAST_7_DAYS;

        String value = get(R.string.pref_show_transaction, defaultValue.name());

        if (TextUtils.isEmpty(value)) {
            return defaultValue;
        }

        DefinedDateRangeName result = null;

        // try directly first
        try {
            result = DefinedDateRangeName.valueOf(value);
        } catch (IllegalArgumentException e) {
            Timber.w("error parsing default date range");
        }
        if (result != null) {
            return result;
        }

        // then try by the previous setting, localized range name
        DefinedDateRanges ranges = new DefinedDateRanges(getContext());
        DefinedDateRange range = ranges.getByLocalizedName(value);
        if (range != null) {
            setShowTransactions(range.key);
            return range.key;
        }

        // if still not found, initialize to a default value.
        setShowTransactions(defaultValue);
        return defaultValue;
    }

    public void setShowTransactions(DefinedDateRangeName value) {
        String key = getSettingsKey(R.string.pref_show_transaction);
        set(key, value.toString());
    }

    public boolean getViewOpenAccounts() {
//        return get(R.string.pref_account_open_visible, true);
        InfoService infoService = new InfoService(getContext());
        String value = infoService.getInfoValue(InfoKeys.SHOW_OPEN_ACCOUNTS);
        return Boolean.valueOf(value);
    }

    public void setViewOpenAccounts(Boolean value) {
        InfoService infoService = new InfoService(getContext());
        infoService.setInfoValue(InfoKeys.SHOW_OPEN_ACCOUNTS, value.toString());
    }

    public boolean getViewFavouriteAccounts() {
//        return get(R.string.pref_account_fav_visible, true);
        InfoService infoService = new InfoService(getContext());
        String value = infoService.getInfoValue(InfoKeys.SHOW_FAVOURITE_ACCOUNTS);
        return Boolean.valueOf(value);
    }

    public void setViewFavouriteAccounts(Boolean value) {
        InfoService infoService = new InfoService(getContext());
        infoService.setInfoValue(InfoKeys.SHOW_FAVOURITE_ACCOUNTS, value.toString());
    }

    public boolean getSortTransactionsByType() {
        String key = getSettingsKey(R.string.pref_transaction_sort_by_type);
        return getBooleanSetting(key, true);
    }
}
