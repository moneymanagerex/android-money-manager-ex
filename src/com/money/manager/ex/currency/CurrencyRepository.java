package com.money.manager.ex.currency;

import android.content.Context;
import android.database.Cursor;

import com.money.manager.ex.database.TableCurrencyFormats;

/**
 * Currency repository. Provides access to TableCurrencyFormats entities.
 */
public class CurrencyRepository {

    public CurrencyRepository(Context context) {
        mContext = context;
    }

    private Context mContext;

    public TableCurrencyFormats loadCurrency(int currencyId) {
        TableCurrencyFormats currency = new TableCurrencyFormats();

        Cursor cursor = mContext.getContentResolver().query(currency.getUri(),
                currency.getAllColumns(),
                TableCurrencyFormats.CURRENCYID + "=?",
                new String[] { Integer.toString(currencyId) },
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
