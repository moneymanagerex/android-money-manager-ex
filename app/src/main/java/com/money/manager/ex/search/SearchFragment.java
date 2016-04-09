/*
 * Copyright (C) 2012-2016 The Android Money Manager Ex Project Team
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
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
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

import com.money.manager.ex.common.AmountInputDialog;
import com.money.manager.ex.common.events.AmountEnteredEvent;
import com.money.manager.ex.domainmodel.SplitCategory;
import com.money.manager.ex.servicelayer.AccountService;
import com.money.manager.ex.common.CategoryListActivity;
import com.money.manager.ex.Constants;
import com.money.manager.ex.PayeeActivity;
import com.money.manager.ex.R;
import com.money.manager.ex.core.FormatUtilities;
import com.money.manager.ex.core.NumericHelper;
import com.money.manager.ex.database.QueryAllData;
import com.money.manager.ex.database.WhereStatementGenerator;
import com.money.manager.ex.domainmodel.Account;
import com.money.manager.ex.settings.AppSettings;
import com.money.manager.ex.settings.LookAndFeelSettings;
import com.money.manager.ex.utils.MyDateTimeUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.joda.time.DateTime;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import info.javaperformance.money.Money;
import info.javaperformance.money.MoneyFactory;

/**
 * The form with search parameter input fields.
 */
