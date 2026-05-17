/*
 * Copyright (C) 2026 The Android Money Manager Ex Project Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.money.manager.ex.investment;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.money.manager.ex.Constants;
import com.money.manager.ex.R;
import com.money.manager.ex.common.AllDataListFragment;
import com.money.manager.ex.common.MmxBaseFragmentActivity;
import com.money.manager.ex.database.QueryAllData;
import com.money.manager.ex.datalayer.AccountRepository;
import com.money.manager.ex.datalayer.StockRepository;
import com.money.manager.ex.domainmodel.Account;
import com.money.manager.ex.domainmodel.Stock;

public class StockTransactionListActivity extends MmxBaseFragmentActivity {

    public static final String EXTRA_STOCK_ID = "StockTransactionListActivity:StockId";
    public static final String EXTRA_ACCOUNT_ID = "StockTransactionListActivity:AccountId";

    private static final String FRAGMENT_TAG = StockTransactionListActivity.class.getSimpleName();

    private AllDataListFragment listFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        long stockId = intent != null ? intent.getLongExtra(EXTRA_STOCK_ID, Constants.NOT_SET) : Constants.NOT_SET;
        if (stockId == Constants.NOT_SET) {
            finish();
            return;
        }

        StockRepository stockRepository = new StockRepository(this);
        Stock stock = stockRepository.load(stockId);
        if (stock == null) {
            finish();
            return;
        }

        long accountId = intent.getLongExtra(EXTRA_ACCOUNT_ID,
                stock.getHeldAt() != Constants.NOT_SET ? stock.getHeldAt() : Constants.NOT_SET);

        setTitle(getStockScreenTitle(stock));

        listFragment = AllDataListFragment.newInstance(accountId, false);
        listFragment.showTotalsFooter();

        Bundle args = new Bundle();
        if (listFragment.getArguments() != null) {
            args.putAll(listFragment.getArguments());
        }
        args.putString(AllDataListFragment.KEY_ARGUMENTS_WHERE,
                QueryAllData.STOCKID + "=" + stockId);
        args.putString(AllDataListFragment.KEY_ARGUMENTS_SORT,
                QueryAllData.TOACCOUNTID + ", " + QueryAllData.Date + " DESC, " +
                        QueryAllData.TransactionType + ", " + QueryAllData.ID + " DESC");
        listFragment.setArguments(args);

        inizializeCommon(listFragment, FRAGMENT_TAG);
        getSupportFragmentManager().executePendingTransactions();

        if (accountId != Constants.NOT_SET) {
            Account account = new AccountRepository(this).load(accountId);
            if (account != null && !TextUtils.isEmpty(account.getName()) && getSupportActionBar() != null) {
                getSupportActionBar().setSubtitle(account.getName());
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (listFragment == null) {
            listFragment = (AllDataListFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG);
        }
        if (listFragment != null && listFragment.isAdded()) {
            listFragment.loadData();
        }
    }

    private String getStockScreenTitle(Stock stock) {
        String name = stock.getName();
        String symbol = stock.getSymbol();

        if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(symbol) && !name.equals(symbol)) {
            return getString(R.string.transactions) + " - " + name + " (" + symbol + ")";
        }

        if (!TextUtils.isEmpty(name)) {
            return getString(R.string.transactions) + " - " + name;
        }

        if (!TextUtils.isEmpty(symbol)) {
            return getString(R.string.transactions) + " - " + symbol;
        }

        return getString(R.string.transactions);
    }
}