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
import android.widget.Toast;

import com.money.manager.ex.R;
import com.money.manager.ex.core.UIHelper;
import com.money.manager.ex.home.RecentDatabaseEntry;
import com.money.manager.ex.home.RecentDatabasesProvider;
import com.money.manager.ex.sync.events.DbFileDownloadedEvent;
import com.money.manager.ex.utils.DialogUtils;

import org.greenrobot.eventbus.EventBus;

/**
 * Handler for the messages received from the sync service.
 * Updates the UI based on the messages received. The messages state the progres of the
 * synchronisation.
 */

public class SyncServiceMessageHandler
    extends Handler {

    public SyncServiceMessageHandler(Context context, ProgressDialog progressDialog, String remoteFile) {
        this.context = context;
        this.progressDialog = progressDialog;
        this.remoteFile = remoteFile;
    }

    private Context context;
    private ProgressDialog progressDialog;
    private String remoteFile;

    @Override
    public void handleMessage(Message msg) {
        SyncServiceMessage message = SyncServiceMessage.parse(msg.what);
        if (message == null) return;

        switch (message) {
            case NOT_ON_WIFI:
                //showMessage();
                closeDialog(progressDialog);
                break;

            case FILE_NOT_CHANGED:
                // close dialog
                closeDialog(progressDialog);
                new UIHelper(getContext()).showToast(R.string.database_is_synchronized, Toast.LENGTH_LONG);
                break;

            case STARTING_DOWNLOAD:
                new UIHelper(getContext()).showToast(R.string.sync_downloading, Toast.LENGTH_LONG);
                break;

            case DOWNLOAD_COMPLETE:
                storeRecentDb(remoteFile);
                // close dialog
                closeDialog(progressDialog);
                // Notify whoever is interested.
                EventBus.getDefault().post(new DbFileDownloadedEvent());
                break;

            case STARTING_UPLOAD:
                // Do not block the user if uploading the changes.
                closeDialog(progressDialog);
                new UIHelper(getContext()).showToast(R.string.sync_uploading, Toast.LENGTH_LONG);
                break;

            case UPLOAD_COMPLETE:
                // close dialog
                closeDialog(progressDialog);
                new UIHelper(getContext()).showToast(R.string.upload_file_complete, Toast.LENGTH_LONG);
                break;

            case ERROR:
                closeDialog(progressDialog);
                new UIHelper(getContext()).showToast(R.string.error, Toast.LENGTH_SHORT);
                break;

            default:
                throw new RuntimeException("unknown message");
        }
    }

    public Context getContext() {
        return this.context;
    }

    private void closeDialog(ProgressDialog progressDialog) {
        if (progressDialog != null && progressDialog.isShowing()) {
            DialogUtils.closeProgressDialog(progressDialog);
        }
    }

    private void storeRecentDb(String remoteFile) {
        RecentDatabasesProvider recents = new RecentDatabasesProvider(getContext());

        String localPath = new SyncManager(getContext()).getLocalPath();
        RecentDatabaseEntry entry = RecentDatabaseEntry.getInstance(localPath, true, remoteFile);

        recents.add(entry);
    }

}