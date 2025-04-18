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
package com.money.manager.ex.reports.cashflow;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;

import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.data.Entry;

import androidx.appcompat.app.AlertDialog;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.money.manager.ex.MmexApplication;
import com.money.manager.ex.R;
import com.money.manager.ex.adapter.MoneySimpleCursorAdapter;
import com.money.manager.ex.common.BaseListFragment;
import com.money.manager.ex.core.TransactionTypes;
import com.money.manager.ex.core.UIHelper;
import com.money.manager.ex.currency.CurrencyService;
import com.money.manager.ex.database.QueryAccountBills;
import com.money.manager.ex.database.QueryBillDeposits;
import com.money.manager.ex.domainmodel.RecurringTransaction;
import com.money.manager.ex.servicelayer.InfoService;
import com.money.manager.ex.servicelayer.RecurringTransactionService;
import com.money.manager.ex.settings.AppSettings;
import com.money.manager.ex.settings.LookAndFeelSettings;
import com.money.manager.ex.utils.MmxDate;
import com.money.manager.ex.utils.MmxDateTimeUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import info.javaperformance.money.Money;
import info.javaperformance.money.MoneyFactory;
import timber.log.Timber;

/* Master note
   Use AllDataAdapter as master view
   - no account selection (like RECURRINGTRANSACTION)
   - Account Balance (like ALLDATA)
    private boolean mShowAccountName = false; // not show header with account
    private boolean mShowBalanceAmount = true; // show balance on transaction


 */

