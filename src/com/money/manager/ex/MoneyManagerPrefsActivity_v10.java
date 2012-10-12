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
package com.money.manager.ex;

import java.util.List;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import com.money.manager.ex.R;
import com.money.manager.ex.database.TableCurrencyFormats;
import com.money.manager.ex.dropbox.DropboxActivity;
/**
 * 
 * @author Alessandro Lazzari (lazzari.ale@gmail.com)
 * @version 1.0.0
 */
public class MoneyManagerPrefsActivity_v10 extends PreferenceActivity {
	// application
	private MoneyManagerApplication application;
	// id preference
	private Preference pUserName, pDatabasePath, pDropboxFile;
	private ListPreference lstBaseCurrency, lstDropboxMode, lstTheme, lstShow, lstTypeHome;
	private CheckBoxPreference chkAccountOpen, chkAccountFav;
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		application = (MoneyManagerApplication)this.getApplication();
		application.setThemeApplication(this);
		
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
		
		// list preference base currency
		lstBaseCurrency = (ListPreference) findPreference(MoneyManagerApplication.PREF_BASE_CURRENCY);
		if (lstBaseCurrency != null) {
			List<TableCurrencyFormats> currencies = application.getAllCurrencyFormats();
			String[] entries = new String[currencies.size()];
			String[] entryValues = new String[currencies.size()];
			// composizione dei due vettori entry
			for(int i = 0; i < currencies.size(); i ++) {
				entries[i] = currencies.get(i).getCurrencyName();
				entryValues[i] = ((Integer)currencies.get(i).getCurrencyId()).toString();
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
					if (application.setBaseCurrencyId(Integer.parseInt((String)newValue), true)) {
						TableCurrencyFormats tableCurrency = application.getCurrencyFormats(application.getBaseCurrencyId());
						if (tableCurrency != null) {
							lstBaseCurrency.setSummary(tableCurrency.getCurrencyName());
						}
					}
					return true;
				}
			});
		}

		//checkbox on open and favorite account
		chkAccountOpen = (CheckBoxPreference)findPreference(MoneyManagerApplication.PREF_ACCOUNT_OPEN_VISIBLE);
		chkAccountFav = (CheckBoxPreference)findPreference(MoneyManagerApplication.PREF_ACCOUNT_FAV_VISIBLE);
		
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
	}
}
