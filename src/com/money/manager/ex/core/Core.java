package com.money.manager.ex.core;

import java.text.DateFormatSymbols;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.util.TypedValue;

import com.money.manager.ex.database.MoneyManagerOpenHelper;
import com.money.manager.ex.database.TableInfoTable;

public class Core {
	private static final String LOGCAT = Core.class.getSimpleName();
	public static final int INVALID_ATTRIBUTE = -1;
	public static final String INFO_NAME_USERNAME = "USERNAME";
	public static final String INFO_NAME_DATEFORMAT = "DATEFORMAT";
	public static final String INFO_NAME_FINANCIAL_YEAR_START_DAY = "FINANCIAL_YEAR_START_DAY";
	public static final String INFO_NAME_FINANCIAL_YEAR_START_MONTH = "FINANCIAL_YEAR_START_MONTH";
	
	
	private Context context;

	public Core(Context context) {
		super();
		this.context = context;
	}

	/**
	 * Resolves the id attribute in color
	 * 
	 * @param attr id attribute
	 * @return color
	 */
	public int resolveColorAttribute(int attr) {
		return context.getResources().getColor(resolveIdAttribute(attr));
	}
	
	/**
	 * Resolve the id attribute into int value
	 * @param attr id attribute
	 * @return
	 */
	public int resolveIdAttribute(int attr) {
		TypedValue tv = new TypedValue();
		if (context.getTheme().resolveAttribute(attr, tv, true))
			return tv.resourceId;
		else 
			return INVALID_ATTRIBUTE;
	}
	/**
	 * Retrieve value of info
	 * @param info to be retrieve
	 * @return value
	 */
	public String getInfoValue(String info) {
		TableInfoTable infoTable = new TableInfoTable();
		MoneyManagerOpenHelper helper = null;
		Cursor data = null;
		String ret = null;

		try {
			helper = new MoneyManagerOpenHelper(context);
			data = helper.getReadableDatabase().query(infoTable.getSource(), null, TableInfoTable.INFONAME + "=?", new String[] {info}, null, null, null);
			if (data != null && data.moveToFirst()) {
				ret = data.getString(data.getColumnIndex(TableInfoTable.INFOVALUE));
			}
		} catch (Exception e) {
			Log.e(LOGCAT, e.getMessage());
		} finally {
			// close data
			if (data != null)
				data.close();
			if (helper != null)
				helper.close();
		}
		
		return ret;
	}
	/**
	 * Update value of info
	 * @param info to be updated
	 * @param value
	 * @return true if update success otherwise false
	 */
	public boolean setInfoValue(String info, String value) {
		boolean ret = true;
		TableInfoTable infoTable = new TableInfoTable();
		MoneyManagerOpenHelper helper = null;
		ContentValues values = new ContentValues();
		values.put(TableInfoTable.INFOVALUE, value);
		
		try {
			helper = new MoneyManagerOpenHelper(context);
			ret = helper.getWritableDatabase().update(infoTable.getSource(), values, TableInfoTable.INFONAME + "=?", new String[] {info}) >= 0;
		} catch (Exception e) {
			Log.e(LOGCAT, e.getMessage());
			ret = false;
		} finally {
			if (helper != null)
				helper.close();
		}
		
		return ret;
	}
	/**
	 * Return arrays of month formatted and localizated
	 * @return arrays of months
	 */
	public String[] getListMonths() {
		return new DateFormatSymbols().getMonths();
	}
}
