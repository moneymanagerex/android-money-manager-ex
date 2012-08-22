/*******************************************************************************
 * Copyright (C) 2012 The Android Money Manager Ex Project
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
 ******************************************************************************/
/**
 * 
 */
package com.android.money.manager.ex;

import java.util.Arrays;

import com.android.money.manager.ex.database.QueryCategorySubCategory;
import com.android.money.manager.ex.database.TableAccountList;
import com.android.money.manager.ex.database.TableCheckingAccount;
import com.android.money.manager.ex.database.TableCurrencyFormats;
import com.android.money.manager.ex.database.TablePayee;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

/**
 * @author Francesco Berton
 * @version 0.1.0
 *
 */
public class AccountListEditActivity extends FragmentActivity {
	// LOGCAT
	private static final String LOGCAT = AccountListEditActivity.class.getSimpleName();
	// ID REQUEST Data
	private static final int REQUEST_PICK_CURRENCY = 1;
	// KEY INTENT for data exchange
	public static final String KEY_INTENT_ACTION = "AccountListEditActivity:IntentAction";
	public static final String KEY_ACCOUNT_ID = "AccountListEditActivity:AccountId";
	public static final String KEY_ACCOUNT_NAME = "AccountListEditActivity:AccountName";
	public static final String KEY_ACCOUNT_TYPE = "AccountListEditActivity:AccountType";
	public static final String KEY_ACCOUNT_NUM = "AccountListEditActivity:AccountNum";
	public static final String KEY_HELD_AT = "AccountListEditActivity:HeldAt";
	public static final String KEY_WEBSITE = "AccountListEditActivity:Website";
	public static final String KEY_CONTACT_INFO = "AccountListEditActivity:ContactInfo";
	public static final String KEY_ACCESS_INFO = "AccountListEditActivity:AccessInfo";
	public static final String KEY_STATUS = "AccountListEditActivity:Status";
	public static final String KEY_INITIAL_BAL = "AccountListEditActivity:InitialBal";
	public static final String KEY_NOTES = "AccountListEditActivity:Notes";
	public static final String KEY_FAVORITE_ACCT = "AccountListEditActivity:FavoriteAcct";
	public static final String KEY_CURRENCY_ID = "AccountListEditActivity:CurrencyId";
	public static final String KEY_CURRENCY_NAME = "AccountListEditActivity:CurrencyName";
	// Action type
	private String mIntentAction; // Add? Edit?
	// Table object instance
	TableAccountList mAccountList = new TableAccountList();
	// Application
	MoneyManagerApplication mApplication = new MoneyManagerApplication();
	// Activity members
	private int mAccountId = -1;
	private String mAccountName;
	private String mAccountType;
	private String mAccountNum;
	private String mHeldAt;
	private String mWebsite;
	private String mContactInfo;
	private String mAccessInfo;
	private String mStatus;
	private String mInitialBal;
	private String mNotes;
	private String mFavoriteAcct;
	private int mCurrencyId = -1;
	private String mCurrencyName;
	// Arrays for spinner items and values
	private String[] mAccountTypeItems;
	private String[] mAccountStatusItems;
	private String[] mAccountTypeValues;
	private String[] mAccountStatusValues;
	// Activity controls
	private EditText edtAccountName;
	private Spinner spinAccountType;
	private EditText edtAccountNumber;
	private EditText edtAccountHeldAt;
	private EditText edtWebsite;
	private EditText edtContact;
	private EditText edtAccessInfo;
	private Spinner spinAccountStatus;
	private EditText edtInitialBalance;
	private EditText edtNotes;
	private CheckBox chkbFavouriteAccount;
	private Button btnOk;
	private Button btnCancel;
	private Button btnSelectCurrency;
	// Methods 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Set ActionBar properties
		getSupportActionBar().setTitle(getResources().getString(R.string.new_edit_account));
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		// Restore saved instance state
		if ((savedInstanceState != null)) {
			mAccountId = savedInstanceState.getInt(KEY_ACCOUNT_ID);
			mAccountName = savedInstanceState.getString(KEY_ACCOUNT_NAME);
			mAccountType = savedInstanceState.getString(KEY_ACCOUNT_TYPE);
			mAccountNum = savedInstanceState.getString(KEY_ACCOUNT_NUM);
			mHeldAt = savedInstanceState.getString(KEY_HELD_AT);
			mWebsite = savedInstanceState.getString(KEY_WEBSITE);
			mContactInfo = savedInstanceState.getString(KEY_CONTACT_INFO);
			mAccessInfo = savedInstanceState.getString(KEY_ACCESS_INFO);
			mStatus = savedInstanceState.getString(KEY_STATUS);
			if (!(TextUtils.isEmpty(savedInstanceState.getString(KEY_INITIAL_BAL)))) {
				mInitialBal = savedInstanceState.getString(KEY_INITIAL_BAL);
			}
			mNotes = savedInstanceState.getString(KEY_NOTES);
			mFavoriteAcct = savedInstanceState.getString(KEY_FAVORITE_ACCT);
			mCurrencyId = savedInstanceState.getInt(KEY_CURRENCY_ID);
			mCurrencyName = savedInstanceState.getString(KEY_CURRENCY_NAME);
		}
		
