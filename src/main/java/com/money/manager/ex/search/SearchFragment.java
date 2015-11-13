/*
 * Copyright (C) 2012-2015 The Android Money Manager Ex Project Team
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

import com.money.manager.ex.domainmodel.SplitCategory;
import com.money.manager.ex.servicelayer.AccountService;
import com.money.manager.ex.common.AllDataListFragment;
import com.money.manager.ex.common.CategoryListActivity;
import com.money.manager.ex.Constants;
import com.money.manager.ex.PayeeActivity;
import com.money.manager.ex.R;
import com.money.manager.ex.common.ICommonFragmentCallbacks;
import com.money.manager.ex.core.ExceptionHandler;
import com.money.manager.ex.core.FormatUtilities;
import com.money.manager.ex.core.NumericHelper;
import com.money.manager.ex.common.IInputAmountDialogListener;
import com.money.manager.ex.common.InputAmountDialog;
import com.money.manager.ex.database.QueryAllData;
import com.money.manager.ex.database.WhereStatementGenerator;
import com.money.manager.ex.domainmodel.Account;
import com.money.manager.ex.settings.AppSettings;
import com.money.manager.ex.settings.LookAndFeelSettings;
import com.money.manager.ex.utils.DateTimeUtils;
import com.money.manager.ex.utils.DateUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import hirondelle.date4j.DateTime;
import info.javaperformance.money.Money;
import info.javaperformance.money.MoneyFactory;

/**
 * The search form with search parameter input fields.
 */
