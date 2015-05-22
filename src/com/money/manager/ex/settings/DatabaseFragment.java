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
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.money.manager.ex.MainActivity;
import com.money.manager.ex.MoneyManagerApplication;
import com.money.manager.ex.R;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.database.DatabaseMigrator14To20;
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

        // Database path.
        final Preference pDatabasePath = findPreference(getActivity().getString(PreferenceConstants.PREF_DATABASE_PATH));
        pDatabasePath.setSummary(MoneyManagerApplication.getDatabasePath(getActivity().getApplicationContext()));

        //sqlite version
        Preference pSQLiteVersion = findPreference(getString(PreferenceConstants.PREF_SQLITE_VERSION));
        if (pSQLiteVersion != null) {
            MoneyManagerOpenHelper helper = MoneyManagerOpenHelper.getInstance(getActivity().getApplicationContext());
            String sqliteVersion = helper.getSQLiteVersion();
            if (sqliteVersion != null) pSQLiteVersion.setSummary(sqliteVersion);
        }

        // Migration of databases from version 1.4 to the location in 2.0.
        setVisibilityOfMigrationButton();

        // Create database option.
        setUpCreateDatabaseOption();
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

    private void setUpCreateDatabaseOption() {
        Preference createDbPreference = findPreference(getString(R.string.pref_database_create));
        if (createDbPreference == null) return;

        createDbPreference.setSummary(getString(R.string.create_database_summary));

        Preference.OnPreferenceClickListener createClicked = new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                promptForDatabaseFilename();
                return false;
            }
        };

        createDbPreference.setOnPreferenceClickListener(createClicked);
    }

    private void promptForDatabaseFilename() {
        new MaterialDialog.Builder(getActivity())
                .title(R.string.create_database)
                .content(R.string.create_database_dialog_content)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input("enter blah", "prefill", false, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog materialDialog, CharSequence charSequence) {
                        createDatabase();
                    }
                })
                .positiveText(android.R.string.ok)
                .negativeText(android.R.string.cancel)
                .show();
    }

    private void createDatabase(String filename) {
        // todo: check if filename already contains the extension

        // try to create the db file.

        // initialize the database with this file

        // store as the default database in settings

        // show the result message.
    }
}
