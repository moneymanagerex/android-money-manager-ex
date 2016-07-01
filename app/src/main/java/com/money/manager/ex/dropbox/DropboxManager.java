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
//package com.money.manager.ex.dropbox;
//
//import android.app.Activity;
//import android.app.ProgressDialog;
//import android.content.Context;
//import android.content.Intent;
//import android.os.Handler;
//import android.os.Message;
//import android.os.Messenger;
//import android.text.TextUtils;
//import android.widget.Toast;
//
//import com.money.manager.ex.MoneyManagerApplication;
//import com.money.manager.ex.R;
//import com.money.manager.ex.core.Core;
//import com.money.manager.ex.core.ExceptionHandler;
//import com.money.manager.ex.dropbox.events.DbFileDownloadedEvent;
//import com.money.manager.ex.home.RecentDatabaseEntry;
//import com.money.manager.ex.home.RecentDatabasesProvider;
//import com.money.manager.ex.utils.DialogUtils;
//
//import org.greenrobot.eventbus.EventBus;
//
//import java.io.File;
//
///**
// * Handles the background Dropbox service and provides feedback to the UI.
// */
//public class DropboxManager {
//
//    public DropboxManager(Context context, DropboxHelper dropboxHelper) {
//        mContext = context;
//        mDropboxHelper = dropboxHelper;
//    }
//
//    private Context mContext;
//    private DropboxHelper mDropboxHelper;
//
//    public void synchronizeDropbox() {
//        if (mDropboxHelper == null || !mDropboxHelper.isLinked())  return;
//
//        // Make sure that the current database is also the one linked to Dropbox.
//        String currentDatabasePath = MoneyManagerApplication.getDatabasePath(mContext.getApplicationContext());
//        if (TextUtils.isEmpty(currentDatabasePath)) {
//            return;
//        }
//
//        String dropboxFile = mDropboxHelper.getLinkedRemoteFile();
//        if (TextUtils.isEmpty(dropboxFile)) {
//            Toast.makeText(mContext, R.string.dropbox_select_file, Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        // easy comparison
//        if (!currentDatabasePath.contains(dropboxFile)) {
//            // The current file was probably opened through Open Database.
//            Toast.makeText(mContext, R.string.db_not_dropbox, Toast.LENGTH_LONG).show();
//            return;
//        }
//
//        runDropbox(DropboxServiceIntent.INTENT_ACTION_SYNC);
//    }
//
//    public void downloadFromDropbox() {
//        // toast to show
////        Toast.makeText(context.getApplicationContext(), R.string.dropbox_download_is_starting, Toast.LENGTH_LONG).show();
//
//        runDropbox(DropboxServiceIntent.INTENT_ACTION_DOWNLOAD);
//    }
//
//    public String getDropboxFilePath() {
//        return mDropboxHelper.getLinkedRemoteFile();
//    }
//
//    /**
//     * Provides the path to the local file which is currently linked to Dropbox.
//     * @return path to the local database.
//     */
//    public String getLocalDatabasePath() {
//        String dropboxFile = getDropboxFilePath();
//        Core core = new Core(mContext.getApplicationContext());
//
//        String localFile = core.getExternalStorageDirectoryDropboxApplication().getPath() + dropboxFile;
//
//        return localFile;
//    }
//
//    public void openDownloadedDatabase() {
//        File downloadedDb = new File(this.getLocalDatabasePath());
//        DropboxServiceIntent dropboxService = new DropboxServiceIntent();
//
//        Intent intent = dropboxService.getIntentForOpenDatabase(mContext, downloadedDb);
//
//        mContext.startActivity(intent);
//    }
//
//    // Private area
//
//    private void runDropbox(String intentAction) {
//        // Validation.
//        // We need a value in dropbox file name settings.
//        String dropboxFile = getDropboxFilePath();
//        if (TextUtils.isEmpty(dropboxFile)) return;
//
//        // Action
//
//        String localFile = getLocalDatabasePath();
//
//        Intent service = new Intent(mContext.getApplicationContext(), DropboxServiceIntent.class);
//
//        service.setAction(intentAction);
//
//        service.putExtra(DropboxServiceIntent.INTENT_EXTRA_LOCAL_FILE, localFile);
//        service.putExtra(DropboxServiceIntent.INTENT_EXTRA_REMOTE_FILE, dropboxFile);
//
//        ProgressDialog progressDialog;
//        try {
//            //progress dialog
//            progressDialog = new ProgressDialog(mContext);
//            progressDialog.setCancelable(false);
//            progressDialog.setMessage(mContext.getString(R.string.dropbox_syncProgress));
//            progressDialog.setIndeterminate(true);
//            progressDialog.show();
//
//            Messenger messenger = createMessenger(progressDialog);
//            service.putExtra(DropboxServiceIntent.INTENT_EXTRA_MESSENGER, messenger);
//        } catch (Exception ex) {
//            ExceptionHandler handler = new ExceptionHandler(mContext, this);
//            handler.handle(ex, "displaying dropbox progress dialog");
//        }
//
//        // start service
//        mContext.startService(service);
//
//        // once done, the message is sent out via messenger. See Messenger definition below.
//        // INTENT_EXTRA_MESSENGER_DOWNLOAD
//    }
//
//    private Messenger createMessenger(final ProgressDialog progressDialog) {
//        // Messenger handles received messages from the Dropbox service.
//        Messenger messenger = new Messenger(new Handler() {
//            @Override
//            public void handleMessage(Message msg) {
//                if (msg.what == DropboxServiceIntent.INTENT_EXTRA_MESSENGER_NOT_ON_WIFI) {
//                    //showMessage();
//                    closeDialog(progressDialog);
//
//                } else if (msg.what == DropboxServiceIntent.INTENT_EXTRA_MESSENGER_NOT_CHANGE) {
//                    // close dialog
//                    closeDialog(progressDialog);
//                    showMessage(R.string.dropbox_database_is_synchronized, Toast.LENGTH_LONG);
//
//                } else if (msg.what == DropboxServiceIntent.INTENT_EXTRA_MESSENGER_START_DOWNLOAD) {
//                    showMessage(R.string.dropbox_download_is_starting, Toast.LENGTH_LONG);
//
//                } else if (msg.what == DropboxServiceIntent.INTENT_EXTRA_MESSENGER_DOWNLOAD) {
//                    // Download from Dropbox completed.
//                    storeRecentDb();
//                    // close dialog
//                    closeDialog(progressDialog);
//                    // Notify whoever is interested.
//                    EventBus.getDefault().post(new DbFileDownloadedEvent());
//
//                } else if (msg.what == DropboxServiceIntent.INTENT_EXTRA_MESSENGER_START_UPLOAD) {
//                    showMessage(R.string.dropbox_upload_is_starting, Toast.LENGTH_LONG);
//
//                } else if (msg.what == DropboxServiceIntent.INTENT_EXTRA_MESSENGER_UPLOAD) {
//                    // close dialog
//                    closeDialog(progressDialog);
//                    showMessage(R.string.upload_file_to_dropbox_complete, Toast.LENGTH_LONG);
//                }
//            }
//        });
//        return messenger;
//    }
//
//    private void showMessage(final int message, final int length) {
//        final Activity parent = (Activity) mContext;
//
//        parent.runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                Toast.makeText(mContext, message, length).show();
//            }
//        });
//    }
//
//    private void closeDialog(ProgressDialog progressDialog) {
//        if (progressDialog != null && progressDialog.isShowing()) {
//            DialogUtils.closeProgressDialog(progressDialog);
//        }
//    }
//
//    private void storeRecentDb() {
//        RecentDatabasesProvider recents = new RecentDatabasesProvider(mContext);
//        RecentDatabaseEntry entry = RecentDatabaseEntry.getInstance(getLocalDatabasePath(), true,
//                getDropboxFilePath());
//        recents.add(entry);
//    }
//}
