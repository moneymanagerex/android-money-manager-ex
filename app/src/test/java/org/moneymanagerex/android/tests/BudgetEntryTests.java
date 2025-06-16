package org.moneymanagerex.android.tests;

import com.money.manager.ex.budget.BudgetPeriodEnum;
import com.money.manager.ex.domainmodel.BudgetEntry;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.*;

import android.content.ContentValues;

@RunWith(RobolectricTestRunner.class)
public class BudgetEntryTests {
    private BudgetEntry budgetEntry;

    void setUp() {
        // Initialize BudgetEntry before test
        budgetEntry = new BudgetEntry();
        assertNotNull(budgetEntry);
    }

    @Test
    public void testDefaultConstructor() {
        setUp();

        assertEquals(BudgetPeriodEnum.NONE.getDisplayName(), budgetEntry.getPeriod());
        assertTrue(budgetEntry.getActive());
        assertSame(budgetEntry.getPeriodEnum(), BudgetPeriodEnum.NONE);
        assertEquals(0.0, budgetEntry.getAmount(), 0.0);
        assertEquals(0.0, budgetEntry.getYearlyAmount(), 0.0);
        assertEquals(0.0, budgetEntry.getMonthlyAmount(), 0.0);
    }

    @Test
    public void testInactive() {
        setUp();

        budgetEntry.setActive(false);
        assertFalse(budgetEntry.getActive());
    }

    @Test
    public void testConstructorWithContentValues() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(BudgetEntry.BUDGETENTRYID, 100L);
        contentValues.put(BudgetEntry.AMOUNT, 500.50);
        contentValues.put(BudgetEntry.PERIOD, BudgetPeriodEnum.DAILY.getDisplayName());

        BudgetEntry entryFromContentValues = new BudgetEntry(contentValues);

        assertTrue(entryFromContentValues.getActive());
        assertEquals(100L, (long)entryFromContentValues.getBudgetEntryId());
        assertEquals(500.50,  entryFromContentValues.getAmount(), 0.0);
        assertEquals(BudgetPeriodEnum.DAILY.getDisplayName(), entryFromContentValues.getPeriod());
        assertEquals(BudgetPeriodEnum.DAILY, entryFromContentValues.getPeriodEnum());
        assertEquals((500.50 * 365), entryFromContentValues.getYearlyAmount(), 0);
        assertEquals((500.50 * 365 / 12), entryFromContentValues.getMonthlyAmount(), 0);
    }

    @Test
    public void testConstructorWithParam() {
        setUp();
        budgetEntry.setBudgetEntryId(100L);
        budgetEntry.setAmount(500.50);
        budgetEntry.setPeriod(BudgetPeriodEnum.DAILY.getDisplayName());

        assertTrue(budgetEntry.getActive());
        assertEquals(100L, (long)budgetEntry.getBudgetEntryId());
        assertEquals(500.50, budgetEntry.getAmount(), 0 );
        assertEquals(BudgetPeriodEnum.DAILY.getDisplayName(), budgetEntry.getPeriod());
        assertEquals(BudgetPeriodEnum.DAILY, budgetEntry.getPeriodEnum());
        assertEquals((500.50 * 365), budgetEntry.getYearlyAmount(),0);
        assertEquals((500.50 * 365 / 12), budgetEntry.getMonthlyAmount(),0);
    }

    @Test
    public void testSetPeriodWithInvalidString() {
        setUp();
        String invalidPeriod = "INVALID_PERIOD";
        budgetEntry.setPeriod(invalidPeriod);
        assertEquals(invalidPeriod, budgetEntry.getPeriod());
        assertEquals(BudgetPeriodEnum.NONE, budgetEntry.getPeriodEnum()); // NONE is default if invalid period
    }

}
