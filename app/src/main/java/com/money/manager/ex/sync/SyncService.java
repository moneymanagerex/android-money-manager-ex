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
import com.money.manager.ex.dropbox.IOnDownloadUploadEntry;
import com.money.manager.ex.dropbox.SyncCommon;
import com.money.manager.ex.dropbox.SyncNotificationFactory;
import com.money.manager.ex.home.MainActivity;
import com.money.manager.ex.utils.NetworkUtilities;

import java.io.File;
import java.io.IOException;

/**
 * The background service that synchronizes the database file.
 * It is being invoked by the timer (is it?).
 * It displays the sync notification and invokes the cloud api.
 */
public class SyncService
    extends IntentService {

    public static final String INTENT_EXTRA_MESSENGER = "com.money.manager.ex.sync.MESSENGER";
    public static final Integer INTENT_EXTRA_MESSENGER_NOT_CHANGE = 0x000;
    public static final Integer INTENT_EXTRA_MESSENGER_DOWNLOAD = 0x000A;
    public static final Integer INTENT_EXTRA_MESSENGER_UPLOAD = 0x000B;
    public static final Integer INTENT_EXTRA_MESSENGER_START_DOWNLOAD = 0x000C;
    public static final Integer INTENT_EXTRA_MESSENGER_START_UPLOAD = 0x000D;
    public static final Integer INTENT_EXTRA_MESSENGER_NOT_ON_WIFI = 0x000E;

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
            return;
        }

        SyncManager sync = new SyncManager(getBaseContext());

        String localFilename = intent.getStringExtra(SyncConstants.INTENT_EXTRA_LOCAL_FILE);
        String remoteFilename = intent.getStringExtra(SyncConstants.INTENT_EXTRA_REMOTE_FILE);
        // check if file is correct
        if (TextUtils.isEmpty(localFilename) || TextUtils.isEmpty(remoteFilename)) return;

        // take a file and entries
        File localFile = new File(localFilename);
//        Entry remoteFile = mDropboxHelper.getEntry(remote);
        CloudMetaData remoteFile = sync.getProvider().getMetadata(remoteFilename);
        // check if local file or remote file is null, then exit
        if (remoteFile == null) {
            if (SyncConstants.INTENT_ACTION_UPLOAD.equals(intent.getAction())) {
                Log.w(LOGCAT, "remoteFile is null. DropboxService.onHandleIntent forcing creation of the remote file.");
                // todo: redo this
//            remoteFile = new Entry();
//            remoteFile.path = remote;
            } else {
                Log.e(LOGCAT, "remoteFile is null. DropboxService.onHandleIntent premature exit.");
                return;
            }
        }

        // check if name is same
        if (!localFile.getName().toLowerCase().equals(remoteFile.getName().toLowerCase())) {
            Log.w(LOGCAT, "Local filename different from the remote!");
            return;
        }

        // Execute action.
        if (SyncConstants.INTENT_ACTION_DOWNLOAD.equals(intent.getAction())) {
            triggerDownload(localFile, remoteFilename);
        } else if (SyncConstants.INTENT_ACTION_UPLOAD.equals(intent.getAction())) {
            triggerUpload(localFile, remoteFile);
        } else {
            // Synchronization
//            todo syncFile(localFile, remoteFile);
        }
    }

    public void triggerDownload(final File localFile, String remoteFilename) {
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
            Log.d(LOGCAT, "Download file from Dropbox. Local file: " + localFile.getPath() + "; Remote file: " + remoteFilename);
        }

        //start
        onDownloadUpload.onPreExecute();
        //send message to the database download staring
        Message messageStart = new Message();
        messageStart.what = SyncService.INTENT_EXTRA_MESSENGER_START_DOWNLOAD;
        sendMessenger(messageStart);
        //execute
        boolean ret = sync.download(remoteFilename, tempFile);
        //complete
        onDownloadUpload.onPostExecute(ret);

        //send message to the database download complete
        Message messageComplete = new Message();
        messageComplete.what = SyncService.INTENT_EXTRA_MESSENGER_DOWNLOAD;
        sendMessenger(messageComplete);
    }

    public void triggerUpload(final File localFile, CloudMetaData remoteFile) {
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
        Message messageStart = new Message();
        messageStart.what = SyncService.INTENT_EXTRA_MESSENGER_START_UPLOAD;
        sendMessenger(messageStart);

        //execute
        SyncManager sync = new SyncManager(getBaseContext());
        boolean result = sync.upload(localFile.getPath(), remoteFile.getPath());

        //complete
        onDownloadUpload.onPostExecute(result);
        ///send message to the database upload complete
        Message messageComplete = new Message();
        messageComplete.what = SyncService.INTENT_EXTRA_MESSENGER_UPLOAD;
        sendMessenger(messageComplete);
    }

    private boolean sendMessenger(Message msg) {
        if (mOutMessenger != null) {
            try {
                mOutMessenger.send(msg);
            } catch (Exception e) {
                Log.e(LOGCAT, e.getMessage());
                return false;
            }
        }
        return true;
    }

}
