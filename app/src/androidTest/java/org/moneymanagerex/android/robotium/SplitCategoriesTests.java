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
package org.moneymanagerex.android.robotium;

import android.support.test.runner.AndroidJUnit4;
import android.test.ActivityInstrumentationTestCase2;

import com.money.manager.ex.R;
import com.money.manager.ex.home.MainActivity;
import com.robotium.solo.Solo;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.moneymanagerex.android.testhelpers.UiTestHelpersRobotium;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Split categories
 */
@RunWith(AndroidJUnit4.class)
public class SplitCategoriesTests {
    // extends ActivityInstrumentationTestCase2<MainActivity>

    private Solo solo;

//    public SplitCategoriesTests() {
//        super(MainActivity.class);
//    }

//    @Before
//    public void setUp() {
//        solo = UiTestHelpersRobotium.setUp(this);
//    }

    @After
    public void tearDown() throws Exception {
//        super.tearDown();

        UiTestHelpersRobotium.tearDown(solo);
    }

//    @Test
//    public void formOpens() {
//        assertThat(solo.waitForActivity(getActivity().getClass().getSimpleName())).isTrue();
//    }

    /**
     * Create a transaction with two splits and confirm it has been created correctly.
     */
//    @Test
//    public void createSplitCategories() {
//        UiTestHelpersRobotium helper = new UiTestHelpersRobotium(solo);
//
//        // open new transaction
//        helper.clickOnFloatingButton();
//
//        // open splits
//        solo.clickOnView(solo.getView(R.id.splitButton));
//        assertThat(solo.searchText("Amount")).isTrue();
//
//        // click the category selector
//        solo.clickOnView(solo.getView(R.id.textViewCategory));
//        // select Car:Gas
//        solo.clickOnText("Car");
//        assertThat(solo.searchText("Gas")).isTrue();
//        solo.clickOnView(solo.getView(R.id.selector, 2));
//        // back on splits
//        assertThat(solo.searchText("Amount")).isTrue();
//        assertThat(solo.searchText("Car:Gas")).isTrue();
//        // amount
//        solo.clickOnView(solo.getView(R.id.editTextTotAmount));
//        helper.enterInNumericInput("25");
//        solo.clickOnText("OK");
//
//        // add Car:Parking
//        helper.clickOnFloatingButton();
//        solo.clickOnView(solo.getView(R.id.textViewCategory, 1));
//        // category selector
//        solo.clickOnText("Car");
//        assertThat(solo.searchText("Parking")).isTrue();
//        solo.clickOnView(solo.getView(R.id.selector, 4));
//        // back on splits
//        assertThat(solo.searchText("Car:Parking")).isTrue();
//        // amount
//        solo.clickOnView(solo.getView(R.id.editTextTotAmount, 1));
//        helper.enterInNumericInput("13.5");
//        solo.clickOnText("OK");
//
//        // OK, confirm splits
//        solo.clickOnView(solo.getView(R.id.action_done));
//
//        assertThat(solo.searchText("38.5"));
//
//        // update transaction
//        solo.clickOnView(solo.getView(R.id.action_done));
//
//        deleteSplitTransaction();
//    }

    /**
     * todo: need to find a way to click Delete Transaction in the multi-choice toolbar.
     */
    //@Test
    public void deleteSplitTransaction() {
        // todo: open account transactions
        // or find it through search?
//        solo.clickOnView(solo.getView(R.menu.menu_search_transaction));

        // Open 28 degrees account
        if (!solo.searchText("28 degrees")) {
            solo.clickOnText("Credit Card Accounts");
        }
        solo.clickOnText("28 degrees");

        // select the latest transaction (first in the list after the header).
        solo.clickLongInList(2);
//        solo.clickOnMenuItem("Delete transaction");
//        solo.pressMenuItem(1);
//        solo.sendKey(Solo.MENU);
        solo.clickOnActionBarItem(3);

        solo.waitForDialogToOpen();
    }
}
