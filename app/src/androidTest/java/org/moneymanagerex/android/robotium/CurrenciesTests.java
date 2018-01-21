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

import com.money.manager.ex.currency.list.CurrencyListActivity;
import com.robotium.solo.Solo;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.moneymanagerex.android.testhelpers.UiTestHelpersRobotium;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test sample class. Use as a template for other tests.
 */
//@RunWith(AndroidJUnit4.class)
//public class CurrenciesTests
//        extends ActivityInstrumentationTestCase2<CurrencyListActivity> {
//
//    private Solo solo;
//
//    public CurrenciesTests() {
//        super(CurrencyListActivity.class);
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
////        assertThat(solo.waitForActivity(getActivity().getClass().getSimpleName())).isTrue();
////    }
//
////    @Test
////    public void canCreateAndDeleteCurrency() {
////        // Given
////
////        UiTestHelpersRobotium robot = new UiTestHelpersRobotium(solo);
////        final String accountName = "Test Currency";
////
////        // When
////
////        robot.clickOnFloatingButton();
////
////        solo.waitForDialogToOpen();
////        EditText editText = solo.getEditText(0);
////        solo.enterText(editText, accountName);
////
////        // select symbol
////        solo.pressSpinnerItem(0, 2);
////        assertThat(solo.isSpinnerTextSelected(0, "AFN"));
////
////        robot.clickDone();
////        solo.waitForDialogToClose();
////
////        // delete
//////        solo.clickLongOnText(accountName);
////        solo.clickOnText(accountName);
////        solo.clickOnText("Delete");
////        solo.clickOnText("OK");
////
////        // Then
////
////        assertThat(solo.searchText(accountName)).isFalse();
////    }
//
//    @Test
//    public void getNotificationForCurrencyThatCanNotBeDeleted() {
//        solo.clickOnText("Australian Dollar");
//        solo.clickOnText("Delete");
//
//        solo.waitForDialogToOpen();
//
////        assertThat(solo.searchText("can not be deleted")).isTrue();
//
//        solo.clickOnText("OK");
//    }
//
//}
