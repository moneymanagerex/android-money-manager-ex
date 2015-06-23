/*
 * Copyright (C) 2012-2015 The Android Money Manager Ex Project Team
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
 */
package com.money.manager.ex.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.os.StrictMode;
import android.text.TextUtils;
import android.util.Log;

import com.money.manager.ex.BuildConfig;
import com.money.manager.ex.Constants;
import com.money.manager.ex.currency.CurrencyRepository;
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
 * This class implements all the methods of utility for the management of currencies.
 *
 * @author lazzari.ale@gmail.com
 */
public class CurrencyUtils {

    private static final String LOGCAT = CurrencyUtils.class.getSimpleName();
    private static final String URL_FREE_CURRENCY_CONVERT_API = "http://www.freecurrencyconverterapi.com/api/convert?q=SYMBOL&compact=y";
    // id base currency
    private static Integer mBaseCurrencyId = null;
    // hash map of all currencies
    private static Map<Integer, TableCurrencyFormats> mCurrencies;

    // context
    private Context mContext;

    public CurrencyUtils(Context context) {
        mContext = context;
//        loadCurrencies();
    }

    public static void destroy() {
        mCurrencies = null;
        mBaseCurrencyId = null;
    }

//    /**
//     * @return true if wrapper is init
//     */
//    public Boolean isInit() {
//        return mCurrencies != null && mCurrencies.size() > 0;
//    }

    public Boolean reInit() {
        destroy();

        return loadCurrencies();
    }

    public Double doCurrencyExchange(Integer toCurrencyId, double amount, Integer fromCurrencyId) {
        TableCurrencyFormats fromCurrencyFormats = getCurrency(fromCurrencyId);
        TableCurrencyFormats toCurrencyFormats = getCurrency(toCurrencyId);
        // check if exists from and to currencies
        if (fromCurrencyFormats == null || toCurrencyFormats == null)
            return null;
        // exchange
        double toConversionRate = toCurrencyFormats.getBaseConvRate();
        double fromConversionRate = fromCurrencyFormats.getBaseConvRate();
//        double result = (amount * toConversionRate) / fromConversionRate;
        double result = (amount * fromConversionRate) / toConversionRate;
        return result;
    }

    public boolean updateCurrencyRateFromBase(Integer toCurrencyId) {
        return updateCurrencyRate(getBaseCurrencyId(), toCurrencyId);
    }

