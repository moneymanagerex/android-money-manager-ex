/*
 * Copyright (C) 2012-2015 Money Manager Ex project
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

import com.money.manager.ex.MainActivity;
import com.money.manager.ex.MoneyManagerApplication;
import com.money.manager.ex.R;
import com.money.manager.ex.view.RobotoView;

import org.apache.commons.lang3.math.NumberUtils;

/**
 * Look & feel settings.
 */
public class LookFeelFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.look_and_feel_settings);
        PreferenceManager.getDefaultSharedPreferences(getActivity());

        // checkbox on open and favorite account
        final CheckBoxPreference chkAccountOpen = (CheckBoxPreference) findPreference(getString(PreferencesConstant.PREF_ACCOUNT_OPEN_VISIBLE));
        final CheckBoxPreference chkAccountFav = (CheckBoxPreference) findPreference(getString(PreferencesConstant.PREF_ACCOUNT_FAV_VISIBLE));

        Preference.OnPreferenceChangeListener listener = new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                MainActivity.setRestartActivity(true);
                return true;
            }
        };
        // set listener on the checkbox
        chkAccountOpen.setOnPreferenceChangeListener(listener);
        chkAccountFav.setOnPreferenceChangeListener(listener);

        // show transaction
        final ListPreference lstShow = (ListPreference) findPreference(getString(PreferencesConstant.PREF_SHOW_TRANSACTION));
        if (lstShow != null) {
            lstShow.setSummary(MoneyManagerApplication.getInstanceApp().getShowTransaction());
            lstShow.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    lstShow.setSummary((CharSequence) newValue);
                    return true;
                }
            });
        }

        // font type
        final ListPreference lstFont = (ListPreference) findPreference(getString(PreferencesConstant.PREF_APPLICATION_FONT));
        if (lstFont != null) {
            lstFont.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if (newValue instanceof String && NumberUtils.isNumber(newValue.toString())) {
                        RobotoView.setUserFont(Integer.parseInt(newValue.toString()));
                        return true;
                    }
                    return false;
                }
            });
        }

        //font size
        final ListPreference lstFontSize = (ListPreference) findPreference(getString(PreferencesConstant.PREF_APPLICATION_FONT_SIZE));
        if (lstFontSize != null) {
            lstFontSize.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    RobotoView.setUserFontSize(getActivity().getApplicationContext(), newValue.toString());
                    return true;
                }
            });
        }

        //theme
        final ListPreference lstTheme = (ListPreference) findPreference(getString(PreferencesConstant.PREF_THEME));
        if (lstTheme != null) {
            lstTheme.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    MainActivity.setRestartActivity(true);
                    return true;
                }
            });
        }
    }
}
