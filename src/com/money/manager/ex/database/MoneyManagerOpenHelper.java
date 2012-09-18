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
package com.money.manager.ex.database;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.money.manager.ex.R;
import com.money.manager.ex.MoneyManagerApplication;
/**
 * 
 * @author Alessandro Lazzari (lazzari.ale@gmail.com)
 * @version 1.0.1
 * 
 */
public class MoneyManagerOpenHelper extends SQLiteOpenHelper {
	private static final String LOGCAT = MoneyManagerOpenHelper.class.getSimpleName();
	// database name, database version
	private static final String databaseName = "data.mmb";
	private static final String databaseVersions[] = new String[] {"0000", "0990"};
	private static final int databaseCurrentVersion = 1;
	// context of creation
	private Context mContext;
	// path database
	public static final String databasePath = "/data/data/com.money.manager.ex/databases/"  + databaseName;
		
	public MoneyManagerOpenHelper(Context context) {
		super(context, MoneyManagerApplication.getDatabasePath(context), null, databaseCurrentVersion);
		this.mContext = context;
		// verbose open file
		Log.v(LOGCAT, "Database path:" + MoneyManagerApplication.getDatabasePath(context));
	}
	/**
	 * 
	 * @param db SQLite database to execute raw SQL
	 * @param rawId id raw resource
	 */
	private void executeRawSql(SQLiteDatabase db, int rawId) {
		String sqlCreate = new MoneyManagerApplication().getRawAsString(mContext, rawId);
		String sqlStatment[] = sqlCreate.split(";");
		// process all statment
		for(int i = 0; i < sqlStatment.length; i ++) {
			Log.v(LOGCAT, sqlStatment[i]);

			try {
				db.execSQL(sqlStatment[i]);
			} catch (SQLException E) {
				Log.e(LOGCAT, E.getMessage());
			}
		}
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.i(LOGCAT, "execute onCreate method");
		executeRawSql(db, R.raw.database_create);
		// force update database
		updateDatabase(db, 0, databaseVersions.length - 1);
	}

	@Override
	public void onOpen(SQLiteDatabase db) {
		super.onOpen(db);
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.i(LOGCAT, "execute onUpgrade(" + Integer.toString(oldVersion) + ", " + Integer.toString(newVersion) + " method");
		// update databases
		updateDatabase(db, oldVersion, newVersion);
	}

