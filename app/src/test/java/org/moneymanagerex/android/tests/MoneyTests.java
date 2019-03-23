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
package org.moneymanagerex.android.tests;

import com.money.manager.ex.BuildConfig;
import com.money.manager.ex.Constants;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.moneymanagerex.android.testhelpers.TestApplication;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.math.BigDecimal;

import info.javaperformance.money.Money;
import info.javaperformance.money.MoneyFactory;

import static org.junit.Assert.assertNotNull;


/**
 * Tests for the Money implementation
 */
@RunWith(RobolectricTestRunner.class)
public class MoneyTests {

    @Before
    public void setup() {
//        Context context = UnitTestHelper.getContext();

        // initialize support for activities (UI)
//        this.controller = UnitTestHelper.getController(MainActivity.class);
//        this.activity = UnitTestHelper.getActivity(this.controller);

        // initialize database
        // UnitTestHelper.setupContentProvider();
    }

    @After
    public void tearDown() {
        // Reset database instance between tests.
        // UnitTestHelper.teardownDatabase();

        // Destroy the activity controller.
//        this.controller.destroy();
    }

    @Test
    public void instantiation() {
        Money testObject = MoneyFactory.fromString("156");

        //assertThat(testObject).isNotNull();
        assertNotNull(testObject);
    }

    //@Test
    public void multiplicationWith100() {
        // Given
        Money testObject = MoneyFactory.fromString("2148.56");

        // When
        Money actual = testObject.multiply(100);

//        assertThat(actual.toString()).isEqualTo("214856");
    }

    /**
     * This is the first shot at replicating the bug in multiplication. However, everything
     * works well here.
     */
    //@Test
    public void multiplication() {
        // Given
        Money value = MoneyFactory.fromString("2184.4983599999996");
        double divisor = 7281.6612;

        // When
        Money actual = value.multiply(100).divide(divisor, 2);

        // Then
//        assertThat(actual.toString()).isEqualTo("30");
    }

    /**
     * This case demonstrates the bug in multiplication.
     */
    //@Test
    public void bugTry2() {
        // allocation is 30%
        double allocation = 30;
        // total value
        Money totalValue = MoneyFactory.fromString("7281.6612");
        // money value of 30% allocation
        double value = allocation * totalValue.toDouble() / 100;
        Money moneyValue = MoneyFactory.fromDouble(value);

        // calculate the percentage of the money value
        double currentAllocationD = moneyValue.multiply(100)
            .divide(totalValue.toDouble(), Constants.DEFAULT_PRECISION)
            .toDouble();

        Money currentAllocation = moneyValue.multiply(100)
            .divide(totalValue.toDouble(), Constants.DEFAULT_PRECISION);

        // it should be 30, as set initially.
//        assertThat(currentAllocationD).isEqualTo(30);
//        assertThat(currentAllocation.toString()).isEqualTo("30");
    }

    //@Test
    public void isZero() {
        // Given
        Money money = MoneyFactory.fromString("0");

        // When
        boolean actual = money.isZero();

        // Then
//        assertThat(actual).isTrue();
    }

    //@Test
    public void zeroBigDecimal() {
        // Given
        BigDecimal zero = new BigDecimal(0);
        Money money = MoneyFactory.fromBigDecimal(zero);

        // When
        boolean actual = money.isZero();

        // Then
//        assertThat(actual).isTrue();
    }

    //@Test
    public void isPositive() {
        // Given
        Money longMoney = MoneyFactory.fromString("3");
        Money decimalMoney = MoneyFactory.fromBigDecimal(new BigDecimal(3));

        // When

        // Then
//        assertThat(longMoney.toDouble() > 0).isTrue();
//        assertThat(longMoney.toDouble() < 0).isFalse();
//        assertThat(longMoney.isZero()).isFalse();
//
//        assertThat(decimalMoney.toDouble() > 0).isTrue();
//        assertThat(decimalMoney.toDouble() < 0).isFalse();
//        assertThat(decimalMoney.isZero()).isFalse();
    }
}
