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

            final Preference pMoveDatabase = findPreference(PreferencesConstant.PREF_DATABASE_BACKUP);
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
            Preference pSQLiteVersion = findPreference(PreferencesConstant.PREF_SQLITE_VERSION);
            if (pSQLiteVersion != null) {
                MoneyManagerOpenHelper helper = MoneyManagerOpenHelper.getInstance(getActivity());
                String sqliteVersion = helper.getSQLiteVersion();
                if (sqliteVersion != null) pSQLiteVersion.setSummary(sqliteVersion);
            }
        }
    }
}
