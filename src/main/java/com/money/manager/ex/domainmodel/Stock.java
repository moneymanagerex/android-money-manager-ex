package com.money.manager.ex.domainmodel;

import android.database.Cursor;
import android.database.DatabaseUtils;

import com.money.manager.ex.utils.DateUtils;

import java.util.Calendar;
import java.util.Date;

import info.javaperformance.money.Money;
import info.javaperformance.money.MoneyFactory;

/**
 * Stock entity.
 * Created by Alen on 5/09/2015.
 */
public class Stock
    extends EntityBase {

    public static final String STOCKID = "STOCKID";
    public static final String HELDAT = "HELDAT";
    public static final String PURCHASEDATE = "PURCHASEDATE";
    public static final String STOCKNAME = "STOCKNAME";
    public static final String SYMBOL = "SYMBOL";
    public static final String NUMSHARES = "NUMSHARES";
    public static final String PURCHASEPRICE = "PURCHASEPRICE";
    public static final String NOTES = "NOTES";
    public static final String CURRENTPRICE = "CURRENTPRICE";
    public static final String VALUE = "VALUE";
    public static final String COMMISSION = "COMMISSION";

    public static Stock fromCursor(Cursor c) {
        Stock stock = new Stock();
        stock.loadFromCursor(c);
        return stock;
    }

    public static Stock getInstance() {
        Stock stock = new Stock();
        // Set to today.
        stock.setPurchaseDate(Calendar.getInstance().getTime());

        stock.setNumberOfShares(0.0);
        stock.setPurchasePrice(MoneyFactory.fromString("0"));
        stock.setCommission(MoneyFactory.fromString("0"));
        // should this be null?
        stock.setCurrentPrice(MoneyFactory.fromString("0"));

        return stock;
    }

    public Stock() {
        // default constructor.
    }

    @Override
    public void loadFromCursor(Cursor c) {
        super.loadFromCursor(c);

        // Reload all money values.
        DatabaseUtils.cursorDoubleToCursorValues(c, COMMISSION, this.contentValues);
        DatabaseUtils.cursorDoubleToCursorValues(c, CURRENTPRICE, this.contentValues);
        DatabaseUtils.cursorDoubleToCursorValues(c, NUMSHARES, this.contentValues);
        DatabaseUtils.cursorDoubleToCursorValues(c, PURCHASEPRICE, this.contentValues);
    }

    // properties

    public Integer getId() {
        if (contentValues.get(STOCKID) == null && mCursor != null) {
            DatabaseUtils.cursorIntToContentValues(mCursor, STOCKID, contentValues);
        }
        return contentValues.getAsInteger(STOCKID);
    }

//    public ContentValues getContentValues() {
//        return this.contentValues;
//    }

    public Money getCommission() {
        return getMoney(COMMISSION);
    }

    public void setCommission(Money value) {
        setMoney(COMMISSION, value);
    }

    public Money getCurrentPrice() {
        String currentPrice = contentValues.getAsString(CURRENTPRICE);
        return MoneyFactory.fromString(currentPrice);
    }

    public void setCurrentPrice(Money currentPrice) {
        contentValues.put(CURRENTPRICE, currentPrice.toString());
    }

    public int getHeldAt() {
        return getInt(HELDAT);
    }

    public void setHeldAt(int value) {
        setInt(HELDAT, value);
    }

    public String getNotes() {
        return getString(NOTES);
    }

    public void setNotes(String value) {
        setString(NOTES, value);
    }

    public Double getNumberOfShares() {
        return getDouble(NUMSHARES);
//        String numShares = contentValues.getAsString(NUMSHARES);
//        return MoneyFactory.fromString(numShares);
    }

    public void setNumberOfShares(Double value) {
        setDouble(NUMSHARES, value);
    }

    public Date getPurchaseDate() {
        if (contentValues.getAsString(PURCHASEDATE) == null && mCursor != null) {
            DatabaseUtils.cursorStringToContentValues(mCursor, PURCHASEDATE, contentValues);
//            if (contentValues.get(Stock.PURCHASEDATE) == null) {
//                Date date = Calendar.getInstance().getTime();
//                contentValues.put(Stock.PURCHASEDATE, DateUtils.getIsoStringDate(date));
//            }
        }
        String dateString = contentValues.getAsString(PURCHASEDATE);
        return DateUtils.getDateFromIsoString(dateString);
    }

    public void setPurchaseDate(Date value) {
        setDate(PURCHASEDATE, value);
    }

    public Money getPurchasePrice() {
        String purchasePrice = contentValues.getAsString(PURCHASEPRICE);
        return MoneyFactory.fromString(purchasePrice);
    }

    public void setPurchasePrice(Money value) {
        setMoney(PURCHASEPRICE, value);
    }

    public String getStockName() {
        return getString(STOCKNAME);
    }

    public void setName(String value) {
        setString(STOCKNAME, value);
    }

    public String getSymbol() {
        return getString(SYMBOL);
    }

    public void setSymbol(String value) {
        setString(SYMBOL, value);
    }

    public Money getValue() {
        // value = current price * num shares
        Money value = this.getCurrentPrice().multiply(this.getNumberOfShares());

        setMoney(VALUE, value);

        return value;
    }

}
