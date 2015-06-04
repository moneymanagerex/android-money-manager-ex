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
package com.money.manager.ex.reports;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;

import com.afollestad.materialdialogs.MaterialDialog;
import com.money.manager.ex.R;
import com.money.manager.ex.database.SQLDataSet;
import com.money.manager.ex.database.ViewMobileData;
import com.money.manager.ex.fragment.BaseListFragment;
import com.money.manager.ex.utils.DateUtils;

import java.util.Calendar;
import java.util.Date;

public abstract class BaseReportFragment extends BaseListFragment implements LoaderCallbacks<Cursor> {
    protected static final int ID_LOADER = 1;
    protected static final String KEY_ITEM_SELECTED = "PayeeReportFragment:ItemSelected";
    protected static final String KEY_WHERE_CLAUSE = "PayeeReportFragment:WhereClause";
    protected static final String KEY_FROM_DATE = "PayeeReportFragment:FromDate";
    protected static final String KEY_TO_DATE = "PayeeReportFragment:ToDate";
    protected int mItemSelected = R.id.menu_all_time;
    protected String mWhereClause = null;
    protected Date mFromDate = null;
    protected Date mToDate = null;

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
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(KEY_ITEM_SELECTED))
                mItemSelected = savedInstanceState.getInt(KEY_ITEM_SELECTED);
            if (savedInstanceState.containsKey(KEY_FROM_DATE))
                mFromDate = (Date) savedInstanceState.getSerializable(KEY_FROM_DATE);
            if (savedInstanceState.containsKey(KEY_TO_DATE))
                mToDate = (Date) savedInstanceState.getSerializable(KEY_TO_DATE);
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
        inflater.inflate(R.menu.menu_period_picker, menu);
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
                ((CursorAdapter) getListAdapter()).swapCursor(data);
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
                whereClause = ViewMobileData.Month + "=" + Integer.toString(currentMonth - 1) + " AND " + ViewMobileData.Year + "=" + Integer.toString(currentYear);
            }
        } else if (item.getItemId() == R.id.menu_last_30_days) {
            whereClause = "(julianday(date('now')) - julianday(" + ViewMobileData.Date + ") <= 30)";
        } else if (item.getItemId() == R.id.menu_current_year) {
            whereClause = ViewMobileData.Year + "=" + Integer.toString(currentYear);
        } else if (item.getItemId() == R.id.menu_last_year) {
            whereClause = ViewMobileData.Year + "=" + Integer.toString(currentYear - 1);
        } else if (item.getItemId() == R.id.menu_all_time) {
        } else if (item.getItemId() == R.id.menu_custom_dates) {
            //check item
            item.setChecked(true);
            mItemSelected = item.getItemId();
            //show dialog
            showDialogCustomDates();

            return true;
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
        if (mFromDate != null)
            outState.putSerializable(KEY_FROM_DATE, mFromDate);
        if (mToDate != null)
            outState.putSerializable(KEY_TO_DATE, mToDate);
    }

    /**
     * Prepare SQL query to execute in content provider
     *
     * @param whereClause
     * @return
     */
    protected abstract String prepareQuery(String whereClause);

    protected void setWhereClause(String mWhereClause) {
        this.mWhereClause = mWhereClause;
    }

    /**
     * Start loader with arguments
     *
     * @param args
     */
    protected void startLoader(Bundle args) {
        getLoaderManager().restartLoader(ID_LOADER, args, this);
    }

    private void showDialogCustomDates() {
        MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                .customView(R.layout.dialog_choose_date_report, false)
                .positiveText(android.R.string.ok)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        View view = dialog.getCustomView();
                        DatePicker fromDatePicker = (DatePicker) view.findViewById(R.id.datePickerFromDate);
                        DatePicker toDatePicker = (DatePicker) view.findViewById(R.id.datePickerToDate);

                        mFromDate = DateUtils.getDateFromDatePicker(fromDatePicker);
                        mToDate = DateUtils.getDateFromDatePicker(toDatePicker);

                        String whereClause = ViewMobileData.Date + ">='" +
                                DateUtils.getSQLiteStringDate(getActivity(), mFromDate) + "' AND " +
                                ViewMobileData.Date + "<='" +
                                DateUtils.getSQLiteStringDate(getActivity(), mToDate) + "'";
                        //compose bundle
                        Bundle args = new Bundle();
                        args.putString(KEY_WHERE_CLAUSE, whereClause);
                        //starts loader
                        startLoader(args);

                        super.onPositive(dialog);
                    }
                })
                .show();
        // set date if is null
        if (mFromDate == null)
            mFromDate = Calendar.getInstance().getTime();
        if (mToDate == null)
            mToDate = Calendar.getInstance().getTime();

        View view = dialog.getCustomView();
        DatePicker fromDatePicker = (DatePicker) view.findViewById(R.id.datePickerFromDate);
        DatePicker toDatePicker = (DatePicker) view.findViewById(R.id.datePickerToDate);

        DateUtils.setDateToDatePicker(mFromDate, fromDatePicker);
        DateUtils.setDateToDatePicker(mToDate, toDatePicker);

    }
}
