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

import android.test.AndroidTestCase;

import com.money.manager.ex.investment.YqlSecurityPriceUpdater;

import junit.framework.Assert;

import java.util.Arrays;
import java.util.List;

/**
 * Tests for YQL security downloader.
 * Created by Alen Siljak on 20/08/2015.
 */
public class YqlSecurityPriceUpdaterTest
        extends AndroidTestCase {

    private YqlSecurityPriceUpdater _testObject;

    public void setUp() throws Exception {
        super.setUp();

        _testObject = new YqlSecurityPriceUpdater(getContext(), null);
    }

    public void tearDown() throws Exception {
        _testObject = null;
    }

    public void testGetYqlQueryFor() throws Exception {
        final String source = "yahoo.finance.quote";
        List<String> fields = Arrays.asList("symbol", "LastTradePriceOnly", "LastTradeDate", "Currency");
        List<String> symbols = Arrays.asList("YHOO", "AAPL", "GOOG", "MSFT");

        String expected = "select symbol,LastTradePriceOnly,LastTradeDate,Currency from yahoo.finance.quote where symbol in (\"YHOO\",\"AAPL\",\"GOOG\",\"MSFT\")";

        String actual = _testObject.getYqlQueryFor(source, fields, symbols);

        Assert.assertEquals(expected, actual);
    }
}