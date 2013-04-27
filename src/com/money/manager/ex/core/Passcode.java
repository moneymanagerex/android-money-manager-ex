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
package com.money.manager.ex.core;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.money.manager.ex.R;
import com.money.manager.ex.database.MoneyManagerOpenHelper;
import com.money.manager.ex.database.TableInfoTable;
import com.money.manager.ex.dropbox.SimpleCrypto;

public class Passcode {
	private Context mContext;
	private static final String KEY = "6c2a6f30726b3447747559525162665768412370297c5573342324705b";
	private static final String LOGCAT = Passcode.class.getSimpleName();
	private static final String INFONAME = "PASSCODEMOBILE";

	/**
	 * Constructor of class
	 * 
	 * @param context
	 */
	public Passcode(Context context) {
		this.mContext = context;
	};

	/**
	 * Decrypt passcode
	 * 
	 * @param s
	 *            passcode crypted
	 * @return passcode
	 */
	private String decrypt(String s) {
		String ret = null;
		try {
			ret = SimpleCrypto.decrypt(KEY, s);
		} catch (Exception e) {
			Log.e(LOGCAT, e.getMessage());
		}
		return ret;
	};

	/**
	 * Encrypt clear passcode
	 * 
	 * @param s
	 *            clear passcode
	 * @return
	 */
	private String encrypt(String s) {
		String ret = null;
		try {
			ret = SimpleCrypto.encrypt(KEY, s);
		} catch (Exception e) {
			Log.e(LOGCAT, e.getMessage());
		}
		return ret;
	};

	/**
	 * Get decrypt passcode
	 * 
	 * @return null if not set passcode else passcode
	 */
	public String getPasscode() {
		String ret = retrievePasscode();
		if (ret != null) {
			// decrypt passcode
			ret = decrypt(ret);
		}
		return ret;
	};

	/**
	 * Return true if passcode has set otherwise false
	 * 
	 * @return
	 */
	public boolean hasPasscode() {
		return !(TextUtils.isEmpty(retrievePasscode()));
	};

	private String retrievePasscode() {
		String ret = null;
		// open connection to database

		TableInfoTable infoTable = new TableInfoTable();
		MoneyManagerOpenHelper helper =  new MoneyManagerOpenHelper(mContext);
		Cursor cursor = helper.getReadableDatabase().query(infoTable.getSource(), null, TableInfoTable.INFONAME + "=?", new String[] { INFONAME }, null, null, null);
		if (cursor != null && cursor.moveToFirst()) {
			ret = cursor.getString(cursor.getColumnIndex(TableInfoTable.INFOVALUE));
		}
		// close connection
		cursor.close();
		helper.close();
		// return
		return ret;
	}

	/**
	 * Set a decrypt passcode
	 * 
	 * @param passcode
	 */
	public boolean setPasscode(String passcode) {
		return updatePasscode(encrypt(passcode));
	}

	/**
	 * Set a passcode into database
	 * 
	 * @param passcode
	 */
	private boolean updatePasscode(String passcode) {
		// content values
		ContentValues contentValues = new ContentValues();
		contentValues.put(TableInfoTable.INFONAME, INFONAME);
		contentValues.put(TableInfoTable.INFOVALUE, passcode);

		if (hasPasscode()) {
			// update data
			if (mContext.getContentResolver().update(new TableInfoTable().getUri(), contentValues, TableInfoTable.INFONAME + "=?", new String[] { INFONAME }) <= 0) {
				Toast.makeText(mContext, R.string.db_update_failed, Toast.LENGTH_LONG).show();
				return false;
			}
		} else {
			// insert data
			if (mContext.getContentResolver().insert(new TableInfoTable().getUri(), contentValues) == null) {
				Toast.makeText(mContext, R.string.db_insert_failed, Toast.LENGTH_LONG).show();
				return false;
			}
		}

		return true;
	}

	public boolean cleanPasscode() {
		if (mContext.getContentResolver().delete(new TableInfoTable().getUri(), TableInfoTable.INFONAME + "=?", new String[] { INFONAME }) <= 0) {
			Toast.makeText(mContext, R.string.db_delete_failed, Toast.LENGTH_LONG).show();
			return false;
		} else
			return true;
	}
}