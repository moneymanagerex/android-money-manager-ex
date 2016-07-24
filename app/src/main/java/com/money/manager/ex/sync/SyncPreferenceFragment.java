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
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
//import android.support.v7.preference.Preference;
//import android.support.v7.preference.PreferenceFragmentCompat;
//import android.support.v7.preference.PreferenceManager;

import android.widget.Toast;

import com.money.manager.ex.MoneyManagerApplication;
import com.money.manager.ex.R;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.sync.events.DbFileDownloadedEvent;
import com.money.manager.ex.settings.PreferenceConstants;
import com.money.manager.ex.settings.events.AppRestartRequiredEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

/**
 * A simple {@link Fragment} subclass.
 */
public class SyncPreferenceFragment
    extends PreferenceFragment { // Compat

    public static final int REQUEST_REMOTE_FILE = 1;
    public static final String EXTRA_REMOTE_FILE = "remote_file";

    public SyncPreferenceFragment() {
        // Required empty public constructor
    }

    private SyncPreferencesViewHolder viewHolder;
    private SyncManager mSyncManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Use own preference file.
        PreferenceManager prefMgr = getPreferenceManager();
        prefMgr.setSharedPreferencesName(PreferenceConstants.SYNC_PREFERENCES);
        prefMgr.setSharedPreferencesMode(Context.MODE_PRIVATE); // MODE_WORLD_READABLE

        addPreferencesFromResource(R.xml.settings_sync);

        initializePreferences();

    }

//    @Override
//    public void onCreatePreferences(Bundle bundle, String s) {
//    }

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
    public void onStart() {
        super.onStart();

        // register as event bus listener
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        // this is probably redundant now.
        getSyncManager().storePersistent();

        EventBus.getDefault().unregister(this);
    }

    /**
     * Called when file is downloaded from the cloud storage.
     */
    @Subscribe
    public void onEvent(DbFileDownloadedEvent event) {
        // set main activity to reload.
//        MainActivity.setRestartActivity(true);
        EventBus.getDefault().post(new AppRestartRequiredEvent());

        // open the new database.
        getSyncManager().openDatabase();
    }

    /*
        Private
     */

    private void handleFileSelection(int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK || data == null) return;

        // get value
        String remoteFile = data.getStringExtra(SyncPreferenceFragment.EXTRA_REMOTE_FILE);

        // show selected value
        viewHolder.remoteFile.setSummary(remoteFile);

        SyncManager sync = getSyncManager();

        String previousFile = sync.getRemotePath();

        // save selection into preferences
        sync.setRemotePath(remoteFile);

        // start sync service
        getSyncManager().startSyncService();

        // download db from the cloud storage
        if (!remoteFile.equals(previousFile)) {
            // force download file
            sync.triggerDownload();
        }
    }

    private SyncManager getSyncManager() {
        if (mSyncManager == null) {
            mSyncManager = new SyncManager(getActivity());
        }
        return mSyncManager;
    }

    private void initializePreferences() {
        viewHolder = new SyncPreferencesViewHolder(this);

        // enable/disable sync.
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

        // provider
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
                SyncManager sync = getSyncManager();
                sync.setSyncInterval(Integer.parseInt(o.toString()));

                sync.stopSyncService();
                sync.startSyncService();
                return true;
            }
        });

        // Download.

        viewHolder.download.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                SyncManager sync = getSyncManager();
                sync.triggerDownload();
                return true;
            }
        });

        // Upload.

        viewHolder.upload.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                forceUpload();
                return false;
            }
        });

        // reset preferences

        viewHolder.resetPreferences.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                SyncManager sync = getSyncManager();
                sync.logout();
                sync.resetPreferences();
                sync.stopSyncService();

                Core.alertDialog(getActivity(), R.string.preferences_reset);

                getActivity().recreate();

                return false;
            }
        });
    }

    private void forceUpload() {
        String localFile = MoneyManagerApplication.getDatabasePath(getActivity());
        String remoteFile = getSyncManager().getRemotePath();

        // trigger upload
        Intent service = new Intent(getActivity().getApplicationContext(), SyncService.class);
        service.setAction(SyncConstants.INTENT_ACTION_UPLOAD);
        service.putExtra(SyncConstants.INTENT_EXTRA_LOCAL_FILE, localFile);
        service.putExtra(SyncConstants.INTENT_EXTRA_REMOTE_FILE, remoteFile);
        // toast to show
        Toast.makeText(getActivity().getApplicationContext(), R.string.sync_uploading, Toast.LENGTH_LONG).show();
        // start service
        getActivity().startService(service);
    }
}
