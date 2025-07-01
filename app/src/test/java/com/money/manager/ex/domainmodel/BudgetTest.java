package com.money.manager.ex.domainmodel;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;


@RunWith(RobolectricTestRunner.class)
public class BudgetTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testGetPrimaryKeyColumn() {
        // simple not null
        Budget emptyBudget = new Budget();
        assertNotNull(emptyBudget.getPrimaryKeyColumn());

        Budget budget = new Budget();
        budget.setName("2025-06");
        assertNotNull(budget.getPrimaryKeyColumn());

        budget = new Budget();
        budget.setName("2025");
        assertNotNull(budget.getPrimaryKeyColumn());
    }

    @Test
    public void testTestName() {

        // simple not null
        Budget emptyBudget = new Budget();
        assertNotNull(emptyBudget.getPrimaryKeyColumn());

        Budget budget = new Budget();
        budget.setName("2025-06");
        assertTrue(budget.getName().equals("2025-06"));

        budget = new Budget();
        budget.setName("2025");
        assertTrue(budget.getName().equals("2025"));

    }

    @Test
    public void testIsMonthlyBudget() {
        // simple not null
        Budget emptyBudget = new Budget();
        assertFalse(emptyBudget.isMonthlyBudget());

        Budget budget = new Budget();
        budget.setName("2025-06");
        assertTrue(emptyBudget.isMonthlyBudget());

        budget = new Budget();
        budget.setName("2025");
        assertFalse(emptyBudget.isMonthlyBudget());
    }

    @Test
    public void testStaticIsMonthlyBudget() {
        // simple not null
        assertTrue(Budget.isMonthlyBudget("2025-06"));
        assertFalse(Budget.isMonthlyBudget("2025"));
    }

    @Test
    public void testGetYear() {
        Budget emptyBudget = new Budget();
        assertNull(emptyBudget.getYear());

        Budget budget = new Budget();
        budget.setName("2025-06");
        assertTrue(budget.getYear() == 2025);

        budget = new Budget();
        budget.setName("2025");
        assertTrue(budget.getYear() == 2025);
    }

    @Test
    public void testGetMonth() {
        Budget emptyBudget = new Budget();
        assertNull(emptyBudget.getYear());

        Budget budget = new Budget();
        budget.setName("2025-06");
        assertTrue(budget.getMonth() == 6);

        budget = new Budget();
        budget.setName("2025");
        assertTrue(budget.getMonth() == 0);
    }
}