	private void updateDatabase(SQLiteDatabase db, int oldVersion, int newVersion) {
		for(int i = oldVersion + 1; i <= newVersion; i++) {
			if (databaseVersions[i].equals("0000")) {
				// nothing
			} else if (databaseVersions[i].equals("0990")) {
				// update version to 0.9.9.0 di money manager
				executeRawSql(db, R.raw.database_0990);
			}
		}
	}
	/**
	 * Execute a single SQL statement that is NOT a SELECT or any other SQL statement that returns data.
	 * It has no means to return any data (such as the number of affected rows). Instead, you're encouraged to use insert(String, String, ContentValues), update(String, ContentValues, String, String[]), et al, when possible.
	 * When using enableWriteAheadLogging(), journal_mode is automatically managed by this class. So, do not set journal_mode using "PRAGMA journal_mode'" statement if your app is using enableWriteAheadLogging()
	 * 
	 * @param sql the SQL statement to be executed. Multiple statements separated by semicolons are not supported.
	 * @since versionCode = 12 Version = 0.5.2
	 * 	 
	 */
	public void execSQL(String sql) throws SQLException {
		execSQL(this.getWritableDatabase(), sql);
	}
	/**
	 * Execute a single SQL statement that is NOT a SELECT or any other SQL statement that returns data.
	 * It has no means to return any data (such as the number of affected rows). Instead, you're encouraged to use insert(String, String, ContentValues), update(String, ContentValues, String, String[]), et al, when possible.
	 * When using enableWriteAheadLogging(), journal_mode is automatically managed by this class. So, do not set journal_mode using "PRAGMA journal_mode'" statement if your app is using enableWriteAheadLogging()
	 * 
	 * @param sql the SQL statement to be executed. Multiple statements separated by semicolons are not supported.
	 * @param bingArgs only byte[], String, Long and Double are supported in bindArgs.
	 * @since versionCode = 12 Version = 0.5.2
	 * 	 
	 */
	public void execSQL(String sql, Object[] bindArgs) throws SQLException {
		execSQL(this.getWritableDatabase(), sql, bindArgs);
	}
	/**
	 * Execute a single SQL statement that is NOT a SELECT or any other SQL statement that returns data.
	 * It has no means to return any data (such as the number of affected rows). Instead, you're encouraged to use insert(String, String, ContentValues), update(String, ContentValues, String, String[]), et al, when possible.
	 * When using enableWriteAheadLogging(), journal_mode is automatically managed by this class. So, do not set journal_mode using "PRAGMA journal_mode'" statement if your app is using enableWriteAheadLogging()
	 * 
	 * @param db the database
	 * @param sql the SQL statement to be executed. Multiple statements separated by semicolons are not supported.
	 * @since versionCode = 12 Version = 0.5.2
	 * 	 
	 */
	public void execSQL(SQLiteDatabase db, String sql) throws SQLException{
		db.execSQL(sql);
	}
	/**
	 * Execute a single SQL statement that is NOT a SELECT or any other SQL statement that returns data.
	 * It has no means to return any data (such as the number of affected rows). Instead, you're encouraged to use insert(String, String, ContentValues), update(String, ContentValues, String, String[]), et al, when possible.
	 * When using enableWriteAheadLogging(), journal_mode is automatically managed by this class. So, do not set journal_mode using "PRAGMA journal_mode'" statement if your app is using enableWriteAheadLogging()
	 * 
	 * @param db the database
	 * @param sql the SQL statement to be executed. Multiple statements separated by semicolons are not supported.
	 * @param bingArgs only byte[], String, Long and Double are supported in bindArgs.
	 * @since versionCode = 12 Version = 0.5.2
	 * 	 
	 */	
	public void execSQL(SQLiteDatabase db, String sql, Object[] bindArgs) throws SQLException {
		db.execSQL(sql, bindArgs);
	}
	/**
	 * 
	 * @return List all accounts
	 */
	public List<TableAccountList> getListAccounts() {
		return getListAccounts(false, false);
	}
	/**
	 * 
	 * @param open show open accounts
	 * @param favorite show favorite account
	 * @return List<TableAccountList> list of accounts selected
	 * 
	 */
	public List<TableAccountList> getListAccounts(boolean open, boolean favorite) {
		// create a return list
		List<TableAccountList> listAccount = new ArrayList<TableAccountList>();
		// compose where clause
		String where = "";
		
		if (open) { where = "LOWER(STATUS)='open'"; }
		if (favorite) { where = "LOWER(FAVORITEACCT)='true'"; }
		// data cursor
		Cursor cursor = mContext.getContentResolver().query(
				new TableAccountList().getUri(), null, where, null,
				"upper(" + TableAccountList.ACCOUNTNAME + ")");
		// populate list from data cursor
		if (cursor != null && cursor.moveToFirst()) {
			while (!(cursor.isAfterLast())) {
				TableAccountList account = new TableAccountList();
				account.setValueFromCursor(cursor);
				listAccount.add(account);
				cursor.moveToNext();
			}
			cursor.close();
		}
		return listAccount;
	}
	/**
	 * 
	 * @param id account id to be search
	 * @return TableAccountList, return null if account id not find
	 */
	public TableAccountList getTableAccountList(int id) {
		String selection = TableAccountList.ACCOUNTID + "=?";
		Cursor cursor = mContext.getContentResolver().query(new TableAccountList().getUri(), null, selection, new String[] {Integer.toString(id)}, null);
		// check if cursor is valid
		if (cursor != null && cursor.moveToFirst()) {
			TableAccountList account = new TableAccountList();
			account.setValueFromCursor(cursor);
			return account;
		}
		// find is false then return null
		return null;
	}
	/**
	 * Return a list of all categories
	 * @return List of all categories
	 * @since version 1.0.1
	 */
	public List<TableCategory> getListCategories() {
		// create a return list
		List<TableCategory> listCategories = new ArrayList<TableCategory>();
		// data cursor
		Cursor cursor = mContext.getContentResolver().query(new TableCategory().getUri(), null, null, null, TableCategory.CATEGNAME);
		// populate list from data cursor
		if ((cursor != null) && (cursor.moveToFirst())) {
			while (!(cursor.isAfterLast())) {
				TableCategory category = new TableCategory();
				category.setValueFromCursor(cursor);
				listCategories.add(category);
				cursor.moveToNext();
			}
			cursor.close();
		}
		return listCategories;
	}
}
