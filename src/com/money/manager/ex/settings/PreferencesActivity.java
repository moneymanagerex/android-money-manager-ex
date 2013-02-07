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
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.money.manager.ex.MainActivity;
import com.money.manager.ex.MoneyManagerApplication;
import com.money.manager.ex.PasscodeActivity;
import com.money.manager.ex.R;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.core.Passcode;
import com.money.manager.ex.database.TableCurrencyFormats;
import com.money.manager.ex.dropbox.DropboxActivity;

public class PreferencesActivity extends SherlockPreferenceActivity {
	private static final String LOGCAT = PreferencesActivity.class.getSimpleName();
	
	private static final int REQUEST_INSERT_PASSCODE = 1;
	private static final int REQUEST_EDIT_PASSCODE = 2;
	private static final int REQUEST_DELETE_PASSCODE = 3;
	private static final int REQUEST_REINSERT_PASSCODE = 10;
	// application
	private MoneyManagerApplication application;
	// id preference
	private Preference pUserName, pDatabasePath, pDropboxFile, pFinancialDay;
	private PreferenceScreen psActivePasscode, psEditPasscode, psDisablePasscode;
	private ListPreference lstDateFormat, lstBaseCurrency, lstFinancialMonth, lstDropboxMode, lstTheme, lstShow, lstTypeHome;
	private CheckBoxPreference chkAccountOpen, chkAccountFav;

	private static String passcode = null;

