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

import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

import com.money.manager.ex.R;

/**
 * View Holder for investment preferences
 */
public class InvestmentPreferencesViewHolder {
    public InvestmentPreferencesViewHolder(PreferenceFragmentCompat container) {
        threshold = container.findPreference(container.getString(R.string.pref_asset_allocation_threshold));
        quoteProvider = (ListPreference) container.findPreference(container.getString(R.string.pref_quote_provider));
        exchangeRateProvider = (ListPreference) container.findPreference(container.getString(R.string.pref_exchange_rate_provider));
    }

    public Preference threshold;
    public ListPreference quoteProvider;
    public ListPreference exchangeRateProvider;
}
