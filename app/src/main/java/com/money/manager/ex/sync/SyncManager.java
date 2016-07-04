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

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Messenger;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.cloudrail.si.exceptions.ParseException;
import com.cloudrail.si.interfaces.CloudStorage;
import com.cloudrail.si.services.Box;
import com.cloudrail.si.services.Dropbox;
import com.cloudrail.si.services.GoogleDrive;
import com.cloudrail.si.services.OneDrive;
import com.cloudrail.si.types.CloudMetaData;
import com.money.manager.ex.BuildConfig;
import com.money.manager.ex.MoneyManagerApplication;
import com.money.manager.ex.R;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.core.ExceptionHandler;
import com.money.manager.ex.dropbox.SyncCommon;
import com.money.manager.ex.dropbox.SyncMessengerFactory;
import com.money.manager.ex.dropbox.SyncSchedulerBroadcastReceiver;
import com.money.manager.ex.settings.SyncPreferences;
import com.money.manager.ex.sync.events.RemoteFolderContentsRetrievedEvent;
import com.money.manager.ex.utils.NetworkUtilities;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;
import org.joda.time.DateTime;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Class used to manage the database synchronization process.
 * Currently forwards the calls to the Dropbox Helper.
 */
public class SyncManager {

    // Delayed synchronization
    private static Handler mDelayedHandler = null;
    private static Runnable mRunSyncRunnable = null;

    // Instance methods

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
    /**
     * Used to temporarily disable auto-upload while performing batch updates.
     */
    private boolean mAutoUploadDisabled = false;

    public void abortScheduledUpload() {
        if (mDelayedHandler != null) {
            mDelayedHandler.removeCallbacks(mRunSyncRunnable);
        }
    }

    public Context getContext() {
        return mContext;
    }

    public CloudStorage getProvider() {
//        AtomicReference<CloudStorage> result = new AtomicReference<>();
        return currentProvider.get();
    }

    /**
     * Performs checks if automatic synchronization should be performed.
     * Used also on immediate upload after file changed.
     * @return boolean indicating if auto sync should be done.
     */
    public boolean canAutoSync() {
        // check if enabled.
        if (!isActive()) return false;

        // should we sync only on wifi?
        if (mPreferences.shouldSyncOnlyOnWifi()) {
            if (BuildConfig.DEBUG) {
                Log.i(this.getClass().getSimpleName(), "Preferences set to sync on WiFi only.");
            }

            // check if we are on WiFi connection.
            NetworkUtilities network = new NetworkUtilities(mContext);
            if (!network.isOnWiFi()) {
                Log.i(this.getClass().getSimpleName(), "Not on WiFi connection. Not synchronizing.");
                return false;
            }
        }

        return true;
    }

    /**
     * This function returns if the file is synchronized or not
     * @return int
     */
    public int compareFilesForSync() {
        if (!isActive()) {
            return SyncService.INTENT_EXTRA_MESSENGER_NOT_CHANGE;
        }

        String localPath = MoneyManagerApplication.getDatabasePath(getContext());
        String remotePath = getRemotePath();

        // check if we have the file names.
        if (TextUtils.isEmpty(localPath) || TextUtils.isEmpty(remotePath)) {
            return SyncService.INTENT_EXTRA_MESSENGER_NOT_CHANGE;
        }
        if (!areFileNamesSame(localPath, remotePath)) return SyncService.INTENT_EXTRA_MESSENGER_NOT_CHANGE;

        // get local and remote file info.
        File localFile = new File(localPath);
        CloudMetaData remoteFile = getProvider().getMetadata(remotePath);

        // date last Modified
        DateTime localLastModified;
        DateTime remoteLastModified;
        try {
            localLastModified = getLastModifiedDate(remoteFile);
            if (localLastModified == null) {
                localLastModified = new DateTime(localFile.lastModified());
            }
            remoteLastModified = new DateTime(remoteFile.getModifiedAt());
        } catch (Exception e) {
            ExceptionHandler handler = new ExceptionHandler(getContext(), this);
            handler.handle(e, "retrieving the last modified date in compareFilesForSync");

            return SyncService.INTENT_EXTRA_MESSENGER_NOT_CHANGE;
        }

        if (remoteLastModified.isAfter(localLastModified)) {
            return SyncService.INTENT_EXTRA_MESSENGER_DOWNLOAD;
        } else if (remoteLastModified.isBefore(localLastModified)) {
            return SyncService.INTENT_EXTRA_MESSENGER_UPLOAD;
        } else {
            return SyncService.INTENT_EXTRA_MESSENGER_NOT_CHANGE;
        }
    }

    public void disableAutoUpload() {
        mAutoUploadDisabled = true;
    }

    /**
     * Downloads the file from Dropbox service.
     * @param remoteFile Remote file entry
     * @param localFile Local file reference
     * @return Indicator whether the download was successful.
     */
    public boolean download(CloudMetaData remoteFile, File localFile) {
        try {
            InputStream inputStream = getProvider().download(remoteFile.getPath());
            OutputStream outputStream = new FileOutputStream(localFile, false);

            IOUtils.copy(inputStream, outputStream);

            inputStream.close();
            outputStream.close();
        } catch (Exception e) {
            ExceptionHandler handler = new ExceptionHandler(mContext, this);
            handler.handle(e, "downloading from the cloud");
            return false;
        }

        saveLastModifiedDate(remoteFile);

        abortScheduledUpload();

        return true;
    }

