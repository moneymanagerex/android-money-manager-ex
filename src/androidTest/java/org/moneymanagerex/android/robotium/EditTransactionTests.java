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
 *
 */
package org.moneymanagerex.android.robotium;

import android.test.ActivityInstrumentationTestCase2;

import com.money.manager.ex.transactions.EditTransactionActivity;
import com.robotium.solo.Solo;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Robotium test for Edit Transaction activity.
 */
public class EditTransactionTests
    extends ActivityInstrumentationTestCase2<EditTransactionActivity> {

  private Solo solo;

  public EditTransactionTests() {
    super(EditTransactionActivity.class);
  }

  public void setUp() throws Exception {
    solo = new Solo(getInstrumentation(), getActivity());
  }


  @Override
  public void tearDown() throws Exception {
    solo.finishOpenedActivities();
  }

    public void testActivityExists() {
        EditTransactionActivity activity = getActivity();
        assertNotNull(activity);
    }

    public void testStatusChange() {
        solo.waitForActivity(EditTransactionActivity.class.getSimpleName());

        boolean spinnerFound = solo.searchText("None");
        assertThat(spinnerFound).isTrue();

        solo.pressSpinnerItem(0, 1);
        assertThat(solo.isSpinnerTextSelected(0, "Reconciled"));

        solo.pressSpinnerItem(0, 1);
        assertThat(solo.isSpinnerTextSelected(0, "Void"));

        solo.pressSpinnerItem(0, -2);
        assertThat(solo.isSpinnerTextSelected(0, "None"));

        solo.pressSpinnerItem(0, 4);
        assertThat(solo.isSpinnerTextSelected(0, "Duplicate"));

        solo.pressSpinnerItem(0, -1);
        assertThat(solo.isSpinnerTextSelected(0, "Follow up"));

    }
}