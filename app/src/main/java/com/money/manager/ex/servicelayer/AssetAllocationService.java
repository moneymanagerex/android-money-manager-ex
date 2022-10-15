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
package com.money.manager.ex.servicelayer;

import android.content.Context;
import android.database.Cursor;

import com.money.manager.ex.Constants;
import com.money.manager.ex.R;
import com.money.manager.ex.account.AccountTypes;
import com.money.manager.ex.assetallocation.ItemType;
import com.money.manager.ex.log.ExceptionHandler;
import com.money.manager.ex.currency.CurrencyService;
import com.money.manager.ex.datalayer.AccountRepository;
import com.money.manager.ex.datalayer.AssetClassRepository;
import com.money.manager.ex.datalayer.AssetClassStockRepository;
import com.money.manager.ex.datalayer.StockRepository;
import com.money.manager.ex.domainmodel.Account;
import com.money.manager.ex.domainmodel.AssetClass;
import com.money.manager.ex.domainmodel.AssetClassStock;
import com.money.manager.ex.domainmodel.Stock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import info.javaperformance.money.Money;
import info.javaperformance.money.MoneyFactory;

/*
                          Group      Asset Class
 allocation               sum        setAllocation <= the only set value!
 value                    sum        totalValue * setAllocation / 100
 Current allocation       sum        value * 100 / totalValue
 current value            sum        value of all stocks (numStocks * price) in base currency!
 difference               sum        currentValue - setValue

 */

/**
 * Functions for the Asset Allocation
 */
