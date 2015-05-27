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

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.text.TextUtils;
import android.util.Log;

import com.money.manager.ex.Constants;
import com.money.manager.ex.DonateActivity;
import com.money.manager.ex.R;
import com.money.manager.ex.about.AboutActivity;

/**
 *
 */
public class SettingsFragment
        extends PreferenceFragment {

    private String LOGCAT = this.getClass().getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
        PreferenceManager.getDefaultSharedPreferences(getActivity());

        // find preferences
        final Preference generalPreference = findPreference(getString(PreferenceConstants.PREF_GENERAL));
        if (generalPreference != null) {
            generalPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    startActivity(new Intent(getActivity(), GeneralSettingsActivity.class));
                    return true;
                }
            });
        }

        final Preference lookAndFeelPreference = findPreference(getString(PreferenceConstants.PREF_LOOK_FEEL));
        if (lookAndFeelPreference != null) {
            lookAndFeelPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    startActivity(new Intent(getActivity(), LookFeelSettingsActivity.class));
                    return true;
                }
            });
        }

        final Preference passcodePreference = findPreference(getString(PreferenceConstants.PREF_SECURITY));
        if (passcodePreference != null) {
            passcodePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    startActivity(new Intent(getActivity(), SecuritySettingsActivity.class));
                    return true;
                }
            });
        }

        final Preference databasesPreference = findPreference(getString(PreferenceConstants.PREF_DATABASE));
        if (databasesPreference != null) {
            databasesPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    startActivity(new Intent(getActivity(), DatabaseSettingsActivity.class));
                    return true;
                }
            });
        }

        final Preference dropboxPreference = findPreference(getString(PreferenceConstants.PREF_DROPBOX_HOWITWORKS));
        if (dropboxPreference != null) {
            dropboxPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    startActivity(new Intent(getActivity(), DropboxSettingsActivity.class));
                    return true;
                }
            });
        }

        //donate
        final Preference pDonate = findPreference(getString(PreferenceConstants.PREF_DONATE));
        if (pDonate != null) {
            pDonate.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    startActivity(new Intent(getActivity(), DonateActivity.class));
                    return false;
                }
            });
        }

        final Preference infoPreference = findPreference(getString(PreferenceConstants.PREF_VERSION_NAME));
        if (infoPreference != null) {
            infoPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference preference) {
                    startActivity(new Intent(getActivity(), AboutActivity.class));
                    return true;
                }
            });
        }

        // manage intent
        if (getActivity().getIntent() != null) {
            if (!TextUtils.isEmpty(getActivity().getIntent().getStringExtra(Constants.INTENT_REQUEST_PREFERENCES_SCREEN))) {
                try {
                    PreferenceScreen screen = getPreferenceScreen();
                    Preference preference = findPreference(getActivity().getIntent().getStringExtra(Constants.INTENT_REQUEST_PREFERENCES_SCREEN));
                    if (preference != null) {
                        screen.onItemClick(null, null, preference.getOrder(), 0);
                    }
                } catch (Exception e) {
                    Log.e(LOGCAT, e.getMessage());
                }
            }
        }
    }
}
