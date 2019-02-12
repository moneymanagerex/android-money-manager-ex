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
package com.money.manager.ex.recurring.transactions;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.mmex_icon_font_typeface_library.MMXIconFont;
import com.money.manager.ex.MmexApplication;
import com.money.manager.ex.common.MmxCursorLoader;
import com.money.manager.ex.datalayer.Select;
import com.money.manager.ex.log.ExceptionHandler;
import com.money.manager.ex.core.UIHelper;
import com.money.manager.ex.datalayer.RecurringTransactionRepository;
import com.money.manager.ex.domainmodel.Account;
import com.money.manager.ex.domainmodel.RecurringTransaction;
import com.money.manager.ex.transactions.CheckingTransactionEditActivity;
import com.money.manager.ex.R;
import com.money.manager.ex.adapter.AllDataAdapter;
import com.money.manager.ex.servicelayer.RecurringTransactionService;
import com.money.manager.ex.transactions.EditTransactionActivityConstants;
import com.money.manager.ex.database.QueryBillDeposits;
import com.money.manager.ex.common.BaseListFragment;
import com.money.manager.ex.utils.MmxDateTimeUtils;
import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidListener;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;

import androidx.fragment.app.FragmentTransaction;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import dagger.Lazy;

/**
 * The recurring transactions list fragment.
 * Includes floating action button.
 */
public class RecurringTransactionListFragment
    extends BaseListFragment
    implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int REQUEST_ADD_REPEATING_TRANSACTION = 1001;
    private static final int REQUEST_ADD_TRANSACTION = 1002;
    private static final int REQUEST_EDIT_REPEATING_TRANSACTION = 1003;

    private static final int ID_LOADER_REPEATING = 0;

    private static QueryBillDeposits mBillDeposits;

    @Inject
    Lazy<MmxDateTimeUtils> dateTimeUtilsLazy;

    // filter
    private String mCurFilter;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // create a object query
        mBillDeposits = new QueryBillDeposits(getActivity());
        // set list view
        setEmptyText(getActivity().getResources().getString(R.string.repeating_empty_transaction));
        setHasOptionsMenu(true);
        registerForContextMenu(getListView());
        getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        UIHelper uiHelper = new UIHelper(getActivity());
        getListView().setDivider(new ColorDrawable(uiHelper.resolveAttribute(R.attr.theme_background_color)));

        setListShown(false);

        getLoaderManager().initLoader(ID_LOADER_REPEATING, null, this);

        // show floating button.
        setFloatingActionButtonVisible(true);
        attachFloatingActionButtonToListView();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MmexApplication.getApp().iocComponent.inject(this);

        setHasOptionsMenu(true);
    }

    @Override
    public void onFloatingActionButtonClicked() {
        // create new recurring transaction.
        startRecurringTransactionEditActivity(null, REQUEST_ADD_REPEATING_TRANSACTION);
    }

    // Menu

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

