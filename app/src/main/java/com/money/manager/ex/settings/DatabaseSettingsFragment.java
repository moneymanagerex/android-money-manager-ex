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

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Html;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.money.manager.ex.BuildConfig;
import com.money.manager.ex.core.UIHelper;
import com.money.manager.ex.core.database.DatabaseManager;
import com.money.manager.ex.home.DatabaseMetadata;
import com.money.manager.ex.home.DatabaseMetadataFactory;
import com.money.manager.ex.MmexApplication;
import com.money.manager.ex.R;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.database.DatabaseMigrator14To20;
import com.money.manager.ex.database.MmxOpenHelper;
import com.money.manager.ex.home.MainActivity;
import com.money.manager.ex.home.RecentDatabasesProvider;
import com.money.manager.ex.utils.DonateDialogUtils;
import com.money.manager.ex.utils.MmxDatabaseUtils;

import java.io.File;

import javax.inject.Inject;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;
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

        // Migration of databases from version 1.4 to the location in 2.0.
        setVisibilityOfMigrationButton();

        // Create database
        initCreateDatabaseOption();

        // Fix duplicates
        initFixDuplicates();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
//        Timber.d("creating");
    }

    // private

    private void initClearRecentFiles() {
        Preference preference = findPreference(getString(R.string.pref_clear_recent_files));
        if (preference == null) return;

        final RecentDatabasesProvider recents = mDatabases.get();


        // show how many items are in the list.
        preference.setSummary(Integer.toString(recents.map.size()));

        preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                // clear recent files list
                boolean success = recents.clear();
                // update display value.
                showNumberOfRecentFiles();

                // notification
                String message = success
                    ? getString(R.string.cleared)
                    : getString(R.string.error);
                new UIHelper(getActivity()).showToast(message);
                return false;
            }
        });
    }

    private void refreshDbVersion() {
        final Preference preference = findPreference(getActivity().getString(R.string.pref_database_version));

        String version = "N/A";

        SQLiteDatabase db = openHelper.get().getReadableDatabase();
        if (db != null) {
            int versionNumber = db.getVersion();
            version = Integer.toString(versionNumber);
        }

        preference.setSummary(version);
    }

    private void setVisibilityOfMigrationButton() {
        Preference migratePreference = findPreference(getString(R.string.pref_database_migrate_14_to_20));
        if (migratePreference == null) return;

        // check if there is a database at the old location.
        final DatabaseMigrator14To20 migrator = new DatabaseMigrator14To20(getActivity());
        boolean legacyDataExists = migrator.legacyDataExists();

        // display description.
        migratePreference.setSummary(getString(R.string.database_migrate_14_to_20_explanation));
        // + " (" + migrator.getLegacyDbPath() + ")");

        Preference.OnPreferenceClickListener migrateClicked = new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                boolean success = migrator.migrateLegacyDatabase();
                if (success) {
                    Toast.makeText(getActivity(), R.string.database_migrate_14_to_20_success, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getActivity(), R.string.database_migrate_14_to_20_failure, Toast.LENGTH_LONG).show();
                }
                // The return value indicates whether to persist the preference,
                // which is not used in this case.
                return false;
            }
        };

        // hide preference if there is no legacy data.
        if (!legacyDataExists) {
            PreferenceScreen screen = getPreferenceScreen();
            screen.removePreference(migratePreference);
        } else {
            // enable listener for migration.
            migratePreference.setOnPreferenceClickListener(migrateClicked);
        }

    }

    private void initCreateDatabaseOption() {
        Preference preference = findPreference(getString(R.string.pref_database_create));
        if (preference == null) return;

        preference.setSummary(getString(R.string.create_db_summary));

        Preference.OnPreferenceClickListener clickListener = new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                promptForDatabaseFilename();
                return false;
            }
        };

        preference.setOnPreferenceClickListener(clickListener);
    }

    private void promptForDatabaseFilename() {
        new MaterialDialog.Builder(getActivity())
                .title(R.string.create_db)
                .content(R.string.create_db_dialog_content)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input(getString(R.string.create_db_hint),
                        null, false, new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(MaterialDialog materialDialog, CharSequence charSequence) {
                                boolean success = createDatabase(charSequence.toString());
                                if (success) {
                                    Toast.makeText(getActivity(), R.string.create_db_success,
                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getActivity(), R.string.create_db_error,
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                .positiveText(android.R.string.ok)
                .negativeText(android.R.string.cancel)
                .show();
    }

    private boolean createDatabase(String filename) {
        // validation
        if (TextUtils.isEmpty(filename)) return false;

        MmxDatabaseUtils dbUtils = new MmxDatabaseUtils(getActivity());

        // Create the db file. Store the path in the preferences.
        String dbPath = dbUtils.createDatabase(filename);
        if (TextUtils.isEmpty(dbPath)) return false;

        DatabaseMetadata db = DatabaseMetadataFactory.getInstance(dbPath);
        boolean isSet = dbUtils.useDatabase(db);
        if (!isSet) return false;

        // set main activity to reload, to open the new db file.
        MainActivity.setRestartActivity(true);

        // update the displayed value.
        refreshDbPath();
        showNumberOfRecentFiles();

        return true;
    }

    private void refreshDbPath() {
        final Preference preference = findPreference(getActivity().getString(R.string.pref_database_path));
        preference.setSummary(new DatabaseManager(getActivity().getApplicationContext()).getDatabasePath());
    }

    private void initDbSchemaCheckOption() {
        Preference preference = findPreference(getString(R.string.pref_db_check_schema));
        if (preference == null) return;

        preference.setSummary(getString(R.string.db_check_schema_summary));

        Preference.OnPreferenceClickListener clickListener = new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                MmxDatabaseUtils db = new MmxDatabaseUtils(getActivity());

                Timber.d("checking db schema");

                boolean result = db.checkSchema();
                if (result) {
                    showToast(R.string.db_check_schema_success, Toast.LENGTH_SHORT);
                } else {
                    showToast(R.string.db_check_schema_error, Toast.LENGTH_SHORT);
                }
                return false;
            }
        };

        preference.setOnPreferenceClickListener(clickListener);
    }

    private void initDatabaseIntegrityOption() {
        Preference preference = findPreference(getString(R.string.pref_database_check_integrity));
        if (preference == null) return;

        preference.setSummary(getString(R.string.db_check_integrity_summary));

        Preference.OnPreferenceClickListener clickListener = new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
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
            }
        };

        preference.setOnPreferenceClickListener(clickListener);
    }

    private void initExportDbOption() {
        final Preference pMoveDatabase = findPreference(getString(PreferenceConstants.PREF_DATABASE_BACKUP));
        if (pMoveDatabase != null) {
            pMoveDatabase.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference preference) {
                    // copy files
                    Core core = new Core(getActivity().getApplicationContext());
                    File newDatabases = core.backupDatabase();
                    if (newDatabases != null) {
                        Toast.makeText(getActivity(), Html.fromHtml(getString(R.string.database_has_been_moved,
                                "<b>" + newDatabases.getAbsolutePath() + "</b>")), Toast.LENGTH_LONG).show();
                        //MainActivity.changeDatabase(newDatabases.getAbsolutePath());
                        // update the database file
//                        MoneyManagerApplication.setDatabasePath(getActivity().getApplicationContext(),
//                                newDatabases.getAbsolutePath());
                        new AppSettings(getActivity().getApplicationContext()).getDatabaseSettings()
                                .setDatabasePath(newDatabases.getAbsolutePath());
                        DonateDialogUtils.resetDonateDialog(getActivity().getApplicationContext());
                        // set to restart activity
                        MainActivity.setRestartActivity(true);
                    } else {
                        Toast.makeText(getActivity(), R.string.copy_database_on_external_storage_failed, Toast.LENGTH_LONG)
                                .show();
                    }
                    return false;
                }
            });
            pMoveDatabase.setEnabled(new DatabaseManager(getActivity().getApplicationContext())
                    .getDatabasePath().startsWith("/data/"));
        }
    }

    private void showToast(int resourceId, int duration) {
        Toast.makeText(getActivity(), resourceId, duration).show();
    }

    private void initFixDuplicates() {
        Preference preference = findPreference(getString(R.string.pref_db_fix_duplicates));
        if (preference == null) return;

        Preference.OnPreferenceClickListener clickListener = new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                MmxDatabaseUtils utils = new MmxDatabaseUtils(getActivity());

                if (BuildConfig.DEBUG) {
                    Log.d(this.getClass().getSimpleName(), "fixing duplicates");
                }

                boolean result = utils.fixDuplicates();

                if (result) {
                    showToast(R.string.success, Toast.LENGTH_SHORT);
                } else {
                    showToast(R.string.error, Toast.LENGTH_SHORT);
                }
                return false;
            }
        };

        preference.setOnPreferenceClickListener(clickListener);
    }

    private void showNumberOfRecentFiles() {
        Preference preference = findPreference(getString(R.string.pref_clear_recent_files));
        if (preference == null) return;

        preference.setSummary(Integer.toString(mDatabases.get().count()));
    }
}
