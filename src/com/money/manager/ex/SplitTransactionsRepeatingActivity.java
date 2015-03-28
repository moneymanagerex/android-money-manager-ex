/*
 * Copyright (C) 2012-2014 Money Manager Ex project
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
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.money.manager.ex.core.Core;
import com.money.manager.ex.database.TableBudgetSplitTransactions;
import com.money.manager.ex.fragment.BaseFragmentActivity;
import com.money.manager.ex.fragment.InputAmountDialog.InputAmountDialogListener;
import com.money.manager.ex.fragment.SplitItemFragment;
import com.money.manager.ex.fragment.SplitItemFragment.SplitItemFragmentCallbacks;
import com.money.manager.ex.interfaces.ISplitTransactionsDataset;

import java.util.ArrayList;
public class SplitTransactionsRepeatingActivity extends BaseFragmentActivity
        implements SplitItemFragmentCallbacks, InputAmountDialogListener {

    public static final String KEY_SPLIT_TRANSACTION = "SplitTransactionsRepeatingActivity:ArraysSplitTransaction";
    public static final String KEY_SPLIT_TRANSACTION_DELETED = "SplitTransactionsRepeatingActivity:ArraysSplitTransactionDeleted";
    public static final String INTENT_RESULT_SPLIT_TRANSACTION = "SplitTransactionsRepeatingActivity:ResultSplitTransaction";
    public static final String INTENT_RESULT_SPLIT_TRANSACTION_DELETED = "SplitTransactionsRepeatingActivity:ResultSplitTransactionDeleted";

    private static final int MENU_ADD_SPLIT_TRANSACTION = 1;
    private static int mIdTag = 0x8000;
    private ArrayList<ISplitTransactionsDataset> mSplitTransactions = null;
    private ArrayList<ISplitTransactionsDataset> mSplitDeleted = null;
    private SplitItemFragment mFragmentInputAmountClick;

    private void addFragmentChild(ISplitTransactionsDataset object) {
        String nameFragment = SplitItemFragment.class.getSimpleName() + "_" + Integer.toString(object.getSplitTransId() == -1 ? mIdTag++ : object.getSplitTransId());
        SplitItemFragment fragment = (SplitItemFragment) getSupportFragmentManager().findFragmentByTag(nameFragment);
        if (fragment == null) {
            fragment = SplitItemFragment.newIstance(object);
            fragment.setOnSplitItemCallback(this);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            // animation
            // transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out);
            // Replace whatever is in the fragment_container view with this fragment,
            // and add the transaction to the back stack
            transaction.add(R.id.linearLayoutSplitTransaction, fragment, nameFragment);
            transaction.commit();
        }
    }

    public ArrayList<ISplitTransactionsDataset> getAllTableSplitTransaction() {
        ArrayList<ISplitTransactionsDataset> items = new ArrayList<ISplitTransactionsDataset>();
        for (int i = 0; i < mIdTag; i++) {
            String nameFragment = SplitItemFragment.class.getSimpleName() + "_" + Integer.toString(i);
            SplitItemFragment fragment = (SplitItemFragment) getSupportFragmentManager().findFragmentByTag(nameFragment);
            if (fragment != null && fragment.isVisible()) {
                items.add(fragment.getTableSplitTransactions());
            }
        }
        return items;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // load intent
        if (getIntent() != null) {
            mSplitTransactions = getIntent().getParcelableArrayListExtra(KEY_SPLIT_TRANSACTION);
            mSplitDeleted = getIntent().getParcelableArrayListExtra(KEY_SPLIT_TRANSACTION_DELETED);
        }
        // load deleted item
        if (savedInstanceState != null && savedInstanceState.containsKey(KEY_SPLIT_TRANSACTION_DELETED)) {
            mSplitTransactions = savedInstanceState.getParcelableArrayList(KEY_SPLIT_TRANSACTION_DELETED);
        }

        // set view
        setContentView(R.layout.activity_split_transactions_repeating);
        // toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            setToolbarStandardAction(toolbar);
        }

        Button buttonAdd = (Button) findViewById(R.id.buttonAdd);
        buttonAdd.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                addFragmentChild(new TableBudgetSplitTransactions());
            }
        });

        if (mSplitTransactions != null) {
            for (int i = 0; i < mSplitTransactions.size(); i++) {
                addFragmentChild(mSplitTransactions.get(i));
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_split_transactions_repeating, menu);
//        return true;

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

        if (item.getItemId() == MENU_ADD_SPLIT_TRANSACTION) {
            addFragmentChild(new TableBudgetSplitTransactions());
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRemoveItem(ISplitTransactionsDataset object) {
        if (mSplitDeleted == null) {
            mSplitDeleted = new ArrayList<ISplitTransactionsDataset>();
        }
        // add item to delete
        if (object.getSplitTransId() != -1) // not new split transaction
            mSplitDeleted.add(object);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mSplitDeleted != null)
            outState.putParcelableArrayList(KEY_SPLIT_TRANSACTION_DELETED, mSplitDeleted);
    }

    @Override
    public boolean onActionCancelClick() {
        setResult(RESULT_CANCELED);
        finish();

        return true;
    }

    @Override
    public boolean onActionDoneClick() {
        ArrayList<ISplitTransactionsDataset> items = getAllTableSplitTransaction();
        // check data
        for (int i = 0; i < items.size(); i++) {
            ISplitTransactionsDataset item = items.get(i);
            if (item.getCategId() == -1 && item.getCategId() == -1) {
                Core.alertDialog(SplitTransactionsRepeatingActivity.this, R.string.error_category_not_selected).show();
                return false;
            }
        }
        Intent result = new Intent();
        result.putParcelableArrayListExtra(INTENT_RESULT_SPLIT_TRANSACTION, items);
        result.putParcelableArrayListExtra(INTENT_RESULT_SPLIT_TRANSACTION_DELETED, mSplitDeleted);
        setResult(RESULT_OK, result);
        finish();

        return true;
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
}
