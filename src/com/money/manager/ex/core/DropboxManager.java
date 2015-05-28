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
package com.money.manager.ex.core;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.widget.Toast;

import com.money.manager.ex.MainActivity;
import com.money.manager.ex.MoneyManagerApplication;
import com.money.manager.ex.R;
import com.money.manager.ex.dropbox.DropboxHelper;
import com.money.manager.ex.dropbox.DropboxServiceIntent;

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

    public void startServiceSyncDropbox() {
        if (mDropboxHelper != null && mDropboxHelper.isLinked()) {
            // Make sure that the current database is also Dropbox-linked.
            String currentDatabasePath = MoneyManagerApplication.getDatabasePath(mContext.getApplicationContext());
            String dropboxPath = mDropboxHelper.getLinkedRemoteFile();
            // easy comparison
            if (!currentDatabasePath.contains(dropboxPath)) {
                // The current file was probably opened through Open Database.
                Toast.makeText(mContext, R.string.db_not_dropbox, Toast.LENGTH_LONG).show();
                return;
            }

            Intent service = new Intent(mContext.getApplicationContext(), DropboxServiceIntent.class);
            service.setAction(DropboxServiceIntent.INTENT_ACTION_SYNC);
            service.putExtra(DropboxServiceIntent.INTENT_EXTRA_LOCAL_FILE, currentDatabasePath);
            service.putExtra(DropboxServiceIntent.INTENT_EXTRA_REMOTE_FILE, dropboxPath);

            //progress dialog
            final ProgressDialog progressDialog = new ProgressDialog(mContext);
            progressDialog.setCancelable(false);
            progressDialog.setMessage(mContext.getString(R.string.dropbox_syncProgress));
            progressDialog.setIndeterminate(true);
            progressDialog.show();
            //create a messenger
            Messenger messenger = new Messenger(new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    final Activity parent = (Activity) mContext;

                    if (msg.what == DropboxServiceIntent.INTENT_EXTRA_MESSENGER_NOT_CHANGE) {
                        // close dialog
                        if (progressDialog != null && progressDialog.isShowing())
                            progressDialog.hide();

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
                        // close dialog
                        if (progressDialog != null && progressDialog.isShowing()) {
                            progressDialog.hide();
                        }
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
                        if (progressDialog != null && progressDialog.isShowing())
                            progressDialog.hide();

                        parent.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(mContext, R.string.upload_file_to_dropbox_complete, Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
            });
            service.putExtra(DropboxServiceIntent.INTENT_EXTRA_MESSENGER, messenger);

            mContext.startService(service);
        }
    }

}
