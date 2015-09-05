package com.money.manager.ex.database;

import android.content.Context;
import android.database.Cursor;

import com.money.manager.ex.model.Stock;

/**
 * Data repository for Stock entities.
 * This is an experiment on how to replace the current dataset objects.
 *
 * Created by Alen on 5/09/2015.
 */
public class StockRepository
    extends RepositoryBase {

    public StockRepository(Context context) {
        super(context, "stock_v1", DatasetType.TABLE, "stock");

    }

    public Stock load(int id) {
        Cursor cursor = mContext.getContentResolver().query(this.getUri(),
                null,
                TableStock.STOCKID + "=?",
                new String[] { Integer.toString(id) },
                null);
        if (cursor == null) return null;

        cursor.moveToNext();

        Stock stock = new Stock(cursor);

        cursor.close();

        return stock;
    }
}
