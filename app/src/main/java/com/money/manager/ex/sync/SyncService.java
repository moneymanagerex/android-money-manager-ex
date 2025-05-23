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
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Message;
import android.os.Messenger;
import android.text.TextUtils;

import androidx.core.app.JobIntentService;

import com.money.manager.ex.MmexApplication;
import com.money.manager.ex.R;
import com.money.manager.ex.core.docstorage.FileStorageHelper;
import com.money.manager.ex.home.DatabaseMetadata;
import com.money.manager.ex.home.RecentDatabasesProvider;
import com.money.manager.ex.sync.events.SyncStartingEvent;
import com.money.manager.ex.sync.events.SyncStoppingEvent;
import com.money.manager.ex.sync.merge.DataMerger;
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
 * <p>
 * Changed to JobIntentService as per
 * <a href="https://android.jlelse.eu/keep-those-background-services-working-when-targeting-android-oreo-sdk-26-cbf6cc2bdb7f">...</a>
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
        Timber.d("Running sync job: %s", action);
//        sendStartEvent();

        // Check if there is a messenger. Used to send the messages back.
        Messenger outMessenger = null;
        if (intent.getExtras().containsKey(SyncService.INTENT_EXTRA_MESSENGER)) {
            outMessenger = intent.getParcelableExtra(SyncService.INTENT_EXTRA_MESSENGER);
        }
        boolean prefMergeOnSync = intent.getBooleanExtra(SyncConstants.INTENT_EXTRA_PREF_MERGE_ON_SYNC, false);

        String localFilename = intent.getStringExtra(SyncConstants.INTENT_EXTRA_LOCAL_FILE);
        String remoteFilename = intent.getStringExtra(SyncConstants.INTENT_EXTRA_REMOTE_FILE);
        // check if file is correct
        if (TextUtils.isEmpty(localFilename) || TextUtils.isEmpty(remoteFilename)) {
//            sendStopEvent();
            return;
        }

        // check if the device is online.
        NetworkUtils network = new NetworkUtils(getApplicationContext());
        if (!network.isOnline() && !Uri.parse(remoteFilename).getAuthority().startsWith("com.android")) {
            Timber.i("Can't sync. Device not online.");
            sendMessage(outMessenger, SyncServiceMessage.NOT_ON_WIFI);
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
                triggerSync(outMessenger, localFile, prefMergeOnSync);
                break;
            default:
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

    private void triggerSync(Messenger outMessenger, File localFile, boolean prefMergeOnSync) {
        DatabaseMetadata currentDb = this.recentDatabasesProvider.get(localFile.getAbsolutePath());
        FileStorageHelper storage = new FileStorageHelper(getApplicationContext());
        // download remote file into tmp (this forces also a refresh of the meta data)
        storage.pullDatabaseToTmpFile(currentDb);
        boolean isLocalModified = currentDb.isLocalFileChanged();
        boolean isRemoteModified = currentDb.isRemoteFileChanged(getApplicationContext());
        Timber.d("Local file has changed: %b, Remote file has changed: %b", isLocalModified, isRemoteModified);
        Uri uri = Uri.parse(currentDb.remotePath);

        // for debug
//        if (true) {
//            isLocalModified = true;
//            isRemoteModified = true;
//        }

        // possible outcomes:
        if (!isLocalModified && !isRemoteModified) {
            sendMessage(outMessenger, SyncServiceMessage.FILE_NOT_CHANGED);
//            sendStopEvent();
            MmexApplication.getAmplitude().track("synchronize", new HashMap<String, String>() {{
                put("authority", uri.getAuthority());
                put("result", "no change");
            }});
            return;
        }

            if (isLocalModified && isRemoteModified) {
                // EXPERIMENTAL setting to merge on sync
                if (prefMergeOnSync) {
                    // TODO duplicate local database in case the user aborts merge and want to resume
                    // start merge changes from remote to local
                    DataMerger merger = new DataMerger(outMessenger);
                    try {
                        merger.merge(currentDb, storage);
                        Timber.d("Local file %s, Remote file %s merged. Triggering upload.", localFile.getPath(), currentDb.remotePath);
                        // upload file
                        storage.pushDatabase(currentDb);
                        sendMessage(outMessenger, SyncServiceMessage.UPLOAD_COMPLETE);
                    } catch (Exception e) {
                        Timber.e(e, "Could not complete sync");
                        sendMessage(outMessenger, SyncServiceMessage.CONFLICT);
                        //           sendStopEvent();
                        MmexApplication.getAmplitude().track("synchronize", new HashMap<String, String>() {{
                            put("authority", uri.getAuthority());
                            put("result", "Conflict");
                        }});
                        showNotificationForConflict();
                    }
                } else {
                    showNotificationForConflict();
                }
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
            Timber.d("Local file %s changed. Triggering upload.", localFile.getPath());
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
