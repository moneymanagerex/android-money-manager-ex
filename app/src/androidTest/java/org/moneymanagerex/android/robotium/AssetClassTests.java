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
package org.moneymanagerex.android.robotium;

import android.support.test.runner.AndroidJUnit4;
import android.test.ActivityInstrumentationTestCase2;
import android.view.View;
import android.widget.TextView;

import com.money.manager.ex.R;
import com.money.manager.ex.assetallocation.AssetClassEditActivity;
import com.money.manager.ex.home.MainActivity;
import com.robotium.solo.Solo;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.moneymanagerex.android.testhelpers.UiTestHelpersRobotium;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Asset Class edit form.
 */
//@RunWith(AndroidJUnit4.class)
//public class AssetClassTests
//        extends ActivityInstrumentationTestCase2<AssetClassEditActivity> {
//
//    private Solo solo;
//
//    public AssetClassTests() {
//        super(AssetClassEditActivity.class);
//    }
//
//    @Before
//    public void setUp() {
//        solo = UiTestHelpersRobotium.setUp(this);
//    }
//
//    @After
//    public void tearDown() throws Exception {
//        super.tearDown();
//
//        UiTestHelpersRobotium.tearDown(solo);
//    }
//
////    @Test
////    public void formOpens() {
////        assertThat(solo.waitForActivity(getActivity().getClass().getSimpleName())).isTrue();
////    }
//
////    @Test
////    public void visualAppearance() {
////        assertThat(solo.searchText("Cancel")).isTrue();
////    }
//
//    @Test
//    public void enterAllocation() {
//        View allocationView = solo.getView(R.id.allocationEdit);
//        solo.clickOnView(allocationView);
//
//        solo.waitForDialogToOpen();
//        solo.clickOnView(solo.getView(R.id.buttonKeyNum2));
//        solo.clickOnView(solo.getView(R.id.buttonKeyNumDecimal));
//        solo.clickOnView(solo.getView(R.id.buttonKeyNum5));
//        solo.clickOnView(solo.getView(R.id.buttonKeyNum6));
//        solo.clickOnText("OK");
//        solo.waitForDialogToClose();
//
////        assertThat(solo.waitForText("2.56")).isTrue();
//    }
//
//}
