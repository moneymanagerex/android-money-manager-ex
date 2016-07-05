///*
// * Copyright (C) 2012-2016 The Android Money Manager Ex Project Team
// *
// * This program is free software; you can redistribute it and/or
// * modify it under the terms of the GNU General Public License
// * as published by the Free Software Foundation; either version 3
// * of the License, or (at your option) any later version.
// *
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU General Public License for more details.
// *
// * You should have received a copy of the GNU General Public License
// * along with this program.  If not, see <http://www.gnu.org/licenses/>.
// */
//
//package com.money.manager.ex.dropbox;
//
//import android.app.IntentService;
//import android.app.NotificationManager;
//import android.app.PendingIntent;
//import android.content.Context;
//import android.content.Intent;
//import android.net.Uri;
//import android.os.Message;
//import android.os.Messenger;
//import android.support.v4.app.NotificationCompat;
//import android.text.TextUtils;
//import android.util.Log;
//
//import com.dropbox.client2.DropboxAPI.Entry;
//import com.dropbox.client2.ProgressListener;
//import com.money.manager.ex.BuildConfig;
//import com.money.manager.ex.home.MainActivity;
//import com.money.manager.ex.core.Core;
//import com.money.manager.ex.sync.SyncConstants;
//import com.money.manager.ex.sync.SyncService;
//import com.money.manager.ex.utils.NetworkUtilities;
//
//import java.io.File;
//import java.io.IOException;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//
///**
// * Database synchronization service. Run on schedule or invoked manually.
// */
//public class DropboxService
//    extends IntentService {
//
//    private static final String LOGCAT = DropboxService.class.getSimpleName();
//
//    private DropboxHelper mDropboxHelper;
//    // messenger
//    private Messenger mOutMessenger;
//
//    public DropboxService() {
//        super("com.money.manager.ex.dropbox.DropboxService");
//    }
//
//    @Override
//    protected void onHandleIntent(Intent intent) {
//        if (BuildConfig.DEBUG) Log.d(LOGCAT, intent.toString());
//
//        // Check if there is a messenger. Used to send the messages back.
//        if (intent.getExtras().containsKey(SyncService.INTENT_EXTRA_MESSENGER)) {
//            mOutMessenger = intent.getParcelableExtra(SyncService.INTENT_EXTRA_MESSENGER);
//        }
//
//        // check if the device is online.
//        NetworkUtilities network = new NetworkUtilities(getApplicationContext());
//        if (!network.isOnline()) {
//            if (BuildConfig.DEBUG) Log.i(LOGCAT, "Can't sync. Device not online.");
//            return;
//        }
//
//        // take instance dropbox
//        mDropboxHelper = DropboxHelper.getInstance(getApplicationContext());
//        // check if connect to dropbox
//        mDropboxHelper.isLinked();
//        // take local and remote files
//        String local = intent.getStringExtra(SyncConstants.INTENT_EXTRA_LOCAL_FILE);
//        String remote = intent.getStringExtra(SyncConstants.INTENT_EXTRA_REMOTE_FILE);
//        // check if file is correct
//        if (TextUtils.isEmpty(local) || TextUtils.isEmpty(remote)) return;
//
//        // take a file and entries
//        File localFile = new File(local);
//        Entry remoteFile = mDropboxHelper.getEntry(remote);
//        // check if local file or remote file is null, then exit
//        if (remoteFile == null && SyncConstants.INTENT_ACTION_UPLOAD.equals(intent.getAction())) {
//            Log.w(LOGCAT, "remoteFile is null. DropboxService.onHandleIntent forcing creation of the remote file.");
//            remoteFile = new Entry();
//            remoteFile.path = remote;
//        } else if (remoteFile == null) {
//            Log.e(LOGCAT, "remoteFile is null. DropboxService.onHandleIntent premature exit.");
//            return;
//        }
//
//        // check if name is same
//        if (!localFile.getName().toUpperCase().equals(remoteFile.fileName().toUpperCase())) {
//            Log.w(LOGCAT, "Local filename different from the remote!");
//            return;
//        }
//
//        // Execute action.
//        if (SyncConstants.INTENT_ACTION_DOWNLOAD.equals(intent.getAction())) {
//            triggerDownload(localFile, remoteFile);
//        } else if (SyncConstants.INTENT_ACTION_UPLOAD.equals(intent.getAction())) {
//            triggerUpload(localFile, remoteFile);
//        } else {
//            // Synchronization
//            triggerSync(localFile, remoteFile);
//        }
//    }
//
//    public void triggerSync(final File localFile, final Entry remoteFile) {
//        Date localLastModified = null;
//        Date remoteLastModified;
//        try {
//            localLastModified = mDropboxHelper.getDateLastModified(remoteFile.fileName());
//        } catch (Exception e) {
//            Log.e(LOGCAT, e.getMessage());
//        }
//        if (localLastModified == null) localLastModified = new Date(localFile.lastModified());
//        remoteLastModified = mDropboxHelper.getLastModifiedEntry(remoteFile);
//
//        if (BuildConfig.DEBUG) Log.d(LOGCAT, "Date last modified local file: " + new SimpleDateFormat().format(localLastModified));
//        if (BuildConfig.DEBUG) Log.d(LOGCAT, "Date last modified remote file: " + new SimpleDateFormat().format(remoteLastModified));
//
//        // check date
//        if (remoteLastModified.after(localLastModified)) {
//            if (BuildConfig.DEBUG) Log.d(LOGCAT, "Download " + remoteFile.path + " from Dropox");
//            // download file
//            triggerDownload(localFile, remoteFile);
//        } else if (remoteLastModified.before(localLastModified)) {
//            if (BuildConfig.DEBUG) Log.d(LOGCAT, "Upload " + localFile.getPath() + " to Dropox");
//            // upload file
//            triggerUpload(localFile, remoteFile);
//        } else {
//            if (BuildConfig.DEBUG) Log.d(LOGCAT, "The local and remote files are the same");
//            Message message = new Message();
//            message.what = SyncService.FILE_NOT_CHANGED;
//            sendMessenger(message);
//        }
//    }
//
//    public void triggerDownload(final File localFile, final Entry remoteFile) {
//        final NotificationCompat.Builder notification = new SyncNotificationFactory(getBaseContext()).getNotificationBuilderForDownload();
//
//        final NotificationManager notificationManager = (NotificationManager) getApplicationContext()
//                .getSystemService(Context.NOTIFICATION_SERVICE);
//        final File tempFile = new File(localFile.toString() + "-download");
//
//        IOnDownloadUploadEntry onDownloadUpload = new IOnDownloadUploadEntry() {
//            @Override
//            public void onPreExecute() {
//                if (notification != null && notificationManager != null) {
//                    notificationManager.notify(SyncConstants.NOTIFICATION_DROPBOX_PROGRESS, notification.build());
//                }
//            }
//
//            @Override
//            public void onPostExecute(boolean result) {
//                if (notification != null && notificationManager != null) {
//                    notificationManager.cancel(SyncConstants.NOTIFICATION_DROPBOX_PROGRESS);
//                    if (result) {
//                        // copy file
//                        Core core = new Core(getApplicationContext());
//                        try {
//                            core.copy(tempFile, localFile);
//                            tempFile.delete();
//                        } catch (IOException e) {
//                            Log.e(LOGCAT, e.getMessage());
//                            return;
//                        }
//                        // create notification for open file
//                        // intent is passed to the notification and called if clicked on.
//                        Intent intent = new SyncCommon().getIntentForOpenDatabase(getBaseContext(), localFile);
//                        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),
//                                MainActivity.REQUEST_PICKFILE_CODE, intent, 0);
//                        // create builder
//                        final NotificationCompat.Builder notification = new SyncNotificationFactory(getBaseContext())
//                                .getNotificationBuilderDownloadComplete(pendingIntent);
//                        // notify
//                        notificationManager.notify(SyncConstants.NOTIFICATION_SYNC_OPEN_FILE, notification.build());
//                    }
//                }
//            }
//        };
//        // create listener
//        ProgressListener listener = new ProgressListener() {
//            @Override
//            public void onProgress(long bytes, long total) {
//                notificationManager.notify(SyncConstants.NOTIFICATION_DROPBOX_PROGRESS,
//                        new SyncNotificationFactory(getBaseContext())
//                                .getNotificationBuilderProgress(notification, (int) total, (int) bytes).build());
//            }
//        };
//        if (BuildConfig.DEBUG) {
//            Log.d(LOGCAT, "Download file from Dropbox. Local file: " + localFile.getPath() + "; Remote file: " + remoteFile.path);
//        }
//
//        //start
//        onDownloadUpload.onPreExecute();
//        //send message to the database download staring
//        Message messageStart = new Message();
//        messageStart.what = SyncService.STARTING_DOWNLOAD;
//        sendMessenger(messageStart);
//        //execute
//        boolean ret = mDropboxHelper.download(remoteFile, tempFile, listener);
//        //complete
//        onDownloadUpload.onPostExecute(ret);
//
//        //send message to the database download complete
//        Message messageComplete = new Message();
//        messageComplete.what = SyncService.DOWNLOAD_COMPLETE;
//        sendMessenger(messageComplete);
//    }
//
//    public void triggerUpload(final File localFile, final Entry remoteFile) {
//        final NotificationCompat.Builder notification = new SyncNotificationFactory(getBaseContext())
//                .getNotificationBuilderUpload();
//        // get instance notification manager
//        final NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
//        // create interface
//        IOnDownloadUploadEntry onDownloadUpload = new IOnDownloadUploadEntry() {
//            @Override
//            public void onPreExecute() {
//                if (notification != null && notificationManager != null) {
//                    notificationManager.notify(SyncConstants.NOTIFICATION_DROPBOX_PROGRESS, notification.build());
//                }
//            }
//
//            @Override
//            public void onPostExecute(boolean result) {
//                if (notification != null && notificationManager != null) {
//                    notificationManager.cancel(SyncConstants.NOTIFICATION_DROPBOX_PROGRESS);
//                    if (result) {
//                        // create notification for open file
//                        // pending intent
//                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
//                        intent.setData(Uri.fromFile(localFile));
//                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), MainActivity.REQUEST_PICKFILE_CODE, intent, 0);
//                        // notification
//                        final NotificationCompat.Builder notification = new SyncNotificationFactory(getBaseContext())
//                                .getNotificationBuilderUploadComplete(pendingIntent);
//                        // notify
//                        notificationManager.notify(SyncConstants.NOTIFICATION_SYNC_OPEN_FILE, notification.build());
//                    }
//                }
//            }
//        };
//        // create listener
//        ProgressListener listener = new ProgressListener() {
//            @Override
//            public void onProgress(long bytes, long total) {
//                notificationManager.notify(SyncConstants.NOTIFICATION_DROPBOX_PROGRESS,
//                        new SyncNotificationFactory(getBaseContext())
//                                .getNotificationBuilderProgress(notification, (int) total, (int) bytes).build());
//            }
//        };
//
//        if (BuildConfig.DEBUG) Log.d(LOGCAT, "Upload file from Dropbox. Local file: " +
//                localFile.getPath() + "; Remote file: " + remoteFile.path);
//        //start
//        onDownloadUpload.onPreExecute();
//        //send message to the database upload staring
//        Message messageStart = new Message();
//        messageStart.what = SyncService.STARTING_UPLOAD;
//        sendMessenger(messageStart);
//        //execute
//        boolean ret = mDropboxHelper.upload(remoteFile.path, localFile, listener);
//        //complete
//        onDownloadUpload.onPostExecute(ret);
//        ///send message to the database upload complete
//        Message messageComplete = new Message();
//        messageComplete.what = SyncService.UPLOAD_COMPLETE;
//        sendMessenger(messageComplete);
//    }
//
//    public boolean sendMessenger(Message msg) {
//        if (mOutMessenger != null) {
//            try {
//                mOutMessenger.send(msg);
//            } catch (Exception e) {
//                Log.e(LOGCAT, e.getMessage());
//                return false;
//            }
//        }
//        return true;
//    }
//
//}