	private String getDateFormatFromMask(String mask) {
		for (int i = 0; i < getResources().getStringArray(R.array.date_format_mask).length; i ++) {
			if (mask.equals(getResources().getStringArray(R.array.date_format_mask)[i])) {
				return getResources().getStringArray(R.array.date_format)[i];
			}
		}
		return null;
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Passcode pass = new Passcode(this);
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == REQUEST_INSERT_PASSCODE && data != null) {
				// check if reinsert
				passcode = data.getStringExtra(PasscodeActivity.INTENT_RESULT_PASSCODE);
				startActivityPasscode(getString(R.string.reinsert_your_passcode), REQUEST_REINSERT_PASSCODE);
			}
			if (requestCode == REQUEST_REINSERT_PASSCODE && data != null) {
				if (passcode.equals(data.getStringExtra(PasscodeActivity.INTENT_RESULT_PASSCODE))) {
					if (!pass.setPasscode(passcode)) {
						Toast.makeText(this, R.string.passcode_not_update, Toast.LENGTH_LONG).show();
					}
				} else {
					Toast.makeText(this, R.string.passocde_no_macth, Toast.LENGTH_LONG).show();
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
						Toast.makeText(this, R.string.passocde_no_macth, Toast.LENGTH_LONG).show();
				}
			}
			if (requestCode == REQUEST_DELETE_PASSCODE && data != null) {
				// check if reinsert
				passcode = data.getStringExtra(PasscodeActivity.INTENT_RESULT_PASSCODE);
				String passcodedb = pass.getPasscode();
				if (passcodedb != null && passcode != null) {
					if (passcodedb.equals(passcode)) {
						if (!pass.cleanPasscode()) {
							Toast.makeText(this, R.string.passcode_not_update, Toast.LENGTH_LONG).show();
						}
					} else
						Toast.makeText(this, R.string.passocde_no_macth, Toast.LENGTH_LONG).show();
				}
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		application = (MoneyManagerApplication) this.getApplication();
		application.setThemeApplication(this);
		final Core core = new Core(this);
		
		super.onCreate(savedInstanceState);

		// set layout
		addPreferencesFromResource(R.xml.prefrences);
		PreferenceManager.getDefaultSharedPreferences(this);

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
		// list date format
		lstDateFormat = (ListPreference) findPreference(MoneyManagerApplication.PREF_DATE_FORMAT);
		if (lstDateFormat != null) {
			lstDateFormat.setEntries(getResources().getStringArray(R.array.date_format));
			lstDateFormat.setEntryValues(getResources().getStringArray(R.array.date_format_mask));
			//set summary
			String value = core.getInfoValue(Core.INFO_NAME_DATEFORMAT);
			lstDateFormat.setSummary(getDateFormatFromMask(value));
			lstDateFormat.setValue(value);
			//on change
			lstDateFormat.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
				
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					if (core.setInfoValue(Core.INFO_NAME_DATEFORMAT, (String)newValue)) {
						lstDateFormat.setSummary(getDateFormatFromMask((String)newValue));
						return true;
					} else {
						return false;
					}
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
		// financial day and month
		pFinancialDay = (Preference)findPreference(MoneyManagerApplication.PREF_FINANCIAL_YEAR_STARTDATE);
		if (pFinancialDay != null) {
			pFinancialDay.setSummary(core.getInfoValue(Core.INFO_NAME_FINANCIAL_YEAR_START_DAY));
			if (pFinancialDay.getSummary() != null) {
				pFinancialDay.setDefaultValue(pFinancialDay.getSummary().toString());
			}
			pFinancialDay.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					int day = 0;
					try { 
						day = Integer.parseInt((String)newValue);
						if (!(day >= 1 && day <= 31)) {
							return false;
						}
						if (core.setInfoValue(Core.INFO_NAME_FINANCIAL_YEAR_START_DAY, Integer.toString(day))) {
							pFinancialDay.setSummary(Integer.toString(day));
						}
						return true;
					} catch (Exception e){
						Log.e(LOGCAT, e.getMessage());
					}
					return false;
				}
			});
		}

		lstFinancialMonth = (ListPreference) findPreference(MoneyManagerApplication.PREF_FINANCIAL_YEAR_STARTMONTH);
		if (lstFinancialMonth != null) {
			lstFinancialMonth.setEntries(core.getListMonths());
			lstFinancialMonth.setEntryValues(new String[] {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11"});
			// get current month
			String currentMonth = core.getInfoValue(Core.INFO_NAME_FINANCIAL_YEAR_START_MONTH);
			if (!TextUtils.isEmpty(currentMonth)) {
				if (Integer.parseInt(currentMonth) > -1 && Integer.parseInt(currentMonth) < lstFinancialMonth.getEntries().length) {
					lstFinancialMonth.setSummary(lstFinancialMonth.getEntries()[Integer.parseInt(currentMonth) - 1]);
					lstFinancialMonth.setValue(currentMonth);
				}
			}
			lstFinancialMonth.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
				
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					try {
						int value = Integer.parseInt(newValue.toString());
						if (value > -1 && value < lstFinancialMonth.getEntries().length) {
							if (core.setInfoValue(Core.INFO_NAME_FINANCIAL_YEAR_START_MONTH, Integer.toString(value + 1))) {
								lstFinancialMonth.setSummary(lstFinancialMonth.getEntries()[value]);
								return true;
							}
						}
					} catch (Exception e) {
						Log.e(LOGCAT, e.getMessage());
						return false;
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
		SharedPreferences prefs = this.getSharedPreferences(DropboxActivity.ACCOUNT_PREFS_NAME, 0);
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
			pDatabasePath.setSummary(MoneyManagerApplication.getDatabasePath(this.getApplicationContext()));
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
		
		//version name code etc...
		try {
			PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
			if ((Preference)findPreference("versionname") != null) {
				((Preference)findPreference("versionname")).setSummary(info.versionName);
			}
			if ((Preference)findPreference("versioncode") != null) {
				((Preference)findPreference("versioncode")).setSummary(Integer.toString(info.versionCode));
			}
			if ((Preference)findPreference("versiondate") != null) {
				((Preference)findPreference("versiondate")).setSummary(getString(R.string.application_build));
			}
		} catch (Exception e) {
			Log.e(LOGCAT, e.getMessage());
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
		super.onPreferenceTreeClick(preferenceScreen, preference);
		if (preference != null)
			if (preference instanceof PreferenceScreen)
				if (((PreferenceScreen) preference).getDialog() != null)
					((PreferenceScreen) preference).getDialog().getWindow().getDecorView()
							.setBackgroundDrawable(this.getWindow().getDecorView().getBackground().getConstantState().newDrawable());
		return false;
	}


	@Override
	public void onResume() {
		super.onResume();
		// check if has passcode
		Passcode passcode = new Passcode(this);

		psActivePasscode.setEnabled(!passcode.hasPasscode());
		psEditPasscode.setEnabled(passcode.hasPasscode());
		psDisablePasscode.setEnabled(passcode.hasPasscode());
	}
	
	private void startActivityPasscode(CharSequence message, int request) {
		Intent intent = new Intent(this, PasscodeActivity.class);
		// set action and data
		intent.setAction(PasscodeActivity.INTENT_REQUEST_PASSWORD);
		intent.putExtra(PasscodeActivity.INTENT_MESSAGE_TEXT, message);
		// start activity
		startActivityForResult(intent, request);
	}
}