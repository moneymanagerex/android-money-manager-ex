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


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.money.manager.ex.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class SyncPreferenceFragment
    extends PreferenceFragmentCompat {

    public SyncPreferenceFragment() {
        // Required empty public constructor
    }

    private SyncPreferencesViewHolder mViewHolder;

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.settings_sync);

        mViewHolder = new SyncPreferencesViewHolder(this);

        mViewHolder.remoteFile.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                // show the file browser/picker.
                Intent intent = new Intent(getActivity(), CloudFilePickerActivity.class);
                //intent.putExtra(CloudFilePickerActivity.INTENT_DROBPOXFILE_PATH, mDropboxHelper.getLinkedRemoteFile());
                //todo startActivityForResult(intent, REQUEST_DROPBOX_FILE);
                startActivity(intent);

                return false;
            }
        });
    }

}