public class SearchFragment
    extends Fragment {

    public static final int REQUEST_PICK_PAYEE = 1;
    public static final int REQUEST_PICK_CATEGORY = 3;

    private static final String KEY_SEARCH_CRITERIA = "KEY_SEARCH_CRITERIA";

    // reference view into layout
    private Spinner spinAccount, spinStatus;
    private EditText txtTransNumber, txtNotes;
    private TextView txtToAmount, txtFromAmount, txtSelectCategory, txtSelectPayee, txtDateFrom, txtDateTo;
    private CheckBox cbxWithdrawal, cbxDeposit, cbxTransfer;
    // arrays list account name and account id
    private ArrayList<String> mAccountNameList = new ArrayList<>();
    private ArrayList<Integer> mAccountIdList = new ArrayList<>();
    private List<Account> mAccountList;
    // status item and values
    private ArrayList<String> mStatusItems = new ArrayList<>();
    private ArrayList<String> mStatusValues = new ArrayList<>();

    public static SearchFragment createInstance() {
        SearchFragment fragment = new SearchFragment();

        Bundle args = new Bundle();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        if (getSearchParameters() == null) {
            setSearchParameters(new SearchParameters());
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);

        super.onStop();
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

        // Date from
        txtDateFrom.setOnClickListener(new OnDateButtonClickListener(getActivity(), txtDateFrom));
        // Date to
        txtDateTo.setOnClickListener(new OnDateButtonClickListener(getActivity(), txtDateTo));

        initializeResetButton(view);

        // Store search criteria values into the controls.
        displaySearchCriteria(view);

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
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        View view = getView();
        if (view != null) {
            initializeUiControlVariables(view);
            setSearchParameters(collectSearchCriteria());
        }

        SearchParameters searchParameters = getSearchParameters();
        savedInstanceState.putParcelable(KEY_SEARCH_CRITERIA, Parcels.wrap(searchParameters));
    }

    // Events

    @Subscribe
    public void onEvent(AmountEnteredEvent event) {
        View rootView = getView();
        if (rootView == null) return;

        int id = Integer.parseInt(event.requestId);
        View view = rootView.findViewById(id);
        if (view != null && view instanceof TextView) {
            TextView textView = (TextView) view;

            // save the value in tag?
            String value = event.amount.toString();
            textView.setTag(value);

            // display amount
            FormatUtilities format = new FormatUtilities(getActivity());
            String displayAmount = format.formatWithLocale(event.amount);
            textView.setText(displayAmount);
        }
    }

    // Public

    public SearchParameters getSearchParameters() {
        Bundle arguments = getArguments();
        if (arguments == null) return null;

        Parcelable searchParcel = arguments.getParcelable(KEY_SEARCH_CRITERIA);
        if (searchParcel == null) return null;

        SearchParameters parameters = Parcels.unwrap(searchParcel);
        return parameters;
    }

    public String getWhereStatement() {
        // Store parameters from UI.
        SearchParameters searchParameters = collectSearchCriteria();
        setSearchParameters(searchParameters);

        String where = assembleWhereClause();

        return where;
    }

    public void setSearchParameters(SearchParameters parameters) {
        if (parameters == null) return;

        getArguments().putParcelable(KEY_SEARCH_CRITERIA, Parcels.wrap(parameters));
        displaySearchCriteria();
    }

    // Private

    /**
     * Assemble SQL query from the search parameters.
     * @return where clause with parameters
     */
    private String assembleWhereClause() {
        WhereStatementGenerator where = new WhereStatementGenerator();
        SearchParameters searchParameters = getSearchParameters();

        // account
        if (searchParameters.accountId != null && searchParameters.accountId != Constants.NOT_SET) {
            where.addStatement(
                    where.concatenateOr(
                            where.getStatement(QueryAllData.ACCOUNTID, "=", searchParameters.accountId),
                            where.getStatement(QueryAllData.TOACCOUNTID, "=", searchParameters.accountId)
                    )
            );
        }
        // transaction type
        if (searchParameters.deposit || searchParameters.transfer || searchParameters.withdrawal) {
            where.addStatement(QueryAllData.TransactionType + " IN (" +
                    (searchParameters.deposit ? "'Deposit'" : "''") + "," +
                    (searchParameters.transfer ? "'Transfer'" : "''") + "," +
                    (searchParameters.withdrawal ? "'Withdrawal'" : "''") + ")");
        }

        // status
        if (!searchParameters.status.equals(SearchParameters.STRING_NULL_VALUE)) {
            where.addStatement(QueryAllData.Status, "=", searchParameters.status);
        }

        addAmountStatements(where, searchParameters);

        // from date
        if (searchParameters.dateFrom != null) {
            where.addStatement(QueryAllData.Date, " >= ", MyDateTimeUtils.getIsoStringFrom(searchParameters.dateFrom));
        }
        // to date
        if (searchParameters.dateTo != null) {
            where.addStatement(QueryAllData.Date, " <= ", MyDateTimeUtils.getIsoStringFrom(searchParameters.dateTo));
        }
        // payee
        if (searchParameters.payeeId != null) {
            where.addStatement(QueryAllData.PayeeID, " = ", searchParameters.payeeId);
        }
        // category
        if (searchParameters.category != null) {
            CategorySub categorySub = searchParameters.category;
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
        if (!TextUtils.isEmpty(searchParameters.transactionNumber)) {
            where.addStatement(QueryAllData.TransactionNumber, " LIKE ", searchParameters.transactionNumber);
        }
        // notes
        if (!TextUtils.isEmpty(searchParameters.notes)) {
            where.addStatement(QueryAllData.Notes + " LIKE '%" + searchParameters.notes + "%'");
        }

        return where.getWhere();
    }

    private void addAmountStatements(WhereStatementGenerator where, SearchParameters searchParameters) {
        if (searchParameters.amountFrom != null && searchParameters.amountTo != null) {
            addAmountStatementForBothAmounts(where, searchParameters);
            return;
        }

        // Only one on no amounts entered.

        // from amount
        if (searchParameters.amountFrom != null) {
            where.addStatement(QueryAllData.Amount, " >= ", searchParameters.amountFrom);
        }
        // to amount
        if (searchParameters.amountTo != null) {
            where.addStatement(QueryAllData.Amount, " <= ", searchParameters.amountTo);
        }

    }

    private void addAmountStatementForBothAmounts(WhereStatementGenerator where, SearchParameters searchParameters) {
        // Automatically decide from/to amounts by comparing them.
        Money lowerAmount = searchParameters.amountFrom.compareTo(searchParameters.amountTo) == -1
                ? searchParameters.amountFrom
                : searchParameters.amountTo;
        Money higherAmount = searchParameters.amountFrom.compareTo(searchParameters.amountTo) == 1
                ? searchParameters.amountFrom
                : searchParameters.amountTo;

        // from amount
        if (searchParameters.amountFrom != null) {
            where.addStatement(QueryAllData.Amount, " >= ", lowerAmount);
        }
        // to amount
        if (searchParameters.amountTo != null) {
            where.addStatement(QueryAllData.Amount, " <= ", higherAmount);
        }
    }

    private SearchParameters collectSearchCriteria() {
        if (getView() == null) {
            return getSearchParameters();
        }

        SearchParameters searchParameters = getSearchParameters();

        // Account
        if (this.spinAccount != null) {
            int selectedAccountPosition = spinAccount.getSelectedItemPosition();
            if (selectedAccountPosition != AdapterView.INVALID_POSITION) {
                int selectedAccountId = mAccountIdList.get(selectedAccountPosition);
                if (selectedAccountId != Constants.NOT_SET) {
                    searchParameters.accountId = selectedAccountId;
                }
            }
        }

        // Transaction Type
        searchParameters.deposit = cbxDeposit.isChecked();
        searchParameters.transfer = cbxTransfer.isChecked();
        searchParameters.withdrawal = cbxWithdrawal.isChecked();

        // Status
        if (spinStatus.getSelectedItemPosition() > 0) {
            searchParameters.status = mStatusValues.get(spinStatus.getSelectedItemPosition());
        }

        // Amount from
        Object tag = txtFromAmount.getTag();
        if (tag != null) {
            searchParameters.amountFrom = MoneyFactory.fromString((String) tag);
        }
        // Amount to
        tag = txtToAmount.getTag();
        if (tag != null) {
            searchParameters.amountTo = MoneyFactory.fromString((String) tag);
        }

        // Date from
        if (txtDateFrom.getTag() != null) {
            searchParameters.dateFrom = new DateTime(txtDateFrom.getTag().toString());
        }
        // Date to
        if (txtDateTo.getTag() != null) {
            String dateString = txtDateTo.getTag().toString();
            searchParameters.dateTo = MyDateTimeUtils.from(dateString);
        }
        // Payee
        if (txtSelectPayee.getTag() != null) {
            searchParameters.payeeId = Integer.parseInt(txtSelectPayee.getTag().toString());
            searchParameters.payeeName = txtSelectPayee.getText().toString();
        }
        // Category
        if (txtSelectCategory.getTag() != null) {
            searchParameters.category = (CategorySub) txtSelectCategory.getTag();
        }
        // Transaction number
        if (!TextUtils.isEmpty(txtTransNumber.getText())) {
            searchParameters.transactionNumber = txtTransNumber.getText().toString();
        }
        // Notes
        if (!TextUtils.isEmpty(txtNotes.getText())) {
            searchParameters.notes = txtNotes.getText().toString();
        }

        return searchParameters;
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
        displaySearchCriteria(getView());
    }

    private void displaySearchCriteria(View view) {
        if (view == null) return;

        SearchParameters searchParameters = getSearchParameters();

        // Account
        this.spinAccount.setSelection(0);

        // Transaction Type
        cbxDeposit.setChecked(searchParameters.deposit);
        cbxTransfer.setChecked(searchParameters.transfer);
        cbxWithdrawal.setChecked(searchParameters.withdrawal);

        // Status
        this.spinStatus.setSelection(0);

        // Amount from
        if (searchParameters.amountFrom != null) {
            FormatUtilities format = new FormatUtilities(getActivity());
            String displayAmount = format.formatWithLocale(searchParameters.amountFrom);
            txtFromAmount.setText(displayAmount);

            txtFromAmount.setTag(searchParameters.amountFrom.toString());
        } else {
            txtFromAmount.setText("");
            txtFromAmount.setTag(null);
        }

        // Amount to
        if (searchParameters.amountTo != null) {
            FormatUtilities format = new FormatUtilities(getActivity());
            String displayAmount = format.formatWithLocale(searchParameters.amountTo);
            txtToAmount.setText(displayAmount);

            txtToAmount.setTag(searchParameters.amountTo.toString());
        } else {
            txtToAmount.setText("");
            txtToAmount.setTag(null);
        }

        // Date from
        if (searchParameters.dateFrom == null) {
            txtDateFrom.setTag(null);
        }
        else {
            txtDateFrom.setTag(MyDateTimeUtils.getIsoStringFrom(searchParameters.dateFrom));
        }
        txtDateFrom.setText(MyDateTimeUtils.getUserStringFromDateTime(getContext(), searchParameters.dateFrom));
        // Date to
        txtDateTo.setTag(MyDateTimeUtils.getIsoStringFrom(searchParameters.dateTo));
        txtDateTo.setText(MyDateTimeUtils.getUserStringFromDateTime(getContext(), searchParameters.dateTo));

        // Payee
        txtSelectPayee.setTag(searchParameters.payeeId);
        txtSelectPayee.setText(searchParameters.payeeName);
        // Category
        displayCategory(searchParameters.category);
        // Transaction number
        txtTransNumber.setText(searchParameters.transactionNumber);
        // Notes
        txtNotes.setText(searchParameters.notes);
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

                AmountInputDialog dialog = AmountInputDialog.getInstance(v.getId(), amount);
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

    private void initializeResetButton(View view) {
        // Reset button.
        Button resetButton = (Button) view.findViewById(R.id.resetButton);
        resetButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setSearchParameters(new SearchParameters());
                displaySearchCriteria();
            }
        });
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

        txtDateFrom = (TextView) view.findViewById(R.id.textViewFromDate);
        txtDateTo = (TextView) view.findViewById(R.id.textViewToDate);

        // transaction number
        txtTransNumber = (EditText) view.findViewById(R.id.editTextTransNumber);
        // notes
        txtNotes = (EditText) view.findViewById(R.id.editTextNotes);
    }
}
