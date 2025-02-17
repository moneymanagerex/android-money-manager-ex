/*
 * Copyright (C) 2012-2025 The Android Money Manager Ex Project Team
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
 *
 * developer wolfsolver
 */
package com.money.manager.ex.reports;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import androidx.cursoradapter.widget.SimpleCursorAdapter;

import com.money.manager.ex.MmexApplication;
import com.money.manager.ex.R;
import com.money.manager.ex.adapter.MoneySimpleCursorAdapter;
import com.money.manager.ex.common.BaseListFragment;
import com.money.manager.ex.core.TransactionTypes;
import com.money.manager.ex.currency.CurrencyService;
import com.money.manager.ex.database.QueryAccountBills;
import com.money.manager.ex.database.QueryBillDeposits;
import com.money.manager.ex.datalayer.Select;
import com.money.manager.ex.domainmodel.RecurringTransaction;
import com.money.manager.ex.servicelayer.InfoService;
import com.money.manager.ex.servicelayer.RecurringTransactionService;
import com.money.manager.ex.settings.AppSettings;
import com.money.manager.ex.settings.LookAndFeelSettings;
import com.money.manager.ex.utils.MmxDate;
import com.money.manager.ex.utils.MmxDateTimeUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import info.javaperformance.money.Money;
import info.javaperformance.money.MoneyFactory;

/* Master note
   Use AllDataAdapter as master view
   - no account selection (like RECURRINGTRANSACTION)
   - Account Balance (like ALLDATA)
    private boolean mShowAccountName = false; // not show header with account
    private boolean mShowBalanceAmount = true; // show balance on transaction


 */

