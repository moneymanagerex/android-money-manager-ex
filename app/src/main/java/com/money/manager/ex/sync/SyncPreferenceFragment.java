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
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

import com.money.manager.ex.R;

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
        addPreferencesFromResource(R.xml.settings_sync);

        viewHolder = new SyncPreferencesViewHolder(this);

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

    private void handleFileSelection(int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK || data == null) return;

        // get value
        String remoteFile = data.getStringExtra(SyncPreferenceFragment.EXTRA_REMOTE_FILE);

        // save selection into preferences
        getSyncManager().setRemoteFile(remoteFile);

        // show selected value
        viewHolder.remoteFile.setSummary(remoteFile);

        // todo:
        // add history record (recent files)
        // download db from the cloud storage
//        if (!oldFile.equals(newFile)) {
//            // force download file
//            downloadFileFromDropbox();
//        }
        // open db file
    }

    private SyncManager getSyncManager() {
        if (mSyncManager == null) {
            mSyncManager = new SyncManager(getActivity());
        }
        return mSyncManager;
    }
}