    /**
     * Called whenever the database has changed and should be uploaded.
     * (Re-)Sets the timer for delayed sync of the database.
     */
    public void dataChanged() {
        if (!isActive()) return;

        // save the last modified date so that we can correctly synchronize later.
        String remotePath = getRemotePath();

        // fake metadata
        CloudMetaData metaData = new CloudMetaData();
        metaData.setPath(remotePath);
        metaData.setModifiedAt(DateTime.now().getMillis());

        saveLastModifiedDate(metaData);

        // Should we upload automatically?
        if (mAutoUploadDisabled) return;
        if (!canAutoSync()) {
            Log.i(this.getClass().getSimpleName(), "Not on WiFi connection. Not synchronizing.");
            return;
        }

        // Should we schedule an upload?
        SyncPreferences preferences = new SyncPreferences(getContext());
        if (preferences.getUploadImmediately()) {
            abortScheduledUpload();
            scheduleUpload();
        }
    }

    public void enableAutoUpload() {
        mAutoUploadDisabled = false;
    }

    /**
     *
     * @return The path of the local cached copy of the remote database.
     */
    public String getLocalPath() {
        String remoteFile = getRemotePath();
        // now get only the file name
        String remoteFileName = new File(remoteFile).getName();

        String localPath = getExternalStorageDirectoryForSync().getPath();
        if (!localPath.endsWith(File.separator)) {
            localPath += File.separator;
        }
        return localPath + remoteFileName;
    }

