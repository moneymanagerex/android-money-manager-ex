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
package org.moneymanagerex.android.espresso;

import android.support.test.espresso.Espresso;
import android.support.test.espresso.action.ViewActions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.money.manager.ex.settings.LookFeelSettingsActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.moneymanagerex.android.testhelpers.UiTestHelpersEspresso;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

/**
 * Look and Feel preferences
 */
//@RunWith(AndroidJUnit4.class)
//public class SettingsLookFeelTests {
//    @Rule
//    public final ActivityTestRule<LookFeelSettingsActivity> activityRule =
//            new ActivityTestRule<>(LookFeelSettingsActivity.class);
//
//    @Test
//    public void activityOpens() {
//        onView(withText("Look & Feel"))
//            .check(matches(isDisplayed()));
//    }
//
//    @Test
//    public void changeTheme() {
//        onView(withText("Theme"))
//            .check(matches(isDisplayed()))
//            .perform(click());
//
//        onView(withText("Material Dark"))
//            .perform(click());
//
//        // TODO: 25/09/2015 confirm that the color has changed
//
//    }
//}
