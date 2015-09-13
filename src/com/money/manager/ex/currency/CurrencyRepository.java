package com.money.manager.ex.currency;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.money.manager.ex.core.ExceptionHandler;
import com.money.manager.ex.database.TableCurrencyFormats;

import java.math.BigDecimal;

import info.javaperformance.money.Money;

/**
 * Currency repository. Provides access to TableCurrencyFormats entities.
 */
public class CurrencyRepository {

    public CurrencyRepository(Context context) {
        mContext = context;
    }

    private Context mContext;
    private TableCurrencyFormats mCurrencyTable = new TableCurrencyFormats();

    public TableCurrencyFormats loadCurrency(int currencyId) {
        return loadCurrency(
                TableCurrencyFormats.CURRENCYID + "=?",
                new String[] { Integer.toString(currencyId) });
    }

    public TableCurrencyFormats loadCurrency(String symbol) {
        return loadCurrency(
                TableCurrencyFormats.CURRENCY_SYMBOL + "=?",
                new String[] { symbol });
    }

    public int saveExchangeRate(int currencyId, Money exchangeRate) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(TableCurrencyFormats.BASECONVRATE, exchangeRate.toString());

        int result = mContext.getContentResolver().update(mCurrencyTable.getUri(),
                contentValues,
                TableCurrencyFormats.CURRENCYID + "=?",
                new String[] { Integer.toString(currencyId) });

        return result;
    }

    // private methods

    private TableCurrencyFormats loadCurrency(String selection, String[] selectionArgs) {
        TableCurrencyFormats result = null;
        try {
            result = loadCurrencyInternal(selection, selectionArgs);
        } catch (Exception e) {
            ExceptionHandler handler = new ExceptionHandler(mContext, this);
            handler.handle(e, "loading currency");
        }
        return result;
    }

    private TableCurrencyFormats loadCurrencyInternal(String selection, String[] selectionArgs) {
        TableCurrencyFormats currency = mCurrencyTable;
        Cursor cursor = mContext.getContentResolver().query(currency.getUri(),
                currency.getAllColumns(),
                selection,
                selectionArgs,
                null);
        if (cursor == null) return null;

        if (cursor.moveToNext()) {
            currency.setValueFromCursor(cursor);
        } else {
            currency = null;
        }
        cursor.close();

        return currency;
    }
}
