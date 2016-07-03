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
import android.content.Intent;
import android.os.Bundle;
import android.os.Messenger;
import android.support.v4.app.Fragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;

import com.money.manager.ex.R;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.core.ExceptionHandler;
import com.money.manager.ex.dropbox.SyncMessengerFactory;
import com.money.manager.ex.settings.PreferenceConstants;

/**
 * A simple {@link Fragment} subclass.
 */
public class SyncPreferenceFragment
    extends PreferenceFragmentCompat {

    public static final int REQUEST_REMOTE_FILE = 1;
    public static final String EXTRA_REMOTE_FILE = "remote_file";

    public SyncPreferenceFragment() {
        // Required empty public constructor
    }

    private SyncPreferencesViewHolder viewHolder;
    private SyncManager mSyncManager;

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        // Use own preference file.
        PreferenceManager prefMgr = getPreferenceManager();
        prefMgr.setSharedPreferencesName(PreferenceConstants.SYNC_PREFERENCES);
        prefMgr.setSharedPreferencesMode(Context.MODE_PRIVATE); // MODE_WORLD_READABLE

        addPreferencesFromResource(R.xml.settings_sync);

        initializePreferences();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_REMOTE_FILE:
                handleFileSelection(resultCode, data);
                break;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        // this is probably redundant now.
        getSyncManager().storePersistent();
    }

    private void handleFileSelection(int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK || data == null) return;

        // get value
        String remoteFile = data.getStringExtra(SyncPreferenceFragment.EXTRA_REMOTE_FILE);

        // save selection into preferences
        getSyncManager().setRemoteFile(remoteFile);

        // show selected value
        viewHolder.remoteFile.setSummary(remoteFile);

        // todo download db from the cloud storage
//        if (!oldFile.equals(newFile)) {
//            // force download file
//            downloadFileFromDropbox();
//        }

        // start sync service
        getSyncManager().startSyncService();

        // todo open db file?
        // todo add history record (recent files)?

//        getSyncManager().storePersistent();
    }

    private SyncManager getSyncManager() {
        if (mSyncManager == null) {
            mSyncManager = new SyncManager(getActivity());
        }
        return mSyncManager;
    }

    private void initializePreferences() {
        viewHolder = new SyncPreferencesViewHolder(this);

        viewHolder.syncEnabled.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                // switch the sync heartbeat
                Boolean enabled = (Boolean) o;
                getSyncManager().setEnabled(enabled);
                if (enabled) {
                    getSyncManager().startSyncService();
                } else {
                    getSyncManager().stopSyncService();
                }
                return true;
            }
        });

        viewHolder.providerList.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                SyncManager sync = getSyncManager();
                // set the new provider
                sync.setProvider(CloudStorageProviderEnum.valueOf(o.toString()));
                // log in to the provider immediately and save to persistence.
                sync.login();
                sync.storePersistent();

                return true;
            }
        });

        // remote file
        viewHolder.remoteFile.setSummary(getSyncManager().getRemotePath());
        viewHolder.remoteFile.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                // show the file browser/picker.
                Intent intent = new Intent(getActivity(), CloudFilePickerActivity.class);
                startActivityForResult(intent, REQUEST_REMOTE_FILE);

                return false;
            }
        });

        // synchronization interval
        viewHolder.syncInterval.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                // reset timer.
                // todo: check if this uses the correct (new) values.
                getSyncManager().stopSyncService();
                getSyncManager().startSyncService();
                return true;
            }
        });

        // download
        viewHolder.download.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                forceDownload();
                return true;
            }
        });

        // reset preferences
        viewHolder.resetPreferences.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                SyncManager sync = getSyncManager();
                sync.resetPreferences();
                sync.stopSyncService();
                sync.logout();

                Core.alertDialog(getActivity(), R.string.preferences_reset);
                return false;
            }
        });
    }

    public void forceDownload() {
        SyncManager sync = getSyncManager();
        Context context = getActivity();

        // Validation.
        String remoteFile = sync.getRemotePath();
        // We need a value in remote file name settings.
        if (TextUtils.isEmpty(remoteFile)) return;

        // Action

        String localFile = sync.getLocalPath();

        Intent service = new Intent(context, SyncService.class);

        service.setAction(SyncConstants.INTENT_ACTION_DOWNLOAD);

        service.putExtra(SyncConstants.INTENT_EXTRA_LOCAL_FILE, localFile);
        service.putExtra(SyncConstants.INTENT_EXTRA_REMOTE_FILE, remoteFile);

        ProgressDialog progressDialog;
        try {
            //progress dialog
            progressDialog = new ProgressDialog(context);
            progressDialog.setCancelable(false);
            progressDialog.setMessage(context.getString(R.string.dropbox_syncProgress));
            progressDialog.setIndeterminate(true);
            progressDialog.show();

            Messenger messenger = new SyncMessengerFactory(context).createMessenger(progressDialog, sync.getRemotePath());
            service.putExtra(SyncService.INTENT_EXTRA_MESSENGER, messenger);
        } catch (Exception ex) {
            ExceptionHandler handler = new ExceptionHandler(context, this);
            handler.handle(ex, "displaying dropbox progress dialog");
        }

        // start service
        context.startService(service);

        // once done, the message is sent out via messenger. See Messenger definition below.
        // INTENT_EXTRA_MESSENGER_DOWNLOAD
    }
}
