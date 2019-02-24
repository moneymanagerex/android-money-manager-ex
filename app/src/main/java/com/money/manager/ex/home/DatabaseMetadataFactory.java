/*
 * Copyright (C) 2012-2019 The Android Money Manager Ex Project Team
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

import com.money.manager.ex.MmexApplication;
import com.money.manager.ex.R;
import com.money.manager.ex.core.database.DatabaseManager;
import com.money.manager.ex.settings.AppSettings;
import com.money.manager.ex.settings.SyncPreferences;
import com.money.manager.ex.sync.SyncManager;
import com.money.manager.ex.utils.MmxDate;

import androidx.annotation.NonNull;

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

    private Context context;

    public Context getContext() {
        return this.context;
    }

    /**
     * Creates a database entry from the current preferences. Used for transition from preferences
     * to Database metadata records.
     * @return A database record that represents the current preferences (local/remote db paths).
     */
    public DatabaseMetadata createDefaultEntry() {
        DatabaseMetadata entry = new DatabaseMetadata();

        // todo remove the local change preference after upgrade.
        entry.localPath = new DatabaseManager(getContext()).getDatabasePath();
        entry.isLocalFileChanged = new AppSettings(getContext()).get(R.string.pref_is_local_file_changed, false);

        SyncManager syncManager = new SyncManager(getContext());
        // todo remove the remote file preference after upgrade
        entry.remotePath = new SyncPreferences(getContext()).loadPreference(R.string.pref_remote_file, "");
        MmxDate cachedRemoteChangeDate = syncManager.getRemoteLastModifiedDatePreferenceFor(entry.remotePath);
        entry.setRemoteLastChangedDate(cachedRemoteChangeDate);

        return entry;
    }
}
