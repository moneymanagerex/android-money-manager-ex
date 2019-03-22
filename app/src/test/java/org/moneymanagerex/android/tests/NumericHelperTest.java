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
import com.money.manager.ex.core.NumericHelper;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.moneymanagerex.android.testhelpers.TestApplication;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Numeric helper tests.
 */
@RunWith(RobolectricTestRunner.class)
public class NumericHelperTest {

    private NumericHelper _numericHelper;

    @Before
    public void setUp() throws Exception {
        Context context = RuntimeEnvironment.application.getApplicationContext();
        _numericHelper = new NumericHelper(context);

    }

    @After
    public void tearDown() throws Exception {
        _numericHelper = null;
    }

    @Test
    public void testIsNumeric() throws Exception {
        boolean actual = NumericHelper.isNumeric("3");
        assertTrue(actual);
    }

    @Test
    public void testTryParse() throws Exception {
        int actual = _numericHelper.tryParse("64");
        assertEquals(64, actual);
    }

    public void testGetNumberFormatted() throws Exception {

    }

    public void testGetNumberDecimal() throws Exception {

    }
}