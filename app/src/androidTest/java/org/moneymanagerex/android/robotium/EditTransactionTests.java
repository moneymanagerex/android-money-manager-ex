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

import com.money.manager.ex.Constants;
import com.money.manager.ex.R;
import com.money.manager.ex.transactions.CheckingTransactionEditActivity;
import com.robotium.solo.Solo;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.moneymanagerex.android.testhelpers.UiTestHelpersRobotium;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Robotium test for Edit Transaction activity.
 */
//@RunWith(AndroidJUnit4.class)
//public class EditTransactionTests
//    extends ActivityInstrumentationTestCase2<CheckingTransactionEditActivity> {
//
//  private Solo solo;
//
//  public EditTransactionTests() {
//    super(CheckingTransactionEditActivity.class);
//  }
//
//    @Before
//    public void setUp() throws Exception {
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
//    @Test
//    public void testActivityExists() {
//        CheckingTransactionEditActivity activity = getActivity();
//        assertNotNull(activity);
//    }
//
////    @Test
////    public void testStatusChange() {
////        solo.waitForActivity(CheckingTransactionEditActivity.class.getSimpleName());
////
////        boolean spinnerFound = solo.searchText("None");
////        assertThat(spinnerFound).isTrue();
////
////        solo.pressSpinnerItem(0, 1);
////        assertThat(solo.isSpinnerTextSelected(0, "Reconciled"));
////
////        solo.pressSpinnerItem(0, 1);
////        assertThat(solo.isSpinnerTextSelected(0, "Void"));
////
////        solo.pressSpinnerItem(0, -2);
////        assertThat(solo.isSpinnerTextSelected(0, "None"));
////
////        solo.pressSpinnerItem(0, 4);
////        assertThat(solo.isSpinnerTextSelected(0, "Duplicate"));
////
////        solo.pressSpinnerItem(0, -1);
////        assertThat(solo.isSpinnerTextSelected(0, "Follow up"));
////    }
//
//    @Test
//    public void changeDate() {
//        // Given
//        // The date is today.
//        Calendar calendar = Calendar.getInstance();
//        Date today = calendar.getTime();
//        Locale defaultLocale = Locale.ENGLISH;
//        int month = calendar.get(Calendar.MONTH);
//        String monthDisplay = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, defaultLocale);
//        String yearDisplay = calendar.getDisplayName(Calendar.YEAR, Calendar.LONG, defaultLocale);
//        int year = calendar.get(Calendar.YEAR);
//        SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.LONG_DATE_PATTERN, defaultLocale);
//        String todayFormatted = dateFormat.format(today);
//
//        // expected date
//        int expectedDay = 15;
//        calendar.set(Calendar.DAY_OF_MONTH, expectedDay);
//        Date expectedDate = calendar.getTime();
//        String expectedDateDisplay = dateFormat.format(expectedDate);
//
//        RobotoTextView dateView = solo.getView(R.id.textViewDate);
//        String displayedDate = dateView.getText().toString();
//
////        assertThat(displayedDate).isEqualTo(todayFormatted);
//
//
//        // When
//        // changing the date to 15th
//        solo.clickOnView(dateView);
//        solo.waitForDialogToOpen(1000);
//        System.out.println("binaryDialog open");
//
////        assertThat(solo.searchText(month + " " + year)).isTrue();
//        solo.clickOnText(Integer.toString(expectedDay));
//
//        // todo: this also does not work!!! Can't select a date.
////        solo.setDatePicker(0, year, month, expectedDay);
//
//        solo.waitForDialogToClose(2000);
//
//        // Then
//        // The displayed date should show the 15th of this month
//        String actualDate = dateView.getText().toString();
//
////        assertThat(actualDate).isEqualTo(expectedDateDisplay);
//    }
//}