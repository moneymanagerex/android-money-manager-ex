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
import com.money.manager.ex.domainmodel.AssetClass;
import com.money.manager.ex.servicelayer.AssetAllocationService;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.moneymanagerex.android.testhelpers.UnitTestHelper;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for asset allocation service.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class AssetAllocationTests {

    private AssetAllocationService testObject;

    @Before
    public void setup() {
        this.testObject = new AssetAllocationService(UnitTestHelper.getContext());
    }

    @After
    public void tearDown() {
        this.testObject = null;
    }

    @Test
    public void testInstantiation() {
        assertThat(testObject).isNotNull();
    }

    @Test
    public void testLoadingOfAllocation() {
        // Given

        // When

        List<AssetClass> actual = testObject.loadAssetAllocation();

        // Then

        assertThat(actual).isNotNull();
        assertThat(actual.size()).isGreaterThan(0);
    }
}