		// Get Intent extras
		if (getIntent() != null) {
			if (savedInstanceState == null) {
				mIntentAction = getIntent().getAction();
				if (mIntentAction != null && getIntent().getAction().equals(Intent.ACTION_EDIT)) {
					mAccountId = getIntent().getIntExtra(KEY_ACCOUNT_ID, -1);
					// Load account row
					selectAccount(mAccountId);
				}
			}
		}
		
		// Compose layout
		setContentView(R.layout.accountlist_edit_activity);
		
		// Get controls from layout
		edtAccountName = (EditText) findViewById(R.id.editTextAccountName);
		spinAccountType = (Spinner) findViewById(R.id.spinnerAccountType);
		edtAccountNumber = (EditText) findViewById(R.id.editTextAccountNumber);
		edtAccountHeldAt = (EditText) findViewById(R.id.editTextAccountHeldAt);
		edtWebsite = (EditText) findViewById(R.id.editTextWebsite);
		edtContact = (EditText) findViewById(R.id.editTextContact);
		edtAccessInfo = (EditText) findViewById(R.id.editTextAccessInfo);
		spinAccountStatus = (Spinner) findViewById(R.id.spinnerAccountStatus);
		edtInitialBalance = (EditText) findViewById(R.id.editTextInitialBalance);
		edtNotes = (EditText) findViewById(R.id.editTextNotes);
		chkbFavouriteAccount = (CheckBox) findViewById(R.id.checkboxFavouriteAccount);
		btnSelectCurrency = (Button) findViewById(R.id.buttonSelectCurrency);
		
		// Initialize control values
		if (!(TextUtils.isEmpty(mAccountName))) {
			edtAccountName.setText(mAccountName);
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
		if (!(TextUtils.isEmpty(mInitialBal))) {
			edtInitialBalance.setText(mInitialBal);
		}
		if (!(TextUtils.isEmpty(mNotes))) {
			edtNotes.setText(mNotes);
		}
		if (!(TextUtils.isEmpty(mFavoriteAcct))) {
			// TODO should be done better with enumeration for TRUE and FALSE
			chkbFavouriteAccount.setChecked((mFavoriteAcct == "TRUE") ? true : false);
		} else {
			mFavoriteAcct = (chkbFavouriteAccount.isChecked() ? "TRUE" : "FALSE");
		}
		
		// spinAccountType adapters and values
		mAccountTypeItems = getResources().getStringArray(R.array.accounttype_items);
		mAccountTypeValues = getResources().getStringArray(R.array.accounttype_values);
		ArrayAdapter<String> adapterAccountType = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mAccountTypeItems);
		adapterAccountType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinAccountType.setAdapter(adapterAccountType);
		if (TextUtils.isEmpty(mAccountType) == false) {
			if (Arrays.asList(mAccountTypeValues).indexOf(mAccountType) >= 0) {
				spinAccountType.setSelection(Arrays.asList(mAccountTypeValues).indexOf(mAccountType), true);
			}
		} else {
			mAccountType = (String) spinAccountType.getSelectedItem();
		}
		
