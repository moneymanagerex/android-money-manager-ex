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

import android.content.Context;

import com.money.manager.ex.BuildConfig;
import com.money.manager.ex.R;
import com.money.manager.ex.settings.AppSettings;
import com.money.manager.ex.settings.DatabaseSettings;
import com.money.manager.ex.transactions.CheckingTransactionEditActivity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.moneymanagerex.android.testhelpers.TestApplication;
import org.moneymanagerex.android.testhelpers.UnitTestHelper;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

/**
 * Tests for edit transaction activity.
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, application = TestApplication.class)
public class EditTransactionTests {

    private Context context;
    private ActivityController<CheckingTransactionEditActivity> controller;

    @Before
    public void setUp() {
        this.context = UnitTestHelper.getContext();
        this.controller = UnitTestHelper.getController(CheckingTransactionEditActivity.class);
//        this.activity = UnitTestHelper.getActivity(this.controller);
    }

    @Test
    public void setDbPath() {
        DatabaseSettings dbSettings = new AppSettings(this.context).getDatabaseSettings();

        String dbPath = dbSettings.getDatabasePath();
//        assertThat(dbPath).isEmpty();

        // set the db path in preferences
        dbPath = "Z:\\Dropbox\\Apps\\MoneyManagerEx Mobile\\dev-db.mmb";
        dbSettings.setDatabasePath(dbPath);

        dbPath = dbSettings.getDatabasePath();
//        assertThat(dbPath).isNotEmpty();
    }

    //@Test
    public void useExistingDatabase() {
        setDbPath();

        // FAILS, because FontIcon can't be instantiated by Robolectric.

        CheckingTransactionEditActivity activity = UnitTestHelper.getActivity(this.controller);

        // now create a transaction
        activity.findViewById(R.id.textViewAmount).performClick();
        // ent
    }
}