public class CashFlowReportListFragment
        extends BaseListFragment {

//    private static final int ID_LOADER_REPORT = 1;
    private double totalAmount = 0;
    private static final String ID = "_id";
    private static final String BALANCE = "BALANCE";
    private static final int monthInAdvance = 12;

    private UIHelper ui;
    private MatrixCursor matrixCursor;
    String[] columnNames;
    CurrencyService currencyService;
    MmxDateTimeUtils dateUtils;
    InfoService infoService;
    MoneySimpleCursorAdapter adapter;
    ArrayList<Long> selectedAccounts = new ArrayList<>();
    ArrayList<Double> graphValue;
//    LimitLine cursorPosition;
    ArrayList<Integer> dayPosition = new ArrayList<>();

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

        Cursor cursor = null;
        try {
            cursor = getContext().getContentResolver().query(billDeposits.getUri(),
                    billDeposits.getAllColumns(),
                    null,
                    null,
                    QueryBillDeposits.NEXTOCCURRENCEDATE);
        } catch ( Exception e) {
            Timber.d(e);
        }
        if (cursor == null ||
                cursor.getCount() == 0)
            return;

        getTotalAmountAndAccounts();

        List<HashMap<String, Object>> listRecurring = new ArrayList<>();
        HashMap<String, Object> row;
        while (cursor.moveToNext()) {
            RecurringTransactionService recurringTransactionService = new RecurringTransactionService(cursor.getLong(cursor.getColumnIndexOrThrow(QueryBillDeposits.BDID)), getContext());
            // ignore transfert if both is on selected account
            // create recurring transaction
            double amount = cursor.getDouble(cursor.getColumnIndexOrThrow(QueryBillDeposits.AMOUNT));
            RecurringTransaction rx = recurringTransactionService.getSimulatedTransaction();
            if (rx.getPaymentDate().after(endDate.toDate())) {
                // first occurence of this transaction is over cashflow visibility
                continue;
            }
            if (rx.getTransactionType() == TransactionTypes.Transfer) {
                if (selectedAccounts.contains(rx.getAccountId()) &&
                        selectedAccounts.contains(rx.getToAccountId())) {
                    // both in
                    continue; // skip
                }
                if (!selectedAccounts.contains(rx.getAccountId()) &&
                        !selectedAccounts.contains(rx.getToAccountId())) {
                    // both out
                    continue; // skip
                }
                if (selectedAccounts.contains(rx.getAccountId())) {
                    // source in
                    amount = 0 - amount;
                }
            } else {
                if (!selectedAccounts.contains(rx.getAccountId())) {
                    continue;
                }
            }

            row = new HashMap<>();
            row.put(ID, cursor.getLong(cursor.getColumnIndexOrThrow(QueryBillDeposits.BDID)));
            row.put(QueryBillDeposits.TRANSDATE, cursor.getString(cursor.getColumnIndexOrThrow(QueryBillDeposits.NEXTOCCURRENCEDATE)));
            row.put(QueryBillDeposits.PAYEENAME, cursor.getString(cursor.getColumnIndexOrThrow(QueryBillDeposits.PAYEENAME)));
            if (row.get(QueryBillDeposits.PAYEENAME) == null)
                row.put(QueryBillDeposits.PAYEENAME, cursor.getString(cursor.getColumnIndexOrThrow(QueryBillDeposits.ACCOUNTNAME)));
            row.put(QueryBillDeposits.CATEGNAME, cursor.getString(cursor.getColumnIndexOrThrow(QueryBillDeposits.CATEGNAME)));
            if (row.get(QueryBillDeposits.CATEGNAME) == null)
                row.put(QueryBillDeposits.CATEGNAME, getString(R.string.transfer));
            row.put(QueryBillDeposits.COLOR, Objects.requireNonNullElse(cursor.getLong(cursor.getColumnIndexOrThrow(QueryBillDeposits.COLOR)), -1L)); // handle null #2235
            row.put(QueryBillDeposits.ATTACHMENTCOUNT, Objects.requireNonNullElse(cursor.getLong(cursor.getColumnIndexOrThrow(QueryBillDeposits.ATTACHMENTCOUNT)), 0L)); // handle null #2235
            row.put(QueryBillDeposits.TAGS, Objects.requireNonNullElse(cursor.getString(cursor.getColumnIndexOrThrow(QueryBillDeposits.TAGS)), "")); // handle null #2235
            row.put(QueryBillDeposits.NOTES, cursor.getString(cursor.getColumnIndexOrThrow(QueryBillDeposits.NOTES)));
            row.put(QueryBillDeposits.STATUS, cursor.getString(cursor.getColumnIndexOrThrow(QueryBillDeposits.STATUS)));
            row.put(QueryBillDeposits.AMOUNT, amount);
            row.put("transCurrency", cursor.getLong(cursor.getColumnIndexOrThrow(QueryBillDeposits.CURRENCYID)));
            row.put(BALANCE, 0);
            listRecurring.add(row);

            int limit = monthInAdvance * 31;
            while (limit > 0 && recurringTransactionService.simulateMoveNext() && recurringTransactionService.getSimulatedTransaction().getPaymentDate().before(endDate.toDate())) {
                limit -= 1;
                HashMap<String, Object> row2 = new HashMap<>();
                row2.put(ID, row.get(ID));
                row2.put(QueryBillDeposits.TRANSDATE, recurringTransactionService.getSimulatedTransaction().getPaymentDateString());
                row2.put(QueryBillDeposits.PAYEENAME, row.get(QueryBillDeposits.PAYEENAME));
                row2.put(QueryBillDeposits.CATEGNAME, row.get(QueryBillDeposits.CATEGNAME));
                row2.put(QueryBillDeposits.COLOR, row.get(QueryBillDeposits.COLOR));
                row2.put(QueryBillDeposits.ATTACHMENTCOUNT, row.get(QueryBillDeposits.ATTACHMENTCOUNT));
                row2.put(QueryBillDeposits.TAGS, row.get(QueryBillDeposits.TAGS));
                row2.put(QueryBillDeposits.NOTES, row.get(QueryBillDeposits.NOTES));
                row2.put(QueryBillDeposits.STATUS, row.get(QueryBillDeposits.STATUS));
                row2.put(QueryBillDeposits.AMOUNT, row.get(QueryBillDeposits.AMOUNT));
                row2.put("transCurrency", row.get("transCurrency"));
                row2.put(BALANCE, 0);
                listRecurring.add(row2);
            }
        }
        cursor.close();

        listRecurring.sort((HashMap<String, Object> uno, HashMap<String, Object> due) -> uno.get(QueryBillDeposits.TRANSDATE).toString().compareTo(due.get(QueryBillDeposits.TRANSDATE).toString()));

        long baseCurrencyId = currencyService.getBaseCurrencyId();
        graphValue = new ArrayList<>();
        for (int x = 0; x < 31 * monthInAdvance; x++) {
            graphValue.add(null);
        }

        Date olderDate = MmxDate.newDate().toDate();
        // copy to matrix cursor
        for (HashMap<String, Object> rowMap : listRecurring) {
            Money amountTrans;
            Money amountBase;
            long transCurrency = (rowMap.get("transCurrency") == null ? baseCurrencyId : (long) rowMap.get("transCurrency"));
            amountTrans = MoneyFactory.fromDouble((double) rowMap.get(QueryBillDeposits.AMOUNT));
            amountBase = currencyService.doCurrencyExchange(baseCurrencyId, amountTrans, transCurrency);

            if (!rowMap.get(QueryBillDeposits.STATUS).equals("V")) {
                totalAmount += amountBase.toDouble();
            }

            Date newerDate = MmxDate.from(rowMap.get(QueryBillDeposits.TRANSDATE).toString(), "yyyy-MM-dd");
            int diffInDays = (int) ((newerDate.getTime() - olderDate.getTime())
                    / (1000 * 60 * 60 * 24));
            if (diffInDays < 0) diffInDays = 0;
            try {
                graphValue.set(diffInDays, totalAmount);
            } catch (Exception e) {
                Timber.d(e);
            }
            dayPosition.add(diffInDays);
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
                    .add(QueryBillDeposits.AMOUNT, amountTrans.toDouble())
                    .add("transCurrency", transCurrency)
                    .add(BALANCE, totalAmount);
        }
    }

    private void getTotalAmountAndAccounts() {
        LookAndFeelSettings settings = new AppSettings(getContext()).getLookAndFeelSettings();
        int accountFilter = settings.get(R.menu.menu_cashflow, R.id.menu_cashflow_open);
        String accountCustomFilters = "";
        if ( accountFilter == R.id.menu_cashflow_custom ) {
            accountCustomFilters = settings.get("AccountFilterCustom", "");
        }
        // compose whereClause
        String where = "";
        switch (accountFilter) {
            case R.id.menu_cashflow_open:
                where += "LOWER(" + QueryAccountBills.STATUS + ") = 'open'";
                break;
            case R.id.menu_cashflow_favorite:
                where += "LOWER(" + QueryAccountBills.FAVORITEACCT + ") = 'true'";
                break;
            case R.id.menu_cashflow_custom:
                where += QueryAccountBills.ACCOUNTID + " IN ( "+accountCustomFilters + " )";
                break;
            default:
                break;
        }

        QueryAccountBills queryAccountBills = new QueryAccountBills(getActivity());
        selectedAccounts = new ArrayList<>();

        Cursor c = getContext().getContentResolver().query(queryAccountBills.getUri(),
                null,
                where,
                null,
                QueryAccountBills.ACCOUNTTYPE + ", upper(" + QueryAccountBills.ACCOUNTNAME + ")");
        if (c != null) {
            totalAmount = 0;
            while (c.moveToNext()) {
                selectedAccounts.add(c.getLong(c.getColumnIndexOrThrow(QueryAccountBills.ACCOUNTID)));
                totalAmount += c.getDouble(c.getColumnIndexOrThrow(QueryAccountBills.TOTALBASECONVRATE));
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

        ui = new UIHelper(getActivity());

        // Update UI elements here
        //createCashFlowRecords();
        new Thread(() -> {
            createCashFlowRecords();

            // here you perform background operation
            //Update the value background thread to UI thread
            Handler mHandler = new Handler(Looper.getMainLooper());
            mHandler.post(() -> {
                // here you can update ui
                if (matrixCursor.getCount() == 0) {
                    setEmptyText(getActivity().getResources().getString(R.string.no_recurring_transaction));
                } else {
                    adapter.swapCursor(matrixCursor);
                    adapter.notifyDataSetChanged();
                    getListView().setOnScrollListener(new AbsListView.OnScrollListener() {
                        @Override
                        public void onScrollStateChanged(AbsListView view, int scrollState) {
                        }

                        @Override
                        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
//                            if (cursorPosition != null ) {
                              if ( chart != null ) {
//                                Timber.d("Position: %d",firstVisibleItem);
//                                chart.getXAxis().removeLimitLine(cursorPosition);
                                int pos = dayPosition.get(firstVisibleItem);
//                                cursorPosition = new LimitLine(pos,"");
//                                cursorPosition.setLineColor(Color.GREEN);
//                                cursorPosition.setLineWidth(2f);
//                                chart.getXAxis().addLimitLine(cursorPosition);
                                chart.highlightValue(pos, 0, false);
                                chart.invalidate();
                            }
                        }
                    });
                    buildChartInfo();
                }
                setListShown(true);
            });
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

        adapter.setViewBinder((aView, aCursor, aColumnIndex) -> {
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
                    if (aCursor.getString(aColumnIndex) == null || aCursor.getString(aColumnIndex).isEmpty())
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
        });

        setListAdapter(adapter);

        // set list view
        // a good idea is to make fill of matrixCursor async, start with showlist false and when matrixcursor is ready set showlist as true
        setListShown(false);

//        getLoaderManager().initLoader(ID_LOADER_REPORT, null, this);

        // show floating button.
        setFabVisible(false);
        // attachFloatingActionButtonToListView();

    }

    private Date getAsDate(Cursor aCursor, int aColumnIndex) {
        return new MmxDate(aCursor.getString(aColumnIndex)).toDate();
    }

    private String getAsString(Cursor aCursor, int aColumnIndex) {
        return aCursor.getString(aColumnIndex);
    }

    private String getAsAmountFromCurrency(Cursor aCursor, int aColumnIndex) {
        Long currency = aCursor.getLong(aCursor.getColumnIndexOrThrow("transCurrency"));
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
//        Intent i = getActivity().getParentActivityIntent();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        // set accounts Filter
        inflater.inflate(R.menu.menu_cashflow, menu);
        LookAndFeelSettings settings = new AppSettings(getContext()).getLookAndFeelSettings();
        int accountFilter = settings.get(R.menu.menu_cashflow, R.id.menu_cashflow_open);
        if ( menu.findItem(accountFilter) != null ) {
            menu.findItem(accountFilter).setChecked(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle accounts filter
        LookAndFeelSettings settings = new AppSettings(getContext()).getLookAndFeelSettings();
//        int accountFilter = settings.get(R.menu.menu_cashflow, R.id.menu_cashflow_open);
        switch (item.getItemId()) {
            case R.id.menu_cashflow_all:
            case R.id.menu_cashflow_open:
            case R.id.menu_cashflow_favorite:
            case R.id.menu_cashflow_custom:
                item.setChecked(true);
                settings.set(R.menu.menu_cashflow, item.getItemId());
                if ( item.getItemId() == R.id.menu_cashflow_custom ){
                    // call popup
                                        String where = "";
                    if ( settings.getViewOpenAccounts() ) {
                        where = "LOWER(" + QueryAccountBills.STATUS + ")='open'";
                    }
                    selectedAccounts = new ArrayList<>();
                    String[] accountList = settings.get("AccountFilterCustom", "").split(",");
                    for (String x : accountList) {
                        if (! x.isEmpty() )
                            try {
                                selectedAccounts.add(Long.valueOf(x));
                            } catch ( Exception e ) {}
                    }
                    QueryAccountBills queryAccountBills = new QueryAccountBills(getActivity());
                    Cursor c = getContext().getContentResolver().query(queryAccountBills.getUri(),
                            null,
                            where,
                            null,
                            QueryAccountBills.ACCOUNTTYPE + ", upper(" + QueryAccountBills.ACCOUNTNAME + ")" );
                    if ( c == null ) return false;
                    MatrixCursor matrixCursor = new MatrixCursor( new String[]{"_id", QueryAccountBills.ACCOUNTNAME, "CHECKED"} );
                    while (c.moveToNext()) {
                        long id = c.getLong(c.getColumnIndexOrThrow(QueryAccountBills.ACCOUNTID));
                        matrixCursor.newRow()
                                .add("_id", id)
                                .add(QueryAccountBills.ACCOUNTNAME, c.getString(c.getColumnIndexOrThrow(QueryAccountBills.ACCOUNTNAME)))
                                .add("CHECKED", ( selectedAccounts.contains(id) ? 1 : 0 ) );
                    }
                    c.close();
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Select Accounts");
                    builder.setCancelable(false);
                    builder.setMultiChoiceItems(matrixCursor,"CHECKED", QueryAccountBills.ACCOUNTNAME,
                            (dialog, which, isChecked) -> {
                                matrixCursor.moveToPosition(which);
                                long id = matrixCursor.getInt(matrixCursor.getColumnIndexOrThrow("_id"));
                                if ( !isChecked ) {
//                                    if ( selectedAccounts.contains(id) ) {
                                        selectedAccounts.remove(id);
//                                    }
                                } else {
                                    if ( ! selectedAccounts.contains( id )) {
                                        selectedAccounts.add( id );
                                    }
                                }
                            } );
                    builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
                        settings.set("AccountFilterCustom", selectedAccounts.toString().replace("[","").replace("]","").replace(" ",""));
                        getActivity().recreate();
                    });
                    builder.show();
                    settings.set("AccountFilterCustom", "");
                    return true;
                }
                getActivity().recreate();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
    public boolean onContextItemSelected(MenuItem item) {
        // context menu
        return false;
    }

    private LineChart chart;

    private void buildChartInfo() {
        List<String> xVal = new ArrayList<>();
        ArrayList<Entry> values = new ArrayList<>();
//        for (int i = 0; i < graphValue.size(); i++) {
        float old = 0;
        float maxV = 0;
        float minV = 0;
        MmxDate date = MmxDate.newDate();
        for (int i = 0; i < 31 * monthInAdvance; i++) {
            if ( date.getDayOfMonth() == 1) {
                xVal.add(dateUtils.format(date.toDate(), "MMM"));
            } else {
                xVal.add("");
            }
//            xVal.add(dateUtils.format(date.toDate(), "dd-MMM"));
            if (graphValue.get(i) != null) {
                old = (Float) graphValue.get(i).floatValue();
            }
            if (i == 0) {
                maxV = old;
                minV = old;
            } else {
                maxV = (maxV < old ? old : maxV);
                minV = (minV > old ? old : minV);
            }
            Entry entry = new Entry(old, i);
            values.add(entry);
            date.addDays(1);
        }

        LineDataSet set1 = new LineDataSet(values, "Balance");
        set1.setColor(Color.GREEN);
        set1.setCircleColor(Color.GREEN);
        set1.setLineWidth(2f);
        set1.setDrawCircleHole(false);
        set1.setDrawCircles(false);
        set1.setHighLightColor(Color.BLUE);
        set1.setDrawHighlightIndicators(true);
        set1.setHighlightLineWidth(1F);

//        LineData data = new LineData(xVal, set1);   //(dataSets);
        LineData data = new LineData(xVal, set1);   //(dataSets);
        data.setDrawValues(false);
//        data.setValueFormatter((value, entry, dataSetIndex, viewPortHandler) -> {
//            return "";
//        });

        // disable dual axis (only use LEFT axis)
        chart = getActivity().findViewById(R.id.chartLine);
        chart.getAxisRight().setEnabled(false);
        chart.getXAxis().setDrawGridLines(false);
        for( int i = 0; i < xVal.size(); i++ ) {
            if (!xVal.get(i).isEmpty()) {
                LimitLine l = new LimitLine(i, xVal.get(i));
                l.setTextSize(10);
                l.setLineColor(Color.DKGRAY);
                l.setTextColor(ui.getPrimaryTextColor());
                chart.getXAxis().addLimitLine(l);
            }
        }
//        cursorPosition = new LimitLine(0,"");
//        cursorPosition.setLineColor(Color.GREEN);
//        cursorPosition.setLineWidth(2);
//        chart.getXAxis().addLimitLine(cursorPosition);
//        chart.setHighlightPerTapEnabled(true);

        chart.getXAxis().setDrawLabels(false);
//        chart.getXAxis().setPosition(XAxis.XAxisPosition.TOP);
        chart.getAxisLeft().setTextColor(ui.getPrimaryTextColor());
        chart.setDescription("");
        chart.setData(data);
//        chart.setTouchEnabled(false);
        chart.setTouchEnabled(true);
        chart.setPinchZoom(false);
        chart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
                int newPos = dayPosition.indexOf(e.getXIndex());
                //                getListView().smoothScrollToPosition(newPos);
                getListView().setSelection(newPos);
            }

            @Override
            public void onNothingSelected() {

            }
        });
        chart.getLegend().setEnabled(false);
        chart.setNoDataText(getString(R.string.loading));
        Timber.d("Max: %f", chart.getAxisLeft().getAxisMaximum());
        Timber.d("Min: %f", chart.getAxisLeft().getAxisMinimum());
        chart.getAxisLeft().setAxisMaxValue( ( (chart.getAxisLeft().getAxisMaximum()  - chart.getAxisLeft().getAxisMinimum() ) / ( chart.getHeight() - 15 ) ) * chart.getHeight() + chart.getAxisLeft().getAxisMinimum() );
        Timber.d("New Max: %f", chart.getAxisLeft().getAxisMaximum());
//        chart.setDrawMarkerViews(true);
        chart.invalidate(); // refresh

    }

}