		// spinAccountStatus adapters and values
		mAccountStatusItems = getResources().getStringArray(R.array.accountstatus_items);
		mAccountStatusValues = getResources().getStringArray(R.array.accountstatus_values);
		ArrayAdapter<String> adapterAccountStatus = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mAccountStatusItems);
		adapterAccountStatus.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinAccountStatus.setAdapter(adapterAccountStatus);
		if (TextUtils.isEmpty(mStatus) == false) {
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
		
		chkbFavouriteAccount.setOnCheckedChangeListener(new OnCheckedChangeListener() {
	           @Override
	           public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
	        	    if ( isChecked ) {
	        	    	mFavoriteAcct = "TRUE";
	        	    }
					else {
						mFavoriteAcct = "FALSE";
					}
				}
		});
		
		btnSelectCurrency.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(AccountListEditActivity.this, CurrencyFormatsListActivity.class);
				intent.setAction(Intent.ACTION_PICK);
				startActivityForResult(intent, REQUEST_PICK_CURRENCY);
			}
		});
		
		btnOk = (Button) findViewById(R.id.buttonOk);
		btnOk.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (updateAccountList()) {
					// If everything is okay, finish the activity
					AccountListEditActivity.this.finish();
				}
			}
		});
		
		btnCancel = (Button)findViewById(R.id.buttonCancel);
		btnCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// force refresh UI MainActivity
				MainActivity.setRefreshUserInterface(true);
				// finish the activity
				AccountListEditActivity.this.finish();
			}
		});
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch(requestCode) {
		case REQUEST_PICK_CURRENCY:
			if ((resultCode == Activity.RESULT_OK) && (data != null)) {
				mCurrencyId = data.getIntExtra(CurrencyFormatsListActivity.INTENT_RESULT_CURRENCYID, -1);
				mCurrencyName = data.getStringExtra(CurrencyFormatsListActivity.INTENT_RESULT_CURRENCYNAME);
				// refresh displayed Currency
				refreshCurrencyName();
			}
		}
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		// Get members values from controls
		validateData(false);
		// Save the state interface
		outState.putInt(KEY_ACCOUNT_ID, mAccountId);
		outState.putString(KEY_ACCOUNT_NAME, mAccountName);
		outState.putString(KEY_ACCOUNT_TYPE, mAccountType);
		outState.putString(KEY_ACCOUNT_NUM, mAccountNum);
		outState.putString(KEY_HELD_AT, mHeldAt);
		outState.putString(KEY_WEBSITE, mWebsite);
		outState.putString(KEY_CONTACT_INFO, mContactInfo);
		outState.putString(KEY_ACCESS_INFO, mAccessInfo);
		outState.putString(KEY_STATUS, mStatus);
		outState.putString(KEY_INITIAL_BAL, mInitialBal);
		outState.putString(KEY_NOTES, mNotes);
		outState.putString(KEY_FAVORITE_ACCT, mFavoriteAcct);
		outState.putInt(KEY_CURRENCY_ID, mCurrencyId);
		outState.putString(KEY_CURRENCY_NAME, mCurrencyName);
	}
	
	/**
	 * validate data entered
	 * @return
	 */
	private boolean validateData(boolean bCheck) {		
		// Getting control values
		mAccountName = edtAccountName.getText().toString();
		mAccountNum = edtAccountNumber.getText().toString();
		mHeldAt = edtAccountHeldAt.getText().toString();
		mWebsite = edtWebsite.getText().toString();
		mContactInfo = edtContact.getText().toString();
		mAccessInfo = edtAccessInfo.getText().toString();
		mInitialBal = edtInitialBalance.getText().toString();
		mNotes = edtNotes.getText().toString();
		mFavoriteAcct = (chkbFavouriteAccount.isChecked() ? "TRUE" : "FALSE");
		
		if (bCheck) {
			if (mCurrencyId == -1) {
				Toast.makeText(this, R.string.error_currency_not_selected, Toast.LENGTH_LONG).show();
				return false;
			}
			if (TextUtils.isEmpty(mInitialBal)) {
				Toast.makeText(this, R.string.error_initialbal_empty, Toast.LENGTH_LONG).show();
				return false;
			}
			if (TextUtils.isEmpty(mAccountName)) {
				Toast.makeText(this, R.string.error_accountname_empty, Toast.LENGTH_LONG).show();
				return false;
			}
			if (TextUtils.isEmpty(mAccountType)) {
				Toast.makeText(this, R.string.error_accounttype_empty, Toast.LENGTH_LONG).show();
				return false;
			}
			if (TextUtils.isEmpty(mStatus)) {
				Toast.makeText(this, R.string.error_status_empty, Toast.LENGTH_LONG).show();
				return false;
			}
		}
		// TODO 
		// Should throw an exception in case favoriteacct is not in {'TRUE', 'FALSE'}
		return true;
	}
	
	/**
	 * update data into database
	 * @return true if update data successful 
	 */
	private boolean updateAccountList() {
		// data validation
		if (!(validateData(true))) {
			return false;
		}
		// content value for insert or update data
		ContentValues values = new ContentValues();
		values.put(TableAccountList.ACCOUNTNAME, mAccountName);
		values.put(TableAccountList.ACCOUNTTYPE, mAccountType);
		values.put(TableAccountList.ACCOUNTNUM, mAccountNum);
		values.put(TableAccountList.STATUS, mStatus);
		values.put(TableAccountList.NOTES, mNotes);
		values.put(TableAccountList.HELDAT, mHeldAt);
		values.put(TableAccountList.WEBSITE, mWebsite);
		values.put(TableAccountList.CONTACTINFO, mContactInfo);
		values.put(TableAccountList.ACCESSINFO, mAccessInfo);
		values.put(TableAccountList.INITIALBAL, Float.valueOf(mInitialBal));
		values.put(TableAccountList.FAVORITEACCT, mFavoriteAcct);
		values.put(TableAccountList.CURRENCYID, mCurrencyId);
		// check whether the application should do the update or insert
		if (mAccountId == -1) {
			// insert
			if (getContentResolver().insert(mAccountList.getUri(), values) == null) {
				Toast.makeText(this, R.string.db_account_insert_failed, Toast.LENGTH_SHORT).show();
				Log.w(LOGCAT, "Error inserting account!");
				return false;
			}
		} else {
			// update
			if (getContentResolver().update(mAccountList.getUri(), values, TableAccountList.ACCOUNTID + "=?", new String[] {Integer.toString(mAccountId)}) <= 0) {
				Toast.makeText(this, R.string.db_account_update_failed, Toast.LENGTH_SHORT).show();
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
	 * @param accountId account id
	 * @return true if data is correctly selected, false if error occurs
	 */
	private boolean selectAccount(int accountId) {
		// Setup the cursor on AccountList
		Cursor cursor = getContentResolver().query(mAccountList.getUri(),
				mAccountList.getAllColumns(),
				TableAccountList.ACCOUNTID + "=?",
				new String[] { Integer.toString(accountId) }, null);
		// Check if cursor is valid and open
		if ((cursor == null) || (!(cursor.moveToFirst()))) {
			return false;
		}
		// Get the data
		mAccountId = cursor.getInt(cursor.getColumnIndex(TableAccountList.ACCOUNTID));
		mAccountName = cursor.getString(cursor.getColumnIndex(TableAccountList.ACCOUNTNAME));
		mAccountType = cursor.getString(cursor.getColumnIndex(TableAccountList.ACCOUNTTYPE));
		mAccountNum = cursor.getString(cursor.getColumnIndex(TableAccountList.ACCOUNTNUM));
		mStatus = cursor.getString(cursor.getColumnIndex(TableAccountList.STATUS));
		mNotes = cursor.getString(cursor.getColumnIndex(TableAccountList.NOTES));
		mHeldAt = cursor.getString(cursor.getColumnIndex(TableAccountList.HELDAT));
		mWebsite = cursor.getString(cursor.getColumnIndex(TableAccountList.WEBSITE));
		mContactInfo = cursor.getString(cursor.getColumnIndex(TableAccountList.CONTACTINFO));
		mAccessInfo = cursor.getString(cursor.getColumnIndex(TableAccountList.ACCESSINFO));
		mInitialBal = mApplication.getBaseNumericFormatted((float) cursor.getDouble(cursor.getColumnIndex(TableAccountList.INITIALBAL)));
		mFavoriteAcct = cursor.getString(cursor.getColumnIndex(TableAccountList.FAVORITEACCT));		
		mCurrencyId = cursor.getInt(cursor.getColumnIndex(TableAccountList.CURRENCYID));
		// TODO Find a better way to format according to system settings 
		// Format Decimal Value 
		mInitialBal = mInitialBal.replace(",", ".");
		// TODO Select currency name: could be improved for better usage of members
		selectCurrencyName(mCurrencyId);
		// return
		return true;
	}
	
	/**
	 * Refresh current currency name on controls
	 * @return
	 */
	public void refreshCurrencyName() {
		// write currency into text button
		if (TextUtils.isEmpty(mCurrencyName) == false)  {
			btnSelectCurrency.setText(mCurrencyName);
		} else {
			btnSelectCurrency.setText(getResources().getString(R.string.select_currency));
		}
	}
	
	/**
	 * Query info for current currency
	 * @param currencyId
	 * @return
	 */
	private boolean selectCurrencyName(int currencyId) {
		TableCurrencyFormats tableCurrencyFormats = new TableCurrencyFormats();
		Cursor cursor = getContentResolver().query(tableCurrencyFormats.getUri(),
				tableCurrencyFormats.getAllColumns(),
				TableCurrencyFormats.CURRENCYID + "=?",
				(new String[] { Integer.toString(currencyId) }) , null);
		// check if cursor is valid and open
		if ((cursor == null) || (cursor.moveToFirst() == false)) {
			return false;
		}
		// set category name
		mCurrencyName = cursor.getString(cursor.getColumnIndex(TableCurrencyFormats.CURRENCYNAME));
		// return
		return true;
	}
	
}
