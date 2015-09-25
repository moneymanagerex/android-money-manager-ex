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
import com.money.manager.ex.investment.YqlQueryGenerator;
import com.money.manager.ex.investment.YqlSecurityPriceUpdater;
import com.money.manager.ex.investment.YqlSecurityPriceUpdaterRetrofit;

import junit.framework.Assert;

import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowApplication;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static com.jayway.awaitility.Awaitility.await;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Tests for YQL security downloader.
 *
 * Created by Alen Siljak on 20/08/2015.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class YqlSecurityPriceUpdaterTest {

    private Context context;
    private YqlSecurityPriceUpdaterRetrofit testObject;

    @Before
    public void setUp() throws Exception {
        this.context = RuntimeEnvironment.application;

        this.testObject = new YqlSecurityPriceUpdaterRetrofit(this.context, null);
    }

    @After
    public void tearDown() throws Exception {
        this.context = null;
        testObject = null;
    }

    @Test
    public void getYqlQuery() {
        YqlQueryGenerator generator = new YqlQueryGenerator();
        final String source = generator.source;
        List<String> fields = Arrays.asList("symbol", "LastTradePriceOnly", "LastTradeDate", "Currency");
        List<String> symbols = Arrays.asList("YHOO", "AAPL", "GOOG", "MSFT");

        String expected = "select symbol,LastTradePriceOnly,LastTradeDate,Currency from yahoo.finance.quotes where symbol in (\"YHOO\",\"AAPL\",\"GOOG\",\"MSFT\")";

        String actual = generator.getQueryFor(source, fields, symbols);

        assertEquals(expected, actual);
    }

    /**
     * Need to finish this.
     */
    @Test
    public void testParseResults() {
//        String symbol = "EL4X.DE";
//        // todo: get a proper result
//        final String jsonResult = "{\"query\":{\"count\":0,\"created\":\"2015-09-23T10:01:14Z\",\"lang\":\"en-US\",\"results\":null}}";
//        PriceUpdatedListener listener = new PriceUpdatedListener();
//        testObject = getTestObjectWithListener(listener);
//
//        // invoke parser
//        testObject.onContentDownloaded(jsonResult);
//        // get the results
//        assertEquals(listener.symbol, symbol);
    }

    /**
     * Incomplete.
     * Test fetching prices using Retrofit library.
     */
    @Test
    public void downloadPriceWithRetrofit() {
        List<String> symbols = getSymbol();

        this.testObject.downloadPrices(symbols);

        Robolectric.flushBackgroundThreadScheduler();
        ShadowApplication.runBackgroundTasks();

//        await()
//            .atMost(15, TimeUnit.SECONDS)
//            .until(responseReceived());

//        String actual = this.testObject.response;

//        assertTrue(StringUtils.isNotEmpty(actual));

        // todo: need to find a way to get async result.
        assertTrue(false);
    }

    // Helpers

//    private Callable<Boolean> responseReceived() {
//        return new Callable<Boolean>() {
//            @Override
//            public Boolean call() throws Exception {
////                return null;
//            return YqlSecurityPriceUpdaterTest.this.testObject.response != null;
//            }
//        };
//    }

    private List<String> getSymbols() {
        List<String> symbols = new ArrayList<>();

        symbols.add("EL4X.DE");

        return symbols;
    }

    private List<String> getSymbol() {
        List<String> symbols = new ArrayList<>();

        symbols.add("EL4X.DE");

        return symbols;
    }

    private YqlSecurityPriceUpdaterRetrofit getTestObjectWithListener(IPriceUpdaterFeedback listener) {
        testObject = new YqlSecurityPriceUpdaterRetrofit(this.context, listener);
        return testObject;
    }
}