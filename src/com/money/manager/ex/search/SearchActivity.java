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

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.LinearLayout;

import com.money.manager.ex.R;
import com.money.manager.ex.fragment.AllDataFragment;
import com.money.manager.ex.fragment.AllDataFragment.AllDataFragmentLoaderCallbacks;
import com.money.manager.ex.fragment.BaseFragmentActivity;
import com.money.manager.ex.fragment.InputAmountDialog.InputAmountDialogListener;

public class SearchActivity extends BaseFragmentActivity
        implements AllDataFragmentLoaderCallbacks, InputAmountDialogListener {
	private boolean mIsDualPanel = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.search_activity);
        super.onCreate(savedInstanceState);

        SearchFragment fragment = (SearchFragment) getSupportFragmentManager().findFragmentByTag(SearchFragment.class.getSimpleName());
        if (fragment == null) {
            // fragment create
            fragment = new SearchFragment();
            // set dual panel
            LinearLayout fragmentDetail = (LinearLayout) findViewById(R.id.fragmentDetail);
            mIsDualPanel = fragmentDetail != null && fragmentDetail.getVisibility() == View.VISIBLE;
            fragment.setDualPanel(mIsDualPanel);
            // add to stack
            getSupportFragmentManager().beginTransaction().add(R.id.fragmentContent, fragment, SearchFragment.class.getSimpleName()).commit();
        }
        // reconfigure the toolbar event
        setToolbarStandardAction(getToolbar(), R.id.action_cancel, R.id.action_search);
    }

    @Override
    public void onCallbackCreateLoader(int id, Bundle args) {	}

	@Override
	public void onCallbackLoaderFinished(Loader<Cursor> loader, final Cursor data) { }

	@Override
	public void onCallbackLoaderReset(Loader<Cursor> loader) {	}
	
	@Override
	protected void onResume() {
		super.onResume();
		AllDataFragment fragment;
		fragment = (AllDataFragment)getSupportFragmentManager().findFragmentByTag(AllDataFragment.class.getSimpleName());
		if (fragment != null && fragment.isVisible()) {
			fragment.startLoaderData();
		}
	}

	@Override
	public void onFinishedInputAmountDialog(int id, Double amount) {
		SearchFragment fragment = (SearchFragment)getSupportFragmentManager().findFragmentByTag(SearchFragment.class.getSimpleName());
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
                SearchFragment searchFragment = (SearchFragment) getSupportFragmentManager().findFragmentByTag(SearchFragment.class.getSimpleName());
                if (searchFragment != null) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContent, searchFragment, SearchFragment.class.getSimpleName()).commit();
                }
            }
        }
        return super.onActionDoneClick();
    }
}