public class CashFlowReportListFragment
        extends BaseListFragment {

    private static final int ID_LOADER_REPORT = 1;
    private double totalAmount = 0;
    private static final String ID = "_id";
    private static final String BALANCE = "BALANCE";
    private int monthInAdvance = 12;

    private MatrixCursor matrixCursor;
    String[] columnNames;
    CurrencyService currencyService;
    MmxDateTimeUtils dateUtils;
    InfoService infoService;
    MoneySimpleCursorAdapter adapter;
    ArrayList<Long> selectedAccounts = new ArrayList<>();

    @SuppressLint("Range")
    private void createCashFlowRecords() {
        // Use MatrixCursor instead
        if (matrixCursor != null) return;

        MmxDate endDate = new MmxDate();
        endDate.addMonth(monthInAdvance);

        // since base report is based on AllDataAdapter, we reuse global variable
        columnNames = new String[]{
                ID,
                QueryBillDeposits.TRANSDATE,
                QueryBillDeposits.PAYEENAME,
                QueryBillDeposits.CATEGNAME,
                QueryBillDeposits.COLOR,
                QueryBillDeposits.ATTACHMENTCOUNT,
                QueryBillDeposits.TAGS,
                QueryBillDeposits.NOTES,
                QueryBillDeposits.STATUS,
                QueryBillDeposits.AMOUNT,
                "transCurrency",
                BALANCE
        };
        matrixCursor = new MatrixCursor(columnNames);

        QueryBillDeposits billDeposits = new QueryBillDeposits(getContext());

        Cursor cursor = getContext().getContentResolver().query(billDeposits.getUri(),
                billDeposits.getAllColumns(),
                null,
                null,
                QueryBillDeposits.NEXTOCCURRENCEDATE);
        if (cursor == null ||
                cursor.getCount() == 0)
            return;

        getTotalAmountAndAccounts();


        List<HashMap<String, Object>> listRecurring = new ArrayList<>();
        HashMap<String, Object> row;
        while (cursor.moveToNext()) {
            RecurringTransactionService recurringTransactionService = new RecurringTransactionService(cursor.getLong(cursor.getColumnIndex(QueryBillDeposits.BDID)), getContext());
            // ignore transfert if both is on selected account
            // create recurring transaction
            double amount = cursor.getDouble(cursor.getColumnIndex(QueryBillDeposits.AMOUNT));
            RecurringTransaction rx = recurringTransactionService.getSimulatedTransaction();
            if (rx.getTransactionType() == TransactionTypes.Transfer ) {
                if (selectedAccounts.contains(rx.getAccountId()) &&
                        selectedAccounts.contains(rx.getAccountToId())) {
                    // both in
                    continue; // skip
                }
                if (!selectedAccounts.contains(rx.getAccountId()) &&
                        !selectedAccounts.contains(rx.getAccountToId())) {
                    // both out
                    continue; // skip
                }
                if (selectedAccounts.contains(rx.getAccountId()) ) {
                    // source in
                    amount = 0 - amount;
                } else {
                    // dest in
//                    amount = rx.getAmountTo().toDouble();
                }
            } else {
//                amount = rx.getAmount().toDouble();
            }

            row = new HashMap<>();
            row.put(ID, cursor.getLong(cursor.getColumnIndex(QueryBillDeposits.BDID)));
            row.put(QueryBillDeposits.TRANSDATE, cursor.getString(cursor.getColumnIndex(QueryBillDeposits.NEXTOCCURRENCEDATE)));
            row.put(QueryBillDeposits.PAYEENAME, cursor.getString(cursor.getColumnIndex(QueryBillDeposits.PAYEENAME)));
            if (row.get(QueryBillDeposits.PAYEENAME) == null)
                row.put(QueryBillDeposits.PAYEENAME, cursor.getString(cursor.getColumnIndex(QueryBillDeposits.ACCOUNTNAME)));
            row.put(QueryBillDeposits.CATEGNAME, cursor.getString(cursor.getColumnIndex(QueryBillDeposits.CATEGNAME)));
            if (row.get(QueryBillDeposits.CATEGNAME) == null)
                row.put(QueryBillDeposits.CATEGNAME, getString(R.string.transfer));
            row.put(QueryBillDeposits.COLOR, Objects.requireNonNullElse(cursor.getLong(cursor.getColumnIndex(QueryBillDeposits.COLOR)),-1L)); // handle null #2235
            row.put(QueryBillDeposits.ATTACHMENTCOUNT, Objects.requireNonNullElse(cursor.getLong(cursor.getColumnIndex(QueryBillDeposits.ATTACHMENTCOUNT)),0L)); // handle null #2235
            row.put(QueryBillDeposits.TAGS, Objects.requireNonNullElse(cursor.getString(cursor.getColumnIndex(QueryBillDeposits.TAGS)),"")); // handle null #2235
            row.put(QueryBillDeposits.NOTES, cursor.getString(cursor.getColumnIndex(QueryBillDeposits.NOTES)));
            row.put(QueryBillDeposits.STATUS, cursor.getString(cursor.getColumnIndex(QueryBillDeposits.STATUS)));
            row.put(QueryBillDeposits.AMOUNT, amount);
            row.put("transCurrency", cursor.getLong(cursor.getColumnIndex(QueryBillDeposits.CURRENCYID)));
            row.put(BALANCE, 0);
            listRecurring.add(row);

            int limit = monthInAdvance * 31;
            while (limit > 0 && recurringTransactionService.simulateMoveNext() && recurringTransactionService.getSimulatedTransaction().getDate().before(endDate.toDate())) {
                limit -= 1;
                HashMap<String, Object> row2 = new HashMap<>();
                row2.put(ID, row.get(ID));
                row2.put(QueryBillDeposits.TRANSDATE, recurringTransactionService.getSimulatedTransaction().getPaymentDateString());
                row2.put(QueryBillDeposits.PAYEENAME         , row.get(QueryBillDeposits.PAYEENAME        ));
                row2.put(QueryBillDeposits.CATEGNAME         , row.get(QueryBillDeposits.CATEGNAME        ));
                row2.put(QueryBillDeposits.COLOR             , row.get(QueryBillDeposits.COLOR            ));
                row2.put(QueryBillDeposits.ATTACHMENTCOUNT   , row.get(QueryBillDeposits.ATTACHMENTCOUNT  ));
                row2.put(QueryBillDeposits.TAGS              , row.get(QueryBillDeposits.TAGS             ));
                row2.put(QueryBillDeposits.NOTES             , row.get(QueryBillDeposits.NOTES            ));
                row2.put(QueryBillDeposits.STATUS            , row.get(QueryBillDeposits.STATUS           ));
                row2.put(QueryBillDeposits.AMOUNT            , row.get(QueryBillDeposits.AMOUNT           ));
                row2.put("transCurrency"                     , row.get("transCurrency"));
                row2.put(BALANCE, 0);
                listRecurring.add(row2);
            }
        }
        cursor.close();

        Collections.sort(listRecurring, new Comparator<HashMap<String, Object>>() {
            public int compare(HashMap<String, Object> uno, HashMap<String, Object> due) {
                return uno.get(QueryBillDeposits.TRANSDATE).toString().compareTo(due.get(QueryBillDeposits.TRANSDATE).toString());
            }
        });

        long baseCurrencyId = currencyService.getBaseCurrencyId();
        // copy to matrix cursor
        for (HashMap<String, Object> rowMap : listRecurring) {
            Money amountTrans ;
            Money amountBase;
            long transCurrency = ( rowMap.get("transCurrency") == null ? baseCurrencyId : (long) rowMap.get("transCurrency") );
            amountTrans = MoneyFactory.fromDouble((double) rowMap.get(QueryBillDeposits.AMOUNT));
            amountBase = currencyService.doCurrencyExchange(baseCurrencyId, amountTrans, transCurrency);

            if (!rowMap.get(QueryBillDeposits.STATUS).equals("V")) {
                totalAmount += amountBase.toDouble();
            }
            matrixCursor.newRow()
                    .add(ID, rowMap.get(ID))
                    .add(QueryBillDeposits.TRANSDATE, rowMap.get(QueryBillDeposits.TRANSDATE))
                    .add(QueryBillDeposits.PAYEENAME, rowMap.get(QueryBillDeposits.PAYEENAME))
                    .add(QueryBillDeposits.CATEGNAME, rowMap.get(QueryBillDeposits.CATEGNAME))
                    .add(QueryBillDeposits.COLOR, rowMap.get(QueryBillDeposits.COLOR))
                    .add(QueryBillDeposits.ATTACHMENTCOUNT, rowMap.get(QueryBillDeposits.ATTACHMENTCOUNT))
                    .add(QueryBillDeposits.TAGS, rowMap.get(QueryBillDeposits.TAGS))
                    .add(QueryBillDeposits.NOTES, rowMap.get(QueryBillDeposits.NOTES))
                    .add(QueryBillDeposits.STATUS, rowMap.get(QueryBillDeposits.STATUS))
                    .add(QueryBillDeposits.AMOUNT,  amountTrans.toDouble())
                    .add("transCurrency", transCurrency)
                    .add(BALANCE, totalAmount);
        }
    }

    private void getTotalAmountAndAccounts() {
        LookAndFeelSettings settings = new AppSettings(getContext()).getLookAndFeelSettings();
        // compose whereClause
        String where = "";
        // check if show only open accounts
        if (settings.getViewOpenAccounts()) {
            where = "LOWER(" + QueryAccountBills.STATUS + ")='open'";
        }
        // check if show fav accounts
        if (settings.getViewFavouriteAccounts()) {
            where = "LOWER(" + QueryAccountBills.FAVORITEACCT + ")='true'";
        }

        QueryAccountBills queryAccountBills = new QueryAccountBills(getActivity());
        Select query = new Select(queryAccountBills.getAllColumns())
                .where(where)
                .orderBy(QueryAccountBills.ACCOUNTTYPE + ", upper(" + QueryAccountBills.ACCOUNTNAME + ")");

        selectedAccounts = new ArrayList<>();

        Cursor c = getContext().getContentResolver().query(queryAccountBills.getUri(),
                null,
                where,
                null,
                null);
        if (c != null) {
            totalAmount = 0;
            while (c.moveToNext()) {
                selectedAccounts.add(c.getLong(c.getColumnIndex(QueryAccountBills.ACCOUNTID)));
                totalAmount += c.getDouble(c.getColumnIndex(QueryAccountBills.TOTALBASECONVRATE));
            }
            c.close();
        }
    }

    @Override
    public String getSubTitle() {
        return "12 month view";
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Update UI elements here
        //createCashFlowRecords();
        new Thread(new Runnable() {
            @Override
            public void run() {
                createCashFlowRecords();

                // here you perform background operation
                //Update the value background thread to UI thread
                Handler mHandler = new Handler(Looper.getMainLooper());
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        // here you can update ui
                        if (matrixCursor.getCount() == 0) {
                            setEmptyText(getActivity().getResources().getString(R.string.no_recurring_transaction));
                        } else {
                            adapter.swapCursor(matrixCursor);
                            adapter.notifyDataSetChanged();
                        }
                        setListShown(true);
                    }
                });
            }
        }).start();

        // create a object query
        setSearchMenuVisible(false);
        // set default text
        setEmptyText(getActivity().getResources().getString(R.string.loading));

        setHasOptionsMenu(true);

        Locale locale = MmexApplication.getApp().getAppLocale();
        dateUtils = new MmxDateTimeUtils(locale);
        currencyService = new CurrencyService(getContext());
        infoService = new InfoService(getContext());


        int layout = R.layout.item_alldata_account;

        adapter = new MoneySimpleCursorAdapter(getActivity(),
                layout,
                matrixCursor,   // filled matrixCursor after
                new String[]{
                        ID,
                        QueryBillDeposits.TRANSDATE, QueryBillDeposits.TRANSDATE, QueryBillDeposits.TRANSDATE,
                        QueryBillDeposits.PAYEENAME,
                        QueryBillDeposits.CATEGNAME,
                        QueryBillDeposits.COLOR,
                        QueryBillDeposits.ATTACHMENTCOUNT,
                        QueryBillDeposits.TAGS,
                        QueryBillDeposits.NOTES,
                        QueryBillDeposits.STATUS,
                        QueryBillDeposits.AMOUNT,
                        BALANCE
                },
                new int[]{
                        R.id.textTransactionId, // for id
                        R.id.textViewMonth,
                        R.id.textViewDay,
                        R.id.textViewYear,
                        R.id.textViewPayee,
                        R.id.textViewCategorySub,
                        R.id.viewColor,
                        R.id.textViewAttachment,
                        R.id.textViewTags,
                        R.id.textViewNotes,
                        R.id.textViewStatus,
                        R.id.textViewAmount,
                        R.id.textViewBalance,
                },
                0);

        adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            public boolean setViewValue(View aView, Cursor aCursor, int aColumnIndex) {
                // set non text field
                switch (aView.getId()) {
                    case R.id.viewColor:
                        int color = aCursor.getInt(aColumnIndex);
                        if (color > 0) {
                            aView.setBackgroundColor(infoService.getColorNumberFromInfoKey(color));
                            aView.setVisibility(View.VISIBLE);
                        } else {
                            aView.setVisibility(View.GONE);
                        }
                        return true;
                    case R.id.textViewAttachment:
                        if (aCursor.getLong(aColumnIndex) <= 0)
                            aView.setVisibility(View.GONE);
                        else
                            aView.setVisibility(View.VISIBLE);
                        return true;
                    case R.id.textViewTags:
                        if (aCursor.getString(aColumnIndex).isEmpty())
                            aView.setVisibility(View.GONE);
                        else
                            aView.setVisibility(View.VISIBLE);
                        return true;
                    case R.id.textTransactionId:
                        aView.setTag(aCursor.getLong(aColumnIndex));
                        aView.setVisibility(View.GONE);
                        return true;
                }
                TextView textView = (TextView) aView;
                switch (textView.getId()) {
                    case R.id.textViewMonth:
                        textView.setText(dateUtils.format(getAsDate(aCursor, aColumnIndex), "MMM"));
                        break;
                    case R.id.textViewDay:
                        textView.setText(dateUtils.format(getAsDate(aCursor, aColumnIndex), "dd"));
                        break;
                    case R.id.textViewYear:
                        textView.setText(dateUtils.format(getAsDate(aCursor, aColumnIndex), "yyyy"));
                        break;
                    case R.id.textViewCategorySub:
                    case R.id.textViewPayee:
                    case R.id.textViewNotes:
                    case R.id.textViewStatus:
                        textView.setText(aCursor.getString(aColumnIndex));
                        break;
                    case R.id.textViewAmount:
                        textView.setText(getAsAmountFromCurrency(aCursor, aColumnIndex));
                        if (aCursor.getDouble(aColumnIndex) <= 0) {
                            textView.setTextColor(getResources().getColor(R.color.material_red_700));
                        } else {
                            textView.setTextColor(getResources().getColor(R.color.material_green_700));
                        }
                        break;
                    case R.id.textViewBalance:
                        textView.setText(getAsAmount(aCursor, aColumnIndex));
                        if (aCursor.getDouble(aColumnIndex) <= 0) {
                            textView.setTextColor(getResources().getColor(R.color.material_red_700));
                        } else {
                            textView.setTextColor(getResources().getColor(R.color.material_green_700));
                        }
                        break;
                    default:
                        return false;
                }
                aView.setVisibility(View.VISIBLE);
                return true;
            }
        });

        setListAdapter(adapter);

        // set list view
        // a good idea is to make fill of matrixCursor async, start with showlist false and when matrixcursor is ready set showlist as true
        setListShown(false);

