package org.moneymanagerex.android.tests;

import com.money.manager.ex.core.NumericHelper;

import junit.framework.TestCase;


/**
 * First unit test.
 * Created by Alen Siljak on 11/08/2015.
 */
public class NumericHelperTest extends TestCase {

    private NumericHelper _numericHelper;

    public void setUp() throws Exception {
        super.setUp();

        _numericHelper = new NumericHelper();

    }

    public void tearDown() throws Exception {

    }

    public void testIsNumeric() throws Exception {
        boolean actual = NumericHelper.isNumeric("3");
        assertTrue(actual);
    }

    public void testTryParse() throws Exception {
        assertTrue(false);
    }

    public void testGetNumberFormatted() throws Exception {

    }

    public void testGetNumberDecimal() throws Exception {

    }
}