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

    @Override
    public String[] getAllColumns() {
        return new String[] {
            "CURRENCYID AS _id", Currency.CURRENCYID, Currency.CURRENCYNAME,
            Currency.PFX_SYMBOL, Currency.SFX_SYMBOL, Currency.DECIMAL_POINT,
            Currency.GROUP_SEPARATOR, Currency.UNIT_NAME, Currency.CENT_NAME,
            Currency.SCALE, Currency.BASECONVRATE, Currency.CURRENCY_SYMBOL
        };
    }

    public Currency load(int id) {
        WhereStatementGenerator where = new WhereStatementGenerator();
        where.addStatement(Currency.CURRENCYID, "=", id);

        return first(where.getWhere());
    }

    public boolean insert(Currency value) {
        return this.insert(value.contentValues) > 0;
    }

    public boolean update(Currency value) {
        int id = value.getCurrencyId();

        WhereStatementGenerator generator = new WhereStatementGenerator();
        String where = generator.getStatement(Currency.CURRENCYID, "=", id);

        return update(id, value.contentValues, where);
    }

    public boolean delete(int id) {
        int result = delete(Currency.CURRENCYID + "=?", new String[]{Integer.toString(id)});
        return result > 0;
    }

    public Currency loadCurrency(int currencyId) {
        return loadCurrency(
            Currency.CURRENCYID + "=?",
            new String[]{Integer.toString(currencyId)});
    }

    public Currency loadCurrency(String symbol) {
        return loadCurrency(
            Currency.CURRENCY_SYMBOL + "=?",
                new String[] { symbol });
    }

    public int saveExchangeRate(int currencyId, Money exchangeRate) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Currency.BASECONVRATE, exchangeRate.toString());

        int result = context.getContentResolver().update(this.getUri(),
            contentValues,
            Currency.CURRENCYID + "=?",
            new String[] { Integer.toString(currencyId) });

        return result;
    }

    // private methods

    private Currency loadCurrency(String selection, String[] selectionArgs) {
        Currency result = null;
        try {
            result = loadCurrencyInternal(selection, selectionArgs);
        } catch (Exception e) {
            ExceptionHandler handler = new ExceptionHandler(context, this);
            handler.handle(e, "loading currency");
        }
        return result;
    }

    private Currency loadCurrencyInternal(String selection, String[] selectionArgs) {
        Currency currency = new Currency();

        Cursor cursor = openCursor(getAllColumns(), selection, selectionArgs);
        if (cursor == null) return null;

        if (cursor.moveToNext()) {
            currency.loadFromCursor(cursor);
        } else {
            currency = null;
        }
        cursor.close();

        return currency;
    }

    public Currency first(String selection) {
        return query(null, selection, null);
    }

    public Currency query(String[] projection, String selection, String[] args) {
        Cursor c = openCursor(projection, selection, args);

        if (c == null) return null;

        Currency account = null;

        if (c.moveToNext()) {
            account = Currency.fromCursor(c);
        }

        c.close();

        return account;
    }

}