//        getLoaderManager().initLoader(ID_LOADER_REPORT, null, this);

        // show floating button.
        setFloatingActionButtonVisible(false);
        // attachFloatingActionButtonToListView();

    }

    private Date getAsDate(Cursor aCursor, int aColumnIndex) {
        return new MmxDate(aCursor.getString(aColumnIndex)).toDate();
    }

    private String getAsString(Cursor aCursor, int aColumnIndex) {
        return aCursor.getString(aColumnIndex);
    }

    private String getAsAmountFromCurrency(Cursor aCursor, int aColumnIndex) {
        Long currency = aCursor.getLong(aCursor.getColumnIndex("transCurrency"));
        if (currency == null) currency = currencyService.getBaseCurrencyId();
        return currencyService.getCurrencyFormatted(
                currency, MoneyFactory.fromDouble(
                aCursor.getDouble(aColumnIndex)
        ));
    }

    private String getAsAmount(Cursor aCursor, int aColumnIndex) {
        return currencyService.getBaseCurrencyFormatted(MoneyFactory.fromDouble(
                aCursor.getDouble(aColumnIndex)
        ));
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        MmexApplication.getApp().iocComponent.inject(this);
        setHasOptionsMenu(false);
        Intent i = getActivity().getParentActivityIntent();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        // set calendar
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle calendar
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        // show context menu here.
//        getActivity().openContextMenu(v);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        // context menu
    }

    @Override
    public boolean onContextItemSelected(android.view.MenuItem item) {
        // context menu
        return false;
    }



}
