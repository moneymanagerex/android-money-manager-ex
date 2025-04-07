/*
 * Copyright (C) 2012-2018 The Android Money Manager Ex Project Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.money.manager.ex.domainmodel;

import android.database.Cursor;
import android.database.DatabaseUtils;

import com.money.manager.ex.utils.MmxDate;

import org.parceler.Parcel;

import java.util.Date;
import java.util.Objects;

import info.javaperformance.money.Money;
import info.javaperformance.money.MoneyFactory;

/**
 * Stock entity.
 */
@Parcel
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

    public static Stock from(Cursor c) {
        Stock stock = new Stock();
        stock.loadFromCursor(c);
        return stock;
    }

    public static Stock create() {
        Stock stock = new Stock();
        // Set to today.
        stock.setPurchaseDate(new MmxDate().toDate());

        stock.setName("");
        stock.setHeldAt(0);

        stock.setNumberOfShares(0.0);
        stock.setPurchasePrice(MoneyFactory.fromString("0"));
        stock.setCommission(MoneyFactory.fromString("0"));
        stock.setCurrentPrice(MoneyFactory.fromString("0"));

        return stock;
    }

    public static Stock create(String date, String name, String purchasePrice, String currentPrice) {
        Stock stock = Stock.create();

        stock.setPurchaseDate(date);
        stock.setName(name);
        stock.setPurchasePrice(MoneyFactory.fromString(purchasePrice));
        stock.setCurrentPrice(MoneyFactory.fromString(currentPrice));

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
    @Override
    public String getPrimaryKeyColumn() {
        return STOCKID;  // This returns the column name specific to Report
    }

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

    public long getHeldAt() {
        return getLong(HELDAT);
    }

    public void setHeldAt(long value) {
        setLong(HELDAT, value);
    }

    public String getNotes() {
        return getString(NOTES);
    }

    public void setNotes(String value) {
        setString(NOTES, value);
    }

    public Double getNumberOfShares() {
        return getDouble(NUMSHARES);
    }

    public void setNumberOfShares(Double value) {
        setDouble(NUMSHARES, value);
    }

    public Date getPurchaseDate() {
        String dateString = getString(PURCHASEDATE);
        return new MmxDate(dateString).toDate();
    }

    public void setPurchaseDate(Date value) {
        setDate(PURCHASEDATE, value);
    }

    public Money getPurchasePrice() {
        String purchasePrice = contentValues.getAsString(PURCHASEPRICE);
        return MoneyFactory.fromString(purchasePrice);
    }

    public void setPurchaseDate(String value) {
        setString(PURCHASEDATE, value);
    }

    public void setPurchasePrice(Money value) {
        setMoney(PURCHASEPRICE, value);
    }

    public String getName() {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Stock stock = (Stock) o;
        return Objects.equals(getId(), stock.getId()) &&
                Objects.equals(getSymbol(), stock.getSymbol()) &&
                Objects.equals(getNumberOfShares(), stock.getNumberOfShares()) &&
                Objects.equals(getPurchasePrice(), stock.getPurchasePrice()) &&
                Objects.equals(getCurrentPrice(), stock.getCurrentPrice());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getSymbol(), getNumberOfShares(), getPurchasePrice(), getCurrentPrice());
    }
}
