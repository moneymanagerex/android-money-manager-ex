package org.moneymanagerex.android.tests;

import android.test.AndroidTestCase;
import android.util.Log;

import com.money.manager.ex.investment.YqlSecurityPriceUpdater;

import junit.framework.Assert;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Tests for YQL security downloader.
 * Created by Alen Siljak on 20/08/2015.
 */
public class YqlSecurityPriceUpdaterTest extends AndroidTestCase {

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