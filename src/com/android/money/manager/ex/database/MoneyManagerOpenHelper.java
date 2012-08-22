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
package com.android.money.manager.ex.database;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.android.money.manager.ex.MoneyManagerApplication;
import com.android.money.manager.ex.R;
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
	public static final String databasePath = "/data/data/com.android.money.manager.ex/databases/"  + databaseName;
		
	public MoneyManagerOpenHelper(Context context) {
		super(context, MoneyManagerApplication.getDatabasePath(context), null, databaseCurrentVersion);
		this.mContext = context;
		// verbose open file
		Log.v(LOGCAT, "Database path:" + MoneyManagerApplication.getDatabasePath(context));
	}
	
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
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.i(LOGCAT, "execute onUpgrade(" + Integer.toString(oldVersion) + ", " + Integer.toString(newVersion) + " method");
		// update databases
		updateDatabase(db, oldVersion, newVersion);
	}

	private void updateDatabase(SQLiteDatabase db, int oldVersion, int newVersion) {
		for(int i = oldVersion + 1; i <= newVersion; i++) {
			if (databaseVersions[i] == "0000") {
				// nothing
			} else if (databaseVersions[i] == "0990") {
				// update version to 0.9.9.0 di money manager
				executeRawSql(db, R.raw.database_0990);
			}
		}
	}
	
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
		Cursor cursor = mContext.getContentResolver().query(new TableAccountList().getUri(), null, where, null, "ACCOUNTNAME ASC");
		// populate list from data cursor
		if (cursor != null && cursor.moveToFirst()) {
			while (cursor.isAfterLast() == false) {
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
		List<TableAccountList> list = this.getListAccounts();
		// cycle elements and find the key
		for(TableAccountList item : list) {
			if (item.getAccountId() == id) { return item; }
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
			while (cursor.isAfterLast() == false) {
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
