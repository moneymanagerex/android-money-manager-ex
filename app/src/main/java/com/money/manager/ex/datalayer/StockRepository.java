/*
 * Copyright (C) 2012-2025 The Android Money Manager Ex Project Team
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

package com.money.manager.ex.datalayer;

import android.content.Context;

import com.money.manager.ex.database.DatasetType;
import com.money.manager.ex.domainmodel.Stock;

import java.util.List;

import javax.inject.Inject;

import info.javaperformance.money.Money;

/**
 * Data repository for Stock entities.
 */
public class StockRepository
    extends RepositoryBase<Stock> {

    private static final String TABLE_NAME = "stock_v1";
    private static final String ID_COLUMN = Stock.STOCKID;
    private static final String NAME_COLUMN = Stock.SYMBOL;

    @Inject
    public StockRepository(Context context) {
        super(context, TABLE_NAME, DatasetType.TABLE, "stock", ID_COLUMN, NAME_COLUMN);
    }

    @Override
    protected Stock createEntity() {
        return new Stock();
    }

    @Override
    public String[] getAllColumns() {
        return new String[] {
                ID_COLUMN + " AS _id", Stock.STOCKID, Stock.STOCKNAME
                , Stock.HELDAT, Stock.SYMBOL, Stock.PURCHASEDATE, Stock.PURCHASEPRICE
                , Stock.NUMSHARES, Stock.NOTES, Stock.CURRENTPRICE
                , Stock.VALUE, Stock.COMMISSION
        };
    }

    /**
     * Update price for all the records with this symbol.
     * @param symbol Stock symbol
     * @param price Stock price
     */
    public void updateCurrentPrice(String symbol, Money price) {

        // recalculate value
        for (Stock stock : loadBySymbol(symbol)) {
            if (stock == null) continue; // this should not happen, but see #2295 -anr-1071-stockrepository
            stock.setCurrentPrice(price);
            // recalculate & assign the value
            stock.getValue();

            save(stock);
        }
    }

    // custom func
    public List<Stock> loadByAccount(long accountId) {
        return query(new Select(getAllColumns()).where(Stock.HELDAT + " = ?", accountId));
    }

    public List<Stock> loadBySymbol(String symbol) {
        return query(new Select(getAllColumns()).where(Stock.SYMBOL + " = ?", symbol));
    }
}
