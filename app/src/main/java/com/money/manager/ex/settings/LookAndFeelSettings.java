/*
 * Copyright (C) 2012-2025 The Android Money Manager Ex Project Team
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
import android.text.TextUtils;

import com.money.manager.ex.R;
import com.money.manager.ex.core.InfoKeys;
import com.money.manager.ex.servicelayer.InfoService;
import com.money.manager.ex.core.DateRange;
import com.money.manager.ex.core.DefinedDateRange;
import com.money.manager.ex.core.DefinedDateRangeName;
import com.money.manager.ex.core.DefinedDateRanges;
import com.money.manager.ex.utils.MmxDate;

import java.util.Date;

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

    public boolean getHideBalances() {
        String key = getSettingsKey(R.string.pref_hide_balances);
        return getBooleanSetting(key, false);
    }

    public void setHideBalances(boolean value) {
        String key = getSettingsKey(R.string.pref_hide_balances);
        set(key, value);
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

    /**
     * The custom date range selected for the account transactions list, or null when a
     * pre-defined period is active. Stored as two ISO dates separated by '|'.
     */
    public DateRange getShowTransactionsCustomRange() {
        String key = getSettingsKey(R.string.pref_show_transaction_custom);
        String value = get(key, "");
        if (TextUtils.isEmpty(value)) {
            return null;
        }

        String[] parts = value.split("\\|");
        if (parts.length != 2) {
            return null;
        }

        try {
            Date from = new MmxDate(parts[0]).toDate();
            Date to = new MmxDate(parts[1]).toDate();
            return new DateRange(from, to);
        } catch (Exception e) {
            Timber.w(e, "error parsing custom transaction date range");
            return null;
        }
    }

    public void setShowTransactionsCustomRange(Date from, Date to) {
        String key = getSettingsKey(R.string.pref_show_transaction_custom);
        if (from == null || to == null) {
            set(key, "");
        } else {
            set(key, new MmxDate(from).toIsoDateString() + "|" + new MmxDate(to).toIsoDateString());
        }
    }

    public boolean getViewOpenAccounts() {
        String key = getSettingsKey(R.string.pref_account_open_visible);
        return getBooleanSetting(key, true);
    }

    public void setViewOpenAccounts(Boolean value) {
        String key = getSettingsKey(R.string.pref_account_open_visible);
        set(key, value);
    }

    public boolean getViewFavouriteAccounts() {
        String key = getSettingsKey(R.string.pref_account_fav_visible);
        return getBooleanSetting(key, false);
    }

    public void setViewFavouriteAccounts(Boolean value) {
        String key = getSettingsKey(R.string.pref_account_fav_visible);
        set(key, value);
    }

    public boolean getSortTransactionsByType() {
        String key = getSettingsKey(R.string.pref_transaction_sort_by_type);
        return getBooleanSetting(key, true);
    }
}
