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
package org.moneymanagerex.android.tests;

import android.content.Intent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.money.manager.ex.BuildConfig;
import com.money.manager.ex.R;
import com.money.manager.ex.common.AllDataListFragment;
import com.money.manager.ex.core.RequestCodes;
import com.money.manager.ex.core.TransactionTypes;
import com.money.manager.ex.search.SearchActivity;
import com.money.manager.ex.search.SearchParametersFragment;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.moneymanagerex.android.testhelpers.DataHelpers;
import org.moneymanagerex.android.testhelpers.TestApplication;
import org.moneymanagerex.android.testhelpers.UnitTestHelper;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.Fragment;
import info.javaperformance.money.MoneyFactory;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;


/**
 * Test Search activity.
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, application = TestApplication.class)
public class SearchActivityTests {

    private ActivityController<SearchActivity> controller;
    private SearchActivity activity;

    @Before
    public void setUp() {
        this.controller = UnitTestHelper.getController(SearchActivity.class);
        this.activity = UnitTestHelper.getActivity(this.controller);

        UnitTestHelper.setupContentProvider();
        UnitTestHelper.setupLog();
    }

    @After
    public void tearDown() {
        this.controller.destroy();
    }

    @Test
    public void activityOpens() {
        assertThat(this.activity, notNullValue());
    }

    /**
     * Add two transaction for Dining Out. Search for this sub/category and confirm that the
     * results are shown and the total & currency match.
     */
    //@Test
    public void searchForSubCategoryWorksWithData() {
        // arrange

        UnitTestHelper.setDefaultCurrency("BAM");
        DataHelpers.insertData();
        // add expected transaction
        DataHelpers.createTransaction(1, -1, TransactionTypes.Withdrawal, 2, 9, MoneyFactory.fromString("-14.68"));
        // add one more for testing the total calculation
        DataHelpers.createTransaction(1, -1, TransactionTypes.Withdrawal, 2, 9, MoneyFactory.fromString("-25.37"));

        //*******************************************
        // act

        // Click Select Category
        TextView selectCategory = (TextView) activity.findViewById(R.id.textViewSelectCategory);
//        assertThat(selectCategory).isNotNull();
        selectCategory.performClick();

        // confirm that clicking the Select Category text view opens category selector
        ShadowActivity shadowActivity = Shadows.shadowOf(this.activity);
        Intent expectedIntent = shadowActivity.peekNextStartedActivityForResult().intent;
//        assertThat(expectedIntent.getComponent()).isEqualTo(new ComponentName(this.activity,
//                CategoryListActivity.class));
//        assertThat(shadowActivity.getNextStartedActivity()).isEqualTo(expectedIntent);

        // Now simulate that we received the category.

        Fragment searchFragment = UnitTestHelper.getFragment(activity, SearchParametersFragment.class.getSimpleName());
//        assertThat(searchFragment).isNotNull();

        // We "selected" Food:Dining out.
        Intent categoryData = UnitTestHelper.getSelectCategoryResult(2, "Food", 9, "Dining out");
        searchFragment.onActivityResult(RequestCodes.CATEGORY, AppCompatActivity.RESULT_OK,
                categoryData);
//        assertThat(selectCategory.getText()).containsSequence("Food : Dining out");

        // Run search

        LinearLayout searchButton = (LinearLayout) activity.findViewById(R.id.action_search);
//        assertThat(searchButton).isNotNull();
        searchButton.performClick();

        //**************************************
        // assert

        // confirm the Total is shown and the sum is 0.

        Fragment resultsFragment = UnitTestHelper.getFragment(activity, AllDataListFragment.class.getSimpleName());
//        assertThat(resultsFragment).isNotNull();

        View totalView = resultsFragment.getView().findViewById(R.id.textViewColumn1);
//        assertThat(totalView).isNotNull();
//        assertThat(totalView).isInstanceOf(TextView.class);
        TextView totalTextView = (TextView) totalView;
//        assertThat(totalTextView.getText()).isEqualTo("Total");

        // total amount
        View totalNumberView = resultsFragment.getView().findViewById(R.id.textViewColumn2);
//        assertThat(totalNumberView).isNotNull();
//        assertThat(totalNumberView).isInstanceOf(TextView.class);
        TextView totalNumberTextView = (TextView) totalNumberView;
//        assertThat(totalNumberTextView.getText()).isEqualTo("KM 40.05");
    }

    //@Test
    public void searchForSubCategoryWorksWithNoData() {
        //*******************************************
        // arrange

        UnitTestHelper.setDefaultCurrency("BAM");
        DataHelpers.insertData();

        //*******************************************
        // act

        // Click Select Category
        TextView selectCategory = (TextView) activity.findViewById(R.id.textViewSelectCategory);
        selectCategory.performClick();

        // confirm that clicking the Select Category text view opens category selector
        ShadowActivity shadowActivity = Shadows.shadowOf(this.activity);
        Intent expectedIntent = shadowActivity.peekNextStartedActivityForResult().intent;
//        assertThat(expectedIntent.getComponent()).isEqualTo(new ComponentName(this.activity,
//            CategoryListActivity.class));
        assertThat(shadowActivity.getNextStartedActivity(), equalTo(expectedIntent));

        // Now simulate that we received the category.

        Fragment searchFragment = UnitTestHelper.getFragment(activity, SearchParametersFragment.class.getSimpleName());

        // We "selected" Food:Dining out.
        Intent categoryData = UnitTestHelper.getSelectCategoryResult(2, "Food", 9, "Dining out");
        searchFragment.onActivityResult(RequestCodes.CATEGORY, AppCompatActivity.RESULT_OK,
            categoryData);
//        assertThat(selectCategory.getText()).containsSequence("Food : Dining out");

        // Run search

        LinearLayout searchButton = (LinearLayout) activity.findViewById(R.id.action_search);
        searchButton.performClick();

        //**************************************
        // assert

        // confirm the Total is shown and the sum is 0.

        Fragment resultsFragment = UnitTestHelper.getFragment(activity, AllDataListFragment.class.getSimpleName());

        View totalView = resultsFragment.getView().findViewById(R.id.textViewColumn1);
//        assertThat(totalView).isInstanceOf(TextView.class);
        TextView totalTextView = (TextView) totalView;
//        assertThat(totalTextView.getText()).isEqualTo("Total");

        // total amount
        View totalNumberView = resultsFragment.getView().findViewById(R.id.textViewColumn2);
//        assertThat(totalNumberView).isInstanceOf(TextView.class);
        TextView totalNumberTextView = (TextView) totalNumberView;
        assertThat(totalNumberTextView.getText().toString(), equalTo("KM 0.00"));
    }

}
