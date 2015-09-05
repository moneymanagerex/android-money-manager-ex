package com.money.manager.ex.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.support.design.widget.TabLayout;
import android.support.v4.database.DatabaseUtilsCompat;

import com.money.manager.ex.database.TableStock;
import com.money.manager.ex.utils.DateUtils;

import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

/**
 * Stock entity.
 * Created by Alen on 5/09/2015.
 */
public class Stock
    extends EntityBase {

    public static Stock getInstance() {
        Stock stock = new Stock();
        // Set to today.
        stock.setPurchaseDate(Calendar.getInstance().getTime());
        stock.setNumberOfShares(BigDecimal.ZERO);
        stock.setPurchasePrice(BigDecimal.ZERO);
        stock.setCommission(BigDecimal.ZERO);
        // should this be null?
        stock.setCurrentPrice(BigDecimal.ZERO);

        return stock;
    }

    public Stock() {
        // default constructor.
    }

//    public Stock(Cursor c) {
//        super(c);
//
//        // todo: see if we need to initialize the content values object.
//        mContentValues = new ContentValues();
//        DatabaseUtils.cursorRowToContentValues(c, mContentValues);
//    }

    // properties

    public Integer getId() {
        if (mContentValues.get(TableStock.STOCKID) == null && mCursor != null) {
            DatabaseUtils.cursorIntToContentValues(mCursor, TableStock.STOCKID, mContentValues);
        }
        return mContentValues.getAsInteger(TableStock.STOCKID);
    }

    public ContentValues getContentValues() {
        return this.mContentValues;
    }

    public BigDecimal getCommission() {
        return getBigDecimal(TableStock.COMMISSION);
    }

    public void setCommission(BigDecimal value) {
        setBigDecimal(TableStock.COMMISSION, value);
    }

    public BigDecimal getCurrentPrice() {
        String currentPrice = mContentValues.getAsString(TableStock.CURRENTPRICE);
        return new BigDecimal(currentPrice);
    }

    public void setCurrentPrice(BigDecimal currentPrice) {
        mContentValues.put(TableStock.CURRENTPRICE, currentPrice.toString());
    }

    public Date getPurchaseDate() {
        if (mContentValues.getAsString(TableStock.PURCHASEDATE) == null && mCursor != null) {
            DatabaseUtils.cursorStringToContentValues(mCursor, TableStock.PURCHASEDATE, mContentValues);
//            if (mContentValues.get(TableStock.PURCHASEDATE) == null) {
//                Date date = Calendar.getInstance().getTime();
//                mContentValues.put(TableStock.PURCHASEDATE, DateUtils.getSQLiteStringDate(date));
//            }
        }
        String dateString = mContentValues.getAsString(TableStock.PURCHASEDATE);
        return DateUtils.getDateFromIsoString(dateString);
    }

    public void setPurchaseDate(Date value) {
        setDate(TableStock.PURCHASEDATE, value);
    }

    public String getNotes() {
        return getString(TableStock.NOTES);
    }

    public void setNotes(String value) {
        setString(TableStock.NOTES, value);
    }

    public BigDecimal getNumberOfShares() {
        String numShares = mContentValues.getAsString(TableStock.NUMSHARES);
        if (numShares == null && mCursor != null) {
            DatabaseUtils.cursorDoubleToCursorValues(mCursor, TableStock.NUMSHARES, mContentValues);
        }
        return new BigDecimal(numShares);
    }

    public void setNumberOfShares(BigDecimal numberOfShares) {
        mContentValues.put(TableStock.NUMSHARES, numberOfShares.toString());
    }

    public BigDecimal getPurchasePrice() {
        String purchasePrice = mContentValues.getAsString(TableStock.PURCHASEPRICE);
        if (StringUtils.isEmpty(purchasePrice) && mCursor != null) {
            DatabaseUtils.cursorDoubleToCursorValues(mCursor, TableStock.PURCHASEPRICE, mContentValues);
        }
        return new BigDecimal(purchasePrice);
    }

    public void setPurchasePrice(BigDecimal value) {
        setBigDecimal(TableStock.PURCHASEPRICE, value);
    }

    public String getStockName() {
        return getString(TableStock.STOCKNAME);
    }

    public void setStockName(String value) {
        setString(TableStock.STOCKNAME, value);
    }

    public String getSymbol() {
        return getString(TableStock.SYMBOL);
    }

    public void setSymbol(String value) {
        setString(TableStock.SYMBOL, value);
    }

    public BigDecimal getValue() {
        // value = current price * num shares
        BigDecimal value = this.getNumberOfShares().multiply(this.getCurrentPrice());

        mContentValues.put(TableStock.VALUE, value.toString());

        return value;
    }

}
