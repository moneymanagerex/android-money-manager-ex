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

import android.content.ComponentName;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.TextView;

import com.money.manager.ex.BuildConfig;
import com.money.manager.ex.R;
import com.money.manager.ex.account.AccountEditActivity;
import com.money.manager.ex.home.HomeFragment;
import com.money.manager.ex.home.MainActivity;
import com.money.manager.ex.tutorial.TutorialActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.moneymanagerex.android.testhelpers.DataHelpers;
import org.moneymanagerex.android.testhelpers.UnitTestHelper;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.util.ActivityController;

import static junit.framework.Assert.assertNotNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.Shadows.shadowOf;

/**
 * Test the MainActivity.
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class MainActivityTests {

    private ActivityController<MainActivity> controller;
    private MainActivity activity;

    @Before
    public void setUp() {
        UnitTestHelper.setupContentProvider();

        this.controller = UnitTestHelper.getController(MainActivity.class);
        this.activity = UnitTestHelper.getActivity(this.controller);
    }

    @After
    public void tearDown() {
        this.controller.destroy();

        UnitTestHelper.teardownDatabase();
    }

    /**
     * Test the activity lifecycle in unit tests.
     * Simulates the opening of the app the very first time, initialization of preferences,
     * database, etc. Displays the Tutorial and the Welcome screen in the Home fragment.
     * Then opens the Add New Account activity.
     * See http://robolectric.org/activity-lifecycle/
     */
    @Test
    public void runMainActivity() {
        Fragment homeFragment;
        Intent expectedIntent;

        homeFragment = UnitTestHelper.getFragment(activity, HomeFragment.class.getSimpleName());
        assertThat(homeFragment).isNotNull();

        // Confirm Tutorial is shown.
        ShadowActivity shadowActivity = Shadows.shadowOf(activity);
        expectedIntent = shadowActivity.peekNextStartedActivityForResult().intent;
        assertThat(expectedIntent.getComponent()).isEqualTo(new ComponentName(activity, TutorialActivity.class));
        assertThat(shadowActivity.getNextStartedActivity()).isEqualTo(expectedIntent);

        TutorialActivity tutorialActivity = Robolectric.buildActivity(TutorialActivity.class)
                .withIntent(expectedIntent)
                .create().get();
        assertThat(tutorialActivity).isNotNull();

        // Close tutorial
        View view = tutorialActivity.findViewById(R.id.skipTextView);
        assertNotNull("Tutorial close not found", view);
        view.performClick();

        // Home Fragment is set-up.
        testHomeFragment(homeFragment);

        // Click Add New Account button.
        view = homeFragment.getView().findViewById(R.id.buttonAddAccount);
        assertNotNull("Add Account button not found", view);
        view.performClick();

        // Add Account opens up.
        expectedIntent = new Intent(activity, AccountEditActivity.class);
        expectedIntent.setAction(Intent.ACTION_INSERT);
        assertThat(shadowOf(activity).getNextStartedActivity()).isEqualTo(expectedIntent);
    }

    @Test
    public void pauseAndResume() {
        Fragment homeFragment = UnitTestHelper.getFragment(activity, HomeFragment.class.getSimpleName());

        testHomeFragment(homeFragment);

        this.controller.pause().resume();

        testHomeFragment(homeFragment);
    }

    /**
     * This is a good idea but the implementation is difficult because of async data loaders.
     */
    //@Test
    public void incomeExpenseQuery() {
        // Given
        // Create a split transaction
        DataHelpers.createSplitTransaction();
        this.controller = UnitTestHelper.getController(MainActivity.class);
        this.activity = UnitTestHelper.getActivity(this.controller);

        // When
        TextView incomeTextView = (TextView) this.activity.findViewById(R.id.textViewIncome);
        String income = incomeTextView.getText().toString();
        TextView expenseTextView = (TextView) this.activity.findViewById(R.id.textViewExpenses);

        // Then
        // Check the income/expense for the month
        assertThat(incomeTextView).isNotNull();
        assertThat(income).isEqualToIgnoringCase("100");
        assertThat(expenseTextView).isNotNull();
    }

    // Private

    /**
     * Confirm that the fragment is initialized, has a view, and athached to the MainActivity.
     * @param homeFragment
     */
    private void testHomeFragment(Fragment homeFragment) {
        assertThat(homeFragment).isNotNull();
        assertThat(homeFragment.getView()).isNotNull();
        assertThat(homeFragment.getActivity()).isNotNull();
        assertThat(homeFragment.getActivity()).isInstanceOf(MainActivity.class);
    }
}
