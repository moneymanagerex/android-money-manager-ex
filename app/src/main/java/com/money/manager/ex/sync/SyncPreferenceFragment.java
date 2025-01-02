/*
 * Copyright (C) 2012-2018 The Android Money Manager Ex Project Team
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

import static android.content.Context.MODE_PRIVATE;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.money.manager.ex.MmexApplication;
import com.money.manager.ex.R;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.core.UIHelper;
import com.money.manager.ex.home.DatabaseMetadata;
import com.money.manager.ex.home.RecentDatabasesProvider;
import com.money.manager.ex.settings.PreferenceConstants;
import com.money.manager.ex.sync.events.DbFileDownloadedEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import javax.inject.Inject;

import dagger.Lazy;
import timber.log.Timber;

/**
 * Synchronization preferences fragment.
 */
public class SyncPreferenceFragment
    extends PreferenceFragmentCompat {

    public SyncPreferenceFragment() {
        // Required empty public constructor
    }

    @Inject Lazy<RecentDatabasesProvider> mDatabases;

    private SyncManager mSyncManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MmexApplication.getApp().iocComponent.inject(this);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        PreferenceManager prefMgr = getPreferenceManager();
        prefMgr.setSharedPreferencesName(PreferenceConstants.SYNC_PREFERENCES);
        prefMgr.setSharedPreferencesMode(MODE_PRIVATE); // MODE_WORLD_READABLE

        addPreferencesFromResource(R.xml.preferences_sync);

        initializePreferences();
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

        EventBus.getDefault().unregister(this);
    }

    /**
     * Called when file is downloaded from the cloud storage.
     */
    @Subscribe
    public void onEvent(DbFileDownloadedEvent event) {
        // open the new database.
        getSyncManager().useDownloadedDatabase();
    }

    /*
        Private
     */
    private SyncManager getSyncManager() {
        if (mSyncManager == null) {
            mSyncManager = new SyncManager(getActivity());
        }
        return mSyncManager;
    }

    private RecentDatabasesProvider getDatabases() {
        return mDatabases.get();
    }

    private void initializePreferences() {
        SyncPreferencesViewHolder viewHolder = new SyncPreferencesViewHolder(this);

        viewHolder.syncInterval.setSummary(viewHolder.syncInterval.getEntries()[viewHolder.syncInterval.findIndexOfValue(viewHolder.syncInterval.getValue())]);
        viewHolder.syncInterval.setOnPreferenceChangeListener((preference, o) -> {
            // reset timer.
            SyncManager sync = getSyncManager();
            int interval = Integer.parseInt(o.toString());
            sync.setSyncInterval(interval);
            Timber.d("sync interval set to %d", interval);

            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(o.toString());
            preference.setSummary(listPreference.getEntries()[prefIndex]);

            sync.stopSyncServiceAlarm();
            if (interval > 0) {
                // don't start sync service if the interval is set to 0.
                sync.startSyncServiceHeartbeat();
            }
            return true;
        });

        // Download.

        viewHolder.download.setOnPreferenceClickListener(preference -> {
            DatabaseMetadata currentDb = getDatabases().getCurrent();

            SyncManager sync = new SyncManager(getContext());
            if (sync.canSync()) {
                boolean isLocalModified = currentDb.isLocalFileChanged();

                String message = String.format(
                        "Local file changes indicator: %s.\n" +
                                "Downloading will overwrite your local version.\nDo you want to continue?"
                        , isLocalModified);

                showConfirmDialog("Download Warning", message, this::forceDownload);
            } else {
                Toast.makeText(getContext(), R.string.remote_unavailable, Toast.LENGTH_SHORT).show();
            }
            return false;
        });

        // Upload.

        viewHolder.upload.setOnPreferenceClickListener(preference -> {
            DatabaseMetadata currentDb = getDatabases().getCurrent();

            SyncManager sync = new SyncManager(getContext());
            if (sync.canSync()) {
                boolean isRemoteModified = currentDb.isRemoteFileChanged(getContext());

                String message = String.format(
                        "Remote file changes indicator: %s.\n" +
                                "Uploading will overwrite your remote version.\nDo you want to continue?"
                        , isRemoteModified);
                showConfirmDialog("Upload Warning",message,this::forceUpload);
            } else {
                Toast.makeText(getContext(), "Remote file unavailable.", Toast.LENGTH_SHORT).show();
            }
            return false;
        });

        // reset preferences
        viewHolder.resetPreferences.setOnPreferenceClickListener(preference -> {
            // TODO
            return false;
        });
    }

    private void forceDownload() {
        getSyncManager().triggerDownload();
    }

    private void forceUpload() {
        String remotePath = getSyncManager().getRemotePath();
        if (TextUtils.isEmpty(remotePath)) {
            new Core(getActivity()).alert(R.string.no_remote_file);
            return;
        }

        try {
            getSyncManager().triggerUpload();
            new UIHelper(getActivity()).showToast(R.string.sync_uploading);
        } catch (RuntimeException e) {
            Timber.e(e, "uploading database");
        }
    }

    private void showConfirmDialog(String title, String message, Runnable onConfirm) {
        new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Continue", (dialog, which) -> onConfirm.run())
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
