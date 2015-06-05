/*
 * Copyright (C) 2012-2015 The Android Money Manager Ex Project Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
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

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.LinearLayout;

import com.money.manager.ex.R;
import com.money.manager.ex.fragment.AllDataFragment;
import com.money.manager.ex.fragment.BaseFragmentActivity;
import com.money.manager.ex.fragment.IInputAmountDialogListener;

public class SearchActivity
        extends BaseFragmentActivity
        implements IInputAmountDialogListener {

    /**
     * Indicates whether to show the account headers in search results.
     */
    public boolean ShowAccountHeaders = true;

	private boolean mIsDualPanel = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_activity);

        SearchFragment searchFragment = (SearchFragment) getSupportFragmentManager()
                .findFragmentByTag(SearchFragment.class.getSimpleName());
        if (searchFragment == null) {
            // fragment create
            searchFragment = new SearchFragment();
            // set dual panel
            LinearLayout fragmentDetail = (LinearLayout) findViewById(R.id.fragmentDetail);
            mIsDualPanel = fragmentDetail != null && fragmentDetail.getVisibility() == View.VISIBLE;
            searchFragment.setDualPanel(mIsDualPanel);
            // add to stack
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragmentContent, searchFragment, SearchFragment.class.getSimpleName())
                    .commit();
        }
        // reconfigure the toolbar event
        setToolbarStandardAction(getToolbar(), R.id.action_cancel, R.id.action_search);
    }

	@Override
	protected void onResume() {
		super.onResume();
		AllDataFragment fragment;
		fragment = (AllDataFragment)getSupportFragmentManager()
                .findFragmentByTag(AllDataFragment.class.getSimpleName());
		if (fragment != null && fragment.isVisible()) {
			fragment.loadData();
		}
	}

	@Override
	public void onFinishedInputAmountDialog(int id, Double amount) {
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
}
