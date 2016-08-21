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

package com.money.manager.ex.sync.adapter;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
//import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.text.TextUtils;

import com.cloudrail.si.types.CloudMetaData;
import com.money.manager.ex.settings.AppSettings;
import com.money.manager.ex.sync.SyncManager;

import org.joda.time.DateTime;

import java.io.File;

import timber.log.Timber;

/**
 * Handle the transfer of data between a server and an
 * app, using the Android sync adapter framework.
 * https://developer.android.com/training/sync-adapters/creating-sync-adapter.html
 */
public class SyncAdapter
    extends AbstractThreadedSyncAdapter {

    // Global variables
    // Define a variable to contain a content resolver instance
//    ContentResolver mContentResolver;

    /**
     * Set up the sync adapter
     */
    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        /*
         * If your app uses a content resolver, get an instance of it
         * from the incoming Context
         */
//        mContentResolver = context.getContentResolver();
    }

    /**
     * Set up the sync adapter. This form of the
     * constructor maintains compatibility with Android 3.0
     * and later platform versions
     */
    public SyncAdapter(
            Context context,
            boolean autoInitialize,
            boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        /*
         * If your app uses a content resolver, get an instance of it
         * from the incoming Context
         */
//        mContentResolver = context.getContentResolver();

    }

    /**
     * Transfer code goes here.
     * @param account An Account object associated with the event that triggered the sync adapter. If your server doesn't use accounts, you don't need to use the information in this object.
     * @param bundle A Bundle containing flags sent by the event that triggered the sync adapter.
     * @param s The authority of a content provider in the system. Your app has to have access to this provider. Usually, the authority corresponds to a content provider in your own app.
     * @param contentProviderClient A ContentProviderClient for the content provider pointed to by the authority argument.
     *                              A ContentProviderClient is a lightweight public interface to a content provider.
     *                              It has the same basic functionality as a ContentResolver.
     *                              If you're using a content provider to store data for your app, you can connect to the provider with this object. Otherwise, you can ignore it.
     * @param syncResult
     */
    @Override
    public void onPerformSync(Account account, Bundle bundle, String s,
                              ContentProviderClient contentProviderClient, SyncResult syncResult) {
        Timber.d("synchronizing!");

        // todo update syncResult object with info

        SyncManager sync = new SyncManager(getContext());
        AppSettings settings = new AppSettings(getContext());

//        String localFilename = settings.getDatabaseSettings().getDatabasePath();
        String localFilename = sync.getLocalPath();
        String remoteFilename = sync.getRemotePath();

        // check if file is correct
        if (TextUtils.isEmpty(localFilename) || TextUtils.isEmpty(remoteFilename)) return;

        File localFile = new File(localFilename);
        CloudMetaData remoteFile = sync.loadMetadata(remoteFilename);
        if (remoteFile == null) {
//            sendMessage(SyncServiceMessage.ERROR);
            return;
        }

        // check if name is same
        if (!localFile.getName().toLowerCase().equals(remoteFile.getName().toLowerCase())) {
            Timber.w("Local filename different from the remote!");
//            sendMessage(SyncServiceMessage.ERROR);
            return;
        }

        triggerSync(localFile, remoteFile);
    }

    private void triggerSync(final File localFile, CloudMetaData remoteFile) {
        SyncManager sync = new SyncManager(getContext());

        DateTime localLastModified = sync.getCachedLastModifiedDateFor(remoteFile);
        if (localLastModified == null) localLastModified = new DateTime(localFile.lastModified());

        DateTime remoteLastModified = new DateTime(remoteFile.getModifiedAt());

        Timber.d("Local file last modified: %s", localLastModified.toString());
        Timber.d("Remote file last modified: %s", remoteLastModified.toString());

        // check date
        if (remoteLastModified.isAfter(localLastModified)) {
            Timber.d("Download %s from the cloud storage.", remoteFile.getPath());
            // download file
            // todo triggerDownload(localFile, remoteFile);
        } else if (remoteLastModified.isBefore(localLastModified)) {
            Timber.d("Upload %s to the cloud storage.", localFile.getPath());
            // upload file
            // todo triggerUpload(localFile, remoteFile);
        } else {
            Timber.d("The local and remote files are the same.");

//            sendMessage(SyncServiceMessage.FILE_NOT_CHANGED);
        }
    }

}