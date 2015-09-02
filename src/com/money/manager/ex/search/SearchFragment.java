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
import android.content.Intent;
import android.database.sqlite.SQLiteQueryBuilder;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.money.manager.ex.common.CategoryListActivity;
import com.money.manager.ex.Constants;
import com.money.manager.ex.PayeeActivity;
import com.money.manager.ex.R;
import com.money.manager.ex.core.NumericHelper;
import com.money.manager.ex.database.AccountRepository;
import com.money.manager.ex.database.TableSplitTransactions;
import com.money.manager.ex.common.AllDataFragment;
import com.money.manager.ex.common.IInputAmountDialogListener;
import com.money.manager.ex.common.InputAmountDialog;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.database.QueryAllData;
import com.money.manager.ex.database.TableAccountList;
import com.money.manager.ex.common.IAllDataFragmentCallbacks;
import com.money.manager.ex.utils.DateUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The search form with search parameter input fields.
 */
public class SearchFragment extends Fragment
        implements IInputAmountDialogListener, IAllDataFragmentCallbacks {

    // ID REQUEST code
    private static final int REQUEST_PICK_PAYEE = 1;
    private static final int REQUEST_PICK_CATEGORY = 3;

    private static final String KEY_SEARCH_CRITERIA = "KEY_SEARCH_CRITERIA";

    // reference view into layout
    private Spinner spinAccount, spinStatus;
    private EditText txtTransNumber, txtNotes;
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
    private SearchParameters mSearchParameters;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        if (savedInstanceState != null) {
            mSearchParameters = savedInstanceState.getParcelable(KEY_SEARCH_CRITERIA);
            // restoreSearchCriteria(); called in onCreateView after the controls have been initialized.
        } else {
            mSearchParameters = new SearchParameters();
        }
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
                if (v.getTag() != null && NumericHelper.isNumeric(v.getTag().toString())) {
                    // && v.getTag() instanceof Double
                    //amount = (Double) v.getTag();
                    amount = Double.parseDouble(v.getTag().toString());
                }
                InputAmountDialog dialog = InputAmountDialog.getInstance(v.getId(), amount);
                dialog.show(getActivity().getSupportFragmentManager(), dialog.getClass().getSimpleName());
            }
        };
        //From Amount
        txtFromAmount = (TextView) view.findViewById(R.id.textViewFromAmount);
        txtFromAmount.setOnClickListener(onClickAmount);
        //To Amount
        txtToAmount = (TextView) view.findViewById(R.id.textViewToAmount);
        txtToAmount.setOnClickListener(onClickAmount);

        // Account
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
        // create adapter for spinAccount
        ArrayAdapter<String> adapterAccount = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, mAccountNameList);
        adapterAccount.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinAccount.setAdapter(adapterAccount);

        // Transaction Type checkboxes.
        cbxDeposit = (CheckBox) view.findViewById(R.id.checkBoxDeposit);
        cbxTransfer = (CheckBox) view.findViewById(R.id.checkBoxTransfer);
        cbxWithdrawal = (CheckBox) view.findViewById(R.id.checkBoxWithdrawal);

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
                Intent intent = new Intent(getActivity(), CategoryListActivity.class);
                intent.setAction(Intent.ACTION_PICK);
                startActivityForResult(intent, REQUEST_PICK_CATEGORY);
            }
        });

        // Status
        if (mStatusItems.size() <= 0) {
            // add blank row
            mStatusItems.add("");
            mStatusValues.add(SearchParameters.STRING_NULL_VALUE);

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
        txtFromDate.setOnClickListener(new OnDateButtonClickListener(getActivity(), txtFromDate));
        // to date
        txtToDate = (TextView) view.findViewById(R.id.textViewToDate);
        txtToDate.setOnClickListener(new OnDateButtonClickListener(getActivity(), txtToDate));
        // transaction number
        txtTransNumber = (EditText) view.findViewById(R.id.editTextTransNumber);
        // notes
        txtNotes = (EditText) view.findViewById(R.id.editTextNotes);

        restoreSearchCriteria();

        // Reset button.
        Button resetButton = (Button) view.findViewById(R.id.resetButton);
        resetButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mSearchParameters = new SearchParameters();
                restoreSearchCriteria();
            }
        });

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
                    categorySub.categId = data.getIntExtra(CategoryListActivity.INTENT_RESULT_CATEGID, -1);
                    categorySub.categName = data.getStringExtra(CategoryListActivity.INTENT_RESULT_CATEGNAME);
                    categorySub.subCategId = data.getIntExtra(CategoryListActivity.INTENT_RESULT_SUBCATEGID, -1);
                    categorySub.subCategName = data.getStringExtra(CategoryListActivity.INTENT_RESULT_SUBCATEGNAME);
                    //save into button
                    displayCategory(categorySub);
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

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        this.saveSearchValues();

        savedInstanceState.putParcelable(KEY_SEARCH_CRITERIA, mSearchParameters);
    }

    @Override
    public void onFinishedInputAmountDialog(int id, Double amount) {
        Core core = new Core(getActivity().getApplicationContext());

        View view = getView().findViewById(id);
        if (view != null && view instanceof TextView)
            core.formatAmountTextView(((TextView) view), amount);
    }


    /**
     * Compose arguments and execute search
     */
    public void executeSearch() {
        saveSearchValues();

        ParameterizedWhereClause where = assembleWhereClause();

        showSearchResultsFragment(where);
    }

    private void displayCategory(CategorySub categorySub) {
        if (categorySub == null) {
            txtSelectCategory.setText("");
            txtSelectCategory.setTag(null);
        } else {
            txtSelectCategory.setText(categorySub.categName + (!TextUtils.isEmpty(categorySub.subCategName) ? " : " + categorySub.subCategName : ""));
            txtSelectCategory.setTag(categorySub);
        }
    }

    /**
     * Assemble SQL query
     * @return
     */
    private ParameterizedWhereClause assembleWhereClause() {
        ParameterizedWhereClause where = new ParameterizedWhereClause();
        // todo: try using query builder
        // SQLiteQueryBuilder builder = new SQLiteQueryBuilder();

        // WHERE

        // account
        if (mSearchParameters.accountId != Constants.NOT_SET) {
            where.Clause.add(QueryAllData.TOACCOUNTID + "=" + mSearchParameters.accountId);
        }
        // transaction type
        if (mSearchParameters.deposit || mSearchParameters.transfer || mSearchParameters.withdrawal) {
            where.Clause.add(QueryAllData.TransactionType + " IN (" +
                    (mSearchParameters.deposit ? "'Deposit'" : "''") + "," +
                    (mSearchParameters.transfer ? "'Transfer'" : "''") + "," +
                    (mSearchParameters.withdrawal ? "'Withdrawal'" : "''") + ")");
        }

        // status
        if (!mSearchParameters.status.equals(SearchParameters.STRING_NULL_VALUE)) {
//            where.Clause.add(QueryAllData.Status + "='" + mSearchParameters.status + "'");
            where.Clause.add(QueryAllData.Status + "=?");
            where.Params.add(mSearchParameters.status);
        }

        // from amount
        if (!TextUtils.isEmpty(mSearchParameters.amountFrom)) {
            where.Clause.add(QueryAllData.Amount + ">=" + mSearchParameters.amountFrom);
        }
        // to amount
        if (!TextUtils.isEmpty(mSearchParameters.amountTo)) {
            where.Clause.add(QueryAllData.Amount + "<=" + mSearchParameters.amountTo);
        }

        // from date
        if (!TextUtils.isEmpty(mSearchParameters.dateFrom)) {
            where.Clause.add(QueryAllData.Date + ">='" + DateUtils.getSQLiteStringDate(
                    getActivity(), DateUtils.getDateFromString(
                            getActivity().getApplicationContext(), mSearchParameters.dateFrom)) + "'");
        }
        // to date
        if (!TextUtils.isEmpty(mSearchParameters.dateTo)) {
            where.Clause.add(QueryAllData.Date + "<='" + DateUtils.getSQLiteStringDate(
                    getActivity(), DateUtils.getDateFromString(
                            getActivity().getApplicationContext(), mSearchParameters.dateTo)) + "'");
        }
        // payee
        if (mSearchParameters.payeeId != null && mSearchParameters.payeeId > 0) {
            where.Clause.add(QueryAllData.PayeeID + "=" + mSearchParameters.payeeId);
        }
        // category
        if (mSearchParameters.category != null) {
            CategorySub categorySub = mSearchParameters.category;
            // Category. Also check the splits.
            where.Clause.add("(" +
                    "(" + QueryAllData.CategID + "=" + Integer.toString(categorySub.categId) + ") " +
                    " OR (" + categorySub.categId + " IN (select " + QueryAllData.CategID +
                        " FROM " + TableSplitTransactions.TABLE_NAME +
                        " WHERE " + TableSplitTransactions.TRANSID + " = " + QueryAllData.ID + "))" +
                    ")");

            // subcategory
            if (categorySub.subCategId != -1) {
                // Subcategory. Also check the splits.
                where.Clause.add("(" +
                        "(" + QueryAllData.SubcategID + "=" + Integer.toString(categorySub.subCategId) + ") " +
                            " OR " + categorySub.subCategId + " IN (select " + QueryAllData.SubcategID +
                                " FROM " + TableSplitTransactions.TABLE_NAME +
                                " WHERE " + TableSplitTransactions.TRANSID + " = " + QueryAllData.ID + ")" +
                        ")");
            }
        }

        // transaction number
        if (!TextUtils.isEmpty(mSearchParameters.transactionNumber)) {
            where.Clause.add(QueryAllData.TransactionNumber + " LIKE '" + mSearchParameters.transactionNumber + "'");
        }
        // notes
        if (!TextUtils.isEmpty(mSearchParameters.notes)) {
            where.Clause.add(QueryAllData.Notes + " LIKE '%" + mSearchParameters.notes + "%'");
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
        // Sorting
        args.putString(AllDataFragment.KEY_ARGUMENTS_SORT,
                QueryAllData.TOACCOUNTID + ", " + QueryAllData.TransactionType + ", " + QueryAllData.ID);
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

    private void saveSearchValues() {
        // Account
        if (spinAccount != null) {
            if (spinAccount.getSelectedItemPosition() != AdapterView.INVALID_POSITION &&
                    mAccountIdList.get(spinAccount.getSelectedItemPosition()) != -1) {
                mSearchParameters.accountId = mAccountIdList.get(spinAccount.getSelectedItemPosition());
            }
        }

        // Transaction Type
        mSearchParameters.deposit = cbxDeposit.isChecked();
        mSearchParameters.transfer = cbxTransfer.isChecked();
        mSearchParameters.withdrawal = cbxWithdrawal.isChecked();

        // Status
        if (spinStatus.getSelectedItemPosition() > 0) {
            mSearchParameters.status = mStatusValues.get(spinStatus.getSelectedItemPosition());
        }

        // Amount from
        if (txtFromAmount.getTag() != null) {
            mSearchParameters.amountFrom = String.valueOf(txtFromAmount.getTag());
        }
        // Amount to
        if (txtToAmount.getTag() != null) {
            mSearchParameters.amountTo = String.valueOf(txtToAmount.getTag());
        }

        // Date from
        if (!TextUtils.isEmpty(txtFromDate.getText())) {
            mSearchParameters.dateFrom = String.valueOf(txtFromDate.getText());
        }
        // Date to
        if (!TextUtils.isEmpty(txtToDate.getText())) {
            mSearchParameters.dateTo = String.valueOf(txtToDate.getText());
        }
        // Payee
        if (txtSelectPayee.getTag() != null) {
            mSearchParameters.payeeId = Integer.parseInt(txtSelectPayee.getTag().toString());
            mSearchParameters.payeeName = txtSelectPayee.getText().toString();
        }
        // Category
        if (txtSelectCategory.getTag() != null) {
            mSearchParameters.category = (CategorySub) txtSelectCategory.getTag();
        }
        // Transaction number
        if (!TextUtils.isEmpty(txtTransNumber.getText())) {
            mSearchParameters.transactionNumber = txtTransNumber.getText().toString();
        }
        // Notes
        if (!TextUtils.isEmpty(txtNotes.getText())) {
            mSearchParameters.notes = txtNotes.getText().toString();
        }
    }

    private void restoreSearchCriteria() {
        // Account
        // no need to restore. The collection is kept in memory.

        // Transaction Type
        cbxDeposit.setChecked(mSearchParameters.deposit);
        cbxTransfer.setChecked(mSearchParameters.transfer);
        cbxWithdrawal.setChecked(mSearchParameters.withdrawal);

        // Status
        // Status is also stored as the collection is kept in an instance variable.

        // Amount from
        txtFromAmount.setText(mSearchParameters.amountFrom);
        txtFromAmount.setTag(mSearchParameters.amountFrom);
        // Amount to
        txtToAmount.setText(mSearchParameters.amountTo);
        txtToAmount.setTag(mSearchParameters.amountTo);

        // Date from
        txtFromDate.setText(mSearchParameters.dateFrom);
        // Date to
        txtToDate.setText(mSearchParameters.dateTo);
        // Payee
        txtSelectPayee.setTag(mSearchParameters.payeeId);
        txtSelectPayee.setText(mSearchParameters.payeeName);
        // Category
        displayCategory(mSearchParameters.category);
        // Transaction number
        txtTransNumber.setText(mSearchParameters.transactionNumber);
        // Notes
        txtNotes.setText(mSearchParameters.notes);
    }

}
