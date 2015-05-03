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

package com.money.manager.ex.core;

import android.app.backup.BackupAgentHelper;
import android.app.backup.FileBackupHelper;
import android.app.backup.SharedPreferencesBackupHelper;

import com.money.manager.ex.MoneyManagerApplication;

/**
 * Created by Alessandro on 03/02/2015.
 */
public class MoneyManagerBackupAgentHelper extends BackupAgentHelper {
    private static final String KEY_BACKUP_APP_PREFERENCES = "KEY_BACKUP_APP_PREFERENCES";
    private static final String KEY_BACKUP_DROPBOX_PREFERENCES = "KEY_BACKUP_DROPBOX_PREFERENCES";
    private static final String KEY_BACKUP_DB = "KEY_BACKUP_DB";

    @Override
    public void onCreate() {
        super.onCreate();

        // create helper preferences
        SharedPreferencesBackupHelper appHelper = new SharedPreferencesBackupHelper(this, getPackageName() + "_preferences");
        SharedPreferencesBackupHelper dropboxHelper = new SharedPreferencesBackupHelper(this, getPackageName() + "_dropbox_preferences");
        // create helper files
        FileBackupHelper databaseHelper = new FileBackupHelper(this, MoneyManagerApplication.getDatabasePath(getApplicationContext()));

        addHelper(KEY_BACKUP_APP_PREFERENCES, appHelper);
        addHelper(KEY_BACKUP_DROPBOX_PREFERENCES, dropboxHelper);
        addHelper(KEY_BACKUP_DB, databaseHelper);
    }
}
