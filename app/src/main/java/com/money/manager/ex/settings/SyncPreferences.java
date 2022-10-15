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

import com.google.common.primitives.Ints;
import com.money.manager.ex.R;

/**
 * Handles sync-related preferences.
 */
public class SyncPreferences
    extends SettingsBase {

    public SyncPreferences(Context context) {
        super(context);

    }

    /**
     * Delete all preferences.
     */
    public void clear() {
        getPreferences().edit().clear().apply();
    }

    public boolean get(Integer key, boolean defaultValue) {
        return getPreferences().getBoolean(getKey(key), defaultValue);
    }

    public String get(Integer key, String defaultValue) {
        return getPreferences().getString(getKey(key), defaultValue);
    }

    @Override
    protected SharedPreferences getPreferences() {
        return getContext().getSharedPreferences(PreferenceConstants.SYNC_PREFERENCES, Context.MODE_PRIVATE);
    }

    public boolean isSyncEnabled() {
        return get(R.string.pref_sync_enabled, false);
    }

    public int getSyncInterval() {
        int defaultSchedule = 30;   // time in minutes
        String setSchedule = get(R.string.pref_sync_interval, Integer.toString(defaultSchedule));

        Integer scheduleInt = Ints.tryParse(setSchedule);
        if (scheduleInt == null) return defaultSchedule;

        return scheduleInt;
    }

    public boolean getUploadImmediately() {
        return get(R.string.pref_upload_immediately, true);
    }

    public String loadPreference(Integer key, String defaultValue) {
        String realKey = getContext().getString(key);

        return getPreferences().getString(realKey, defaultValue);
    }

    public void setSyncEnabled(boolean value) {
        set(R.string.pref_sync_enabled, value);
    }

    /**
     * Set synchronization period.
     * @param value Sync frequency in minutes.
     */
    public void setSyncInterval(int value) {
        set(R.string.pref_sync_interval, Integer.toString(value));
    }

    public boolean shouldSyncOnlyOnWifi() {
        return get(R.string.pref_sync_via_wifi, false);
    }

    // private

    private String getKey(Integer resourceId) {
        return getContext().getString(resourceId);
    }
}
