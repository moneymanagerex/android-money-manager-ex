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
import com.money.manager.ex.Constants;
import com.money.manager.ex.R;
import com.money.manager.ex.assetallocation.ItemType;
import com.money.manager.ex.core.ExceptionHandler;
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

    public static Money sumStockValues(List<Stock> stocks) {
        Money sum = MoneyFactory.fromString("0");
        for (Stock stock : stocks) {
            sum = sum.add(stock.getValue());
        }
        return sum;
    }

    public AssetAllocationService(Context context) {
        this.context = context;
        this.repository = new AssetClassRepository(context);
    }

    public AssetClassRepository repository;

    private Context context;

    public boolean deleteAllocation(int assetClassId) {
        ExceptionHandler handler = new ExceptionHandler(this.context, this);
        AssetClassRepository repo = new AssetClassRepository(this.context);
//        AssetClass assetClass = repo.load(assetClassId);

        // todo: use transaction? (see bulkUpdate)

        // todo: if there are child elements? block for now. Later offer to delete them as well.

        // delete any stock links
        AssetClassStockRepository stockRepo = new AssetClassStockRepository(this.context);
        boolean linksDeleted = stockRepo.deleteAllForAssetClass(assetClassId);
        if (!linksDeleted) {
            handler.showMessage("Error deleting stock links.");
            return false;
        }

        // delete allocation record
        boolean assetClassDeleted = repo.delete(assetClassId);
        if (!assetClassDeleted) {
            handler.showMessage("Error deleting asset class.");
            return false;
        }

        return true;
    }

    public AssetClass loadAssetAllocation() {
        // http://docs.mongodb.org/manual/tutorial/model-tree-structures/

        // Step 1: Load all elements, ordered by ParentId.
        Cursor c = loadData();
        return loadAssetAllocation(c);
    }

    public AssetClass loadAssetAllocation(Cursor c) {
        // Step 2: Fill a hash map with one pass through cursor.
        HashMap<Integer, AssetClass> map = loadMap(c);
        c.close();

        // Step 3: Assign children to their parents.
        List<AssetClass> list = assignChildren(map);

        // Step 4: Load stock links and stocks
        loadStocks(list);

        // Step 5: Calculate and store totals.
        AssetClass main = AssetClass.create("Asset Allocation");
        main.setType(ItemType.Group);
        main.setChildren(list);
        Money totalValue = calculateCurrentValue(list);
        main.setCurrentValue(totalValue);

        // Step 6: Calculate current allocation and difference.
//        calculateCurrentAllocation(main, totalValue);
        // difference
        calculateStats(main, totalValue);

        return main;
    }

    /**
     * Loads asset class name, given the id.
     * @param id Id of the asset class.
     * @return String name of the asset class.
     */
    public String loadName(int id) {
        if (id == Constants.NOT_SET) return "";

        AssetClassRepository repo = new AssetClassRepository(this.context);
        Cursor c = repo.openCursor(
            new String[] { AssetClass.NAME },
            AssetClass.ID + "=?",
            new String[] { Integer.toString(id)}
        );
        if (c == null) return null;

        c.moveToNext();
        AssetClass ac = AssetClass.from(c);
        c.close();

        return ac.getName();
    }

    /**
     * Move the asset class up in the sort order.
     * Increase sort order for this item. Finds the next in order and decrease it's sort order.
     */
    public void moveClassUp(int id) {
        // todo: this is incomplete. Need to set the default value on creation and handle
        // deletions. Also pay attention if the order will be ascending or descending and adjust.

//        List<AssetClass> bulk = new ArrayList();

        AssetClass up = repository.load(id);
        Integer currentPosition = up.getSortOrder();
        if (currentPosition == null) currentPosition = 0;
        int upPosition = currentPosition + 1;

        up.setSortOrder(upPosition);
//        bulk.add(up);

//        WhereStatementGenerator where = new WhereStatementGenerator();
//        String filter = where.getStatement(AssetClass.SORTORDER, "=", upPosition);
//        AssetClass down = repository.first(filter);
//        if (down != null) {
//            down.setSortOrder(currentPosition);
//            bulk.add(down);
//        }
//
//        // save in transaction
//        repository.bulkUpdate(bulk);

        // for now, just increase the sort order on the selected item
        repository.update(up);
    }

    public void moveClassDown(int id) {
        AssetClass assetClass = repository.load(id);
        Integer currentPosition = assetClass.getSortOrder();
        if (currentPosition == null) currentPosition = 0;
        int nextPosition = currentPosition - 1;
        if (nextPosition < 0) return;

        assetClass.setSortOrder(nextPosition);

        repository.update(assetClass);
    }

    public boolean assignStockToAssetClass(String stockSymbol, Integer assetClassId) {
        AssetClassStock link = AssetClassStock.create(assetClassId, stockSymbol);

        AssetClassStockRepository repo = new AssetClassStockRepository(context);
        boolean success = repo.insert(link);

        if (!success) {
            ExceptionHandler handler = new ExceptionHandler(context, this);
            handler.showMessage(context.getString(R.string.error));
        }
        return success;
    }

    /**
     * Find asset allocation by id.
     * @param childId Id of the class to find.
     * @return asset class with the required id.
     */
    public AssetClass findChild(int childId, AssetClass tree) {
        AssetClass result = null;

        if (childId == Constants.NOT_SET) {
            return tree;
        }

        // iterate through all elements

        Integer id = tree.getId();
        if (id != null && id == childId) return tree;

        for (AssetClass child : tree.getChildren()) {
            result = findChild(childId, child);
            if (result != null) break;
        }
        return result;
    }

    // Private.

    private Cursor loadData() {
        Cursor c = repository.openCursor(null, null, null, AssetClass.PARENTID);
        return c;
    }

    private HashMap<Integer, AssetClass> loadMap(Cursor c) {
        HashMap<Integer, AssetClass> result = new HashMap<>();

        if (c == null) return result;

        while (c.moveToNext()) {
            AssetClass ac = AssetClass.from(c);
            result.put(ac.getId(), ac);
        }
//        c.close();

        return result;
    }

    private List<AssetClass> assignChildren(HashMap<Integer, AssetClass> map) {
        List<AssetClass> allocation = new ArrayList<>();

        // Iterate through all the allocations.
        for (AssetClass ac : map.values()) {
            Integer parentId = ac.getParentId();
            if (parentId != null && parentId != Constants.NOT_SET) {
                // add child elements to their parents based on the Id field.
                AssetClass parent = map.get(parentId);
                parent.setType(ItemType.Group);
                parent.addChild(ac);
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
            // Group. Load values for child elements.
            for (AssetClass child : assetClass.getChildren()) {
                loadStocks(child);
            }
        } else {
            // No child elements. This is the actual allocation. Load value from linked stocks.

            assetClass.setType(ItemType.Allocation);

            // load stock links
            AssetClassStockRepository linkRepo = new AssetClassStockRepository(this.context);
            List<AssetClassStock> links = linkRepo.loadForClass(assetClass.getId());
            assetClass.setStockLinks(links);

            if (assetClass.getStockLinks().size() == 0) return;

            String[] symbols = Queryable.from(assetClass.getStockLinks())
                .map(new Converter<AssetClassStock, String>() {
                    @Override
                    public String convert(AssetClassStock element) {
                        return element.getStockSymbol();
                    }
                }).toArray();

            StockRepository stockRepo = new StockRepository(this.context);
            List<Stock> stocks = stockRepo.loadForSymbols(symbols);
            assetClass.setStocks(stocks);
        }
    }

    private Money calculateCurrentValue(List<AssetClass> allocation) {
        // iterate recursively
        Money result = MoneyFactory.fromString("0");

        for (AssetClass ac : allocation) {
            Money itemValue;

            if (ac.getChildren().size() > 0) {
                // Group. Calculate for children.

                itemValue = calculateCurrentValue(ac.getChildren());
            } else {
                // Allocation. get value of all stocks.

                itemValue = sumStockValues(ac.getStocks());
            }

            ac.setCurrentValue(itemValue);
            result = result.add(itemValue);
        }

        return result;
    }

//    private Money calculateCurrentAllocation(AssetClass allocation, Money totalValue) {
//        Money result;
//
//        if (allocation.getChildren().size() > 0) {
//            // Group.
//            result = MoneyFactory.fromString("0");
//
//            // iterate children
//            for (AssetClass child : allocation.getChildren()) {
//                // find edge node
//                result = result.add(calculateCurrentAllocation(child, totalValue));
//            }
//        } else {
//            // Allocation. Calculate current allocation as percentage of total value.
//            // current allocation = current Value * 100 / total value
//            Double totalValueD = totalValue.toDouble();
//            Money currentAllocation = allocation.getCurrentValue()
//                .multiply(100)
//                .divide(totalValueD, 2);
//            // Using 2 decimal places for allocation values.
//
//            result = currentAllocation;
//        }
//
//        allocation.setCurrentAllocation(result);
//
//        return result;
//    }

    /**
     * Calculate all dependent statistics for allocation records.
     * @param allocation Asset Class/Allocation record
     * @param totalValue Total value of the portfolio. Used to calculate the current allocation.
     */
    private void calculateStats(AssetClass allocation, Money totalValue) {
        List<AssetClass> children = allocation.getChildren();

        // Group or allocation?
        if (children.size() > 0) {
            // Group. Calculate stats for children *and* get the summaries here.
            for (AssetClass child : children) {
                // find edge node
                calculateStats(child, totalValue);
            }

            // Allocation
            Money setAllocation = getAllocationSum(children);
            allocation.setAllocation(setAllocation);
            // Value
            Money setValue = getValueSum(children);
            allocation.setValue(setValue);
            // current allocation
            Money currentAllocation = getCurrentAllocationSum(children);
            allocation.setCurrentAllocation(currentAllocation);
            // current value
            Money currentValue = getCurrentValueSum(children);
            allocation.setCurrentValue(currentValue);
            // difference
            Money difference = getDifferenceSum(children);
            allocation.setDifference(difference);
        } else {
            // Allocation. Calculate all stats on the spot.
            allocation.calculateStats(totalValue);
        }

    }

    private Money getAllocationSum(List<AssetClass> group) {
        List<Money> allocations = Queryable.from(group)
            .map(new Converter<AssetClass, Money>() {
                @Override
                public Money convert(AssetClass element) {
                    return element.getAllocation();
                }
            }).toList();

        Money sum = MoneyFactory.fromString("0");
        for (Money allocation : allocations) {
            sum = sum.add(allocation);
        }
        return sum;
    }

    private Money getValueSum(List<AssetClass> group) {
        Converter<AssetClass, Money> converter = new Converter<AssetClass, Money>() {
            @Override
            public Money convert(AssetClass element) {
                return element.getValue();
            }
        };
        return getMoneySum(group, converter);
    }

    private Money getCurrentAllocationSum(List<AssetClass> group) {
        Converter<AssetClass, Money> converter = new Converter<AssetClass, Money>() {
            @Override
            public Money convert(AssetClass element) {
                return element.getCurrentAllocation();
            }
        };
        return getMoneySum(group, converter);
    }

    private Money getCurrentValueSum(List<AssetClass> group) {
        Converter<AssetClass, Money> converter = new Converter<AssetClass, Money>() {
            @Override
            public Money convert(AssetClass element) {
                return element.getCurrentValue();
            }
        };
        return getMoneySum(group, converter);
    }

    private Money getDifferenceSum(List<AssetClass> group) {
        Converter<AssetClass, Money> converter = new Converter<AssetClass, Money>() {
            @Override
            public Money convert(AssetClass element) {
                return element.getDifference();
            }
        };
        return getMoneySum(group, converter);
    }

    private Money getMoneySum(List<AssetClass> group, Converter<AssetClass, Money> converter) {
        List<Money> values = Queryable.from(group)
            .map(converter).toList();

        Money sum = MoneyFactory.fromString("0");
        for (Money value : values) {
            sum = sum.add(value);
        }
        return sum;
    }

    private double sumDouble(List<AssetClass> group, Converter<AssetClass, Double> converter) {
        List<Double> values = Queryable.from(group)
            .map(converter).toList();

        double sum = 0;
        for (double value : values) {
            sum += value;
        }
        return sum;
    }
}
