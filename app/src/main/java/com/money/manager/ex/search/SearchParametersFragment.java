/*
 * Copyright (C) 2012-2019 The Android Money Manager Ex Project Team
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

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.codetroopers.betterpickers.calendardatepicker.CalendarDatePickerDialogFragment;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.money.manager.ex.Constants;
import com.money.manager.ex.MmexApplication;
import com.money.manager.ex.PayeeActivity;
import com.money.manager.ex.R;
import com.money.manager.ex.common.Calculator;
import com.money.manager.ex.common.CalculatorActivity;
import com.money.manager.ex.common.CategoryListActivity;
import com.money.manager.ex.core.FormatUtilities;
import com.money.manager.ex.core.RequestCodes;
import com.money.manager.ex.core.UIHelper;
import com.money.manager.ex.currency.CurrencyService;
import com.money.manager.ex.database.QueryAllData;
import com.money.manager.ex.database.WhereStatementGenerator;
import com.money.manager.ex.domainmodel.Account;
import com.money.manager.ex.domainmodel.Currency;
import com.money.manager.ex.domainmodel.SplitCategory;
import com.money.manager.ex.servicelayer.AccountService;
import com.money.manager.ex.settings.AppSettings;
import com.money.manager.ex.settings.LookAndFeelSettings;
import com.money.manager.ex.utils.MmxDate;
import com.money.manager.ex.utils.MmxDateTimeUtils;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dagger.Lazy;
import info.javaperformance.money.Money;
import info.javaperformance.money.MoneyFactory;

/**
 * The form with search parameter input fields.
 */
