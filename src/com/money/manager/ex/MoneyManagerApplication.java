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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.app.Application;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.os.Build;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.dropbox.client2.session.Session.AccessType;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.database.MoneyManagerOpenHelper;
import com.money.manager.ex.database.QueryAccountBills;
import com.money.manager.ex.database.TableCurrencyFormats;
import com.money.manager.ex.database.TableInfoTable;
import com.money.manager.ex.preferences.PreferencesConstant;
import com.money.manager.ex.view.RobotoView;
import com.money.manager.ex.widget.AccountBillsWidgetProvider;
import com.money.manager.ex.widget.SummaryWidgetProvider;

/**
 * This class extends Application and implements all the methods common in the
 * former money manager application for Android
 * 
 * @author Alessandro Lazzari (lazzari.ale@gmail.com)
 * @version 1.1.0
 * 
 */
public class MoneyManagerApplication extends Application {
	private static final String LOGCAT = "MoneyManagerApplication";
	///////////////////////////////////////////////////////////////////////////
	public static final String KEY = "8941ED03A52BF76CD48EF951CA623B0709564CA238DB7FE1BA3980E4F617CD52";
    ///////////////////////////////////////////////////////////////////////////
    //                      drop box app settings                            //
    ///////////////////////////////////////////////////////////////////////////
    public static final String DROPBOX_APP_KEY = "cakbv9zh9l083ep";
    public static final String DROPBOX_APP_SECRET = "5E01FC70A055AE4A6C9DB346343CE822";
    public static final AccessType DROPBOX_ACCESS_TYPE = AccessType.APP_FOLDER;
    ///////////////////////////////////////////////////////////////////////////
    //                         CONSTANTS VALUES                              //
    ///////////////////////////////////////////////////////////////////////////
    public static final int TYPE_HOME_CLASSIC = R.layout.main_fragments_activity;
    public static final int TYPE_HOME_ADVANCE = R.layout.main_pager_activity;
	///////////////////////////////////////////////////////////////////////////
	//                         CONSTANTS VALUES                              //
	///////////////////////////////////////////////////////////////////////////
    public static String PATTERN_DB_DATE = "yyyy-MM-dd";
    
	private static SharedPreferences appPreferences;
	private static float mTextSize;
	
	/**
	 * Take a versioncode of this application
	 * @param context
	 * @return application version name
	 */
    public static int getCurrentVersionCode(Context context) {
		try {
			PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionCode;
		} catch (NameNotFoundException e) {
			return 0;
		}
		
    }
	/**
	 * Take a versioncode of this application
	 * @param context
	 * @return application version name
	 */
    public static String getCurrentVersionName(Context context) {
		try {
			PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionName;
		} catch (NameNotFoundException e) {
			return "";
		}
    }
    
    /***
	 * 
	 * @param context
	 * @return path database file
	 */
	public static String getDatabasePath(Context context) {
		String defaultPath = "/data/data/" + context.getApplicationContext().getPackageName() + "/databases/data.mmb";
		String dbFile = PreferenceManager.getDefaultSharedPreferences(context).getString(PreferencesConstant.PREF_DATABASE_PATH, defaultPath);
		File f = new File(dbFile);
		// check if database exists
		if (f.getAbsoluteFile().exists()) {
			return dbFile;
		} else {
			return defaultPath;
		}
	}
	
