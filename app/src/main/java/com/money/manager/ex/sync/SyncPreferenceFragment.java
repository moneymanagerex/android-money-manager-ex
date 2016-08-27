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

import android.widget.Toast;

import com.money.manager.ex.BuildConfig;
import com.money.manager.ex.MoneyManagerApplication;
import com.money.manager.ex.R;
import com.money.manager.ex.common.BaseFragmentActivity;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.core.UIHelper;
import com.money.manager.ex.settings.AppSettings;
import com.money.manager.ex.sync.events.DbFileDownloadedEvent;
import com.money.manager.ex.settings.PreferenceConstants;
import com.money.manager.ex.utils.MmxDatabaseUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;

import rx.SingleSubscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import timber.log.Timber;

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

        // Use a separate sync preference file.
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

    private void handleFileSelection(int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK || data == null) return;

        // get value
        String remoteFile = data.getStringExtra(SyncPreferenceFragment.EXTRA_REMOTE_FILE);

        // show selected value
        viewHolder.remoteFile.setSummary(remoteFile);

        // Save the selection into preferences.
        getSyncManager().setRemotePath(remoteFile);

        // start sync service
        getSyncManager().startSyncServiceAlarm();

        // save local path.
        storeLocalFileSetting(remoteFile);

        ((BaseFragmentActivity) getActivity()).compositeSubscription.add(
            UIHelper.binaryDialog(getActivity(), R.string.download, R.string.confirm_download)
                    .filter(new Func1<Boolean, Boolean>() {
                        @Override
                        public Boolean call(Boolean aBoolean) {
                            // proceed only if user accepts
                            return aBoolean;
                        }
                    })
    //                .toBlocking().single();
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean aBoolean) {
                        // download db from the cloud storage
                        checkIfLocalFileExists();
                    }
                })
        );
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
                    getSyncManager().startSyncServiceAlarm();
                } else {
                    getSyncManager().stopSyncServiceAlarm();
                }
                return true;
            }
        });


        // provider
        viewHolder.providerList.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                final SyncManager sync = getSyncManager();
                // set the new provider
                sync.setProvider(CloudStorageProviderEnum.valueOf(o.toString()));
//                final boolean[] result = new boolean[1];

                // log in to the provider immediately and save to persistence.
//                ((BaseFragmentActivity) getActivity()).compositeSubscription.add(
                    sync.login()
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
//                            .toBlocking().value();
                            .subscribe(new SingleSubscriber<Void>() {
                                @Override
                                public void onSuccess(Void value) {
//                                    result[0] = true;
                                }

                                @Override
                                public void onError(Throwable error) {
                                    if (error.getMessage().equals("Authentication was cancelled")) {
                                        Timber.w("authentication cancelled");
                                    } else {
                                        Timber.e(error, "logging in to cloud provider");
                                    }
                                }
                            });
//                );

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

        // interval
//        if (BuildConfig.DEBUG) {
//            // insert a 1-minute in debug mode
//            CharSequence[] entries = viewHolder.syncInterval.getEntries();
//            String[] newEntries = new String[entries.length + 1];
//            newEntries[0] = "1-minute";
//            System.arraycopy(entries, 0, newEntries, 1, entries.length);
//            viewHolder.syncInterval.setEntries(newEntries);
//            // values
//            CharSequence[] values = viewHolder.syncInterval.getEntryValues();
//            String[] newValues = new String[values.length + 1];
//            newValues[0] = "1";
//            System.arraycopy(values, 0, newValues, 1, values.length);
//            viewHolder.syncInterval.setEntryValues(newValues);
//        }

        viewHolder.syncInterval.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                // reset timer.
                SyncManager sync = getSyncManager();
                int interval = Integer.parseInt(o.toString());
                sync.setSyncInterval(interval);
                Timber.d("sync interval set to %d", interval);

                sync.stopSyncServiceAlarm();
                if (interval > 0) {
                    // don't start sync service if the interval is set to 0.
                    sync.startSyncServiceAlarm();
                }
                return true;
            }
        });

        // Download.

        viewHolder.download.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                forceDownload();
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
                final SyncManager sync = getSyncManager();
                sync.logout()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleSubscriber<Void>() {
                        @Override
                        public void onSuccess(Void value) {
                            sync.resetPreferences();
                            sync.stopSyncServiceAlarm();

                            Core.alertDialog(getActivity(), R.string.preferences_reset);

                            getActivity().recreate();
                        }

                        @Override
                        public void onError(Throwable error) {
                            Timber.e(error, "logging out the cloud provider");                        }
                    });
                return false;
            }
        });
    }

    private void checkIfLocalFileExists() {
        String local = getSyncManager().getLocalPath();

        // check if the file exists and prompt the user.
        if (new File(local).exists()) {
            // prompt
            ((BaseFragmentActivity) getActivity()).compositeSubscription.add(
                UIHelper.binaryDialog(getActivity(), R.string.file_exists, R.string.file_exists_long)
                        .filter(new Func1<Boolean, Boolean>() {
                            @Override
                            public Boolean call(Boolean aBoolean) {
                                // proceed only if user confirms
                                return aBoolean;
                            }
                        })
                        .subscribe(new Action1<Boolean>() {
                            @Override
                            public void call(Boolean aBoolean) {
                                // finally download the file.
                                forceDownload();
                            }
                        })
            );
        } else {
            forceDownload();
        }
    }

    private void forceDownload() {
        getSyncManager().triggerDownload(true);
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

    private void storeLocalFileSetting(String remoteFile) {
        String fileName = new File(remoteFile).getName();

        MmxDatabaseUtils dbUtils = new MmxDatabaseUtils(getActivity());
        String dbDirectory = dbUtils.getDefaultDatabaseDirectory();

        String dbPath = dbDirectory.concat(File.separator).concat(fileName);

        // save preference
        new AppSettings(getActivity()).getDatabaseSettings().setDatabasePath(dbPath);
    }
}
