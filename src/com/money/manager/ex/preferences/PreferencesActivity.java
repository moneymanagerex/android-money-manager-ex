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
package com.money.manager.ex.preferences;

import java.io.File;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.money.manager.ex.Constants;
import com.money.manager.ex.DonateActivity;
import com.money.manager.ex.HelpActivity;
import com.money.manager.ex.MainActivity;
import com.money.manager.ex.MoneyManagerApplication;
import com.money.manager.ex.PasscodeActivity;
import com.money.manager.ex.R;
import com.money.manager.ex.about.AboutActivity;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.core.CurrencyUtils;
import com.money.manager.ex.core.Passcode;
import com.money.manager.ex.database.MoneyManagerOpenHelper;
import com.money.manager.ex.database.TableCurrencyFormats;
import com.money.manager.ex.dropbox.DropboxBrowserActivity;
import com.money.manager.ex.dropbox.DropboxHelper;
import com.money.manager.ex.dropbox.DropboxReceiver;
import com.money.manager.ex.dropbox.DropboxServiceIntent;
import com.money.manager.ex.fragment.TipsDialogFragment;
import com.money.manager.ex.view.RobotoView;

@SuppressWarnings("deprecation")
public class PreferencesActivity extends SherlockPreferenceActivity {
	private static final String LOGCAT = PreferencesActivity.class.getSimpleName();
	
	private static final int REQUEST_INSERT_PASSCODE = 1;
	private static final int REQUEST_EDIT_PASSCODE = 2;
	private static final int REQUEST_DELETE_PASSCODE = 3;
	private static final int REQUEST_REINSERT_PASSCODE = 10;
	private static final int REQUEST_DROPBOX_FILE = 20;

	// application
	private MoneyManagerApplication application;
	private CurrencyUtils currencyUtils;
	
	// core application
	private Core mCore;
	// dropbox object
	private DropboxHelper mDropboxHelper;
	private boolean mDropboxLoginBegin = false;
	// passcode
	private static String passcode = null;

