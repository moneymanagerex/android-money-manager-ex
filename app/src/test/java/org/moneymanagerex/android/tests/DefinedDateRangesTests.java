/*
 * Copyright (C) 2012-2019 The Android Money Manager Ex Project Team
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

import com.money.manager.ex.BuildConfig;
import com.money.manager.ex.R;
import com.money.manager.ex.core.DefinedDateRange;
import com.money.manager.ex.core.DefinedDateRangeName;
import com.money.manager.ex.core.DefinedDateRanges;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.moneymanagerex.android.testhelpers.TestApplication;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

/**
 * Test Defined Date Ranges.
 */
@RunWith(RobolectricTestRunner.class)
public class DefinedDateRangesTests {

    private Context context;
    private DefinedDateRanges testObject;

    @Before
    public void setup() {
        this.context = RuntimeEnvironment.application;

        this.testObject = create();
    }

    @After
    public void teardown() {

    }

    public Context getContext() {
        return this.context;
    }

    @Test
    public void testInstantiation() {
        DefinedDateRanges ranges = create();

        assertNotNull(ranges);
    }

    //@Test
    public void testMembersCreated() {
        DefinedDateRanges ranges = create();

        for (DefinedDateRangeName name : DefinedDateRangeName.values()) {
            System.out.println(name.name());

            assertTrue(ranges.contains(name));
        }
    }

    //@Test
    public void testGetByMenuId() {
        int menuId = R.id.menu_today;
        String expectedName = DefinedDateRangeName.TODAY.toString();

        DefinedDateRange actual = this.testObject.getByMenuId(menuId);

        assertNotNull(actual);
        assertEquals(expectedName, actual.getName());
        assertEquals(DefinedDateRangeName.TODAY, actual.key);
    }

    //@Test
    public void testGetByNameId() {
        int nameId = R.string.last3months;
        String expectedName = DefinedDateRangeName.LAST_3_MONTHS.toString();

        DefinedDateRange actual = this.testObject.getByNameId(nameId);

        assertNotNull(actual);
        assertEquals(expectedName, actual.getName());
        assertEquals(DefinedDateRangeName.LAST_3_MONTHS, actual.key);
    }

    //@Test
    @Config(qualifiers="fr-land")
    public void testLocalizedName() {
        String expected = this.context.getString(R.string.future_transactions);

        DefinedDateRange range = this.testObject.get(DefinedDateRangeName.FUTURE_TRANSACTIONS);
        String actual = range.getLocalizedName(getContext());

        assertEquals(expected, actual);
    }

    // Private methods.

    private DefinedDateRanges create() {
        DefinedDateRanges ranges = new DefinedDateRanges(this.context);
        return ranges;
    }
}
