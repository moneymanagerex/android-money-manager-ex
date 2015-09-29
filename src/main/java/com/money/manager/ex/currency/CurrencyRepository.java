package com.money.manager.ex.currency;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.money.manager.ex.core.ExceptionHandler;
import com.money.manager.ex.database.DatasetType;
import com.money.manager.ex.datalayer.RepositoryBase;
import com.money.manager.ex.database.TableCurrencyFormats;
import com.money.manager.ex.database.WhereStatementGenerator;
import com.money.manager.ex.domainmodel.Currency;

import info.javaperformance.money.Money;

/**
 * Currency repository. Provides access to TableCurrencyFormats entities.
 */
public class CurrencyRepository
    extends RepositoryBase {

    public CurrencyRepository(Context context) {
        super(context, "currencyformats_v1", DatasetType.TABLE, "currencyformats");

    }

    public boolean insert(Currency currency) {
        return this.insert(currency.contentValues) > 0;
    }

    public boolean update(int id, Currency value) {
        WhereStatementGenerator generator = new WhereStatementGenerator();
        String where = generator.getStatement(TableCurrencyFormats.CURRENCYID, "=", id);

        return update(id, value.contentValues, where);
    }

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

        int result = context.getContentResolver().update(this.getUri(),
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
            ExceptionHandler handler = new ExceptionHandler(context, this);
            handler.handle(e, "loading currency");
        }
        return result;
    }

    private TableCurrencyFormats loadCurrencyInternal(String selection, String[] selectionArgs) {
        TableCurrencyFormats currency = new TableCurrencyFormats();

        Cursor cursor = this.openCursor(currency.getAllColumns(), selection, selectionArgs);
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
