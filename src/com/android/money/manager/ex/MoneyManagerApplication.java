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
package com.android.money.manager.ex;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Application;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.android.money.manager.ex.database.MoneyManagerOpenHelper;
import com.android.money.manager.ex.database.TableCurrencyFormats;
import com.android.money.manager.ex.database.TableInfoTable;
import com.dropbox.client2.session.Session.AccessType;

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
    //                           PREFERENCES                                 //
    ///////////////////////////////////////////////////////////////////////////
    public static final String PREF_DATABASE_PATH = "databasepath";
    public static final String PREF_USER_NAME = "username";
    public static final String PREF_BASE_CURRENCY = "basecurrency";
    public static final String PREF_ACCOUNT_OPEN_VISIBLE = "accountsopenvisible";
    public static final String PREF_ACCOUNT_FAV_VISIBLE = "accountsfavoritevisible";
    public static final String PREF_DROPBOX_MODE = "dropboxmodesync";
    
	// application preferences
	private static final String APP_PREFERENCES = "MoneyManagerPreferences";
	private static SharedPreferences appPreferences;
	private Editor editPreferences;
    ///////////////////////////////////////////////////////////////////////////
    //                           PREFERENCES                                 //
    ///////////////////////////////////////////////////////////////////////////
    // definition of the hashmap for the management of the currency
	private static Map<Integer, TableCurrencyFormats> mMapCurrency = new HashMap<Integer, TableCurrencyFormats>();
	// Id of BaseCurrency
	private static int mBaseCurrencyId = 0;
	// user name application
	private static String userName = "";
	private static String dropboxSyncMode = "";
	// application context
	private static Context applicationContext;
	
	@Override
	public void onCreate() {
		Log.v(LOGCAT, "Application created");

		if (appPreferences == null) { 
			appPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		}
		applicationContext = this;
	}
	@Override
	public void onTerminate() {
		Log.v(LOGCAT, "Application terminated");
	}
	/**
	 * close process application
	 */
	public static void killApplication() {
		// close application
		android.os.Process.killProcess(android.os.Process.myPid());
	}
	/**
	 * 
	 * @return la currency di base utilizzata dalla app
	 */
	public int getBaseCurrencyId() {
		return MoneyManagerApplication.mBaseCurrencyId;
	}
	/**
	 * set il valore della valuta di base
	 * @param value
	 * @return
	 */
	public boolean setBaseCurrencyId(int value) {
		return this.setBaseCurrencyId(value, false);
	}
	/**
	 * set il valore della valuta di base
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
		editPreferences.putString(PREF_BASE_CURRENCY, ((Integer)value).toString());
		// commit
		editPreferences.commit();
		// imposto il valore
		MoneyManagerApplication.mBaseCurrencyId = value;
		
		return true;
	}

	/**
	 * @return the userName
	 */
	public String getUserName() {
		return userName;
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
			// passo all'aggiornamento
			if (getContentResolver().update(infoTable.getUri(), values, TableInfoTable.INFONAME + "='USERNAME'", null) != 1) {
				return false;
			}
		}
		// edit preferences
		editPreferences = appPreferences.edit();
		editPreferences.putString(PREF_USER_NAME, userName);
		// commit
		editPreferences.commit();
		// set the value 
		MoneyManagerApplication.userName = userName;
		return true;
	}
	/**
	 * 
	 * @return preferences accounts visible
	 */
	public boolean getAccountsOpenVisible() {
		return appPreferences.getBoolean(PREF_ACCOUNT_OPEN_VISIBLE, false);
	}
	/**
	 * 
	 * @return preferences account fav visible
	 */
	public boolean getAccountFavoriteVisible() {
		return appPreferences.getBoolean(PREF_ACCOUNT_FAV_VISIBLE, false);
	}
	/**
	 * 
	 * @param value mode of syncronization to apply
	 */
	public void setDropboxSyncMode(String value) {
		// open edit preferences
		editPreferences = appPreferences.edit();
		editPreferences.putString(PREF_DROPBOX_MODE, value);
		// commit
		editPreferences.commit();
		// set the value 
		MoneyManagerApplication.dropboxSyncMode = value;
	}
	/**
	 * 
	 * @return the dropbox mode synchronization
	 */
	public String getDropboxSyncMode() {
		return appPreferences.getString(PREF_DROPBOX_MODE, applicationContext.getString(R.string.synchronize));
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
		Cursor cursorCurrency = queryBuilder.query(
				openHelper.getReadableDatabase(),
				tableCurrency.getAllColumns(), null, null, null, null, null);
		
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

	public String getBaseCurrencyFormatted(float value) {
		return this.getCurrencyFormatted(mBaseCurrencyId, value);
	}
	/**
	 * 
	 * @param currency id della valuta
	 * @param value valore da formattare
	 * @return valore formattato
	 */
	public String getCurrencyFormatted(int currency, float value) {
		// ricerco il currency per id
		TableCurrencyFormats tableCurrency = mMapCurrency.get(currency);
		// controllo se l'ho trovato mediante il null
		if (tableCurrency == null) {
			return Float.toString(value);
		}
		// adesso richiamo la funzione che mi restituice il valore formattato
		return tableCurrency.getValueFormatted(value);
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
	 * this method convert a float value to numeric string
	 * @param currency id of currency to format
	 * @param value value to format
	 * @return value formatted
	 */
	public String getNumericFormatted(int currency, float value) {
		// ricerco il currency per id
		TableCurrencyFormats tableCurrency = mMapCurrency.get(currency);
		// controllo se l'ho trovato mediante il null
		if (tableCurrency == null) {
			return Float.toString(value);
		}
		// adesso richiamo la funzione che mi restituice il valore formattato
		return tableCurrency.getValueFormatted(value, false);		
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
	 * @return List di tutte le CurrencyFormats
	 */
	public List<TableCurrencyFormats> getAllCurrencyFormats() {
		List<TableCurrencyFormats> ret = new ArrayList<TableCurrencyFormats>(mMapCurrency.values());
		return ret;
	}
	/**
	 * 
	 * @param status stato della transazione
	 * @return stringa decodificata della transazione
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
			return this.getResources().getString(R.string.status_follow_up);
		}
		return "";
	}
	/***
	 * 
	 * @param context
	 * @return path database file
	 */
	public static String getDatabasePath(Context context) {
		String dbFile = PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_DATABASE_PATH, MoneyManagerOpenHelper.databasePath);
		File f = new File(dbFile);
		// check if database exists
		if (f.getAbsoluteFile().exists()) {
			return dbFile;
		} else {
			return MoneyManagerOpenHelper.databasePath;
		}
	}
	/**
	 * 
	 * @param context
	 * @param dbpath path of database file to save
	 */
	public static void setDatabasePath(Context context, String dbpath) {
		// save a reference dbpath
		Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
		editor.putString(PREF_DATABASE_PATH, dbpath);
		editor.commit();
	}
	/**
	 * 
	 * @param resId: ID della risorsa da ricercare nel raw
	 * @return
	 */
	public String getRawAsString(int resId) {
		return getRawAsString(getApplicationContext(), resId);
	}

	/**
	 * 
	 * @param Context context della chiamata 
	 * int resId: ID della risorsa da ricercare nel raw
	 * @return String: valore di ritorno del Raw come stringa
	 */
	public String getRawAsString(Context context, int resId) {
		final int BUFFER_DIMENSION = 128;
		// Risultato
		String result = null;
		// Otteniamo il riferimento all'inputStream
		InputStream is = context.getResources().openRawResource(resId);
		if (is != null) {
			// Leggiamo i byte
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] buffer = new byte[BUFFER_DIMENSION];
			int numRead = 0;
			try {
				while ((numRead = is.read(buffer)) >= 0) {
					baos.write(buffer, 0, numRead);
				}
				// Alla fine otteniamo la corrispondente String
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
		// Ritorniamo il risultato
		return result;
	}
}
