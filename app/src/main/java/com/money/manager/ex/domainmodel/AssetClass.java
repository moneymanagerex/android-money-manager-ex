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

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;

import com.money.manager.ex.assetallocation.ItemType;

import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import info.javaperformance.money.Money;
import info.javaperformance.money.MoneyFactory;

/**
 * Asset Class
 * The serialization (parcel) is not fully working.
 */
@Parcel(analyze = {ContentValues.class})
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
        entity.setAllocation(MoneyFactory.fromString("0"));
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

//    @Override
//    public void writeToParcel(Parcel dest, int flags) {
//        super.writeToParcel(dest, flags);
//
//        // now just update calculated fields
//
//        dest.writeString(getValue().toString());
//        dest.writeString(getCurrentAllocation().toString());
//        dest.writeString(getCurrentValue().toString());
//        dest.writeString(getDifference().toString());
//        dest.writeString(getType().toString());
//    }

//    public void readFromParcel(Parcel source) {
//        this.contentValues = ContentValues.CREATOR.createFromParcel(source);
//
//        String value = source.readString();
//        setValue(MoneyFactory.fromString(value));
//
//        value = source.readString();
//        setCurrentAllocation(MoneyFactory.fromString(value));
//
//        setCurrentValue(MoneyFactory.fromString(source.readString()));
//        setDifference(MoneyFactory.fromString(source.readString()));
//        setTransactionType(ItemType.valueOf(source.readString()));
//    }

    public Integer getId() {
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

    public Money getAllocation() {
        return getMoney(ALLOCATION);
    }

    public void setAllocation(Money value) {
        setMoney(ALLOCATION, value);
    }

    public String getName() {
        return getString(NAME);
    }

    public void setName(String value) {
        setString(NAME, value);
    }

    public Integer getSortOrder() {
        Integer sortOrder = getInt(SORTORDER);
        if (sortOrder == null) sortOrder = 0;
        return sortOrder;
    }

    public void setSortOrder(int value) {
        setInt(SORTORDER, value);
    }

    public void addChild(AssetClass child) {
        this.getChildren().add(child);
    }

    public AssetClass getDirectChild(String name) {
        for (AssetClass child  : this.children) {
            if (child.getName().equalsIgnoreCase(name)) {
                return child;
            }
        }
        return null;
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

        // sort by stock symbol/name
        Collections.sort(this.stocks, new Comparator<Stock>() {
            @Override
            public int compare(Stock lhs, Stock rhs) {
                return lhs.getSymbol().compareTo(rhs.getSymbol());
            }
        });

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

    /**
     * Difference expressed as a percentage of set allocation.
     * i.e. 1000 EUR difference between current allocation (4%/2000) and set allocation (3%/1000)
     * is 50% of set allocation.
     * @return difference expressed as a percentage of set allocation
     */
    public Money getDiffAsPercentOfSet() {
        if (getDifference() == null) return MoneyFactory.fromDouble(-1);

        Money diffPercentage = this.getDifference()
            .multiply(100)
            .divide(this.getValue().toDouble(), 2);
        return diffPercentage;
    }

    public ItemType getType() {
        return this.type;
    }

    public void setType(ItemType value) {
        this.type = value;
    }
}
