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

import android.content.Context;
import android.widget.Toast;

import com.money.manager.ex.R;

/**
 * Manipulates database settings/preferences.
 */
public class DatabaseSettings {
    public DatabaseSettings(AppSettings mainSettings) {
        mAppSettings = mainSettings;
//        mContext = context;
//        mApplicationContext = context.getApplicationContext();
    }

    private AppSettings mAppSettings;

    public String getDatabasePath() {
//        String path = PreferenceManager.getDefaultSharedPreferences(mApplicationContext)
//                .getString(mContext.getString(PreferenceConstants.PREF_DATABASE_PATH), "");
        Context context = mAppSettings.getContext();
        String key = context.getString(PreferenceConstants.PREF_DATABASE_PATH);
        String path = mAppSettings.get(key, "");
        return path;
    }

    public boolean setDatabasePath(String path) {
        Context context = mAppSettings.getContext();
        String key = context.getString(PreferenceConstants.PREF_DATABASE_PATH);
        boolean pathSet = mAppSettings.set(key, path);
        return pathSet;
    }
}
