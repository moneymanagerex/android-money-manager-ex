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
package org.moneymanagerex.android.espresso;

import android.support.test.espresso.contrib.DrawerActions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.money.manager.ex.R;
import com.money.manager.ex.home.MainActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

/**
 * Espresso tests for the Main Activity.
 */
//@RunWith(AndroidJUnit4.class)
//public class MainActivityTests {
//
//    @Rule
//    public final ActivityTestRule<MainActivity> activityRule = new ActivityTestRule<>(MainActivity.class);
//
//    @Before
//    public void setUp() throws Exception {
//    }
//
//    @After
//    public void tearDown() throws Exception {
//    }
//
//    @Test
//    public void openDashboard() {
//        onView(withText("Money Manager Ex"))
//                .check(matches(isDisplayed()));
//
//        onView(withId(R.id.drawerLayout))
//                .perform(DrawerActions.open());
//
//        onView(withText("Entities"))
//            .check(matches(isDisplayed()));
//
//    }
//
//    @Test
//    public void isWelcomeViewDisplayed() {
//        onView(withText("Welcome to MoneyManagerEx!"))
//                .check(matches(isDisplayed()));
//    }
//
//    @Test
//    public void isAccountsListDisplayed() {
//        onView(withText("Bank Accounts"))
//                .check(matches(isDisplayed()));
//    }
//
//}
