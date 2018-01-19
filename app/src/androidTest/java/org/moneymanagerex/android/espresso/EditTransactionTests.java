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

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.money.manager.ex.R;
import com.money.manager.ex.transactions.CheckingTransactionEditActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Calendar;
import java.util.Locale;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalToIgnoringCase;

/**
 * Edit transaction tests.
 * This test writes into the database! Therefore, make sure to run it on a test device or with a
 * test database only!!!
 */
//@RunWith(AndroidJUnit4.class)
//public class EditTransactionTests {
//
//    @Rule
//    public final ActivityTestRule<CheckingTransactionEditActivity> activityRule =
//            new ActivityTestRule<>(CheckingTransactionEditActivity.class);
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
//    public void activityOpens() {
//        onView(withText("Cancel"))
//            .check(matches(isDisplayed()))
//            .perform(click());
//    }
//
//    @Test
//    public void enterAmount() {
//        onView(withId(R.id.textViewAmount))
//            .check(matches(isDisplayed()))
//            .perform(click());
//
//        // this opens numeric input
//        onView(withId(R.id.buttonKeyEqual))
//            .check(matches(isDisplayed()));
//
//        // enter 15.26
//        onView(withId(R.id.buttonKeyNum1))
//            .perform(click());
//        onView(withId(R.id.buttonKeyNum5))
//            .perform(click());
//        onView(withId(R.id.buttonKeyNumDecimal))
//            .perform(click());
//        onView(withId(R.id.buttonKeyNum2))
//            .perform(click());
//        onView(withId(R.id.buttonKeyNum6))
//            .perform(click());
//
//        // press OK
//        onView(withText("OK"))
//            .check(matches(isDisplayed()))
//            .perform(click());
//
//        // The amount is displayed in the edit transaction.
////        final String expectedAmount = "$ 15.26";
////        onView(withText(expectedAmount))
////                .check(matches(isDisplayed()));
////        onView(withId(R.id.textViewAmount))
////                .check(matches(withText(expectedAmount)));
//        onView(withId(R.id.textViewAmount))
//                .check(matches(withText(containsString("15.26"))));
//    }
//
//    @Test
//    public void enterWithdrawal() {
//        enterAmount();
//
//        selectPayee();
//
//        selectCategory();
//
//        // TODO: change date
//
//        // TODO: 25/09/2015 update
//        // TODO: 25/09/2015 check that the transaction exists with today's date
//
//        // not finished and probably won't be as we can't select the correct date.
////        assertThat(false).isTrue();
//    }
//
//    @Test
//    public void canRemovePayee() {
//        // select payee
//        selectPayee();
//
//        // remove payee
//        onView(withId(R.id.removePayeeButton))
//                .perform(click());
//
//        // confirm the payee field is empty
//        onView(withId(R.id.textViewPayee))
//            .check(matches(withText("")));
//    }
//
//    @Test
//    public void selectPayee() {
//        final String payeeName = "Falafel";
//
//        onView(withId(R.id.textViewPayee))
//                .perform(click());
//
//        // Payees activity opens, PayeeActivity
//        onView(withId(R.id.fab))
//                .check(matches(isDisplayed()));
//        onView(withText(payeeName))
//                .perform(click());
//
//        // Payee field contains the name now
//        onView(withId(R.id.textViewPayee))
//                .check(matches(withText(payeeName)));
//    }
//
//    @Test
//    public void selectCategory() {
//        final String category = "Dining Out";
//
//        onView(withId(R.id.textViewCategory))
//                .perform(click());
//
//        // Categories activity opens
//        onView(withId(R.id.fab))
//                .check(matches(isDisplayed()));
//        onView(withText("Food"))
//                .check(matches(isDisplayed()))
//                .perform(click());
//        onView(withText(category))
//                .perform(click());
//
//        // Payee field contains the name now
//        onView(withId(R.id.textViewCategory))
//                .check(matches(withText(category)));
//    }
//
//    /**
//     * Another shortcoming of Espresso:
//     * Can't change the date programmatically. :(
//     */
////    @Test
//    public void changeDate() {
//        Calendar calendar = Calendar.getInstance();
////        Date today = calendar.getTime();
//
//        onView(withId(R.id.textViewDate))
//                .check(matches(isDisplayed()))
//                .perform(click());
//
//        // date selector opens
//        // today's name is displayed on top of calendar picker
//        String dayName = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.ENGLISH);
//        onView(withText(equalToIgnoringCase(dayName)))
//                .check(matches(isDisplayed()));
////        onView(withText("17"))
////                .perform(click());
//        // todo: need to find how to change the date.
////        onView(withClassName(equalTo(DatePickerDialog.class.getName())))
////            .perform(setDate(
////                    calendar.get(Calendar.YEAR),
////                    calendar.get(Calendar.MONTH),
////                    calendar.get(Calendar.DATE)
////            ));
//        // com.fourmob.datetimepicker.date.DayPickerView
//        onView(withText("Done"))
//                .check(matches(isDisplayed()))
//                .perform(click());
//
//        String longDate = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.ENGLISH);
//
//        onView(withId(R.id.textViewDate))
//                .check(matches(withText(containsString(longDate))));
//    }
//}
