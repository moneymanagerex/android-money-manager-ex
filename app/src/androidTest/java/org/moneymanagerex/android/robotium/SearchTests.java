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
//@RunWith(AndroidJUnit4.class)
//public class SearchTests
//        extends ActivityInstrumentationTestCase2<SearchActivity> {
//
//    private Solo solo;
//
//    public SearchTests() {
//        super(SearchActivity.class);
//    }
//
//    @Before
//    public void setUp() {
//        solo = UiTestHelpersRobotium.setUp(this);
//    }
//
//    @After
//    public void tearDown() throws Exception {
//        super.tearDown();
//
//        UiTestHelpersRobotium.tearDown(solo);
//    }
//
////    @Test
////    public void formOpens() {
////        assertThat(solo.getView(R.id.action_search).isShown()).isTrue();
////    }
//
//    /**
//     * This test illustrates another bug in Espresso. It is next to impossible to select a
//     * child menu in expandable list view, in this case a SubCategory.
//     * Let's see if it can be resolved with Robotium (comes to the rescue!)
//     */
//    @Test
//    public void searchForSubcategory() {
//        // Given
//
////        assertThat(solo.searchText("Select Category")).isTrue();
//
//        // When
//
//        solo.clickOnText("Select Category");
//
//        // Select category.
//        solo.clickOnText("Food");
////        solo.clickOnText("Dining out");
//        solo.clickOnView(solo.getView(R.id.selector, 5));
//
//        // Then
//
//        solo.waitForActivity(SearchActivity.class.getName());
////        assertThat(solo.searchText("Food : Dining out")).isTrue();
//    }
//}
