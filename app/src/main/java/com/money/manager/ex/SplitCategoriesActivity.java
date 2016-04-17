/*
 * Copyright (C) 2012-2016 The Android Money Manager Ex Project Team
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
package com.money.manager.ex;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

import com.melnykov.fab.FloatingActionButton;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.core.TransactionTypes;
import com.money.manager.ex.common.BaseFragmentActivity;
import com.money.manager.ex.database.ISplitTransaction;
import com.money.manager.ex.transactions.SplitItemFactory;
import com.money.manager.ex.transactions.SplitItemFragment;
import com.money.manager.ex.transactions.events.SplitItemRemovedEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import info.javaperformance.money.Money;
import info.javaperformance.money.MoneyFactory;

public class SplitCategoriesActivity
    extends BaseFragmentActivity {

    public static final String KEY_SPLIT_TRANSACTION = "SplitCategoriesActivity:ArraysSplitTransaction";
    public static final String KEY_SPLIT_TRANSACTION_DELETED = "SplitCategoriesActivity:ArraysSplitTransactionDeleted";
    public static final String KEY_TRANSACTION_TYPE = "SplitCategoriesActivity:TransactionType";
    public static final String KEY_DATASET_TYPE = "SplitCategoriesActivity:DatasetType";
    public static final String KEY_CURRENCY_ID = "SplitCategoriesActivity:CurrencyId";

    public static final String INTENT_RESULT_SPLIT_TRANSACTION = "SplitCategoriesActivity:ResultSplitTransaction";
    public static final String INTENT_RESULT_SPLIT_TRANSACTION_DELETED = "SplitCategoriesActivity:ResultSplitTransactionDeleted";

    private static int mIdTag = 0x8000;

    public TransactionTypes mParentTransactionType;

    /**
     * The name of the entity to create when adding split transactions.
     * Needed to distinguish between SplitCategory and SplitRecurringCategory.
     */
    private String entityTypeName = null;
    private ArrayList<ISplitTransaction> mSplitTransactions = null;
    private ArrayList<ISplitTransaction> mSplitDeleted = null;
    private FloatingActionButton mFloatingActionButton;
    private Integer currencyId = Constants.NOT_SET;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // load intent
        handleIntent();

        // load deleted item
        if (savedInstanceState != null && savedInstanceState.containsKey(KEY_SPLIT_TRANSACTION_DELETED)) {
            // todo: is this the correct variable?
            mSplitTransactions = Parcels.unwrap(savedInstanceState.getParcelable(KEY_SPLIT_TRANSACTION_DELETED));
        }

        // If this is a new split (no existing split categories), then create the first one.
        if(mSplitTransactions == null || mSplitTransactions.isEmpty()) {
            addSplitTransaction();
        }

        // set view
        setContentView(R.layout.splittransaction_activity);

        // toolbar
        if (getToolbar() != null) {
            setSupportActionBar();
            setToolbarStandardActions();
        }

        // 'Add' button

        if (mSplitTransactions != null) {
            for (int i = 0; i < mSplitTransactions.size(); i++) {
                addFragmentChild(mSplitTransactions.get(i));
            }
        }

        // show the floating "Add" button
        setUpFloatingButton();
    }

    @Override
    public boolean onActionCancelClick() {
        setResult(RESULT_CANCELED);
        finish();

        return true;
    }

    @Override
    public boolean onActionDoneClick() {
        ArrayList<ISplitTransaction> allSplitTransactions = getAllSplitCategories();
        Money total = MoneyFactory.fromString("0");

        // check data
        for (int i = 0; i < allSplitTransactions.size(); i++) {
            ISplitTransaction splitTransactions = allSplitTransactions.get(i);
            if (splitTransactions.getCategoryId() == -1 && splitTransactions.getSubcategoryId() == -1) {
                Core.alertDialog(SplitCategoriesActivity.this, R.string.error_category_not_selected);
                return false;
            }

            total = total.add(splitTransactions.getAmount());
        }

        // total amount must not be negative.
        if (total.toDouble() < 0) {
            Core.alertDialog(this, R.string.split_amount_negative);
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
        if (mSplitDeleted != null)
            outState.putParcelable(KEY_SPLIT_TRANSACTION_DELETED, Parcels.wrap(mSplitDeleted));
    }

    @Override
    protected void onStart() {
        super.onStart();

        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);

        super.onStop();
    }

    // Events

    @Subscribe
    public void onEvent(SplitItemRemovedEvent event) {
        onRemoveItem(event.entity);
    }

    /**
     * Returns all split categories created on the form.
     * @return List of Split Transactions that are displayed.
     */
    public ArrayList<ISplitTransaction> getAllSplitCategories() {
        ArrayList<ISplitTransaction> splitCategories = new ArrayList<>();
        List<Fragment> fragments = getSupportFragmentManager().getFragments();

        for(Fragment fragment:fragments) {
            SplitItemFragment splitFragment = (SplitItemFragment) fragment;
            if (splitFragment != null) {
                splitCategories.add(splitFragment.getSplitTransaction(mParentTransactionType));
            }
        }

        return splitCategories;
    }

    /**
     * Set the visibility of the floating button.
     * @param visible visibility
     */
    public void setFloatingActionButtonVisible(boolean visible) {
        if (mFloatingActionButton != null) {
            mFloatingActionButton.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    public void onFloatingActionButtonClickListener() {
        addSplitTransaction();
    }

    // Private

    private void addSplitTransaction() {
        // find which split transactions data set to instantiate.
        ISplitTransaction entity = SplitItemFactory.create(this.entityTypeName);
        addFragmentChild(entity);
    }

    private void addFragmentChild(ISplitTransaction entity) {
        int tagNumber = entity.getId() == null || entity.getId() == Constants.NOT_SET
            ? mIdTag++
            : entity.getId();
        String fragmentTag = SplitItemFragment.class.getSimpleName() + "_" + Integer.toString(tagNumber);

        SplitItemFragment fragment = (SplitItemFragment) getSupportFragmentManager().findFragmentByTag(fragmentTag);

        if (fragment == null) {
            fragment = SplitItemFragment.newInstance(entity, this.currencyId);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out);
            transaction.add(R.id.linearLayoutSplitTransaction, fragment, fragmentTag);
            transaction.commit();
        }
    }

    private void handleIntent() {
        Intent intent = getIntent();
        if (intent == null) return;

        this.entityTypeName = intent.getStringExtra(KEY_DATASET_TYPE);

        int transactionType = intent.getIntExtra(KEY_TRANSACTION_TYPE, 0);
        mParentTransactionType = TransactionTypes.values()[transactionType];

        this.currencyId = intent.getIntExtra(KEY_CURRENCY_ID, Constants.NOT_SET);

        mSplitTransactions = Parcels.unwrap(intent.getParcelableExtra(KEY_SPLIT_TRANSACTION));
        mSplitDeleted = Parcels.unwrap(intent.getParcelableExtra(KEY_SPLIT_TRANSACTION_DELETED));
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

    private void setUpFloatingButton() {
        mFloatingActionButton = (FloatingActionButton) findViewById(R.id.fab);
        if (mFloatingActionButton != null) {
            mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onFloatingActionButtonClickListener();
                }
            });
        }

        setFloatingActionButtonVisible(true);
    }
}
