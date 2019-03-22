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

package com.money.manager.ex.core;

import android.app.backup.BackupAgentHelper;
import android.app.backup.FileBackupHelper;
import android.app.backup.SharedPreferencesBackupHelper;

import com.money.manager.ex.MmexApplication;
import com.money.manager.ex.core.database.DatabaseManager;

/**
 * Manage preferences backup.
 */
public class MoneyManagerBackupAgentHelper
    extends BackupAgentHelper {

    private static final String KEY_BACKUP_APP_PREFERENCES = "KEY_BACKUP_APP_PREFERENCES";
    private static final String KEY_BACKUP_DROPBOX_PREFERENCES = "KEY_BACKUP_DROPBOX_PREFERENCES";
//    private static final String KEY_BACKUP_RECENT_DB_PREFERENCES = "KEY_BACKUP_RECENT_DB_PREFERENCES";
    private static final String KEY_BACKUP_DB = "KEY_BACKUP_DB";

    @Override
    public void onCreate() {
        super.onCreate();

        // create helper preferences
        SharedPreferencesBackupHelper appHelper = new SharedPreferencesBackupHelper(this, getPackageName() + "_preferences");
        //SharedPreferencesBackupHelper dropboxHelper = new SharedPreferencesBackupHelper(this, getPackageName() + "_dropbox_preferences");
        // create helper files
        FileBackupHelper databaseHelper = new FileBackupHelper(this,
                new DatabaseManager(getApplicationContext()).getDatabasePath());

        addHelper(KEY_BACKUP_APP_PREFERENCES, appHelper);
        //addHelper(KEY_BACKUP_DROPBOX_PREFERENCES, dropboxHelper);
        // todo: addHelper(KEY_BACKUP_RECENT_DB_PREFERENCES, dr);
        addHelper(KEY_BACKUP_DB, databaseHelper);
    }
}
