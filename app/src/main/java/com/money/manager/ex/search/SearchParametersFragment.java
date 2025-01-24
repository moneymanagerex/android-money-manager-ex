/*
 * Copyright (C) 2012-2018 The Android Money Manager Ex Project Team
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

import android.app.DatePickerDialog;
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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.money.manager.ex.Constants;
import com.money.manager.ex.MmexApplication;
import com.money.manager.ex.nestedcategory.NestedCategoryEntity;
import com.money.manager.ex.nestedcategory.QueryNestedCategory;
import com.money.manager.ex.payee.PayeeActivity;
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
import com.money.manager.ex.servicelayer.AccountService;
import com.money.manager.ex.servicelayer.InfoService;
import com.money.manager.ex.settings.AppSettings;
import com.money.manager.ex.settings.LookAndFeelSettings;
import com.money.manager.ex.tag.TagActivity;
import com.money.manager.ex.utils.MmxDate;
import com.money.manager.ex.utils.MmxDateTimeUtils;
import com.money.manager.ex.utils.TransactionColorUtils;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import dagger.Lazy;
import info.javaperformance.money.Money;
import info.javaperformance.money.MoneyFactory;
import timber.log.Timber;

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
    private CheckBox cbxSearchSubCategory;
    // arrays list account name and account id
    private final ArrayList<String> mAccountNameList = new ArrayList<>();
    private final ArrayList<Long> mAccountIdList = new ArrayList<>();
    private List<Account> mAccountList;
    // currencies
    private ArrayList<String> mCurrencySymbolList = new ArrayList<>();
    private final ArrayList<Long> mCurrencyIdList = new ArrayList<>();
    private List<Currency> mCurrencies;
    // status item and values
    private final ArrayList<String> mStatusItems = new ArrayList<>();
    private final ArrayList<String> mStatusValues = new ArrayList<>();

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
                    mAccountIdList.add(Constants.NOT_SET); // honor -1 as invalid id : issue #1919
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

        //
        viewHolder.txtDateFrom.setOnClickListener(v -> onDateFromClicked());
        viewHolder.txtDateTo.setOnClickListener(v -> onDateToClicked());
        viewHolder.txtAmountFrom.setOnClickListener(v -> onAmountFromClicked());
        viewHolder.txtAmountTo.setOnClickListener(v -> onAmountToClicked());

        //Payee
        viewHolder.txtSelectPayee.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), PayeeActivity.class);
                intent.setAction(Intent.ACTION_PICK);
                startActivityForResult(intent, RequestCodes.PAYEE);
            }
        });

        //tag
        viewHolder.txtSelectTag.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), TagActivity.class);
                intent.setAction(Intent.ACTION_PICK);
                startActivityForResult(intent, RequestCodes.TAG);
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
        viewHolder.edtNotes.setCompoundDrawablesWithIntrinsicBounds(ui.getIcon(GoogleMaterial.Icon.gmd_content_paste), null, null,null);

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
                viewHolder.txtSelectPayee.setTag(data.getLongExtra(PayeeActivity.INTENT_RESULT_PAYEEID, Constants.NOT_SET));
                viewHolder.txtSelectPayee.setText(data.getStringExtra(PayeeActivity.INTENT_RESULT_PAYEENAME));
                break;
            case RequestCodes.TAG:
                viewHolder.txtSelectTag.setTag(data.getLongExtra(TagActivity.INTENT_RESULT_TAGID, Constants.NOT_SET));
                viewHolder.txtSelectTag.setText(data.getStringExtra(TagActivity.INTENT_RESULT_TAGNAME));
                break;
            case RequestCodes.CATEGORY:
                //create class for store data
                CategorySub categorySub = new CategorySub();
                categorySub.categId = data.getLongExtra(CategoryListActivity.INTENT_RESULT_CATEGID, Constants.NOT_SET);
                categorySub.categName = data.getStringExtra(CategoryListActivity.INTENT_RESULT_CATEGNAME);
//                categorySub.subCategId = data.getLongExtra(CategoryListActivity.INTENT_RESULT_SUBCATEGID, Constants.NOT_SET);
//                categorySub.subCategName = data.getStringExtra(CategoryListActivity.INTENT_RESULT_SUBCATEGNAME);
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
        if (item.getItemId() == R.id.clearMenuItem) {
            setSearchParameters(new SearchParameters());
            displaySearchCriteria();
            return true;
        }
        return super.onOptionsItemSelected(item);
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
        Timber.d(this.getClass().getName(),"Where: \n"+where);
        return where;
    }

    public void setSearchParameters(SearchParameters parameters) {
        if (parameters == null) return;

        getArguments().putParcelable(KEY_SEARCH_CRITERIA, Parcels.wrap(parameters));
        displaySearchCriteria();
    }

    private void onDateFromClicked() {
        MmxDate currentValue = new MmxDate(getSearchParameters().dateFrom);

        DatePickerDialog datePicker = new DatePickerDialog(
                getActivity(),
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        MmxDate date = new MmxDate(year, month, dayOfMonth);

                        SearchParameters parameters = getSearchParameters();
                        parameters.dateFrom = date.toDate();
                        setSearchParameters(parameters);

                        String displayText = new MmxDateTimeUtils().getUserFormattedDate(getActivity(), date.toDate());
                        viewHolder.txtDateFrom.setText(displayText);
                    }
                },
                currentValue.getYear(),
                currentValue.getMonthOfYear(),
                currentValue.getDayOfMonth()
        );
        // Optionally, you can customize the DatePickerDialog further if needed
        datePicker.show();
    }

    private void onDateToClicked() {
        MmxDate currentValue = new MmxDate(getSearchParameters().dateTo);

        DatePickerDialog datePicker = new DatePickerDialog(
                getActivity(),
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        MmxDate date = new MmxDate(year, month, dayOfMonth);

                        SearchParameters parameters = getSearchParameters();
                        parameters.dateTo = date.toDate();
                        setSearchParameters(parameters);

                        String displayText = new MmxDateTimeUtils().getUserFormattedDate(getActivity(), date.toDate());
                        viewHolder.txtDateTo.setText(displayText);
                    }
                },
                currentValue.getYear(),
                currentValue.getMonthOfYear(),
                currentValue.getDayOfMonth()
        );
        // Optionally, you can customize the DatePickerDialog further if needed
        datePicker.show();
    }

    private void onAmountFromClicked() {
        Money amount = getSearchParameters().amountFrom;
        if (amount == null) {
            amount = MoneyFactory.fromDouble(0);
        }

        Calculator.forFragment(this).amount(amount).show(RequestCodes.AMOUNT_FROM);
    }

    private void onAmountToClicked() {
        Money amount = getSearchParameters().amountTo;
        if (amount == null) {
            amount = MoneyFactory.fromDouble(0);
        }

        Calculator.forFragment(this).amount(amount).show(RequestCodes.AMOUNT_TO);
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
        if (searchParameters.status != null &&
                !searchParameters.status.equals(SearchParameters.STRING_NULL_VALUE) &&
                !searchParameters.status.isEmpty()) {
            where.addStatement(QueryAllData.STATUS, "=", searchParameters.status);
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
            where.addStatement(QueryAllData.PAYEEID, " = ", searchParameters.payeeId);
        }
        // category

        if (searchParameters.category != null) {
            // Issue 1532 need to check subcategory first
            long categId = searchParameters.category.categId;

            // Category. Also check the splits.
            if (searchParameters.searchSubCategory) {
                // build where also for sub category
                String whereSubCategory = null;
                QueryNestedCategory subQuery = new QueryNestedCategory(this.getActivity());
                List<NestedCategoryEntity> subCat = subQuery.getChildrenNestedCategoryEntities(categId);
                for( NestedCategoryEntity child : subCat) {
                    if (whereSubCategory != null ) {
                        whereSubCategory += ", ";
                    } else {
                        whereSubCategory = "";
                    }
                    whereSubCategory += Long.toString(child.getCategoryId());
                }
                whereSubCategory = "(" + whereSubCategory + ")" ;

                // now using QueryMobileData that have split directly at cateId (uniformed both from transaction or Split)
                where.addStatement(
                        "(" + QueryAllData.CATEGID + " in " + whereSubCategory + ") "
                        );
            } else {
                // now using QueryMobileData that have split directly at cateId (uniformed both from transaction or Split)
                where.addStatement(
                        "(" + QueryAllData.CATEGID + "=" + categId + ") "
                        );

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

        // tag
        if (searchParameters.tagId != null) {
            where.addStatement(QueryAllData.TAGS + " LIKE '%" + searchParameters.tagName +"%'");
        }

        // color
        if (searchParameters.color != -1) {
            where.addStatement(QueryAllData.COLOR, "=", searchParameters.color);
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
            where.addStatement(QueryAllData.AMOUNT, " >= ", searchParameters.amountFrom);
        }
        // to amount
        if (searchParameters.amountTo != null) {
            where.addStatement(QueryAllData.AMOUNT, " <= ", searchParameters.amountTo);
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
            where.addStatement(QueryAllData.AMOUNT, " >= ", lowerAmount);
        }
        // to amount
        if (searchParameters.amountTo != null) {
            where.addStatement(QueryAllData.AMOUNT, " <= ", higherAmount);
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
                long selectedAccountId = mAccountIdList.get(selectedAccountPosition);
                if (selectedAccountId != Constants.NOT_SET) {
                    searchParameters.accountId = selectedAccountId;
                }
            }
        }

        // Currency
        if (this.spinCurrency != null) {
            int position = spinCurrency.getSelectedItemPosition();
            if (position != AdapterView.INVALID_POSITION) {
                long currencyId = mCurrencyIdList.get(position);
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
            searchParameters.payeeId = Long.parseLong(viewHolder.txtSelectPayee.getTag().toString());
            searchParameters.payeeName = viewHolder.txtSelectPayee.getText().toString();
        }
        // tag
        if (viewHolder.txtSelectTag.getTag() != null) {
            searchParameters.tagId = Long.parseLong(viewHolder.txtSelectTag.getTag().toString());
            searchParameters.tagName = viewHolder.txtSelectTag.getText().toString();
        }
        // Category
        if (txtSelectCategory.getTag() != null) {
            searchParameters.category = (CategorySub) txtSelectCategory.getTag();
            searchParameters.searchSubCategory = cbxSearchSubCategory.isChecked();
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
            txtSelectCategory.setText(categorySub.categName);
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
        cbxSearchSubCategory.setEnabled(true);
        cbxSearchSubCategory.setChecked(true);

        // Transaction number
        viewHolder.txtTransNumber.setText(searchParameters.transactionNumber);
        // Notes
        txtNotes.setText(searchParameters.notes);

        // color
        TransactionColorUtils tsc = new TransactionColorUtils(getContext());
        tsc.initColorControls(viewHolder.viewTextColor,
                searchParameters.color,
                color -> {
                    searchParameters.color = color;
                });

    }

    private void initializeUiControlVariables(View view) {
        if (view == null) return;

        spinAccount = view.findViewById(R.id.spinnerAccount);
        spinCurrency = view.findViewById(R.id.spinnerCurrency);

        // Transaction Type checkboxes.
        cbxTransfer = view.findViewById(R.id.checkBoxTransfer);
        cbxWithdrawal = view.findViewById(R.id.checkBoxWithdrawal);

        txtSelectCategory = view.findViewById(R.id.textViewSelectCategory);
        cbxSearchSubCategory = view.findViewById(R.id.checkBoxSearchSubCategory);

        spinStatus = view.findViewById(R.id.spinnerStatus);

        // notes
        txtNotes = view.findViewById(R.id.editTextNotes);
    }
}
