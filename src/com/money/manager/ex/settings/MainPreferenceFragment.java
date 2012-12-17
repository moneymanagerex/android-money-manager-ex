/*******************************************************************************
 * Copyright (C) 2012 The Android Money Manager Ex Project
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
 ******************************************************************************/
package com.money.manager.ex.settings;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.widget.Toast;

import com.money.manager.ex.MainActivity;
import com.money.manager.ex.MoneyManagerApplication;
import com.money.manager.ex.PasscodeActivity;
import com.money.manager.ex.R;
import com.money.manager.ex.core.Passcode;
import com.money.manager.ex.database.TableCurrencyFormats;
import com.money.manager.ex.dropbox.DropboxActivity;

public class MainPreferenceFragment extends PreferenceListFragment implements SharedPreferences.OnSharedPreferenceChangeListener,
		PreferenceListFragment.OnPreferenceAttachedListener {
	private static final int REQUEST_INSERT_PASSCODE = 1;
	private static final int REQUEST_EDIT_PASSCODE = 2;
	private static final int REQUEST_DELETE_PASSCODE = 3;
	private static final int REQUEST_REINSERT_PASSCODE = 10;
	// application
	private MoneyManagerApplication application;
	// id preference
	private Preference pUserName, pDatabasePath, pDropboxFile;
	private PreferenceScreen psActivePasscode, psEditPasscode, psDisablePasscode;
	private ListPreference lstBaseCurrency, lstDropboxMode, lstTheme, lstShow, lstTypeHome;
	private CheckBoxPreference chkAccountOpen, chkAccountFav;

	private static String passcode = null;

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Passcode pass = new Passcode(getActivity());
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == REQUEST_INSERT_PASSCODE && data != null) {
				// check if reinsert
				passcode = data.getStringExtra(PasscodeActivity.INTENT_RESULT_PASSCODE);
				startActivityPasscode(getString(R.string.reinsert_your_passcode), REQUEST_REINSERT_PASSCODE);
			}
			if (requestCode == REQUEST_REINSERT_PASSCODE && data != null) {
				if (passcode.equals(data.getStringExtra(PasscodeActivity.INTENT_RESULT_PASSCODE))) {
					if (!pass.setPasscode(passcode)) {
						Toast.makeText(getActivity(), R.string.passcode_not_update, Toast.LENGTH_LONG).show();
					}
				} else {
					Toast.makeText(getActivity(), R.string.passocde_no_macth, Toast.LENGTH_LONG).show();
				}
			}
			if (requestCode == REQUEST_EDIT_PASSCODE && data != null) {
				// check if reinsert
				passcode = data.getStringExtra(PasscodeActivity.INTENT_RESULT_PASSCODE);
				String passcodedb = pass.getPasscode();
				if (passcodedb != null && passcode != null) {
					if (passcodedb.equals(passcode)) {
						startActivityPasscode(getString(R.string.enter_your_passcode), REQUEST_INSERT_PASSCODE);
					} else
						Toast.makeText(getActivity(), R.string.passocde_no_macth, Toast.LENGTH_LONG).show();
				}
			}
			if (requestCode == REQUEST_DELETE_PASSCODE && data != null) {
				// check if reinsert
				passcode = data.getStringExtra(PasscodeActivity.INTENT_RESULT_PASSCODE);
				String passcodedb = pass.getPasscode();
				if (passcodedb != null && passcode != null) {
					if (passcodedb.equals(passcode)) {
						if (!pass.cleanPasscode()) {
							Toast.makeText(getActivity(), R.string.passcode_not_update, Toast.LENGTH_LONG).show();
						}
					} else
						Toast.makeText(getActivity(), R.string.passocde_no_macth, Toast.LENGTH_LONG).show();
				}
			}
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		application = (MoneyManagerApplication) getActivity().getApplication();
		application.setThemeApplication(getActivity());

		// set layout
		addPreferencesFromResource(R.xml.prefrences);
		PreferenceManager.getDefaultSharedPreferences(getActivity());

		// preference username
		pUserName = findPreference(MoneyManagerApplication.PREF_USER_NAME);
		if (pUserName != null) {
			pUserName.setSummary(application.getUserName());
			pUserName.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					application.setUserName((String) newValue, true);
					pUserName.setSummary(application.getUserName());
					return true;
				}
			});
		}

		// list preference base currency
		lstBaseCurrency = (ListPreference) findPreference(MoneyManagerApplication.PREF_BASE_CURRENCY);
		if (lstBaseCurrency != null) {
			List<TableCurrencyFormats> currencies = application.getAllCurrencyFormats();
			String[] entries = new String[currencies.size()];
			String[] entryValues = new String[currencies.size()];
			// composizione dei due vettori entry
			for (int i = 0; i < currencies.size(); i++) {
				entries[i] = currencies.get(i).getCurrencyName();
				entryValues[i] = ((Integer) currencies.get(i).getCurrencyId()).toString();
			}
			// set value
			lstBaseCurrency.setEntries(entries);
			lstBaseCurrency.setEntryValues(entryValues);
			TableCurrencyFormats tableCurrency = application.getCurrencyFormats(application.getBaseCurrencyId());
			if (tableCurrency != null) {
				lstBaseCurrency.setSummary(tableCurrency.getCurrencyName());
			}
			lstBaseCurrency.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					if (application.setBaseCurrencyId(Integer.parseInt((String) newValue), true)) {
						TableCurrencyFormats tableCurrency = application.getCurrencyFormats(application.getBaseCurrencyId());
						if (tableCurrency != null) {
							lstBaseCurrency.setSummary(tableCurrency.getCurrencyName());
						}
					}
					return true;
				}
			});
		}

		// checkbox on open and favorite account
		chkAccountOpen = (CheckBoxPreference) findPreference(MoneyManagerApplication.PREF_ACCOUNT_OPEN_VISIBLE);
		chkAccountFav = (CheckBoxPreference) findPreference(MoneyManagerApplication.PREF_ACCOUNT_FAV_VISIBLE);

		OnPreferenceChangeListener listener = new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				MainActivity.setRestartActivity(true);
				return true;
			}
		};
		// set listener on the checkbox
		chkAccountOpen.setOnPreferenceChangeListener(listener);
		chkAccountFav.setOnPreferenceChangeListener(listener);

		// dropbox account and file
		pDropboxFile = findPreference("dropboxlinkedfile");
		SharedPreferences prefs = getActivity().getSharedPreferences(DropboxActivity.ACCOUNT_PREFS_NAME, 0);
		pDropboxFile.setSummary(prefs.getString(DropboxActivity.REMOTE_FILE, null));
		pDropboxFile.setSelectable(false);

		// dropbox sync mode
		lstDropboxMode = (ListPreference) findPreference(MoneyManagerApplication.PREF_DROPBOX_MODE);
		if (lstDropboxMode != null) {
			lstDropboxMode.setSummary(application.getDropboxSyncMode());
			lstDropboxMode.setDefaultValue(getResources().getStringArray(R.array.dropbox_sync_item)[0]);
			// imposto un listener sulla modifica
			lstDropboxMode.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					lstDropboxMode.setSummary((CharSequence) newValue);
					return true;
				}
			});
			pDatabasePath = findPreference(MoneyManagerApplication.PREF_DATABASE_PATH);
			pDatabasePath.setSummary(MoneyManagerApplication.getDatabasePath(getActivity().getApplicationContext()));
		}

		// list theme
		lstTheme = (ListPreference) findPreference(MoneyManagerApplication.PREF_THEME);
		if (lstTheme != null) {
			lstTheme.setSummary(application.getApplicationTheme());
			lstTheme.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					lstTheme.setSummary((CharSequence) newValue);
					MainActivity.setRestartActivity(true);
					return true;
				}
			});
		}

		// show transaction
		lstShow = (ListPreference) findPreference(MoneyManagerApplication.PREF_SHOW_TRANSACTION);
		if (lstShow != null) {
			lstShow.setSummary(application.getShowTransaction());
			lstShow.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					lstShow.setSummary((CharSequence) newValue);
					return true;
				}
			});
		}

		// type of home
		lstTypeHome = (ListPreference) findPreference(MoneyManagerApplication.PREF_TYPE_HOME);
		if (lstTypeHome != null) {
			if (application.getTypeHome() == MoneyManagerApplication.TYPE_HOME_CLASSIC) {
				lstTypeHome.setSummary(getString(R.string.classic));
			} else {
				lstTypeHome.setSummary(getString(R.string.advance));
			}
			// set default value
			if (application.getDefaultTypeHome() == MoneyManagerApplication.TYPE_HOME_CLASSIC) {
				lstTypeHome.setDefaultValue(getString(R.string.classic));
			} else {
				lstTypeHome.setDefaultValue(getString(R.string.advance));
			}
			// set summary on change
			lstTypeHome.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					lstTypeHome.setSummary(newValue.toString());
					MainActivity.setRestartActivity(true);
					return true;
				}
			});
		}

		// active passcode
		psActivePasscode = (PreferenceScreen) findPreference("activepasscode");
		psActivePasscode.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				passcode = null;
				startActivityPasscode(getString(R.string.enter_your_passcode), REQUEST_INSERT_PASSCODE);
				return false;
			}
		});

		psEditPasscode = (PreferenceScreen) findPreference("editpasscode");
		psEditPasscode.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				passcode = null;
				startActivityPasscode(getString(R.string.enter_your_previous_passcode), REQUEST_EDIT_PASSCODE);
				return false;
			}
		});

		psDisablePasscode = (PreferenceScreen) findPreference("disablepasscode");
		psDisablePasscode.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				passcode = null;
				startActivityPasscode(getString(R.string.enter_your_passcode), REQUEST_DELETE_PASSCODE);
				return false;
			}
		});
	}

	@Override
	public void onPreferenceAttached(PreferenceScreen root, int xmlId) {
		if (root == null)
			return;
	}

	@Override
	public void onResume() {
		super.onResume();
		// check if has passcode
		Passcode passcode = new Passcode(getActivity());

		psActivePasscode.setEnabled(!passcode.hasPasscode());
		psEditPasscode.setEnabled(passcode.hasPasscode());
		psDisablePasscode.setEnabled(passcode.hasPasscode());
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
	}

	private void startActivityPasscode(CharSequence message, int request) {
		Intent intent = new Intent(getActivity(), PasscodeActivity.class);
		// set action and data
		intent.setAction(PasscodeActivity.INTENT_REQUEST_PASSWORD);
		intent.putExtra(PasscodeActivity.INTENT_MESSAGE_TEXT, message);
		// start activity
		startActivityForResult(intent, request);
	}
}