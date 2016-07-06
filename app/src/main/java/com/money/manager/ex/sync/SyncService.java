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

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Message;
import android.os.Messenger;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.cloudrail.si.types.CloudMetaData;
import com.money.manager.ex.BuildConfig;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.core.ExceptionHandler;
import com.money.manager.ex.dropbox.IOnDownloadUploadEntry;
import com.money.manager.ex.home.MainActivity;
import com.money.manager.ex.utils.NetworkUtilities;

import org.joda.time.DateTime;

import java.io.File;
import java.io.IOException;

/**
 * The background service that synchronizes the database file.
 * It is being invoked by the timer.
 * It displays the sync notification and invokes the cloud api.
 */
public class SyncService
    extends IntentService {

    public static final String INTENT_EXTRA_MESSENGER = "com.money.manager.ex.sync.MESSENGER";
    private static final String LOGCAT = SyncService.class.getSimpleName();

    public SyncService() {
        super("com.money.manager.ex.sync.SyncService");
    }

    private Messenger mOutMessenger;

    @Override
    protected void onHandleIntent(Intent intent) {
        if (BuildConfig.DEBUG) Log.d(LOGCAT, intent.toString());

        // Check if there is a messenger. Used to send the messages back.
        if (intent.getExtras().containsKey(SyncService.INTENT_EXTRA_MESSENGER)) {
            mOutMessenger = intent.getParcelableExtra(SyncService.INTENT_EXTRA_MESSENGER);
        }

        // check if the device is online.
        NetworkUtilities network = new NetworkUtilities(getApplicationContext());
        if (!network.isOnline()) {
            if (BuildConfig.DEBUG) Log.i(LOGCAT, "Can't sync. Device not online.");
            sendMessage(SyncMessages.NOT_ON_WIFI);
            return;
        }

        SyncManager sync = new SyncManager(getBaseContext());

        String localFilename = intent.getStringExtra(SyncConstants.INTENT_EXTRA_LOCAL_FILE);
        String remoteFilename = intent.getStringExtra(SyncConstants.INTENT_EXTRA_REMOTE_FILE);
        // check if file is correct
        if (TextUtils.isEmpty(localFilename) || TextUtils.isEmpty(remoteFilename)) return;

        File localFile = new File(localFilename);
        CloudMetaData remoteFile;
        try {
            remoteFile = sync.getProvider().getMetadata(remoteFilename);
        } catch (Exception e) {
            ExceptionHandler handler = new ExceptionHandler(getBaseContext());
            handler.handle(e, "fetching remote metadata");
            sendMessage(SyncMessages.ERROR);
            return;
        }

        if (remoteFile == null) {
            // file not found on remote server.
            if (intent.getAction().equals(SyncConstants.INTENT_ACTION_UPLOAD)) {
                // Create a new entry in the root?
                Log.w(LOGCAT, "remoteFile is null. SyncService forcing creation of the new remote file.");
                remoteFile = new CloudMetaData();
                remoteFile.setPath(remoteFilename);
            } else {
                Log.e(LOGCAT, "remoteFile is null. SyncService.onHandleIntent premature exit.");
                sendMessage(SyncMessages.ERROR);
                return;
            }
        }

        // check if name is same
        if (!localFile.getName().toLowerCase().equals(remoteFile.getName().toLowerCase())) {
            Log.w(LOGCAT, "Local filename different from the remote!");
            sendMessage(SyncMessages.ERROR);
            return;
        }

        // Execute action.
        String action = intent.getAction();
        switch (action) {
            case SyncConstants.INTENT_ACTION_DOWNLOAD:
                triggerDownload(localFile, remoteFile);
                break;
            case SyncConstants.INTENT_ACTION_UPLOAD:
                triggerUpload(localFile, remoteFile);
                break;
            case SyncConstants.INTENT_ACTION_SYNC:
            default:
                triggerSync(localFile, remoteFile);
                break;
        }
    }

    // private

    private void triggerDownload(final File localFile, CloudMetaData remoteFile) {
        SyncManager sync = new SyncManager(getBaseContext());

        final android.support.v4.app.NotificationCompat.Builder notification = new SyncNotificationFactory(getBaseContext())
                .getNotificationBuilderForDownload();

        final NotificationManager notificationManager = (NotificationManager) getApplicationContext()
                .getSystemService(Context.NOTIFICATION_SERVICE);
        final File tempFile = new File(localFile.toString() + "-download");

        IOnDownloadUploadEntry onDownloadUpload = new IOnDownloadUploadEntry() {
            @Override
            public void onPreExecute() {
                if (notification != null && notificationManager != null) {
                    notificationManager.notify(SyncConstants.NOTIFICATION_DROPBOX_PROGRESS, notification.build());
                }
            }

            @Override
            public void onPostExecute(boolean result) {
                if (notification != null && notificationManager != null) {
                    notificationManager.cancel(SyncConstants.NOTIFICATION_DROPBOX_PROGRESS);
                    if (result) {
                        // copy file
                        Core core = new Core(getApplicationContext());
                        try {
                            core.copy(tempFile, localFile);
                            tempFile.delete();
                        } catch (IOException e) {
                            Log.e(LOGCAT, e.getMessage());
                            return;
                        }
                        // create notification for open file
                        // intent is passed to the notification and called if clicked on.
                        Intent intent = new SyncCommon().getIntentForOpenDatabase(getBaseContext(), localFile);
                        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),
                                MainActivity.REQUEST_PICKFILE_CODE, intent, 0);
                        // create builder
                        final NotificationCompat.Builder notification =
                                new SyncNotificationFactory(getBaseContext())
                                        .getNotificationBuilderDownloadComplete(pendingIntent);
                        // notify
                        notificationManager.notify(SyncConstants.NOTIFICATION_SYNC_OPEN_FILE, notification.build());
                    }
                }
            }
        };
        if (BuildConfig.DEBUG) {
            Log.d(LOGCAT, "Download file from Dropbox. Local file: " + localFile.getPath() + "; Remote file: " + remoteFile.getPath());
        }

        //start
        onDownloadUpload.onPreExecute();
        //send message to the database download staring
        sendMessage(SyncMessages.STARTING_DOWNLOAD);

        boolean ret = sync.download(remoteFile, tempFile);

        onDownloadUpload.onPostExecute(ret);

        //send message to the database download complete
        sendMessage(SyncMessages.DOWNLOAD_COMPLETE);
    }

    private void triggerUpload(final File localFile, CloudMetaData remoteFile) {
        final NotificationCompat.Builder notification = new SyncNotificationFactory(getBaseContext())
                .getNotificationBuilderUpload();
        final NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        IOnDownloadUploadEntry onDownloadUpload = new IOnDownloadUploadEntry() {
            @Override
            public void onPreExecute() {
                if (notification != null && notificationManager != null) {
                    notificationManager.notify(SyncConstants.NOTIFICATION_DROPBOX_PROGRESS, notification.build());
                }
            }

            @Override
            public void onPostExecute(boolean result) {
                if (notification != null && notificationManager != null) {
                    notificationManager.cancel(SyncConstants.NOTIFICATION_DROPBOX_PROGRESS);
                    if (result) {
                        // create notification for open file
                        // pending intent
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.setData(Uri.fromFile(localFile));
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), MainActivity.REQUEST_PICKFILE_CODE, intent, 0);
                        // notification
                        final NotificationCompat.Builder notification = new SyncNotificationFactory(getBaseContext())
                                .getNotificationBuilderUploadComplete(pendingIntent);
                        // notify
                        notificationManager.notify(SyncConstants.NOTIFICATION_SYNC_OPEN_FILE, notification.build());
                    }
                }
            }
        };

        if (BuildConfig.DEBUG) {
            Log.d(LOGCAT, "Uploading db. Local file: " +
                    localFile.getPath() + "; Remote file: " + remoteFile.getPath());
        }

        //start
        onDownloadUpload.onPreExecute();
        //send message to the database upload staring
        sendMessage(SyncMessages.STARTING_UPLOAD);

        //execute
        SyncManager sync = new SyncManager(getBaseContext());
        boolean result = sync.upload(localFile.getPath(), remoteFile.getPath());

        //complete
        onDownloadUpload.onPostExecute(result);
        ///send message to the database upload complete
        sendMessage(SyncMessages.UPLOAD_COMPLETE);
    }

    private void triggerSync(final File localFile, CloudMetaData remoteFile) {
        SyncManager sync = new SyncManager(getBaseContext());

        DateTime localLastModified = sync.getLastModifiedDate(remoteFile);
        if (localLastModified == null) localLastModified = new DateTime(localFile.lastModified());

        DateTime remoteLastModified = new DateTime(remoteFile.getModifiedAt());

        if (BuildConfig.DEBUG) Log.d(LOGCAT, "Date last modified local file: " + localLastModified.toString());
        if (BuildConfig.DEBUG) Log.d(LOGCAT, "Date last modified remote file: " + remoteLastModified.toString());

        // check date
        if (remoteLastModified.isAfter(localLastModified)) {
            if (BuildConfig.DEBUG) Log.d(LOGCAT, "Download " + remoteFile.getPath() + " from the cloud storage.");
            // download file
            triggerDownload(localFile, remoteFile);
        } else if (remoteLastModified.isBefore(localLastModified)) {
            if (BuildConfig.DEBUG) Log.d(LOGCAT, "Upload " + localFile.getPath() + " to the cloud storage.");
            // upload file
            triggerUpload(localFile, remoteFile);
        } else {
            if (BuildConfig.DEBUG) Log.d(LOGCAT, "The local and remote files are the same.");

            sendMessage(SyncMessages.FILE_NOT_CHANGED);
        }
    }

    private boolean sendMessage(Integer message) {
        if (mOutMessenger == null) return true;

        Message msg = new Message();
        msg.what = message;

        try {
            mOutMessenger.send(msg);
        } catch (Exception e) {
            Log.e(LOGCAT, e.getMessage());
            return false;
        }
        return true;
    }
}
