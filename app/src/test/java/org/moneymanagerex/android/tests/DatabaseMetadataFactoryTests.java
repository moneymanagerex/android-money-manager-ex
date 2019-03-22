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

import com.money.manager.ex.BuildConfig;
import com.money.manager.ex.home.DatabaseMetadata;
import com.money.manager.ex.home.DatabaseMetadataFactory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.moneymanagerex.android.testhelpers.TestApplication;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.*;


/**
 * Test the factory
 */

@RunWith(RobolectricTestRunner.class)
public class DatabaseMetadataFactoryTests {

    private DatabaseMetadataFactory _testObject;

    @Before
    public void setUp() throws Exception {
        _testObject = new DatabaseMetadataFactory(RuntimeEnvironment.application);
    }

    @After
    public void tearDown() throws Exception {
        _testObject = null;
    }

    @Test
    public void createDefaultItem() {
        DatabaseMetadata empty = _testObject.createDefaultEntry();

        assertThat(empty, notNullValue());
    }

}