public class AssetAllocationService
    extends ServiceBase {

    private static final String CashName = "Cash";

    public AssetAllocationService(Context context) {
        super(context);

        this.repository = new AssetClassRepository(context);
        mCurrencyService = new CurrencyService(context);
//        mAccountCurrencies = new HashMap<>();
    }

    public AssetClassRepository repository;

    private CurrencyService mCurrencyService;
    /**
     * Hashmap of Account Id / Currency Id pairs to speed up the calculation with caching.
     */
    private HashMap<Integer, Integer> mAccountCurrencies;

    public boolean deleteAllocation(int assetClassId) {
        ExceptionHandler handler = new ExceptionHandler(getContext(), this);
        AssetClassRepository repo = new AssetClassRepository(getContext());

        // todo: use transaction? (see bulkUpdate)

        // Delete all child elements.
        List<Integer> childIds = getAllChildrenIds(assetClassId);
        repo.deleteAll(childIds);

        // delete any stock links
        AssetClassStockRepository stockRepo = new AssetClassStockRepository(getContext());
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

    public List<Integer> getAllChildrenIds(int assetClassId) {
        List<Integer> ids = new ArrayList<>();

        AssetClassRepository repo = new AssetClassRepository(getContext());
        List<Integer> childIds = repo.loadAllChildrenIds(assetClassId);

        ids.addAll(childIds);

        // iterate recursively and get all children's children ids.
        for (int childId : childIds) {
            ids.addAll(getAllChildrenIds(childId));
        }

        return ids;
    }

    /**
     * Main entry point when no data is loaded yet..
     * @return Asset Allocation, see the method with the cursor parameter.
     */
    public AssetClass loadAssetAllocation() {
        // http://docs.mongodb.org/manual/tutorial/model-tree-structures/

        // Step 1: Load all elements, ordered by ParentId.
        Cursor c = loadData();
        return loadAssetAllocationFrom(c);
    }

    /**
     * Main entry point.
     * @param c Cursor with asset classes, from which to load the Asset Allocation.
     * @return Full Asset Allocation with all the calculated fields.
     */
    private AssetClass loadAssetAllocationFrom(Cursor c) {
        if (c == null) return null;

        // Main asset allocation object.
        AssetClass root = AssetClass.create("Asset Allocation");
        root.setType(ItemType.Group);

        // Fill a hash map with one pass through cursor. Used for easier fetching of asset classes.
        HashMap<Integer, AssetClass> map = loadMap(c);
        c.close();
        // Assign children to their parents. Create a hierarchical list.
        List<AssetClass> list = assignChildren(map);

        // Load stock links and stocks to asset allocations.
        loadStocks(list);

        root.setChildren(list);

        // create automatic Cash asset class by taking cash amounts from investment accounts.
        addCash(root);

        sortChildren(root);

        // Calculate and store current Value amounts.
        Money totalValue = calculateCurrentValue(list);
        root.setCurrentValue(totalValue);

        // Calculate all the derived values.
        calculateStats(root, totalValue);

        return root;
    }

    /**
     * Loads asset class name, given the id.
     * @param id Id of the asset class.
     * @return String name of the asset class.
     */
    public String loadName(int id) {
        if (id == Constants.NOT_SET) return "";

        AssetClassRepository repo = new AssetClassRepository(getContext());
        Cursor c = repo.openCursor(
            new String[]{AssetClass.NAME},
            AssetClass.ID + "=?",
            new String[]{Integer.toString(id)}
        );
        if (c == null) return null;

        c.moveToNext();
        AssetClass ac = AssetClass.from(c);
        c.close();

        return ac.getName();
    }

    /**
     * Move the asset class down in the sort order.
     * Increase sort order for this item. Finds the next in order and decrease it's sort order.
     */
    public void moveClassDown(int id) {
        // todo: this is incomplete. Need to set the default value on creation and e
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
//        // update in transaction
//        repository.bulkUpdate(bulk);

        // for now, just increase the sort order on the selected item
        repository.update(up);
    }

    /**
     * Increase the ranking value, effectively moving the item down in the list.
     */
    public void moveClassUp(int id) {
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

        AssetClassStockRepository repo = new AssetClassStockRepository(getContext());
        boolean success = repo.insert(link);

        if (!success) {
            ExceptionHandler handler = new ExceptionHandler(getContext(), this);
            handler.showMessage(getContext().getString(R.string.error));
            //Timber.e(getContext().getString(R.string.error));
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

    /**
     * Add Cash as a separate asset class that uses all the cash amounts from
     * the investment accounts.
     * @param assetAllocation Main asset allocation object.
     */
    private void addCash(AssetClass assetAllocation) {
        //String cashLocalizedName = getContext().getString(R.string.cash);
        AssetClass cash = assetAllocation.getDirectChild(CashName);

        if (cash == null) {
            cash = createCashAssetClass();
            assetAllocation.addChild(cash);
        }
        cash.setType(ItemType.Cash);

        Money currentValue = calculateCashCurrentValue();
        cash.setCurrentValue(currentValue);
    }

    private Money calculateCurrentAllocation(Money currentValue, Money portfolioValue) {
        Money currentAllocation = currentValue
            .multiply(100)
            .divide(portfolioValue.toDouble(), Constants.DEFAULT_PRECISION);
        return currentAllocation;
    }

    /**
     * The magic happens here. Calculate all dependent variables.
     * @param portfolioValue The total value of the portfolio, in base currency.
     */
    private void calculateStatsFor(AssetClass item, Money portfolioValue) {
        Money zero = MoneyFactory.fromDouble(0);
        if (portfolioValue.toDouble() == 0) {
            item.setValue(zero);
            item.setCurrentAllocation(zero);
            item.setCurrentValue(zero);
            item.setDifference(zero);
            return;
        }

        // Set Value
        Money allocation = item.getAllocation();
        // setValue = allocation * portfolioValue / 100;
        Money setValue = calculateSetValue(portfolioValue, allocation);
        item.setValue(setValue);

        // Current value
        Money currentValue = sumStockValues(item.getStocks());
        item.setCurrentValue(currentValue);

        // Current allocation.
        Money currentAllocation = calculateCurrentAllocation(currentValue, portfolioValue);
        item.setCurrentAllocation(currentAllocation);

        // difference
        Money difference = currentValue.subtract(setValue);
        item.setDifference(difference);
    }

    private Money calculateSetValue(Money portfolioValue, Money allocation) {
        Money value = portfolioValue
            .multiply(allocation.toDouble())
            .divide(100, Constants.DEFAULT_PRECISION);
        return value;
    }

    private AssetClass createCashAssetClass() {
        //String cashLocalizedName = getContext().getString(R.string.cash);

        // Create a new asset class for Cash.
        AssetClass cash = AssetClass.create(CashName);

        AssetClassRepository repo = new AssetClassRepository(getContext());
        repo.insert(cash);

        return cash;
    }

    private Money calculateCashCurrentValue() {
        // get all investment accounts, their currencies and cash balances.
        AccountService accountService = new AccountService(getContext());
        List<String> investmentAccounts = new ArrayList<>();
        investmentAccounts.add(AccountTypes.INVESTMENT.toString());
        int destinationCurrency = mCurrencyService.getBaseCurrencyId();

        List<Account> accounts = accountService.loadAccounts(false, false, investmentAccounts);

        Money sum = MoneyFactory.fromDouble(0);

        // Get the balances in base currency.
        for (Account account : accounts) {
            int sourceCurrency = account.getCurrencyId();
            Money amountInBase = mCurrencyService.doCurrencyExchange(destinationCurrency,
                    account.getInitialBalance(), sourceCurrency);
            sum = sum.add(amountInBase);
        }

        return sum;
    }

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
        List<AssetClass> children = new ArrayList<>();

        // Iterate through all the allocations.
        for (AssetClass ac : map.values()) {
            Integer parentId = ac.getParentId();
            if (parentId != null && parentId != Constants.NOT_SET) {
                // add child elements to their parents based on the Id field.
                AssetClass parent = map.get(parentId);

                // delete any orphans
                if (parent == null) {
                    deleteAllocation(ac.getId());
                    continue;
                }

                parent.setType(ItemType.Group);
                parent.addChild(ac);
            } else {
                // this is one of the root elements
                children.add(ac);
            }
        }

        return children;
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
            AssetClassStockRepository linkRepo = new AssetClassStockRepository(getContext());
            List<AssetClassStock> links = linkRepo.loadForClass(assetClass.getId());
            assetClass.setStockLinks(links);

            if (assetClass.getStockLinks().size() == 0) return;

            int size = assetClass.getStockLinks().size();
            String[] symbols = new String[size];
            for (int i = 0; i < size; i++) {
                AssetClassStock link = assetClass.getStockLinks().get(i);
                symbols[i] = link.getStockSymbol();
            }

            StockRepository stockRepo = new StockRepository(getContext());
            List<Stock> stocks = stockRepo.loadForSymbols(symbols);
            assetClass.setStocks(stocks);
        }
    }

    private Money calculateCurrentValue(List<AssetClass> allocations) {
        Money result = MoneyFactory.fromDouble(0);

        for (AssetClass ac : allocations) {
            Money itemValue;

            ItemType type = ac.getType();
            switch (type) {
                case Group:
                    // Group. Calculate for children.
                    itemValue = calculateCurrentValue(ac.getChildren());
                    break;

                case Allocation:
                    // Allocation. get value of all stocks.
                    itemValue = sumStockValues(ac.getStocks());
                    break;

                case Cash:
                    itemValue = ac.getCurrentValue();
                    break;

                default:
                    ExceptionHandler handler = new ExceptionHandler(getContext());
                    handler.showMessage("encountered an item with no type set!");
                    itemValue = MoneyFactory.fromDouble(0);
                    break;
            }

            ac.setCurrentValue(itemValue);
            result = result.add(itemValue);
        }

        return result;
    }

    /**
     * Calculate all dependent statistics for allocation records.
     * @param allocation Asset Class/Allocation record
     * @param portfolioValue Total value of the portfolio. Used to calculate the current allocation.
     */
    private void calculateStats(AssetClass allocation, Money portfolioValue) {
        List<AssetClass> children = allocation.getChildren();
        Money setAllocation, currentAllocation, setValue, currentValue, difference;

        ItemType type = allocation.getType();
        switch (type) {
            case Group:
                // Group. Calculate stats for children *and* get the summaries here.
                for (AssetClass child : children) {
                    // find edge node
                    calculateStats(child, portfolioValue);
                }

                // Allocation
                setAllocation = getAllocationSum(children);
                allocation.setAllocation(setAllocation);
                // Value
                setValue = getValueSum(children);
                allocation.setValue(setValue);
                // current allocation
                currentAllocation = getCurrentAllocationSum(children);
                allocation.setCurrentAllocation(currentAllocation);
                // current value
                currentValue = getCurrentValueSum(children);
                allocation.setCurrentValue(currentValue);
                // difference
                difference = getDifferenceSum(children);
                allocation.setDifference(difference);
                break;

            case Allocation:
                // Allocation. Calculate all stats.
                calculateStatsFor(allocation, portfolioValue);
                break;

            case Cash:
                // Allocation. Set manually.
                // Set Value
                setValue = calculateSetValue(portfolioValue, allocation.getAllocation());
                allocation.setValue(setValue);
                // Current Allocation
                currentAllocation = calculateCurrentAllocation(allocation.getCurrentValue(), portfolioValue);
                allocation.setCurrentAllocation(currentAllocation);
                // Current Value. Calculated when Cash created/loaded.
                // Difference
                difference = allocation.getCurrentValue().subtract(setValue);
                allocation.setDifference(difference);
                break;
        }
    }

    private Money getAllocationSum(List<AssetClass> group) {
        List<Money> allocations = new ArrayList<>();
        for (AssetClass item : group) {
            allocations.add(item.getAllocation());
        }

        Money sum = MoneyFactory.fromString("0");
        for (Money allocation : allocations) {
            sum = sum.add(allocation);
        }
        return sum;
    }

    private Money getValueSum(List<AssetClass> group) {
        Money sum = MoneyFactory.fromDouble(0);
        for (AssetClass item : group) {
            sum = sum.add(item.getValue());
        }
        return sum;
    }

    private Money getCurrentAllocationSum(List<AssetClass> group) {
        Money sum = MoneyFactory.fromDouble(0);
        for (AssetClass item : group) {
            sum = sum.add(item.getCurrentAllocation());
        }
        return sum;
    }

    private Money getCurrentValueSum(List<AssetClass> group) {
        Money sum = MoneyFactory.fromDouble(0);
        for (AssetClass item : group) {
            sum = sum.add(item.getCurrentValue());
        }
        return sum;
    }

    private Money getDifferenceSum(List<AssetClass> group) {
        Money sum = MoneyFactory.fromDouble(0);
        for (AssetClass item : group) {
            sum = sum.add(item.getDifference());
        }
        return sum;
    }

    private void sortChildren(AssetClass allocation) {
        // sort immediate children
        Collections.sort(allocation.getChildren(), new Comparator<AssetClass>() {
            @Override
            public int compare(AssetClass lhs, AssetClass rhs) {
                return lhs.getSortOrder().compareTo(rhs.getSortOrder());
            }
        });

        // sort grandchildren recursively
        for (AssetClass child : allocation.getChildren()) {
            sortChildren(child);
        }
    }

    private Money sumStockValues(List<Stock> stocks) {
        Money sum = MoneyFactory.fromDouble(0);
        int baseCurrencyId = mCurrencyService.getBaseCurrencyId();

        for (Stock stock : stocks) {
            // convert the stock value to the base currency.
            int accountId = stock.getHeldAt();
            int currencyId = getAccountCurrencyId(accountId);
            Money value = mCurrencyService.doCurrencyExchange(baseCurrencyId, stock.getValue(), currencyId);

            sum = sum.add(value);
        }
        return sum;
    }

    private Integer getAccountCurrencyId(int accountId) {
        if (mAccountCurrencies == null) {
            mAccountCurrencies = new HashMap<>();
        }

        if (mAccountCurrencies.containsKey(accountId)) {
            return mAccountCurrencies.get(accountId);
        }

        // else load

        AccountRepository repo = new AccountRepository(getContext());
        Integer currencyId = repo.loadCurrencyIdFor(accountId);
        mAccountCurrencies.put(accountId, currencyId);

        return currencyId;
    }
}
