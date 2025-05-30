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

import android.os.Bundle;

import com.money.manager.ex.core.DefinedDateRange;
import com.money.manager.ex.core.DefinedDateRangeName;
import com.money.manager.ex.core.DefinedDateRanges;
import com.money.manager.ex.R;
import com.money.manager.ex.home.MainActivity;
import com.money.manager.ex.view.RobotoView;

import androidx.preference.CheckBoxPreference;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

import timber.log.Timber;

/**
 * Look & feel preferences.
 */
public class LookFeelPreferenceFragment
    extends PreferenceFragmentCompat {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActivity().setTitle(R.string.preferences_display);

        addPreferencesFromResource(R.xml.preferences_look_and_feel);

        PreferenceManager.getDefaultSharedPreferences(getActivity());

        final LookAndFeelSettings settings = new AppSettings(getActivity()).getLookAndFeelSettings();

        // Show Open accounts

        final CheckBoxPreference chkAccountOpen = findPreference(getString(R.string.pref_account_open_visible));
        if (chkAccountOpen != null) {
            // set initial value
            boolean showOpenAccounts = settings.getViewOpenAccounts();
            chkAccountOpen.setChecked(showOpenAccounts);

            chkAccountOpen.setOnPreferenceChangeListener((preference, newValue) -> {
                settings.setViewOpenAccounts((Boolean) newValue);
                MainActivity.setRestartActivity(true);
                return true;
            });
        }

        // Show Favourite accounts

        final CheckBoxPreference chkAccountFav = findPreference(getString(R.string.pref_account_fav_visible));
        if (chkAccountFav != null) {
            // set initial value
            boolean showOpenAccounts = settings.getViewFavouriteAccounts();
            chkAccountFav.setChecked(showOpenAccounts);

            chkAccountFav.setOnPreferenceChangeListener((preference, newValue) -> {
                settings.setViewFavouriteAccounts((Boolean) newValue);
                MainActivity.setRestartActivity(true);
                return true;
            });
        }

        // Hide reconciled amounts setting.

        final SwitchPreferenceCompat chkHideReconciled = findPreference(getString(
                R.string.pref_transaction_hide_reconciled_amounts));

        // Set the main activity to restart on change of any of the following preferences.
        chkHideReconciled.setOnPreferenceChangeListener((preference, newValue) -> {
            MainActivity.setRestartActivity(true);
            return true;
        });

        // Show Transactions, period

        final ListPreference lstShow = findPreference(getString(
                R.string.pref_show_transaction));
        if (lstShow != null) {
            // set the available values for selection.

            final DefinedDateRanges ranges = new DefinedDateRanges(getActivity());
            lstShow.setEntries(ranges.getLocalizedNames());
            lstShow.setEntryValues(ranges.getValueNames());

            // Show current value.

            final DefinedDateRangeName rangeName = new AppSettings(getActivity()).getLookAndFeelSettings()
                    .getShowTransactions();
            DefinedDateRange range = ranges.get(rangeName);

            lstShow.setSummary(range.getLocalizedName(getActivity()));
            lstShow.setOnPreferenceChangeListener((preference, newValue) -> {
                String newRangeKey = newValue.toString();
                DefinedDateRange range1 = ranges.getByName(newRangeKey);

                lstShow.setSummary(range1.getLocalizedName(getActivity()));
                return true;
            });
        }

        // Font type
        final ListPreference lstFont = findPreference(getString(PreferenceConstants.PREF_APPLICATION_FONT));
        if (lstFont != null) {
            lstFont.setOnPreferenceChangeListener((preference, newValue) -> {
                Integer newInt = Integer.parseInt(newValue.toString());
                if (newInt != null) {
                    Timber.d("Preference set: font = %s", newValue.toString());

                    RobotoView.setUserFont(newInt);
                    return true;
                }
                return false;
            });
        }

        // Font size
        final ListPreference lstFontSize = findPreference(getString(PreferenceConstants.PREF_APPLICATION_FONT_SIZE));
        if (lstFontSize != null) {
            lstFontSize.setSummary(lstFontSize.getValue());
            lstFontSize.setOnPreferenceChangeListener((preference, newValue) -> {
                Timber.d("Preference set: font = %s", newValue.toString());
                lstFontSize.setSummary(newValue.toString());

                RobotoView.setUserFontSize(getActivity().getApplicationContext(), newValue.toString());
                return true;
            });
        }
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
//        Timber.d("creating");
    }
}
