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

import com.money.manager.ex.R;

/**
 * Manipulates database preferences/preferences.
 */
public class DatabaseSettings {

    public DatabaseSettings(AppSettings mainSettings) {
        mAppSettings = mainSettings;
    }

    private AppSettings mAppSettings;

    public String getDatabasePath() {
        String path = mAppSettings.get(R.string.pref_database_path, "");
        return path;
    }

    public void setDatabasePath(String path) {
        mAppSettings.set(R.string.pref_database_path, path);
    }
}