    /**
	 * 
	 * @param Context context from call
	 * int resId: rawid
	 * @return String: String file
	 */
	public static String getRawAsString(Context context, int resId) {
		final int BUFFER_DIMENSION = 128;
		String result = null;
		// take input stream
		InputStream is = context.getResources().openRawResource(resId);
		if (is != null) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] buffer = new byte[BUFFER_DIMENSION];
			int numRead = 0;
			try {
				while ((numRead = is.read(buffer)) >= 0) {
					baos.write(buffer, 0, numRead);
				}
				// convert to string
				result = new String(baos.toByteArray());
			} catch (IOException e) {
				Log.e(LOGCAT, e.getMessage());
				e.printStackTrace();
			} finally {
				if (baos != null) {
					try {
						baos.close();
					} catch (IOException e) {
						Log.e(LOGCAT, e.getMessage());
					}
				}
			}
		}
		return result;
	}
	
	/**
	 * Reset to force show donate dialog
	 * @param context
	 */
	public static void resetDonateDialog(final Context context) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		preferences.edit().putInt(PreferencesConstant.PREF_DONATE_LAST_VERSION_KEY, -1).commit();
	}
    /**
	 * 
	 * @param context
	 * @param dbpath path of database file to save
	 */
	public static void setDatabasePath(Context context, String dbpath) {
		// save a reference dbpath
		Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
		editor.putString(PreferencesConstant.PREF_DATABASE_PATH, dbpath);
		editor.commit();
	}

	/**
     * Show changelog dialog
     * @param context
     * @param forceShow force show changelog alert dialog
     * @return
     */
	public static boolean showChangeLog(Context context, boolean forceShow) {
		return showChangeLog(context, forceShow, true);
	}
	
	/**
     * Show changelog dialog
     * @param context
     * @param forceShow force show changelog alert dialog
     * @param complete to show all changelog
     * @return
     */
	public static boolean showChangeLog(Context context, boolean forceShow, boolean complete) {
		int currentVersionCode = getCurrentVersionCode(context);
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		int lastVersionCode = preferences.getInt(PreferencesConstant.PREF_LAST_VERSION_KEY, -1);
		if (!(lastVersionCode == currentVersionCode) || forceShow) {
			preferences.edit().putInt(PreferencesConstant.PREF_LAST_VERSION_KEY, currentVersionCode).commit();
			//get text changelog
			String changelog = getRawAsString(context, R.raw.changelog);
			//check complete changelog
			if (!complete) {
				final String ESCAPE = "<b> Version";
				int end = changelog.indexOf(ESCAPE, changelog.indexOf(ESCAPE) + ESCAPE.length());
				changelog = changelog.substring(0, end);
			}
			changelog = "<small>" + changelog.replace("\n", "<br>") + "</small>";
			while (changelog.indexOf("<br></small>") >= 0) {
				changelog = changelog.replace("<br></small>", "</small>");
			}
				
			//create dialog
			AlertDialog.Builder showDialog = new AlertDialog.Builder(context);
			showDialog.setCancelable(false);
			showDialog.setTitle(R.string.changelog);
			showDialog.setMessage(Html.fromHtml(changelog));
			showDialog.setNeutralButton(android.R.string.ok,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
			// show dialog
			showDialog.create().show();
			return true;
		} else
			return false;
	}
	
	/**
	 * Show donate dialog
	 * @param context
	 * @param forceShow
	 * @return
	 */
	public static boolean showDonateDialog(final Context context, boolean forceShow) {
		int currentVersionCode = getCurrentVersionCode(context);
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		int lastVersionCode = preferences.getInt(PreferencesConstant.PREF_DONATE_LAST_VERSION_KEY, -1);
		if (!(lastVersionCode == currentVersionCode) || forceShow) {
			preferences.edit().putInt(PreferencesConstant.PREF_DONATE_LAST_VERSION_KEY, currentVersionCode).commit();
			Core core = new Core(context);
			if (TextUtils.isEmpty(core.getInfoValue(Core.INFO_SKU_ORDER_ID))) {
				//get text donate
				String donateText = context.getString(R.string.donate_header);
				//create dialog
				AlertDialog.Builder showDialog = new AlertDialog.Builder(context);
				showDialog.setCancelable(false);
				showDialog.setTitle(R.string.donate);
				showDialog.setIcon(R.drawable.ic_launcher);
				showDialog.setMessage(Html.fromHtml(donateText));
				showDialog.setNegativeButton(R.string.no_thanks, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				showDialog.setPositiveButton(R.string.donate_exlamation, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						context.startActivity(new Intent(context, DonateActivity.class));
						dialog.dismiss();
					}
				});
				// show dialog
				showDialog.create().show();
			}
			return true;
		} else
			return false;
	}
	
	/**
     * This method show introduction activity
     * @param context activity called
     * @param force true show
     */
    public static boolean showIntroduction(Context context, boolean forceShow) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		Boolean isShowed = preferences.getBoolean(PreferencesConstant.PREF_SHOW_INTRODUCTION, false);
		if ((!isShowed) || forceShow) {
			preferences.edit().putBoolean(PreferencesConstant.PREF_SHOW_INTRODUCTION, true).commit();
			context.startActivity(new Intent(context, IntroductionActivity.class));
			return true;
		} else
			return false;
    }
    
    public static void setTextSize(float textSize) {
    	MoneyManagerApplication.mTextSize = textSize;
    }
    
    public static float getTextSize() {
    	return MoneyManagerApplication.mTextSize;
    }
	
	///////////////////////////////////////////////////////////////////////////
    //                           PREFERENCES                                 //
    ///////////////////////////////////////////////////////////////////////////
    private Editor editPreferences;
    // definition of the hashmap for the management of the currency
	private static Map<Integer, TableCurrencyFormats> mMapCurrency = new HashMap<Integer, TableCurrencyFormats>();
	// Id of BaseCurrency
	private static int mBaseCurrencyId = 0;
	// user name application
	private static String userName = "";
	
	
	/**
	 * close process application
	 */
	public static void killApplication() {
		// close application
		android.os.Process.killProcess(android.os.Process.myPid());
	}
	/**
	 * Shown database path with toast message
	 * @param context
	 */
	public static void showDatabasePathWork(Context context) {
		String currentPath = getDatabasePath(context);
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		String lastPath = preferences.getString(PreferencesConstant.PREF_LAST_DB_PATH_SHOWN, "");
		if (!lastPath.equals(currentPath)) {
			Toast.makeText(context, Html.fromHtml(context.getString(R.string.path_database_using, "<b>" + currentPath + "</b>")), Toast.LENGTH_LONG).show();
			preferences.edit().putString(PreferencesConstant.PREF_LAST_DB_PATH_SHOWN, currentPath).commit();
		}
	}
	/**
	 * 
	 * @return preferences account fav visible
	 */
	public boolean getAccountFavoriteVisible() {
		return appPreferences.getBoolean(PreferencesConstant.PREF_ACCOUNT_FAV_VISIBLE, false);
	}
	/**
	 * 
	 * @return preferences accounts visible
	 */
	public boolean getAccountsOpenVisible() {
		return appPreferences.getBoolean(PreferencesConstant.PREF_ACCOUNT_OPEN_VISIBLE, false);
	}

	/**
	 * 
	 * @return List di tutte le CurrencyFormats
	 */
	public List<TableCurrencyFormats> getAllCurrencyFormats() {
		List<TableCurrencyFormats> ret = new ArrayList<TableCurrencyFormats>(mMapCurrency.values());
		return ret;
	}
	public String getBaseCurrencyFormatted(float value) {
		return this.getCurrencyFormatted(mBaseCurrencyId, value);
	}
	/**
	 * 
	 * @return the base currency used from application
	 */
	public int getBaseCurrencyId() {
		return MoneyManagerApplication.mBaseCurrencyId;
	}

	/**
	 * this method convert a float value to base numeric string
	 * @param value to format
	 * @return value formatted
	 */
	public String getBaseNumericFormatted(float value) {
		return getNumericFormatted(mBaseCurrencyId, value);
	}

	/**
	 * @param currency
	 * @return CurrencyFormats
	 */
	public TableCurrencyFormats getCurrencyFormats(int currency) {
		return mMapCurrency.get(currency);
	}
	/**
	 * 
	 * @param currency id della valuta
	 * @param value valore da formattare
	 * @return valore formattato
	 */
	public String getCurrencyFormatted(int currency, float value) {
		// find currencyid
		TableCurrencyFormats tableCurrency = mMapCurrency.get(currency);

		if (tableCurrency == null) {
			return Float.toString(value);
		}
		// formatted value
		return tableCurrency.getValueFormatted(value);
	}
	/**
	 * Convert string date into date object using pattern define to user
	 * @param date string to convert
	 * @return date converted
	 */
	public Date getDateFromString(String date) {
		return getDateFromString(date, getUserDatePattern());
	}
	/**
	 * Convert string date into date object using pattern params
	 * @param date string to convert
	 * @param pattern to use for convert
	 * @return date object converted
	 */
	public Date getDateFromString(String date, String pattern) {
		try {
			return new SimpleDateFormat(pattern).parse(date);
		} catch (ParseException e) {
			Log.e(LOGCAT, e.getMessage());
		}
		return null;		
	}
	public Date getDateNextOccurence(Date date, int repeats) {
		if (repeats >= 200) { repeats = repeats - 200; } // set auto execute without user acknowlegement
		if (repeats >= 100) { repeats = repeats - 100; } // set auto execute on the next occurrence
		// create object calendar
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		switch (repeats) {
        case 0: //none
        	break;
        case 1: //weekly
        	calendar.add(Calendar.DATE, 7);
        	break;
        case 2: //bi_weekly
        	calendar.add(Calendar.DATE, 14);
        	break;
        case 3: //monthly
        	calendar.add(Calendar.MONTH, 1);
        	break;
        case 4: //bi_monthly
        	calendar.add(Calendar.MONTH, 2);
        	break;
        case 5: //quaterly
        	calendar.add(Calendar.MONTH, 3);
        	break;
        case 6: //half_year
        	calendar.add(Calendar.MONTH, 6);
        	break;
        case 7: //yearly
        	calendar.add(Calendar.YEAR, 1);
        	break;
        case 8: //four_months
        	calendar.add(Calendar.MONTH, 4);
        	break;
        case 9: //four_weeks
        	calendar.add(Calendar.DATE, 28);
        	break;
        case 10: //daily
        	calendar.add(Calendar.DATE, 1);
        	break;
        case 11: //in_x_days
        case 12: //in_x_months
        case 13: //every_x_days
        case 14: //every_x_months
		}
		return calendar.getTime();
	}
	/**
	 * 
	 * @return default home type
	 */
	public int getDefaultTypeHome() {
		return Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB ? TYPE_HOME_CLASSIC : TYPE_HOME_ADVANCE;
	}
	/**
	 * 
	 * @return the dropbox mode synchronization
	 */
	public String getDropboxSyncMode() {
		return appPreferences.getString(PreferencesConstant.PREF_DROPBOX_MODE, getString(R.string.synchronize));
	}
	/**
	 * 
	 * @param context
	 * @return the username
	 */
	public String getFromDatabaseUserName(Context context) {
		TableInfoTable infoTable = new TableInfoTable();
		MoneyManagerOpenHelper helper = new MoneyManagerOpenHelper(context);
		Cursor data = helper.getReadableDatabase().query(infoTable.getSource(), null, TableInfoTable.INFONAME + "=?", new String[] {"USERNAME"}, null, null, null);
		String ret = "";
		if (data != null && data.moveToFirst()) {
			ret = data.getString(data.getColumnIndex(TableInfoTable.INFOVALUE));
		}
		data.close();
		helper.close();
		
		return ret;
	}
	/**
	 * this method convert a float value to numeric string
	 * @param currency id of currency to format
	 * @param value value to format
	 * @return value formatted
	 */
	public String getNumericFormatted(int currency, float value) {
		// find currency
		TableCurrencyFormats tableCurrency = mMapCurrency.get(currency);
		
		if (tableCurrency == null) {
			return Float.toString(value);
		}
		// formatted value
		return tableCurrency.getValueFormatted(value, false);		
	}
	/**
	 * 
	 * @param repeat frequency repeats
	 * @return frequency
	 */
	public String getRepeatAsString(int repeat) {
		if (repeat >= 200) { repeat = repeat - 200; } // set auto execute without user acknowlegement
		if (repeat >= 100) { repeat = repeat - 100; } // set auto execute on the next occurrence
		String[] arrays = getResources().getStringArray(R.array.frequencies_items);
		if (arrays != null && repeat >= 0 && repeat <= arrays.length) {
			return arrays[repeat];
		}
		return "";
	}
	/**
	 * 
	 * @return the show transaction
	 */
	public String getShowTransaction() {
		return PreferenceManager.getDefaultSharedPreferences(this).getString(PreferencesConstant.PREF_SHOW_TRANSACTION, getResources().getString(R.string.last7days));
	}
	/**
	 * Convert date object in string SQLite date format
	 * @param date to convert
	 * @return string formatted date SQLite
	 */
	public String getSQLiteStringDate(Date date) {
		return getStringFromDate(date, PATTERN_DB_DATE);
	}
	/**
	 * 
	 * @param status char of status
	 * @return decode status char
	 */
	public String getStatusAsString(String status) {
		if (TextUtils.isEmpty(status)) {
			return this.getResources().getString(R.string.status_none);
		} else if (status.toUpperCase().equals("R")) {
			return this.getResources().getString(R.string.status_reconciled);
		} else if (status.toUpperCase().equals("V")) {
			return this.getResources().getString(R.string.status_void);
		} else if (status.toUpperCase().equals("F")) {
			return this.getResources().getString(R.string.status_follow_up);
		} else if (status.toUpperCase().equals("D")) {
			return this.getResources().getString(R.string.status_duplicate);
		}
		return "";
	}
	
	/**
	 * Convert date object to string from user pattern
	 * @param date
	 * @return
	 */
	public String getStringFromDate(Date date) {
		return getStringFromDate(date, getUserDatePattern());
	}
	/**
	 * 
	 * @param date object to convert in string
	 * @param pattern pattern to use to convert
	 * @return
	 */
	public String getStringFromDate(Date date, String pattern) {
		return new SimpleDateFormat(pattern).format(date);
	}
	/**
	 * Compute account balance and returns balance
	 * @param context
	 * @return
	 */
	public float getSummaryAccounts(Context context) {
		// compose whereClause
		String where = "";
		// check if show only open accounts
		if (this.getAccountsOpenVisible()) {
			where = "LOWER(STATUS)='open'";
		}
		// check if show fav accounts
		if (this.getAccountFavoriteVisible()) {
			where = "LOWER(FAVORITEACCT)='true'";
		}
		QueryAccountBills accountBills = new QueryAccountBills(context);
		Cursor data = context.getContentResolver().query(accountBills.getUri(), null, where, null, null);
		float curTotal = 0;
		
		if (data != null && data.moveToFirst()) {
			// calculate summary
			while (data.isAfterLast() == false) {
				curTotal = curTotal + data.getFloat(data.getColumnIndex(QueryAccountBills.TOTALBASECONVRATE));
				data.moveToNext();
			}
		}
		data.close();
		
		return curTotal;
	}
	/**
	 * Get pattern define from user
	 * @return pattern user define
	 */
	public String getUserDatePattern() {
		TableInfoTable infoTable = new TableInfoTable();
		MoneyManagerOpenHelper helper = new MoneyManagerOpenHelper(getApplicationContext());
		Cursor cursor = helper.getReadableDatabase().query(infoTable.getSource(), null, TableInfoTable.INFONAME + "=?", new String[] {"DATEFORMAT"}, null, null, null);
		String pattern = null;
		if (cursor != null && cursor.moveToFirst()) {
			pattern = cursor.getString(cursor.getColumnIndex(TableInfoTable.INFOVALUE));
			//replace part of pattern
			pattern = pattern.replace("%d", "dd").replace("%m", "MM").replace("%y", "yy").replace("%Y", "yyyy").replace("'", "''");
		}
		//close cursor and helper
		cursor.close();
		helper.close();
		
		return pattern;
	}
	/**
	 * @return the userName
	 */
	public String getUserName() {
		return userName;
	}
	public boolean isUriAvailable(Context context, Intent intent) {
		return context.getPackageManager().resolveActivity(intent, 0) != null;
	}
	/**
	 * method that loads the base currency
	 * @param context contesto della chiamata
	 */
	public void loadBaseCurrencyId(Context context) {
		MoneyManagerOpenHelper openHelper = new MoneyManagerOpenHelper(context);
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		TableInfoTable tableInfo = new TableInfoTable();
		
		// set table
		queryBuilder.setTables(tableInfo.getSource());
		
		// get cursor from query builder
		Cursor cursorInfo = queryBuilder.query(
				openHelper.getReadableDatabase(),
				tableInfo.getAllColumns(), TableInfoTable.INFONAME + "='BASECURRENCYID'", null, null, null, null);
		
		// set BaseCurrencyId
		if (cursorInfo != null && cursorInfo.moveToFirst()) {
			mBaseCurrencyId = cursorInfo.getInt(cursorInfo.getColumnIndex(TableInfoTable.INFOVALUE)); 
			cursorInfo.close();
		} else {
			mBaseCurrencyId = 0;
		}
		// chiudo la connessione
		openHelper.close();
	}
	/**
	 * populate the hashmap Currency
	 */
	public void loadHashMapCurrency(Context context) {
		MoneyManagerOpenHelper openHelper = new MoneyManagerOpenHelper(context);
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		TableCurrencyFormats tableCurrency = new TableCurrencyFormats();
		
		// set table name
		queryBuilder.setTables(tableCurrency.getSource());
		
		// get cursor
		Cursor cursorCurrency = queryBuilder.query(openHelper.getReadableDatabase(), tableCurrency.getAllColumns(), null, null, null, null, null);
		
		// clear hashmap for new populate
		mMapCurrency.clear();
		
		// load data into hashmap
		if (cursorCurrency != null && cursorCurrency.moveToFirst()) {
			while (cursorCurrency.isAfterLast() == false) {
				TableCurrencyFormats mapCur = new TableCurrencyFormats();
				mapCur.setValueFromCursor(cursorCurrency);

				int currencyId = cursorCurrency.getInt(cursorCurrency.getColumnIndex(TableCurrencyFormats.CURRENCYID));
				// put object into hashmap
				mMapCurrency.put(currencyId, mapCur);

				cursorCurrency.moveToNext();
			}
			cursorCurrency.close();
		}
		openHelper.close();
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		if (BuildConfig.DEBUG) Log.d(LOGCAT, "Application created");
		// set default value
		setTextSize(new TextView(getApplicationContext()).getTextSize());
		// preference
		if (appPreferences == null) { 
			appPreferences = PreferenceManager.getDefaultSharedPreferences(this);
			RobotoView.setUserFont(Integer.parseInt(appPreferences.getString(PreferencesConstant.PREF_APPLICATION_FONT, "12")));
			RobotoView.setUserFontSize(getApplicationContext(), appPreferences.getString(PreferencesConstant.PREF_APPLICATION_FONT_SIZE, ""));
		}
	}
	
	@Override
	public void onTerminate() {
		if (BuildConfig.DEBUG) Log.d(LOGCAT, "Application terminated");
	}

	/**
	 * 
	 * @param theme to save into preferences
	 */
	public void setApplicationTheme(String theme) {
		Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
		editor.putString(PreferencesConstant.PREF_THEME, theme);
		editor.commit();
	}
	/**
	 * set value of base currency
	 * @param value
	 * @return
	 */
	public boolean setBaseCurrencyId(int value) {
		return this.setBaseCurrencyId(value, false);
	}
	/**
	 * set value of base currency
	 * @param value
	 * @param save save value into database
	 */
	public boolean setBaseCurrencyId(int value, boolean save) {
		if (save) {
			TableInfoTable infoTable = new TableInfoTable();
			// update data into database
			ContentValues values = new ContentValues();
			values.put(TableInfoTable.INFOVALUE, value);

			if (getContentResolver().update(infoTable.getUri(), values, TableInfoTable.INFONAME + "='BASECURRENCYID'", null) != 1) {
				return false;
			}
		}
		// edit preferences
		editPreferences = appPreferences.edit();
		editPreferences.putString(PreferencesConstant.PREF_BASE_CURRENCY, ((Integer)value).toString());
		// commit
		editPreferences.commit();
		// imposto il valore
		MoneyManagerApplication.mBaseCurrencyId = value;
		
		return true;
	}
	
	/**
	 * 
	 * @param value mode of syncronization to apply
	 */
	public void setDropboxSyncMode(String value) {
		// open edit preferences
		editPreferences = appPreferences.edit();
		editPreferences.putString(PreferencesConstant.PREF_DROPBOX_MODE, value);
		// commit
		editPreferences.commit();
	}
	
	public boolean setUserName(String userName) {
		return this.setUserName(userName, false);
	}
	
	/**
	 * @param userName the userName to set
	 * @param save save into database
	 */
	public boolean setUserName(String userName, boolean save) {
		if (save) {
			TableInfoTable infoTable = new TableInfoTable();
			// update data into database
			ContentValues values = new ContentValues();
			values.put(TableInfoTable.INFOVALUE, userName);

			if (getContentResolver().update(infoTable.getUri(), values, TableInfoTable.INFONAME + "='USERNAME'", null) != 1) {
				return false;
			}
		}
		// edit preferences
		editPreferences = appPreferences.edit();
		editPreferences.putString(PreferencesConstant.PREF_USER_NAME, userName);
		// commit
		editPreferences.commit();
		// set the value 
		MoneyManagerApplication.userName = userName;
		return true;
	}
	
	/**
	 * update all widget of application
	 */
	public void updateAllWidget() {
		Class<?>[] classes = {AccountBillsWidgetProvider.class, SummaryWidgetProvider.class };
		for (Class<?> cls : classes) {
			try {
				AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
				ComponentName componentName = new ComponentName(getApplicationContext(), cls);
				int[] ids = appWidgetManager.getAppWidgetIds(componentName);
				Intent update_widget = new Intent();
				update_widget.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
				update_widget.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
				getApplicationContext().sendBroadcast(update_widget);
			} catch (Exception e) {
				Log.e(LOGCAT, "update All Widget:" + e.getMessage());
			}
		}
	}
}