public class SearchParametersFragment
    extends Fragment {

    private static final String KEY_SEARCH_CRITERIA = "KEY_SEARCH_CRITERIA";
    public static final String DATEPICKER_TAG = "datepicker";

    @Inject Lazy<MmxDateTimeUtils> dateTimeUtilsLazy;

    private SearchParametersViewHolder viewHolder;

    private Spinner spinAccount, spinStatus, spinCurrency;
    private EditText txtNotes;
    private TextView txtSelectCategory;
    private CheckBox cbxWithdrawal, cbxTransfer;
    // arrays list account name and account id
    private ArrayList<String> mAccountNameList = new ArrayList<>();
    private ArrayList<Integer> mAccountIdList = new ArrayList<>();
    private List<Account> mAccountList;
    // currencies
    private ArrayList<String> mCurrencySymbolList = new ArrayList<>();
    private ArrayList<Integer> mCurrencyIdList = new ArrayList<>();
    private List<Currency> mCurrencies;
    // status item and values
    private ArrayList<String> mStatusItems = new ArrayList<>();
    private ArrayList<String> mStatusValues = new ArrayList<>();

    public static SearchParametersFragment createInstance() {
        SearchParametersFragment fragment = new SearchParametersFragment();

        Bundle args = new Bundle();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MmexApplication.getApp().iocComponent.inject(this);

        setHasOptionsMenu(true);

        if (getSearchParameters() == null) {
            setSearchParameters(new SearchParameters());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (container == null) return null;

        View view = inflater.inflate(R.layout.search_parameters_fragment, container, false);

        // bind events
        ButterKnife.bind(this, view);
        // bind controls
        viewHolder = new SearchParametersViewHolder(view);

        initializeUiControlVariables(view);

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
        // Account selector
        ArrayAdapter<String> adapterAccount = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, mAccountNameList);
        adapterAccount.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinAccount.setAdapter(adapterAccount);

        // Currency selector.
        if (mCurrencies == null) {
            CurrencyService currencyService = new CurrencyService(getContext());
            mCurrencies = currencyService.getUsedCurrencies();
            mCurrencies.add(0, null);
            mCurrencySymbolList = new ArrayList<>();
            for (Currency currency : mCurrencies) {
                if (currency != null) {
                    mCurrencySymbolList.add(currency.getCode());
                    mCurrencyIdList.add(currency.getCurrencyId());
                } else {
                    mCurrencySymbolList.add("");
                    mCurrencyIdList.add(Constants.NOT_SET);
                }
            }
        }
        ArrayAdapter<String> currencyAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, mCurrencySymbolList);
        currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinCurrency.setAdapter(currencyAdapter);

        //Payee
        viewHolder.txtSelectPayee.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), PayeeActivity.class);
                intent.setAction(Intent.ACTION_PICK);
                startActivityForResult(intent, RequestCodes.PAYEE);
            }
        });

        //Category
        txtSelectCategory.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), CategoryListActivity.class);
                intent.setAction(Intent.ACTION_PICK);
                startActivityForResult(intent, RequestCodes.CATEGORY);
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
        ArrayAdapter<String> adapterStatus = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, mStatusItems);
        adapterStatus.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinStatus.setAdapter(adapterStatus);

        // Icons
        UIHelper ui = new UIHelper(getContext());
        viewHolder.edtNotes.setCompoundDrawablesRelativeWithIntrinsicBounds(ui.getIcon(GoogleMaterial.Icon.gmd_content_paste), null, null,null);

        // Store search criteria values into the controls.
        displaySearchCriteria(view);

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if ((resultCode == AppCompatActivity.RESULT_CANCELED) || data == null) return;

        SearchParameters searchParameters;
        String stringExtra;

        switch (requestCode) {
            case RequestCodes.PAYEE:
                viewHolder.txtSelectPayee.setTag(data.getIntExtra(PayeeActivity.INTENT_RESULT_PAYEEID, Constants.NOT_SET));
                viewHolder.txtSelectPayee.setText(data.getStringExtra(PayeeActivity.INTENT_RESULT_PAYEENAME));
                break;
            case RequestCodes.CATEGORY:
                //create class for store data
                CategorySub categorySub = new CategorySub();
                categorySub.categId = data.getIntExtra(CategoryListActivity.INTENT_RESULT_CATEGID, Constants.NOT_SET);
                categorySub.categName = data.getStringExtra(CategoryListActivity.INTENT_RESULT_CATEGNAME);
                categorySub.subCategId = data.getIntExtra(CategoryListActivity.INTENT_RESULT_SUBCATEGID, Constants.NOT_SET);
                categorySub.subCategName = data.getStringExtra(CategoryListActivity.INTENT_RESULT_SUBCATEGNAME);
                //update into button
                displayCategory(categorySub);
                break;

            case RequestCodes.AMOUNT_FROM:
                stringExtra = data.getStringExtra(CalculatorActivity.RESULT_AMOUNT);
                searchParameters = getSearchParameters();
                searchParameters.amountFrom = MoneyFactory.fromString(stringExtra);
                setSearchParameters(searchParameters);
                displayAmountFrom();
                break;

            case RequestCodes.AMOUNT_TO:
                stringExtra = data.getStringExtra(CalculatorActivity.RESULT_AMOUNT);
                searchParameters = getSearchParameters();
                searchParameters.amountTo = MoneyFactory.fromString(stringExtra);
                setSearchParameters(searchParameters);
                displayAmountTo();
                break;
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        UIHelper ui = new UIHelper(getActivity());

        // 'Reset' toolbar item
        inflater.inflate(R.menu.menu_clear, menu);
        MenuItem item = menu.findItem(R.id.clearMenuItem);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        item.setIcon(ui.getIcon(GoogleMaterial.Icon.gmd_clear));

        super.onCreateOptionsMenu(menu,inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.clearMenuItem:
                setSearchParameters(new SearchParameters());
                displaySearchCriteria();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /*
        Public
     */

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

    @OnClick(R.id.textViewFromDate)
    void onDateFromClicked() {
        MmxDate currentValue = new MmxDate(getSearchParameters().dateFrom);

        CalendarDatePickerDialogFragment datePicker = new CalendarDatePickerDialogFragment()
                .setFirstDayOfWeek(dateTimeUtilsLazy.get().getFirstDayOfWeek())
                .setOnDateSetListener(new CalendarDatePickerDialogFragment.OnDateSetListener() {
                    @Override
                    public void onDateSet(CalendarDatePickerDialogFragment dialog, int year, int monthOfYear, int dayOfMonth) {
                        MmxDate date = new MmxDate(year, monthOfYear, dayOfMonth);

                        SearchParameters parameters = getSearchParameters();
                        parameters.dateFrom = date.toDate();
                        setSearchParameters(parameters);

                        String displayText = new MmxDateTimeUtils().getUserFormattedDate(getActivity(), date.toDate());
                        viewHolder.txtDateFrom.setText(displayText);
                    }
                })
                .setPreselectedDate(currentValue.getYear(), currentValue.getMonthOfYear(), currentValue.getDayOfMonth());

        if (new UIHelper(getActivity()).isUsingDarkTheme()) {
            datePicker.setThemeDark();
        }
        datePicker.show(getActivity().getSupportFragmentManager(), DATEPICKER_TAG);
    }

    @OnClick(R.id.textViewToDate)
    void onDateToClicked() {
        MmxDate currentValue = new MmxDate(getSearchParameters().dateTo);

        CalendarDatePickerDialogFragment datePicker = new CalendarDatePickerDialogFragment()
                .setFirstDayOfWeek(dateTimeUtilsLazy.get().getFirstDayOfWeek())
                .setOnDateSetListener(new CalendarDatePickerDialogFragment.OnDateSetListener() {
                    @Override
                    public void onDateSet(CalendarDatePickerDialogFragment dialog, int year, int monthOfYear, int dayOfMonth) {
                        MmxDate date = new MmxDate(year, monthOfYear, dayOfMonth);

                        SearchParameters parameters = getSearchParameters();
                        parameters.dateTo = date.toDate();
                        setSearchParameters(parameters);

                        String displayText = new MmxDateTimeUtils().getUserFormattedDate(getActivity(), date.toDate());
                        viewHolder.txtDateTo.setText(displayText);
                    }
                })
                .setPreselectedDate(currentValue.getYear(), currentValue.getMonthOfYear(), currentValue.getDayOfMonth());

        if (new UIHelper(getActivity()).isUsingDarkTheme()) {
            datePicker.setThemeDark();
        }
        datePicker.show(getActivity().getSupportFragmentManager(), DATEPICKER_TAG);
    }

    @OnClick(R.id.textViewFromAmount)
    void onAmountFromClicked() {
        Money amount = getSearchParameters().amountFrom;
        if (amount == null) {
            amount = MoneyFactory.fromDouble(0);
        }

        Calculator.forFragment(this)
                .amount(amount)
                .show(RequestCodes.AMOUNT_FROM);
    }

    @OnClick(R.id.textViewToAmount)
    void onAmountToClicked() {
        Money amount = getSearchParameters().amountTo;
        if (amount == null) {
            amount = MoneyFactory.fromDouble(0);
        }

        Calculator.forFragment(this)
                .amount(amount)
                .show(RequestCodes.AMOUNT_TO);
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

        // currency
        if (searchParameters.currencyId != null && searchParameters.currencyId != Constants.NOT_SET) {
            where.addStatement(QueryAllData.CURRENCYID, "=", searchParameters.currencyId);
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
            where.addStatement(QueryAllData.Date, " >= ",
                    new MmxDate(searchParameters.dateFrom).toIsoDateString());
        }
        // to date
        if (searchParameters.dateTo != null) {
            where.addStatement(QueryAllData.Date, " <= ",
                    new MmxDate(searchParameters.dateTo).toIsoDateString());
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

        // Currency
        if (this.spinCurrency != null) {
            int position = spinCurrency.getSelectedItemPosition();
            if (position != AdapterView.INVALID_POSITION) {
                int currencyId = mCurrencyIdList.get(position);
                if (currencyId != Constants.NOT_SET) {
                    searchParameters.currencyId = currencyId;
                }
            }
        }

        // Transaction Type
        searchParameters.deposit = viewHolder.cbxDeposit.isChecked();
        searchParameters.transfer = cbxTransfer.isChecked();
        searchParameters.withdrawal = cbxWithdrawal.isChecked();

        // Status
        if (spinStatus.getSelectedItemPosition() > 0) {
            searchParameters.status = mStatusValues.get(spinStatus.getSelectedItemPosition());
        }

        // Amount from
        Object tag = viewHolder.txtAmountFrom.getTag();
        if (tag != null) {
            searchParameters.amountFrom = MoneyFactory.fromString((String) tag);
        }
        // Amount to
        tag = viewHolder.txtAmountTo.getTag();
        if (tag != null) {
            searchParameters.amountTo = MoneyFactory.fromString((String) tag);
        }

//        // Date from
//        if (viewHolder.txtDateFrom.getTag() != null) {
//            searchParameters.dateFrom = new MmxDate(viewHolder.txtDateFrom.getTag().toString()).toDate();
//        }
//        // Date to
//        if (viewHolder.txtDateTo.getTag() != null) {
//            String dateString = viewHolder.txtDateTo.getTag().toString();
//            searchParameters.dateTo = new MmxDate(dateString).toDate();
//        }
        // Payee
        if (viewHolder.txtSelectPayee.getTag() != null) {
            searchParameters.payeeId = Integer.parseInt(viewHolder.txtSelectPayee.getTag().toString());
            searchParameters.payeeName = viewHolder.txtSelectPayee.getText().toString();
        }
        // Category
        if (txtSelectCategory.getTag() != null) {
            searchParameters.category = (CategorySub) txtSelectCategory.getTag();
        }
        // Transaction number
        if (!TextUtils.isEmpty(viewHolder.txtTransNumber.getText())) {
            searchParameters.transactionNumber = viewHolder.txtTransNumber.getText().toString();
        }
        // Notes
        if (!TextUtils.isEmpty(txtNotes.getText())) {
            searchParameters.notes = txtNotes.getText().toString();
        }

        return searchParameters;
    }

    private void displayAmountFrom() {
            FormatUtilities format = new FormatUtilities(getActivity());
            String displayAmount = format.formatWithLocale(getSearchParameters().amountFrom);
            viewHolder.txtAmountFrom.setText(displayAmount);
    }

    private void displayAmountTo() {
        FormatUtilities format = new FormatUtilities(getActivity());
        String displayAmount = format.formatWithLocale(getSearchParameters().amountTo);
        viewHolder.txtAmountTo.setText(displayAmount);
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
        this.spinCurrency.setSelection(0);

        // Transaction Type
        viewHolder.cbxDeposit.setChecked(searchParameters.deposit);
        cbxTransfer.setChecked(searchParameters.transfer);
        cbxWithdrawal.setChecked(searchParameters.withdrawal);

        // Status
        this.spinStatus.setSelection(0);

        // Amount from
        if (searchParameters.amountFrom != null) {
            FormatUtilities format = new FormatUtilities(getActivity());
            String displayAmount = format.formatWithLocale(searchParameters.amountFrom);
            viewHolder.txtAmountFrom.setText(displayAmount);

            viewHolder.txtAmountFrom.setTag(searchParameters.amountFrom.toString());
        } else {
            viewHolder.txtAmountFrom.setText("");
            viewHolder.txtAmountFrom.setTag(null);
        }

        // Amount to
        if (searchParameters.amountTo != null) {
            FormatUtilities format = new FormatUtilities(getActivity());
            String displayAmount = format.formatWithLocale(searchParameters.amountTo);
            viewHolder.txtAmountTo.setText(displayAmount);

            viewHolder.txtAmountTo.setTag(searchParameters.amountTo.toString());
        } else {
            viewHolder.txtAmountTo.setText("");
            viewHolder.txtAmountTo.setTag(null);
        }

        // Dates
        viewHolder.txtDateFrom.setText(dateTimeUtilsLazy.get().getUserFormattedDate(getContext(), searchParameters.dateFrom));
        viewHolder.txtDateTo.setText(dateTimeUtilsLazy.get().getUserFormattedDate(getContext(), searchParameters.dateTo));

        // Payee
        viewHolder.txtSelectPayee.setTag(searchParameters.payeeId);
        viewHolder.txtSelectPayee.setText(searchParameters.payeeName);
        // Category
        displayCategory(searchParameters.category);
        // Transaction number
        viewHolder.txtTransNumber.setText(searchParameters.transactionNumber);
        // Notes
        txtNotes.setText(searchParameters.notes);
    }

    private void initializeUiControlVariables(View view) {
        if (view == null) return;

        spinAccount = view.findViewById(R.id.spinnerAccount);
        spinCurrency = view.findViewById(R.id.spinnerCurrency);

        // Transaction Type checkboxes.
        cbxTransfer = view.findViewById(R.id.checkBoxTransfer);
        cbxWithdrawal = view.findViewById(R.id.checkBoxWithdrawal);

        txtSelectCategory = view.findViewById(R.id.textViewSelectCategory);

        spinStatus = view.findViewById(R.id.spinnerStatus);

        // notes
        txtNotes = view.findViewById(R.id.editTextNotes);
    }
}
