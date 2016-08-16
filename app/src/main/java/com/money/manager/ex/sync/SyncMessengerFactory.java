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
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.widget.Toast;

import com.money.manager.ex.R;
import com.money.manager.ex.sync.events.DbFileDownloadedEvent;
import com.money.manager.ex.home.RecentDatabaseEntry;
import com.money.manager.ex.home.RecentDatabasesProvider;
import com.money.manager.ex.sync.SyncManager;
import com.money.manager.ex.sync.SyncMessages;
import com.money.manager.ex.utils.DialogUtils;

import org.greenrobot.eventbus.EventBus;

/**
 * Creates a Messenger
 */
public class SyncMessengerFactory {
    public SyncMessengerFactory(Context context) {
        this.context = context;
    }

    private Context context;

    public Context getContext() {
        return this.context;
    }

    public Messenger createMessenger(final ProgressDialog progressDialog, final String remoteFile) {
        // Handler can be used only when running in a Looper.
        if (!(getContext() instanceof Activity)) return null;

        // Messenger handles received messages from the sync service.
        Messenger messenger = new Messenger(new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == SyncMessages.NOT_ON_WIFI) {
                    //showMessage();
                    closeDialog(progressDialog);

                } else if (msg.what == SyncMessages.FILE_NOT_CHANGED) {
                    // close dialog
                    closeDialog(progressDialog);
                    showMessage(R.string.database_is_synchronized, Toast.LENGTH_LONG);

                } else if (msg.what == SyncMessages.STARTING_DOWNLOAD) {
                    showMessage(R.string.sync_downloading, Toast.LENGTH_LONG);

                } else if (msg.what == SyncMessages.DOWNLOAD_COMPLETE) {
                    // Download from Dropbox completed.
                    storeRecentDb(remoteFile);
                    // close dialog
                    closeDialog(progressDialog);
                    // Notify whoever is interested.
                    EventBus.getDefault().post(new DbFileDownloadedEvent());

                } else if (msg.what == SyncMessages.STARTING_UPLOAD) {
                    // Do not block the user if uploading the changes.
                    closeDialog(progressDialog);
                    showMessage(R.string.sync_uploading, Toast.LENGTH_LONG);

                } else if (msg.what == SyncMessages.UPLOAD_COMPLETE) {
                    // close dialog
                    closeDialog(progressDialog);
                    showMessage(R.string.upload_file_complete, Toast.LENGTH_LONG);
                } else if (msg.what == SyncMessages.ERROR) {
                    closeDialog(progressDialog);
                    showMessage(R.string.error, Toast.LENGTH_SHORT);
                }
            }
        });
        return messenger;
    }

    private void closeDialog(ProgressDialog progressDialog) {
        if (progressDialog != null && progressDialog.isShowing()) {
            DialogUtils.closeProgressDialog(progressDialog);
        }
    }

    private void showMessage(final int message, final int length) {
        Context context = getContext();
        if (!(context instanceof Activity)) return;

        final Activity parent = (Activity) context;

        parent.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getContext(), message, length).show();
            }
        });
    }

    private void storeRecentDb(String remoteFile) {
        RecentDatabasesProvider recents = new RecentDatabasesProvider(getContext());

        String localPath = new SyncManager(getContext()).getLocalPath();
        RecentDatabaseEntry entry = RecentDatabaseEntry.getInstance(localPath, true, remoteFile);

        recents.add(entry);
    }
}
