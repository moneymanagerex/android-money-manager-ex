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
package org.moneymanagerex.android.robotium;

import android.support.test.runner.AndroidJUnit4;
import android.test.ActivityInstrumentationTestCase2;

import com.money.manager.ex.R;
import com.money.manager.ex.assetallocation.AssetAllocationActivity;
import com.money.manager.ex.home.MainActivity;
import com.robotium.solo.Solo;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.moneymanagerex.android.testhelpers.UiTestHelpersRobotium;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Asset Allocation UI tests.
 */
@RunWith(AndroidJUnit4.class)
public class AssetAllocationTests
        extends ActivityInstrumentationTestCase2<AssetAllocationActivity> {

    private Solo solo;

    public AssetAllocationTests() {
        super(AssetAllocationActivity.class);
    }

    @Before
    public void setUp() {
        solo = UiTestHelpersRobotium.setUp(this);
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();

        UiTestHelpersRobotium.tearDown(solo);
    }

    @Test
    public void formOpens() {
        assertThat(solo.waitForActivity(getActivity().getClass().getSimpleName())).isTrue();
    }

    @Test
    public void clickingBackArrowExits() {
        solo.clickOnActionBarHomeButton();
//        assertThat(solo.waitForLogMessage("Finishing Asset Allocation")).isTrue();
        assertThat(getActivity().isFinishing()).isTrue();
    }

    @Test
    public void initialView() {
        // layoutIsLandscape ?
        // title
        assertThat(solo.searchText("Asset Allocation")).isTrue();
        // addNew button
        assertThat(solo.waitForView(R.id.fab)).isTrue();
    }

    /*
    todo: Tasks
    - Add sortOrder column.
     */

    /*
    todo: Tests
    - we see a grid of asset allocations
    - can add a new allocation
      - create/edit form opens
      - can select a security from all the available ones
    - can delete an allocation
    - when adding a stock, the allocation value updates
    - updating a stock price updates the allocation value (update manually for test value)

    - test that fab opens the new asset class form
    - test that the asset classes are sorted.
     */
}
