/*
 * Copyright (C) 2012-2014 Alessandro Lazzari
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
package com.money.manager.ex;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.money.manager.ex.adapter.AllDataAdapter;
import com.money.manager.ex.adapter.AllDataAdapter.TypeCursor;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.core.Passcode;
import com.money.manager.ex.database.QueryBillDeposits;
import com.money.manager.ex.database.TableAccountList;
import com.money.manager.ex.database.TableBillsDeposits;
import com.money.manager.ex.fragment.BaseFragmentActivity;
import com.money.manager.ex.fragment.BaseListFragment;
import com.money.manager.ex.utils.DateUtils;

import java.util.Date;

/**
 * @author Alessandro Lazzari (lazzari.ale@gmail.com)
 */
public class RepeatingTransactionListActivity extends BaseFragmentActivity {
    public static final String INTENT_EXTRA_LAUNCH_NOTIFICATION = "RepeatingTransactionListActivity:LaunchNotification";
    public static final int INTENT_REQUEST_PASSCODE = 2;
    public static final String INTENT_RESULT_ACCOUNTID = "AccountListActivity:ACCOUNTID";
    public static final String INTENT_RESULT_ACCOUNTNAME = "AccountListActivity:ACCOUNTNAME";
    @SuppressWarnings("unused")
    private static final String LOGCAT = RepeatingTransactionListActivity.class.getSimpleName();
    private static final String FRAGMENTTAG = RepeatingTransactionListActivity.class.getSimpleName() + "_Fragment";
    // ID loader
    private static final int ID_LOADER_REPEATING = 0;
    private RepeatingTransactionListFragment listFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // check if launch from notification
        if (getIntent() != null && getIntent().getBooleanExtra(INTENT_EXTRA_LAUNCH_NOTIFICATION, false)) {
            Passcode passcode = new Passcode(getApplicationContext());
            if (passcode.hasPasscode()) {
                Intent intent = new Intent(this, PasscodeActivity.class);
                // set action and data
                intent.setAction(PasscodeActivity.INTENT_REQUEST_PASSWORD);
                intent.putExtra(PasscodeActivity.INTENT_MESSAGE_TEXT, getString(R.string.enter_your_passcode));
                // start activity
                startActivityForResult(intent, INTENT_REQUEST_PASSCODE);
            }
        }
        // set actionbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // set fragment and fragment manager
        FragmentManager fm = getSupportFragmentManager();
        listFragment = new RepeatingTransactionListFragment();
        // attach fragment on activity
        if (fm.findFragmentById(android.R.id.content) == null) {
            fm.beginTransaction().add(android.R.id.content, listFragment, FRAGMENTTAG).commit();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // check request code
        switch (requestCode) {
            case INTENT_REQUEST_PASSCODE:
                boolean isAuthenticated = false;
                if (resultCode == RESULT_OK && data != null) {
                    Passcode passcode = new Passcode(getApplicationContext());
                    String passIntent = data.getStringExtra(PasscodeActivity.INTENT_RESULT_PASSCODE);
                    String passDb = passcode.getPasscode();
                    if (passIntent != null && passDb != null) {
                        isAuthenticated = passIntent.equals(passDb);
                        if (!isAuthenticated) {
                            Toast.makeText(getApplicationContext(), R.string.passocde_no_macth, Toast.LENGTH_LONG).show();
                        }
                    }
                }
                // close if not authenticated
                if (!isAuthenticated) {
                    this.finish();
                }
        }
    }

    public static class RepeatingTransactionListFragment extends BaseListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
        /**
         * start RepeatingTransaction Activity for insert
         */
        public static final String INTENT_EXTRA_LAUNCH_NOTIFICATION = "RepeatingTransactionListActivity:LaunchNotification";
        // ID request to add repeating transaction
        private static final int REQUEST_ADD_REPEATING_TRANSACTION = 1001;
        private static final int REQUEST_ADD_TRANSACTION = 1002;
        // ID item menu add
        private static final int MENU_ITEM_ADD = 1;
        // query
        private static QueryBillDeposits mBillDeposits;
        // filter
        private String mCurFilter;

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            // create a object query
            mBillDeposits = new QueryBillDeposits(getActivity());
            // set listview
            setEmptyText(getActivity().getResources().getString(R.string.repeating_empty_transaction));
            setHasOptionsMenu(true);
            registerForContextMenu(getListView());
            getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            getListView().setDivider(new ColorDrawable(new Core(getActivity().getApplicationContext()).resolveIdAttribute(R.attr.theme_background_color)));

