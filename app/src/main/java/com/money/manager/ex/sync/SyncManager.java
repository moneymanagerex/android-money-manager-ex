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

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Messenger;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.cloudrail.si.exceptions.AuthenticationException;
import com.cloudrail.si.exceptions.NotFoundException;
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

    /**
     * Performs checks if automatic synchronization should be performed.
     * Used also on immediate upload after file changed.
     * @return boolean indicating if auto sync should be done.
     */
    public boolean canSync() {
        // check if enabled.
        if (!isActive()) return false;

        // should we sync only on wifi?
        if (getPreferences().shouldSyncOnlyOnWifi()) {
            if (BuildConfig.DEBUG) {
                Log.i(this.getClass().getSimpleName(), "Preferences set to sync on WiFi only.");
            }

            // check if we are on WiFi connection.
            NetworkUtilities network = new NetworkUtilities(getContext());
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
            return SyncMessages.FILE_NOT_CHANGED;
        }

        String localPath = MoneyManagerApplication.getDatabasePath(getContext());
        String remotePath = getRemotePath();

        // check if we have the file names.
        if (TextUtils.isEmpty(localPath) || TextUtils.isEmpty(remotePath)) {
            return SyncMessages.FILE_NOT_CHANGED;
        }
        if (!areFileNamesSame(localPath, remotePath)) return SyncMessages.FILE_NOT_CHANGED;

        // get local and remote file info.
        File localFile = new File(localPath);
        CloudMetaData remoteFile = loadMetadata(remotePath) ;
        if (remoteFile == null) {
            return SyncMessages.ERROR;
        }

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

            return SyncMessages.FILE_NOT_CHANGED;
        }

        if (remoteLastModified.isAfter(localLastModified)) {
            return SyncMessages.STARTING_DOWNLOAD;
        } else if (remoteLastModified.isBefore(localLastModified)) {
            return SyncMessages.STARTING_UPLOAD;
        } else {
            return SyncMessages.FILE_NOT_CHANGED;
        }
    }

    public void disableAutoUpload() {
        mAutoUploadDisabled = true;
    }

    /**
     * Downloads the file from the storage service.
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

            // save any renewed tokens
            this.storePersistent();
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
        // save the last modified date so that we can correctly synchronize later.
        String remotePath = getRemotePath();

        // fake metadata
        CloudMetaData metaData = new CloudMetaData();
        metaData.setPath(remotePath);
        metaData.setModifiedAt(DateTime.now().getMillis());

        saveLastModifiedDate(metaData);

        // Should we upload automatically?
        if (mAutoUploadDisabled) return;
        if (!canSync()) {
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
                List<CloudMetaData> items = null;

                try {
                    items = getProvider().getChildren(folder);

                    // save any renewed tokens
                    storePersistent();
                } catch (Exception ex) {
//                    if (ex instanceof RuntimeException && ex.getMessage().equals("ServiceCode Error in function standardJSONRequest at 11")) {
//                        // error fetching remote data. Usually a network problem.
//                    }
                    ExceptionHandler handler = new ExceptionHandler(getContext());
                    handler.handle(ex, "retrieving the remote folder contents");
                }

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
        String dateString = getPreferences().get(file.getPath(), null);
        if (TextUtils.isEmpty(dateString)) return null;

        return new DateTime(dateString);
    }

    public String getRemotePath() {
        if (StringUtils.isEmpty(mRemoteFile)) {
            mRemoteFile = getPreferences().loadPreference(R.string.pref_remote_file, "");
        }
        return mRemoteFile;
    }

    /**
     * Indicates whether synchronization can be performed, meaning all of the criteria must be
     * true: sync enabled, respect wi-fi sync setting, provider is selected, network is online,
     * remote file is set.
     * @return A boolean indicating that sync can be performed.
     */
    public boolean isActive() {
        if (!isSyncEnabled()) return false;

        // network is online.
        NetworkUtilities networkUtilities = new NetworkUtilities(getContext());
        if (!networkUtilities.isOnline()) return false;

        // wifi preferences
        if (getPreferences().shouldSyncOnlyOnWifi()) {
            if (!networkUtilities.isOnWiFi()) return false;
        }

        // Remote file must be set.
        if (StringUtils.isEmpty(getRemotePath())) {
            return false;
        }

        // check if a provider is selected? Default is Dropbox, so no need.

        return true;
    }

    boolean isSyncEnabled() {
        // The sync needs to be enabled.
        return getPreferences().isSyncEnabled();
    }

    CloudMetaData loadMetadata(String remotePath) {
        CloudMetaData result = null;
        try {
            result = getProvider().getMetadata(remotePath);

            // save any renewed tokens
            this.storePersistent();
        } catch (NotFoundException e) {
            // just show a message
            ExceptionHandler handler = new ExceptionHandler(getContext());
            handler.showMessage(R.string.remote_file_not_found);
        } catch (Exception e) {
            ExceptionHandler handler = new ExceptionHandler(getContext());
            handler.handle(e, "fetching remote file info");
        }
        return result;
    }

    public void login() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    getProvider().login();
                } catch (AuthenticationException e) {
                    if (e.getMessage().equals("Authentication was cancelled")) {
                        ExceptionHandler.warn("authentication cancelled");
                    } else {
                        ExceptionHandler handler = new ExceptionHandler(getContext());
                        handler.handle(e, "logging in to cloud provider");
                    }
                } catch (Exception e) {
                    ExceptionHandler handler = new ExceptionHandler(getContext());
                    handler.handle(e, "logging in to cloud provider");
                }
            }
        }).start();
    }

    void logout() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    getProvider().logout();
                } catch (Exception e) {
                    ExceptionHandler handler = new ExceptionHandler(getContext());
                    handler.handle(e, "logging out the cloud provider");
                }
            }
        }).start();
    }

    public void openDatabase() {
        // Do this only if called from an activity.
        if (!(getContext() instanceof Activity)) return;

        File downloadedDb = new File(getLocalPath());
        SyncCommon common = new SyncCommon();

        Intent intent = common.getIntentForOpenDatabase(getContext(), downloadedDb);

        getContext().startActivity(intent);
    }

    /**
     * Resets the synchronization preferences and cache.
     */
    void resetPreferences() {
        getPreferences().clear();

        // reset provider cache
        createProviders();
        storePersistent();
    }

    private void scheduleUpload() {
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
        getPreferences().setSyncEnabled(enabled);
    }

    /**
     * Save the last modified datetime of the remote file into Settings for comparison during
     * the synchronization.
     * @param file file name
     */
    private void saveLastModifiedDate(CloudMetaData file) {
        DateTime date = new DateTime(file.getModifiedAt());

        if (BuildConfig.DEBUG) {
            Log.d(this.getClass().getSimpleName(),
                    "Set remote file: " + file + " last modification date " + date.toString());
        }

        boolean saved = getPreferences().set(file.getPath(), date.toString());

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

    public void setRemotePath(String value) {
        mRemoteFile = value;

        getPreferences().set(R.string.pref_remote_file, value);
    }

    void setSyncInterval(int minutes) {
        getPreferences().setSyncInterval(minutes);
    }

    void startSyncService() {
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
        if (dropbox.get() != null) {
            getPreferences().set(R.string.pref_dropbox_persistent, dropbox.get().saveAsString());
        }
        if (box.get() != null) {
            getPreferences().set(R.string.pref_onedrive_persistent, box.get().saveAsString());
        }
        if (googledrive.get() != null) {
            getPreferences().set(R.string.pref_gdrive_persistent, googledrive.get().saveAsString());
        }
        if (onedrive.get() != null) {
            getPreferences().set(R.string.pref_box_persistent, onedrive.get().saveAsString());
        }
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
            Toast.makeText(getContext(), R.string.dropbox_select_file, Toast.LENGTH_SHORT).show();
            return;
        }

        // easy comparison, just by the file name.
        if (!areFileNamesSame(localPath, remotePath)) {
            // The current file was probably opened through Open Database.
            Toast.makeText(getContext(), R.string.db_not_dropbox, Toast.LENGTH_LONG).show();
            return;
        }

        invokeSyncService(SyncConstants.INTENT_ACTION_SYNC);
    }

    void triggerDownload() {
        invokeSyncService(SyncConstants.INTENT_ACTION_DOWNLOAD);
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

        try {
            getProvider().upload(remoteFile, input, localFile.length(), true);
        } catch (Exception e) {
            ExceptionHandler handler = new ExceptionHandler(getContext());
            handler.handle(e, "uploading database file");
            return false;
        }

        try {
            input.close();
        } catch (IOException e) {
            ExceptionHandler handler = new ExceptionHandler(getContext());
            handler.handle(e, "closing input stream after upload");
        }

        // set last modified date
        try {
            CloudMetaData remoteFileMetadata = getProvider().getMetadata(remoteFile);
            saveLastModifiedDate(remoteFileMetadata);
        } catch (Exception e) {
            ExceptionHandler handler = new ExceptionHandler(getContext());
            handler.handle(e, "closing input stream after upload");
        }

        // set remote file, if not set (setLinkedRemoteFile)
        if (TextUtils.isEmpty(getRemotePath())) {
            setRemotePath(remoteFile);
        }

        // save any renewed tokens
        this.storePersistent();

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

    private void createProviders() {
        try {
            dropbox.set(new Dropbox(getContext(), "6328lyguu3wwii6", "oa7k0ju20qss11l"));
            onedrive.set(new OneDrive(getContext(), "b76e0230-4f4e-4bff-9976-fd660cdebc4a", "fmAOPrAuq6a5hXzY1v7qcDn"));
            googledrive.set(new GoogleDrive(getContext(), "843259487958-p65svijbdvj1knh5ove1ksp0hlnufli8.apps.googleusercontent.com", "cpU0rnBiMW9lQaYfaoW1dwLU"));
            box.set(new Box(getContext(), "95f7air3i2ed19r28hi31vwtta4wgz1p", "i6j0NLd3G6Ui9FpZyuQfiLK8jLs4YZRM"));
        } catch (Exception e) {
            ExceptionHandler handler = new ExceptionHandler(getContext());
            handler.handle(e, "creating cloud providers");
        }
    }

    private SyncPreferences getPreferences() {
        if (mPreferences == null) {
            mPreferences = new SyncPreferences(getContext());
        }
        return mPreferences;
    }

    private void init() {
        // Do not initialize providers if the network is not present.
        NetworkUtilities network = new NetworkUtilities(getContext());
        if (!network.isOnline()) return;

        createProviders();
        restoreProviderCache();

        // Use current provider.
        String providerCode = getPreferences().loadPreference(R.string.pref_sync_provider, CloudStorageProviderEnum.DROPBOX.name());
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

    private CloudStorage getProvider() {
        return currentProvider.get();
    }

    private void invokeSyncService(String action) {
        try {
            invokeSyncServiceInternal(action);
        } catch (Exception e) {
            ExceptionHandler handler = new ExceptionHandler(getContext());
            handler.handle(e, "invoking sync service");
        }
    }

    private void invokeSyncServiceInternal(String action) {
        // Validation.
        String remoteFile = getRemotePath();
        // We need a value in remote file name settings.
        if (TextUtils.isEmpty(remoteFile)) return;

        // Action

        Messenger messenger = createMessenger();
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

    private Messenger createMessenger() {
        ProgressDialog progressDialog = null;
        // Create progress dialog only if called from the UI.
        if (getContext() instanceof Activity) {
            try {
                //progress dialog shown only when downloading an updated db file.
                progressDialog = new ProgressDialog(getContext());
                progressDialog.setCancelable(false);
                progressDialog.setMessage(getContext().getString(R.string.syncProgress));
                progressDialog.setIndeterminate(true);
                progressDialog.show();
            } catch (Exception ex) {
                ExceptionHandler handler = new ExceptionHandler(getContext(), this);
                handler.handle(ex, "displaying sync progress dialog");
            }
        }

        Messenger messenger = new SyncMessengerFactory(getContext())
                .createMessenger(progressDialog, getRemotePath());

        return messenger;
    }

    private void restoreProviderCache() {
        try {
            String persistent = getPreferences().loadPreference(R.string.pref_dropbox_persistent, null);
            if (persistent != null) dropbox.get().loadAsString(persistent);

            persistent = getPreferences().loadPreference(R.string.pref_box_persistent, null);
            if (persistent != null) box.get().loadAsString(persistent);

            persistent = getPreferences().loadPreference(R.string.pref_gdrive_persistent, null);
            if (persistent != null) googledrive.get().loadAsString(persistent);

            persistent = getPreferences().loadPreference(R.string.pref_onedrive_persistent, null);
            if (persistent != null) onedrive.get().loadAsString(persistent);
        } catch (Exception e) {
            if (e instanceof ParseException) {
                if (BuildConfig.DEBUG) Log.w("cloud persistence", e.getMessage());
            } else {
                ExceptionHandler handler = new ExceptionHandler(getContext());
                handler.handle(e, "restoring providers from cache");
            }
        }
    }
}
