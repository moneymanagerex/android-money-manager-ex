/*
 * Copyright (C) 2012-2015 The Android Money Manager Ex Project Team
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

import com.money.manager.ex.Constants;
import com.money.manager.ex.assetallocation.ItemType;
import com.money.manager.ex.servicelayer.AssetAllocationService;

import java.util.ArrayList;
import java.util.List;

import info.javaperformance.money.Money;
import info.javaperformance.money.MoneyFactory;

/**
 * Asset Class
 */
public class AssetClass
    extends EntityBase {

    public static final String ID = "ID";
    public static final String PARENTID = "PARENTID";
    public static final String NAME = "NAME";
    public static final String ALLOCATION = "ALLOCATION";
    public static final String SORTORDER = "SORTORDER";

    public static AssetClass from(Cursor c) {
        AssetClass entity = new AssetClass();
        entity.loadFromCursor(c);
        return entity;
    }

    public static AssetClass create(String name) {
        AssetClass entity = new AssetClass();
        entity.setName(name);
        entity.setAllocation(0.0);
        return entity;
    }

    public AssetClass() {
        super();
    }

    // temporary values
    private List<Stock> stocks;
    private List<AssetClassStock> stockLinks;
    private List<AssetClass> children;
    /**
     * The set value in base currency. Calculated from allocation & total portfolio value.
     */
    private Money value;
    private Money currentAllocation;
    private Money currentValue;
    private Money difference;
    private ItemType type;

    @Override
    public void loadFromCursor(Cursor c) {
        super.loadFromCursor(c);

        // Reload all money values.
        DatabaseUtils.cursorDoubleToCursorValues(c, ALLOCATION, this.contentValues);
    }

    public Integer getId() {
        return getInteger(ID);
    }

    public void setId(int value) {
        setInteger(ID, value);
    }

    public Integer getParentId() {
        return getInteger(PARENTID);
    }

    public void setParentId(int value) {
        setInteger(PARENTID, value);
    }

    public Double getAllocation() {
        return getDouble(ALLOCATION);
    }

    public void setAllocation(Double value) {
        setDouble(ALLOCATION, value);
    }

    public String getName() {
        return getString(NAME);
    }

    public void setName(String value) {
        setString(NAME, value);
    }

    public Integer getSortOrder() {
        return getInteger(SORTORDER);
    }

    public void setSortOrder(int value) {
        setInteger(SORTORDER, value);
    }

    public void addChild(AssetClass child) {
        this.getChildren().add(child);
    }

    public List<AssetClass> getChildren() {
        if (this.children == null) {
            this.children = new ArrayList<>();
        }
        return this.children;
    }

    public void setChildren(List<AssetClass> value) {
        this.children = value;
    }

    public List<AssetClassStock> getStockLinks() {
        if (this.stockLinks == null) {
            this.stockLinks = new ArrayList<>();
        }
        return this.stockLinks;
    }

    public void addStockLink(AssetClassStock link) {
        this.getStockLinks().add(link);
    }

    public void setStockLinks(List<AssetClassStock> links) {
        this.stockLinks = links;
    }

    public List<Stock> getStocks() {
        if (this.stocks == null) {
            this.stocks = new ArrayList<>();
        }
        return this.stocks;
    }

    public void setStocks(List<Stock> value) {
        this.stocks = value;
    }

    public void addStock(Stock stock) {
        this.getStocks().add(stock);
    }

    public Money getValue() {
        return this.value;
    }

    public void setValue(Money value) {
        this.value = value;
    }

    public Money getCurrentAllocation() {
        return this.currentAllocation;
    }

    public void setCurrentAllocation(Money value) {
        this.currentAllocation = value;
    }

    public Money getCurrentValue() {
        return this.currentValue;
    }

    public void setCurrentValue(Money value) {
        this.currentValue = value;
    }

    public Money getDifference() {
        return this.difference;
    }

    public void setDifference(Money value) {
        this.difference = value;
    }

    /*
                              Group      Asset Class
     allocation               sum        setAllocation <= the only set value!
     value                    sum        setAllocation * 100 / totalValue
     Current allocation       sum        value * 100 / totalValue
     current value            sum        value of all stocks (numStocks * price) in base currency!
     difference               sum        currentValue - setValue

     */

    /**
     * The magic happens here. Calculate all dependent variables.
     * @param totalPortfolioValue The total value of the portfolio, in base currency.
     */
    public void calculateStats(Money totalPortfolioValue) {
        Money zero = MoneyFactory.fromString("0");
        if (totalPortfolioValue.toDouble() == 0) {
            this.value = zero;
            this.currentAllocation = zero;
            this.currentValue = zero;
            this.difference = zero;
            return;
        }

        // see the service for the formulas.

        // Set Value
        double allocation = getAllocation();
//        double value = allocation * totalPortfolioValue.toDouble() / 100;
        this.value = totalPortfolioValue
            .multiply(allocation)
            .divide(100, Constants.DEFAULT_PRECISION);

        // current allocation. Use 2 decimals for now.
//        double totalPortfolioValueD = totalPortfolioValue.toDouble();
        this.currentAllocation = this.value
            .multiply(100)
            .divide(totalPortfolioValue.toDouble(), Constants.DEFAULT_PRECISION);
        // current value
        this.currentValue = AssetAllocationService.sumStockValues(this.stocks);
        // difference
        this.difference = this.currentValue.subtract(this.value);
    }

    public ItemType getType() {
        return this.type;
    }

    public void setType(ItemType value) {
        this.type = value;
    }
}
