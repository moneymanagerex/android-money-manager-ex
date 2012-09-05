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
 * along with getActivity() program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 ******************************************************************************/
package com.android.money.manager.ex;

import java.util.List;

import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;

import com.android.money.manager.ex.database.TableCurrencyFormats;
import com.android.money.manager.ex.dropbox.DropboxActivity;
/**
 * 
 * @author Alessandro Lazzari (lazzari.ale@gmail.com)
 * @version 1.0.0
 */
public class MoneyManagerPrefsActivity extends FragmentActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		// theme
		((MoneyManagerApplication)getApplication()).setThemeApplication(this);
		// Display the fragment as the main content.
        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new PrefsFragment()).commit();
	}
	
	public static class PrefsFragment extends PreferenceFragment {
		// prendo la referenza all'applicazione
		private MoneyManagerApplication application;
		// ID delle preferenze
		private Preference pUserName;
		private Preference pDatabasePath;
		private Preference pDropboxFile;
		private ListPreference lstBaseCurrency;
		private ListPreference lstDropboxMode;
		private ListPreference lstTheme;
		private CheckBoxPreference chkAccountOpen;
		private CheckBoxPreference chkAccountFav;
		@SuppressWarnings("deprecation")
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			// prendo la referenza all'applicazione
			application = (MoneyManagerApplication)getActivity().getApplication();
			// set thmee
			application.setThemeApplication(getActivity());
			// set layout
			addPreferencesFromResource(R.xml.prefrences);
			/*if (application.getApplicationTheme().equalsIgnoreCase(getResources().getString(R.string.theme_light))) {
				getListView().setBackgroundResource(android.R.color.white);
			}*/
			PreferenceManager.getDefaultSharedPreferences(getActivity());
			// prendo la preferenza della username e imposto la summery
			pUserName = findPreference(MoneyManagerApplication.PREF_USER_NAME);
			if (pUserName != null) {
				pUserName.setSummary(application.getUserName());
				// imposto il listener per la modifica al database
				pUserName.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
					@Override
					public boolean onPreferenceChange(Preference preference, Object newValue) {
						application.setUserName((String) newValue, true);
						pUserName.setSummary(application.getUserName());
						return true;
					}
				});
			}
			// prendo la ListPreference della BaseCurrency
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
				// imposto i valori
				lstBaseCurrency.setEntries(entries);
				lstBaseCurrency.setEntryValues(entryValues);
				// imposto il summary
				TableCurrencyFormats tableCurrency = application.getCurrencyFormats(application.getBaseCurrencyId());
				if (tableCurrency != null) {
					lstBaseCurrency.setSummary(tableCurrency.getCurrencyName());
				}
				// imposto un listener sulla modifica
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
			// prendo il check open per la gestione se devo riavviare l'interfaccia
			chkAccountOpen = (CheckBoxPreference)findPreference(MoneyManagerApplication.PREF_ACCOUNT_OPEN_VISIBLE);
			chkAccountFav = (CheckBoxPreference)findPreference(MoneyManagerApplication.PREF_ACCOUNT_FAV_VISIBLE);
			// definizione del listener di mod
			OnPreferenceChangeListener listener = new OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					// imposto che la MainActivity va riavviata
					MainActivity.setRestartActivity(true);
					return true;
				}
			};
			// linster sul cambiamento
			chkAccountOpen.setOnPreferenceChangeListener(listener);
			chkAccountFav.setOnPreferenceChangeListener(listener);
			// sezione dropbox account e file
			pDropboxFile = findPreference("dropboxlinkedfile");
			SharedPreferences prefs = getActivity().getSharedPreferences(DropboxActivity.ACCOUNT_PREFS_NAME, 0);
			pDropboxFile.setSummary(prefs.getString(DropboxActivity.REMOTE_FILE, null));
			// set che non sono selezionabili
			pDropboxFile.setSelectable(false);
			// gestione della listview sulle modalit� di sincronizzazione
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
			lstTheme = (ListPreference) findPreference(MoneyManagerApplication.PREF_THEME);
			if (lstTheme != null) {
				lstTheme.setSummary(application.getApplicationTheme());
				// imposto un listener sulla modifica
				lstTheme.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
					@Override
					public boolean onPreferenceChange(Preference preference, Object newValue) {
						lstTheme.setSummary((CharSequence) newValue);
						MainActivity.setRestartActivity(true);
						return true;
					}
				});
			}
		}
	}
	
}
