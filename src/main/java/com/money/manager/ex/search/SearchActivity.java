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
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.money.manager.ex.search;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.LinearLayout;

import com.money.manager.ex.R;
import com.money.manager.ex.common.AllDataListFragment;
import com.money.manager.ex.common.BaseFragmentActivity;
import com.money.manager.ex.common.ICommonFragmentCallbacks;
import com.money.manager.ex.common.IInputAmountDialogListener;

import java.math.BigDecimal;

import info.javaperformance.money.Money;

public class SearchActivity
        extends BaseFragmentActivity
        implements IInputAmountDialogListener, ICommonFragmentCallbacks {

    public static final String EXTRA_SEARCH_PARAMETERS = "SearchActivity:SearchCriteria";

    /**
     * Indicates whether to show the account headers in search results.
     */
    public boolean ShowAccountHeaders = true;

	private boolean mIsDualPanel = false;
    private SearchFragment mSearchFragment;
    private SearchParameters mSearchParameters;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_activity);

        SearchFragment searchFragment = getSearchFragment();
//        if (searchFragment == null) {
            // fragment create
//            searchFragment = getSearchFragment();
            if (!searchFragment.isAdded()) {
                // set dual panel
                LinearLayout fragmentDetail = (LinearLayout) findViewById(R.id.fragmentDetail);
                mIsDualPanel = fragmentDetail != null && fragmentDetail.getVisibility() == View.VISIBLE;
                searchFragment.setDualPanel(mIsDualPanel);
                // add to stack
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.fragmentContent, searchFragment, SearchFragment.class.getSimpleName())
                        .commit();
            }
//        }
        // reconfigure the toolbar event
        setToolbarStandardAction(getToolbar(), R.id.action_cancel, R.id.action_search);

        handleSearchRequest();
    }

	@Override
	protected void onResume() {
		super.onResume();
		AllDataListFragment fragment;
		fragment = (AllDataListFragment) getSupportFragmentManager()
                .findFragmentByTag(AllDataListFragment.class.getSimpleName());
		if (fragment != null && fragment.isVisible()) {
			fragment.loadData();
		}
	}

	@Override
	public void onFinishedInputAmountDialog(int id, Money amount) {
		SearchFragment fragment = (SearchFragment)getSupportFragmentManager()
                .findFragmentByTag(SearchFragment.class.getSimpleName());
		if (fragment != null) fragment.onFinishedInputAmountDialog(id, amount);
	}

    @Override
    public boolean onActionCancelClick() {
        finish();
        return true;
    }

    @Override
    public boolean onActionDoneClick() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContent);
        if (fragment != null && fragment instanceof SearchFragment) {
            ((SearchFragment) fragment).executeSearch();
        } else {
            if (!mIsDualPanel) {
                SearchFragment searchFragment = (SearchFragment) getSupportFragmentManager()
                        .findFragmentByTag(SearchFragment.class.getSimpleName());
                if (searchFragment != null) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragmentContent, searchFragment, SearchFragment.class.getSimpleName())
                            .commit();
                }
            }
        }
        return super.onActionDoneClick();
    }

    private SearchFragment getSearchFragment() {
        if (mSearchFragment == null) {
            // try to find the search fragment
            mSearchFragment = (SearchFragment) getSupportFragmentManager()
                    .findFragmentByTag(SearchFragment.class.getSimpleName());

            if (mSearchFragment == null) {
                mSearchFragment = new SearchFragment();
            }
        }
        return mSearchFragment;
    }

    private void handleSearchRequest() {
        Intent intent = getIntent();
        if (intent == null) return;

        // see if we have the search criteria.
        mSearchParameters = intent.getParcelableExtra(EXTRA_SEARCH_PARAMETERS);
//        if (parameters == null) return;

//        SearchFragment searchFragment = getSearchFragment();
//        searchFragment.setSearchParameters(parameters);
//        searchFragment.executeSearch();
    }

    @Override
    public void onFragmentViewCreated(String tag) {
        if (mSearchParameters != null && tag.equals(SearchFragment.class.getSimpleName())) {
            // Get search criteria if any was sent from an external caller.
            getSearchFragment().handleSearchRequest(mSearchParameters);
            // remove search parameters once used.
            mSearchParameters = null;
        }
    }
}
