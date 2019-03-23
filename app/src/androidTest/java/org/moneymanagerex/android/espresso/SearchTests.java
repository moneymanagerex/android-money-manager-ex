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

import android.support.test.rule.ActivityTestRule;

import com.money.manager.ex.R;
import com.money.manager.ex.search.SearchActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withHint;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

/**
 * This is not operational because it is not possible to select a subcategory - a child item
 * in expandable list view.
 * There is the correct test in Robotium test suite.
 *
 * Various Search activity tests.
 */
//public class SearchTests {
//    @Rule
//    public final ActivityTestRule<SearchActivity> activityRule =
//        new ActivityTestRule<>(SearchActivity.class);
//
//    @Before
//    public void setUp() {
//    }
//
//    @After
//    public void tearDown() {
//    }
//
//    @Test
//    public void formOpens() {
////        Espresso.onView(allOf(withText("Search")))
//        onView(withId(R.id.action_search))
//            .check(matches(isDisplayed()));
//    }
//
//    /**
//     * This test illustrates another bug in Espresso. It is next to impossible to select a
//     * child menu in expandable list view, in this case a SubCategory.
//     */
//    @Test
//    public void searchForSubcategory() {
//        formOpens();
//
//        onView(withHint("Select Category"))
//            .check(matches(isDisplayed()))
//            .perform(click());
//
//        onView(withText("Food"))
//            .check(matches(isDisplayed()))
//            .perform(click());
//
////        onData(allOf(is(instanceOf(Category.class))))
////            .atPosition(5)
////            .onChildView(withId(R.id.selector))
////            .perform(click());
//
//        formOpens();
//
//        onView(withHint("Select Category"))
//            .check(matches(isDisplayed()))
//            .check(matches(withText("Food : Dining out")));
//    }
//}
