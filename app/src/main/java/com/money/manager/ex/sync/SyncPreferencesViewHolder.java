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

package com.money.manager.ex.sync;

import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

import com.money.manager.ex.R;

/**
 * View holder for sync preferences.
 */
public class SyncPreferencesViewHolder {

    public CheckBoxPreference syncEnabled;
    public ListPreference providerList;
    public Preference remoteFile;
    public ListPreference syncInterval;
    public Preference resetPreferences;
    public PreferenceScreen download;
    public PreferenceScreen upload;

    public SyncPreferencesViewHolder(PreferenceFragment view) {
        // Compat

        syncEnabled = (CheckBoxPreference) view.findPreference(view.getString(R.string.pref_sync_enabled));
        providerList = (ListPreference) view.findPreference(view.getString(R.string.pref_sync_provider));
        remoteFile = view.findPreference(view.getString(R.string.pref_remote_file));
        syncInterval = (ListPreference) view.findPreference(view.getString(R.string.pref_sync_interval));
        resetPreferences = view.findPreference(view.getString(R.string.pref_reset_preferences));
        download = (PreferenceScreen) view.findPreference(view.getString(R.string.pref_sync_download));
        upload = (PreferenceScreen) view.findPreference(view.getString(R.string.pref_sync_upload));
    }

}
