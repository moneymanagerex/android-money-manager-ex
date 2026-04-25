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

import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.cursoradapter.widget.CursorAdapter;
import androidx.lifecycle.Lifecycle;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

import com.money.manager.ex.R;
import com.money.manager.ex.common.BaseListFragment;
import com.money.manager.ex.common.MmxCursorLoader;
import com.money.manager.ex.database.SQLDataSet;
import com.money.manager.ex.database.QueryAllData;
import com.money.manager.ex.datalayer.Select;
import com.money.manager.ex.settings.AppSettings;
import com.money.manager.ex.settings.LookAndFeelSettings;
import com.money.manager.ex.utils.MmxDate;

import java.util.Date;
import java.util.List;

import timber.log.Timber;

public abstract class BaseReportFragment
    extends BaseListFragment
    implements LoaderManager.LoaderCallbacks<Cursor> {

    protected static final int ID_LOADER = 1;
    protected static final String KEY_ITEM_SELECTED = "PayeeReportFragment:ItemSelected";
    protected static final String KEY_WHERE_CLAUSE = "PayeeReportFragment:WhereClause";
    protected static final String KEY_FROM_DATE = "PayeeReportFragment:FromDate";
    protected static final String KEY_TO_DATE = "PayeeReportFragment:ToDate";
    protected static final int ACCOUNT_FILTER_DEFAULT_MODE = R.id.menu_account_filter_open;

    @Override
    protected boolean isFabAutoToggleEnabled() {
        return false;
    }

    protected int mItemSelected = R.id.menu_all_time;
    protected String mWhereClause = null;
    protected Date mDateFrom = null;
    protected Date mDateTo = null;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //set list view
        // setHasOptionsMenu(true);
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
        } else {
            restorePeriodSelectionFromPreferences();
        }

        mWhereClause = ReportDateRangeSupport.buildWhereClause(mDateFrom, mDateTo, QueryAllData.Date);
        //start loader
        startLoader(savedInstanceState);
    }

    @Override
    protected void addCustomMenuProviders(MenuHost menuHost) {
        menuHost.addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.menu_report, menu);
                menuInflater.inflate(R.menu.menu_period_picker, menu);

                if (isPeriodPickerActionVisible()) {
                    MenuItem periodItem = menu.findItem(R.id.menu_period);
                    if (periodItem != null) {
                        periodItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
                    }
                }

                if (isAccountFilterEnabled() && menu.findItem(R.id.menu_account_filter) == null) {
                    menuInflater.inflate(R.menu.menu_report_account_filter, menu);

                    int selectedMode = getAccountFilterMode();
                    MenuItem selectedItem = menu.findItem(selectedMode);
                    if (selectedItem != null) {
                        selectedItem.setChecked(true);
                    }
                }

                MenuItem item = menu.findItem(mItemSelected);
                if (item != null) {
                    item.setChecked(true);
                }

                old_onCreateOptionsMenu(menu, menuInflater);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                int itemId = menuItem.getItemId();

                if (isAccountFilterEnabled() && isAccountFilterMenuItem(itemId)) {
                    menuItem.setChecked(true);
                    saveAccountFilterMode(itemId);

                    if (itemId == R.id.menu_account_filter_custom) {
                        showAccountSelectionDialog();
                    } else {
                        onAccountFilterChanged();
                    }
                    return true;
                }

                if (itemId == R.id.menu_all_time) {
                    mDateFrom = null;
                    mDateTo = null;
                } else if (itemId == R.id.menu_custom_dates) {
                    menuItem.setChecked(true);
                    mItemSelected = itemId;
                    showDialogCustomDates();
                    return true;
                } else {
                    com.money.manager.ex.core.DateRange dateRange = ReportDateRangeSupport.resolveDateRange(requireContext(), itemId);
                    if (dateRange == null) {
                        return false;
                    }
                    mDateFrom = dateRange.dateFrom;
                    mDateTo = dateRange.dateTo;
                    Timber.v("Date range from: " + mDateFrom + " to: " + mDateTo);
                }

                String whereClause = ReportDateRangeSupport.buildWhereClause(mDateFrom, mDateTo, QueryAllData.Date);

                menuItem.setChecked(true);
                mItemSelected = itemId;
                savePeriodSelection();

                Bundle args = new Bundle();
                args.putString(KEY_WHERE_CLAUSE, whereClause);
                startLoader(args);

                return true;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }

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

    protected boolean isAccountFilterEnabled() {
        return true;
    }

    protected boolean isPeriodPickerActionVisible() {
        return true;
    }

    protected String getAccountFilterModePrefKey() {
        return getClass().getSimpleName() + ":FilterMode";
    }

    protected String getAccountFilterCustomPrefKey() {
        return getClass().getSimpleName() + ":FilterCustom";
    }

    protected String getPeriodSelectionPrefKey() {
        return getClass().getSimpleName() + ":PeriodSelection";
    }

    protected String getPeriodFromDatePrefKey() {
        return getClass().getSimpleName() + ":PeriodFromDate";
    }

    protected String getPeriodToDatePrefKey() {
        return getClass().getSimpleName() + ":PeriodToDate";
    }

    protected String getAccountFilterSelection(String accountIdColumn) {
        LookAndFeelSettings settings = new AppSettings(requireContext()).getLookAndFeelSettings();
        int mode = getAccountFilterMode();
        return AccountFilterSupport.getSelectionForAccountIdColumn(
                mode, settings, getAccountFilterCustomPrefKey(), accountIdColumn);
    }

    protected void onAccountFilterChanged() {
        startLoader(new Bundle());
    }

    private boolean isAccountFilterMenuItem(int itemId) {
        return AccountFilterSupport.isAccountFilterMenuItem(itemId);
    }

    private int getAccountFilterMode() {
        LookAndFeelSettings settings = new AppSettings(requireContext()).getLookAndFeelSettings();
        return AccountFilterSupport.getFilterMode(settings, getAccountFilterModePrefKey(), ACCOUNT_FILTER_DEFAULT_MODE);
    }

    private void saveAccountFilterMode(int mode) {
        LookAndFeelSettings settings = new AppSettings(requireContext()).getLookAndFeelSettings();
        AccountFilterSupport.saveFilterMode(settings, getAccountFilterModePrefKey(), mode);
    }

    private void showAccountSelectionDialog() {
        LookAndFeelSettings settings = new AppSettings(requireContext()).getLookAndFeelSettings();
        AccountFilterSupport.showAndPersistAccountSelectionDialog(
                requireContext(), settings, getAccountFilterCustomPrefKey(), this::onAccountFilterChanged);
    }

    private void showDialogCustomDates() {
        ReportDateRangeSupport.showCustomDateDialog(requireContext(), mDateFrom, mDateTo, (fromDate, toDate) -> {
            mDateFrom = fromDate;
            mDateTo = toDate;
            savePeriodSelection();
            String whereClause = ReportDateRangeSupport.buildWhereClause(mDateFrom, mDateTo, QueryAllData.Date);
            Bundle args = new Bundle();
            args.putString(KEY_WHERE_CLAUSE, whereClause);
            startLoader(args);
        });
    }

    private void restorePeriodSelectionFromPreferences() {
        LookAndFeelSettings settings = new AppSettings(requireContext()).getLookAndFeelSettings();

        String selectionValue = settings.get(getPeriodSelectionPrefKey(), Integer.toString(R.id.menu_all_time));
        try {
            mItemSelected = Integer.parseInt(selectionValue);
        } catch (Exception ignored) {
            mItemSelected = R.id.menu_all_time;
        }

        String fromDateString = settings.get(getPeriodFromDatePrefKey(), "");
        if (!fromDateString.trim().isEmpty()) {
            try {
                mDateFrom = new MmxDate(fromDateString).toDate();
            } catch (Exception ignored) {
                mDateFrom = null;
            }
        }

        String toDateString = settings.get(getPeriodToDatePrefKey(), "");
        if (!toDateString.trim().isEmpty()) {
            try {
                mDateTo = new MmxDate(toDateString).toDate();
            } catch (Exception ignored) {
                mDateTo = null;
            }
        }

        if (mItemSelected != R.id.menu_all_time && mItemSelected != R.id.menu_custom_dates) {
            com.money.manager.ex.core.DateRange dateRange =
                    ReportDateRangeSupport.resolveDateRange(requireContext(), mItemSelected);
            if (dateRange != null) {
                mDateFrom = dateRange.dateFrom;
                mDateTo = dateRange.dateTo;
            } else {
                mItemSelected = R.id.menu_all_time;
                mDateFrom = null;
                mDateTo = null;
            }
        }
    }

    private void savePeriodSelection() {
        LookAndFeelSettings settings = new AppSettings(requireContext()).getLookAndFeelSettings();
        settings.set(getPeriodSelectionPrefKey(), Integer.toString(mItemSelected));
        settings.set(getPeriodFromDatePrefKey(), mDateFrom == null ? "" : new MmxDate(mDateFrom).toIsoDateString());
        settings.set(getPeriodToDatePrefKey(), mDateTo == null ? "" : new MmxDate(mDateTo).toIsoDateString());
    }
}
