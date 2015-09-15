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

import com.money.manager.ex.businessobjects.AccountService;
import com.money.manager.ex.common.AllDataListFragment;
import com.money.manager.ex.common.CategoryListActivity;
import com.money.manager.ex.Constants;
import com.money.manager.ex.PayeeActivity;
import com.money.manager.ex.R;
import com.money.manager.ex.common.ICommonFragmentCallbacks;
import com.money.manager.ex.core.ExceptionHandler;
import com.money.manager.ex.core.NumericHelper;
import com.money.manager.ex.database.TableSplitTransactions;
import com.money.manager.ex.common.IInputAmountDialogListener;
import com.money.manager.ex.common.InputAmountDialog;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.database.QueryAllData;
import com.money.manager.ex.database.WhereStatementGenerator;
import com.money.manager.ex.domainmodel.Account;
import com.money.manager.ex.settings.AppSettings;
import com.money.manager.ex.settings.LookAndFeelSettings;
import com.money.manager.ex.utils.DateUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import info.javaperformance.money.Money;
import info.javaperformance.money.MoneyFactory;

/**
 * The search form with search parameter input fields.
 */
public class SearchFragment extends Fragment
        implements IInputAmountDialogListener {

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
    private List<Account> mAccountList;
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
            // displaySearchCriteria(); called in onCreateView after the controls have been initialized.
        } else {
            mSearchParameters = new SearchParameters();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (container == null) return null;

//        Core core = new Core(getActivity().getApplicationContext());
        //create view
        View view = inflater.inflate(R.layout.search_fragment, container, false);

        //create listener amount
        OnClickListener onClickAmount = new OnClickListener() {
            @Override
            public void onClick(View v) {
                Money amount = MoneyFactory.fromString("0");
                if (v.getTag() != null && NumericHelper.isNumeric(v.getTag().toString())) {
                    amount = MoneyFactory.fromString(v.getTag().toString());
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
            LookAndFeelSettings settings = new AppSettings(getContext()).getLookAndFeelSettings();
            AccountService accountService = new AccountService(getContext());
            mAccountList = accountService.getAccountList(
                    settings.getViewOpenAccounts(),
                    settings.getViewFavouriteAccounts());
            mAccountList.add(0, null);
            for (int i = 0; i <= mAccountList.size() - 1; i++) {
                if (mAccountList.get(i) != null) {
                    mAccountNameList.add(mAccountList.get(i).getName());
                    mAccountIdList.add(mAccountList.get(i).getId());
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

        // Reset button.
        Button resetButton = (Button) view.findViewById(R.id.resetButton);
        resetButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mSearchParameters = new SearchParameters();
                displaySearchCriteria();
            }
        });

        // Store search criteria values into the controls.
        displaySearchCriteria();

        ICommonFragmentCallbacks listener = (ICommonFragmentCallbacks) getActivity();
        if (listener != null) {
            listener.onFragmentViewCreated(this.getTag());
        }

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

        this.saveSearchCriteria();

        savedInstanceState.putParcelable(KEY_SEARCH_CRITERIA, mSearchParameters);
    }

    @Override
    public void onFinishedInputAmountDialog(int id, Money amount) {
        View rootView = getView();
        if (rootView == null) return;

        Core core = new Core(getActivity().getApplicationContext());

        View view = rootView.findViewById(id);
        if (view != null && view instanceof TextView)
            core.formatAmountTextView(((TextView) view), amount);
    }

    /**
     * Compose arguments and execute search
     */
    public void executeSearch() {
        saveSearchCriteria();

        String where = assembleWhereClause();

        try {
            showSearchResultsFragment(where);
        } catch (Exception e) {
            ExceptionHandler handler = new ExceptionHandler(getContext(), this);
            handler.handle(e, "showing result fragment");
        }
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

    // Private

    private void displayCategory(CategorySub categorySub) {
        if (categorySub == null) {
            txtSelectCategory.setText("");
            txtSelectCategory.setTag(null);
        } else {
            txtSelectCategory.setText(categorySub.categName +
                    (!TextUtils.isEmpty(categorySub.subCategName) ? " : " + categorySub.subCategName : ""));
            txtSelectCategory.setTag(categorySub);
        }
    }

    /**
     * Assemble SQL query
     * @return where clause with parameters
     */
    private String assembleWhereClause() {
        WhereStatementGenerator where = new WhereStatementGenerator();

        // todo: try using query builder
        // SQLiteQueryBuilder builder = new SQLiteQueryBuilder();

        // account
        if (mSearchParameters.accountId != Constants.NOT_SET) {
            where.addStatement(QueryAllData.TOACCOUNTID, "=", mSearchParameters.accountId);
        }
        // transaction type
        if (mSearchParameters.deposit || mSearchParameters.transfer || mSearchParameters.withdrawal) {
            where.addStatement(QueryAllData.TransactionType + " IN (" +
                    (mSearchParameters.deposit ? "'Deposit'" : "''") + "," +
                    (mSearchParameters.transfer ? "'Transfer'" : "''") + "," +
                    (mSearchParameters.withdrawal ? "'Withdrawal'" : "''") + ")");
        }

        // status
        if (!mSearchParameters.status.equals(SearchParameters.STRING_NULL_VALUE)) {
            where.addStatement(QueryAllData.Status, "=", mSearchParameters.status);
        }

        // from amount
        if (!TextUtils.isEmpty(mSearchParameters.amountFrom)) {
            where.addStatement(QueryAllData.Amount, ">=", mSearchParameters.amountFrom);
        }
        // to amount
        if (!TextUtils.isEmpty(mSearchParameters.amountTo)) {
            where.addStatement(QueryAllData.Amount, "<=", mSearchParameters.amountTo);
        }

        // from date
        if (mSearchParameters.dateFrom != null) {
            where.addStatement(QueryAllData.Date, ">=", DateUtils.getIsoStringDate(mSearchParameters.dateFrom));
        }
        // to date
        if (mSearchParameters.dateTo != null) {
            where.addStatement(QueryAllData.Date, "<=", DateUtils.getIsoStringDate(mSearchParameters.dateTo));
        }
        // payee
        if (mSearchParameters.payeeId != null && mSearchParameters.payeeId > 0) {
            where.addStatement(QueryAllData.PayeeID, "=", mSearchParameters.payeeId);
        }
        // category
        if (mSearchParameters.category != null) {
            CategorySub categorySub = mSearchParameters.category;
            // Category. Also check the splits.
            where.addStatement("(" +
                    "(" + QueryAllData.CategID + "=" + Integer.toString(categorySub.categId) + ") " +
                    " OR (" + categorySub.categId + " IN (select " + QueryAllData.CategID +
                        " FROM " + TableSplitTransactions.TABLE_NAME +
                        " WHERE " + TableSplitTransactions.TRANSID + "=" + QueryAllData.ID + "))" +
                    ")");

            // subcategory
            if (categorySub.subCategId != -1) {
                // Subcategory. Also check the splits.
                where.addStatement("(" +
                        "(" + QueryAllData.SubcategID + "=" + Integer.toString(categorySub.subCategId) + ") " +
                            " OR " + categorySub.subCategId + " IN (select " + QueryAllData.SubcategID +
                                " FROM " + TableSplitTransactions.TABLE_NAME +
                                " WHERE " + TableSplitTransactions.TRANSID + " = " + QueryAllData.ID + ")" +
                        ")");
            }
        }

        // transaction number
        if (!TextUtils.isEmpty(mSearchParameters.transactionNumber)) {
            where.addStatement(QueryAllData.TransactionNumber, " LIKE ", mSearchParameters.transactionNumber);
        }
        // notes
        if (!TextUtils.isEmpty(mSearchParameters.notes)) {
            where.addStatement(QueryAllData.Notes + " LIKE '%" + mSearchParameters.notes + "%'");
        }

        return where.getWhere();
    }

    public void handleSearchRequest(SearchParameters parameters) {
        if (parameters == null) return;

        mSearchParameters = parameters;
        displaySearchCriteria();

        executeSearch();
    }

    private void showSearchResultsFragment(String where) {
        //create a fragment for search results.
        AllDataListFragment searchResultsFragment;
        searchResultsFragment = (AllDataListFragment) getActivity().getSupportFragmentManager()
                .findFragmentByTag(AllDataListFragment.class.getSimpleName());
        if (searchResultsFragment != null) {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .remove(searchResultsFragment)
                    .commit();
        }

        searchResultsFragment = AllDataListFragment.newInstance(-1);

        //create parameter bundle
        Bundle args = new Bundle();
        args.putString(AllDataListFragment.KEY_ARGUMENTS_WHERE, where);
//        args.putStringArrayList(AllDataListFragment.KEY_ARGUMENTS_WHERE_PARAMS, where.Params);
        // Sorting
        args.putString(AllDataListFragment.KEY_ARGUMENTS_SORT,
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
            transaction.add(R.id.fragmentDetail, searchResultsFragment, AllDataListFragment.class.getSimpleName());
        } else {
            // transaction.remove()
            transaction.replace(R.id.fragmentContent, searchResultsFragment, AllDataListFragment.class.getSimpleName());
            transaction.addToBackStack(null);
        }
        // Commit the transaction
        transaction.commit();
    }

    private void saveSearchCriteria() {
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
        if (txtFromDate.getTag() != null) {
            mSearchParameters.dateFrom = (Date) txtFromDate.getTag();
        }
        // Date to
        if (txtToDate.getTag() != null) {
            mSearchParameters.dateTo = (Date) txtToDate.getTag();
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

    private void displaySearchCriteria() {
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
        txtFromDate.setTag(mSearchParameters.dateFrom);
        txtFromDate.setText(DateUtils.getUserStringFromDate(getContext(), mSearchParameters.dateFrom));
        // Date to
        txtToDate.setTag(mSearchParameters.dateTo);
        txtToDate.setText(DateUtils.getUserStringFromDate(getContext(), mSearchParameters.dateTo));
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
