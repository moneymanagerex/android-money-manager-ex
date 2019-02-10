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

//import android.support.test.InstrumentationRegistry;
//import android.support.test.runner.AndroidJUnit4;
//import androidx.drawerlayout.widget.DrawerLayout;
//import android.test.ActivityInstrumentationTestCase2;

import com.money.manager.ex.budget.BudgetsActivity;
import com.robotium.solo.Solo;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Robotium tests for the Main Activity.
 */
//@RunWith(AndroidJUnit4.class)
public class MainActivityTests {
// extends ActivityInstrumentationTestCase2<MainActivity>

    private Solo solo;

//    public MainActivityTests() {
//        super(MainActivity.class);
//
//    }

//    @Before
//    public void setUp() throws Exception {
//        super.setUp();
//
//        injectInstrumentation(InstrumentationRegistry.getInstrumentation());
//
//        solo = new Solo(getInstrumentation(), getActivity());
//    }

    @After
    public void tearDown() throws Exception {
//        super.tearDown();

        if (solo != null) {
            solo.finishOpenedActivities();
        }
    }

//    @Test
//    public void testCreation() {
//        assertNotNull(getActivity());
//    }

    public void welcomeViewDisplayed() {
        solo.waitForText("Welcome to MoneyManagerEx");

        //assert
    }

//    @Test
//    public void testDrawerOpenClose() {
////        solo.waitForActivity(MainActivity.class.getSimpleName());
//
//        View view = getActivity().findViewById(R.id.drawerLayout);
//        DrawerLayout drawer = (DrawerLayout) view;
//        assertThat(drawer).isNotNull();
//
////        assertThat(drawer.isDrawerOpen(view)).isFalse();
//        assertThat(solo.searchText("Budgets", true)).isFalse();
//
//        solo.clickOnActionBarHomeButton();
////        assertThat(drawer.isDrawerOpen(drawer)).isFalse();
//        assertThat(solo.searchText("Budgets", true)).isTrue();
//
//        solo.clickOnActionBarHomeButton();
////        assertThat(drawer.isDrawerOpen(drawer)).isFalse();
//        assertThat(solo.searchText("Budgets", true)).isFalse();
//    }

    @Test
    public void testBudgetsOpen() {
        solo.clickOnActionBarHomeButton();
//        assertThat(solo.searchText("Budgets", true)).as("Budgets menu item not visible.").isTrue();

        solo.clickOnText("Budgets");
        solo.waitForActivity(BudgetsActivity.class.getSimpleName());

//        assertThat(solo.searchText("Budget list")).isTrue();
    }

    @Test
    public void testEntitiesSubmenu() {
        solo.clickOnActionBarHomeButton();

//        assertThat(solo.searchText("Currencies")).isFalse();

        solo.clickOnText("Entities");

//        assertThat(solo.searchText("Accounts")).isTrue();
//        assertThat(solo.searchText("Categories")).isTrue();
//        assertThat(solo.searchText("Currencies")).isTrue();
//        assertThat(solo.searchText("Payees")).isTrue();
    }

    @Test
    public void testCurrenciesOpen() {
        solo.clickOnActionBarHomeButton();
        solo.clickOnText("Entities");
        solo.clickOnText("Currencies");

//        assertThat(solo.waitForActivity(CurrencyListActivity.class.getSimpleName())).isTrue();
//        assertThat(solo.searchText("Bosnia and Herzegovina")).isTrue();
    }

    @Test
    public void openAssetAllocation() {
        solo.clickOnActionBarHomeButton();
        solo.clickOnText("Asset Allocation");

//        assertThat(solo.waitForActivity(AssetAllocationEditorActivity.class.getSimpleName()))
//            .as("Asset Allocation not started")
//            .isTrue();
    }
}
