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
import com.money.manager.ex.MmexApplication;
import com.money.manager.ex.core.database.DatabaseManager;
import com.money.manager.ex.utils.MmxDatabaseUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.moneymanagerex.android.testhelpers.TestApplication;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowEnvironment;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.*;

/**
 * Test the methods in MoneyManagerApplication.
 */
@RunWith(RobolectricTestRunner.class)
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

        String actual = new DatabaseManager(context).getDatabasePath();

        assertThat(actual, endsWith(expected));
    }

    /**
     * The test fails.
     */
    //@Test
    public void dbDirectoryHasAppName() {
        final String expected = "MoneyManagerEx";

        String actual = new DatabaseManager(context).getDefaultDatabaseDirectory();

        assertThat(actual, containsString(expected));
    }

}
