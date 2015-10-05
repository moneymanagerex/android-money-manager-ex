package com.money.manager.ex.datalayer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;

import com.money.manager.ex.Constants;
import com.money.manager.ex.database.DatasetType;
import com.money.manager.ex.database.WhereStatementGenerator;
import com.money.manager.ex.domainmodel.Stock;

import org.apache.commons.lang3.ArrayUtils;

import java.math.BigDecimal;

import info.javaperformance.money.Money;

/**
 * Data repository for Stock entities.
 */
public class StockRepository
    extends RepositoryBase {

    public StockRepository(Context context) {
        super(context, "stock_v1", DatasetType.TABLE, "stock");

    }

    @Override
    public String[] getAllColumns() {
        String [] idColumn = new String[] {
                "STOCKID AS _id"
        };

        return ArrayUtils.addAll(idColumn, tableColumns());
    }

    public String[] tableColumns() {
        return new String[] {
            Stock.STOCKID,
            Stock.CURRENTPRICE,
            Stock.COMMISSION,
            Stock.HELDAT,
            Stock.PURCHASEDATE,
            Stock.STOCKNAME,
            Stock.SYMBOL,
            Stock.NOTES,
            Stock.NUMSHARES,
            Stock.PURCHASEPRICE,
            Stock.VALUE
        };
    }

    public Stock load(int id) {
        if (id == Constants.NOT_SET) return null;

        Cursor cursor = context.getContentResolver().query(this.getUri(),
                null,
                Stock.STOCKID + "=?",
                new String[] { Integer.toString(id) },
                null);
        if (cursor == null) return null;

        Stock stock = null;
        if (cursor.moveToNext()) {
            stock = new Stock();
            stock.loadFromCursor(cursor);
        }

        cursor.close();

        return stock;
    }

    public ContentValues loadContentValues(int id) {
        if (id == Constants.NOT_SET) return null;

        Cursor cursor = context.getContentResolver().query(this.getUri(),
                null,
                Stock.STOCKID + "=?",
                new String[] { Integer.toString(id)},
                null);
        if (cursor == null) return null;
        if (!cursor.moveToNext()) return null;

        ContentValues stockValues = new ContentValues();

        String[] columns = tableColumns();
        for(String column : columns) {
            DatabaseUtils.cursorDoubleToContentValuesIfPresent(cursor, stockValues, column);
        }

        cursor.close();

        return stockValues;
    }

//    public boolean loadFor(int accountId) {
//        boolean result = false;
//
//        String selection = TableAccountList.ACCOUNTID + "=?";
//        Cursor cursor = context.getContentResolver().query(this.getUri(),
//                null,
//                selection,
//                new String[] { Integer.toString(accountId) },
//                null
//        );
//        if (cursor == null) return false;
//
//        if (cursor.moveToFirst()) {
//            this.setValueFromCursor(cursor);
//
//            result = true;
//        }
//        cursor.close();
//
//        return result;
//    }

    /**
     * Retrieves all record ids which refer the given symbol.
     * @return array of ids of records which contain the symbol.
     */
    public int[] findIdsBySymbol(String symbol) {
        int[] result = null;

        Cursor cursor = context.getContentResolver().query(this.getUri(),
                new String[]{ Stock.STOCKID },
                Stock.SYMBOL + "=?", new String[]{symbol},
                null);

        if (cursor != null) {
            int records = cursor.getCount();
            result = new int[records];

            for (int i = 0; i < records; i++) {
                cursor.moveToNext();
                result[i] = cursor.getInt(cursor.getColumnIndex(Stock.STOCKID));
            }
            cursor.close();
        }

        return result;
    }

    public boolean insert(Stock stock) {
        return insert(stock.contentValues) > 0;
    }

//    public boolean update(int id, ContentValues values) {
//        boolean result = false;
//
//        int updateResult = context.getContentResolver().update(this.getUri(),
//                values,
//                Stock.STOCKID + "=?",
//                new String[]{Integer.toString(id)}
//        );
//
//        if (updateResult != 0) {
//            result = true;
//        } else {
//            Log.w(this.getClass().getSimpleName(), "Price update failed for stock id:" + id);
//        }
//
//        return  result;
//    }

    public boolean update(int id, Stock stock) {
        WhereStatementGenerator generator = new WhereStatementGenerator();
        String where = generator.getStatement(Stock.STOCKID, "=", id);

        return update(id, stock.contentValues, where);
    }

    /**
     * Update price for all the records with this symbol.
     * @param symbol Stock symbol
     * @param price Stock price
     */
    public void updateCurrentPrice(String symbol, Money price) {
        int[] ids = findIdsBySymbol(symbol);

        // recalculate value

        for (int id : ids) {
            //updatePrice(id, price);

            ContentValues oldValues = loadContentValues(id);
            double numberOfSharesD = oldValues.getAsDouble(Stock.NUMSHARES);
            BigDecimal numberOfShares = new BigDecimal(numberOfSharesD);
            BigDecimal value = numberOfShares.multiply(price.toBigDecimal());

//            ContentValues newValues = new ContentValues();
            Stock stock = new Stock();
            stock.contentValues.put(Stock.CURRENTPRICE, price.toDouble());
            stock.contentValues.put(Stock.VALUE, value.doubleValue());

            update(id, stock);
        }
    }

}
