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

import android.content.Context;

import com.money.manager.ex.BuildConfig;
import com.money.manager.ex.investment.IPriceUpdaterFeedback;
import com.money.manager.ex.investment.YqlSecurityPriceUpdater;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.moneymanagerex.android.testhelpers.PriceUpdatedListener;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Tests for YQL security downloader.
 *
 * Created by Alen Siljak on 20/08/2015.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class YqlSecurityPriceUpdaterTest {

    private Context context;
    private YqlSecurityPriceUpdater _testObject;

    @Before
    public void setUp() throws Exception {
        this.context = RuntimeEnvironment.application;

        _testObject = new YqlSecurityPriceUpdater(this.context, null);
    }

    @After
    public void tearDown() throws Exception {
        _testObject = null;
    }

    @Test
    public void testGetYqlQueryFor() throws Exception {
        final String source = "yahoo.finance.quote";
        List<String> fields = Arrays.asList("symbol", "LastTradePriceOnly", "LastTradeDate", "Currency");
        List<String> symbols = Arrays.asList("YHOO", "AAPL", "GOOG", "MSFT");

        String expected = "select symbol,LastTradePriceOnly,LastTradeDate,Currency from yahoo.finance.quote where symbol in (\"YHOO\",\"AAPL\",\"GOOG\",\"MSFT\")";

        String actual = _testObject.getYqlQueryFor(source, fields, symbols);

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testParseResults() {
        String symbol = "EL4X.DE";
        // todo: get a proper result
        final String jsonResult = "{\"query\":{\"count\":0,\"created\":\"2015-09-23T10:01:14Z\",\"lang\":\"en-US\",\"results\":null}}";
        PriceUpdatedListener listener = new PriceUpdatedListener();
        _testObject = getTestObjectWithListener(listener);

        // invoke parser
        _testObject.onContentDownloaded(jsonResult);
        // get the results
        assertEquals(listener.symbol, symbol);
    }

    /**
     * Not finished. Need to find a way to get a result from an async task.
     */
    @Test
    public void testPriceDownload() {
        List<String> symbols = getSymbols();
        //IPriceUpdaterFeedback listener =

        _testObject.updatePrices(symbols);

        // todo: get/test the results
        Robolectric.flushBackgroundThreadScheduler();


    }

    // Helpers

    private List<String> getSymbols() {
        List<String> symbols = new ArrayList<>();

        symbols.add("EL4X.DE");

        return symbols;
    }

    private YqlSecurityPriceUpdater getTestObjectWithListener(IPriceUpdaterFeedback listener) {
        _testObject = new YqlSecurityPriceUpdater(this.context, listener);
        return _testObject;
    }
}