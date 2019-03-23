/*
 * Copyright (C) 2012-2019 The Android Money Manager Ex Project Team
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

import org.parceler.Parcel;

/**
 * Mapping between Asset Classes and Stocks.
 */
@Parcel
public class AssetClassStock
    extends EntityBase {

    public static final String ID = "ID";
    public static final String ASSETCLASSID = "ASSETCLASSID";
    public static final String STOCKSYMBOL = "STOCKSYMBOL";

    public static AssetClassStock create(int assetClassId, String stockSymbol) {
        AssetClassStock entity = new AssetClassStock();

        entity.setAssetClassId(assetClassId);
        entity.setStockSymbol(stockSymbol);

        return entity;
    }

    public AssetClassStock() {
        super();
    }

    public void setId(int value) {
        setInt(ID, value);
    }

    public Integer getAssetClassId() {
        return getInt(ASSETCLASSID);
    }

    public void setAssetClassId(int value) {
        setInt(ASSETCLASSID, value);
    }

    public String getStockSymbol() {
        return getString(STOCKSYMBOL);
    }

    public void setStockSymbol(String value) {
        setString(STOCKSYMBOL, value);
    }
}
