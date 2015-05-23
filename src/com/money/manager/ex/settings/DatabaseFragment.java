/*
 * Copyright (C) 2012-2015 The Android Money Manager Ex Project Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.money.manager.ex.settings;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.text.Html;
import android.text.InputType;
import android.text.TextUtils;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.money.manager.ex.MainActivity;
import com.money.manager.ex.MoneyManagerApplication;
import com.money.manager.ex.R;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.database.DatabaseMigrator14To20;
import com.money.manager.ex.database.MmexDatabase;
import com.money.manager.ex.database.MoneyManagerOpenHelper;
import com.money.manager.ex.utils.DonateDialogUtils;

import java.io.File;

/**
 * Database settings fragment.
 */
public class DatabaseFragment
        extends PreferenceFragment {

    private final String LOGCAT = this.getClass().getSimpleName();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.database_settings);

        PreferenceManager.getDefaultSharedPreferences(getActivity());

        // Database path.
        refreshDbPath();

        // Check schema
        initDbSchemaCheckOption();

        // Export database.
        initExportDbOption();

        //sqlite version
        Preference pSQLiteVersion = findPreference(getString(PreferenceConstants.PREF_SQLITE_VERSION));
        if (pSQLiteVersion != null) {
            MoneyManagerOpenHelper helper = MoneyManagerOpenHelper.getInstance(getActivity().getApplicationContext());
            String sqliteVersion = helper.getSQLiteVersion();
            if (sqliteVersion != null) pSQLiteVersion.setSummary(sqliteVersion);
        }

        // Check integrity
        initDatabaseIntegrityOption();

        // Migration of databases from version 1.4 to the location in 2.0.
        setVisibilityOfMigrationButton();

        // Create database
        initCreateDatabaseOption();
    }

    private void setVisibilityOfMigrationButton() {
        Preference migratePreference = findPreference(getString(R.string.pref_database_migrate_14_to_20));
        if (migratePreference == null) return;

        // display description.
        migratePreference.setSummary(getString(R.string.database_migrate_14_to_20_explanation));

        // check if there is a database at the old location.
        final DatabaseMigrator14To20 migrator = new DatabaseMigrator14To20(getActivity());
        boolean legacyDataExists = migrator.legacyDataExists();

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
        boolean result = false;

        if (TextUtils.isEmpty(filename)) {
            return result;
        }

        // try to create the db file.
        MmexDatabase db = new MmexDatabase(getActivity());
        boolean created = db.createDatabase(filename);

        if (created) {
            // set main activity to reload, to open the new db file.
            MainActivity.setRestartActivity(true);

            // update the displayed value.
            refreshDbPath();

            result = true;
        }

        return result;
    }

    private void refreshDbPath() {
        final Preference pDatabasePath = findPreference(getActivity().getString(PreferenceConstants.PREF_DATABASE_PATH));
        pDatabasePath.setSummary(MoneyManagerApplication.getDatabasePath(getActivity().getApplicationContext()));
    }

    private void initDbSchemaCheckOption() {
        Preference preference = findPreference(getString(R.string.pref_db_check_schema));
        if (preference == null) return;

        preference.setSummary(getString(R.string.db_check_schema_summary));

        Preference.OnPreferenceClickListener clickListener = new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                MmexDatabase db = new MmexDatabase(getActivity());
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
                MmexDatabase db = new MmexDatabase(getActivity());
                boolean result = db.checkIntegrity();
                if (result) {
                    showToast(R.string.db_check_integrity_success, Toast.LENGTH_SHORT);
                } else {
                    showToast(R.string.db_check_integrity_error, Toast.LENGTH_SHORT);
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
                        // save the database file
                        MoneyManagerApplication.setDatabasePath(getActivity().getApplicationContext(),
                                newDatabases.getAbsolutePath());
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
            pMoveDatabase.setEnabled(MoneyManagerApplication.getDatabasePath(getActivity().getApplicationContext())
                    .startsWith("/data/"));
        }
    }

    private void showToast(int resourceId, int duration) {
        Toast.makeText(getActivity(), resourceId, duration).show();
    }

}