//        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_item_calendar, menu);
        MenuItem calendar = menu.findItem(R.id.menuCalendar);
        if (calendar != null) {
            Drawable icon = new UIHelper(getActivity()).getIcon(MMXIconFont.Icon.mmx_calendar);
            calendar.setIcon(icon);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle calendar
        switch (item.getItemId()) {
            case R.id.menuCalendar:
                showCaldroidFragment();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        // show context menu here.
        getActivity().openContextMenu(v);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        // take a cursor and move to position
        Cursor cursor = ((AllDataAdapter) getListAdapter()).getCursor();
        if (cursor != null) {
            cursor.moveToPosition(info.position);
            // set title and inflate menu
            menu.setHeaderTitle(getActivity().getTitle());
            getActivity().getMenuInflater().inflate(R.menu.contextmenu_repeating_transactions, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(android.view.MenuItem item) {

        ContextMenu.ContextMenuInfo menuInfo = item.getMenuInfo();
        if (menuInfo == null) {
            String errorMessage = "no context menu info";
            Log.w(this.getClass().getSimpleName(), errorMessage);
            ExceptionHandler handler = new ExceptionHandler(getActivity(), this);
            handler.showMessage("no context menu info");
            return false;
        }
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;

        int menuItemId = item.getItemId();
        int transactionId = (int) info.id;

        switch (menuItemId) {
            case R.id.menu_enter_next_occurrence:
                showCreateTransactionActivity(transactionId);
                break;

            case R.id.menu_skip_next_occurrence:
                confirmSkip(transactionId);
                break;

            case R.id.menu_edit:
                startRecurringTransactionEditActivity(transactionId, REQUEST_EDIT_REPEATING_TRANSACTION);
                break;

            case R.id.menu_delete:
                confirmDelete(transactionId);
                break;
        }

        return false;
    }

    // Loader callbacks.

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case ID_LOADER_REPEATING:
                String select = null;
                if (!TextUtils.isEmpty(mCurFilter)) {
                    select = Account.ACCOUNTNAME + " LIKE '" + mCurFilter + "%'";
                }
                Select query = new Select(mBillDeposits.getAllColumns())
                        .where(select)
                        .orderBy(QueryBillDeposits.NEXTOCCURRENCEDATE);

                return new MmxCursorLoader(getActivity(), mBillDeposits.getUri(), query);
        }

        return null;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case ID_LOADER_REPEATING:
                AllDataAdapter adapter = (AllDataAdapter) getListAdapter();
                if (adapter != null) {
//                    adapter.swapCursor(null);
                    adapter.changeCursor(null);
                }
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case ID_LOADER_REPEATING:
                if (data == null) return;

                AllDataAdapter adapter = new AllDataAdapter(getActivity(), data,
                        AllDataAdapter.TypeCursor.RECURRINGTRANSACTION);
                setListAdapter(adapter);

                if (isResumed()) {
                    setListShown(true);
                    if (data.getCount() <= 0 && getFloatingActionButton() != null)
                        getFloatingActionButton().show(true);
                } else {
                    setListShownNoAnimation(true);
                }
        }
    }

    // Other

    @Override
    public boolean onQueryTextChange(String newText) {
        // Called when the action bar search text has changed. Update
        // the search filter, and restart the loader to do a new query
        // with this filter.
        mCurFilter = !TextUtils.isEmpty(newText) ? newText : null;
        getLoaderManager().restartLoader(ID_LOADER_REPEATING, null, this);
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RecurringTransactionListActivity.RESULT_OK) {
//                switch (requestCode) {
//                    case REQUEST_ADD_REPEATING_TRANSACTION:
//                        break;
//                    case REQUEST_ADD_TRANSACTION:
//                        break;
//                    case REQUEST_EDIT_REPEATING_TRANSACTION:
//                        break;
//                }
            // Always reload the activity?
            getLoaderManager().restartLoader(ID_LOADER_REPEATING, null, this);
        }
    }

    @Override
    public String getSubTitle() {
        return getString(R.string.recurring_transactions);
    }

    // private

    private void confirmDelete(final int id) {
        UIHelper ui = new UIHelper(getContext());

        // create alert binaryDialog
        new MaterialDialog.Builder(getContext())
            .title(R.string.delete_repeating_transaction)
            .icon(ui.getIcon(FontAwesome.Icon.faw_question_circle_o))
            .content(R.string.confirmDelete)
                .positiveText(android.R.string.ok)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        RecurringTransactionService recurringTransaction = new RecurringTransactionService(id, getActivity());
                        recurringTransaction.delete();

                        // restart loader
                        getLoaderManager().restartLoader(ID_LOADER_REPEATING,
                                null, RecurringTransactionListFragment.this);
                    }
                })
                .negativeText(android.R.string.cancel)
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.cancel();
                    }
                })
            .build().show();
    }

    private void confirmSkip(final int id) {
        UIHelper ui = new UIHelper(getContext());

        new MaterialDialog.Builder(getContext())
            .title(R.string.skip_next_occurrence)
            .icon(ui.getIcon(FontAwesome.Icon.faw_question_circle_o))
            .content(R.string.skip_next_occurrence_confirmation)
                .positiveText(android.R.string.ok)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        RecurringTransactionService recurringTransaction = new RecurringTransactionService(id, getActivity());
                        recurringTransaction.moveNextOccurrence();
                        getLoaderManager().restartLoader(ID_LOADER_REPEATING, null,
                                RecurringTransactionListFragment.this);
                    }
                })
                .negativeText(android.R.string.cancel)
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.cancel();
                    }
                })
            .build().show();
    }

    private void showCaldroidFragment() {
        Locale appLocale = MmexApplication.getApp().getAppLocale();
        CaldroidFragment caldroidFragment = new CaldroidFragment();


        // Customization
        Bundle args = new Bundle();
        Calendar cal = Calendar.getInstance(appLocale);
        args.putInt(CaldroidFragment.MONTH, cal.get(Calendar.MONTH) + 1);
        args.putInt(CaldroidFragment.YEAR, cal.get(Calendar.YEAR));
        args.putInt(CaldroidFragment.START_DAY_OF_WEEK, dateTimeUtilsLazy.get().getFirstDayOfWeek());
        if (new UIHelper(getActivity()).isUsingDarkTheme()) {
            args.putInt(CaldroidFragment.THEME_RESOURCE, com.caldroid.R.style.CaldroidDefaultDark);
        }
        // disable switching month for now.
        args.putBoolean(CaldroidFragment.SHOW_NAVIGATION_ARROWS, Boolean.FALSE);
        args.putBoolean(CaldroidFragment.ENABLE_SWIPE, Boolean.FALSE);
        caldroidFragment.setArguments(args);

        // add different background for dates with events.
        showDatesWithEvents(caldroidFragment);

        // behaviour
        caldroidFragment.setCaldroidListener(getCalendarListener());

        FragmentTransaction t = getActivity().getSupportFragmentManager()
                .beginTransaction();
        t.replace(R.id.fragmentMain, caldroidFragment);
        t.addToBackStack(null);
        t.commit();
    }

    private CaldroidListener getCalendarListener() {
        return new CaldroidListener() {

            @Override
            public void onSelectDate(Date date, View view) {
//                fragment.setCalendarDate(date);
                // todo show the recurring transactions on this date.
            }

            @Override
            public void onChangeMonth(int month, int year) {
            }

            @Override
            public void onLongClickDate(Date date, View view) {
            }

            @Override
            public void onCaldroidViewCreated() {
            }

        };
    }

    private void showDatesWithEvents(CaldroidFragment caldroid) {
        ListAdapter adapter = getListAdapter();
        int count = adapter.getCount();
        ColorDrawable orange = new ColorDrawable(ContextCompat.getColor(getActivity(), R.color.holo_orange_dark));
        RecurringTransaction tx = RecurringTransaction.createInstance();

        for (int i = 0; i < count; i++) {
            Cursor cursor = (Cursor) adapter.getItem(i);
            tx.loadFromCursor(cursor);

            caldroid.setBackgroundDrawableForDate(orange, tx.getPaymentDate());
        }
    }

    private void showCreateTransactionActivity(int recurringTransactionId) {
        RecurringTransactionRepository repo = new RecurringTransactionRepository(getActivity());
        RecurringTransaction tx = repo.load(recurringTransactionId);
        if (tx == null) return;

        Intent intent = new Intent(getActivity(), CheckingTransactionEditActivity.class);
        intent.setAction(Intent.ACTION_INSERT);
        intent.putExtra(EditTransactionActivityConstants.KEY_BDID_ID, recurringTransactionId);
        intent.putExtra(EditTransactionActivityConstants.KEY_TRANS_SOURCE, "RecurringTransactionListFragment.java");
        // start for insert new transaction
        startActivityForResult(intent, REQUEST_ADD_TRANSACTION);
    }

    /**
     * start RepeatingTransaction for insert or edit transaction
     *
     * @param billDepositsId Id of the recurring transaction.
     * @param purposeCode       Code that indicates why we are opening the editor.
     *                          example: REQUEST_ADD_REPEATING_TRANSACTION
     */
    private void startRecurringTransactionEditActivity(Integer billDepositsId, int purposeCode) {
        // create intent, set Bill Deposits ID
        Intent intent = new Intent(getActivity(), RecurringTransactionEditActivity.class);
        // check transId not null
        if (billDepositsId != null) {
            intent.putExtra(RecurringTransactionEditActivity.KEY_BILL_DEPOSITS_ID, billDepositsId);
            intent.setAction(Intent.ACTION_EDIT);
        } else {
            intent.setAction(Intent.ACTION_INSERT);
        }
        // launch activity
        startActivityForResult(intent, purposeCode);
    }
}
