package com.money.manager.ex.core;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.os.StrictMode;
import android.text.TextUtils;
import android.util.Log;

import com.money.manager.ex.BuildConfig;
import com.money.manager.ex.Constants;
import com.money.manager.ex.database.MoneyManagerOpenHelper;
import com.money.manager.ex.database.TableCurrencyFormats;
import com.money.manager.ex.database.TableInfoTable;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class implements all the methods of utility for the management of currencies
 * @author lazzari.ale@gmail.com
 *
 */

public class CurrencyUtils {
	private static final String LOGCAT = CurrencyUtils.class.getSimpleName();
	private static final String URL_FREE_CURRENCY_CONVERT_API = "http://www.freecurrencyconverterapi.com/api/convert?q=SYMBOL&compact=y";
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
	
	public Double doCurrencyExchange(Integer toCurrencyId, double toAmount, Integer fromCurrencyId) {
		TableCurrencyFormats fromCurrencyFormats = getTableCurrencyFormats(fromCurrencyId);
		TableCurrencyFormats toCurrencyFormats = getTableCurrencyFormats(toCurrencyId);
		// check if exists from and to currencies
		if (fromCurrencyFormats == null || toCurrencyFormats == null)
			return null;
		// exchange
		return (double) ((toAmount * toCurrencyFormats.getBaseConvRate()) / fromCurrencyFormats.getBaseConvRate());
	}
	
	public boolean updateCurrencyRateFromBase(Integer toCurrencyId) {
		return updateCurrencyRate(getBaseCurrencyId(), toCurrencyId);
	}
	
	public boolean updateCurrencyRate(Integer fromCurrencyId, Integer toCurrencyId) {
		TableCurrencyFormats fromCurrencyFormats = getTableCurrencyFormats(fromCurrencyId);
		TableCurrencyFormats toCurrencyFormats = getTableCurrencyFormats(toCurrencyId);
		// check if exists from and to currencies
		if (fromCurrencyFormats == null || toCurrencyFormats == null)
			return false;
		// take symbol from and to currencies
		String fromSymbol = fromCurrencyFormats.getCurrencySymbol();
		String toSymbol = toCurrencyFormats.getCurrencySymbol();
		// check if symbol is empty
		if (TextUtils.isEmpty(fromSymbol) || TextUtils.isEmpty(toSymbol))
			return false;
		// compose symbol
		String symbolRate = fromSymbol + "-" + toSymbol;
		// compose url
		String url = URL_FREE_CURRENCY_CONVERT_API.replace("SYMBOL", symbolRate);
		// check if DEBUG
		if (BuildConfig.DEBUG) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		    StrictMode.setThreadPolicy(policy); 
		}
		// compose connection
		StringBuilder stringBuilder = new StringBuilder();
		HttpClient client = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(url);
		try {
			HttpResponse httpResponse = client.execute(httpGet);
			StatusLine statusLine = httpResponse.getStatusLine();
			if (statusLine.getStatusCode() == 200) {
				HttpEntity httpEntity = httpResponse.getEntity();
				InputStream inputStream = httpEntity.getContent();
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
				String line;
				while ((line = bufferedReader.readLine()) != null) {
					stringBuilder.append(line);
				}
				// close buffer and stream
				bufferedReader.close();
				inputStream.close();
				// convert string builder in json object
				JSONObject jsonObject = new JSONObject(stringBuilder.toString());
				JSONObject jsonRate = jsonObject.getJSONObject(symbolRate);
				Double rate = (double) jsonRate.getDouble("val");
				// update value on database
				ContentValues contentValues = new ContentValues();
				contentValues.put(TableCurrencyFormats.BASECONVRATE, rate);

				return mContext.getContentResolver().update(toCurrencyFormats.getUri(), contentValues, TableCurrencyFormats.CURRENCYID + "=" + Integer.toString(toCurrencyId), null) > 0;
			}
		} catch (ClientProtocolException e) {
			Log.e(LOGCAT, e.getMessage());
			return false;
		} catch (IOException e) {
			Log.e(LOGCAT, e.getMessage());
			return false;
		} catch (JSONException e) {
			Log.e(LOGCAT, e.getMessage());
			return false;
		}
		return true;
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
	public String getBaseCurrencyFormatted(Double value) {
		return this.getCurrencyFormatted(mBaseCurrencyId, value);
	}
	
	/**
	 * 
	 * @param value to format
	 * @return fomatted value
	 */
	public String getBaseNumericFormatted(Double value) {
		return getNumericFormatted(mBaseCurrencyId, value);
	}

	/**
	 * 
	 * @param currencyId of the currency to be formatted
	 * @param value value to format
	 * @return formatted value
	 */
	public String getCurrencyFormatted(Integer currencyId, Double value) {
		// check if value is null
		if (value == null)
			value = 0d;
		
		// find currencyid
		if (currencyId != null) {
			TableCurrencyFormats tableCurrency = getTableCurrencyFormats(currencyId);
		
			if (tableCurrency == null) {
				return String.valueOf(value);
			}
			
			// formatted value
			return tableCurrency.getValueFormatted(value);
		} else {
			return String.valueOf(value);
		}
	}

	/**
	 * 
	 * @param currencyId of the currency to be formatted
	 * @param value value to format
	 * @return formatted value
	 */
	public String getNumericFormatted(Integer currencyId, Double value) {
		// check if value is null
		if (value == null)
			value = 0d;
		
		// find currencyid
		if (currencyId != null) {
			TableCurrencyFormats tableCurrency = getTableCurrencyFormats(currencyId);
			
			if (tableCurrency == null) {
				return String.valueOf(value);
			}
			// formatted value
			return tableCurrency.getValueFormatted(value, Boolean.FALSE);
		} else {
			return String.valueOf(value);
		}
	}
	
	/**
	 * @param currencyId of the currency to be get
	 * @return an instance of class TableCurrencyFormats. Null if fail
	 */
	public TableCurrencyFormats getTableCurrencyFormats(Integer currencyId) {
		if (mCurrencies != null && currencyId != null) {
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