    public void getRemoteFolderContentsAsync(final String folder) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<CloudMetaData> items = getProvider().getChildren(folder);
                EventBus.getDefault().post(new RemoteFolderContentsRetrievedEvent(items));
            }
        }).start();
    }

    /**
     * Gets last modified datetime of the database file from the preferences.
     * @param file file name
     * @return date of last modification
     */
    public DateTime getLastModifiedDate(CloudMetaData file) {
        String dateString = mPreferences.get(file.getPath(), null);
        if (TextUtils.isEmpty(dateString)) return null;

        return new DateTime(dateString);
    }

    public String getRemotePath() {
        if (StringUtils.isEmpty(mRemoteFile)) {
            mRemoteFile = mPreferences.loadPreference(R.string.pref_remote_file, "");
        }
        return mRemoteFile;
    }

    /**
     * Indicates whether cloud sync is in use.
     * @return A boolean
     */
    public boolean isActive() {
        // check preferences and authentication?
        return mPreferences.isSyncEnabled();

        // check if a provider is selected?
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

    public void openDatabase() {
        File downloadedDb = new File(getLocalPath());
        SyncCommon common = new SyncCommon();

        Intent intent = common.getIntentForOpenDatabase(getContext(), downloadedDb);

        getContext().startActivity(intent);
    }

    public void resetPreferences() {
        mPreferences.clear();
    }

    public void scheduleUpload() {
        // Create task/runnable for synchronization.
        if (mRunSyncRunnable == null) {
            mRunSyncRunnable = new Runnable() {
                @Override
                public void run() {
                    if (BuildConfig.DEBUG) {
                        Log.d("SyncManager", "Starting delayed upload");
                    }

                    invokeSyncService(SyncConstants.INTENT_ACTION_UPLOAD);
                }
            };
        }

        // Schedule delayed execution of the sync task.
        if (BuildConfig.DEBUG) Log.d(this.getClass().getSimpleName(), "Scheduling delayed upload to the cloud storage.");

        mDelayedHandler = new Handler();

        // Synchronize after 30 seconds.
        mDelayedHandler.postDelayed(mRunSyncRunnable, 30 * 1000);
    }

    public void setEnabled(boolean enabled) {
        mPreferences.setSyncEnabled(enabled);
    }

    /**
     * Save the last modified datetime of the remote file into Settings for comparison during
     * the synchronization.
     * @param file file name
     */
    public void saveLastModifiedDate(CloudMetaData file) {
        DateTime date = new DateTime(file.getModifiedAt());

        if (BuildConfig.DEBUG) {
            Log.d(this.getClass().getSimpleName(),
                    "Set remote file: " + file + " last modification date " + date.toString());
        }

        boolean saved = mPreferences.set(file.getPath(), date.toString());

        if (!saved) {
            Log.e(this.getClass().getSimpleName(), "Could not store last modified date!");
        }
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

        mPreferences.set(R.string.pref_remote_file, value);
    }

    public void setSyncInterval(int minutes) {
        mPreferences.setSyncInterval(minutes);
    }

    public void startSyncService() {
        Intent intent = new Intent(getContext(), SyncSchedulerBroadcastReceiver.class);
        intent.setAction(SyncSchedulerBroadcastReceiver.ACTION_START);
        getContext().sendBroadcast(intent);
    }

    public void stopSyncService() {
        Intent intent = new Intent(mContext, SyncSchedulerBroadcastReceiver.class);
        intent.setAction(SyncSchedulerBroadcastReceiver.ACTION_STOP);
        getContext().sendBroadcast(intent);
    }

    public void storePersistent() {
        mPreferences.set(R.string.pref_dropbox_persistent, dropbox.get().saveAsString());
        mPreferences.set(R.string.pref_onedrive_persistent, box.get().saveAsString());
        mPreferences.set(R.string.pref_gdrive_persistent, googledrive.get().saveAsString());
        mPreferences.set(R.string.pref_box_persistent, onedrive.get().saveAsString());
    }

    public void triggerSynchronization() {
        if (!isActive())  return;

        // Make sure that the current database is also the one linked in the cloud.
        String localPath = MoneyManagerApplication.getDatabasePath(getContext());
        if (TextUtils.isEmpty(localPath)) {
            return;
        }

        String remotePath = getRemotePath();
        if (TextUtils.isEmpty(remotePath)) {
            Toast.makeText(mContext, R.string.dropbox_select_file, Toast.LENGTH_SHORT).show();
            return;
        }

        // easy comparison, just by the file name.
        if (!areFileNamesSame(localPath, remotePath)) {
            // The current file was probably opened through Open Database.
            Toast.makeText(mContext, R.string.db_not_dropbox, Toast.LENGTH_LONG).show();
            return;
        }

        invokeSyncService(SyncConstants.INTENT_ACTION_SYNC);
    }

    public void triggerDownload() {
        ProgressDialog progressDialog = null;
        try {
            //progress dialog shown only when downloading an updated db file.
            progressDialog = new ProgressDialog(getContext());
            progressDialog.setCancelable(false);
            progressDialog.setMessage(getContext().getString(R.string.syncProgress));
            progressDialog.setIndeterminate(true);
            progressDialog.show();
        } catch (Exception ex) {
            ExceptionHandler handler = new ExceptionHandler(getContext(), this);
            handler.handle(ex, "displaying download progress dialog");
        }

        Messenger messenger = new SyncMessengerFactory(getContext())
                .createMessenger(progressDialog, getRemotePath());

        invokeSyncService(SyncConstants.INTENT_ACTION_DOWNLOAD, messenger);
    }

    /**
     * Upload the file to cloud storage.
     * @param localPath The path to the file to upload.
     * @param remoteFile The remote path.
     */
    public boolean upload(String localPath, String remoteFile) {
        File localFile = new File(localPath);
        if (!localFile.exists()) return false;

        FileInputStream input;
        try {
            input = new FileInputStream(localFile);
        } catch (FileNotFoundException e) {
            ExceptionHandler handler = new ExceptionHandler(getContext());
            handler.handle(e, "opening local file for upload");
            return false;
        }

        getProvider().upload(remoteFile, input, localFile.length(), true);

        try {
            input.close();
        } catch (IOException e) {
            ExceptionHandler handler = new ExceptionHandler(getContext());
            handler.handle(e, "closing input stream after upload");
        }

        return true;
    }

    // private

    /**
     * Compares the local and remote db filenames. Use for safety check before synchronization.
     * @return A boolean indicating if the filenames are the same.
     */
    private boolean areFileNamesSame(String localPath, String remotePath) {
        if (StringUtils.isEmpty(localPath)) return false;
        if (StringUtils.isEmpty(remotePath)) return false;

        File localFile = new File(localPath);
        String localName = localFile.getName();

        File remoteFile = new File(remotePath);
        String remoteName = remoteFile.getName();

        return localName.equalsIgnoreCase(remoteName);
    }

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

    private File getExternalStorageDirectoryForSync() {
        Core core = new Core(mContext.getApplicationContext());
        File folder = core.getExternalStorageDirectory();
        // manage folder
        if (folder != null && folder.exists() && folder.isDirectory() && folder.canWrite()) {
            // create a folder for remote files
            File folderSync = new File(folder + "/sync");
            // check if folder exists otherwise create
            if (!folderSync.exists()) {
                if (!folderSync.mkdirs()) return mContext.getFilesDir();
            }
            return folderSync;
        } else {
            return mContext.getFilesDir();
        }
    }

    private void invokeSyncService(String action, Messenger messenger) {
        // Validation.
        String remoteFile = getRemotePath();
        // We need a value in remote file name settings.
        if (TextUtils.isEmpty(remoteFile)) return;

        // Action

        String localFile = getLocalPath();

        Intent service = new Intent(getContext(), SyncService.class);

        service.setAction(action);

        service.putExtra(SyncConstants.INTENT_EXTRA_LOCAL_FILE, localFile);
        service.putExtra(SyncConstants.INTENT_EXTRA_REMOTE_FILE, remoteFile);

        if (messenger != null) {
            service.putExtra(SyncService.INTENT_EXTRA_MESSENGER, messenger);
        }

        // start service
        getContext().startService(service);

        // Reset any other scheduled uploads as the current operation will modify the files.
        abortScheduledUpload();

        // once done, the message is sent out via messenger. See Messenger definition in factory.
    }

    private void invokeSyncService(String action) {
        invokeSyncService(action, null);
    }
}