public class SearchFragment extends Fragment
        implements IInputAmountDialogListener {

    // ID REQUEST code
    public static final int REQUEST_PICK_PAYEE = 1;
    public static final int REQUEST_PICK_CATEGORY = 3;
    public static final int REQUEST_AMOUNT = 4;

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

        View view = inflater.inflate(R.layout.search_fragment, container, false);

        initializeUiControlVariables(view);

        initializeAmountSelectors(view);

        // Account
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

        //Payee
        txtSelectPayee.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), PayeeActivity.class);
                intent.setAction(Intent.ACTION_PICK);
                startActivityForResult(intent, REQUEST_PICK_PAYEE);
            }
        });

        //Category
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
        ArrayAdapter<String> adapterStatus = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, mStatusItems);
        adapterStatus.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinStatus.setAdapter(adapterStatus);

        // from date
        txtFromDate.setOnClickListener(new OnDateButtonClickListener(getActivity(), txtFromDate));
        // to date
        txtToDate.setOnClickListener(new OnDateButtonClickListener(getActivity(), txtToDate));

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
                    txtSelectPayee.setTag(data.getIntExtra(PayeeActivity.INTENT_RESULT_PAYEEID, Constants.NOT_SET));
                    txtSelectPayee.setText(data.getStringExtra(PayeeActivity.INTENT_RESULT_PAYEENAME));
                }
                break;
            case REQUEST_PICK_CATEGORY:
                if ((resultCode == Activity.RESULT_OK) && (data != null)) {
                    //create class for store data
                    CategorySub categorySub = new CategorySub();
                    categorySub.categId = data.getIntExtra(CategoryListActivity.INTENT_RESULT_CATEGID, Constants.NOT_SET);
                    categorySub.categName = data.getStringExtra(CategoryListActivity.INTENT_RESULT_CATEGNAME);
                    categorySub.subCategId = data.getIntExtra(CategoryListActivity.INTENT_RESULT_SUBCATEGID, Constants.NOT_SET);
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

        View view = getView();
        if (view != null) {
            initializeUiControlVariables(view);
            mSearchParameters = collectSearchCriteria();
        }

        savedInstanceState.putParcelable(KEY_SEARCH_CRITERIA, mSearchParameters);
    }

    @Override
    public void onFinishedInputAmountDialog(int id, Money amount) {
        View rootView = getView();
        if (rootView == null) return;

        View view = rootView.findViewById(id);
        if (view != null && view instanceof TextView) {
            TextView textView = (TextView) view;

            // save the value in tag?
            String value = amount.toString();
            textView.setTag(value);

            // display amount
            FormatUtilities format = new FormatUtilities(getActivity());
            String displayAmount = format.formatWithLocale(amount);
            textView.setText(displayAmount);
        }
    }

    /**
     * Compose arguments and execute search
     */
    public void executeSearch() {
        mSearchParameters = collectSearchCriteria();

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

    /**
     * Assemble SQL query
     * @return where clause with parameters
     */
    private String assembleWhereClause() {
        WhereStatementGenerator where = new WhereStatementGenerator();

        // account
        if (mSearchParameters.accountId != null && mSearchParameters.accountId != Constants.NOT_SET) {
            where.addStatement(
                where.concatenateOr(
                    where.getStatement(QueryAllData.ACCOUNTID, "=", mSearchParameters.accountId),
                    where.getStatement(QueryAllData.TOACCOUNTID, "=", mSearchParameters.accountId)
                )
            );
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
        if (mSearchParameters.amountFrom != null) {
            where.addStatement(QueryAllData.Amount, ">=", mSearchParameters.amountFrom);
        }
        // to amount
        if (mSearchParameters.amountTo != null) {
            where.addStatement(QueryAllData.Amount, "<=", mSearchParameters.amountTo);
        }

        // from date
        if (mSearchParameters.dateFrom != null) {
            where.addStatement(QueryAllData.Date, ">=", mSearchParameters.dateFrom.format(Constants.ISO_DATE_FORMAT));
        }
        // to date
        if (mSearchParameters.dateTo != null) {
            where.addStatement(QueryAllData.Date, "<=", DateUtils.getIsoStringDate(mSearchParameters.dateTo));
        }
        // payee
        if (mSearchParameters.payeeId != null) {
            where.addStatement(QueryAllData.PayeeID, "=", mSearchParameters.payeeId);
        }
        // category
        if (mSearchParameters.category != null) {
            CategorySub categorySub = mSearchParameters.category;
            // Category. Also check the splits.
            where.addStatement("(" +
                "(" + QueryAllData.CategID + "=" + Integer.toString(categorySub.categId) + ") " +
                " OR (" + categorySub.categId + " IN (select " + QueryAllData.CategID +
                    " FROM " + SplitCategory.TABLE_NAME +
                    " WHERE " + SplitCategory.TRANSID + "=" + QueryAllData.ID + ")" +
                ")" +
            ")");

            // subcategory
            if (categorySub.subCategId != Constants.NOT_SET) {
                // Subcategory. Also check the splits.
                where.addStatement("(" +
                    "(" + QueryAllData.SubcategID + "=" + Integer.toString(categorySub.subCategId) + ") " +
                        " OR (" + categorySub.subCategId + " IN (select " + QueryAllData.SubcategID +
                            " FROM " + SplitCategory.TABLE_NAME +
                            " WHERE " + SplitCategory.TRANSID + " = " + QueryAllData.ID + ")" +
                        ")" +
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

    private void displaySearchCriteria() {
        // Account
        this.spinAccount.setSelection(0);

        // Transaction Type
        cbxDeposit.setChecked(mSearchParameters.deposit);
        cbxTransfer.setChecked(mSearchParameters.transfer);
        cbxWithdrawal.setChecked(mSearchParameters.withdrawal);

        // Status
        this.spinStatus.setSelection(0);

        // Amount from
        if (mSearchParameters.amountFrom != null) {
            FormatUtilities format = new FormatUtilities(getActivity());
            String displayAmount = format.formatWithLocale(mSearchParameters.amountFrom);
            txtFromAmount.setText(displayAmount);

            txtFromAmount.setTag(mSearchParameters.amountFrom.toString());
        } else {
            txtFromAmount.setText("");
            txtFromAmount.setTag(null);
        }

        // Amount to
        if (mSearchParameters.amountTo != null) {
            FormatUtilities format = new FormatUtilities(getActivity());
            String displayAmount = format.formatWithLocale(mSearchParameters.amountTo);
            txtToAmount.setText(displayAmount);

            txtToAmount.setTag(mSearchParameters.amountTo.toString());
        } else {
            txtToAmount.setText("");
            txtToAmount.setTag(null);
        }

        // Date from
        if (mSearchParameters.dateFrom != null) {
            txtFromDate.setTag(mSearchParameters.dateFrom.format(Constants.ISO_DATE_FORMAT));
        }
        txtFromDate.setText(DateTimeUtils.getUserStringFromDateTime(getContext(), mSearchParameters.dateFrom));
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

    public void handleSearchRequest(SearchParameters parameters) {
        if (parameters == null) return;

        mSearchParameters = parameters;
        displaySearchCriteria();

        executeSearch();
    }

    private void initializeAmountSelectors(View view) {
        //create listener amount
        OnClickListener onClickAmount = new OnClickListener() {
            @Override
            public void onClick(View v) {
                Money amount = MoneyFactory.fromString("0");
                Object tag = v.getTag();
                if (tag != null && NumericHelper.isNumeric(tag.toString())) {
                    amount = MoneyFactory.fromString(tag.toString());
                }
                InputAmountDialog dialog = InputAmountDialog.getInstance(v.getId(), amount);
                dialog.setTargetFragment(SearchFragment.this, REQUEST_AMOUNT);
                dialog.show(getActivity().getSupportFragmentManager(), dialog.getClass().getSimpleName());
            }
        };
        //From Amount
        txtFromAmount = (TextView) view.findViewById(R.id.textViewFromAmount);
        txtFromAmount.setOnClickListener(onClickAmount);
        //To Amount
        txtToAmount = (TextView) view.findViewById(R.id.textViewToAmount);
        txtToAmount.setOnClickListener(onClickAmount);
    }

    private SearchParameters collectSearchCriteria() {
        // Account
        if (this.spinAccount != null) {
            int selectedAccountPosition = spinAccount.getSelectedItemPosition();
            if (selectedAccountPosition != AdapterView.INVALID_POSITION) {
                int selectedAccountId = mAccountIdList.get(selectedAccountPosition);
                if (selectedAccountId != Constants.NOT_SET) {
                    mSearchParameters.accountId = selectedAccountId;
                }
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
        Object tag = txtFromAmount.getTag();
        if (tag != null) {
            mSearchParameters.amountFrom = MoneyFactory.fromString((String) tag);
        }
        // Amount to
        tag = txtToAmount.getTag();
        if (tag != null) {
            mSearchParameters.amountTo = MoneyFactory.fromString((String) tag);
        }

        // Date from
        if (txtFromDate.getTag() != null) {
            mSearchParameters.dateFrom = new DateTime(txtFromDate.getTag().toString());
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

        return mSearchParameters;
    }

    private void showSearchResultsFragment(String where) {
        //create a fragment for search results.
        AllDataListFragment searchResultsFragment = (AllDataListFragment) getActivity().getSupportFragmentManager()
                .findFragmentByTag(AllDataListFragment.class.getSimpleName());

        if (searchResultsFragment != null) {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .remove(searchResultsFragment)
                    .commit();
        }

        searchResultsFragment = AllDataListFragment.newInstance(Constants.NOT_SET);

        searchResultsFragment.showTotalsFooter();

        //create parameter bundle
        Bundle args = new Bundle();
        args.putString(AllDataListFragment.KEY_ARGUMENTS_WHERE, where);
        // Sorting
        args.putString(AllDataListFragment.KEY_ARGUMENTS_SORT,
                QueryAllData.TOACCOUNTID + ", " + QueryAllData.Date + ", " +
                QueryAllData.TransactionType + ", " + QueryAllData.ID);
        //set arguments
        searchResultsFragment.setArguments(args);

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

    private void initializeUiControlVariables(View view) {
        if (view == null) return;

        spinAccount = (Spinner) view.findViewById(R.id.spinnerAccount);

        // Transaction Type checkboxes.
        cbxDeposit = (CheckBox) view.findViewById(R.id.checkBoxDeposit);
        cbxTransfer = (CheckBox) view.findViewById(R.id.checkBoxTransfer);
        cbxWithdrawal = (CheckBox) view.findViewById(R.id.checkBoxWithdrawal);

        txtSelectPayee = (TextView) view.findViewById(R.id.textViewSelectPayee);
        txtSelectCategory = (TextView) view.findViewById(R.id.textViewSelectCategory);

        spinStatus = (Spinner) view.findViewById(R.id.spinnerStatus);

        txtFromDate = (TextView) view.findViewById(R.id.textViewFromDate);
        txtToDate = (TextView) view.findViewById(R.id.textViewToDate);

        // transaction number
        txtTransNumber = (EditText) view.findViewById(R.id.editTextTransNumber);
        // notes
        txtNotes = (EditText) view.findViewById(R.id.editTextNotes);
    }
}
