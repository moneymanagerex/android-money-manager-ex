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

    public static AssetClass create() {
        AssetClass entity = new AssetClass();
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
    private Money currentAllocation;
    private Money currentValue;

    @Override
    public void loadFromCursor(Cursor c) {
        super.loadFromCursor(c);

        // Reload all money values.
        DatabaseUtils.cursorDoubleToCursorValues(c, ALLOCATION, this.contentValues);
    }

    public int getId() {
        return getInt(ID);
    }

    public void setId(int value) {
        setInt(ID, value);
    }

    public Integer getParentId() {
        return getInt(PARENTID);
    }

    public void setParentId(int value) {
        setInt(PARENTID, value);
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
        return getInt(SORTORDER);
    }

    public void setSortOrder(int value) {
        setInt(SORTORDER, value);
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

}
