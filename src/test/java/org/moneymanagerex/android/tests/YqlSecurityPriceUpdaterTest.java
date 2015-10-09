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

import android.content.Context;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.money.manager.ex.BuildConfig;
import com.money.manager.ex.investment.IPriceUpdaterFeedback;
import com.money.manager.ex.investment.IYqlService;
import com.money.manager.ex.investment.YqlQueryGenerator;
import com.money.manager.ex.investment.YqlSecurityPriceUpdaterRetrofit;

import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.moneymanagerex.android.testhelpers.FakePriceListener;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import info.javaperformance.money.MoneyFactory;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.http.Query;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.assertj.core.api.Assertions.assertThat;

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
     * Incomplete.
     * Test fetching prices using Retrofit library.
     */
    @Test
    public void downloadPriceWithRetrofit() {
        // Listener
        FakePriceListener listener = new FakePriceListener();
        this.testObject = getTestObjectWithListener(listener);

        List<String> symbols = getSymbol();

        JsonArray quote = new JsonArray();
        quote.add(getPriceObject("EL4X.DE", "36.95", "EUR", "09/29/2015"));

        // todo: IYqlService fakeYql = getFakeYqlService(quote);
//        this.testObject.setService(fakeYql);

        // make the call
        this.testObject.downloadPrices(symbols);

//        Robolectric.flushBackgroundThreadScheduler();
//        ShadowApplication.runBackgroundTasks();
//        await();
//            .atMost(15, TimeUnit.SECONDS)
//            .until(responseReceived());

        assertTrue(StringUtils.isNotEmpty(listener.symbol));
        assertEquals("EL4X.DE", listener.symbol);

        assertTrue(listener.price != null);
        assertEquals(MoneyFactory.fromString("36.95"), listener.price);

        assertTrue(listener.date != null);

        Calendar date = Calendar.getInstance();
        date.set(Calendar.DATE, 29);
        date.set(Calendar.MONTH, 9-1);
        date.set(Calendar.YEAR, 2015);
        date.set(Calendar.HOUR_OF_DAY, 0);
        date.set(Calendar.MINUTE, 0);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);
        assertThat(listener.date).isEqualTo(date.getTime());
    }

    // Helpers

    private JsonObject getPriceObject(String symbol, String price, String currency, String date) {
        JsonObject jsonPrice = new JsonObject();
        jsonPrice.addProperty("symbol", symbol);
        jsonPrice.addProperty("LastTradePriceOnly", price);
        jsonPrice.addProperty("Currency", currency);
        jsonPrice.addProperty("LastTradeDate", date);
        return jsonPrice;
    }

    // todo: implement IYqlService in a separate, mock class.
//    private IYqlService getFakeYqlService(JsonArray quote) {
//        JsonObject results = new JsonObject();
//        results.add("quote", quote);
//
//        JsonObject query = new JsonObject();
//        query.add("results", results);
//
//        final JsonObject fakeElement = new JsonObject();
//        fakeElement.add("query", query);
//        final Response<JsonElement> fakeResponse = Response.success((JsonElement) fakeElement);
//
//        IYqlService fakeYql = new IYqlService() {
//            @Override
//            public Call<JsonElement> getPrices(@Query("q") String query) {
//                return new Call<JsonElement>() {
//                    @Override
//                    public Response<JsonElement> execute() throws IOException {
//                        return null;
//                    }
//
//                    @Override
//                    public void enqueue(Callback<JsonElement> callback) {
//                        callback.onResponse(fakeResponse, retrofit);
//                    }
//
//                    @Override
//                    public void cancel() {
//
//                    }
//
//                    @Override
//                    public Call<JsonElement> clone() {
//                        return null;
//                    }
//                };
//            }
//        };
//
//        return fakeYql;
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