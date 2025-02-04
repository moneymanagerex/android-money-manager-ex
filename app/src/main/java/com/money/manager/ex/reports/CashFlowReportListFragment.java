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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.fragment.app.ListFragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

import com.google.common.primitives.Doubles;
import com.google.common.primitives.Ints;
import com.mikepenz.mmex_icon_font_typeface_library.MMXIconFont;
import com.money.manager.ex.MmexApplication;
import com.money.manager.ex.R;
import com.money.manager.ex.adapter.AllDataAdapter;
import com.money.manager.ex.adapter.MoneySimpleCursorAdapter;
import com.money.manager.ex.common.BaseListFragment;
import com.money.manager.ex.common.IAllDataMultiChoiceModeListenerCallbacks;
import com.money.manager.ex.common.MmxCursorLoader;
import com.money.manager.ex.core.IntentFactory;
import com.money.manager.ex.core.UIHelper;
import com.money.manager.ex.currency.CurrencyService;
import com.money.manager.ex.database.QueryAllData;
import com.money.manager.ex.database.QueryBillDeposits;
import com.money.manager.ex.database.QueryMobileData;
import com.money.manager.ex.database.QueryReportIncomeVsExpenses;
import com.money.manager.ex.database.SQLDataSet;
import com.money.manager.ex.datalayer.Select;
import com.money.manager.ex.domainmodel.Account;
import com.money.manager.ex.domainmodel.Tag;
import com.money.manager.ex.home.events.AccountsTotalLoadedEvent;
import com.money.manager.ex.log.ExceptionHandler;
import com.money.manager.ex.search.SearchParameters;
import com.money.manager.ex.utils.MmxDate;
import com.money.manager.ex.viewmodels.IncomeVsExpenseReportEntity;

