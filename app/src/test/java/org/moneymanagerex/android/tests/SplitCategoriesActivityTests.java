package org.moneymanagerex.android.tests;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.TextView;

import com.money.manager.ex.BuildConfig;
import com.money.manager.ex.R;
import com.money.manager.ex.SplitCategoriesActivity;
import com.money.manager.ex.core.TransactionTypes;
import com.money.manager.ex.database.ITransactionEntity;
import com.money.manager.ex.domainmodel.SplitCategory;
import com.money.manager.ex.view.RobotoTextView;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.moneymanagerex.android.testhelpers.UnitTestHelper;
import org.parceler.Parcels;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.util.ActivityController;

import java.util.ArrayList;

import info.javaperformance.money.Money;
import info.javaperformance.money.MoneyFactory;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for Split Categories activity.
 *
 * Created by Alen Siljak on 28/09/2015.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
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

        this.controller = UnitTestHelper.getController(SplitCategoriesActivity.class);
    }

    @After
    public void tearDown() {
        this.controller.destroy();

        // destroy db helper
        UnitTestHelper.teardownDatabase();
    }

    @Test
    public void activityRunsStandalone() {
        SplitCategoriesActivity activity = UnitTestHelper.getActivity(this.controller);
        assertThat(activity).isNotNull();
    }

    /**
     * Confirm that the displayed amount after entry contains the correctly formatted currency,
     * and the correct currency.
     */
    @Test
    public void displayCurrencyMatchesTheAccount() {
        // set up

        Intent intent = createIntent();
        Money enteredAmount = MoneyFactory.fromString("5.38");

        // run

        SplitCategoriesActivity activity = this.controller
                .withIntent(intent)
                .create().visible().start().get();

        assertThat(activity).isNotNull();
        assertThat(activity.getIntent().getStringExtra(SplitCategoriesActivity.KEY_DATASET_TYPE))
                .isEqualTo(SplitCategory.class.getSimpleName());

        // enter number
        Fragment fragment = activity.getSupportFragmentManager().getFragments().get(0);
        assertThat(fragment).isNotNull();
        // get amount text box.
        View view = fragment.getView().findViewById(R.id.editTextTotAmount);
        assertThat(view).isNotNull();

        // click to open input dialog here

        // receive amount back
        // todo: replace this with EventBus
//        ((IInputAmountDialogListener) fragment).onFinishedInputAmountDialog(
//                view.getId(), enteredAmount);

        // view must be text view.
        assertThat(view.getClass()).isEqualTo(RobotoTextView.class);
        assertThat((String) view.getTag()).isEqualTo(enteredAmount.toString());
        String actualAmountText = ((TextView) view).getText().toString();
        assertThat(actualAmountText).isNotEqualTo(enteredAmount.toString());
        assertThat(actualAmountText).isEqualTo("€ 5.38");
    }

    private Intent createIntent() {
        // Recurring transactions
        // TableBudgetSplitTransactions.class.getSimpleName()
        // Account Transactions
        String datasetName = SplitCategory.class.getSimpleName();
        TransactionTypes transactionType = TransactionTypes.Withdrawal;
        ArrayList<ITransactionEntity> mSplitTransactions = null;
        ArrayList<ITransactionEntity> mSplitTransactionsDeleted = null;
        int currencyId = 2;

        Context context = UnitTestHelper.getContext();

        // this is a copy of production intent code

        Intent intent = new Intent(context, SplitCategoriesActivity.class);
        intent.putExtra(SplitCategoriesActivity.KEY_DATASET_TYPE, datasetName);
        intent.putExtra(SplitCategoriesActivity.KEY_TRANSACTION_TYPE, transactionType.getCode());
        intent.putExtra(SplitCategoriesActivity.KEY_SPLIT_TRANSACTION, Parcels.wrap(mSplitTransactions));
        intent.putExtra(SplitCategoriesActivity.KEY_SPLIT_TRANSACTION_DELETED, Parcels.wrap(mSplitTransactionsDeleted));
        intent.putExtra(SplitCategoriesActivity.KEY_CURRENCY_ID, currencyId);

//        mParent.startActivityForResult(intent, REQUEST_PICK_SPLIT_TRANSACTION);

        return intent;
    }
}
