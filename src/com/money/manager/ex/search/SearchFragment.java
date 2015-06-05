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
package com.money.manager.ex.search;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.money.manager.ex.CategorySubCategoryExpandableListActivity;
import com.money.manager.ex.Constants;
import com.money.manager.ex.PayeeActivity;
import com.money.manager.ex.R;
import com.money.manager.ex.database.AccountRepository;
import com.money.manager.ex.database.TableSplitTransactions;
import com.money.manager.ex.fragment.AllDataFragment;
import com.money.manager.ex.fragment.IInputAmountDialogListener;
import com.money.manager.ex.fragment.InputAmountDialog;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.database.QueryAllData;
import com.money.manager.ex.database.TableAccountList;
import com.money.manager.ex.interfaces.IAllDataFragmentCallbacks;
import com.money.manager.ex.utils.DateUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * The search form with search parameter input fields.
 */
public class SearchFragment extends Fragment
        implements IInputAmountDialogListener,
            IAllDataFragmentCallbacks {

    // LOGCAT
    private static final String LOGCAT = SearchFragment.class.getSimpleName();
    // ID REQUEST code
    private static final int REQUEST_PICK_PAYEE = 1;
    private static final int REQUEST_PICK_CATEGORY = 3;
    // reference view into layout
    private Spinner spinAccount, spinStatus;
    private EditText edtTransNumber, txtNotes;
    private TextView txtToAmount, txtFromAmount, txtSelectCategory, txtSelectPayee, txtFromDate, txtToDate;
    private CheckBox cbxWithdrawal, cbxDeposit, cbxTransfer;
    // arrays list account name and account id
    private ArrayList<String> mAccountNameList = new ArrayList<>();
    private ArrayList<Integer> mAccountIdList = new ArrayList<>();
    private List<TableAccountList> mAccountList;
    // status item and values
    private ArrayList<String> mStatusItems = new ArrayList<>(),
            mStatusValues = new ArrayList<>();
    // dual panel
    private boolean mDualPanel = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

//        AllDataFragment fragment = (AllDataFragment) getActivity().getSupportFragmentManager()
//                .findFragmentByTag(AllDataFragment.class.getSimpleName());
//        if (fragment != null) {
//            fragment.setSearResultFragmentLoaderCallbacks((SearchActivity) getActivity());
//        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (container == null) return null;

        Core core = new Core(getActivity().getApplicationContext());
        //create view
        View view = inflater.inflate(R.layout.search_fragment, container, false);
        //create listener amount
        OnClickListener onClickAmount = new OnClickListener() {
            @Override
            public void onClick(View v) {
                double amount = 0;
                if (v.getTag() != null && v.getTag() instanceof Double) {
                    amount = (Double) v.getTag();
                }
                InputAmountDialog dialog = InputAmountDialog.getInstance(SearchFragment.this, v.getId(), amount);
                dialog.show(getActivity().getSupportFragmentManager(), dialog.getClass().getSimpleName());
            }
        };
        //To Amount
        txtToAmount = (TextView) view.findViewById(R.id.textViewFromAmount);
        txtToAmount.setOnClickListener(onClickAmount);
        //From Amount
        txtFromAmount = (TextView) view.findViewById(R.id.textViewToAmount);
        txtFromAmount.setOnClickListener(onClickAmount);

        // account list <> to populate the spin
        spinAccount = (Spinner) view.findViewById(R.id.spinnerAccount);
        if (mAccountList == null) {
            AccountRepository accountRepository = new AccountRepository(getActivity().getApplicationContext());
            mAccountList = accountRepository.getAccountList(core.getAccountsOpenVisible(),
                    core.getAccountFavoriteVisible());
            mAccountList.add(0, null);
            for (int i = 0; i <= mAccountList.size() - 1; i++) {
                if (mAccountList.get(i) != null) {
                    mAccountNameList.add(mAccountList.get(i).getAccountName());
                    mAccountIdList.add(mAccountList.get(i).getAccountId());
                } else {
                    mAccountNameList.add("");
                    mAccountIdList.add(AdapterView.INVALID_POSITION);
                }
            }
        }
        // checkbox
        cbxDeposit = (CheckBox) view.findViewById(R.id.checkBoxDeposit);
        cbxTransfer = (CheckBox) view.findViewById(R.id.checkBoxTransfer);
        cbxWithdrawal = (CheckBox) view.findViewById(R.id.checkBoxWithdrawal);
        // create adapter for spinAccount
        ArrayAdapter<String> adapterAccount = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, mAccountNameList);
        adapterAccount.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinAccount.setAdapter(adapterAccount);
        //Payee
        txtSelectPayee = (TextView) view.findViewById(R.id.textViewSelectPayee);
        txtSelectPayee.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), PayeeActivity.class);
                intent.setAction(Intent.ACTION_PICK);
                startActivityForResult(intent, REQUEST_PICK_PAYEE);
            }
        });
        //Category
        txtSelectCategory = (TextView) view.findViewById(R.id.textViewSelectCategory);
        txtSelectCategory.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), CategorySubCategoryExpandableListActivity.class);
                intent.setAction(Intent.ACTION_PICK);
                startActivityForResult(intent, REQUEST_PICK_CATEGORY);
            }
        });
        if (mStatusItems.size() <= 0) {
            // arrays to manage Status
            mStatusItems.add("");
            mStatusValues.add("");
            mStatusItems.addAll(Arrays.asList(getResources().getStringArray(R.array.status_items)));
            mStatusValues.addAll(Arrays.asList(getResources().getStringArray(R.array.status_values)));
        }
        // create adapter for spinnerStatus
        spinStatus = (Spinner) view.findViewById(R.id.spinnerStatus);
        ArrayAdapter<String> adapterStatus = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, mStatusItems);
        adapterStatus.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinStatus.setAdapter(adapterStatus);
        // from date
        txtFromDate = (TextView) view.findViewById(R.id.textViewFromDate);
        txtFromDate.setOnClickListener(new OnDateButtonClickListener(txtFromDate));
        // to date
        txtToDate = (TextView) view.findViewById(R.id.textViewToDate);
        txtToDate.setOnClickListener(new OnDateButtonClickListener(txtToDate));
        // transaction number
        edtTransNumber = (EditText) view.findViewById(R.id.editTextTransNumber);
        // notes
        txtNotes = (EditText) view.findViewById(R.id.editTextNotes);

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_PICK_PAYEE:
                if ((resultCode == Activity.RESULT_OK) && (data != null)) {
                    txtSelectPayee.setTag(data.getIntExtra(PayeeActivity.INTENT_RESULT_PAYEEID, -1));
                    txtSelectPayee.setText(data.getStringExtra(PayeeActivity.INTENT_RESULT_PAYEENAME));
                }
                break;
            case REQUEST_PICK_CATEGORY:
                if ((resultCode == Activity.RESULT_OK) && (data != null)) {
                    //create class for store data
                    CategorySub categorySub = new CategorySub();
                    categorySub.categId = data.getIntExtra(CategorySubCategoryExpandableListActivity.INTENT_RESULT_CATEGID, -1);
                    categorySub.categName = data.getStringExtra(CategorySubCategoryExpandableListActivity.INTENT_RESULT_CATEGNAME);
                    categorySub.subCategId = data.getIntExtra(CategorySubCategoryExpandableListActivity.INTENT_RESULT_SUBCATEGID, -1);
                    categorySub.subCategName = data.getStringExtra(CategorySubCategoryExpandableListActivity.INTENT_RESULT_SUBCATEGNAME);
                    //save into button
                    txtSelectCategory.setText(categorySub.categName + (!TextUtils.isEmpty(categorySub.subCategName) ? " : " + categorySub.subCategName : ""));
                    txtSelectCategory.setTag(categorySub);
                }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_cancel:
                getActivity().finish();
                return true;
            case R.id.menu_done:
            case R.id.menu_search_transaction:
                executeSearch();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Compose arguments and execute search
     */
    public void executeSearch() {
        ParameterizedWhereClause where = assembleWhereClause();
//        ArrayList<String> whereClause = assembleWhereClause();

        showSearchResultsFragment(where);
    }

    private ParameterizedWhereClause assembleWhereClause() {
        ParameterizedWhereClause where = new ParameterizedWhereClause();

        // account
        if (spinAccount.getSelectedItemPosition() != AdapterView.INVALID_POSITION && mAccountIdList.get(spinAccount.getSelectedItemPosition()) != -1) {
            where.Clause.add(QueryAllData.ACCOUNTID + "=" + mAccountIdList.get(spinAccount.getSelectedItemPosition()));
        }
        // transaction type
        if (cbxDeposit.isChecked() || cbxTransfer.isChecked() || cbxWithdrawal.isChecked()) {
            where.Clause.add(QueryAllData.TransactionType + " IN (" +
                    (cbxDeposit.isChecked() ? "'Deposit'" : "''") + "," + (cbxTransfer.isChecked() ? "'Transfer'" : "''")
                    + "," + (cbxWithdrawal.isChecked() ? "'Withdrawal'" : "''") + ")");
        }
        // status
        if (spinStatus.getSelectedItemPosition() > 0) {
            where.Clause.add(QueryAllData.Status + "='" + mStatusValues.get(spinStatus.getSelectedItemPosition()) + "'");
        }
        // from date
        if (!TextUtils.isEmpty(txtFromDate.getText())) {
            where.Clause.add(QueryAllData.Date + ">='" + DateUtils.getSQLiteStringDate(
                    getActivity(), DateUtils.getDateFromString(
                            getActivity().getApplicationContext(), String.valueOf(txtFromDate.getText()))) + "'");
        }
        // to date
        if (!TextUtils.isEmpty(txtToDate.getText())) {
            where.Clause.add(QueryAllData.Date + "<='" + DateUtils.getSQLiteStringDate(
                    getActivity(), DateUtils.getDateFromString(
                            getActivity().getApplicationContext(), String.valueOf(txtToDate.getText()))) + "'");
        }
        // payee
        if (txtSelectPayee.getTag() != null) {
            where.Clause.add(QueryAllData.PayeeID + "=" + String.valueOf(txtSelectPayee.getTag()));
        }
        // category
        if (txtSelectCategory.getTag() != null) {
            CategorySub categorySub = (CategorySub) txtSelectCategory.getTag();
            // Category. Also check the splits.
//            where.Clause.add(QueryAllData.CategID + "=?");
            where.Clause.add("(" +
                    "(" + QueryAllData.CategID + "=" + Integer.toString(categorySub.categId) + ") " +
                    " OR (" + categorySub.categId + " IN (select " + QueryAllData.CategID +
                        " FROM " + TableSplitTransactions.TABLE_NAME +
                        " WHERE " + TableSplitTransactions.TRANSID + " = " + QueryAllData.ID + "))" +
                    ")");

            // subcategory
            if (categorySub.subCategId != -1) {
                // Subcategory. Also check the splits.
//                where.Clause.add(QueryAllData.SubcategID + "=?");
                where.Clause.add("(" +
                        "(" + QueryAllData.SubcategID + "=" + Integer.toString(categorySub.subCategId) + ") " +
                            " OR " + categorySub.subCategId + " IN (select " + QueryAllData.SubcategID +
                                " FROM " + TableSplitTransactions.TABLE_NAME +
                                " WHERE " + TableSplitTransactions.TRANSID + " = " + QueryAllData.ID + ")" +
                        ")");
            }
        }
        // from amount: Trans Amount <= parameter
        if (txtFromAmount.getTag() != null) {
            where.Clause.add(QueryAllData.Amount + "<=" + String.valueOf(txtFromAmount.getTag()));
        }
        // to amount: Trans Amount >= parameter
        if (txtToAmount.getTag() != null) {
            where.Clause.add(QueryAllData.Amount + ">=" + String.valueOf(txtToAmount.getTag()));
        }
        // transaction number
        if (!TextUtils.isEmpty(edtTransNumber.getText())) {
            where.Clause.add(QueryAllData.TransactionNumber + " LIKE '" + edtTransNumber.getText() + "'");
        }
        // notes
        if (!TextUtils.isEmpty(txtNotes.getText())) {
            where.Clause.add(QueryAllData.Notes + " LIKE '%" + txtNotes.getText() + "%'");
        }

        return where;
    }

    private void showSearchResultsFragment(ParameterizedWhereClause where) {
        //create a fragment for search results.
        AllDataFragment searchResultsFragment;
        searchResultsFragment = (AllDataFragment) getActivity().getSupportFragmentManager()
                .findFragmentByTag(AllDataFragment.class.getSimpleName());
        if (searchResultsFragment != null) {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .remove(searchResultsFragment).commit();
        }

        searchResultsFragment = AllDataFragment.newInstance(-1, this);

        //create bundle
        Bundle args = new Bundle();
        args.putStringArrayList(AllDataFragment.KEY_ARGUMENTS_WHERE, where.Clause);
        args.putStringArrayList(AllDataFragment.KEY_ARGUMENTS_WHERE_PARAMS, where.Params);
        args.putString(AllDataFragment.KEY_ARGUMENTS_SORT, QueryAllData.ACCOUNTID + ", " + QueryAllData.ID);
        //set arguments
        searchResultsFragment.setArguments(args);
//        searchResultsFragment.setSearResultFragmentLoaderCallbacks((SearchActivity) getActivity());
//        searchResultsFragment.setShownHeader(true);
        if (getActivity() instanceof SearchActivity) {
            SearchActivity activity = (SearchActivity) getActivity();
            activity.ShowAccountHeaders = true;
        }

        //add fragment
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        //animation
        transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left,
                R.anim.slide_in_right, R.anim.slide_out_left);
        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack.
        if (isDualPanel()) {
            transaction.add(R.id.fragmentDetail, searchResultsFragment, AllDataFragment.class.getSimpleName());
        } else {
            transaction.replace(R.id.fragmentContent, searchResultsFragment, AllDataFragment.class.getSimpleName());
            transaction.addToBackStack(null);
        }
        // Commit the transaction
        transaction.commit();
    }

    /**
     * @return the mDualPanel
     */
    public boolean isDualPanel() {
        return mDualPanel;
    }

    /**
     * @param mDualPanel the mDualPanel to set
     */
    public void setDualPanel(boolean mDualPanel) {
        this.mDualPanel = mDualPanel;
    }

    @Override
    public void onFinishedInputAmountDialog(int id, Double amount) {
        Core core = new Core(getActivity().getApplicationContext());

        View view = getView().findViewById(id);
        if (view != null && view instanceof TextView)
            core.formatAmountTextView(((TextView) view), amount);
    }

    private class CategorySub {
        public int categId;
        public String categName;
        public int subCategId;
        public String subCategName;
    }

    private class OnDateButtonClickListener implements OnClickListener {
        private TextView mTextView;

        public OnDateButtonClickListener(TextView txtFromDate) {
            super();
            mTextView = txtFromDate;
        }

        @Override
        public void onClick(View v) {
            Calendar date = Calendar.getInstance();
            if (!TextUtils.isEmpty(mTextView.getText())) {
                date.setTime(DateUtils.getDateFromString(getActivity().getApplicationContext(), mTextView.getText().toString()));
            }
            DatePickerDialog dialog = new DatePickerDialog(getActivity(), mDateSetListener,
                    date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DATE));
            dialog.show();
        }

        private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                try {
                    Date date = new SimpleDateFormat(Constants.PATTERN_DB_DATE).parse(Integer.toString(year) +
                            "-" + Integer.toString(monthOfYear + 1) + "-" + Integer.toString(dayOfMonth));
                    mTextView.setText(DateUtils.getStringFromDate(getActivity().getApplicationContext(), date));
                } catch (Exception e) {
                    Log.e(LOGCAT, e.getMessage());
                }

            }
        };
    }
}
