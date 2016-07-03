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
//import com.money.manager.ex.sync.SyncConstants;
//import com.money.manager.ex.sync.SyncManager;
//import com.money.manager.ex.sync.SyncService;
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
//    public Context getContext() {
//        return mContext;
//    }
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
//        runDropbox(DropboxService.INTENT_ACTION_SYNC);
//    }
//
//    public void downloadFromDropbox() {
//        // toast to show
////        Toast.makeText(context.getApplicationContext(), R.string.dropbox_download_is_starting, Toast.LENGTH_LONG).show();
//
//        runDropbox(SyncConstants.INTENT_ACTION_DOWNLOAD);
//    }
//
//    /**
//     * Provides the path to the local file which is currently linked to Dropbox.
//     * @return path to the local database.
//     */
//    public String getLocalPath() {
//        String dropboxFile = mDropboxHelper.getLinkedRemoteFile();
//        String localFile = getExternalStorageDirectoryDropbox().getPath() + dropboxFile;
//
//        return localFile;
//    }
//
//    public void openDownloadedDatabase() {
//        File downloadedDb = new File(this.getLocalPath());
//        DropboxService dropboxService = new DropboxService();
//
//        Intent intent = dropboxService.getIntentForOpenDatabase(mContext, downloadedDb);
//
//        mContext.startActivity(intent);
//    }
//
//    // Private area
//
//    /**
//     * Get dropbox application directory on external storage. The directory is created if it does
//     * not exist.
//     * @return Location for Dropbox files.
//     */
//    private File getExternalStorageDirectoryDropbox() {
//        Core core = new Core(mContext.getApplicationContext());
//        File folder = core.getExternalStorageDirectory();
//        // manage folder
//        if (folder != null && folder.exists() && folder.isDirectory() && folder.canWrite()) {
//            // create a folder for dropbox
//            File folderDropbox = new File(folder + "/dropbox");
//            // check if folder exists otherwise create
//            if (!folderDropbox.exists()) {
//                if (!folderDropbox.mkdirs()) return mContext.getFilesDir();
//            }
//            return folderDropbox;
//        } else {
//            return mContext.getFilesDir();
//        }
//    }
//
//    private void runDropbox(String intentAction) {
//        // Validation.
//        // We need a value in dropbox file name settings.
//        String dropboxFile = mDropboxHelper.getLinkedRemoteFile();
//        if (TextUtils.isEmpty(dropboxFile)) return;
//
//        // Action
//
//        String localFile = getLocalPath();
//
//        Intent service = new Intent(mContext.getApplicationContext(), DropboxService.class);
//
//        service.setAction(intentAction);
//
//        service.putExtra(SyncConstants.INTENT_EXTRA_LOCAL_FILE, localFile);
//        service.putExtra(SyncConstants.INTENT_EXTRA_REMOTE_FILE, dropboxFile);
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
//            Messenger messenger = new SyncMessengerFactory(getContext())
//                    .createMessenger(progressDialog, mDropboxHelper.getLinkedRemoteFile());
//            service.putExtra(SyncService.INTENT_EXTRA_MESSENGER, messenger);
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
//}
