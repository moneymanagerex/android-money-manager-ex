package test.java.org.moneymanagerex.android.tests;

import android.test.mock.MockContext;

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

        MockContext context = new MockContext();
        _numericHelper = new NumericHelper(context);

    }

    public void tearDown() throws Exception {
        _numericHelper = null;
    }

    public void testIsNumeric() throws Exception {
        boolean actual = NumericHelper.isNumeric("3");
        assertTrue(actual);
    }

    public void testTryParse() throws Exception {
        int actual = _numericHelper.tryParse("64");
        assertEquals(64, actual);
    }

    public void testGetNumberFormatted() throws Exception {

    }

    public void testGetNumberDecimal() throws Exception {

    }
}