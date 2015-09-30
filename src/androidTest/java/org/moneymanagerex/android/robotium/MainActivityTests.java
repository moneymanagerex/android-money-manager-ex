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
 *
 */
package org.moneymanagerex.android.robotium;

import android.support.v4.widget.DrawerLayout;
import android.test.ActivityInstrumentationTestCase2;
import android.view.View;

import com.money.manager.ex.R;
import com.money.manager.ex.budget.BudgetsActivity;
import com.money.manager.ex.currency.CurrenciesActivity;
import com.money.manager.ex.home.MainActivity;
import com.robotium.solo.Solo;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Robotium tests for the Main Activity.
 */
public class MainActivityTests
        extends ActivityInstrumentationTestCase2<MainActivity> {

    private Solo solo;

    public MainActivityTests() {
        super(MainActivity.class);

    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        solo = new Solo(getInstrumentation(), getActivity());
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();

        if (solo != null) {
            solo.finishOpenedActivities();
        }
    }

    public void testCreation() {
        assertNotNull(getActivity());
    }

    public void welcomeViewDisplayed() {
        solo.waitForText("Welcome to MoneyManagerEx");

        //assert
    }

    public void testDrawerOpenClose() {
//        solo.waitForActivity(MainActivity.class.getSimpleName());

        View view = getActivity().findViewById(R.id.drawerLayout);
        DrawerLayout drawer = (DrawerLayout) view;
        assertThat(drawer).isNotNull();

//        assertThat(drawer.isDrawerOpen(view)).isFalse();
        assertThat(solo.searchText("Budgets", true)).isFalse();

        solo.clickOnActionBarHomeButton();
//        assertThat(drawer.isDrawerOpen(drawer)).isFalse();
        assertThat(solo.searchText("Budgets", true)).isTrue();

        solo.clickOnActionBarHomeButton();
//        assertThat(drawer.isDrawerOpen(drawer)).isFalse();
        assertThat(solo.searchText("Budgets", true)).isFalse();
    }

    public void testBudgetsOpen() {
        solo.clickOnActionBarHomeButton();
        assertThat(solo.searchText("Budgets", true)).as("Budgets menu item not visible.").isTrue();

        solo.clickOnText("Budgets");
        solo.waitForActivity(BudgetsActivity.class.getSimpleName());

        assertThat(solo.searchText("Budget list")).isTrue();
    }

    public void testEntitiesSubmenu() {
        solo.clickOnActionBarHomeButton();

        assertThat(solo.searchText("Currencies")).isFalse();

        solo.clickOnText("Entities");

        assertThat(solo.searchText("Accounts")).isTrue();
        assertThat(solo.searchText("Categories")).isTrue();
        assertThat(solo.searchText("Currencies")).isTrue();
        assertThat(solo.searchText("Payees")).isTrue();
    }

    public void testCurrenciesOpen() {
        solo.clickOnActionBarHomeButton();
        solo.clickOnText("Entities");
        solo.clickOnText("Currencies");

        assertThat(solo.waitForActivity(CurrenciesActivity.class.getSimpleName())).isTrue();
        assertThat(solo.searchText("Bosnia and Herzegovina")).isTrue();
    }
}
