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

import com.money.manager.ex.BuildConfig;
import com.money.manager.ex.assetallocation.AssetAllocationActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.moneymanagerex.android.testhelpers.DataHelpers;
import org.moneymanagerex.android.testhelpers.UnitTestHelper;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.util.ActivityController;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Asset Allocation UI
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class AssetAllocationActivityTests {

    private ActivityController<AssetAllocationActivity> controller;
    private AssetAllocationActivity activity;

    @Before
    public void setup() {
        // initialize database
        UnitTestHelper.setupContentProvider();

        // initialize support for activities (UI)
        this.controller = UnitTestHelper.getController(AssetAllocationActivity.class);
        this.activity = UnitTestHelper.getActivity(this.controller);
    }

    @After
    public void tearDown() {
        // Destroy the activity controller.
        this.controller.destroy();
        // Reset database instance between tests.
        UnitTestHelper.teardownDatabase();
    }

    @Test
    public void instantiation() {
        assertThat(this.activity).isNotNull();
    }

    @Test
    public void loadAllocation() {
        // Given
        DataHelpers.createAllocation();

        // When

        // Then
        assertThat(this.activity).isNotNull();
    }

    /**
     * Exception test example.
     */
    @Test(expected=RuntimeException.class)
    public void throwsException() {
        throw new RuntimeException("bang!");
    }

}
