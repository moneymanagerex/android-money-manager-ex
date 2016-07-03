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
import android.support.v4.app.Fragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;

import com.money.manager.ex.R;
import com.money.manager.ex.core.Core;
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
        // todo start sync service
//        mDropboxHelper.sendBroadcastStartServiceScheduled(SyncSchedulerBroadcastReceiver.ACTION_START);

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

        viewHolder.providerList.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                // log in to the provider immediately and save to persistence.
                getSyncManager().login();
                getSyncManager().storePersistent();

                return true;
            }
        });

        viewHolder.remoteFile.setSummary(getSyncManager().getRemoteFile());

        viewHolder.remoteFile.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                // show the file browser/picker.
                Intent intent = new Intent(getActivity(), CloudFilePickerActivity.class);
                startActivityForResult(intent, REQUEST_REMOTE_FILE);

                return false;
            }
        });

        viewHolder.resetPreferences.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                getSyncManager().resetPreferences();
                //todo: stop sync
                // mDropboxHelper.sendBroadcastStartServiceScheduled(SyncSchedulerBroadcastReceiver.ACTION_CANCEL);
                Core.alertDialog(getActivity(), R.string.preferences_reset);
                return false;
            }
        });
    }
}
