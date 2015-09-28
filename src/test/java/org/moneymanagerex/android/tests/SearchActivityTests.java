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
package org.moneymanagerex.android.tests;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.money.manager.ex.BuildConfig;
import com.money.manager.ex.R;
import com.money.manager.ex.common.CategoryListActivity;
import com.money.manager.ex.search.SearchActivity;
import com.money.manager.ex.search.SearchFragment;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.moneymanagerex.android.testhelpers.UnitTestHelper;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.util.ActivityController;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test Search activity.
 *
 * Created by Alen on 27/09/2015.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class SearchActivityTests {

    private ActivityController<SearchActivity> controller;
    private SearchActivity activity;

    @Before
    public void setUp() {
        this.controller = UnitTestHelper.getController(SearchActivity.class);
        this.activity = UnitTestHelper.getActivity(this.controller);
    }

    @After
    public void tearDown() {
        this.controller.destroy();
    }

    @Test
    public void activityOpens() {
        assertThat(this.activity).isNotNull();
    }

    @Test
    public void searchForSubCategory() {
        Intent expectedIntent;

        // Click Select Category
        TextView selectCategory = (TextView) activity.findViewById(R.id.textViewSelectCategory);
        assertThat(selectCategory).isNotNull();

        selectCategory.performClick();

        // confirm that clicking the Select Category text view opens category selector
        ShadowActivity shadowActivity = Shadows.shadowOf(this.activity);
        expectedIntent = shadowActivity.peekNextStartedActivityForResult().intent;
        assertThat(expectedIntent.getComponent()).isEqualTo(new ComponentName(this.activity,
            CategoryListActivity.class));
        assertThat(shadowActivity.getNextStartedActivity()).isEqualTo(expectedIntent);

        // Now simulate that we received the category.

        Fragment searchFragment = UnitTestHelper.getFragment(activity, SearchFragment.class.getSimpleName());
        assertThat(searchFragment).isNotNull();

        // We "selected" Food:Dining out.
        Intent categoryData = UnitTestHelper.getSelectCategoryResult(2, "Food", 9, "Dining out");
        searchFragment.onActivityResult(SearchFragment.REQUEST_PICK_CATEGORY, Activity.RESULT_OK,
            categoryData);

        assertThat(selectCategory.getText()).containsSequence("Food : Dining out");

        // Run search

        LinearLayout searchButton = (LinearLayout) activity.findViewById(R.id.action_search);
        assertThat(searchButton).isNotNull();

        searchButton.performClick();

        // todo: test the results.
        // We need the data first. :S

    }
}
