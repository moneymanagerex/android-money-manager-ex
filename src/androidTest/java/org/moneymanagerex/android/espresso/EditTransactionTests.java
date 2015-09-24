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
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.moneymanagerex.android.espresso;

import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.money.manager.ex.home.MainActivity;
import com.money.manager.ex.transactions.EditTransactionActivity;

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
 * Espresso version of the tests for the Main Activity.
 * The tests start with a fresh copy of preferences.
 *
 * Created by Alen Siljak on 24/09/2015.
 */
@RunWith(AndroidJUnit4.class)
public class EditTransactionTests {

    @Rule
    public final ActivityTestRule<EditTransactionActivity> activityRule =
            new ActivityTestRule<>(EditTransactionActivity.class);
    private UiTestHelpersEspresso helper;

    @Before
    public void setUp() {
        this.helper = new UiTestHelpersEspresso();

    }

    @After
    public void tearDown() {
        this.helper = null;
    }

    @Test
    public void run() {
        onView(withText("OK"))
            .check(matches(isDisplayed()));

    }

}
