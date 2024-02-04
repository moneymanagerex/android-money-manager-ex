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
package com.money.manager.ex.transactions;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.money.manager.ex.Constants;
import com.money.manager.ex.R;
import com.money.manager.ex.common.Calculator;
import com.money.manager.ex.common.CategoryListActivity;
import com.money.manager.ex.common.CommonSplitCategoryLogic;
import com.money.manager.ex.common.MmxBaseFragmentActivity;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.core.MenuHelper;
import com.money.manager.ex.core.TransactionTypes;
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
    private final int amountRequestOffset = 1000; // used to offset the request number for Amounts.
    @BindView(R.id.splitsRecyclerView)
    RecyclerView mRecyclerView;
    /**
     * The name of the entity to create when adding split transactions.
     * Needed to distinguish between SplitCategory and SplitRecurringCategory.
     */
    private String entityTypeName;
    private ArrayList<ISplitTransaction> mSplitDeleted;
    private SplitCategoriesAdapter mAdapter;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAdapter = new SplitCategoriesAdapter();

        handleIntent();

        // restore collections
        if (null != savedInstanceState) {
            mAdapter.splitTransactions = Parcels.unwrap(savedInstanceState.getParcelable(KEY_SPLIT_TRANSACTION));
            mSplitDeleted = Parcels.unwrap(savedInstanceState.getParcelable(KEY_SPLIT_TRANSACTION_DELETED));
        }

        // If this is a new split (no existing split categories), then create the first one.
        if (null == mAdapter.splitTransactions || mAdapter.splitTransactions.isEmpty()) {
            addSplitTransaction();
        }

        setContentView(R.layout.activity_split_categories);
        ButterKnife.bind(this);

        setDisplayHomeAsUpEnabled(true);

        initRecyclerView();
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        super.onCreateOptionsMenu(menu);

        new MenuHelper(this, menu).addSaveToolbarIcon();

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (MenuHelper.save == item.getItemId()) {
            return onActionDoneClick();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if ((Activity.RESULT_OK != resultCode) || (null == data)) return;

        if (REQUEST_PICK_CATEGORY == requestCode) {
            final int categoryId = data.getIntExtra(CategoryListActivity.INTENT_RESULT_CATEGID, Constants.NOT_SET);
            final int subcategoryId = data.getIntExtra(CategoryListActivity.INTENT_RESULT_SUBCATEGID, Constants.NOT_SET);
            final int location = data.getIntExtra(CategoryListActivity.KEY_REQUEST_ID, Constants.NOT_SET);

            final ISplitTransaction split = mAdapter.splitTransactions.get(location);
            split.setCategoryId(categoryId);

            mAdapter.notifyItemChanged(location);
        }

        if (amountRequestOffset <= requestCode) {
            final int id = requestCode - amountRequestOffset;
            final Money amount = Calculator.getAmountFromResult(data);
            onAmountEntered(id, amount);
        }
    }

    public boolean onActionDoneClick() {
        final List<ISplitTransaction> allSplitTransactions = mAdapter.splitTransactions;
        final Core core = new Core(this);
        Money total = MoneyFactory.fromString("0");

        // Validate Category.
        for (int i = 0; i < allSplitTransactions.size(); i++) {
            final ISplitTransaction splitTransaction = allSplitTransactions.get(i);
            if (Constants.NOT_SET == splitTransaction.getCategoryId()) {
                core.alert(R.string.error_category_not_selected);
                return false;
            }

            total = total.add(splitTransaction.getAmount());
        }

        // total amount must not be negative.
        if (0 > total.toDouble()) {
            core.alert(R.string.split_amount_negative);
            return false;
        }

        final Intent result = new Intent();
        result.putExtra(INTENT_RESULT_SPLIT_TRANSACTION, Parcels.wrap(allSplitTransactions));
        result.putExtra(INTENT_RESULT_SPLIT_TRANSACTION_DELETED, Parcels.wrap(mSplitDeleted));
        setResult(RESULT_OK, result);
        finish();

        return true;
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(KEY_SPLIT_TRANSACTION, Parcels.wrap(mAdapter.splitTransactions));

        if (null != mSplitDeleted) {
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
    public void onEvent(final SplitItemRemovedEvent event) {
        onRemoveItem(event.entity);
    }

    @Subscribe
    public void onEvent(final AmountEntryRequestedEvent event) {
        Calculator.forActivity(this)
                .currency(mAdapter.currencyId)
                .amount(event.amount)
                .show(event.requestId + amountRequestOffset);
    }

    @Subscribe
    public void onEvent(final CategoryRequestedEvent event) {
        showCategorySelector(event.requestId);
    }

    /*
        Private
     */

    private void addSplitTransaction() {
        final ISplitTransaction entity = SplitItemFactory.create(entityTypeName, mAdapter.transactionType);

        mAdapter.splitTransactions.add(entity);

        final int position = mAdapter.splitTransactions.size() - 1;
        mAdapter.notifyItemInserted(position);

        if (null != mRecyclerView) {
//            mRecyclerView.smoothScrollToPosition(position);
            mRecyclerView.scrollToPosition(position);
        }
    }

    private void handleIntent() {
        final Intent intent = getIntent();
        if (null == intent) return;

        entityTypeName = intent.getStringExtra(KEY_DATASET_TYPE);

        final int transactionType = intent.getIntExtra(KEY_TRANSACTION_TYPE, 0);
        mAdapter.transactionType = TransactionTypes.values()[transactionType];

        mAdapter.currencyId = intent.getIntExtra(KEY_CURRENCY_ID, Constants.NOT_SET);

        final List<ISplitTransaction> splits = Parcels.unwrap(intent.getParcelableExtra(KEY_SPLIT_TRANSACTION));
        if (null != splits) {
            mAdapter.splitTransactions = splits;
        }
        mSplitDeleted = Parcels.unwrap(intent.getParcelableExtra(KEY_SPLIT_TRANSACTION_DELETED));
    }

    private void initRecyclerView() {
//        mRecyclerView = (RecyclerView) findViewById(R.id.splitsRecyclerView);
        if (null == mRecyclerView) return;

        // adapter
        mRecyclerView.setAdapter(mAdapter);

        // layout manager - LinearLayoutManager
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // item animator
        // RecyclerView.ItemAnimator

        // optimizations
        mRecyclerView.setHasFixedSize(true);

        // Support for swipe-to-dismiss
        final ItemTouchHelper.Callback swipeCallback = new SplitItemTouchCallback(mAdapter);
        final ItemTouchHelper touchHelper = new ItemTouchHelper(swipeCallback);
        touchHelper.attachToRecyclerView(mRecyclerView);
    }

    private void onAmountEntered(final int requestId, final Money amount) {
        if (0 >= amount.toDouble()) {
            showInvalidAmountDialog();
            return;
        }
        // The amount is always positive, ensured by the validation above.

//        int position = Integer.parseInt(requestId);
        final int position = requestId;
        final ISplitTransaction split = mAdapter.splitTransactions.get(position);

        final Money adjustedAmount = CommonSplitCategoryLogic.getStorageAmount(mAdapter.transactionType, amount, split);
        split.setAmount(adjustedAmount);

        mAdapter.notifyItemChanged(position);
    }

    private void onRemoveItem(final ISplitTransaction splitTransaction) {
        if (null == splitTransaction) return;

        if (null == mSplitDeleted) {
            mSplitDeleted = new ArrayList<>();
        }

        // Add item to delete. Only if not a new, non-saved split item.
        if (null != splitTransaction.getId() && Constants.NOT_SET != splitTransaction.getId()) {
            // not new split transaction
            mSplitDeleted.add(splitTransaction);
        }
    }

    private void showCategorySelector(final int requestId) {
        final Intent intent = new Intent(this, CategoryListActivity.class);
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
