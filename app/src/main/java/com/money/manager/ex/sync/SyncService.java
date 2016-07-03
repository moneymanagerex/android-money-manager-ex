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
import android.content.Intent;
import android.os.Messenger;
import android.text.TextUtils;
import android.util.Log;

import com.cloudrail.si.types.CloudMetaData;
import com.money.manager.ex.BuildConfig;
import com.money.manager.ex.utils.NetworkUtilities;

import java.io.File;

/**
 * The background service that synchronizes the database file.
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

        String local = intent.getStringExtra(SyncConstants.INTENT_EXTRA_LOCAL_FILE);
        String remote = intent.getStringExtra(SyncConstants.INTENT_EXTRA_REMOTE_FILE);
        // check if file is correct
        if (TextUtils.isEmpty(local) || TextUtils.isEmpty(remote)) return;

        // take a file and entries
        File localFile = new File(local);
//        Entry remoteFile = mDropboxHelper.getEntry(remote);
        CloudMetaData remoteMetaData = sync.getProvider().getMetadata(remote);
        // check if local file or remote file is null, then exit
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
//            downloadFile(localFile, remoteFile);
//        } else if (SyncConstants.INTENT_ACTION_UPLOAD.equals(intent.getAction())) {
//            uploadFile(localFile, remoteFile);
//        } else {
//            // Synchronization
//            syncFile(localFile, remoteFile);
//        }
    }
}
