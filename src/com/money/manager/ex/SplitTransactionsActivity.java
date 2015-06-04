/*
 * Copyright (C) 2012-2015 The Android Money Manager Ex Project Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.money.manager.ex;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.melnykov.fab.FloatingActionButton;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.core.TransactionTypes;
import com.money.manager.ex.database.TableBudgetSplitTransactions;
import com.money.manager.ex.database.TableSplitTransactions;
import com.money.manager.ex.fragment.BaseFragmentActivity;
import com.money.manager.ex.fragment.IInputAmountDialogListener;
import com.money.manager.ex.fragment.SplitItemFragment;
import com.money.manager.ex.fragment.SplitItemFragment.SplitItemFragmentCallbacks;
import com.money.manager.ex.interfaces.ISplitTransactionsDataset;

import java.util.ArrayList;
import java.util.List;

public class SplitTransactionsActivity
        extends BaseFragmentActivity
        implements SplitItemFragmentCallbacks, IInputAmountDialogListener {

    public static final String KEY_SPLIT_TRANSACTION = "SplitTransactionsActivity:ArraysSplitTransaction";
    public static final String KEY_SPLIT_TRANSACTION_DELETED = "SplitTransactionsActivity:ArraysSplitTransactionDeleted";
    public static final String KEY_TRANSACTION_TYPE = "SplitTransactionsActivity:TransactionType";
    public static final String KEY_DATASET_TYPE = "SplitTransactionsActivity:DatasetType";
    public static final String INTENT_RESULT_SPLIT_TRANSACTION = "SplitTransactionsActivity:ResultSplitTransaction";
    public static final String INTENT_RESULT_SPLIT_TRANSACTION_DELETED = "SplitTransactionsActivity:ResultSplitTransactionDeleted";

    private static int mIdTag = 0x8000;

    public TransactionTypes mParentTransactionType;

    private SplitItemFragment mFragmentInputAmountClick;

    /**
     * The name of the entity to create when adding split transactions.
     * Needed to distinguish between TableSplitTransactions and TableBudgetSplitTransactions.
     */
    private String EntityTypeName = null;
    private ArrayList<ISplitTransactionsDataset> mSplitTransactions = null;
    private ArrayList<ISplitTransactionsDataset> mSplitDeleted = null;
    private FloatingActionButton mFloatingActionButton;

    private void addFragmentChild(ISplitTransactionsDataset object) {
        String fragmentName = SplitItemFragment.class.getSimpleName() + "_" +
                Integer.toString(object.getSplitTransId() == -1 ? mIdTag++ : object.getSplitTransId());
        SplitItemFragment fragment = (SplitItemFragment) getSupportFragmentManager().findFragmentByTag(fragmentName);
        if (fragment == null) {
            fragment = SplitItemFragment.newInstance(object);
            fragment.setOnSplitItemCallback(this);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            // animation
            // transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out);
            // Replace whatever is in the fragment_container view with this fragment,
            // and add the transaction to the back stack
            transaction.add(R.id.linearLayoutSplitTransaction, fragment, fragmentName);
            transaction.commit();
        }
    }

    /**
     * Returns all split categories created on the form.
     * @return List of Split Transactions that are displayed.
     */
    public ArrayList<ISplitTransactionsDataset> getAllSplitCategories() {
        ArrayList<ISplitTransactionsDataset> splitCategories = new ArrayList<>();
        List<Fragment> fragments = getSupportFragmentManager().getFragments();

        for(Fragment fragment:fragments) {
            SplitItemFragment splitFragment = (SplitItemFragment) fragment;
            if (splitFragment != null) {
                splitCategories.add(splitFragment.getSplitTransaction(mParentTransactionType));
            }
        }

        return splitCategories;
    }

    @Override
    public boolean onActionCancelClick() {
        setResult(RESULT_CANCELED);
        finish();

        return true;
    }

    @Override
    public boolean onActionDoneClick() {
        ArrayList<ISplitTransactionsDataset> allSplitTransactions = getAllSplitCategories();
        double total = 0;

        // check data
        for (int i = 0; i < allSplitTransactions.size(); i++) {
            ISplitTransactionsDataset splitTransactions = allSplitTransactions.get(i);
            if (splitTransactions.getCategId() == -1 && splitTransactions.getSubCategId() == -1) {
                Core.alertDialog(SplitTransactionsActivity.this, R.string.error_category_not_selected);
                return false;
            }

            total += splitTransactions.getSplitTransAmount();
        }

        // total amount must not be negative.
        if (total < 0) {
            Core.alertDialog(this, R.string.split_amount_negative);
            return false;
        }

        Intent result = new Intent();
        result.putParcelableArrayListExtra(INTENT_RESULT_SPLIT_TRANSACTION, allSplitTransactions);
        result.putParcelableArrayListExtra(INTENT_RESULT_SPLIT_TRANSACTION_DELETED, mSplitDeleted);
        setResult(RESULT_OK, result);
        finish();

        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // load intent
        Intent intent = getIntent();
        if (intent != null) {
            this.EntityTypeName = intent.getStringExtra(KEY_DATASET_TYPE);

            int transactionType = intent.getIntExtra(KEY_TRANSACTION_TYPE, 0);
            mParentTransactionType = TransactionTypes.values()[transactionType];

            mSplitTransactions = intent.getParcelableArrayListExtra(KEY_SPLIT_TRANSACTION);
            mSplitDeleted = intent.getParcelableArrayListExtra(KEY_SPLIT_TRANSACTION_DELETED);
        }

        // load deleted item
        if (savedInstanceState != null && savedInstanceState.containsKey(KEY_SPLIT_TRANSACTION_DELETED)) {
            // todo: is this the correct variable?
            mSplitTransactions = savedInstanceState.getParcelableArrayList(KEY_SPLIT_TRANSACTION_DELETED);
        }

        // If this is a new split (no existing split categories), then create the first one.
        if(mSplitTransactions == null || mSplitTransactions.isEmpty()) {
            addSplitTransaction();
        }

        // set view
        setContentView(R.layout.splittransaction_activity);

        // toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            setToolbarStandardAction(toolbar);
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
    public void onRemoveItem(ISplitTransactionsDataset object) {
        if (mSplitDeleted == null) {
            mSplitDeleted = new ArrayList<>();
        }
        // add item to delete
        if (object.getSplitTransId() != -1) {
            // not new split transaction
            mSplitDeleted.add(object);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mSplitDeleted != null)
            outState.putParcelableArrayList(KEY_SPLIT_TRANSACTION_DELETED, mSplitDeleted);
    }

    @Override
    public void onFinishedInputAmountDialog(int id, Double amount) {
        SplitItemFragment fragment = getFragmentInputAmountClick();
        if (fragment != null && fragment.isVisible() && fragment.isResumed()) {
            fragment.onFinishedInputAmountDialog(id, amount);
        }
    }

    public SplitItemFragment getFragmentInputAmountClick() {
        return mFragmentInputAmountClick;
    }

    public void setFragmentInputAmountClick(SplitItemFragment mFragmentInputAmountClick) {
        this.mFragmentInputAmountClick = mFragmentInputAmountClick;
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

    private void addSplitTransaction() {
        // find which split transactions dataset to instantiate.
        String recurringSplitName = TableBudgetSplitTransactions.class.getSimpleName();
        if (EntityTypeName != null && EntityTypeName.contains(recurringSplitName)) {
            addFragmentChild(new TableBudgetSplitTransactions());
        } else {
            addFragmentChild(new TableSplitTransactions());
        }
    }
}
