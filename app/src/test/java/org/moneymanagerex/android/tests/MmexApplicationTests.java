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

import com.money.manager.ex.BuildConfig;
import com.money.manager.ex.MoneyManagerApplication;
import com.money.manager.ex.utils.MmexDatabaseUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowEnvironment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Test the methods in MoneyManagerApplication.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class MmexApplicationTests {

    private Context context;

    @Before
    public void setUp() throws Exception {
        this.context = RuntimeEnvironment.application;

        // External storage
//        File externalStorage = new File("/sdcard/MoneyManagerEx/");
//        ShadowEnvironment.setExternalStorageEmulated(externalStorage, true);
    }

    @After
    public void tearDown() throws Exception {
        this.context = null;

        ShadowEnvironment.reset();
    }

    /**
     * Ensure that the default file name can not be empty.
     */
    @Test
    public void defaultDatabaseNameContainsFileName() throws Exception {
        String expected = "data.mmb";

        String actual = MoneyManagerApplication.getDatabasePath(context);

//        assertEquals(expected, actual);
        assertTrue(actual.contains("/" + expected));
    }

    /**
     * The test fails.
     */
    @Test
    public void dbDirectoryHasAppName() {
        final String expected = "MoneyManagerEx";

        MmexDatabaseUtils dbUtils = new MmexDatabaseUtils(this.context);
        String actual = dbUtils.getDatabaseDirectory();

        assertTrue(actual.contains(expected));
    }

}
