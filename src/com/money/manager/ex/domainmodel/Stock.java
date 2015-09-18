package com.money.manager.ex.domainmodel;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;

import com.money.manager.ex.database.TableStock;
import com.money.manager.ex.utils.DateUtils;

import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
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

    public static Stock getInstance() {
        Stock stock = new Stock();
        // Set to today.
        stock.setPurchaseDate(Calendar.getInstance().getTime());

        stock.setNumberOfShares(MoneyFactory.fromString("0"));
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
        DatabaseUtils.cursorDoubleToCursorValues(c, TableStock.COMMISSION, this.contentValues);
        DatabaseUtils.cursorDoubleToCursorValues(c, TableStock.CURRENTPRICE, this.contentValues);
        DatabaseUtils.cursorDoubleToCursorValues(c, TableStock.NUMSHARES, this.contentValues);
        DatabaseUtils.cursorDoubleToCursorValues(c, TableStock.PURCHASEPRICE, this.contentValues);
    }

    // properties

    public Integer getId() {
        if (contentValues.get(TableStock.STOCKID) == null && mCursor != null) {
            DatabaseUtils.cursorIntToContentValues(mCursor, TableStock.STOCKID, contentValues);
        }
        return contentValues.getAsInteger(TableStock.STOCKID);
    }

//    public ContentValues getContentValues() {
//        return this.contentValues;
//    }

    public Money getCommission() {
        return getMoney(TableStock.COMMISSION);
    }

    public void setCommission(Money value) {
        setMoney(TableStock.COMMISSION, value);
    }

    public Money getCurrentPrice() {
        String currentPrice = contentValues.getAsString(TableStock.CURRENTPRICE);
        return MoneyFactory.fromString(currentPrice);
    }

    public void setCurrentPrice(Money currentPrice) {
        contentValues.put(TableStock.CURRENTPRICE, currentPrice.toString());
    }

    public int getHeldAt() {
        return getInt(TableStock.HELDAT);
    }

    public void setHeldAt(int value) {
        setInt(TableStock.HELDAT, value);
    }

    public String getNotes() {
        return getString(TableStock.NOTES);
    }

    public void setNotes(String value) {
        setString(TableStock.NOTES, value);
    }

    public Money getNumberOfShares() {
        String numShares = contentValues.getAsString(TableStock.NUMSHARES);
        return MoneyFactory.fromString(numShares);
    }

    public void setNumberOfShares(Money numberOfShares) {
        contentValues.put(TableStock.NUMSHARES, numberOfShares.toString());
    }

    public Date getPurchaseDate() {
        if (contentValues.getAsString(TableStock.PURCHASEDATE) == null && mCursor != null) {
            DatabaseUtils.cursorStringToContentValues(mCursor, TableStock.PURCHASEDATE, contentValues);
//            if (contentValues.get(TableStock.PURCHASEDATE) == null) {
//                Date date = Calendar.getInstance().getTime();
//                contentValues.put(TableStock.PURCHASEDATE, DateUtils.getIsoStringDate(date));
//            }
        }
        String dateString = contentValues.getAsString(TableStock.PURCHASEDATE);
        return DateUtils.getDateFromIsoString(dateString);
    }

    public void setPurchaseDate(Date value) {
        setDate(TableStock.PURCHASEDATE, value);
    }

    public Money getPurchasePrice() {
        String purchasePrice = contentValues.getAsString(TableStock.PURCHASEPRICE);
        return MoneyFactory.fromString(purchasePrice);
    }

    public void setPurchasePrice(Money value) {
        setMoney(TableStock.PURCHASEPRICE, value);
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

    public Money getValue() {
        // value = current price * num shares
        Money value = this.getNumberOfShares().multiply(this.getCurrentPrice().toDouble());

        contentValues.put(TableStock.VALUE, value.toString());

        return value;
    }

}
