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

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.money.manager.ex.MmexApplication;
import com.money.manager.ex.R;
import com.money.manager.ex.home.MainActivity;

import timber.log.Timber;

/**
 * Fragment that contains the general preferences.
 */
public class GeneralSettingsFragment
    extends PreferenceFragmentCompat {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences_general);

        initializeControls();
    }

    // Private

    private void initializeControls() {
        AppSettings settings = new AppSettings(getActivity());

        // Application Locale

        final ListPreference lstLocaleApp = findPreference(getString(R.string.pref_locale));
        if (lstLocaleApp != null) {
            String summary = settings.getGeneralSettings().getApplicationLanguage();
            setSummaryListPreference(lstLocaleApp, summary, R.array.application_locale_values, R.array.application_locale_entries);
            lstLocaleApp.setOnPreferenceChangeListener((preference, newValue) -> {
                String language = String.valueOf(newValue);
                setSummaryListPreference(preference, language, R.array.application_locale_values, R.array.application_locale_entries);

                restartActivity();

                return true;
            });
        }

        // Theme

        final ListPreference lstTheme = findPreference(getString(R.string.pref_theme));
        if (lstTheme != null) {
            String summary = settings.getGeneralSettings().getTheme();
            lstTheme.setSummary(summary);
            lstTheme.setOnPreferenceChangeListener((preference, newValue) -> {
                Timber.d("setting theme: %s", newValue.toString());
                lstTheme.setSummary(newValue.toString());

                restartActivity();

                return true;
            });
        }

        // default status

        final ListPreference lstDefaultStatus = findPreference(getString(PreferenceConstants.PREF_DEFAULT_STATUS));
        if (lstDefaultStatus != null) {
            setSummaryListPreference(lstDefaultStatus, lstDefaultStatus.getValue(), R.array.status_values, R.array.status_items);
            lstDefaultStatus.setOnPreferenceChangeListener((preference, newValue) -> {
                setSummaryListPreference(lstDefaultStatus, newValue.toString(), R.array.status_values, R.array.status_items);
                return true;
            });
        }

        //default payee

        final ListPreference lstDefaultPayee = findPreference(getString(PreferenceConstants.PREF_DEFAULT_PAYEE));
        if (lstDefaultPayee != null) {
            setSummaryListPreference(lstDefaultPayee, lstDefaultPayee.getValue(), R.array.new_transaction_dialog_values, R.array.new_transaction_dialog_items);
            lstDefaultPayee.setOnPreferenceChangeListener((preference, newValue) -> {
                setSummaryListPreference(lstDefaultPayee, newValue.toString(), R.array.new_transaction_dialog_values, R.array.new_transaction_dialog_items);
                return true;
            });
        }

        // send anonymous usage data
        final SwitchPreferenceCompat sPreference = findPreference(getString(R.string.pref_anonymous_usage));
        if (sPreference != null) {
            sPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                // Handle the switch state change
                boolean isChecked = (Boolean) newValue;
                MmexApplication.getAmplitude().getConfiguration().setOptOut(!isChecked);
                // Add your logic here based on the switch state
                return true; // Return true to persist the preference change
            });
        }
    }

    public void setSummaryListPreference(Preference preference, String value, int idArrayValues, int idArrayItems) {
        final String[] values = getResources().getStringArray(idArrayValues);
        final String[] items = getResources().getStringArray(idArrayItems);
        for (int i = 0; i < values.length; i++) {
            if (value.equals(values[i])) {
                preference.setSummary(items[i]);
            }
        }
    }

    private void restartActivity() {
        MainActivity.setRestartActivity(true);

        requireActivity().recreate();
    }
}
