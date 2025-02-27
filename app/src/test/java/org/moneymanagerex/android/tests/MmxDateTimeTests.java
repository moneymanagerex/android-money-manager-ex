/*
 * Copyright (C) 2012-2018 The Android Money Manager Ex Project Team
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

import android.util.Log;

import com.money.manager.ex.utils.MmxDate;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertEquals;

/**
 * Tests for conversion from JodaTime back to the standard Java date types.
 */

@RunWith(RobolectricTestRunner.class)


public class MmxDateTimeTests {

    private MmxDate _util;

    @Before
    public void setup() {
        _util = new MmxDate();
    }

    @Test
    public void basicTests() {
        String current = _util.getCalendar().toString();
        Log.d("test", current);
    }

    @Test
    public void testIsoCombinedFormat() {
        MmxDate d = MmxDate.fromIso8601("2025-02-26T21:36:07.000+0100");
        assertEquals(36, d.getMinute());
    }

    @Test
    public void testInvalidPattern() {
        MmxDate d = MmxDate.fromIso8601("2016-10-21T18:42:18.000Z");
        assertEquals(42, d.getMinute());
    }

    @Test
    public void testCombinedPattern() {
        MmxDate d = MmxDate.fromIso8601("2025-07-25T21:08:51");
        assertEquals(21, d.getHour());

        d = MmxDate.fromIso8601("2025-01-25T21:08:51");
        assertEquals(21, d.getHour());
    }

    @Test
    public void testValidPattern() {
        MmxDate d = MmxDate.fromIso8601("2016-10-22T02:36:46.000+0200");
        assertEquals(36, d.getMinute());
    }


}
