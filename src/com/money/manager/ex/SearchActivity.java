/*******************************************************************************
 * Copyright (C) 2013 The Android Money Manager Ex Project
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
 ******************************************************************************/
package com.money.manager.ex;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.LinearLayout;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.money.manager.ex.fragment.AllDataFragment;
import com.money.manager.ex.fragment.AllDataFragment.AllDataFragmentLoaderCallbacks;
import com.money.manager.ex.fragment.BaseFragmentActivity;
import com.money.manager.ex.fragment.SearchFragment;

public class SearchActivity extends BaseFragmentActivity implements AllDataFragmentLoaderCallbacks {
	private boolean mIsDualPanel = false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// if large screen set orietation landscape 
		if ((getResources().getConfiguration().screenLayout &  Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE) {     
			 setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			 setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
		}
		setContentView(R.layout.main_fragments_activity);
		SearchFragment fragment = (SearchFragment)getSupportFragmentManager().findFragmentByTag(SearchFragment.class.getSimpleName());
		if (fragment == null) {
			// fragment create
			fragment = new SearchFragment();
			// set dual panle
			LinearLayout fragmentDetail = (LinearLayout)findViewById(R.id.fragmentDetail); 
			mIsDualPanel = fragmentDetail != null && fragmentDetail.getVisibility() == View.VISIBLE;
			fragment.setDualPanel(mIsDualPanel);
			// add to stack
			getSupportFragmentManager().beginTransaction().add(R.id.fragmentContent, fragment, SearchFragment.class.getSimpleName()).commit();
		}
		// home
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSherlock().getMenuInflater().inflate(R.menu.menu_search_transaction, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onCallbackCreateLoader(int id, Bundle args) {
		return;
	}

	@Override
	public void onCallbackLoaderFinished(Loader<Cursor> loader, Cursor data) {
		if (loader != null && loader.getId() == AllDataFragment.ID_LOADER_ALL_DATA_DETAIL && data != null) {
			getSupportActionBar().setSubtitle(getString(R.string.number_transaction_found, data.getCount()));
		}
		return;
	}

	@Override
	public void onCallbackLoaderReset(Loader<Cursor> loader) {
		return;
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		AllDataFragment fragment;
		fragment = (AllDataFragment)getSupportFragmentManager().findFragmentByTag(AllDataFragment.class.getSimpleName());
		if (fragment != null && fragment.isVisible()) {
			fragment.startLoaderData();
		}
	}
}
