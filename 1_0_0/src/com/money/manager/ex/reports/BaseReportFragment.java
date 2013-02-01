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
package com.money.manager.ex.reports;

import java.util.Calendar;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.view.View;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.money.manager.ex.R;
import com.money.manager.ex.database.SQLDataSet;
import com.money.manager.ex.database.ViewMobileData;
import com.money.manager.ex.fragment.BaseListFragment;

public abstract class BaseReportFragment extends BaseListFragment implements LoaderCallbacks<Cursor> {
	protected static final int ID_LOADER = 1;
	protected static final String KEY_ITEM_SELECTED = "PayeeReportFragment:ItemSelected";
	protected static final String KEY_WHERE_CLAUSE = "PayeeReportFragment:WhereClause";
	protected int mItemSelected = R.id.menu_all_time;
	protected String mWhereClause = null;
	
	protected View addListViewHeaderFooter(int layout) {
		return View.inflate(getActivity(), layout, null);
	}

	protected String getWhereClause() {
		return mWhereClause;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		//set listview
		setHasOptionsMenu(true);
		setEmptyText(getString(R.string.no_data));
		setListShown(false);
		//item selected
		if (savedInstanceState != null && savedInstanceState.containsKey(KEY_ITEM_SELECTED)) {
			mItemSelected = savedInstanceState.getInt(KEY_ITEM_SELECTED);
		}
		//start loader
		startLoader(savedInstanceState);
	}
	
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		switch (id) {
		case ID_LOADER:
			if (args != null && args.containsKey(KEY_WHERE_CLAUSE)) {
				setWhereClause(args.getString(KEY_WHERE_CLAUSE));
			}
			return new CursorLoader(getActivity(), new SQLDataSet().getUri(), null, prepareQuery(getWhereClause()), null, null);
		}
		return null;
	}
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		//inflate menu
		inflater.inflate(R.menu.menu_report, menu);
		//checked item
		MenuItem item = menu.findItem(mItemSelected);
		if (item != null) {
			item.setChecked(true);
		}
	}
	
	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		switch (loader.getId()) {
		case ID_LOADER:
			((CursorAdapter) getListAdapter()).swapCursor(null);
		}
	}
	
	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		switch (loader.getId()) {
		case ID_LOADER:
			((CursorAdapter)getListAdapter()).swapCursor(data);
			if (isResumed()) {
                setListShown(true);
            } else {
                setListShownNoAnimation(true);
            }
			break;
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		String whereClause = null;
		int currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1;
		int currentYear = Calendar.getInstance().get(Calendar.YEAR);
		//quick-fix convert 'switch' to 'if-else'
		if (item.getItemId() == R.id.menu_current_month) {
			whereClause = ViewMobileData.Month + "=" + Integer.toString(currentMonth) + " AND " + ViewMobileData.Year + "=" + Integer.toString(currentYear);
		} else if (item.getItemId() == R.id.menu_last_month) {
			if (currentMonth == 1) {
				whereClause = ViewMobileData.Month + "=" + Integer.toString(12) + " AND " + ViewMobileData.Year + "=" + Integer.toString(currentYear - 1);
			} else {
				whereClause = ViewMobileData.Month + "=" + Integer.toString(currentMonth - 1) + " AND " + ViewMobileData.Year + "=" + Integer.toString(currentYear - 1);;
			}
		} else if (item.getItemId() == R.id.menu_last_30_days) {
			whereClause = "(julianday(date('now')) - julianday(" + ViewMobileData.Date + ") <= 30)";
		} else if (item.getItemId() == R.id.menu_current_year) {
			whereClause = ViewMobileData.Year + "=" + Integer.toString(currentYear);
		} else if (item.getItemId() == R.id.menu_last_year) {
			whereClause = ViewMobileData.Year + "=" + Integer.toString(currentYear - 1);
		} else if (item.getItemId() == R.id.menu_all_time) {
		} else {
			return super.onOptionsItemSelected(item);
		}
		//check item
		item.setChecked(true);
		mItemSelected = item.getItemId();
		//compose bundle
		Bundle args = new Bundle();
		args.putString(KEY_WHERE_CLAUSE, whereClause);
		//starts loader
		startLoader(args);
		
		return true;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(KEY_ITEM_SELECTED, mItemSelected);
		outState.putString(KEY_WHERE_CLAUSE, getWhereClause());
	}
	/**
	 * Prepare SQL query to execute in content provider
	 * @param whereClause
	 * @return
	 */
	protected abstract String prepareQuery(String whereClause) ;

	protected void setWhereClause(String mWhereClause) {
		this.mWhereClause = mWhereClause;
	}

	/**
	 * Start loader with arguments
	 * @param args
	 */
	protected void startLoader(Bundle args) {
		getLoaderManager().restartLoader(ID_LOADER, args, this);
	}
}
