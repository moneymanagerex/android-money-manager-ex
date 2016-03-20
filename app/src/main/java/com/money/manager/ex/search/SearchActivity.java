/*
 * Copyright (C) 2012-2016 The Android Money Manager Ex Project Team
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
package com.money.manager.ex.search;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.LinearLayout;

import com.money.manager.ex.Constants;
import com.money.manager.ex.R;
import com.money.manager.ex.common.AllDataListFragment;
import com.money.manager.ex.common.BaseFragmentActivity;
import com.money.manager.ex.common.events.FragmentViewCreatedEvent;
import com.money.manager.ex.database.QueryAllData;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class SearchActivity
    extends BaseFragmentActivity {

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
        if (!searchFragment.isAdded()) {
            // set dual panel
            LinearLayout fragmentDetail = (LinearLayout) findViewById(R.id.fragmentDetail);
            mIsDualPanel = fragmentDetail != null && fragmentDetail.getVisibility() == View.VISIBLE;
        }
        // reconfigure the toolbar event
        setToolbarStandardAction(getToolbar(), R.id.action_cancel, R.id.action_search);

        handleSearchRequest();
    }

    @Override
    protected void onStart() {
        super.onStart();

        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);

        super.onStop();
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
    public boolean onActionCancelClick() {
        finish();
        return true;
    }

    @Override
    public boolean onActionDoneClick() {
        performSearch();

        return super.onActionDoneClick();
    }

    // Events

    @Subscribe
    public void onFragmentViewCreated(FragmentViewCreatedEvent event) {
        String tag = event.fragmentTag;

        if (mSearchParameters != null && tag.equals(SearchFragment.class.getSimpleName())) {
            // Get search criteria if any was sent from an external caller.
            getSearchFragment().handleSearchRequest(mSearchParameters);
            performSearch();
            // remove search parameters once used.
            mSearchParameters = null;
        }
    }

//    @Subscribe
//    public void onSearchResultsRequested(ShowSearchResultsRequestEvent event) {
//        showSearchResultsFragment(event.conditions);
//    }

    // Public

    private SearchFragment createSearchFragment() {
        SearchFragment searchFragment = new SearchFragment();

        // add to stack
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragmentContent, searchFragment, SearchFragment.class.getSimpleName())
                .commit();

        return searchFragment;
    }

    private SearchFragment getSearchFragment() {
        if (mSearchFragment == null) {
            // try to find the search fragment
            mSearchFragment = (SearchFragment) getSupportFragmentManager()
                    .findFragmentByTag(SearchFragment.class.getSimpleName());

            if (mSearchFragment == null) {
                mSearchFragment = createSearchFragment();
            }
        }
        return mSearchFragment;
    }

    /**
     * Read the search request from the intent, if the activity was invoked from elsewhere.
     */
    private void handleSearchRequest() {
        Intent intent = getIntent();
        if (intent == null) return;

        // see if we have the search criteria.
        mSearchParameters = intent.getParcelableExtra(EXTRA_SEARCH_PARAMETERS);
    }

    private void performSearch() {
        // Perform search

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContent);

        if (fragment != null && fragment instanceof SearchFragment) {
//            ((SearchFragment) fragment).executeSearch();
            String where = ((SearchFragment) fragment).getWhereStatement();
            showSearchResultsFragment(where);
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
    }

    private void showSearchResultsFragment(String where) {
        //create a fragment for search results.
        AllDataListFragment searchResultsFragment = (AllDataListFragment) this.getSupportFragmentManager()
                .findFragmentByTag(AllDataListFragment.class.getSimpleName());

        if (searchResultsFragment != null) {
            this.getSupportFragmentManager().beginTransaction()
                    .remove(searchResultsFragment)
                    .commit();
        }

        searchResultsFragment = AllDataListFragment.newInstance(Constants.NOT_SET, false);

        searchResultsFragment.showTotalsFooter();

        //create parameter bundle
//        Bundle args = new Bundle();
        Bundle args = searchResultsFragment.getArguments();
        args.putString(AllDataListFragment.KEY_ARGUMENTS_WHERE, where);
        // Sorting
        args.putString(AllDataListFragment.KEY_ARGUMENTS_SORT,
                QueryAllData.TOACCOUNTID + ", " + QueryAllData.Date + ", " +
                        QueryAllData.TransactionType + ", " + QueryAllData.ID);
        //set arguments
//        searchResultsFragment.setArguments(args);

        this.ShowAccountHeaders = true;

        //add fragment
        FragmentTransaction transaction = this.getSupportFragmentManager().beginTransaction();
        //animation
        transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left,
                R.anim.slide_in_right, R.anim.slide_out_left);
        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack.
        if (mIsDualPanel) {
            transaction.add(R.id.fragmentDetail, searchResultsFragment, AllDataListFragment.class.getSimpleName());
        } else {
            // transaction.remove()
            transaction.replace(R.id.fragmentContent, searchResultsFragment, AllDataListFragment.class.getSimpleName());
            transaction.addToBackStack(null);
        }
        // Commit the transaction
        transaction.commit();
    }
}
