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

package com.money.manager.ex.settings;

import android.content.Context;
import android.content.SharedPreferences;

import com.money.manager.ex.R;

/**
 * Handles sync-related preferences.
 */
public class SyncPreferences {
    public SyncPreferences(Context context) {
        mContext = context;
    }

    private Context mContext;

    /**
     * Delete all preferences.
     */
    public void clear() {
        getSyncPreferences().edit().clear().apply();
    }

    public boolean get(Integer key, boolean defaultValue) {
        return getSyncPreferences().getBoolean(getKey(key), defaultValue);
    }

    public Context getContext() {
        return mContext;
    }

    public boolean isSyncEnabled() {
        return get(R.string.pref_sync_enabled, false);
    }

    public String loadPreference(Integer key, String defaultValue) {
        String realKey = getContext().getString(key);

        return getSyncPreferences().getString(realKey, defaultValue);
    }

    public void savePreference(Integer key, String value) {
        String realKey = getContext().getString(key);

        getSyncPreferences()
                .edit()
                .putString(realKey, value)
                .apply();
    }

    public void setSyncEnabled(boolean value) {
        set(R.string.pref_sync_enabled, value);
    }

    public boolean shouldSyncOnlyOnWifi() {
        return get(R.string.pref_sync_via_wifi, false);
    }

    // private

    private String getKey(Integer resourceId) {
        return getContext().getString(resourceId);
    }

    private SharedPreferences getSyncPreferences() {
        return getContext().getSharedPreferences(PreferenceConstants.SYNC_PREFERENCES, Context.MODE_PRIVATE);
    }

    private void set(Integer key, boolean value) {
        getSyncPreferences().edit()
                .putBoolean(getContext().getString(key), value)
                .apply();
    }
}
