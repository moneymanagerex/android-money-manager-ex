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
/**
 *
 */
package com.money.manager.ex.account;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.money.manager.ex.Constants;
import com.money.manager.ex.R;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.core.NumericHelper;
import com.money.manager.ex.currency.CurrenciesActivity;
import com.money.manager.ex.currency.CurrencyRepository;
import com.money.manager.ex.currency.CurrencyService;
import com.money.manager.ex.database.AccountRepository;
import com.money.manager.ex.database.TableAccountList;
import com.money.manager.ex.database.TableCurrencyFormats;
import com.money.manager.ex.common.BaseFragmentActivity;
import com.money.manager.ex.common.IInputAmountDialogListener;
import com.money.manager.ex.common.InputAmountDialog;
import com.money.manager.ex.domainmodel.Account;

import java.math.BigDecimal;
import java.util.Arrays;

import info.javaperformance.money.Money;
import info.javaperformance.money.MoneyFactory;

/**
 * @author Francesco Berton
 */
public class AccountEditActivity
        extends BaseFragmentActivity
        implements IInputAmountDialogListener {

    public static final String KEY_ACCOUNT_ID = "AccountEditActivity:AccountId";
    public static final String KEY_ACCOUNT_NAME = "AccountEditActivity:AccountName";
    public static final String KEY_ACCOUNT_TYPE = "AccountEditActivity:AccountType";
    public static final String KEY_ACCOUNT_NUM = "AccountEditActivity:AccountNum";
    public static final String KEY_HELD_AT = "AccountEditActivity:HeldAt";
    public static final String KEY_WEBSITE = "AccountEditActivity:Website";
    public static final String KEY_CONTACT_INFO = "AccountEditActivity:ContactInfo";
    public static final String KEY_ACCESS_INFO = "AccountEditActivity:AccessInfo";
    public static final String KEY_STATUS = "AccountEditActivity:Status";
    public static final String KEY_INITIAL_BAL = "AccountEditActivity:InitialBal";
    public static final String KEY_NOTES = "AccountEditActivity:Notes";
    public static final String KEY_FAVORITE_ACCT = "AccountEditActivity:FavoriteAcct";
    public static final String KEY_CURRENCY_ID = "AccountEditActivity:CurrencyId";
    public static final String KEY_CURRENCY_NAME = "AccountEditActivity:CurrencyName";
    public static final String KEY_SYMBOL = "AccountEditActivity:Symbol";
    // LOGCAT
    private static final String LOGCAT = AccountEditActivity.class.getSimpleName();
    // ID REQUEST Data
    private static final int REQUEST_PICK_CURRENCY = 1;
    private static final String KEY_ACTION = "AccountEditActivity:Action";
    // Constant
    private static final int PLUS = 0;
    private static final int LESS = 1;

    Account mAccount;

    // Action type
    private String mIntentAction = ""; // Insert? Edit?

    // Activity members
//    private int mAccountId = -1;
    // mAccountName
    private String mAccountType, mAccountNum, mHeldAt, mWebsite, mContactInfo, mAccessInfo,
            mStatus, mNotes, mFavoriteAcct, mCurrencyName;
    private Money mInitialBal = MoneyFactory.fromString("0");
    private Integer mCurrencyId = null;
    private String[] mAccountTypeValues;
    private String[] mAccountStatusValues;
    // Activity controls
    private EditText edtAccountName, edtAccountNumber, edtAccountHeldAt, edtWebsite, edtContact, edtAccessInfo, edtNotes;
    ;
    private Spinner spinSymbolInitialBalance;
    private TextView txtSelectCurrency, txtInitialBalance;
    private ImageView imgbFavouriteAccount;

    @Override
    public boolean onActionCancelClick() {
        finish();
        return super.onActionCancelClick();
    }

    @Override
    public boolean onActionDoneClick() {
        if (updateAccountList()) {
            // If everything is okay, finish the activity
            finish();
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAccount = new Account();
        mAccount.setId(Constants.NOT_SET);

        Core core = new Core(getApplicationContext());

        // Restore saved instance state
        if ((savedInstanceState != null)) {
            restoreInstanceState(savedInstanceState);
        }

        // Get Intent extras
        if (getIntent() != null) {
            if (savedInstanceState == null) {
                mIntentAction = getIntent().getAction();
                if (mIntentAction != null && Intent.ACTION_EDIT.equals(getIntent().getAction())) {
                    int accountId = getIntent().getIntExtra(KEY_ACCOUNT_ID, -1);
                    // Load account row
                    selectAccount(accountId);

                    mAccount.setId(accountId);
                }
            }
        }

        // default currency
        if (mCurrencyId == null) {
            CurrencyService currencyService = new CurrencyService(getApplicationContext());
            TableCurrencyFormats baseCurrency = currencyService.getBaseCurrency();

            if (baseCurrency != null) {
                mCurrencyId = baseCurrency.getCurrencyId();
                mCurrencyName = baseCurrency.getCurrencyName();
            }
        }

        // Compose layout
        setContentView(R.layout.accountlist_edit_activity);
        // toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            setToolbarStandardAction(toolbar);
        }

        // Get controls from layout
        edtAccountName = (EditText) findViewById(R.id.editTextAccountName);
        Spinner spinAccountType = (Spinner) findViewById(R.id.spinnerAccountType);
        edtAccountNumber = (EditText) findViewById(R.id.editTextAccountNumber);
        edtAccountHeldAt = (EditText) findViewById(R.id.editTextAccountHeldAt);
        edtWebsite = (EditText) findViewById(R.id.editTextWebsite);
        edtContact = (EditText) findViewById(R.id.editTextContact);
        edtAccessInfo = (EditText) findViewById(R.id.editTextAccessInfo);
        Spinner spinAccountStatus = (Spinner) findViewById(R.id.spinnerAccountStatus);
        spinSymbolInitialBalance = (Spinner) findViewById(R.id.spinnerSymbolInitialBalance);
        txtInitialBalance = (TextView) findViewById(R.id.editTextInitialBalance);
        edtNotes = (EditText) findViewById(R.id.editTextNotes);
        imgbFavouriteAccount = (ImageView) findViewById(R.id.imageViewAccountFav);
        txtSelectCurrency = (TextView) findViewById(R.id.textViewSelectCurrency);

        // Initialize control values
        if (!(TextUtils.isEmpty(mAccount.getName()))) {
            edtAccountName.setText(mAccount.getName());
        }
        if (!(TextUtils.isEmpty(mAccountNum))) {
            edtAccountNumber.setText(mAccountNum);
        }
        if (!(TextUtils.isEmpty(mHeldAt))) {
            edtAccountHeldAt.setText(mHeldAt);
        }
        if (!(TextUtils.isEmpty(mWebsite))) {
            edtWebsite.setText(mWebsite);
        }
        if (!(TextUtils.isEmpty(mContactInfo))) {
            edtContact.setText(mContactInfo);
        }
        if (!(TextUtils.isEmpty(mAccessInfo))) {
            edtAccessInfo.setText(mAccessInfo);
        }

        // Show initial balance.

        ArrayAdapter<String> adapterSymbol = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"+", "-"});
        spinSymbolInitialBalance.setAdapter(adapterSymbol);
        spinSymbolInitialBalance.setSelection(mInitialBal.compareTo(MoneyFactory.fromString("0")) >= 0
                ? PLUS : LESS);

        // always use positive value. The sign is in the spinner.
        mInitialBal = MoneyFactory.fromDouble(Math.abs(mInitialBal.toDouble()));

        core.formatAmountTextView(txtInitialBalance, mInitialBal, mCurrencyId);
        txtInitialBalance.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Money amount = MoneyFactory.fromString(v.getTag().toString());
                InputAmountDialog dialog = InputAmountDialog.getInstance(v.getId(), amount, mCurrencyId);
                dialog.show(getSupportFragmentManager(), dialog.getClass().getSimpleName());
            }
        });

        // Notes

        if (!(TextUtils.isEmpty(mNotes))) {
            edtNotes.setText(mNotes);
        }
        if (TextUtils.isEmpty(mFavoriteAcct)) {
            // TODO should be done better with enumeration for TRUE and FALSE
            mFavoriteAcct = String.valueOf(Boolean.FALSE);
        }
        imgbFavouriteAccount.setBackgroundResource(String.valueOf(Boolean.TRUE).equalsIgnoreCase(mFavoriteAcct)
                ? R.drawable.ic_star : R.drawable.ic_star_outline);
        imgbFavouriteAccount.setTag(mFavoriteAcct);

        // spinAccountType adapters and values
        String[] mAccountTypeItems = getResources().getStringArray(R.array.accounttype_items);
        mAccountTypeValues = getResources().getStringArray(R.array.accounttype_values);
        ArrayAdapter<String> adapterAccountType = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mAccountTypeItems);
        adapterAccountType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinAccountType.setAdapter(adapterAccountType);
        if (!(TextUtils.isEmpty(mAccountType))) {
            if (Arrays.asList(mAccountTypeValues).indexOf(mAccountType) >= 0) {
                spinAccountType.setSelection(Arrays.asList(mAccountTypeValues).indexOf(mAccountType), true);
            }
        } else {
            mAccountType = (String) spinAccountType.getSelectedItem();
        }

        // spinAccountStatus adapters and values
        String[] mAccountStatusItems = getResources().getStringArray(R.array.accountstatus_items);
        mAccountStatusValues = getResources().getStringArray(R.array.accountstatus_values);
        ArrayAdapter<String> adapterAccountStatus = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mAccountStatusItems);
        adapterAccountStatus.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinAccountStatus.setAdapter(adapterAccountStatus);
        if (!(TextUtils.isEmpty(mStatus))) {
            if (Arrays.asList(mAccountStatusValues).indexOf(mStatus) >= 0) {
                spinAccountStatus.setSelection(Arrays.asList(mAccountStatusValues).indexOf(mStatus), true);
            }
        } else {
            mStatus = (String) spinAccountStatus.getSelectedItem();
        }

        // Set up control listeners
        spinAccountType.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if ((position >= 0) && (position <= mAccountTypeValues.length)) {
                    mAccountType = mAccountTypeValues[position];
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spinAccountStatus.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if ((position >= 0) && (position <= mAccountStatusValues.length)) {
                    mStatus = mAccountStatusValues[position];
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        imgbFavouriteAccount.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                String status = (String) v.getTag();
                // check empty string
                if (TextUtils.isEmpty(status))
                    status = String.valueOf(Boolean.FALSE);
                if (String.valueOf(Boolean.TRUE).equalsIgnoreCase(status)) {
                    v.setTag(String.valueOf(Boolean.FALSE));
                } else {
                    v.setTag(String.valueOf(Boolean.TRUE));
                }
                imgbFavouriteAccount.setBackgroundResource(String.valueOf(Boolean.TRUE).equalsIgnoreCase(String.valueOf(v.getTag()))
                        ? R.drawable.ic_star : R.drawable.ic_star_outline);
            }
        });

        txtSelectCurrency.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AccountEditActivity.this, CurrenciesActivity.class);
                intent.setAction(Intent.ACTION_PICK);
                startActivityForResult(intent, REQUEST_PICK_CURRENCY);
            }
        });

        // Refresh data on the other controls
        refreshCurrencyName();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_PICK_CURRENCY:
                if ((resultCode == Activity.RESULT_OK) && (data != null)) {
                    mCurrencyId = data.getIntExtra(CurrenciesActivity.INTENT_RESULT_CURRENCYID, -1);
                    mCurrencyName = data.getStringExtra(CurrenciesActivity.INTENT_RESULT_CURRENCYNAME);
                    // refresh displayed Currency
                    refreshCurrencyName();
                    // refresh amount
//                    NumericHelper numericHelper = new NumericHelper(getApplicationContext());
//                    BigDecimal initialBalance = numericHelper.getNumberFromString(txtInitialBalance.getTag().toString());
                    Money initialBalance = MoneyFactory.fromString(txtInitialBalance.getTag().toString());
                    if (initialBalance != null) {
                        onFinishedInputAmountDialog(R.id.editTextInitialBalance, initialBalance);
                    }
                }
        }
    }

    @Override
    public void onFinishedInputAmountDialog(int id, Money amount) {
        if (amount == null) {
            Log.w(LOGCAT, "Received amount is null.");
            return;
        }

        Core core = new Core(getApplicationContext());

        View view = findViewById(id);
        if (view != null && view instanceof TextView)
            core.formatAmountTextView(((TextView) view), amount, mCurrencyId);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Get members values from controls
        validateData(false);

        // Save the state interface
//        outState.putInt(KEY_ACCOUNT_ID, mAccountId);
        outState.putInt(KEY_ACCOUNT_ID, mAccount.getId());
//        outState.putString(KEY_ACCOUNT_NAME, mAccountName);
        outState.putString(KEY_ACCOUNT_NAME, mAccount.getName());

        outState.putString(KEY_ACCOUNT_TYPE, mAccountType);
        outState.putString(KEY_ACCOUNT_NUM, mAccountNum);
        outState.putString(KEY_HELD_AT, mHeldAt);
        outState.putString(KEY_WEBSITE, mWebsite);
        outState.putString(KEY_CONTACT_INFO, mContactInfo);
        outState.putString(KEY_ACCESS_INFO, mAccessInfo);
        outState.putString(KEY_STATUS, mStatus);
        outState.putString(KEY_INITIAL_BAL, txtInitialBalance.getTag().toString());
        outState.putString(KEY_NOTES, mNotes);
        outState.putString(KEY_FAVORITE_ACCT, String.valueOf(imgbFavouriteAccount.getTag()));
        outState.putInt(KEY_CURRENCY_ID, mCurrencyId != null ? mCurrencyId : -1);
        outState.putString(KEY_CURRENCY_NAME, mCurrencyName);
        outState.putString(KEY_ACTION, mIntentAction);
    }

    /**
     * validate data entered
     *
     * @return A boolean indicating whether the data is valid for saving.
     */
    private boolean validateData(boolean bCheck) {
        // Getting control values
//        mAccountName = edtAccountName.getText().toString();
        mAccount.setName(edtAccountName.getText().toString());
        mAccountNum = edtAccountNumber.getText().toString();
        mHeldAt = edtAccountHeldAt.getText().toString();
        mWebsite = edtWebsite.getText().toString();
        mContactInfo = edtContact.getText().toString();
        mAccessInfo = edtAccessInfo.getText().toString();

        mInitialBal = MoneyFactory.fromString(txtInitialBalance.getTag().toString());
        mNotes = edtNotes.getText().toString();

        if (bCheck) {
            if (mCurrencyId == null) {
                Core.alertDialog(this, R.string.error_currency_not_selected);
                return false;
            }
            if (TextUtils.isEmpty(txtInitialBalance.getText().toString())) {
                Core.alertDialog(this, R.string.error_initialbal_empty);
                return false;
            }
            if (TextUtils.isEmpty(mAccount.getName())) {
                Core.alertDialog(this, R.string.error_accountname_empty);
                return false;
            }
            if (TextUtils.isEmpty(mAccountType)) {
                Core.alertDialog(this, R.string.error_accounttype_empty);
                return false;
            }
            if (TextUtils.isEmpty(mStatus)) {
                Core.alertDialog(this, R.string.error_status_empty);
                return false;
            }
        }
        // TODO: Should throw an exception in case favoriteacct is not in {'TRUE', 'FALSE'}
        return true;
    }

    /**
     * update data into database
     *
     * @return true if update data successful
     */
    private boolean updateAccountList() {
        // data validation
        if (!(validateData(true))) {
            return false;
        }

        // content value for insert or update data
        // todo: replace with mAccount.contentValues. What about Id field?
        ContentValues values = new ContentValues();
        values.put(TableAccountList.ACCOUNTNAME, mAccount.getName());
        values.put(TableAccountList.ACCOUNTTYPE, mAccountType);
        values.put(TableAccountList.ACCOUNTNUM, mAccountNum);
        values.put(TableAccountList.STATUS, mStatus);
        values.put(TableAccountList.NOTES, mNotes);
        values.put(TableAccountList.HELDAT, mHeldAt);
        values.put(TableAccountList.WEBSITE, mWebsite);
        values.put(TableAccountList.CONTACTINFO, mContactInfo);
        values.put(TableAccountList.ACCESSINFO, mAccessInfo);
        values.put(TableAccountList.INITIALBAL, MoneyFactory.fromString(txtInitialBalance.getTag().toString()).toDouble() *
                (spinSymbolInitialBalance.getSelectedItemPosition() == PLUS ? 1 : -1));
        values.put(TableAccountList.FAVORITEACCT, imgbFavouriteAccount.getTag().toString().toUpperCase());
        values.put(TableAccountList.CURRENCYID, mCurrencyId);

        TableAccountList mAccountList = new TableAccountList();

        // check whether the application should update or insert
        if (Constants.INTENT_ACTION_INSERT.equals(mIntentAction)) {
            // insert
            Uri insertResult = getContentResolver().insert(mAccountList.getUri(), values);
            if (insertResult == null) {
                Core.alertDialog(this, R.string.db_account_insert_failed);
                Log.w(LOGCAT, "Error inserting account!");
                return false;
            }
        } else {
            // update
            int updateCount = getContentResolver().update(mAccountList.getUri(),
                    values,
                    TableAccountList.ACCOUNTID + "=?",
                    new String[]{Integer.toString(mAccount.getId())});
            if (updateCount <= 0) {
                Core.alertDialog(this, R.string.db_account_update_failed);
                Log.w(LOGCAT, "Error updating account!");
                return false;
            }
        }
        // eventually update more tables as side effect
        // TODO (verify if that is the case)
        return true;
    }

    /**
     * Select the account indentified by accountId
     *
     * @param accountId account id
     * @return true if data is correctly selected, false if error occurs
     */
    private boolean selectAccount(int accountId) {
        AccountRepository repository = new AccountRepository(getApplicationContext());
        mAccount = repository.load(accountId);
        if (mAccount == null) return false;

        Account account = mAccount;
//        mAccountId = account.getId();
//        mAccountName = account.getName();
        mAccountType = account.getType();
        mAccountNum = account.getAccountNumber();
        mStatus = account.getStatus();
        mNotes = account.getNotes();
        mHeldAt = account.getHeldAt();
        mWebsite = account.getWebSite();
        mContactInfo = account.getContactInfo();
        mAccessInfo = account.getAccessInfo();
        mInitialBal = account.getInitialBalance();
        mFavoriteAcct = account.getFavourite();
        mCurrencyId = account.getCurrencyId();

        // TODO Select currency name: could be improved for better usage of members
        selectCurrencyName(mCurrencyId);

        return true;
    }

    /**
     * Refresh current currency name on controls
     */
    public void refreshCurrencyName() {
        // write currency into text button
        if (!(TextUtils.isEmpty(mCurrencyName))) {
            txtSelectCurrency.setText(mCurrencyName);
        } else {
            txtSelectCurrency.setText(getResources().getString(R.string.select_currency));
        }
    }

    private void restoreInstanceState(Bundle savedInstanceState) {
        // load into Account model object.
//        mAccountId = savedInstanceState.getInt(KEY_ACCOUNT_ID);
        mAccount.setId(savedInstanceState.getInt(KEY_ACCOUNT_ID));
//        mAccountName = savedInstanceState.getString(KEY_ACCOUNT_NAME);
        mAccount.setName(savedInstanceState.getString(KEY_ACCOUNT_NAME));

        mAccountType = savedInstanceState.getString(KEY_ACCOUNT_TYPE);
        mAccountNum = savedInstanceState.getString(KEY_ACCOUNT_NUM);
        mHeldAt = savedInstanceState.getString(KEY_HELD_AT);
        mWebsite = savedInstanceState.getString(KEY_WEBSITE);
        mContactInfo = savedInstanceState.getString(KEY_CONTACT_INFO);
        mAccessInfo = savedInstanceState.getString(KEY_ACCESS_INFO);
        mStatus = savedInstanceState.getString(KEY_STATUS);
        mInitialBal = MoneyFactory.fromString(savedInstanceState.getString(KEY_INITIAL_BAL));
        if (savedInstanceState.getInt(KEY_SYMBOL) == LESS) {
//            mInitialBal = mInitialBal * -1;
            mInitialBal = mInitialBal.negate();
        }
        mNotes = savedInstanceState.getString(KEY_NOTES);
        mFavoriteAcct = savedInstanceState.getString(KEY_FAVORITE_ACCT);
        mCurrencyId = savedInstanceState.getInt(KEY_CURRENCY_ID);
        if (mCurrencyId == Constants.NOT_SET) {
            mCurrencyId = null;
        }
        // todo: mAccount.setCurrencyId(mCurrencyId);

        mCurrencyName = savedInstanceState.getString(KEY_CURRENCY_NAME);
        mIntentAction = savedInstanceState.getString(KEY_ACTION);
    }

    /**
     * Query info for current currency
     *
     * @param currencyId Id of the currency to select
     * @return A boolean indicating whether the retrieval of currency name was successful.
     */
    private boolean selectCurrencyName(int currencyId) {
        CurrencyRepository repository = new CurrencyRepository(getApplicationContext());
        TableCurrencyFormats currency = repository.loadCurrency(currencyId);
        mCurrencyName = currency.getCurrencyName();

//        TableCurrencyFormats tableCurrencyFormats = new TableCurrencyFormats();
//        Cursor cursor = getContentResolver().query(tableCurrencyFormats.getUri(),
//                tableCurrencyFormats.getAllColumns(),
//                TableCurrencyFormats.CURRENCYID + "=?",
//                (new String[]{Integer.toString(currencyId)}), null);
//        // check if cursor is valid and open
//        if ((cursor == null) || (!(cursor.moveToFirst()))) {
//            return false;
//        }
//        // set category name
//        mCurrencyName = cursor.getString(cursor.getColumnIndex(TableCurrencyFormats.CURRENCYNAME));
//
//        cursor.close();

        return true;
    }
}
