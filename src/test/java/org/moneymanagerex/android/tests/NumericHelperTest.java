package org.moneymanagerex.android.tests;

import com.money.manager.ex.core.NumericHelper;

import junit.framework.TestCase;

import org.junit.Before;

/**
 * First unit test.
 * Created by Alen Siljak on 11/08/2015.
 */
public class NumericHelperTest extends TestCase {

    private NumericHelper _numericHelper;

    @Before
    public void setup() {
        _numericHelper = new NumericHelper();
    }

    public void testIsNumeric() throws Exception {
        boolean actual = NumericHelper.isNumeric("3");
        assertTrue(actual);
    }

    public void testTryParse() throws Exception {

    }

    public void testGetNumberFormatted() throws Exception {

    }

    public void testGetNumberDecimal() throws Exception {

    }
}