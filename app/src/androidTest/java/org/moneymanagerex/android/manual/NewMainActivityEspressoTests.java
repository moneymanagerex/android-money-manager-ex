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
package org.moneymanagerex.android.manual;

import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.money.manager.ex.home.MainActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.moneymanagerex.android.testhelpers.UiTestHelpersEspresso;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.assertTrue;

/**
 * Espresso tests for the Main Activity.
 * The tests start with a fresh copy of preferences.
 */
//@RunWith(AndroidJUnit4.class)
//public class NewMainActivityEspressoTests {
//
//    @Rule
//    public final ActivityTestRule<MainActivity> activityRule = new ActivityTestRule<>(MainActivity.class);
//
//    private UiTestHelpersEspresso helper;
//
//    @Before
//    public void setUp() {
//        this.helper = new UiTestHelpersEspresso();
//        helper.clearPreferences(InstrumentationRegistry.getContext());
//    }
//
//    @After
//    public void tearDown() {
//        this.helper.clearPreferences(InstrumentationRegistry.getContext());
//        this.helper = null;
//    }
//
//    @Ignore
//    public void welcomeScreen() {
//        onView(withText("Welcome to MoneyManagerEx"))
//                .check(matches(isDisplayed()));
//    }
//
//    @Ignore
//    public void changelog() {
////        assertTrue(solo.waitForActivity(MainActivity.class, 2000));
////
////        assertTrue(solo.waitForDialogToOpen());
//
////        assertTrue(solo.waitForText("Changelog"));
//        onView(withText("Changelog"))
//                .check(matches(isDisplayed()));
//
////        assertTrue(solo.searchText("OK", true));
////        solo.clickOnText("OK", 1, true);
//        onView(withText("OK"))
//                .check(matches(isDisplayed()))
//                .perform(click());
//    }
//
//    @Ignore
//    public void tutorial() {
//        onView(withText("Accounts"))
//            .check(matches(isDisplayed()));
//
//        onView(withText("Close"))
//            .check(matches(isDisplayed()));
//    }
//
//    /**
//     * This is the full test for the new installation of the app. Runs all the partial tests.
//     */
//    @Test
//    public void runAll() {
////        Intent intent = new Intent();
//
//
//        // First the tutorial should be shown.
//        tutorial();
//        onView(withText("Close"))
//            .perform(click());
//
//        changelog();
//
//        welcomeScreen();
//
//        assertTrue(true);
//    }
//
//}
