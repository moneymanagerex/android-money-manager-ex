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
package com.money.manager.ex.transactions;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;

import com.afollestad.materialdialogs.MaterialDialog;
import com.money.manager.ex.Constants;
import com.money.manager.ex.R;
import com.money.manager.ex.common.Calculator;
import com.money.manager.ex.common.CategoryListActivity;
import com.money.manager.ex.common.CommonSplitCategoryLogic;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.core.MenuHelper;
import com.money.manager.ex.core.TransactionTypes;
import com.money.manager.ex.common.MmxBaseFragmentActivity;
import com.money.manager.ex.database.ISplitTransaction;
import com.money.manager.ex.transactions.events.AmountEntryRequestedEvent;
import com.money.manager.ex.transactions.events.CategoryRequestedEvent;
import com.money.manager.ex.transactions.events.SplitItemRemovedEvent;

import org.greenrobot.eventbus.Subscribe;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import info.javaperformance.money.Money;
import info.javaperformance.money.MoneyFactory;

public class SplitCategoriesActivity
    extends MmxBaseFragmentActivity {

    public static final String KEY_SPLIT_TRANSACTION = "SplitCategoriesActivity:ArraysSplitTransaction";
    public static final String KEY_SPLIT_TRANSACTION_DELETED = "SplitCategoriesActivity:ArraysSplitTransactionDeleted";
    public static final String KEY_TRANSACTION_TYPE = "SplitCategoriesActivity:TransactionType";
    public static final String KEY_DATASET_TYPE = "SplitCategoriesActivity:DatasetType";
    public static final String KEY_CURRENCY_ID = "SplitCategoriesActivity:CurrencyId";

    public static final String INTENT_RESULT_SPLIT_TRANSACTION = "SplitCategoriesActivity:ResultSplitTransaction";
    public static final String INTENT_RESULT_SPLIT_TRANSACTION_DELETED = "SplitCategoriesActivity:ResultSplitTransactionDeleted";

    private static final int REQUEST_PICK_CATEGORY = 1;

    /**
     * The name of the entity to create when adding split transactions.
     * Needed to distinguish between SplitCategory and SplitRecurringCategory.
     */
    private String entityTypeName = null;
    private ArrayList<ISplitTransaction> mSplitDeleted = null;
    private SplitCategoriesAdapter mAdapter;
    @BindView(R.id.splitsRecyclerView) RecyclerView mRecyclerView;
    private int amountRequestOffset = 1000; // used to offset the request number for Amounts.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAdapter = new SplitCategoriesAdapter();

        handleIntent();

        // restore collections
        if (savedInstanceState != null) {
            mAdapter.splitTransactions = Parcels.unwrap(savedInstanceState.getParcelable(KEY_SPLIT_TRANSACTION));
            mSplitDeleted = Parcels.unwrap(savedInstanceState.getParcelable(KEY_SPLIT_TRANSACTION_DELETED));
        }

        // If this is a new split (no existing split categories), then create the first one.
        if(mAdapter.splitTransactions == null || mAdapter.splitTransactions.isEmpty()) {
            addSplitTransaction();
        }

        setContentView(R.layout.activity_split_categories);
        ButterKnife.bind(this);

        setDisplayHomeAsUpEnabled(true);

        initRecyclerView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        new MenuHelper(this, menu).addSaveToolbarIcon();

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MenuHelper.save:
                return onActionDoneClick();

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if ((resultCode != Activity.RESULT_OK) || (data == null)) return;

        switch (requestCode) {
            case REQUEST_PICK_CATEGORY:
                int categoryId = data.getIntExtra(CategoryListActivity.INTENT_RESULT_CATEGID, Constants.NOT_SET);
                int subcategoryId = data.getIntExtra(CategoryListActivity.INTENT_RESULT_SUBCATEGID, Constants.NOT_SET);
                int location = data.getIntExtra(CategoryListActivity.KEY_REQUEST_ID, Constants.NOT_SET);

                ISplitTransaction split = mAdapter.splitTransactions.get(location);
                split.setCategoryId(categoryId);
                split.setSubcategoryId(subcategoryId);

                mAdapter.notifyItemChanged(location);
                break;
        }

        if (requestCode >= amountRequestOffset) {
            int id = requestCode - amountRequestOffset;
            Money amount = Calculator.getAmountFromResult(data);
            onAmountEntered(id, amount);
        }
    }

    public boolean onActionDoneClick() {
        List<ISplitTransaction> allSplitTransactions = mAdapter.splitTransactions;
        Core core = new Core(this);
        Money total = MoneyFactory.fromString("0");

        // Validate Category.
        for (int i = 0; i < allSplitTransactions.size(); i++) {
            ISplitTransaction splitTransaction = allSplitTransactions.get(i);
            if (splitTransaction.getCategoryId() == Constants.NOT_SET) {
                core.alert(R.string.error_category_not_selected);
                return false;
            }

            total = total.add(splitTransaction.getAmount());
        }

        // total amount must not be negative.
        if (total.toDouble() < 0) {
            core.alert(R.string.split_amount_negative);
            return false;
        }

        Intent result = new Intent();
        result.putExtra(INTENT_RESULT_SPLIT_TRANSACTION, Parcels.wrap(allSplitTransactions));
        result.putExtra(INTENT_RESULT_SPLIT_TRANSACTION_DELETED, Parcels.wrap(mSplitDeleted));
        setResult(RESULT_OK, result);
        finish();

        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(KEY_SPLIT_TRANSACTION, Parcels.wrap(mAdapter.splitTransactions));

        if (mSplitDeleted != null) {
            outState.putParcelable(KEY_SPLIT_TRANSACTION_DELETED, Parcels.wrap(mSplitDeleted));
        }
    }

    @OnClick(R.id.fab)
    protected void onFabClick() {
        addSplitTransaction();
    }

    /*
        Events
     */

    @Subscribe
    public void onEvent(SplitItemRemovedEvent event) {
        onRemoveItem(event.entity);
    }

    @Subscribe
    public void onEvent(AmountEntryRequestedEvent event) {
        Calculator.forActivity(this)
                .currency(mAdapter.currencyId)
                .amount(event.amount)
                .show(event.requestId + amountRequestOffset);
    }

    @Subscribe
    public void onEvent(CategoryRequestedEvent event) {
        showCategorySelector(event.requestId);
    }

    /*
        Private
     */

    private void addSplitTransaction() {
        ISplitTransaction entity = SplitItemFactory.create(this.entityTypeName, mAdapter.transactionType);

        mAdapter.splitTransactions.add(entity);

        int position = mAdapter.splitTransactions.size() - 1;
        mAdapter.notifyItemInserted(position);

        if (mRecyclerView != null) {
//            mRecyclerView.smoothScrollToPosition(position);
            mRecyclerView.scrollToPosition(position);
        }
    }

    private void handleIntent() {
        Intent intent = getIntent();
        if (intent == null) return;

        this.entityTypeName = intent.getStringExtra(KEY_DATASET_TYPE);

        int transactionType = intent.getIntExtra(KEY_TRANSACTION_TYPE, 0);
        mAdapter.transactionType = TransactionTypes.values()[transactionType];

        mAdapter.currencyId = intent.getIntExtra(KEY_CURRENCY_ID, Constants.NOT_SET);

        List<ISplitTransaction> splits = Parcels.unwrap(intent.getParcelableExtra(KEY_SPLIT_TRANSACTION));
        if (splits != null) {
            mAdapter.splitTransactions = splits;
        }
        mSplitDeleted = Parcels.unwrap(intent.getParcelableExtra(KEY_SPLIT_TRANSACTION_DELETED));
    }

    private void initRecyclerView() {
//        mRecyclerView = (RecyclerView) findViewById(R.id.splitsRecyclerView);
        if (mRecyclerView == null) return;

        // adapter
        mRecyclerView.setAdapter(mAdapter);

        // layout manager - LinearLayoutManager
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // item animator
        // RecyclerView.ItemAnimator

        // optimizations
        mRecyclerView.setHasFixedSize(true);

        // Support for swipe-to-dismiss
        ItemTouchHelper.Callback swipeCallback = new SplitItemTouchCallback(mAdapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(swipeCallback);
        touchHelper.attachToRecyclerView(mRecyclerView);
    }

    private void onAmountEntered(int requestId, Money amount) {
        if (amount.toDouble() <= 0) {
            showInvalidAmountDialog();
            return;
        }
        // The amount is always positive, ensured by the validation above.

//        int position = Integer.parseInt(requestId);
        int position = requestId;
        ISplitTransaction split = mAdapter.splitTransactions.get(position);

        Money adjustedAmount = CommonSplitCategoryLogic.getStorageAmount(mAdapter.transactionType, amount, split);
        split.setAmount(adjustedAmount);

        mAdapter.notifyItemChanged(position);
    }

    private void onRemoveItem(ISplitTransaction splitTransaction) {
        if (splitTransaction == null) return;

        if (mSplitDeleted == null) {
            mSplitDeleted = new ArrayList<>();
        }

        // Add item to delete. Only if not a new, non-saved split item.
        if (splitTransaction.getId() != null && splitTransaction.getId() != Constants.NOT_SET) {
            // not new split transaction
            mSplitDeleted.add(splitTransaction);
        }
    }

    private void showCategorySelector(int requestId) {
        Intent intent = new Intent(this, CategoryListActivity.class);
        intent.setAction(Intent.ACTION_PICK);

        // add id of the item that requested the category.
        intent.putExtra(CategoryListActivity.KEY_REQUEST_ID, requestId);

        startActivityForResult(intent, REQUEST_PICK_CATEGORY);
    }

    private void showInvalidAmountDialog() {
        new MaterialDialog.Builder(this)
            .title(R.string.error)
            .content(R.string.error_amount_must_be_positive)
            .positiveText(android.R.string.ok)
            .show();
    }
}
