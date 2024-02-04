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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import com.money.manager.ex.R;
import com.money.manager.ex.core.DefinedDateRange;
import com.money.manager.ex.core.DefinedDateRangeName;
import com.money.manager.ex.core.DefinedDateRanges;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

/**
 * Test Defined Date Ranges.
 */
@RunWith(RobolectricTestRunner.class)
public class DefinedDateRangesTests {

    private Context context;
    private DefinedDateRanges testObject;

    @Before
    public void setup() {
        context = RuntimeEnvironment.application;

        testObject = create();
    }

    @After
    public void teardown() {

    }

    public Context getContext() {
        return context;
    }

    @Test
    public void testInstantiation() {
        final DefinedDateRanges ranges = create();

        assertNotNull(ranges);
    }

    //@Test
    public void testMembersCreated() {
        final DefinedDateRanges ranges = create();

        for (final DefinedDateRangeName name : DefinedDateRangeName.values()) {
            System.out.println(name.name());

            assertTrue(ranges.contains(name));
        }
    }

    //@Test
    public void testGetByMenuId() {
        final int menuId = R.id.menu_today;
        final String expectedName = DefinedDateRangeName.TODAY.toString();

        final DefinedDateRange actual = testObject.getByMenuId(menuId);

        Assert.assertNotNull(actual);
        assertEquals(expectedName, actual.getName());
        Assert.assertEquals(DefinedDateRangeName.TODAY, actual.key);
    }

    //@Test
    public void testGetByNameId() {
        final int nameId = R.string.last3months;
        final String expectedName = DefinedDateRangeName.LAST_3_MONTHS.toString();

        final DefinedDateRange actual = testObject.getByNameId(nameId);

        Assert.assertNotNull(actual);
        Assert.assertEquals(expectedName, actual.getName());
        Assert.assertEquals(DefinedDateRangeName.LAST_3_MONTHS, actual.key);
    }

    //@Test
    @Config(qualifiers = "fr-land")
    public void testLocalizedName() {
        final String expected = context.getString(R.string.future_transactions);

        final DefinedDateRange range = testObject.get(DefinedDateRangeName.FUTURE_TRANSACTIONS);
        final String actual = range.getLocalizedName(context);

        Assert.assertEquals(expected, actual);
    }

    // Private methods.

    private DefinedDateRanges create() {
        final DefinedDateRanges ranges = new DefinedDateRanges(context);
        return ranges;
    }
}
