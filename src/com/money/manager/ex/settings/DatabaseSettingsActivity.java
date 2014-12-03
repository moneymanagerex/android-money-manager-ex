/*
 * Copyright (C) 2012-2014 Alessandro Lazzari
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
import android.text.Html;
import android.widget.Toast;

import com.money.manager.ex.MainActivity;
import com.money.manager.ex.MoneyManagerApplication;
import com.money.manager.ex.R;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.core.DonateDialogUtils;
import com.money.manager.ex.database.MoneyManagerOpenHelper;
import com.money.manager.ex.fragment.BaseFragmentActivity;
import com.money.manager.ex.preferences.PreferencesConstant;

import java.io.File;

public class DatabaseSettingsActivity extends BaseFragmentActivity {
    private static String LOGCAT = DatabaseSettingsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setDisplayHomeAsUpEnabled(true);

        getFragmentManager().beginTransaction().replace(android.R.id.content, new DatabaseFragment()).commit();
    }

    public static class DatabaseFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.database_settings);
            PreferenceManager.getDefaultSharedPreferences(getActivity());

            final Preference pMoveDatabase = findPreference(getString(PreferencesConstant.PREF_DATABASE_BACKUP));
            if (pMoveDatabase != null) {
                pMoveDatabase.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        // copy files
                        Core core = new Core(getActivity());
                        File newDatabases = core.backupDatabase();
                        if (newDatabases != null) {
                            Toast.makeText(getActivity(), Html.fromHtml(getString(R.string.database_has_been_moved, "<b>" + newDatabases.getAbsolutePath() + "</b>")), Toast.LENGTH_LONG).show();
                            //MainActivity.changeDatabase(newDatabases.getAbsolutePath());
                            // save the database file
                            MoneyManagerApplication.setDatabasePath(getActivity().getApplicationContext(), newDatabases.getAbsolutePath());
                            DonateDialogUtils.resetDonateDialog(getActivity().getApplicationContext());
                            // set to restart activity
                            MainActivity.setRestartActivity(true);
                        } else {
                            Toast.makeText(getActivity(), R.string.copy_database_on_external_storage_failed, Toast.LENGTH_LONG).show();
                        }
                        return false;
                    }
                });
                pMoveDatabase.setEnabled(MoneyManagerApplication.getDatabasePath(getActivity().getApplicationContext()).startsWith("/data/"));
            }
            final Preference pDatabasePath = findPreference(getActivity().getString(PreferencesConstant.PREF_DATABASE_PATH));
            pDatabasePath.setSummary(MoneyManagerApplication.getDatabasePath(getActivity().getApplicationContext()));
            //sqlite version
            Preference pSQLiteVersion = findPreference(getString(PreferencesConstant.PREF_SQLITE_VERSION));
            if (pSQLiteVersion != null) {
                MoneyManagerOpenHelper helper = MoneyManagerOpenHelper.getInstance(getActivity());
                String sqliteVersion = helper.getSQLiteVersion();
                if (sqliteVersion != null) pSQLiteVersion.setSummary(sqliteVersion);
            }
        }
    }
}
