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

    public ContentValues getContentValues() {
        return this.contentValues;
    }

    public BigDecimal getCommission() {
        return getBigDecimal(TableStock.COMMISSION);
    }

    public void setCommission(BigDecimal value) {
        setBigDecimal(TableStock.COMMISSION, value);
    }

    public BigDecimal getCurrentPrice() {
        String currentPrice = contentValues.getAsString(TableStock.CURRENTPRICE);
        return new BigDecimal(currentPrice);
    }

    public void setCurrentPrice(BigDecimal currentPrice) {
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

    public BigDecimal getNumberOfShares() {
        String numShares = contentValues.getAsString(TableStock.NUMSHARES);
        if (numShares == null && mCursor != null) {
            DatabaseUtils.cursorDoubleToCursorValues(mCursor, TableStock.NUMSHARES, contentValues);
        }
        return new BigDecimal(numShares);
    }

    public void setNumberOfShares(BigDecimal numberOfShares) {
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

    public BigDecimal getPurchasePrice() {
        String purchasePrice = contentValues.getAsString(TableStock.PURCHASEPRICE);
        if (StringUtils.isEmpty(purchasePrice) && mCursor != null) {
            DatabaseUtils.cursorDoubleToCursorValues(mCursor, TableStock.PURCHASEPRICE, contentValues);
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

        contentValues.put(TableStock.VALUE, value.toString());

        return value;
    }

}
