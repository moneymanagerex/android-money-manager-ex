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

import android.content.Context;

import com.money.manager.ex.BuildConfig;
import com.money.manager.ex.currency.CurrencyService;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.moneymanagerex.android.testhelpers.TestApplication;
import org.moneymanagerex.android.testhelpers.UnitTestHelper;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.*;

/**
 * Test Currency Service
 */
@RunWith(RobolectricTestRunner.class)
public class CurrencyServiceTests {

    private CurrencyService testObject;

    @Before
    public void setup() {
        // initialize database
        UnitTestHelper.setupContentProvider();

        Context context = UnitTestHelper.getContext();
        testObject = new CurrencyService(context);
    }

    @After
    public void tearDown() {
        testObject = null;

        // Reset database instance between tests.
        //UnitTestHelper.teardownDatabase();
    }

    @Test
    public void instantiation() {
        assertThat(testObject, notNullValue());
    }

//    @Test
//    public void fetchingIdsBySymbol() {
//        // Given
//
//        // When
//        Integer id = testObject.getIdForCode("EUR");
//
//        // Then
//        assertThat(id).isNotNull();
//        assertThat(id).isEqualTo(2);
//    }
//
//    @Test
//    public void fetchingSymbolById() {
//        String symbol = testObject.getSymbolFor(2);
//
//        assertThat(symbol).isNotNull();
//        assertThat(symbol).isEqualTo("EUR");
//    }

//    @Test
//    public void gettingDefaultCurrency() {
//        // Given
//
//        // When
//        String actual = this.testObject.getBaseCurrencyCode();
//
//        // Then
//        //assertThat(actual).isEqualTo("EUR");
//        assertThat(actual).isNotEmpty();
//    }
}
