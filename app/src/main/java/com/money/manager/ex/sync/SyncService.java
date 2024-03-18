/*
 * Copyright (C) 2012-2018 The Android Money Manager Ex Project Team
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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Message;
import android.os.Messenger;
import android.text.TextUtils;

import androidx.core.app.JobIntentService;

import com.money.manager.ex.MmexApplication;
import com.money.manager.ex.R;
import com.money.manager.ex.core.RequestCodes;
import com.money.manager.ex.core.docstorage.FileStorageHelper;
import com.money.manager.ex.home.DatabaseMetadata;
import com.money.manager.ex.home.MainActivity;
import com.money.manager.ex.home.RecentDatabasesProvider;
import com.money.manager.ex.sync.events.SyncStartingEvent;
import com.money.manager.ex.sync.events.SyncStoppingEvent;
import com.money.manager.ex.utils.NetworkUtils;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.HashMap;

import javax.inject.Inject;

import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * The background service that synchronizes the database file.
 * It is being invoked by the timer.
 * It displays the sync notification and invokes the cloud api.
 *
 * Changed to JobIntentService as per
 * https://android.jlelse.eu/keep-those-background-services-working-when-targeting-android-oreo-sdk-26-cbf6cc2bdb7f
 * to make it compatible with Android 8 Oreo.
 */
