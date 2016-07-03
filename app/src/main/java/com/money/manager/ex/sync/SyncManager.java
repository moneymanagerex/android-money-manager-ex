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
import android.util.Log;

import com.cloudrail.si.exceptions.ParseException;
import com.cloudrail.si.interfaces.CloudStorage;
import com.cloudrail.si.services.Box;
import com.cloudrail.si.services.Dropbox;
import com.cloudrail.si.services.GoogleDrive;
import com.cloudrail.si.services.OneDrive;
import com.cloudrail.si.types.CloudMetaData;
import com.money.manager.ex.BuildConfig;
import com.money.manager.ex.R;
import com.money.manager.ex.settings.AppSettings;
import com.money.manager.ex.settings.PreferenceConstants;
import com.money.manager.ex.settings.SyncPreferences;
import com.money.manager.ex.sync.events.RemoteFolderContentsRetrievedEvent;

import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

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

        init();
    }

    private final AtomicReference<CloudStorage> dropbox = new AtomicReference<>();
    private final AtomicReference<CloudStorage> box = new AtomicReference<>();
    private final AtomicReference<CloudStorage> googledrive = new AtomicReference<>();
    private final AtomicReference<CloudStorage> onedrive = new AtomicReference<>();

    private Context mContext;
    private String mRemoteFile;
    private AtomicReference<CloudStorage> currentProvider;
    private SyncPreferences mPreferences;

    public Context getContext() {
        return mContext;
    }

    public CloudStorage getProvider() {
//        AtomicReference<CloudStorage> result = new AtomicReference<>();
        return currentProvider.get();
    }

    public void download() {
        // todo: We need a value in the remote file name settings.

    }

    /**
     * Indicates whether cloud sync is in use.
     * @return A boolean
     */
    public boolean isActive() {
        // check preferences and authentication?
        SyncPreferences preferences = new SyncPreferences(getContext());
        boolean isEnabled = preferences.get(R.string.pref_sync_enabled, false);

        // check if a provider is selected?

        return isEnabled;
    }

    public void login() {
        new Thread() {
            @Override
            public void run() {
                getProvider().login();
            }
        }.start();
    }

    public void logout() {
        new Thread() {
            @Override
            public void run() {
                getProvider().logout();
            }
        }.start();
    }

    public void getFolderContentsAsync(final String folder) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<CloudMetaData> items = getProvider().getChildren(folder);
                EventBus.getDefault().post(new RemoteFolderContentsRetrievedEvent(items));
            }
        }).start();
    }

    public String getRemoteFile() {
        if (StringUtils.isEmpty(mRemoteFile)) {
            mRemoteFile = mPreferences.loadPreference(R.string.pref_remote_file, "");
        }
        // todo:  mDropboxHelper.getLinkedRemoteFile();

        return mRemoteFile;
    }

    public void resetPreferences() {
        mPreferences.clear();
    }

    public void setProvider(CloudStorageProviderEnum provider) {
        // Sync provider mapping
        switch (provider) {
            case DROPBOX:
                currentProvider = dropbox;
                break;
            case ONEDRIVE:
                // OneDrive
                currentProvider = onedrive;
                break;
            case GOOGLEDRIVE:
                // Google Drive
                currentProvider = googledrive;
                break;
            case BOX:
                // Box
                currentProvider = box;
                break;
            default:
                // default provider
                currentProvider = dropbox;
                break;
        }

    }

    public void setRemoteFile(String value) {
        mRemoteFile = value;

        mPreferences.savePreference(R.string.pref_remote_file, value);

        // todo: mDropboxHelper.setLinkedRemoteFile(dropboxPath);
    }

    public void storePersistent() {
        mPreferences.savePreference(R.string.pref_dropbox_persistent, dropbox.get().saveAsString());
        mPreferences.savePreference(R.string.pref_onedrive_persistent, box.get().saveAsString());
        mPreferences.savePreference(R.string.pref_gdrive_persistent, googledrive.get().saveAsString());
        mPreferences.savePreference(R.string.pref_box_persistent, onedrive.get().saveAsString());
    }

    // private

    private void init() {
        mPreferences = new SyncPreferences(getContext());

        dropbox.set(new Dropbox(getContext(), "6328lyguu3wwii6", "oa7k0ju20qss11l"));
        onedrive.set(new OneDrive(getContext(), "b76e0230-4f4e-4bff-9976-fd660cdebc4a", "fmAOPrAuq6a5hXzY1v7qcDn"));
        googledrive.set(new GoogleDrive(getContext(), "843259487958-p65svijbdvj1knh5ove1ksp0hlnufli8.apps.googleusercontent.com", "cpU0rnBiMW9lQaYfaoW1dwLU"));
        box.set(new Box(getContext(), "95f7air3i2ed19r28hi31vwtta4wgz1p", "i6j0NLd3G6Ui9FpZyuQfiLK8jLs4YZRM"));

        // read from persistence
        try {
            String persistent = mPreferences.loadPreference(R.string.pref_dropbox_persistent, null);
            if (persistent != null) dropbox.get().loadAsString(persistent);

            persistent = mPreferences.loadPreference(R.string.pref_box_persistent, null);
            if (persistent != null) box.get().loadAsString(persistent);

            persistent = mPreferences.loadPreference(R.string.pref_gdrive_persistent, null);
            if (persistent != null) googledrive.get().loadAsString(persistent);

            persistent = mPreferences.loadPreference(R.string.pref_onedrive_persistent, null);
            if (persistent != null) onedrive.get().loadAsString(persistent);
        } catch (ParseException e) {
            if (BuildConfig.DEBUG) Log.w("cloud persistence", e.getMessage());
        }

        // Use current provider.
        String providerCode = mPreferences.loadPreference(R.string.pref_sync_provider, CloudStorageProviderEnum.DROPBOX.name());
        CloudStorageProviderEnum provider = CloudStorageProviderEnum.DROPBOX;
        if (CloudStorageProviderEnum.contains(providerCode)) {
            provider = CloudStorageProviderEnum.valueOf(providerCode);
        }
        setProvider(provider);
    }
}
