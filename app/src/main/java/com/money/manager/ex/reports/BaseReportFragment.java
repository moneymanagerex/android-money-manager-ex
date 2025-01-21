/*
 * Copyright (C) 2012-2018 The Android Money Manager Ex Project Team
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

import android.app.AlertDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;

import androidx.cursoradapter.widget.CursorAdapter;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

import com.money.manager.ex.MmexApplication;
import com.money.manager.ex.R;
import com.money.manager.ex.common.BaseListFragment;
import com.money.manager.ex.common.MmxCursorLoader;
import com.money.manager.ex.core.InfoKeys;
import com.money.manager.ex.database.SQLDataSet;
import com.money.manager.ex.database.QueryAllData;
import com.money.manager.ex.datalayer.Select;
import com.money.manager.ex.servicelayer.InfoService;
import com.money.manager.ex.settings.AppSettings;
import com.money.manager.ex.utils.MmxDate;
import com.money.manager.ex.utils.MmxDateTimeUtils;

import java.util.Date;

import javax.inject.Inject;

import dagger.Lazy;
import timber.log.Timber;

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

        if (id == ID_LOADER) {
            if (args != null && args.containsKey(KEY_WHERE_CLAUSE)) {
                setWhereClause(args.getString(KEY_WHERE_CLAUSE));
            }
            String where = prepareQuery(getWhereClause());
            Select query = new Select()
                    .where(where);

            result = new MmxCursorLoader(getActivity(),  // context
                    new SQLDataSet().getUri(),          // uri
                    query);
        }
        return result;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == ID_LOADER) {//                ((CursorAdapter) getListAdapter()).swapCursor(null);
            ((CursorAdapter) getListAdapter()).changeCursor(null);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == ID_LOADER) {//                ((CursorAdapter) getListAdapter()).swapCursor(data);
            ((CursorAdapter) getListAdapter()).changeCursor(data);
            if (isResumed()) {
                setListShown(true);
            } else {
                setListShownNoAnimation(true);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        MmxDateTimeUtils dateUtils = dateTimeUtilsLazy.get();
        MmxDate dateTime = MmxDate.newDate();

        int itemId = item.getItemId();

        if (itemId == R.id.menu_current_month) {
            mDateFrom = dateTime.firstDayOfMonth().toDate();
            mDateTo = dateTime.lastDayOfMonth().toDate();
        } else if (itemId == R.id.menu_last_month) {
            mDateFrom = dateTime.minusMonths(1).firstDayOfMonth().toDate();
            mDateTo = dateTime.lastDayOfMonth().toDate();
        } else if (itemId == R.id.menu_last_30_days) {
            mDateTo = dateTime.toDate();
            mDateFrom = dateTime.minusDays(30).toDate();
        } else if (itemId == R.id.menu_current_year) {
            mDateFrom = dateTime.firstMonthOfYear().firstDayOfMonth().toDate();
            mDateTo = dateTime.lastMonthOfYear().lastDayOfMonth().toDate();
        } else if (itemId == R.id.menu_last_year) {
            mDateFrom = dateTime.minusYears(1)
                    .firstMonthOfYear()
                    .firstDayOfMonth()
                    .toDate();
            mDateTo = dateTime.lastMonthOfYear().lastDayOfMonth().toDate();
// issue #1790 - handling financial year
        } else if (itemId == R.id.menu_current_fin_year ||
                   itemId == R.id.menu_last_fin_year) {
            InfoService infoService = new InfoService(getActivity());
            int financialYearStartDay = Integer.valueOf(infoService.getInfoValue(InfoKeys.FINANCIAL_YEAR_START_DAY, "1"));
            int financialYearStartMonth = Integer.valueOf(infoService.getInfoValue(InfoKeys.FINANCIAL_YEAR_START_MONTH, "1"))-1;
            if (financialYearStartMonth < 0) financialYearStartMonth = 0;
            MmxDate newDate = MmxDate.newDate();
            newDate.setDate(financialYearStartDay);
            newDate.setMonth(financialYearStartMonth);
            if (newDate.toDate().after(dateTime.toDate())) {
                // today is not part of current financial year, so we need to go back on year
                newDate.minusYears(1);
            }
            // right now newDAte is start of current fiscal year
            if (itemId == R.id.menu_last_fin_year) {
                newDate.minusYears(1);
            }
            mDateFrom = newDate.toDate();
            mDateTo = newDate.addYear(1).minusDays(1).toDate();
            Timber.v("FISCAL YEAR from: " + mDateFrom.toString() + " to: " + mDateTo.toString());
        } else if (itemId == R.id.menu_all_time) {
            mDateFrom = null;
            mDateTo = null;
        } else if (itemId == R.id.menu_custom_dates) {
            // Check item
            item.setChecked(true);
            mItemSelected = itemId;
            // Show custom dates dialog
            showDialogCustomDates();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }

        String whereClause = null;
        if (mDateFrom != null && mDateTo != null) {
            whereClause = QueryAllData.Date + " >= '" + new MmxDate(mDateFrom).toIsoDateString() +
                "' AND " + QueryAllData.Date + " <= '" + new MmxDate(mDateTo).toIsoDateString() + "'";
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
        LoaderManager.getInstance(this).restartLoader(ID_LOADER, args, this);
    }

    protected String getWhereClause() {
        return mWhereClause;
    }

    private void showDialogCustomDates() {
        // Assuming mDateFrom, mDateTo, and KEY_WHERE_CLAUSE are class variables

        // Inflate the custom view
        View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_choose_date_report, null);
        DatePicker fromDatePicker = dialogView.findViewById(R.id.datePickerFromDate);
        DatePicker toDatePicker = dialogView.findViewById(R.id.datePickerToDate);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(dialogView)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    mDateFrom = dateTimeUtilsLazy.get().from(fromDatePicker);
                    mDateTo = dateTimeUtilsLazy.get().from(toDatePicker);

                    String whereClause =
                            QueryAllData.Date + ">='" + new MmxDate(mDateFrom).toIsoDateString() +
                                    "' AND " +
                                    QueryAllData.Date + "<='" + new MmxDate(mDateTo).toIsoDateString() + "'";

                    Bundle args = new Bundle();
                    args.putString(KEY_WHERE_CLAUSE, whereClause);

                    startLoader(args);
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
        // set date if is null
        if (mDateFrom == null) mDateFrom = new MmxDate().today().toDate();
        if (mDateTo == null) mDateTo = new MmxDate().today().toDate();

        dateTimeUtilsLazy.get().setDatePicker(mDateFrom, fromDatePicker);
        dateTimeUtilsLazy.get().setDatePicker(mDateTo, toDatePicker);
    }
}
