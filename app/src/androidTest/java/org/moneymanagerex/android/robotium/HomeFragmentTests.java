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
package org.moneymanagerex.android.robotium;

import android.support.test.runner.AndroidJUnit4;
import android.test.ActivityInstrumentationTestCase2;

import com.money.manager.ex.R;
import com.money.manager.ex.home.MainActivity;
import com.money.manager.ex.search.SearchActivity;
import com.robotium.solo.Solo;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.moneymanagerex.android.testhelpers.UiTestHelpersRobotium;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Various Search activity tests.
 */
@RunWith(AndroidJUnit4.class)
public class HomeFragmentTests {
    // extends ActivityInstrumentationTestCase2<MainActivity>

    private Solo solo;

//    public HomeFragmentTests() {
//        super(MainActivity.class);
//    }

    @Before
    public void setUp() {
//        solo = UiTestHelpersRobotium.setUp(this);
    }

    @After
    public void tearDown() throws Exception {
//        super.tearDown();

        UiTestHelpersRobotium.tearDown(solo);
    }

    /**
     * Open/close expandable view of accounts.
     * Attention: groups must be manually collapsed prior to running the test.
     */
    @Test
    public void expandCollapseAccountList() {
        // Given

        String accountName = "cash, BAM";
        boolean initiallyExpanded = solo.searchText(accountName);
//        collapseAllGroups();

        // When

        solo.clickOnText("Bank Accounts");

        // Then

        boolean accountVisible = solo.searchText(accountName);
        if (initiallyExpanded) {
//            assertThat(accountVisible).isFalse();
        } else {
//            assertThat(accountVisible).isTrue();
        }

        // and again

        solo.clickOnText("Bank Accounts");

        accountVisible = solo.searchText(accountName);
        if (initiallyExpanded) {
//            assertThat(accountVisible).isTrue();
        } else {
//            assertThat(accountVisible).isFalse();
        }
    }

    private void collapseAllGroups() {
        if (solo.searchText("28 degrees")) {
            solo.clickOnText("Credit Card Accounts");
        }

        if (solo.searchText("brl")) {
            solo.clickOnText("Investment Accounts");
        }

        if (solo.searchText("HSBC savings")) {
            solo.clickOnText("Term Accounts");
        }
    }
}
