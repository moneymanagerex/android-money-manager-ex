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
package org.moneymanagerex.android.tests;

import android.content.Intent;

import com.money.manager.ex.BuildConfig;
import com.money.manager.ex.R;
import com.money.manager.ex.home.MainActivity;
import com.money.manager.ex.transactions.EditTransactionActivity;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

//import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertThat;
import static org.robolectric.Shadows.shadowOf;


/**
 * Test the MainActivity.
 *
 * Created by Alen Siljak on 22/09/2015.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk=21)
public class MainActivityTests {

    /**
     * This does not work!
     */
    @Test
    public void testRunActivity() {
//        WelcomeActivity activity = Robolectric.setupActivity(WelcomeActivity.class);
//        activity.findViewById(R.id.login).performClick();
//        Intent expectedIntent = new Intent(activity, WelcomeActivity.class);
//        assertThat(shadowOf(activity).getNextStartedActivity()).isEqualTo(expectedIntent);

        MainActivity activity = Robolectric.setupActivity(MainActivity.class);
        activity.findViewById(R.id.fab).performClick();

        Intent expectedIntent = new Intent(activity, EditTransactionActivity.class);
//        assertThat(shadowOf(activity).getNextStartedActivity()).isEqualTo(expectedIntent);
//        String blah = shadowOf(activity).getNextStartedActivity().toString();
        Matcher<Intent> matcher = new Matcher<Intent>() {
            @Override
            public boolean matches(Object item) {
//                return false;
                return this.equals(item);
            }

            @Override
            public void describeMismatch(Object item, Description mismatchDescription) {

            }

            @Override
            public void _dont_implement_Matcher___instead_extend_BaseMatcher_() {

            }

            @Override
            public void describeTo(Description description) {

            }
        };

        assertThat(shadowOf(activity).getNextStartedActivity(), matcher);

    }
}