import org.greenrobot.eventbus.Subscribe;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

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
        extends BaseListFragment
{

    private static final int ID_LOADER_REPORT = 1;
    private String totalAmount = "";

    private MatrixCursor matrixCursor;
    String[] columnNames;
    private void createCashFlowRecords() {
        // Use MatrixCursor instead
        if (matrixCursor != null) return;

        // since base report is based on AllDataAdapter, we reuse global variable
        columnNames = new String[] {
                "_id",
                QueryAllData.Date,
                QueryAllData.ACCOUNTID,
                QueryAllData.STATUS,
                QueryAllData.AMOUNT,
                QueryAllData.TransactionType,
                QueryAllData.ATTACHMENTCOUNT,
                QueryAllData.CURRENCYID,
                QueryAllData.PAYEENAME,
                QueryAllData.AccountName,
                QueryAllData.Category,
                QueryAllData.Notes,
                QueryAllData.ToCurrencyId,
                QueryAllData.TOACCOUNTID,
                QueryAllData.ToAmount,
                QueryAllData.ToAccountName,
                QueryAllData.TAGS,
                QueryAllData.COLOR,
                QueryAllData.SPLITTED,
                QueryAllData.CATEGID,
        };
        matrixCursor = new MatrixCursor(columnNames);

        matrixCursor.addRow(new String[]{"1", "20250101", "1", "STATUS", "AMOUNT", "TRANSACTIONTYPE", "ATTACHMENTCOUNT", "CURRENCYID", "PAYEE", "ACCOUNTNAME", "CATEGORY", "NOTES", "TOCURRENCYID", "TOACCOUNTID", "TOAMOUNT", "TOACCOUNTNAME", "TAGS", "COLOR", "SPLITTED", "CATEGID"});
        matrixCursor.addRow(new String[]{"1","45698","2","","55","Withdrawal","","2","Emmanuele","Ing - Conto Corrente","Varie:Tabacchi","","","-1","0","","Evitabile","1","0","70"});
        matrixCursor.addRow(new String[]{"2","2025-02-11T00:00:00","2","","4300","Deposit","","2","Emmanuele","Ing - Conto Corrente","Entrate:Stipendio","","","-1","4300","","","4","0","63"});
        matrixCursor.addRow(new String[]{"3","2026-05-11T00:00:00","2","","4000","Deposit","","2","Emmanuele","Ing - Conto Corrente","Entrate Straordinarie:Premio Produzione","","","-1","4000","","","-1","0","101"});
        matrixCursor.addRow(new String[]{"7","45703","2","","1200","Withdrawal","","2","Anna","Ing - Conto Corrente","Vitto:Trasferimento Anna","","","-1","1200","","","0","0","75"});
        matrixCursor.addRow(new String[]{"10","45705","5","","6.99","Withdrawal","","2","Anna","Ing - Carta Prepagata","Casa:Bollette:Cellulari","","","-1","6.99","","","-1","0","25"});
        matrixCursor.addRow(new String[]{"11","45714","5","","9.99","Withdrawal","","2","Casa","Ing - Carta Prepagata","Casa:Abbonamenti","","","-1","0","","Evitabile","3","0","66"});
        matrixCursor.addRow(new String[]{"12","45705","5","","6.99","Withdrawal","","2","Emmanuele","Ing - Carta Prepagata","Casa:Bollette:Cellulari","","","-1","6.99","","","-1","0","25"});
        matrixCursor.addRow(new String[]{"13","45700","5","","17.99","Withdrawal","","2","Casa","Ing - Carta Prepagata","Casa:Abbonamenti","","","-1","17.99","","","-1","0","66"});
        matrixCursor.addRow(new String[]{"15","45719","2","","65.74","Withdrawal","","2","Emmanuele","Ing - Conto Corrente","Casa:Assicurazione","","","-1","65.74","","","-1","0","97"});
        matrixCursor.addRow(new String[]{"16","2025-09-30T00:00:00","2","","300","Withdrawal","","2","Figli","Ing - Conto Corrente","Sport","","","-1","0","","","0","0","18"});
        matrixCursor.addRow(new String[]{"18","2025-09-30T00:00:00","2","","300","Withdrawal","","2","Figli","Ing - Conto Corrente","Sport","","","-1","300","","","0","0","18"});
        matrixCursor.addRow(new String[]{"19","45700","2","","300","Withdrawal","","2","Figli","Ing - Conto Corrente","Sport","","","-1","300","","","-1","0","18"});
        matrixCursor.addRow(new String[]{"20","45695","5","","5.99","Withdrawal","","2","Figli","Ing - Carta Prepagata","Casa:Bollette:Cellulari","","","-1","5.99","","","-1","0","25"});
        matrixCursor.addRow(new String[]{"23","2025-05-31T00:00:00","2","V","0","Withdrawal","","2","Anna","Ing - Conto Corrente","Veicoli:Auto:Bolli e Tasse","","","-1","0","","","-1","0","90"});
        matrixCursor.addRow(new String[]{"24","2025-02-28T00:00:00","2","V","70","Withdrawal","","2","Emmanuele","Ing - Conto Corrente","Veicoli:Moto:Bolli e Tasse","","","-1","70","","","-1","0","93"});
        matrixCursor.addRow(new String[]{"25","45701","2","","300","Withdrawal","","2","Casa","Ing - Conto Corrente","Casa:Bollette:Elettricità","","","-1","300","","","-1","0","26"});
        matrixCursor.addRow(new String[]{"26","2025-02-28T00:00:00","2","","90","Withdrawal","","2","Casa","Ing - Conto Corrente","Casa:Bollette:Telefono Fisso","","","-1","90","","","-1","0","28"});
        matrixCursor.addRow(new String[]{"27","45762","2","","130","Withdrawal","","2","Anna","Ing - Conto Corrente","Tasse:Ordine Medici","","","-1","130","","","-1","0","71"});
        matrixCursor.addRow(new String[]{"29","2025-12-30T00:00:00","2","","419.72","Withdrawal","","2","Casa","Ing - Conto Corrente","Casa:Condominio:Caldaia","","","-1","419.72","","","-1","0","107"});
        matrixCursor.addRow(new String[]{"30","2025-05-31T00:00:00","2","","900","Withdrawal","","2","Anna","Ing - Conto Corrente","Veicoli:Auto:Assicurazione","","","-1","900","","","-1","0","94"});
        matrixCursor.addRow(new String[]{"31","2025-05-31T00:00:00","2","","350","Withdrawal","","2","Casa","Ing - Conto Corrente","Casa:Assicurazione","","","-1","350","","","-1","0","97"});
        matrixCursor.addRow(new String[]{"32","2025-09-30T00:00:00","2","","600","Withdrawal","","2","Figli","Ing - Conto Corrente","Sport","","","-1","600","","","-1","0","18"});
        matrixCursor.addRow(new String[]{"33","2026-01-30T00:00:00","2","","500","Withdrawal","","2","Figli","Ing - Conto Corrente","Sport","","","-1","500","","","-1","0","18"});
        matrixCursor.addRow(new String[]{"34","2025-05-31T00:00:00","2","","1943.75","Withdrawal","","2","Anna","Ing - Conto Corrente","Tasse:Enpav","","","-1","1943.75","","","-1","0","67"});
        matrixCursor.addRow(new String[]{"37","2025-10-31T00:00:00","2","","1943.75","Withdrawal","","2","Anna","Ing - Conto Corrente","Tasse:Enpav","","","-1","1943.75","","","-1","0","67"});
        matrixCursor.addRow(new String[]{"38","2025-07-07T00:00:00","2","","210","Withdrawal","","2","Casa","Ing - Conto Corrente","Tasse:Casa","","","-1","210","","","-1","0","61"});
        matrixCursor.addRow(new String[]{"39","46013","2","","4000","Deposit","","2","Emmanuele","Ing - Conto Corrente","Entrate:Stipendio","","","-1","4000","","","-1","0","63"});
        matrixCursor.addRow(new String[]{"40","45971","2","","3115","Deposit","","2","Emmanuele","Ing - Conto Corrente","Entrate Straordinarie:Rimborso 730","","","-1","3115","","","-1","0","100"});
        matrixCursor.addRow(new String[]{"41","45693","2","","150","Withdrawal","","2","Casa","Ing - Conto Corrente","Vitto","","","-1","150","","","-1","0","2"});
        matrixCursor.addRow(new String[]{"42","46006","4","","500","Withdrawal","","2","Figli","Ing - Carta Credito","Regali","","","-1","500","","","-1","0","12"});
        matrixCursor.addRow(new String[]{"43","45708","5","","6.99","Withdrawal","","2","Casa","Ing - Carta Prepagata","Casa:Abbonamenti","","","-1","6.99","","","-1","0","66"});
        matrixCursor.addRow(new String[]{"49","2025-06-03T00:00:00","2","","1000","Withdrawal","","2","Anna","Ing - Conto Corrente","Veicoli:Auto:Manutenzione","","","-1","1000","","","-1","0","91"});
        matrixCursor.addRow(new String[]{"50","45792","2","","200","Transfer","","2","","Ing - Conto Corrente","Transfer","","","5","200","Ing - Carta Prepagata","","-1","0","16"});
        matrixCursor.addRow(new String[]{"52","2025-06-30T00:00:00","4","","140","Withdrawal","","2","Figli","Ing - Carta Credito","Scuola","","","-1","140","","","-1","0","20"});
        matrixCursor.addRow(new String[]{"58","2028-01-17T00:00:00","2","","700","Withdrawal","","2","Figli","Ing - Conto Corrente","Salute:Occhiali e Lenti","","","-1","700","","","-1","0","49"});
        matrixCursor.addRow(new String[]{"59","45704","5","","6.99","Withdrawal","","2","Figli","Ing - Carta Prepagata","Casa:Bollette:Cellulari","","","-1","6.99","","","-1","0","25"});
        matrixCursor.addRow(new String[]{"61","2025-10-30T00:00:00","2","","200","Withdrawal","","2","Figli","Ing - Conto Corrente","Veicoli:Trasporti","","","-1","200","","","-1","0","74"});
    }

    @Override
    public String getSubTitle() {
        return "CashFlowReport";
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        createCashFlowRecords();
        // create a object query
        setSearchMenuVisible(false);
        // set default text
        setEmptyText(getActivity().getResources().getString(R.string.loading));

        setHasOptionsMenu(true);

        int layout = R.layout.item_alldata_account;

        MoneySimpleCursorAdapter adapter = new MoneySimpleCursorAdapter(getActivity(),
                layout,
                matrixCursor,   // normaly is null
                new String[] {  // set only visible column
                        QueryAllData.Date,
                        QueryAllData.Category,
                        QueryAllData.AMOUNT,
                        QueryAllData.AMOUNT},
                new int[]{
                        R.id.textViewPayee,
                        R.id.textViewCategorySub,
                        R.id.textViewAmount,
                        R.id.textViewBalance,
                        }, // todo add list of view element
                0);

        adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            public boolean setViewValue(View aView, Cursor aCursor, int aColumnIndex) {
                aView.setVisibility(View.VISIBLE);
                TextView textView = (TextView) aView;
                CharSequence text;
                if (aView.getId() == R.id.textViewBalance) {
                    text = totalAmount;
                } else {
                    text = aCursor.getString(aColumnIndex);
                }
                textView.setText(text);
                return true;
            }
        });

        setListAdapter(adapter);

        // set list view
        // a good idea is to make fill of matrixCursor async, start with showlist false and when matrixcursor is ready set showlist as true
//        setListShown(false);
        setListShown(true);

//        getLoaderManager().initLoader(ID_LOADER_REPORT, null, this);

        // show floating button.
        setFloatingActionButtonVisible(false);
        // attachFloatingActionButtonToListView();

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
