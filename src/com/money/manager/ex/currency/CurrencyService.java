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
package com.money.manager.ex.currency;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;
import android.widget.Toast;

import com.money.manager.ex.Constants;
import com.money.manager.ex.R;
import com.money.manager.ex.core.ExceptionHandler;
import com.money.manager.ex.core.NumericHelper;
import com.money.manager.ex.database.AccountRepository;
import com.money.manager.ex.database.TableAccountList;
import com.money.manager.ex.database.TableCurrencyFormats;
import com.money.manager.ex.database.TableInfoTable;
import com.money.manager.ex.settings.AppSettings;

import java.util.ArrayList;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * This class implements all the methods of utility for the management of currencies.
 *
 * @author lazzari.ale@gmail.com
 */
public class CurrencyService {

    private static final String LOGCAT = CurrencyService.class.getSimpleName();
    // id base currency
    private static Integer mBaseCurrencyId = null;
    // hash map of all currencies
    private static Map<Integer, TableCurrencyFormats> mCurrencies;

    public static void destroy() {
        mCurrencies = null;
        mBaseCurrencyId = null;
    }

    public CurrencyService(Context context) {
        mContext = context.getApplicationContext();
    }

    // context
    private Context mContext;

    public String getBaseCurrencyCode() {
        // get base currency
        int baseCurrencyId = this.getBaseCurrencyId();

        TableCurrencyFormats currency = this.getCurrency(baseCurrencyId);
        if (currency == null) {
            ExceptionHandler handler = new ExceptionHandler(mContext, this);
            handler.showMessage(mContext.getString(R.string.base_currency_not_set));
            return "";
        }
        return currency.getCurrencySymbol();
    }

    public Boolean reInit() {
        destroy();

        return loadAllCurrencies();
    }

    public Map<Integer, TableCurrencyFormats> getCurrenciesStore() {
        if (mCurrencies == null) mCurrencies = new HashMap<>();
        return mCurrencies;
    }

    public List<TableCurrencyFormats> getUsedCurrencies() {
        AppSettings settings = new AppSettings(mContext);
        AccountRepository repo = new AccountRepository(mContext);

        boolean favourite = settings.getLookAndFeelSettings().getViewFavouriteAccounts();
        boolean open = settings.getLookAndFeelSettings().getViewOpenAccounts();
        List<TableAccountList> accounts = repo.getAccountList(open, favourite);

        List<TableCurrencyFormats> currencies = new ArrayList<>();
        for(TableAccountList account : accounts) {
            TableCurrencyFormats currency = getCurrency(account.getCurrencyId());
            if (!currencies.contains(currency)) {
                currencies.add(currency);
            }
        }

        return currencies;
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
        double result = (amount * fromConversionRate) / toConversionRate;
        return result;
    }

    /**
     * Get all currencies format
     *
     * @return list of all CurrencyFormats
     */
    public List<TableCurrencyFormats> getAllCurrencyFormats() {
        // try loading the currencies first.
        this.loadAllCurrencies();

        return new ArrayList<>(getCurrenciesStore().values());
    }

    /**
     * Get id of base currency.
     * Lazy loaded, no need to initialize separately.
     *
     * @return Id of base currency
     */
    public int getBaseCurrencyId() {
        int result = Constants.NOT_SET;

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
     * Formats the given value, in base currency, as a string for display.
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
        String result;

        // check if value is null
        if (value == null) value = 0d;

        // find currency id
        if (currencyId != null) {
            TableCurrencyFormats tableCurrency = getCurrency(currencyId);

            if (tableCurrency == null) {
                result = String.valueOf(value);
            } else {
                // formatted value
                NumericHelper helper = new NumericHelper(mContext);
                result = helper.getValueFormatted(value, tableCurrency);
            }
        } else {
            result = String.valueOf(value);
        }
        return result;
    }

    /**
     * @param currencyId of the currency to be get
     * @return an instance of class TableCurrencyFormats. Null if fail
     */
    public TableCurrencyFormats getCurrency(int currencyId) {
        // check if the currency is cached.
        TableCurrencyFormats result =  getCurrenciesStore().get(currencyId);

        // if not cached, try to load it.
        if (result == null) {
            CurrencyRepository repository = new CurrencyRepository(mContext);
            result = repository.loadCurrency(currencyId);

            // cache
            if (!getCurrenciesStore().containsKey(currencyId)) {
                getCurrenciesStore().put(currencyId, result);
            }
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
        } else {
            Toast.makeText(mContext, mContext.getString(R.string.error_saving_default_currency), Toast.LENGTH_SHORT)
                    .show();
        }

        return success;
    }

    /**
     *  Load all currencies into map
     */
    public boolean loadAllCurrencies() {
        boolean result = true;
        TableCurrencyFormats tableCurrency = new TableCurrencyFormats();
        Cursor cursor;

        try {
            cursor = mContext.getContentResolver().query(tableCurrency.getUri(),
                    tableCurrency.getAllColumns(),
                    null, null, null);
            if (cursor == null) return false;

            // load data into map
            while (cursor.moveToNext()) {
                cacheCurrencyFromCursor(cursor);
            }
            cursor.close();
        } catch (Exception e) {
            Log.e(LOGCAT, "Error loading currencies: " + e.getMessage());
        }

        return result;
    }

    private void cacheCurrencyFromCursor(Cursor cursor) {
        TableCurrencyFormats currency = new TableCurrencyFormats();
        currency.setValueFromCursor(cursor);

        Integer currencyId = cursor.getInt(cursor.getColumnIndex(TableCurrencyFormats.CURRENCYID));
        // put object into map
        getCurrenciesStore().put(currencyId, currency);
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

        try {
            Cursor cursor = mContext.getContentResolver().query(tableInfo.getUri(),
                    tableInfo.getAllColumns(),
                    TableInfoTable.INFONAME + "=?",
                    new String[]{ Constants.INFOTABLE_BASECURRENCYID },
                    null, null);
            if (cursor == null) return null;

            // set BaseCurrencyId
            if (cursor.moveToFirst()) {
                currencyId = cursor.getInt(cursor.getColumnIndex(TableInfoTable.INFOVALUE));
            }
        } catch (Exception e) {
            ExceptionHandler handler = new ExceptionHandler(mContext, this);
            handler.handle(e, "init base currency");
        }

        return currencyId;
    }

    public Currency getSystemDefaultCurrency() {
        Currency currency = null;
        Locale defaultLocale = null;

        try {
            defaultLocale = Locale.getDefault();
            currency = Currency.getInstance(defaultLocale);
        } catch (Exception ex) {
            String message = "getting default system currency";
            if (defaultLocale != null) {
                message += " for " + defaultLocale.getCountry();
            }
            ExceptionHandler handler = new ExceptionHandler(mContext, this);
            handler.handle(ex, message);
        }
        return currency;
    }

    /**
     * Retrieves the currency Id for the given symbol.
     * Accesses the database directly.
     * @param currencySymbol
     * @param database
     * @return
     */
    public int loadCurrencyIdFromSymbolRaw(String currencySymbol, SQLiteDatabase database) {
        Cursor cursor = database.rawQuery(
                "SELECT " + TableCurrencyFormats.CURRENCYID +
                        " FROM CURRENCYFORMATS_V1" +
                        " WHERE " + TableCurrencyFormats.CURRENCY_SYMBOL + "=?",
                new String[]{ currencySymbol });
        if (cursor == null || !cursor.moveToFirst()) return Constants.NOT_SET;

        int result = cursor.getInt(0);

        cursor.close();

        return result;
    }
}
