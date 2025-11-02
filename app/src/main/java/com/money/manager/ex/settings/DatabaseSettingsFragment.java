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
package com.money.manager.ex.settings;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.money.manager.ex.BuildConfig;
import com.money.manager.ex.MmexApplication;
import com.money.manager.ex.R;
import com.money.manager.ex.core.UIHelper;
import com.money.manager.ex.database.MmxOpenHelper;
import com.money.manager.ex.home.DatabaseMetadata;
import com.money.manager.ex.home.RecentDatabasesProvider;
import com.money.manager.ex.utils.MmxDatabaseUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.inject.Inject;

import dagger.Lazy;
import timber.log.Timber;

/**
 * Database preferences fragment.
 */
public class DatabaseSettingsFragment
    extends PreferenceFragmentCompat {

    @Inject Lazy<MmxOpenHelper> openHelper;
    @Inject Lazy<RecentDatabasesProvider> mDatabases;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if( getActivity() == null) return;

        getActivity().setTitle(R.string.database);

        MmexApplication.getApp().iocComponent.inject(this);

        addPreferencesFromResource(R.xml.preferences_database);

        // Database path.
        refreshDbPath();

        initClearRecentFiles();

        refreshDbVersion();

        // Check schema
        initDbSchemaCheckOption();

        // Export database.
        initExportDbOption();

        //sqlite version
        Preference pSQLiteVersion = findPreference(getString(PreferenceConstants.PREF_SQLITE_VERSION));
        if (pSQLiteVersion != null) {
            String sqliteVersion = openHelper.get().getSQLiteVersion();
            if (sqliteVersion != null) pSQLiteVersion.setSummary(sqliteVersion);
        }

        // Check integrity
        initDatabaseIntegrityOption();

        // Fix duplicates
        initFixDuplicates();
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(R.string.database);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
//        Timber.d("creating");
    }

    // private

    private void initClearRecentFiles() {
        Preference preference = findPreference(getString(R.string.pref_clear_recent_files));
        if (preference == null) return;

        final RecentDatabasesProvider recent = mDatabases.get();


        // show how many items are in the list.
        preference.setSummary(Integer.toString(recent.map.size()));

        preference.setOnPreferenceClickListener(preference1 -> {
            // clear recent files list
            boolean success = recent.clear();
            // update display value.
            showNumberOfRecentFiles();

            // notification
            String message = success
                ? getString(R.string.cleared)
                : getString(R.string.error);
            new UIHelper(getActivity()).showToast(message);
            return false;
        });
    }

    private void refreshDbVersion() {
        final Preference preference = findPreference(requireActivity().getString(R.string.pref_database_version));

        String version = "N/A";
        SupportSQLiteDatabase db = null;
        try {
            db = openHelper.get().getReadableDatabase();
        } catch (Exception e) {
            Timber.e(e);
        }
        if (db != null) {
            int versionNumber = db.getVersion();
            version = Integer.toString(versionNumber);
        }

        assert preference != null;
        preference.setSummary(version);
    }

    private void refreshDbPath() {

        DatabaseMetadata db = mDatabases.get().getCurrent();
        final Preference preference = findPreference(getActivity().getString(R.string.pref_database_path));

        // issue 2199
        if (preference == null || db == null || db.localPath == null) {
            Timber.e("db path is null");
            return;
        }
        preference.setSummary(db.localPath);

        final Preference remotePreference = findPreference(getActivity().getString(R.string.pref_remote_path));
        remotePreference.setSummary(db.remotePath);
    }

    private void initDbSchemaCheckOption() {
        Preference preference = findPreference(getString(R.string.pref_db_check_schema));
        if (preference == null) return;

        preference.setSummary(getString(R.string.db_check_schema_summary));

        preference.setOnPreferenceClickListener(preference1 -> {
            MmxDatabaseUtils db = new MmxDatabaseUtils(getActivity());

            Timber.d("checking db schema");

            boolean result = db.checkSchema();
            if (result) {
                showToast(R.string.db_check_schema_success, Toast.LENGTH_SHORT);
            } else {
                showToast(R.string.db_check_schema_error, Toast.LENGTH_SHORT);
            }
            return false;
        });
    }

    private void initDatabaseIntegrityOption() {
        Preference preference = findPreference(getString(R.string.pref_database_check_integrity));
        if (preference == null) return;

        preference.setSummary(getString(R.string.db_check_integrity_summary));

        preference.setOnPreferenceClickListener(preference1 -> {
            MmxDatabaseUtils db = new MmxDatabaseUtils(getActivity());
            boolean result;
            try {
                Timber.d("checking db integrity.");

                result = db.checkIntegrity();

                if (result) {
                    showToast(R.string.db_check_integrity_success, Toast.LENGTH_SHORT);
                } else {
                    showToast(R.string.db_check_integrity_error, Toast.LENGTH_SHORT);
                }
            } catch (Exception ex) {
                Timber.e(ex, "checking integrity");
            }
            return false;
        });
    }

    private final ActivityResultLauncher<Intent> backupLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        // Questo blocco viene eseguito quando la SecondActivity si chiude
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            Uri uri = data.getData();
                            if (uri != null) {
                                // Perform the backup operation using the selected URI
                                backupDatabase(uri);
                            }
                        }
                    });

    private void requestBackup() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_TITLE, "your_export_db.mmb"); // Set a default file name

//        startActivityForResult(intent, RequestCodes.CODE_BACKUP);
        backupLauncher.launch(intent);
    }

/* Relpace with backupLauncher
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RequestCodes.CODE_BACKUP && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            if (uri != null) {
                // Perform the backup operation using the selected URI
                backupDatabase(uri);
            }
        }
    }
*/

    private void backupDatabase(Uri destinationUri) {
        try {
            DatabaseMetadata db = mDatabases.get().getCurrent();
            InputStream inputStream = new FileInputStream(db.localPath);
            OutputStream outputStream = getActivity().getContentResolver().openOutputStream(destinationUri);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            inputStream.close();
            outputStream.close();

            // Show a success message or handle as needed
            showToast("Backup successful");
        } catch (IOException e) {
            e.printStackTrace();
            // Handle errors appropriately
            showToast("Backup failed");
        } catch ( Exception e ) { // handle issue #2603
            showToast("Unable to perform backup.");
        }
    }

    private void initExportDbOption() {
        final Preference pMoveDatabase = findPreference(getString(PreferenceConstants.PREF_DATABASE_BACKUP));
        if (pMoveDatabase != null) {
            pMoveDatabase.setOnPreferenceClickListener(preference -> {
                requestBackup();
                return false;
            });
        }
    }

    private void showToast(int resourceId, int duration) {
        Toast.makeText(getActivity(), resourceId, duration).show();
    }

    private void showToast(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    private void initFixDuplicates() {
        Preference preference = findPreference(getString(R.string.pref_db_fix_duplicates));
        if (preference == null) return;

        preference.setOnPreferenceClickListener(preference1 -> {
            MmxDatabaseUtils utils = new MmxDatabaseUtils(getActivity());

            if (BuildConfig.DEBUG) {
                Timber.d("fixing duplicates");
            }

            boolean result = utils.fixDuplicates();

            if (result) {
                showToast(R.string.success, Toast.LENGTH_SHORT);
            } else {
                showToast(R.string.error, Toast.LENGTH_SHORT);
            }
            return false;
        });
    }

    private void showNumberOfRecentFiles() {
        Preference preference = findPreference(getString(R.string.pref_clear_recent_files));
        if (preference == null) return;

        preference.setSummary(Integer.toString(mDatabases.get().count()));
    }
}
