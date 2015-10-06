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
package com.money.manager.ex.servicelayer;

import android.content.Context;
import android.database.Cursor;

import com.innahema.collections.query.functions.Converter;
import com.innahema.collections.query.queriables.Queryable;
import com.money.manager.ex.database.WhereStatementGenerator;
import com.money.manager.ex.datalayer.AssetClassRepository;
import com.money.manager.ex.datalayer.AssetClassStockRepository;
import com.money.manager.ex.datalayer.StockRepository;
import com.money.manager.ex.domainmodel.AssetClass;
import com.money.manager.ex.domainmodel.AssetClassStock;
import com.money.manager.ex.domainmodel.Stock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import info.javaperformance.money.Money;
import info.javaperformance.money.MoneyFactory;

/**
 * Functions for the Asset Allocation
 */
public class AssetAllocationService {

    public AssetAllocationService(Context context) {
        this.context = context;
        this.repository = new AssetClassRepository(context);
    }

    private Context context;
    private AssetClassRepository repository;

    /**
     * Move the asset class up in the sort order.
     * Increase sort order for this item. Finds the next in order and decrease it's sort order.
     */
    public void moveClassUp(int id) {
        // todo: this is incomplete. Need to set the default value on creation and handle
        // deletions. Also pay attention if the order will be ascending or descending and adjust.

        List<AssetClass> bulk = new ArrayList();

        AssetClass up = repository.load(id);
        Integer currentPosition = up.getSortOrder();
        if (currentPosition == null) currentPosition = 0;
        int upPosition = currentPosition + 1;

        up.setSortOrder(upPosition);
        bulk.add(up);

        WhereStatementGenerator where = new WhereStatementGenerator();
        String filter = where.getStatement(AssetClass.SORTORDER, "=", upPosition);
        AssetClass down = repository.first(filter);
        if (down != null) {
            down.setSortOrder(currentPosition);
            bulk.add(down);
        }

        // save in transaction
        repository.bulkUpdate(bulk);
    }

    public List<AssetClass> loadAssetAllocation() {
        // http://docs.mongodb.org/manual/tutorial/model-tree-structures/

        // Step 1: Load all elements, ordered by ParentId.
        // Step 2: Fill a hash map with one pass through cursor.
        HashMap<Integer, AssetClass> map = loadMap();

        // Step 3: Assign children to their parents.
        List<AssetClass> result = assignChildren(map);

        // Step 4: Load stock links and stocks
        loadStocks(result);

        // Step 5: Calculate and store totals.
        calculateTotals(result);

        return result;
    }

    public Money getStockValue(List<Stock> stocks) {
        Money sum = MoneyFactory.fromString("0");
        for (Stock stock : stocks) {
            sum = sum.add(stock.getValue());
        }
        return sum;
    }

    // Private.

    private HashMap<Integer, AssetClass> loadMap() {
        HashMap<Integer, AssetClass> result = new HashMap<>();

        Cursor c = repository.openCursor(null, null, null, AssetClass.PARENTID);
        if (c == null) return null;

        while (c.moveToNext()) {
            AssetClass ac = AssetClass.from(c);
            result.put(ac.getId(), ac);
        }
        c.close();

        return result;

    }

    private List<AssetClass> assignChildren(HashMap<Integer, AssetClass> map) {
        List<AssetClass> allocation = new ArrayList<>();

        // iterate through items
        for (AssetClass ac : map.values()) {
            // add child to the parent
            Integer parentId = ac.getParentId();
            if (parentId != null) {
                map.get(parentId).addChild(ac);
            } else {
                // this is one of the root elements
                allocation.add(ac);
            }
        }
        return allocation;
    }

    private List<AssetClass> loadStocks(List<AssetClass> allocation) {
        // iterate
        for (AssetClass ac : allocation) {
            // if element has no children, load related stocks
            if (ac.getChildren().size() == 0) {
                loadStocks(ac);
            } else {
                loadStocks(ac.getChildren());
            }
        }
        return allocation;
    }

    private void loadStocks(AssetClass assetClass) {
        if (assetClass.getChildren().size() > 0) {
            for (AssetClass child : assetClass.getChildren()) {
                loadStocks(child);
            }
        } else {
            // load stocks
            AssetClassStockRepository classStockRepo = new AssetClassStockRepository(this.context);
            assetClass.setStockLinks(classStockRepo.loadForClass(assetClass.getId()));

            Integer[] ids = Queryable.from(assetClass.getStockLinks()).map(new Converter<AssetClassStock, Integer>() {
                @Override
                public Integer convert(AssetClassStock element) {
                    return element.getStockId();
                }
            }).toArray();

            StockRepository stockRepo = new StockRepository(this.context);
            assetClass.setStocks(stockRepo.load(ids));
        }
    }

    private Money calculateTotals(List<AssetClass> allocation) {
        // iterate recursively
        Money result = MoneyFactory.fromString("0");

        for (AssetClass ac : allocation) {
            if (ac.getChildren().size() > 0) {
                // Group. Calculate for children.
                result = calculateTotals(ac.getChildren());
                // Store the value
            } else {
                // Allocation. get value of all stocks.
                result = getStockValue(ac.getStocks());
            }

            ac.setCurrentValue(result);
        }

        return result;
    }
}
