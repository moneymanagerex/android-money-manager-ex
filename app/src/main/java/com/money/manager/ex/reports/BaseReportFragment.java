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
package com.money.manager.ex.reports;

import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.money.manager.ex.MmexApplication;
import com.money.manager.ex.R;
import com.money.manager.ex.common.MmxCursorLoader;
import com.money.manager.ex.database.SQLDataSet;
import com.money.manager.ex.database.ViewMobileData;
import com.money.manager.ex.common.BaseListFragment;
import com.money.manager.ex.datalayer.Select;
import com.money.manager.ex.utils.MmxDate;
import com.money.manager.ex.utils.MmxDateTimeUtils;

import java.util.Date;

import javax.inject.Inject;

import androidx.cursoradapter.widget.CursorAdapter;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import dagger.Lazy;

public abstract class BaseReportFragment
    extends BaseListFragment
    implements LoaderManager.LoaderCallbacks<Cursor> {

    protected static final int ID_LOADER = 1;
    protected static final String KEY_ITEM_SELECTED = "PayeeReportFragment:ItemSelected";
    protected static final String KEY_WHERE_CLAUSE = "PayeeReportFragment:WhereClause";
    protected static final String KEY_FROM_DATE = "PayeeReportFragment:FromDate";
    protected static final String KEY_TO_DATE = "PayeeReportFragment:ToDate";

    @Inject Lazy<MmxDateTimeUtils> dateTimeUtilsLazy;

    protected int mItemSelected = R.id.menu_all_time;
    protected String mWhereClause = null;
    protected Date mDateFrom = null;
    protected Date mDateTo = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MmexApplication.getApp().iocComponent.inject(this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //set list view
        setHasOptionsMenu(true);
        setEmptyText(getString(R.string.no_data));
        setListShown(false);

        // Restore instance state.
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(KEY_ITEM_SELECTED))
                mItemSelected = savedInstanceState.getInt(KEY_ITEM_SELECTED);
            if (savedInstanceState.containsKey(KEY_FROM_DATE)) {
                String dateFromString = savedInstanceState.getString(KEY_FROM_DATE);
                mDateFrom = new MmxDate(dateFromString).toDate();
            }
            if (savedInstanceState.containsKey(KEY_TO_DATE)) {
                String dateToString = savedInstanceState.getString(KEY_TO_DATE);
                mDateTo = new MmxDate(dateToString).toDate();
            }
        }
        //start loader
        startLoader(savedInstanceState);
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

    // Loader events

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Loader<Cursor> result = null;

        switch (id) {
            case ID_LOADER:
                if (args != null && args.containsKey(KEY_WHERE_CLAUSE)) {
                    setWhereClause(args.getString(KEY_WHERE_CLAUSE));
                }
                String where = prepareQuery(getWhereClause());
                Select query = new Select()
                        .where(where);

                result = new MmxCursorLoader(getActivity(),  // context
                        new SQLDataSet().getUri(),          // uri
                        query);
                break;
        }
        return result;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case ID_LOADER:
//                ((CursorAdapter) getListAdapter()).swapCursor(null);
                ((CursorAdapter) getListAdapter()).changeCursor(null);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case ID_LOADER:
//                ((CursorAdapter) getListAdapter()).swapCursor(data);
                ((CursorAdapter) getListAdapter()).changeCursor(data);
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
//        MmxDateTimeUtils dateUtils = dateTimeUtilsLazy.get();
        MmxDate dateTime = MmxDate.newDate();

        switch (item.getItemId()) {
            case R.id.menu_current_month:
                mDateFrom = dateTime.firstDayOfMonth().toDate();
                mDateTo = dateTime.lastDayOfMonth().toDate();
                break;

            case R.id.menu_last_month:
                mDateFrom = dateTime.minusMonths(1)
                        .firstDayOfMonth().toDate();
                mDateTo = dateTime.lastDayOfMonth().toDate();
                break;

            case R.id.menu_last_30_days:
                mDateTo = dateTime.toDate();
                mDateFrom = dateTime.minusDays(30).toDate();
                break;

            case R.id.menu_current_year:
                mDateFrom = dateTime.firstMonthOfYear().firstDayOfMonth().toDate();
                mDateTo = dateTime.lastMonthOfYear().lastDayOfMonth().toDate();
                break;

            case R.id.menu_last_year:
                mDateFrom = dateTime.minusYears(1)
                        .firstMonthOfYear()
                        .firstDayOfMonth()
                        .toDate();
                mDateTo = dateTime
                        .lastMonthOfYear()
                        .lastDayOfMonth()
                        .toDate();
                break;

            case R.id.menu_all_time:
                mDateFrom = null;
                mDateTo = null;
                break;
            case R.id.menu_custom_dates:
                //check item
                item.setChecked(true);
                mItemSelected = item.getItemId();
                //show binaryDialog
                showDialogCustomDates();
                return true;
//                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        String whereClause = null;
        if (mDateFrom != null && mDateTo != null) {
            whereClause = ViewMobileData.Date + " >= '" + new MmxDate(mDateFrom).toIsoDateString() +
                "' AND " + ViewMobileData.Date + " <= '" + new MmxDate(mDateTo).toIsoDateString() + "'";
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
        if (mDateFrom != null) {
            outState.putString(KEY_FROM_DATE, new MmxDate(mDateFrom).toIsoDateString());
        }
        if (mDateTo != null) {
            outState.putString(KEY_TO_DATE, new MmxDate(mDateTo).toIsoDateString());
        }
    }

    protected View addListViewHeaderFooter(int layout) {
        return View.inflate(getActivity(), layout, null);
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

    protected String getWhereClause() {
        return mWhereClause;
    }

    private void showDialogCustomDates() {
        MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
            .customView(R.layout.dialog_choose_date_report, false)
            .positiveText(android.R.string.ok)
            .onPositive(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(MaterialDialog materialDialog, DialogAction dialogAction) {
                    View view = materialDialog.getCustomView();
                    DatePicker fromDatePicker = (DatePicker) view.findViewById(R.id.datePickerFromDate);
                    DatePicker toDatePicker = (DatePicker) view.findViewById(R.id.datePickerToDate);

                    mDateFrom = dateTimeUtilsLazy.get().from(fromDatePicker);
                    mDateTo = dateTimeUtilsLazy.get().from(toDatePicker);

                    String whereClause =
                        ViewMobileData.Date + ">='" + new MmxDate(mDateFrom).toIsoDateString() +
                                "' AND " +
                        ViewMobileData.Date + "<='" + new MmxDate(mDateTo).toIsoDateString() + "'";

                    Bundle args = new Bundle();
                    args.putString(KEY_WHERE_CLAUSE, whereClause);

                    startLoader(args);

                    //super.onPositive(binaryDialog);
                }
            })
            .show();
        // set date if is null
        if (mDateFrom == null) mDateFrom = new MmxDate().today().toDate();
        if (mDateTo == null) mDateTo = new MmxDate().today().toDate();

        View view = dialog.getCustomView();
        DatePicker fromDatePicker = (DatePicker) view.findViewById(R.id.datePickerFromDate);
        DatePicker toDatePicker = (DatePicker) view.findViewById(R.id.datePickerToDate);

        dateTimeUtilsLazy.get().setDatePicker(mDateFrom, fromDatePicker);
        dateTimeUtilsLazy.get().setDatePicker(mDateTo, toDatePicker);
    }
}
