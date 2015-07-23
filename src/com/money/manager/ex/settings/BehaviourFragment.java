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

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;

import com.money.manager.ex.BuildConfig;
import com.money.manager.ex.MoneyManagerApplication;
import com.money.manager.ex.R;
import com.money.manager.ex.home.MainActivity;
import com.money.manager.ex.view.RobotoView;

import org.apache.commons.lang3.math.NumberUtils;

/**
 * Look & feel settings.
 */
public class BehaviourFragment
        extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.settings_behaviour);

        PreferenceManager.getDefaultSharedPreferences(getActivity());

        // checkbox on open and favorite account
        final CheckBoxPreference chkFilter = (CheckBoxPreference) findPreference(getString(
                R.string.pref_behaviour_focus_filter));

//        Preference.OnPreferenceChangeListener listener = new Preference.OnPreferenceChangeListener() {
//            @Override
//            public boolean onPreferenceChange(Preference preference, Object newValue) {
//                MainActivity.setRestartActivity(true);
//                return true;
//            }
//        };
        // Set the main activity to restart on change of any of the following settings.
//        chkFilter.setOnPreferenceChangeListener(listener);

    }
}
