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
package com.money.manager.ex.home;

import android.content.Context;

import androidx.annotation.NonNull;

import com.money.manager.ex.R;
import com.money.manager.ex.core.database.DatabaseManager;
import com.money.manager.ex.settings.AppSettings;
import com.money.manager.ex.settings.SyncPreferences;
import com.money.manager.ex.sync.SyncManager;
import com.money.manager.ex.utils.MmxDate;

/**
 * Factory for the database metadata records.
 */

public class DatabaseMetadataFactory {

    public static DatabaseMetadata getInstance(String localPath) {
        DatabaseMetadata db = new DatabaseMetadata();
        db.localPath = localPath;
        db.remotePath = "";
        return db;
    }

    public static DatabaseMetadata getInstance(String filePath, @NonNull String remoteFileName) {
        DatabaseMetadata entry = new DatabaseMetadata();
        entry.localPath = filePath;
        entry.remotePath = remoteFileName;
        return entry;
    }

    /*
        dynamic
     */

    public DatabaseMetadataFactory(Context context) {
        this.context = context;
    }

    private final Context context;

    public Context getContext() {
        return this.context;
    }
}