    public boolean updateCurrencyRate(Integer fromCurrencyId, Integer toCurrencyId) {
        TableCurrencyFormats fromCurrencyFormats = getCurrency(fromCurrencyId);
        TableCurrencyFormats toCurrencyFormats = getCurrency(toCurrencyId);
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
        String symbolRate = toSymbol + "-" + fromSymbol;
        // compose url
        String url = URL_FREE_CURRENCY_CONVERT_API.replace("SYMBOL", symbolRate);
        // check if DEBUG
        if (BuildConfig.DEBUG) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        // new connection
        // todo: use this
//        URL urlObject = createUrl(url);
//        HttpURLConnection urlConnection = (HttpURLConnection) urlObject.openConnection();


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
                Double rate = jsonRate.getDouble("val");
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
     *
     * @return list of all CurrencyFormats
     */
    public List<TableCurrencyFormats> getAllCurrencyFormats() {
        // try loading the currencies first.
        if (mCurrencies == null) {
            this.loadCurrencies();
        }

        if (mCurrencies != null) {
            return new ArrayList<>(mCurrencies.values());
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Get id of base currency.
     * Lazy loaded, no need to initialize separately.
     *
     * @return Id of base currency
     */
    public int getBaseCurrencyId() {
        int result = -1;

        // lazy loading the base currency id.
        if (mBaseCurrencyId == null) {
            Integer baseCurrencyId = getInitBaseCurrencyId();
            if(baseCurrencyId != null) {
                setBaseCurrencyId(baseCurrencyId);
                result = baseCurrencyId;
            }
        } else {
            result = mBaseCurrencyId;
        }

        return result;
    }

    public void setBaseCurrencyId(int baseCurrencyId) {
        mBaseCurrencyId = baseCurrencyId;
    }

    public TableCurrencyFormats getBaseCurrency() {
        int baseCurrencyId = getBaseCurrencyId();
        return getCurrency(baseCurrencyId);
    }

    /**
     * @param value to format
     * @return formatted value
     */
    public String getBaseCurrencyFormatted(Double value) {
        int baseCurrencyId = getBaseCurrencyId();
        return this.getCurrencyFormatted(baseCurrencyId, value);
    }

    /**
     * @param currencyId of the currency to be formatted
     * @param value      value to format
     * @return formatted value
     */
    public String getCurrencyFormatted(Integer currencyId, Double value) {
        // check if value is null
        if (value == null) value = 0d;

        // find currency id
        if (currencyId != null) {
            TableCurrencyFormats tableCurrency = getCurrency(currencyId);

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
     * @param currencyId of the currency to be get
     * @return an instance of class TableCurrencyFormats. Null if fail
     */
    public TableCurrencyFormats getCurrency(int currencyId) {
        TableCurrencyFormats result = null;

        // check if the currency is cached.
        if (mCurrencies != null) {
            result =  mCurrencies.get(currencyId);
        }
        // if not cached, try to load it.
        if (result == null) {
            CurrencyRepository repository = new CurrencyRepository(mContext);
            result = repository.loadCurrency(currencyId);
        }

        return result;
    }

    /**
     * Update database with new Base Currency Id
     *
     * @param currencyId of the currency
     * @return true if update succeed, otherwise false
     */
    public Boolean saveBaseCurrencyId(Integer currencyId) {
        TableInfoTable mInfoTable = new TableInfoTable();

        // update data into database
        ContentValues values = new ContentValues();
        values.put(TableInfoTable.INFOVALUE, currencyId);

        boolean success = mContext.getContentResolver().update(mInfoTable.getUri(),
                values,
                TableInfoTable.INFONAME + "=?",
                new String[]{Constants.INFOTABLE_BASECURRENCYID}) == 1;

        // cache the new base currency
        if (success) {
            mBaseCurrencyId = currencyId;
        }

        return success;
    }

    /**
     *  Load all currencies into map
     */
    public boolean loadCurrencies() {
        boolean result = true;
        TableCurrencyFormats tableCurrency = new TableCurrencyFormats();
        Cursor cursor;
        mCurrencies = new HashMap<>();

        try {
            cursor = mContext.getContentResolver().query(tableCurrency.getUri(),
                    tableCurrency.getAllColumns(),
                    null, null, null);
            if (cursor == null) return false;

            // load data into map
            while (cursor.moveToNext()) {
                TableCurrencyFormats mapCur = new TableCurrencyFormats();
                mapCur.setValueFromCursor(cursor);

                Integer currencyId = cursor.getInt(cursor.getColumnIndex(TableCurrencyFormats.CURRENCYID));
                // put object into map
                mCurrencies.put(currencyId, mapCur);
            }
            cursor.close();
        } catch (Exception e) {
            Log.e(LOGCAT, "Error loading currencies: " + e.getMessage());
        }

        return result;
    }

    /**
     * Get id of base currency
     *
     * @return Id base currency
     */
    protected Integer getInitBaseCurrencyId() {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        TableInfoTable tableInfo = new TableInfoTable();
        Integer currencyId = null;

        // set table
        queryBuilder.setTables(tableInfo.getSource());

        // get cursor from query builder
        MoneyManagerOpenHelper helper;
        Cursor cursorInfo;

        try {
            helper = MoneyManagerOpenHelper.getInstance(mContext);
            cursorInfo = queryBuilder.query(helper.getReadableDatabase(),
                    tableInfo.getAllColumns(),
                    TableInfoTable.INFONAME + "=?",
                    new String[]{Constants.INFOTABLE_BASECURRENCYID}, null, null, null);
            if (cursorInfo == null) return null;

            // set BaseCurrencyId
            if (cursorInfo.moveToFirst()) {
                currencyId = cursorInfo.getInt(cursorInfo.getColumnIndex(TableInfoTable.INFOVALUE));
            }
        } catch (Exception e) {
            Log.e(LOGCAT, e.getMessage());
        }

        return currencyId;
    }

    public String getBaseCurrencyCode() {
        // get base currency
        int baseCurrencyId = this.getBaseCurrencyId();

        TableCurrencyFormats currency = this.getCurrency(baseCurrencyId);
        return currency.getCurrencySymbol();
    }
}
