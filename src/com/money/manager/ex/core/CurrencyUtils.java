package com.money.manager.ex.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;

import com.money.manager.ex.Constants;
import com.money.manager.ex.database.MoneyManagerOpenHelper;
import com.money.manager.ex.database.TableCurrencyFormats;
import com.money.manager.ex.database.TableInfoTable;

/**
 * This class implements all the methods of utility for the management of currencies
 * @author lazzari.ale@gmail.com
 *
 */

public class CurrencyUtils {
	private static final String LOGCAT = CurrencyUtils.class.getSimpleName();
	// context
	private Context mContext;
	// TableInfoTable
	private TableInfoTable mInfoTable = new TableInfoTable();
	// id base currency
	private static Integer mBaseCurrencyId = null;
	// hash map of all currencies
	private static Map<Integer, TableCurrencyFormats> mCurrencies;
	
	public CurrencyUtils(Context context) {
		mContext = context;
		init();
	}
	
	public CurrencyUtils(Context context, Boolean init) {
		mContext = context;
		if (init)
			init();
	}
	
	/**
	 * Initializes the structures of class
	 * @return Return true if initialization successfully otherwise Return Boolean.FALSE
	 */
	public Boolean init() {
		// check if map currencies is create 
		if (mCurrencies == null) {
			mCurrencies = new HashMap<Integer, TableCurrencyFormats>();
			
			// clear map for new populate
			mCurrencies.clear();
			
			// load all currencies
			if (!loadCurrencies())
				return Boolean.FALSE;
		}
		
		// load id base currency
		if (mBaseCurrencyId == null)
			mBaseCurrencyId = getInitBaseCurrencyId();
		return Boolean.TRUE;
	}
	
	/**
	 * 
	 * @return true if wrapper is init
	 */
	public Boolean isInit() {
		return mCurrencies != null && mCurrencies.size() > 0;
	}
	
	public Boolean reInit() {
		destroy();
		
		return init();
	}
	
	public static void destroy() {
		mCurrencies = null;
		mBaseCurrencyId = null;
	}
	
	/**
	 * Get all currencies format
	 * @return list of all CurrencyFormats
	 */
	public List<TableCurrencyFormats> getAllCurrencyFormats() {
		if (mCurrencies != null) {
			return new ArrayList<TableCurrencyFormats>(mCurrencies.values()); 
		} else {
			return Collections.emptyList();
		}
	}	
	
	/**
	 * Get id of base currency
	 * @return Id of base currency
	 */
	public Integer getBaseCurrencyId() {
		return mBaseCurrencyId;
	}

	/**
	 * 
	 * @param value to format
	 * @return formatted value
	 */
	public String getBaseCurrencyFormatted(Float value) {
		return this.getCurrencyFormatted(mBaseCurrencyId, value);
	}
	
	/**
	 * 
	 * @param value to format
	 * @return fomatted value
	 */
	public String getBaseNumericFormatted(Float value) {
		return getNumericFormatted(mBaseCurrencyId, value);
	}

	/**
	 * 
	 * @param currencyId of the currency to be formatted
	 * @param value value to format
	 * @return formatted value
	 */
	public String getCurrencyFormatted(Integer currencyId, Float value) {
		// find currencyid
		TableCurrencyFormats tableCurrency = getTableCurrencyFormats(currencyId);
	
		if (tableCurrency == null) {
			return String.valueOf(value);
		}
		
		// formatted value
		return tableCurrency.getValueFormatted(value);
	}

	/**
	 * 
	 * @param currencyId of the currency to be formatted
	 * @param value value to format
	 * @return formatted value
	 */
	public String getNumericFormatted(Integer currencyId, Float value) {
		// find currency
		TableCurrencyFormats tableCurrency = getTableCurrencyFormats(currencyId);
		
		if (tableCurrency == null) {
			return String.valueOf(value);
		}
		// formatted value
		return tableCurrency.getValueFormatted(value, Boolean.FALSE);		
	}
	
	/**
	 * @param currencyId of the currency to be get
	 * @return an instance of class TableCurrencyFormats. Null if fail
	 */
	public TableCurrencyFormats getTableCurrencyFormats(Integer currencyId) {
		if (mCurrencies != null) {
			return mCurrencies.get(currencyId);
		} else {
			return null;
		}
	}
	
	/**
	 * Update database with new Base Currency Id 
	 * @param currencyId of the currency
	 * @return true if update succeed, otherwise false
	 */
	public Boolean setBaseCurrencyId(Integer currencyId) {
		// update data into database
		ContentValues values = new ContentValues();
		values.put(TableInfoTable.INFOVALUE, currencyId);

		return mContext.getContentResolver().update(mInfoTable.getUri(), values, TableInfoTable.INFONAME + "=?",
				new String[] { Constants.INFOTABLE_BASECURRENCYID }) == 1;
	}

	/*
	 * Load all currencies into map
	 */
	protected Boolean loadCurrencies() {
		Boolean ret = Boolean.TRUE;
		// ************************************************************
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		TableCurrencyFormats tableCurrency = new TableCurrencyFormats();
		MoneyManagerOpenHelper helper = null;
		Cursor cursor = null;
		 
		try {
			// set table name
			queryBuilder.setTables(tableCurrency.getSource());
			helper = new MoneyManagerOpenHelper(mContext);
			cursor = queryBuilder.query(helper.getReadableDatabase(), tableCurrency.getAllColumns(), null, null, null, null, null);

			// load data into map
			if (cursor != null && cursor.moveToFirst()) {
				while (!cursor.isAfterLast()) {
					TableCurrencyFormats mapCur = new TableCurrencyFormats();
					mapCur.setValueFromCursor(cursor);

					Integer currencyId = cursor.getInt(cursor.getColumnIndex(TableCurrencyFormats.CURRENCYID));
					// put object into map
					mCurrencies.put(currencyId, mapCur);

					cursor.moveToNext();
				}
				cursor.close();
			} else {
				ret = Boolean.FALSE;
			}
		} catch (Exception e) {
			Log.e(LOGCAT, e.getMessage());
		} finally {
			if (helper != null)
				helper.close();
		}

		return ret;
	}
	
	/**
	 * Get id of base currency
	 * @return Id base currency
	 */
	protected Integer getInitBaseCurrencyId() {
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		TableInfoTable tableInfo = new TableInfoTable();
		Integer currencyId = null;
		
		// set table
		queryBuilder.setTables(tableInfo.getSource());
		
		// get cursor from query builder
		MoneyManagerOpenHelper helper = null;
		Cursor cursorInfo = null;
		try {
			helper = new MoneyManagerOpenHelper(mContext);
			cursorInfo = queryBuilder.query(helper.getReadableDatabase(), tableInfo.getAllColumns(), TableInfoTable.INFONAME + "=?", new String[] {Constants.INFOTABLE_BASECURRENCYID}, null, null, null);
			// set BaseCurrencyId
			if (cursorInfo != null && cursorInfo.moveToFirst()) {
				currencyId = cursorInfo.getInt(cursorInfo.getColumnIndex(TableInfoTable.INFOVALUE));
			}
		} catch (Exception e) {
			Log.e(LOGCAT, e.getMessage());
		} finally {
			if (helper != null)
				helper.close();
		}
		return currencyId;
	}
}
