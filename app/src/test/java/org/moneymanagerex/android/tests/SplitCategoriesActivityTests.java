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

package org.moneymanagerex.android.tests;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.money.manager.ex.R;
import com.money.manager.ex.core.TransactionTypes;
import com.money.manager.ex.database.ITransactionEntity;
import com.money.manager.ex.domainmodel.SplitCategory;
import com.money.manager.ex.transactions.SplitCategoriesActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.moneymanagerex.android.testhelpers.UnitTestHelper;
import org.parceler.Parcels;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;

import java.util.ArrayList;

import info.javaperformance.money.Money;
import info.javaperformance.money.MoneyFactory;


/**
 * Unit tests for Split Categories activity.
 */
@RunWith(RobolectricTestRunner.class)
public class SplitCategoriesActivityTests {

    private ActivityController<SplitCategoriesActivity> controller;
//    private SplitCategoriesActivity activity;

    @BeforeClass
    public static void suiteSetup() {
        // can't initialize content provider here as the static context does not have an application.
    }

    @Before
    public void setUp() {
        // set up the content provider
        UnitTestHelper.setupContentProvider();
        // todo: insert any data here, if needed.

        controller = UnitTestHelper.getController(SplitCategoriesActivity.class);
    }

    @After
    public void tearDown() {
        controller.destroy();
    }

    @Test
    public void activityRunsStandalone() {
        final SplitCategoriesActivity activity = UnitTestHelper.getActivity(controller);
        assertThat(activity, notNullValue());
    }

    /**
     * Confirm that the displayed amount after entry contains the correctly formatted currency,
     * and the correct currency.
     */
    //@Test
    public void displayCurrencyMatchesTheAccount() {
        // set up

        final Intent intent = createIntent();
        final Money enteredAmount = MoneyFactory.fromString("5.38");

        // run

        final SplitCategoriesActivity activity = Robolectric
                .buildActivity(SplitCategoriesActivity.class, intent)
                .create().visible().start().get();


//        assertThat(activity).isNotNull();
//        assertThat(activity.getIntent().getStringExtra(SplitCategoriesActivity.KEY_DATASET_TYPE))
//                .isEqualTo(SplitCategory.class.getSimpleName());

        // enter number
        final Fragment fragment = activity.getSupportFragmentManager().getFragments().get(0);
//        assertThat(fragment).isNotNull();
        // get amount text box.
        final View view = fragment.getView().findViewById(R.id.editTextTotAmount);
//        assertThat(view).isNotNull();

        // click to open input binaryDialog here

        // receive amount back
        // todo: replace this with EventBus
//        ((IInputAmountDialogListener) fragment).onFinishedInputAmountDialog(
//                view.getId(), enteredAmount);

        // view must be text view.
//        assertThat(view.getClass()).isEqualTo(RobotoTextView.class);
//        assertThat((String) view.getTag()).isEqualTo(enteredAmount.toString());
        final String actualAmountText = ((TextView) view).getText().toString();
//        assertThat(actualAmountText).isNotEqualTo(enteredAmount.toString());
//        assertThat(actualAmountText).isEqualTo("€ 5.38");
    }

    private Intent createIntent() {
        // Recurring transactions
        // TableBudgetSplitTransactions.class.getSimpleName()
        // Account Transactions
        final String datasetName = SplitCategory.class.getSimpleName();
        final TransactionTypes transactionType = TransactionTypes.Withdrawal;
        final ArrayList<ITransactionEntity> mSplitTransactions = null;
        final ArrayList<ITransactionEntity> mSplitTransactionsDeleted = null;
        final int currencyId = 2;

        final Context context = UnitTestHelper.getContext();

        // this is a copy of production intent code

        final Intent intent = new Intent(context, SplitCategoriesActivity.class);
        intent.putExtra(SplitCategoriesActivity.KEY_DATASET_TYPE, datasetName);
        intent.putExtra(SplitCategoriesActivity.KEY_TRANSACTION_TYPE, transactionType.getCode());
        intent.putExtra(SplitCategoriesActivity.KEY_SPLIT_TRANSACTION, Parcels.wrap(mSplitTransactions));
        intent.putExtra(SplitCategoriesActivity.KEY_SPLIT_TRANSACTION_DELETED, Parcels.wrap(mSplitTransactionsDeleted));
        intent.putExtra(SplitCategoriesActivity.KEY_CURRENCY_ID, currencyId);

//        mParent.startActivityForResult(intent, REQUEST_PICK_SPLIT_TRANSACTION);

        return intent;
    }
}
