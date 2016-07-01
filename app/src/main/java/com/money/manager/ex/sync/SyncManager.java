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

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;

import com.cloudrail.si.interfaces.CloudStorage;
import com.cloudrail.si.services.Box;
import com.cloudrail.si.services.Dropbox;
import com.cloudrail.si.services.GoogleDrive;
import com.cloudrail.si.services.OneDrive;
import com.money.manager.ex.R;
import com.money.manager.ex.settings.SettingsActivity;

/**
 * Class used to manage the database synchronization process.
 * Currently forwards the calls to the Dropbox Helper.
 */
public class SyncManager {
    public static void disableAutoUpload() {
        // todo: DropboxHelper.setAutoUploadDisabled(false);
    }

    public static void enableAutoUpload() {
        // todo: DropboxHelper.setAutoUploadDisabled(true);
    }

    public static void dataChanged() {
        // todo: DropboxHelper.notifyDataChanged();
    }

    public static void synchronize() {
        // todo:
//        DropboxManager dropbox = new DropboxManager(mContext, mDropboxHelper);
//        dropbox.synchronizeDropbox();
    }

    public static boolean isFileInSync() {
        // todo: return mDropboxHelper.checkIfFileIsSync();
        return false;
    }

    /**
     * Indicates whether cloud sync is in use. Replaces isLinked() call.
     * @return A boolean
     */
    public static boolean isActive() {
        // todo: check preferences and authentication?
        // mDropboxHelper.isLinked()
        return false;
    }

    public static String getRemotePath() {
        // todo:  mDropboxHelper.getLinkedRemoteFile();
        return "";
    }

    public static void setRemotePath(String filePath) {
        // todo: mDropboxHelper.setLinkedRemoteFile(dropboxPath);
    }

    public static void openDatabase() {
        // todo: replace this method
//        DropboxManager dropbox = new DropboxManager(this, mDropboxHelper);
//        dropbox.openDownloadedDatabase();
    }

    /**
     * Instance methods
     */

    public SyncManager(Context context) {
        mContext = context;

        readConfig();
    }

    private CloudStorage mStorage;
    private Context mContext;

    public Context getContext() {
        return mContext;
    }

    public void login() {
        new Thread() {
            @Override
            public void run() {
                mStorage.login();
            }
        }.start();
    }

    private void readConfig() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        String providerKey = sharedPref.getString(getContext().getString(R.string.pref_sync_provider), "1");

        String packageName = getContext().getApplicationInfo().packageName;

        // Sync provider mapping
        switch (providerKey) {
            case "1":
                // Dropbox
                mStorage = new Dropbox(getContext(), "6328lyguu3wwii6", "oa7k0ju20qss11l");
                break;
            case "2":
                // OneDrive
                mStorage = new OneDrive(getContext(), "", "");
                break;
            case "3":
                // Google Drive
                mStorage = new GoogleDrive(getContext(), "", "");
                break;
            case "4":
                // Box
                mStorage = new Box(getContext(), "", "");
                break;
            default:
                // ?
                break;
        }
    }
}
