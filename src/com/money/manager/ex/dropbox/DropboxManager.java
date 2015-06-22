/*
 * Copyright (C) 2012-2015 The Android Money Manager Ex Project Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.money.manager.ex.dropbox;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.text.TextUtils;
import android.widget.Toast;

import com.money.manager.ex.MainActivity;
import com.money.manager.ex.MoneyManagerApplication;
import com.money.manager.ex.R;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.core.ExceptionHandler;
import com.money.manager.ex.core.IDropboxManagerCallbacks;
import com.money.manager.ex.dropbox.DropboxHelper;
import com.money.manager.ex.dropbox.DropboxServiceIntent;

import java.io.File;

/**
 * Handles the background Dropbox service and provides feedback to the UI.
 */
public class DropboxManager {

    public DropboxManager(Context context, DropboxHelper dropboxHelper, IDropboxManagerCallbacks callbacks) {
        mContext = context;
        mDropboxHelper = dropboxHelper;
        mCallbacks = callbacks;
    }

    private Context mContext;
    private DropboxHelper mDropboxHelper;
    private IDropboxManagerCallbacks mCallbacks;

    public void synchronizeDropbox() {
        if (mDropboxHelper == null || !mDropboxHelper.isLinked())  return;

        // Make sure that the current database is also the one linked to Dropbox.
        String currentDatabasePath = MoneyManagerApplication.getDatabasePath(mContext.getApplicationContext());
        if (TextUtils.isEmpty(currentDatabasePath)) {
            return;
        }

        String dropboxFile = mDropboxHelper.getLinkedRemoteFile();
        if (TextUtils.isEmpty(dropboxFile)) {
            Toast.makeText(mContext, R.string.dropbox_select_file, Toast.LENGTH_SHORT).show();
            return;
        }

        // easy comparison
        if (!currentDatabasePath.contains(dropboxFile)) {
            // The current file was probably opened through Open Database.
            Toast.makeText(mContext, R.string.db_not_dropbox, Toast.LENGTH_LONG).show();
            return;
        }

        runDropbox(DropboxServiceIntent.INTENT_ACTION_SYNC);
    }

    public void downloadFromDropbox() {
        // toast to show
//        Toast.makeText(mContext.getApplicationContext(), R.string.dropbox_download_is_starting, Toast.LENGTH_LONG).show();

        runDropbox(DropboxServiceIntent.INTENT_ACTION_DOWNLOAD);
    }

    public String getDropboxFileSetting() {
        return mDropboxHelper.getLinkedRemoteFile();
    }

    /**
     * Provides the path to the local file which is currently linked to Dropbox.
     * @return
     */
    public String getLocalDropboxFile() {
        String dropboxFile = getDropboxFileSetting();
        Core core = new Core(mContext.getApplicationContext());

        String localFile = core.getExternalStorageDirectoryDropboxApplication().getPath() + dropboxFile;

        return localFile;
    }

    public void openDownloadedDatabase() {
        File downloadedDb = new File(this.getLocalDropboxFile());
        DropboxServiceIntent dropboxService = new DropboxServiceIntent();

        Intent intent = dropboxService.getIntentForOpenDatabase(mContext, downloadedDb);

        mContext.startActivity(intent);
    }

    private void runDropbox(String intentAction) {
        // Validation.
        // We need a value in dropbox file name settings.
        String dropboxFile = getDropboxFileSetting();
        if (TextUtils.isEmpty(dropboxFile)) return;

        String localFile = getLocalDropboxFile();

        Intent service = new Intent(mContext.getApplicationContext(), DropboxServiceIntent.class);

        service.setAction(intentAction);

        service.putExtra(DropboxServiceIntent.INTENT_EXTRA_LOCAL_FILE, localFile);
        service.putExtra(DropboxServiceIntent.INTENT_EXTRA_REMOTE_FILE, dropboxFile);

        ProgressDialog progressDialog;
        try {
            //progress dialog
            progressDialog = new ProgressDialog(mContext);
            progressDialog.setCancelable(false);
            progressDialog.setMessage(mContext.getString(R.string.dropbox_syncProgress));
            progressDialog.setIndeterminate(true);
            progressDialog.show();

            Messenger messenger = createMessenger(progressDialog);
            service.putExtra(DropboxServiceIntent.INTENT_EXTRA_MESSENGER, messenger);
        } catch (Exception ex) {
            ExceptionHandler handler = new ExceptionHandler(mContext, this);
            handler.handle(ex, "Error displaying dropbox progress dialog");
        }

        // start service
        mContext.startService(service);

        // once done, the message is sent out via messenger.
    }

    private Messenger createMessenger(final ProgressDialog progressDialog) {
        Messenger messenger = new Messenger(new Handler() {
            @Override
            public void handleMessage(Message msg) {
                final Activity parent = (Activity) mContext;

                if (msg.what == DropboxServiceIntent.INTENT_EXTRA_MESSENGER_NOT_CHANGE) {
                    // close dialog
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.hide();
                    }

                    parent.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(mContext, R.string.dropbox_database_is_synchronized, Toast.LENGTH_LONG).show();
                        }
                    });
                } else if (msg.what == DropboxServiceIntent.INTENT_EXTRA_MESSENGER_START_DOWNLOAD) {
                    parent.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(mContext, R.string.dropbox_download_is_starting, Toast.LENGTH_LONG).show();
                        }
                    });
                } else if (msg.what == DropboxServiceIntent.INTENT_EXTRA_MESSENGER_DOWNLOAD) {
                    // Download from Dropbox completed.
                    // close dialog
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.hide();
                    }
                    // Notify whoever is interested.
                    mCallbacks.onFileDownloaded();
                } else if (msg.what == DropboxServiceIntent.INTENT_EXTRA_MESSENGER_START_UPLOAD) {
                    parent.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(mContext, R.string.dropbox_upload_is_starting, Toast.LENGTH_LONG).show();
                        }
                    });
                } else if (msg.what == DropboxServiceIntent.INTENT_EXTRA_MESSENGER_UPLOAD) {
                    // close dialog
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.hide();
                    }

                    parent.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(mContext, R.string.upload_file_to_dropbox_complete, Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
        return messenger;
    }
}
