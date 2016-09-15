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
package com.money.manager.ex.home;

import android.content.Context;
import android.support.annotation.NonNull;

import com.money.manager.ex.MoneyManagerApplication;
import com.money.manager.ex.R;
import com.money.manager.ex.settings.AppSettings;
import com.money.manager.ex.settings.SyncPreferences;
import com.money.manager.ex.sync.SyncManager;

/**
 * Factory for the database metadata records.
 */

public class DatabaseMetadataFactory {

    public static RecentDatabaseEntry getInstance(String localPath) {
        RecentDatabaseEntry db = new RecentDatabaseEntry();
        db.localPath = localPath;
        db.remotePath = "";
        return db;
    }

    public static RecentDatabaseEntry getInstance(String filePath, @NonNull String remoteFileName) {
        RecentDatabaseEntry entry = new RecentDatabaseEntry();
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
     * @return A database record that represents the current settings (local/remote db paths).
     */
    public RecentDatabaseEntry createDefaultEntry() {
        RecentDatabaseEntry entry = new RecentDatabaseEntry();

        entry.localPath = MoneyManagerApplication.getDatabasePath(getContext());
        // todo remove the local change preference after upgrade.
        entry.isLocalFileChanged = new AppSettings(getContext()).get(R.string.pref_is_local_file_changed, false);

        SyncManager syncManager = new SyncManager(getContext());
        //entry.remotePath = syncManager.getRemotePath();
        // todo remove the remote file preference after upgrade
        entry.remotePath = new SyncPreferences(getContext()).loadPreference(R.string.pref_remote_file, "");
        entry.remoteLastChangedOn = syncManager.getCachedLastModifiedDateFor(entry.remotePath);

        return entry;
    }

}