public class SyncService
        extends JobIntentService {

    public static final int SYNC_JOB_ID = 1000;
    public static final String INTENT_EXTRA_MESSENGER = "com.money.manager.ex.sync.MESSENGER";

//    public SyncService() {
//        super("com.money.manager.ex.sync.SyncService");
//    }

    @Inject
    RecentDatabasesProvider recentDatabasesProvider;

    private CompositeSubscription compositeSubscription;
    private NotificationManager mNotificationManager;

    @Override
    public void onCreate() {
        super.onCreate();

        compositeSubscription = new CompositeSubscription();
        mNotificationManager = (NotificationManager) getApplicationContext()
                .getSystemService(Context.NOTIFICATION_SERVICE);

        MmexApplication.getApp().iocComponent.inject(this);
    }

    @Override
    protected void onHandleWork(Intent intent) {
        String action = intent != null
                ? intent.getAction()
                : "null";
        Timber.d("Running sync service: %s", action);
//        sendStartEvent();

        // Check if there is a messenger. Used to send the messages back.
        Messenger outMessenger = null;
        if (intent.getExtras().containsKey(SyncService.INTENT_EXTRA_MESSENGER)) {
            outMessenger = intent.getParcelableExtra(SyncService.INTENT_EXTRA_MESSENGER);
        }

        // check if the device is online.
        NetworkUtils network = new NetworkUtils(getApplicationContext());
        if (!network.isOnline()) {
            Timber.i("Can't sync. Device not online.");
            sendMessage(outMessenger, SyncServiceMessage.NOT_ON_WIFI);
//            sendStopEvent();
            return;
        }

        String localFilename = intent.getStringExtra(SyncConstants.INTENT_EXTRA_LOCAL_FILE);
        String remoteFilename = intent.getStringExtra(SyncConstants.INTENT_EXTRA_REMOTE_FILE);
        // check if file is correct
        if (TextUtils.isEmpty(localFilename) || TextUtils.isEmpty(remoteFilename)) {
//            sendStopEvent();
            return;
        }

        File localFile = new File(localFilename);
        DatabaseMetadata currentDb = this.recentDatabasesProvider.get(localFile.getAbsolutePath());
        FileStorageHelper storage = new FileStorageHelper(getApplicationContext());

        // Execute action.
        switch (action) {
            case SyncConstants.INTENT_ACTION_DOWNLOAD:
                storage.pullDatabase(currentDb);
                sendMessage(outMessenger, SyncServiceMessage.DOWNLOAD_COMPLETE);
                break;
            case SyncConstants.INTENT_ACTION_UPLOAD:
                storage.pushDatabase(currentDb);
                sendMessage(outMessenger, SyncServiceMessage.UPLOAD_COMPLETE);
                break;
            case SyncConstants.INTENT_ACTION_SYNC:
            default:
                triggerSync(outMessenger, localFile);
                break;
        }
    }

    @Override
    public void onDestroy() {
        if (!compositeSubscription.isUnsubscribed()) {
            compositeSubscription.unsubscribe();
        }

        super.onDestroy();
    }

    public static void enqueueWork(Context context, Intent intent) {
        enqueueWork(context, SyncService.class, SyncService.SYNC_JOB_ID, intent);
    }

    /*
        Private
    */

//    private void triggerDownload(final File localFile, final CloudMetaData remoteFile) {
//        final SyncManager sync = new SyncManager(getApplicationContext());
//
//        Notification notification = new SyncNotificationFactory(getApplicationContext())
//                .getNotificationForDownload();
//
//        final NotificationManager notificationManager = (NotificationManager) getApplicationContext()
//                .getSystemService(Context.NOTIFICATION_SERVICE);
//        final File tempFile = new File(localFile.toString() + "-download");
//
//        Timber.d("Download file. Local file: %s, remote file: %s", localFile.getPath(), remoteFile.getPath());
//
//        if (notification != null && notificationManager != null) {
//            notificationManager.notify(SyncConstants.NOTIFICATION_SYNC_IN_PROGRESS, notification);
//        }
//        sendMessage(SyncServiceMessage.STARTING_DOWNLOAD);
//
//        compositeSubscription.add(
//                sync.downloadSingle(remoteFile, tempFile)
//                        // do not run on another thread as then the service will be destroyed.
////                    .subscribeOn(Schedulers.io())
//                        .subscribe(new SingleSubscriber<Void>() {
//                            @Override
//                            public void onSuccess(Void value) {
//                                //onDownloadHandler.onPostExecute(true);
//                                afterDownload(notificationManager, tempFile, localFile,
//                                        remoteFile, sync);
//                                sendMessage(SyncServiceMessage.DOWNLOAD_COMPLETE);
//                                sendStopEvent();
//                            }
//
//                            @Override
//                            public void onError(Throwable error) {
//                                Timber.e(error, "async download");
//                                sendMessage(SyncServiceMessage.ERROR);
//                                sendStopEvent();
//                            }
//                        })
//        );
//    }

//    private void afterDownload(NotificationManager notificationManager, File tempFile, File localFile,
//                               CloudMetaData remoteFile, SyncManager sync) {
//        if (notificationManager == null) return;
//
//        notificationManager.cancel(SyncConstants.NOTIFICATION_SYNC_IN_PROGRESS);
//
//        // copy file
//        try {
//            MmxFileUtils.copy(tempFile, localFile);
//            tempFile.delete();
//        } catch (IOException e) {
//            Timber.e(e, "copying downloaded database file");
//            return;
//        }
//
//        sync.saveRemoteLastModifiedDate(localFile.getAbsolutePath(), remoteFile);
//
//        // Create the notification for opening of the downloaded file.
//        // The Intent is passed to the notification and called if clicked on.
//        Intent intent = new SyncCommon().getIntentForOpenDatabase(getApplicationContext(), localFile);
//        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),
//                RequestCodes.SELECT_FILE, intent, 0);
//
//        Notification completeNotification = new SyncNotificationFactory(getApplicationContext())
//                .getNotificationDownloadComplete(pendingIntent);
//
//        // notify
//        notificationManager.notify(SyncConstants.NOTIFICATION_SYNC_OPEN_FILE, completeNotification);
//    }

//    private void triggerUpload(final File localFile, CloudMetaData remoteFile) {
//        Timber.d("Uploading db. Local file: %s, remote file: %s", localFile.getPath(), remoteFile.getPath());
//
//        showNotificationUploading();
//        sendMessage(SyncServiceMessage.STARTING_UPLOAD);
//
//        // upload
//        SyncManager sync = new SyncManager(getApplicationContext());
//        boolean result = sync.upload(localFile.getPath(), remoteFile.getPath());
//
//        // notification, upload complete
//        showNotificationUploadComplete(result, localFile);
//        sendMessage(SyncServiceMessage.UPLOAD_COMPLETE);
//        sendStopEvent();
//    }

    private void showNotificationUploadComplete(boolean result, File localFile) {
        if (mNotificationManager == null) return;

        mNotificationManager.cancel(SyncConstants.NOTIFICATION_SYNC_IN_PROGRESS);

        if (!result) return;

        // create notification for open file
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setData(Uri.fromFile(localFile));
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), RequestCodes.SELECT_FILE, intent, 0);
        // notification
        Notification notification = new SyncNotificationFactory(getApplicationContext())
                .getNotificationUploadComplete(pendingIntent);
        // notify
        mNotificationManager.notify(SyncConstants.NOTIFICATION_SYNC_OPEN_FILE, notification);

    }

    private void triggerSync(Messenger outMessenger, File localFile) {
        SyncManager sync = new SyncManager(getApplicationContext());

        DatabaseMetadata currentDb = this.recentDatabasesProvider.get(localFile.getAbsolutePath());
        FileStorageHelper storage = new FileStorageHelper(getApplicationContext());
        boolean isLocalModified = storage.isLocalFileChanged(currentDb);
        boolean isRemoteModified = storage.isRemoteFileChanged(currentDb);
        Timber.d("local file has changes: %b", isLocalModified);
        Timber.d("Remote file has changes: %b", isRemoteModified);
        Uri uri = Uri.parse(currentDb.remotePath);

        // possible outcomes:
        if (!isLocalModified && !isRemoteModified) {
            sendMessage(outMessenger, SyncServiceMessage.FILE_NOT_CHANGED);
//            sendStopEvent();
            MmexApplication.getAmplitude().track("synchronize", new HashMap() {{
                put("authority", uri.getAuthority());
                put("result", "no change");
            }});
            return;
        }

        if (isLocalModified && isRemoteModified) {
            // if both changed, there is a conflict!
            Timber.w(getString(R.string.both_files_modified));
            sendMessage(outMessenger, SyncServiceMessage.CONFLICT);
 //           sendStopEvent();
            MmexApplication.getAmplitude().track("synchronize", new HashMap() {{
                put("authority", uri.getAuthority());
                put("result", "Conflict");
            }});
            showNotificationForConflict();
            return;
        }
        if (isRemoteModified) {
            Timber.d("Remote file %s changed. Triggering download.", currentDb.remotePath);
            // download file
            storage.pullDatabase(currentDb);
            sendMessage(outMessenger, SyncServiceMessage.DOWNLOAD_COMPLETE);
            return;
        }
        if (isLocalModified) {
            Timber.d("Local file %s has changed. Triggering upload.", localFile.getPath());
            // upload file
            storage.pushDatabase(currentDb);
            sendMessage(outMessenger, SyncServiceMessage.UPLOAD_COMPLETE);
            return;
        }
    }

    private boolean sendMessage(Messenger outMessenger, SyncServiceMessage message) {
        if (outMessenger == null) return true;

        Message msg = new Message();
        msg.what = message.code;

        try {
            outMessenger.send(msg);
        } catch (Exception e) {
            Timber.e(e, "sending message from the sync service");

            return false;
        }
        return true;
    }

    private void sendStartEvent() {
        // send notification via event bus
        if (EventBus.getDefault().hasSubscriberForEvent(SyncStartingEvent.class)) {
            EventBus.getDefault().post(new SyncStartingEvent());
        }
    }

    private void sendStopEvent() {
        if (EventBus.getDefault().hasSubscriberForEvent(SyncStoppingEvent.class)) {
            EventBus.getDefault().post(new SyncStoppingEvent());
        }
    }

    private void showNotificationUploading() {
        Notification notification = new SyncNotificationFactory(getApplicationContext())
                .getNotificationUploading();

        // send notification, upload starting
        if (notification == null || mNotificationManager == null) return;

        mNotificationManager.notify(SyncConstants.NOTIFICATION_SYNC_IN_PROGRESS, notification);
    }

    private void showNotificationForConflict() {
        Notification notification = new SyncNotificationFactory(getApplicationContext())
                .getNotificationForConflict();

        mNotificationManager.notify(SyncConstants.NOTIFICATION_SYNC_ERROR, notification);
    }
}
