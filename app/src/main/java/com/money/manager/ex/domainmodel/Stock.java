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

import com.money.manager.ex.datalayer.StockFields;
import com.money.manager.ex.utils.MmxDate;

import org.parceler.Parcel;

import java.util.Date;

import info.javaperformance.money.Money;
import info.javaperformance.money.MoneyFactory;

/**
 * Stock entity.
 */
@Parcel
public class Stock
        extends EntityBase {

    public Stock() {
        // default constructor.
    }

    public static Stock from(final Cursor c) {
        final Stock stock = new Stock();
        stock.loadFromCursor(c);
        return stock;
    }

    public static Stock create() {
        final Stock stock = new Stock();
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

    public static Stock create(final String date, final String name, final String purchasePrice, final String currentPrice) {
        final Stock stock = create();

        stock.setPurchaseDate(date);
        stock.setName(name);
        stock.setPurchasePrice(MoneyFactory.fromString(purchasePrice));
        stock.setCurrentPrice(MoneyFactory.fromString(currentPrice));

        return stock;
    }

    @Override
    public void loadFromCursor(final Cursor c) {
        super.loadFromCursor(c);

        // Reload all money values.
        DatabaseUtils.cursorDoubleToCursorValues(c, StockFields.COMMISSION, contentValues);
        DatabaseUtils.cursorDoubleToCursorValues(c, StockFields.CURRENTPRICE, contentValues);
        DatabaseUtils.cursorDoubleToCursorValues(c, StockFields.NUMSHARES, contentValues);
        DatabaseUtils.cursorDoubleToCursorValues(c, StockFields.PURCHASEPRICE, contentValues);
    }

    // properties

    public Integer getId() {
        return getInt(StockFields.STOCKID);
    }

    public Money getCommission() {
        return getMoney(StockFields.COMMISSION);
    }

    public void setCommission(final Money value) {
        setMoney(StockFields.COMMISSION, value);
    }

    public Money getCurrentPrice() {
        final String currentPrice = contentValues.getAsString(StockFields.CURRENTPRICE);
        return MoneyFactory.fromString(currentPrice);
    }

    public void setCurrentPrice(final Money currentPrice) {
        contentValues.put(StockFields.CURRENTPRICE, currentPrice.toString());
    }

    public int getHeldAt() {
        return getInt(StockFields.HELDAT);
    }

    public void setHeldAt(final int value) {
        setInt(StockFields.HELDAT, value);
    }

    public String getNotes() {
        return getString(StockFields.NOTES);
    }

    public void setNotes(final String value) {
        setString(StockFields.NOTES, value);
    }

    public Double getNumberOfShares() {
        return getDouble(StockFields.NUMSHARES);
    }

    public void setNumberOfShares(final Double value) {
        setDouble(StockFields.NUMSHARES, value);
    }

    public Date getPurchaseDate() {
        final String dateString = getString(StockFields.PURCHASEDATE);
        return new MmxDate(dateString).toDate();
    }

    public void setPurchaseDate(final Date value) {
        setDate(StockFields.PURCHASEDATE, value);
    }

    public void setPurchaseDate(final String value) {
        setString(StockFields.PURCHASEDATE, value);
    }

    public Money getPurchasePrice() {
        final String purchasePrice = contentValues.getAsString(StockFields.PURCHASEPRICE);
        return MoneyFactory.fromString(purchasePrice);
    }

    public void setPurchasePrice(final Money value) {
        setMoney(StockFields.PURCHASEPRICE, value);
    }

    public String getName() {
        return getString(StockFields.STOCKNAME);
    }

    public void setName(final String value) {
        setString(StockFields.STOCKNAME, value);
    }

    public String getSymbol() {
        return getString(StockFields.SYMBOL);
    }

    public void setSymbol(final String value) {
        setString(StockFields.SYMBOL, value);
    }

    public Money getValue() {
        // value = current price * num shares
        final Money value = getCurrentPrice().multiply(getNumberOfShares());

        setMoney(StockFields.VALUE, value);

        return value;
    }
}
