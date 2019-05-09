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
package com.money.manager.ex.search;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.money.manager.ex.Constants;
import com.money.manager.ex.R;
import com.money.manager.ex.common.AllDataListFragment;
import com.money.manager.ex.common.MmxBaseFragmentActivity;
import com.money.manager.ex.core.UIHelper;
import com.money.manager.ex.database.QueryAllData;

import org.parceler.Parcels;

import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.FragmentTransaction;

public class SearchActivity
    extends MmxBaseFragmentActivity {

    public static final String EXTRA_SEARCH_PARAMETERS = "SearchActivity:SearchCriteria";

    /**
     * Indicates whether to show the account headers in search results.
     */
    public boolean ShowAccountHeaders = true;

	private boolean mIsDualPanel = false;
    private SearchParametersFragment mSearchParametersFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_activity);

        SearchParametersFragment searchParametersFragment = getSearchFragment();
        if (!searchParametersFragment.isAdded()) {
            // set dual panel
            LinearLayout fragmentDetail = (LinearLayout) findViewById(R.id.fragmentDetail);
            mIsDualPanel = fragmentDetail != null && fragmentDetail.getVisibility() == View.VISIBLE;
        }
        // reconfigure the toolbar event
//        showStandardToolbarActions(getToolbar(), R.id.action_cancel, R.id.action_search);
        setDisplayHomeAsUpEnabled(true);

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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // handled in the search fragment
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        UIHelper ui = new UIHelper(this);

        // Add Search icon.
        getMenuInflater().inflate(R.menu.menu_search, menu);
        MenuItem item = menu.findItem(R.id.searchMenuItem);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        item.setIcon(ui.getIcon(GoogleMaterial.Icon.gmd_search));
        // show this menu item last

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            case R.id.searchMenuItem:
                performSearch();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Public

    private SearchParametersFragment createSearchFragment() {
        SearchParametersFragment searchParametersFragment = SearchParametersFragment.createInstance();

        // add to stack
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragmentMain, searchParametersFragment, SearchParametersFragment.class.getSimpleName())
                .commit();

        return searchParametersFragment;
    }

    private SearchParametersFragment getSearchFragment() {
        if (mSearchParametersFragment == null) {
            // try to find the search fragment
            mSearchParametersFragment = (SearchParametersFragment) getSupportFragmentManager()
                    .findFragmentByTag(SearchParametersFragment.class.getSimpleName());

            if (mSearchParametersFragment == null) {
                mSearchParametersFragment = createSearchFragment();
            }
        }
        return mSearchParametersFragment;
    }

    /**
     * Read the search request from the intent, if the activity was invoked from elsewhere.
     */
    private void handleSearchRequest() {
        Intent intent = getIntent();
        if (intent == null) return;

        // see if we have the search criteria.
        Parcelable searchParcel = intent.getParcelableExtra(EXTRA_SEARCH_PARAMETERS);
        SearchParameters searchParameters = Parcels.unwrap(searchParcel);

        if (searchParameters != null) {
            Log.d("SearchActivity", "-------\n\n\n\n\n\n\n\n\n\n\n\n\nPayeeName: " + searchParameters.payeeName + "\nPayeeId: " + searchParameters.payeeId);
            getSearchFragment().setSearchParameters(searchParameters);
            performSearch();
        }
    }

    private void performSearch() {
        SearchParametersFragment searchParametersFragment = getSearchFragment();
        String where = searchParametersFragment.getWhereStatement();
        showSearchResultsFragment(where);
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
        Bundle args = new Bundle();
        args.putString(AllDataListFragment.KEY_ARGUMENTS_WHERE, where);
        // Sorting
        args.putString(AllDataListFragment.KEY_ARGUMENTS_SORT,
                QueryAllData.TOACCOUNTID + ", " + QueryAllData.Date + ", " +
                        QueryAllData.TransactionType + ", " + QueryAllData.ID);
        //set arguments
        searchResultsFragment.getArguments().putAll(args);

        this.ShowAccountHeaders = true;

        //add fragment
        FragmentTransaction transaction = this.getSupportFragmentManager().beginTransaction();
        //animation
        transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left,
                R.anim.slide_in_right, R.anim.slide_out_left);
        if (mIsDualPanel) {
            transaction.add(R.id.fragmentDetail, searchResultsFragment, AllDataListFragment.class.getSimpleName());
        } else {
            // transaction.remove()
            transaction.replace(R.id.fragmentMain, searchResultsFragment, AllDataListFragment.class.getSimpleName());
            transaction.addToBackStack(null);
        }
        // Commit the transaction
        transaction.commit();
    }
}