            setListShown(false);
            // start loaderapplication.getSQLiteStringDate(date)
            getLoaderManager().initLoader(ID_LOADER_REPEATING, null, this);
            // set fab visible
            setFloatingActionButtonVisbile(true);
            setFloatingActionButtonAttachListView(true);
        }

        @Override
        public void onFloatingActionButtonClickListener() {
            startRepeatingTransactionActivity();
        }

        @Override
        public boolean onContextItemSelected(android.view.MenuItem item) {
            String nextOccurrence;
            int repeats, bdId;
            Date date;

            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            // take cursor and move to position
            Cursor cursor = ((AllDataAdapter) getListAdapter()).getCursor();
            if (cursor != null) {
                cursor.moveToPosition(info.position);
                //quick-fix convert 'switch' to 'if-else'
                if (item.getItemId() == R.id.menu_enter_next_occurrence) {
                    nextOccurrence = cursor.getString(cursor.getColumnIndex(TableBillsDeposits.NEXTOCCURRENCEDATE));
                    repeats = cursor.getInt(cursor.getColumnIndex(TableBillsDeposits.REPEATS));
                    bdId = cursor.getInt(cursor.getColumnIndex(TableBillsDeposits.BDID));
                    date = DateUtils.getDateFromString(getActivity().getApplicationContext(), nextOccurrence, MoneyManagerApplication.PATTERN_DB_DATE);
                    date = DateUtils.getDateNextOccurence(date, repeats);
                    if (date != null) {
                        Intent intent = new Intent(getActivity(), CheckingAccountActivity.class);
                        intent.setAction(Constants.INTENT_ACTION_INSERT);
                        intent.putExtra(CheckingAccountActivity.KEY_BDID_ID, bdId);
                        intent.putExtra(CheckingAccountActivity.KEY_NEXT_OCCURRENCE, DateUtils.getSQLiteStringDate(getActivity(), date));
                        // start for insert new transaction
                        startActivityForResult(intent, REQUEST_ADD_TRANSACTION);
                    }
                } else if (item.getItemId() == R.id.menu_skip_next_occurrence) {
                    nextOccurrence = cursor.getString(cursor.getColumnIndex(TableBillsDeposits.NEXTOCCURRENCEDATE));
                    repeats = cursor.getInt(cursor.getColumnIndex(TableBillsDeposits.REPEATS));
                    bdId = cursor.getInt(cursor.getColumnIndex(TableBillsDeposits.BDID));
                    date = DateUtils.getDateFromString(getActivity().getApplicationContext(), nextOccurrence, MoneyManagerApplication.PATTERN_DB_DATE);
                    date = DateUtils.getDateNextOccurence(date, repeats);
                    if (date != null) {
                        ContentValues values = new ContentValues();
                        values.put(TableBillsDeposits.NEXTOCCURRENCEDATE, DateUtils.getSQLiteStringDate(getActivity(), date));
                        // update date
                        if (getActivity().getContentResolver().update(new TableBillsDeposits().getUri(), values, TableBillsDeposits.BDID + "=?", new String[]{Integer.toString(bdId)}) > 0) {
                            getLoaderManager().restartLoader(ID_LOADER_REPEATING, null, this);
                        } else {
                            Toast.makeText(getActivity(), R.string.db_update_failed, Toast.LENGTH_SHORT).show();
                        }
                    }
                } else if (item.getItemId() == R.id.menu_edit) {
                    startRepeatingTransactionActivity(cursor.getInt(cursor.getColumnIndex(TableBillsDeposits.BDID)));
                } else if (item.getItemId() == R.id.menu_delete) {
                    showDialogDeleteRepeatingTransaction(cursor.getInt(cursor.getColumnIndex(TableBillsDeposits.BDID)));
                }
            }
            return false;
        }

        private void showDialogDeleteRepeatingTransaction(final int BDID) {
            // create alert dialog
            AlertDialogWrapper.Builder alertDialog = new AlertDialogWrapper.Builder(getActivity());
            alertDialog.setTitle(R.string.delete_repeating_transaction);
            alertDialog.setMessage(R.string.confirmDelete);
            // set listener
            alertDialog.setPositiveButton(android.R.string.ok,
                    new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (getActivity().getContentResolver().delete(new TableBillsDeposits().getUri(), TableBillsDeposits.BDID + "=" + BDID, null) == 0) {
                                Toast.makeText(getActivity(), R.string.db_delete_failed, Toast.LENGTH_SHORT).show();
                            }
                            // restart loader
                            getLoaderManager().restartLoader(ID_LOADER_REPEATING, null, RepeatingTransactionListFragment.this);
                        }
                    });
            alertDialog.setNegativeButton(android.R.string.cancel, new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            alertDialog.create().show();
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
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
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            switch (id) {
                case ID_LOADER_REPEATING:
                    String select = null;
                    if (!TextUtils.isEmpty(mCurFilter)) {
                        select = TableAccountList.ACCOUNTNAME + " LIKE '" + mCurFilter + "%'";
                    }
                    return new CursorLoader(getActivity(), mBillDeposits.getUri(), mBillDeposits.getAllColumns(), select, null, QueryBillDeposits.NEXTOCCURRENCEDATE);
            }

            return null;
        }

        @Override
        public void onListItemClick(ListView l, View v, int position, long id) {
            super.onListItemClick(l, v, position, id);
            // Intent launcher here
            // todo: show context menu here
            System.out.println("touch");
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            switch (loader.getId()) {
                case ID_LOADER_REPEATING:
            }
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            switch (loader.getId()) {
                case ID_LOADER_REPEATING:
                    AllDataAdapter adapter = new AllDataAdapter(getActivity(), data, TypeCursor.REPEATINGTRANSACTION);
                    setListAdapter(adapter);

                    if (isResumed()) {
                        setListShown(true);
                    } else {
                        setListShownNoAnimation(true);
                    }
            }
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            // Called when the action bar search text has changed. Update
            // the search filter, and restart the loader to do a new query
            // with this filter.
            mCurFilter = !TextUtils.isEmpty(newText) ? newText : null;
            getLoaderManager().restartLoader(ID_LOADER_REPEATING, null, this);
            return true;
        }

        private void startRepeatingTransactionActivity() {
            startRepeatingTransactionActivity(null);
        }

        /**
         * start RepeatingTransaction for insert or edit transaction
         *
         * @param billDepositsId
         */
        private void startRepeatingTransactionActivity(Integer billDepositsId) {
            // create intent, set Bill Deposits ID
            Intent intent = new Intent(getActivity(), RepeatingTransactionActivity.class);
            // check transId not null
            if (billDepositsId != null) {
                intent.putExtra(RepeatingTransactionActivity.KEY_BILL_DEPOSITS_ID, billDepositsId);
                intent.setAction(Intent.ACTION_EDIT);
            } else {
                intent.setAction(Intent.ACTION_INSERT);
            }
            // launch activity
            startActivityForResult(intent, REQUEST_ADD_REPEATING_TRANSACTION);
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (resultCode == RESULT_OK) {
                switch (requestCode) {
                    case REQUEST_ADD_REPEATING_TRANSACTION:
                    case REQUEST_ADD_TRANSACTION:
                        getLoaderManager().restartLoader(ID_LOADER_REPEATING, null, this);
                }
            }
        }

        @Override
        public String getSubTitle() {
            return getString(R.string.repeating_transactions);
        }
    }
}