	private String getDateFormatFromMask(String mask) {
		if (!TextUtils.isEmpty(mask)) {
			for (int i = 0; i < getResources().getStringArray(R.array.date_format_mask).length; i++) {
				if (mask.equals(getResources().getStringArray(R.array.date_format_mask)[i])) {
					return getResources().getStringArray(R.array.date_format)[i];
				}
			}
		}
		return null;
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case REQUEST_INSERT_PASSCODE:
		case REQUEST_REINSERT_PASSCODE:
		case REQUEST_EDIT_PASSCODE:
		case REQUEST_DELETE_PASSCODE:
			onActivityResultPasscode(requestCode, resultCode, data);
			break;
		case REQUEST_DROPBOX_FILE:
			onActivityResultDropbox(requestCode, resultCode, data);
			break;
		}
	}
	
	public void onActivityResultDropbox(int requestCode, int resultCode, Intent data) { 
		if (resultCode == Activity.RESULT_OK && data != null) {
			final Preference pDropboxFile = findPreference(PreferencesConstant.PREF_DROPBOX_LINKED_FILE);
			if (pDropboxFile != null) {
				CharSequence oldFile = "", newFile = "";
				if (!TextUtils.isEmpty(pDropboxFile.getSummary())) {
					oldFile = pDropboxFile.getSummary();
				}
				newFile = data.getStringExtra(DropboxBrowserActivity.INTENT_DROBPOXFILE_PATH);
				
				if (newFile == null) return;
						
				// save value
				mDropboxHelper.setLinkedRemoteFile(newFile.toString());
				pDropboxFile.setSummary(newFile);
				// check if files is modified
				if (!oldFile.equals(newFile)) {
					// force download file
					downloadFileFromDropbox((String) newFile);
				}
			}
		}
	}
	
	public void downloadFileFromDropbox(String fileDropbox) {
		Core core = new Core(getApplicationContext());
		// compose intent to lauch service for download
		Intent service = new Intent(getApplicationContext(), DropboxServiceIntent.class);
		service.setAction(DropboxServiceIntent.INTENT_ACTION_DOWNLOAD);
		service.putExtra(DropboxServiceIntent.INTENT_EXTRA_LOCAL_FILE, core.getExternalStorageDirectoryDropboxApplication().getPath() + fileDropbox);
		service.putExtra(DropboxServiceIntent.INTENT_EXTRA_REMOTE_FILE, fileDropbox);
		// toast to show 
		Toast.makeText(getApplicationContext(), R.string.dropbox_download_is_starting, Toast.LENGTH_LONG).show();
		// start service
		startService(service);
	}
	 
	public void uploadFileFromDropbox(String dropboxFile) {
		// compose intent to launch service for download
		Intent service = new Intent(getApplicationContext(), DropboxServiceIntent.class);
		service.setAction(DropboxServiceIntent.INTENT_ACTION_UPLOAD);
		service.putExtra(DropboxServiceIntent.INTENT_EXTRA_LOCAL_FILE, MoneyManagerApplication.getDatabasePath(getApplicationContext()));
		service.putExtra(DropboxServiceIntent.INTENT_EXTRA_REMOTE_FILE, dropboxFile);
		// toast to show 
		Toast.makeText(getApplicationContext(), R.string.dropbox_upload_is_starting, Toast.LENGTH_LONG).show();
		// start service
		startService(service);
	}
	
	public void onActivityResultPasscode(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_INSERT_PASSCODE:
		case REQUEST_REINSERT_PASSCODE:
		case REQUEST_EDIT_PASSCODE:
		case REQUEST_DELETE_PASSCODE:
			if (resultCode == Activity.RESULT_OK) {
				Passcode pass = new Passcode(this);
				// insert passcode
				if (requestCode == REQUEST_INSERT_PASSCODE && data != null) {
					// check if reinsert
					passcode = data.getStringExtra(PasscodeActivity.INTENT_RESULT_PASSCODE);
					startActivityPasscode(getString(R.string.reinsert_your_passcode), REQUEST_REINSERT_PASSCODE);
				}
				// re-insert passcode
				if (requestCode == REQUEST_REINSERT_PASSCODE && data != null) {
					if (passcode.equals(data.getStringExtra(PasscodeActivity.INTENT_RESULT_PASSCODE))) {
						if (!pass.setPasscode(passcode)) {
							Toast.makeText(this, R.string.passcode_not_update, Toast.LENGTH_LONG).show();
						}
					} else {
						Toast.makeText(this, R.string.passocde_no_macth, Toast.LENGTH_LONG).show();
					}
				}
				// edit passcode
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
				// delete passcode
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
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		application = (MoneyManagerApplication) this.getApplication();
		currencyUtils = new CurrencyUtils(this);
		
		mCore = new Core(this);
		
		// set theme application
		try {
			setTheme(mCore.getThemeApplication());
		} catch (Exception e) {
			Log.e(LOGCAT, e.getMessage());
		}
		
		super.onCreate(savedInstanceState);
		PreferenceManager.setDefaultValues(this, R.xml.prefrences, false);
		// set layout
		addPreferencesFromResource(R.xml.prefrences);
		PreferenceManager.getDefaultSharedPreferences(this);
		
		// general screen preference
		onCreateScreenPreferenceGeneral();
		// display screen preference
		onCreateScreenPreferenceDisplay();
		
		// database preference
		onCreateScreenPreferenceDatabase();
		
		// dropbox preference screen
		mDropboxHelper = DropboxHelper.getInstance(getApplicationContext());
		onCreateScreenPreferenceDropbox();
		
		// security preference screen
		onCreateScreenPreferenceSecurity();
		
		// donate preference screen
		onCreateScreenPreferenceDonate();
		
		// about preference screen
		onCreateScreenPreferenceInfo();
		
		// manage intent
		if (getIntent() != null) {
			if (!TextUtils.isEmpty(getIntent().getStringExtra(Constants.INTENT_REQUEST_PREFERENCES_SCREEN))) {
				try {
					PreferenceScreen screen = getPreferenceScreen();
					Preference preference = findPreference(getIntent().getStringExtra(Constants.INTENT_REQUEST_PREFERENCES_SCREEN));
					if (preference != null) {
						screen.onItemClick(null, null, preference.getOrder(), 0);
					}
				} catch (Exception e) {
					Log.e(LOGCAT, e.getMessage());
				}
			}
		}
		
		/*PreferenceScreen screen = getPreferenceScreen();
		if (screen != null) {
			screen.onItemClick(null, null, findPreference(PreferencesConstant.PREF_DROPBOX_HOWITWORKS).getOrder(), 0);
		}*/
	}

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

	public void onCreateScreenPreferenceGeneral() {
		// application locale
		final ListPreference lstLocaleApp = (ListPreference) findPreference(PreferencesConstant.PREF_LOCALE);
		if (lstLocaleApp != null) {
			String summary = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(PreferencesConstant.PREF_LOCALE, "");
			setSummaryListPreference(lstLocaleApp, summary, R.array.application_locale_values, R.array.application_locale_entries);
			lstLocaleApp.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
				
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					setSummaryListPreference(preference, String.valueOf(newValue), R.array.application_locale_values, R.array.application_locale_entries);
					MainActivity.setRestartActivity(true);
					return true;
				}
			});
		}
		// preference username
		final Preference pUserName = findPreference(PreferencesConstant.PREF_USER_NAME);
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
		final ListPreference lstDateFormat = (ListPreference) findPreference(PreferencesConstant.PREF_DATE_FORMAT);
		if (lstDateFormat != null) {
			lstDateFormat.setEntries(getResources().getStringArray(R.array.date_format));
			lstDateFormat.setEntryValues(getResources().getStringArray(R.array.date_format_mask));
			//set summary
			String value = mCore.getInfoValue(Constants.INFOTABLE_DATEFORMAT);
			lstDateFormat.setSummary(getDateFormatFromMask(value));
			lstDateFormat.setValue(value);
			//on change
			lstDateFormat.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
				
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					if (mCore.setInfoValue(Constants.INFOTABLE_DATEFORMAT, (String)newValue)) {
						lstDateFormat.setSummary(getDateFormatFromMask((String)newValue));
						return true;
					} else {
						return false;
					}
				}
			});
		}
		
		// list preference base currency
		final ListPreference lstBaseCurrency = (ListPreference) findPreference(PreferencesConstant.PREF_BASE_CURRENCY);
		if (lstBaseCurrency != null) {
			List<TableCurrencyFormats> currencies = currencyUtils.getAllCurrencyFormats();
			String[] entries = new String[currencies.size()];
			String[] entryValues = new String[currencies.size()];
			// list of currency
			for (int i = 0; i < currencies.size(); i++) {
				entries[i] = currencies.get(i).getCurrencyName();
				entryValues[i] = ((Integer) currencies.get(i).getCurrencyId()).toString();
			}
			// set value
			lstBaseCurrency.setEntries(entries);
			lstBaseCurrency.setEntryValues(entryValues);
			TableCurrencyFormats tableCurrency = currencyUtils.getTableCurrencyFormats(currencyUtils.getBaseCurrencyId());
			if (tableCurrency != null) {
				lstBaseCurrency.setSummary(tableCurrency.getCurrencyName());
			}
			lstBaseCurrency.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					if (currencyUtils.setBaseCurrencyId(Integer.valueOf(String.valueOf(newValue)))) {
						currencyUtils.reInit();
						TableCurrencyFormats tableCurrency = currencyUtils.getTableCurrencyFormats(currencyUtils.getBaseCurrencyId());
						if (tableCurrency != null) {
							lstBaseCurrency.setSummary(tableCurrency.getCurrencyName());
						}
					}
					return true;
				}
			});
		}
		
		// default status
		final ListPreference lstDefaultStatus = (ListPreference)findPreference(PreferencesConstant.PREF_DEFAULT_STATUS);
		if (lstDefaultStatus != null) {
			setSummaryListPreference(lstDefaultStatus, lstDefaultStatus.getValue(), R.array.status_values, R.array.status_items);
			lstDefaultStatus.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {	
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					setSummaryListPreference(lstDefaultStatus, newValue.toString(), R.array.status_values, R.array.status_items);
					return true;
				}
			});
		}
		
		//default payee
		final ListPreference lstDefaultPayee = (ListPreference)findPreference(PreferencesConstant.PREF_DEFAULT_PAYEE);
		if (lstDefaultPayee != null) {
			setSummaryListPreference(lstDefaultPayee, lstDefaultPayee.getValue(), R.array.new_transaction_dialog_values, R.array.new_transaction_dialog_items);
			lstDefaultPayee.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {	
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					setSummaryListPreference(lstDefaultPayee, newValue.toString(), R.array.new_transaction_dialog_values, R.array.new_transaction_dialog_items);
					return true;
				}
			});
		} 
		
		// financial day and month
		final Preference pFinancialDay = (Preference)findPreference(PreferencesConstant.PREF_FINANCIAL_YEAR_STARTDATE);
		if (pFinancialDay != null) {
			pFinancialDay.setSummary(mCore.getInfoValue(Constants.INFOTABLE_FINANCIAL_YEAR_START_DAY));
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
						if (mCore.setInfoValue(Constants.INFOTABLE_FINANCIAL_YEAR_START_DAY, Integer.toString(day))) {
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

		final ListPreference lstFinancialMonth = (ListPreference) findPreference(PreferencesConstant.PREF_FINANCIAL_YEAR_STARTMONTH);
		if (lstFinancialMonth != null) {
			lstFinancialMonth.setEntries(mCore.getListMonths());
			lstFinancialMonth.setEntryValues(new String[] {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11"});
			lstFinancialMonth.setDefaultValue("0");
			// get current month
			try {
				String currentMonth = mCore.getInfoValue(Constants.INFOTABLE_FINANCIAL_YEAR_START_MONTH);
				if ((!TextUtils.isEmpty(currentMonth)) && Core.StringUtils.isNumeric(currentMonth)) {
					int month = Integer.parseInt(currentMonth) - 1;
					if (month > -1 && month < lstFinancialMonth.getEntries().length) {
						lstFinancialMonth.setSummary(lstFinancialMonth.getEntries()[month]);
						lstFinancialMonth.setValue(Integer.toString(month));
					}
				}
			} catch (Exception e) {
				Log.e(LOGCAT, e.getMessage());
			}
			lstFinancialMonth.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
				
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					try {
						int value = Integer.parseInt(newValue.toString());
						if (value > -1 && value < lstFinancialMonth.getEntries().length) {
							if (mCore.setInfoValue(Constants.INFOTABLE_FINANCIAL_YEAR_START_MONTH, Integer.toString(value + 1))) {
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
	}

	public void onCreateScreenPreferenceDisplay() {
		// checkbox on open and favorite account
		final CheckBoxPreference chkAccountOpen = (CheckBoxPreference) findPreference(PreferencesConstant.PREF_ACCOUNT_OPEN_VISIBLE);
		final CheckBoxPreference chkAccountFav = (CheckBoxPreference) findPreference(PreferencesConstant.PREF_ACCOUNT_FAV_VISIBLE);

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

		// show transaction
		final ListPreference lstShow = (ListPreference) findPreference(PreferencesConstant.PREF_SHOW_TRANSACTION);
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
		
		// font type
		final ListPreference lstFont = (ListPreference)findPreference(PreferencesConstant.PREF_APPLICATION_FONT);
		if (lstFont != null) {
			lstFont.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					if (newValue instanceof String && Core.StringUtils.isNumeric(newValue.toString())) {
						RobotoView.setUserFont(Integer.parseInt(newValue.toString()));
						return true;
					}
					return false;
				}
			});
		}
		
		//font size
		final ListPreference lstFontSize = (ListPreference)findPreference(PreferencesConstant.PREF_APPLICATION_FONT_SIZE);
		if (lstFontSize != null) {
			lstFontSize.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
				
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					RobotoView.setUserFontSize(getApplicationContext(), newValue.toString());
					return true;
				}
			});
		}
		
		//theme
		final ListPreference lstTheme = (ListPreference)findPreference(PreferencesConstant.PREF_THEME);
		if (lstTheme != null) {
			lstTheme.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
				
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					MainActivity.setRestartActivity(true);
					return true;
				}
			});
		}
	}

	public void onCreateScreenPreferenceSecurity() {
		// active passcode
		final PreferenceScreen psActivePasscode = (PreferenceScreen) findPreference(PreferencesConstant.PREF_ACTIVE_PASSCODE);
		psActivePasscode.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				passcode = null;
				startActivityPasscode(getString(R.string.enter_your_passcode), REQUEST_INSERT_PASSCODE);
				return false;
			}
		});

		final PreferenceScreen psEditPasscode = (PreferenceScreen) findPreference(PreferencesConstant.PREF_EDIT_PASSCODE);
		psEditPasscode.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				passcode = null;
				startActivityPasscode(getString(R.string.enter_your_previous_passcode), REQUEST_EDIT_PASSCODE);
				return false;
			}
		});

		final PreferenceScreen psDisablePasscode = (PreferenceScreen) findPreference(PreferencesConstant.PREF_DISABLE_PASSCODE);
		psDisablePasscode.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				passcode = null;
				startActivityPasscode(getString(R.string.enter_your_passcode), REQUEST_DELETE_PASSCODE);
				return false;
			}
		});
	}
	
	public void onCreateScreenPreferenceDatabase() {
		final PreferenceScreen pMoveDatabase = (PreferenceScreen) findPreference(PreferencesConstant.PREF_DATABASE_BACKUP);
		if (pMoveDatabase != null) {
			pMoveDatabase.setOnPreferenceClickListener(new OnPreferenceClickListener() {
				
				@Override
				public boolean onPreferenceClick(Preference preference) {
					// copy files
					Core core = new Core(PreferencesActivity.this);
					File newDatabases = core.backupDatabase();
					if (newDatabases != null) {
						Toast.makeText(PreferencesActivity.this, Html.fromHtml(getString(R.string.database_has_been_moved, "<b>" + newDatabases.getAbsolutePath() + "</b>")), Toast.LENGTH_LONG).show();
						//MainActivity.changeDatabase(newDatabases.getAbsolutePath());
						// save the database file
						MoneyManagerApplication.setDatabasePath(getApplicationContext(), newDatabases.getAbsolutePath());
						MoneyManagerApplication.resetDonateDialog(getApplicationContext());
						// set to restart activity
						MainActivity.setRestartActivity(true);
					} else {
						Toast.makeText(PreferencesActivity.this, R.string.copy_database_on_external_storage_failed, Toast.LENGTH_LONG).show();
					}
					return false;
				}
			});
		}
		final PreferenceScreen pDatabasePath = (PreferenceScreen) findPreference(PreferencesConstant.PREF_DATABASE_PATH);
		pDatabasePath.setSummary(MoneyManagerApplication.getDatabasePath(this.getApplicationContext()));
		//sqlite version
		PreferenceScreen pSQLiteVersion = (PreferenceScreen)findPreference(PreferencesConstant.PREF_SQLITE_VERSION);
		if (pSQLiteVersion != null) {
			MoneyManagerOpenHelper helper = new MoneyManagerOpenHelper(this);
			String sqliteVersion = helper.getSQLiteVersion();
			if (sqliteVersion != null) pSQLiteVersion.setSummary(sqliteVersion);
			helper.close();
		}
	}
	
	public void onCreateScreenPreferenceDropbox() {
		final PreferenceScreen pDropbox = (PreferenceScreen) findPreference(PreferencesConstant.PREF_DROPBOX_HOWITWORKS);
		if (pDropbox != null) {
			pDropbox.setOnPreferenceClickListener(new OnPreferenceClickListener() {
				
				@Override
				public boolean onPreferenceClick(Preference preference) {
					showWebTipsDialog(PreferencesConstant.PREF_DROPBOX_HOWITWORKS, getString(R.string.dropbox_how_it_works), R.raw.help_dropbox, false);
					return false;
				}
			});
		}
		
		//login to dropbox
		final Preference pDropboxLink = findPreference(PreferencesConstant.PREF_DROPBOX_LINK);
		pDropboxLink.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				mDropboxHelper.logIn();
				mDropboxLoginBegin = true;
				return false;
			}
		});
		
		//logout from dropbox
		final Preference pDropboxUnlink = findPreference(PreferencesConstant.PREF_DROPBOX_UNLINK);
		pDropboxUnlink.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				mDropboxHelper.logOut();
				/* SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
				sharedPreferences.edit().putString(PreferencesConstant.PREF_DROPBOX_TIMES_REPEAT, null).commit(); */
				mDropboxHelper.sendBroadcastStartServiceScheduled(DropboxReceiver.ACTION_CANCEL);
				// refresh ui
				onResume();
				return false;
			}
		});
		
		//wiki
		Preference pWiki = findPreference(PreferencesConstant.PREF_DROPBOX_WIKI);
		if (pWiki != null) {
			pWiki.setOnPreferenceClickListener(new OnPreferenceClickListener() {
				
				@Override
				public boolean onPreferenceClick(Preference preference) {
					//startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://code.google.com/p/android-money-manager-ex/wiki/DropboxSync")));
					Intent intent = new Intent(PreferencesActivity.this, HelpActivity.class);
					intent.setData(Uri.parse("android.resource://com.money.manager.ex/" + R.raw.help_dropbox));
					//intent.setData(Uri.parse("http://code.google.com/p/android-money-manager-ex/wiki/DropboxSync"));
					startActivity(intent);
					return false;
				}
			});
		}
		
		//link file
		final Preference pDropboxFile = findPreference(PreferencesConstant.PREF_DROPBOX_LINKED_FILE);
		if (pDropboxFile != null) {
			pDropboxFile.setSummary(mDropboxHelper.getLinkedRemoteFile());
			// check if summary is null and
			if (TextUtils.isEmpty(pDropboxFile.getSummary())) {
				pDropboxFile.setSummary(R.string.click_to_select_file_dropbox);
			}
			// open DropboxBrowse Activity
			pDropboxFile.setOnPreferenceClickListener(new OnPreferenceClickListener() {
				
				@Override
				public boolean onPreferenceClick(Preference preference) {
					Intent intent = new Intent(PreferencesActivity.this, DropboxBrowserActivity.class);
					intent.putExtra(DropboxBrowserActivity.INTENT_DROBPOXFILE_PATH, mDropboxHelper.getLinkedRemoteFile());
					startActivityForResult(intent, REQUEST_DROPBOX_FILE);
					return false;
				}
			});
		}
		
		//force download
		PreferenceScreen pDownload = (PreferenceScreen) findPreference(PreferencesConstant.PREF_DROPBOX_DOWNLOAD);
		if (pDownload != null) {
			pDownload.setOnPreferenceClickListener(new OnPreferenceClickListener() {
				
				@Override
				public boolean onPreferenceClick(Preference preference) {
					if (!TextUtils.isEmpty(mDropboxHelper.getLinkedRemoteFile()))
						downloadFileFromDropbox(mDropboxHelper.getLinkedRemoteFile());
					return false;
				}
			});
		}
		
		//force upload
		PreferenceScreen pUpload = (PreferenceScreen) findPreference(PreferencesConstant.PREF_DROPBOX_UPLOAD);
		if (pUpload != null) {
			pUpload.setOnPreferenceClickListener(new OnPreferenceClickListener() {
				
				@Override
				public boolean onPreferenceClick(Preference preference) {
					String dropboxFile = mDropboxHelper.getLinkedRemoteFile();
					if (TextUtils.isEmpty(dropboxFile)) {
						dropboxFile = "/" + new File(MoneyManagerApplication.getDatabasePath(getApplicationContext())).getName();
					}
					uploadFileFromDropbox(dropboxFile);
					
					return false;
				}
			});
		}
		
		//times repeat
		ListPreference pRepeats = (ListPreference)findPreference(PreferencesConstant.PREF_DROPBOX_TIMES_REPEAT);
		if (pRepeats != null) {
			pRepeats.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
				
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					mDropboxHelper.sendBroadcastStartServiceScheduled(DropboxReceiver.ACTION_CANCEL);
					mDropboxHelper.sendBroadcastStartServiceScheduled(DropboxReceiver.ACTION_START);
					return true;
				}
			});
		}
	}
	
	public void onCreateScreenPreferenceDonate() { 
		//donate
		final Preference pDonate = (Preference)findPreference(PreferencesConstant.PREF_DONATE);
		if (pDonate != null) {
			pDonate.setOnPreferenceClickListener(new OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					startActivity(new Intent(PreferencesActivity.this, DonateActivity.class));
					return false;
				}
			});
		}
	}
	
	public void onCreateScreenPreferenceInfo() { 
		if (findPreference(PreferencesConstant.PREF_VERSION_NAME) != null) {
			findPreference(PreferencesConstant.PREF_VERSION_NAME).setOnPreferenceClickListener(new OnPreferenceClickListener() {
				
				@Override
				public boolean onPreferenceClick(Preference preference) {
					startActivity(new Intent(PreferencesActivity.this, AboutActivity.class));
					return true;
				}
			});
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
		// check if has passcode
		Passcode passcode = new Passcode(this);

		if (findPreference(PreferencesConstant.PREF_ACTIVE_PASSCODE) != null)
			findPreference(PreferencesConstant.PREF_ACTIVE_PASSCODE).setEnabled(!passcode.hasPasscode());
		if (findPreference(PreferencesConstant.PREF_EDIT_PASSCODE) != null)
			findPreference(PreferencesConstant.PREF_EDIT_PASSCODE).setEnabled(passcode.hasPasscode());
		if (findPreference(PreferencesConstant.PREF_DISABLE_PASSCODE) != null)
			findPreference(PreferencesConstant.PREF_DISABLE_PASSCODE).setEnabled(passcode.hasPasscode());

		// complete process authentication
		if (mDropboxLoginBegin) {
			mDropboxHelper.completeAuthenticationDropbox();
			mDropboxHelper.sendBroadcastStartServiceScheduled(DropboxReceiver.ACTION_START);
			mDropboxLoginBegin = false;
		}

		// dropbox link and unlink
		if (findPreference(PreferencesConstant.PREF_DROPBOX_LINK) != null) {
			findPreference(PreferencesConstant.PREF_DROPBOX_LINK).setSelectable(!mDropboxHelper.isLinked());
			findPreference(PreferencesConstant.PREF_DROPBOX_LINK).setEnabled(!mDropboxHelper.isLinked());
		}
		if (findPreference(PreferencesConstant.PREF_DROPBOX_UNLINK) != null) {
			findPreference(PreferencesConstant.PREF_DROPBOX_UNLINK).setSelectable(mDropboxHelper.isLinked());
			findPreference(PreferencesConstant.PREF_DROPBOX_UNLINK).setEnabled(mDropboxHelper.isLinked());
		}
	}
	
	
	
	private void startActivityPasscode(CharSequence message, int request) {
		Intent intent = new Intent(this, PasscodeActivity.class);
		// set action and data
		intent.setAction(PasscodeActivity.INTENT_REQUEST_PASSWORD);
		intent.putExtra(PasscodeActivity.INTENT_MESSAGE_TEXT, message);
		// start activity
		startActivityForResult(intent, request);
	}
	
	/*
	 * This method is implemented was workaround of SherlockPreference
	 */
	private void showWebTipsDialog(final String key, final CharSequence title, final int rawResources, boolean force) {
		if (!force) {
			if (getSharedPreferences(TipsDialogFragment.PREF_DIALOG, 0).getBoolean(key, false)) return;
		}
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
		// title and icons
		alertDialog.setTitle(title);
		// view body
		final View view = (LinearLayout)LayoutInflater.from(this).inflate(R.layout.dialog_tips, null);
		// set invisible tips
		final TextView textTips = (TextView)view.findViewById(R.id.textViewTips);
		textTips.setVisibility(View.GONE);
		// set webView
		final WebView webTips = (WebView)view.findViewById(R.id.webViewTips);
		webTips.loadData(MoneyManagerApplication.getRawAsString(getApplicationContext(), rawResources), "text/html", "UTF-8");
		webTips.setVisibility(View.VISIBLE);
		
		final CheckBox checkDont = (CheckBox)view.findViewById(R.id.checkBoxDontShow);
		checkDont.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				getSharedPreferences(TipsDialogFragment.PREF_DIALOG, 0).edit().putBoolean(key, isChecked).commit();
			}
		});
		// bug CheckBox object of Android
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
			final float scale = this.getResources().getDisplayMetrics().density;
			checkDont.setPadding(checkDont.getPaddingLeft() + (int) (40.0f * scale + 0.5f),
								 checkDont.getPaddingTop(),
								 checkDont.getPaddingRight(),
								 checkDont.getPaddingBottom());
		}
		alertDialog.setView(view);
		// set neutral button
		alertDialog.setNeutralButton(android.R.string.ok, new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		// set auto close to false
		alertDialog.setCancelable(false);
		// show dialog
		alertDialog.create().show();
	}
	
	public void setSummaryListPreference(Preference preference, String value, int idArrayValues, int idArrayItems) {
		final String[] values = getResources().getStringArray(idArrayValues);
		final String[] items = getResources().getStringArray(idArrayItems);
		for(int i = 0; i < values.length; i ++) {
			if (value.equals(values[i])) {
				preference.setSummary(items[i]);
			}
		}		
	}
}