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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.moneymanagerex.android.tests;

import com.money.manager.ex.BuildConfig;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import info.javaperformance.money.Money;
import info.javaperformance.money.MoneyFactory;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the Money implementation
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class MoneyTests {

    @Before
    public void setup() {
//        Context context = UnitTestHelper.getContext();

        // initialize support for activities (UI)
//        this.controller = UnitTestHelper.getController(MainActivity.class);
//        this.activity = UnitTestHelper.getActivity(this.controller);

        // initialize database
        // UnitTestHelper.initializeContentProvider();
    }

    @After
    public void tearDown() {
        // Reset database instance between tests.
        // UnitTestHelper.resetDatabase();

        // Destroy the activity controller.
//        this.controller.destroy();
    }

    @Test
    public void instantiation() {
        Money testObject = MoneyFactory.fromString("156");

        assertThat(testObject).isNotNull();
    }

    @Test
    public void multiplicationWith100() {
        // Given
        Money testObject = MoneyFactory.fromString("2148.56");

        // When
        Money actual = testObject.multiply(100);

        assertThat(actual.toString()).isEqualTo("214856");
    }

    @Test
    public void bugInMultiplication() {
        // Given
        Money value = MoneyFactory.fromString("2184.4983599999996");
        double divisor = 7281.6612;

        // When
        Money actual = value.multiply(100).divide(divisor, 2);

        // Then
        assertThat(actual.toString()).isEqualTo("30");
    }

    /**
     * Exception test example.
     */
    @Test(expected=RuntimeException.class)
    public void throwsException() {
        throw new RuntimeException("bang!");
    }
}
