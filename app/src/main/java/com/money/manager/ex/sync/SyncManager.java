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
import com.cloudrail.si.types.CloudMetaData;
import com.money.manager.ex.R;
import com.money.manager.ex.settings.AppSettings;
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
//    private static SyncManager ourInstance = new SyncManager();

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

        mSettings = new AppSettings(getContext());

        readConfig(mSettings);
    }

    private final AtomicReference<CloudStorage> dropbox = new AtomicReference<>();
    private final AtomicReference<CloudStorage> box = new AtomicReference<>();
    private final AtomicReference<CloudStorage> googledrive = new AtomicReference<>();
    private final AtomicReference<CloudStorage> onedrive = new AtomicReference<>();

    private AppSettings mSettings;
    private CloudStorage mStorage;
    private Context mContext;
    private String mRemoteFile;

    public Context getContext() {
        return mContext;
    }

    public String getRemotePath() {
        // todo:  mDropboxHelper.getLinkedRemoteFile();
        return "";
    }

    public void login() {
        new Thread() {
            @Override
            public void run() {
                mStorage.login();
            }
        }.start();
    }

//    public List<CloudMetaData> getFolderContents(final String folder) {
//        List<CloudMetaData> items = null;
//
//        ExecutorService executor = Executors.newSingleThreadExecutor();
//        Callable<List<CloudMetaData>> callable = new Callable<List<CloudMetaData>>() {
//            @Override
//            public List<CloudMetaData> call() {
//                return mStorage.getChildren(folder);
//            }
//        };
//        Future<List<CloudMetaData>> future = executor.submit(callable);
//
//        try {
//            // future.get() returns 2 or raises an exception if the thread dies, so safer
//            items = future.get();
//        } catch (Exception ex) {
//            ExceptionHandler handler = new ExceptionHandler(getContext(), this);
//            handler.handle(ex, "fetching remote contents");
//        }
//
//        executor.shutdown();
//
//        return items;
//    }

    public void getFolderContentsAsync(final String folder) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<CloudMetaData> items = mStorage.getChildren(folder);
                EventBus.getDefault().post(new RemoteFolderContentsRetrievedEvent(items));
            }
        }).start();
    }

    public String getRemoteFile() {
        if (StringUtils.isEmpty(mRemoteFile)) {
            mRemoteFile = mSettings.get(R.string.pref_remote_file, "");
        }

        return mRemoteFile;
    }

    // private

    private void readConfig(AppSettings settings) {
        String provider = settings.get(R.string.pref_sync_provider, "1");

//        String packageName = getContext().getApplicationInfo().packageName;

        // todo: read from persistence

        // Sync provider mapping
        switch (provider) {
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

        //todo save credentials
    }

    public void setRemoteFile(String value) {
        mRemoteFile = value;

        mSettings.set(R.string.pref_remote_file, value);

        // todo: mDropboxHelper.setLinkedRemoteFile(dropboxPath);
    }

    public void storePersistent() {
//        SharedPreferences sharedPreferences = getContext().getP.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("dropboxPersistent", dropbox.get().saveAsString());
        editor.putString("boxPersistent", box.get().saveAsString());
        editor.putString("googledrivePersistent", googledrive.get().saveAsString());
        editor.putString("onedrivePersistent", onedrive.get().saveAsString());

//        editor.commit();
        editor.apply();
    }
}
