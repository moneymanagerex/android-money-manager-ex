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
package com.money.manager.ex;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.view.MenuItem;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.money.manager.ex.database.MoneyManagerOpenHelper;
import com.money.manager.ex.database.QueryCategorySubCategory;
import com.money.manager.ex.database.TableAccountList;
import com.money.manager.ex.database.TableCategory;
import com.money.manager.ex.database.TableCheckingAccount;
import com.money.manager.ex.database.TablePayee;
import com.money.manager.ex.database.TableSubCategory;
import com.money.manager.ex.fragment.AccountFragment;
import com.money.manager.ex.fragment.BaseFragmentActivity;
/**
 * 
 * @author Alessandro Lazzari (lazzari.ale@gmail.com)
 * @version 1.0.1
 */
public class CheckingAccountActivity extends BaseFragmentActivity {
	private static final String LOGCAT = CheckingAccountActivity.class.getSimpleName();
	// ID REQUEST Data
	private static final int REQUEST_PICK_PAYEE = 1;
	private static final int REQUEST_PICK_ACCOUNT = 2;
	private static final int REQUEST_PICK_CATEGORY = 3;
	// Intent action
	public static final String INTENT_ACTION_EDIT = "android.intent.action.EDIT";
	public static final String INTENT_ACTION_INSERT = "android.intent.action.INSERT";
	// KEY INTENT per il passaggio dei dati
	public static final String KEY_TRANS_ID = "AllDataActivity:TransId";
	public static final String KEY_ACCOUNT_ID = "AllDataActivity:AccountId";
	public static final String KEY_TO_ACCOUNT_ID = "AllDataActivity:ToAccountId";
	public static final String KEY_TO_ACCOUNT_NAME = "AllDataActivity:ToAccountName";
	public static final String KEY_TRANS_CODE = "AllDataActivity:TransCode";
	public static final String KEY_TRANS_STATUS = "AllDataActivity:TransStatus";
	public static final String KEY_TRANS_DATE = "AllDataActivity:TransDate";
	public static final String KEY_TRANS_AMOUNT = "AllDataActivity:TransAmount";
	public static final String KEY_TRANS_TOTAMOUNT = "AllDataActivity:TransTotAmount";
	public static final String KEY_PAYEE_ID = "AllDataActivity:PayeeId";
	public static final String KEY_PAYEE_NAME = "AllDataActivity:PayeeName";
	public static final String KEY_CATEGORY_ID = "AllDataActivity:CategoryId";
	public static final String KEY_CATEGORY_NAME = "AllDataActivity:CategoryName";
	public static final String KEY_SUBCATEGORY_ID = "AllDataActivity:SubCategoryId";
	public static final String KEY_SUBCATEGORY_NAME = "AllDataActivity:SubCategoryName";
	public static final String KEY_NOTES = "AllDataActivity:Notes";
	public static final String KEY_ACTION = "AllDataActivity:Action";
	// object of the table
	TableCheckingAccount mCheckingAccount = new TableCheckingAccount();
	// action type intent
	private String mIntentAction;
	// id account from and id ToAccount
	private int mAccountId = -1, mToAccountId = -1;
	private List<TableAccountList> mAccountList;
	private String mToAccountName;
	private int mTransId = -1;
	private String mTransCode, mStatus;
	// info payee
	private int mPayeeId = -1;
	private String mPayeeName, mTextDefaultPayee;
	// info category and subcategory
	private int mCategoryId = -1, mSubCategoryId = -1;
	private String mCategoryName, mSubCategoryName;
	// arrays to manage transcode and status 
	private String[] mTransCodeItems, mStatusItems;
	private String[] mTransCodeValues, mStatusValues;
	// arrayslist accountname and accountid
	private ArrayList<String> mAccountNameList = new ArrayList<String>();
	private ArrayList<Integer> mAccountIdList = new ArrayList<Integer>();
	// amount
	private float mTotAmount = 0, mAmount = 0;
	// notes
	private String mNotes = "";
	// application
	private MoneyManagerApplication mApplication = new MoneyManagerApplication();
	// datepicker value
	private ArrayList<Integer> mDate = new ArrayList<Integer>(); //YEAR#MONTH#DAY
	private static final int YEAR = 0;
	private static final int MONTH = 1;
	private static final int DAY = 2;
	// reference view into layout
	private LinearLayout linearPayee, linearToAccount;
	private Spinner spinAccount, spinTransCode, spinStatus;
	private Button btnSelectPayee, btnSelectToAccount, btnSelectCategory, btnCancel, btnOk;
	private EditText edtTotAmount, edtAmount, edtNotes;
	private TextView txtPayee, txtAmount;
	private DatePicker dtpDate;	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().setTitle(getResources().getString(R.string.new_edit_transaction));
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		// manage save instance
		if ((savedInstanceState != null)) {
			mTransId = savedInstanceState.getInt(KEY_TRANS_ID);
			mAccountId = savedInstanceState.getInt(KEY_ACCOUNT_ID);
			mToAccountId = savedInstanceState.getInt(KEY_TO_ACCOUNT_ID);
			mToAccountName = savedInstanceState.getString(KEY_TO_ACCOUNT_NAME);
			mDate = savedInstanceState.getIntegerArrayList(KEY_TRANS_DATE);
			mTransCode = savedInstanceState.getString(KEY_TRANS_CODE);
			mStatus = savedInstanceState.getString(KEY_TRANS_STATUS);
			if (TextUtils.isEmpty(savedInstanceState.getString(KEY_TRANS_AMOUNT)) == false) {
				mAmount = savedInstanceState.getFloat(KEY_TRANS_AMOUNT);
			}
			if (TextUtils.isEmpty(savedInstanceState.getString(KEY_TRANS_TOTAMOUNT)) == false) {
				mTotAmount = savedInstanceState.getFloat(KEY_TRANS_TOTAMOUNT);
			}
			mPayeeId = savedInstanceState.getInt(KEY_PAYEE_ID);
			mPayeeName = savedInstanceState.getString(KEY_PAYEE_NAME);
			mCategoryId = savedInstanceState.getInt(KEY_CATEGORY_ID);
			mCategoryName = savedInstanceState.getString(KEY_CATEGORY_NAME);
			mSubCategoryId = savedInstanceState.getInt(KEY_SUBCATEGORY_ID);
			mSubCategoryName = savedInstanceState.getString(KEY_SUBCATEGORY_NAME);
			mNotes = savedInstanceState.getString(KEY_NOTES);
			// action
			mIntentAction = savedInstanceState.getString(KEY_ACTION);
		}
		// manage intent
		if (getIntent() != null) {
			if (savedInstanceState == null) {
				mAccountId = getIntent().getIntExtra(KEY_ACCOUNT_ID, -1);
				if (getIntent().getAction() != null && getIntent().getAction().equals(Intent.ACTION_EDIT)) {
					mTransId = getIntent().getIntExtra(KEY_TRANS_ID, -1);
					// select data transaction
					selectCheckingAccount(mTransId);
				}
			}
			mIntentAction = getIntent().getAction();
		}
		// compose layout
		setContentView(R.layout.checkingaccount_activity);
		// take a reference view into layout
		linearPayee = (LinearLayout)findViewById(R.id.linearLayoutPayee);
		linearToAccount = (LinearLayout)findViewById(R.id.linearLayoutToAccount);

		txtPayee = (TextView)findViewById(R.id.textViewPayee);
		txtAmount = (TextView)findViewById(R.id.textViewTotAmount);

		btnSelectPayee = (Button)findViewById(R.id.buttonSelectPayee);
		btnSelectToAccount = (Button)findViewById(R.id.buttonSelectToAccount);
		btnSelectCategory = (Button)findViewById(R.id.buttonSelectCategory);

		spinAccount = (Spinner)findViewById(R.id.spinnerAccount);
		spinTransCode = (Spinner)findViewById(R.id.spinnerTransCode);
		spinStatus = (Spinner)findViewById(R.id.spinnerStatus);

		edtTotAmount = (EditText)findViewById(R.id.editTextTotAmount);
		if (!(mTotAmount == 0)) {
			edtTotAmount.setText(Float.toString(mTotAmount));
		}
		edtAmount = (EditText)findViewById(R.id.editTextAmount);
		if (!(mAmount == 0)) {
			edtAmount.setText(Float.toString(mAmount));
		}
		
		// accountlist <> to populate the spin
		mAccountList = new MoneyManagerOpenHelper(this).getListAccounts(mApplication.getAccountsOpenVisible(), mApplication.getAccountFavoriteVisible());
		for(int i = 0; i <= mAccountList.size() - 1; i ++) {
			mAccountNameList.add(mAccountList.get(i).getAccountName());
			mAccountIdList.add(mAccountList.get(i).getAccountId());
		}
		// create adapter for spinAccount
		ArrayAdapter<String> adapterAccount = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mAccountNameList);
		adapterAccount.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinAccount.setAdapter(adapterAccount);
		// select current value
		if (mAccountIdList.indexOf(mAccountId) >= 0) {
			spinAccount.setSelection(mAccountIdList.indexOf(mAccountId), true);
		}
		spinAccount.setOnItemSelectedListener(new OnItemSelectedListener() {
	           @Override
	           public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
					if ((position >= 0) && (position <= mAccountIdList.size())) {
						mAccountId = mAccountIdList.get(position);
					}
				}

            @Override
            public void onNothingSelected(AdapterView<?> parent) {       	   
            }
		});
		
		// populate arrays TransCode
		mTransCodeItems = getResources().getStringArray(R.array.transcode_items);
		mTransCodeValues = getResources().getStringArray(R.array.transcode_values);
		// create adapter for TransCode
		ArrayAdapter<String> adapterTrans = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mTransCodeItems);
		adapterTrans.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinTransCode.setAdapter(adapterTrans);
		// select a current value
		if (TextUtils.isEmpty(mTransCode) == false) {
			if (Arrays.asList(mTransCodeValues).indexOf(mTransCode) >= 0) {
				spinTransCode.setSelection(Arrays.asList(mTransCodeValues).indexOf(mTransCode), true);
			}
		} else {
			mTransCode = (String)spinTransCode.getSelectedItem();
		}
		spinTransCode.setOnItemSelectedListener(new OnItemSelectedListener() {
	           @Override
	           public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
					if ((position >= 0) && (position <= mTransCodeValues.length)) {
						mTransCode = mTransCodeValues[position];
					}
					// aggiornamento dell'interfaccia grafica
					updateTransCode();
				}

               @Override
               public void onNothingSelected(AdapterView<?> parent) {       	   
               }
		});

		btnSelectToAccount.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(CheckingAccountActivity.this, AccountListActivity.class);
				intent.setAction(Intent.ACTION_PICK);
				startActivityForResult(intent, REQUEST_PICK_ACCOUNT);
			}
		});
		btnSelectPayee.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(CheckingAccountActivity.this, PayeeActivity.class);
				intent.setAction(Intent.ACTION_PICK);
				startActivityForResult(intent, REQUEST_PICK_PAYEE);
			}
		});
		btnSelectCategory.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(CheckingAccountActivity.this, CategorySubCategoryActivity.class);
				intent.setAction(Intent.ACTION_PICK);
				startActivityForResult(intent, REQUEST_PICK_CATEGORY);
			}
		});
		// arrays to manage Status
		mStatusItems = getResources().getStringArray(R.array.status_items);
		mStatusValues = getResources().getStringArray(R.array.status_values);
		// create adapter for spinnerStatus
		ArrayAdapter<String> adapterStatus = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mStatusItems);
		adapterStatus.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinStatus.setAdapter(adapterStatus);
		// select current value
		if (TextUtils.isEmpty(mStatus) == false) {
			if (Arrays.asList(mStatusValues).indexOf(mStatus) >= 0) {
				spinStatus.setSelection(Arrays.asList(mStatusValues).indexOf(mStatus), true);
			}
		} else {
			mStatus = (String)spinStatus.getSelectedItem();
		}
		spinStatus.setOnItemSelectedListener(new OnItemSelectedListener() {
	           @Override
               public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
					if ((position >= 0) && (position <= mStatusValues.length)) {
						mStatus = mStatusValues[position];
					}
               }

               @Override
               public void onNothingSelected(AdapterView<?> parent) {   
               }
		});
		
		dtpDate = (DatePicker)findViewById(R.id.selectDate);
		// set current data
		if (mDate.isEmpty() == false) {
			dtpDate.updateDate(mDate.get(YEAR), mDate.get(MONTH) - 1, mDate.get(DAY));
		} else {
			getDate();
		}

		btnOk = (Button)findViewById(R.id.buttonOk);
		btnOk.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (updateData() == true) {
					// set result ok and finish
					setResult(RESULT_OK);
					finish();
				}
			}
		});
		
		btnCancel = (Button)findViewById(R.id.buttonCancel);
		btnCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// force refresh UI MainActivity
				MainActivity.setRefreshUserInterface(true);
				// finish CheckingAccountActivity
				setResult(RESULT_CANCELED);
				finish();
			}
		});
		
		edtNotes = (EditText)findViewById(R.id.editTextNotes);
		if (!(TextUtils.isEmpty(mNotes))) {
			edtNotes.setText(mNotes);
		}
		// refresh user interface
		updateTransCode();
		updatePayeeName();
		updateAccountName();
		updateCategoryName();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
	    case android.R.id.home:
	        // close this activity and return to home
	        startActivity(new Intent(this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
	        this.finish();
	        break;
		}
		return false;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch(requestCode) {
		case REQUEST_PICK_PAYEE:
			if ((resultCode == Activity.RESULT_OK) && (data != null)) {
				mPayeeId = data.getIntExtra(PayeeActivity.INTENT_RESULT_PAYEEID, -1);
				mPayeeName = data.getStringExtra(PayeeActivity.INTENT_RESULT_PAYEENAME);
				// select last category used from payee
				if (getCategoryFromPayee(mPayeeId)) {
					updateCategoryName(); // refresh UI
				}
				// refresh UI
				updatePayeeName();
			}
			break;
		case REQUEST_PICK_ACCOUNT:
			if ((resultCode == Activity.RESULT_OK) && (data != null)) {
				mToAccountId = data.getIntExtra(AccountListActivity.INTENT_RESULT_ACCOUNTID, -1);
				mToAccountName = data.getStringExtra(AccountListActivity.INTENT_RESULT_ACCOUNTNAME);
				// refresh UI account name
				updateAccountName();
			}
			break;
		case REQUEST_PICK_CATEGORY:
			if ((resultCode == Activity.RESULT_OK) && (data != null)) {
				mCategoryId = data.getIntExtra(CategorySubCategoryActivity.INTENT_RESULT_CATEGID, -1);
				mCategoryName = data.getStringExtra(CategorySubCategoryActivity.INTENT_RESULT_CATEGNAME);
				mSubCategoryId = data.getIntExtra(CategorySubCategoryActivity.INTENT_RESULT_SUBCATEGID, -1);
				mSubCategoryName = data.getStringExtra(CategorySubCategoryActivity.INTENT_RESULT_SUBCATEGNAME);
				// refresh UI category
				updateCategoryName();
			}
		}
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		// save the date set in datepicker
		this.getDate();
		// save the state interface
		outState.putInt(KEY_TRANS_ID, mTransId);
		outState.putInt(KEY_ACCOUNT_ID, mAccountId);
		outState.putInt(KEY_TO_ACCOUNT_ID, mToAccountId);
		outState.putString(KEY_TO_ACCOUNT_NAME, mToAccountName);
		outState.putIntegerArrayList(KEY_TRANS_DATE, mDate);
		outState.putString(KEY_TRANS_CODE, mTransCode);
		outState.putString(KEY_TRANS_STATUS, mStatus);
		outState.putString(KEY_TRANS_TOTAMOUNT, edtTotAmount.getText().toString());
		outState.putString(KEY_TRANS_AMOUNT, edtAmount.getText().toString());
		outState.putInt(KEY_PAYEE_ID, mPayeeId);
		outState.putString(KEY_PAYEE_NAME, mPayeeName);
		outState.putInt(KEY_CATEGORY_ID, mCategoryId);
		outState.putString(KEY_CATEGORY_NAME, mCategoryName);
		outState.putInt(KEY_SUBCATEGORY_ID, mSubCategoryId);
		outState.putString(KEY_SUBCATEGORY_NAME, mSubCategoryName);
		outState.putString(KEY_NOTES, edtNotes.getText().toString());
		outState.putString(KEY_ACTION, mIntentAction);
	}
	/**
	 * update UI interface with PayeeName
	 */
	public void updatePayeeName() {
		// write into text button payee name
		btnSelectPayee.setText(TextUtils.isEmpty(mPayeeName) == false ? mPayeeName : mTextDefaultPayee);
	}
	
	public void updateAccountName() {
		// write into text button account name
		btnSelectToAccount.setText(TextUtils.isEmpty(mToAccountName) == false ? mToAccountName : getResources().getString(R.string.select_to_account));
	}
	
	public void updateCategoryName() {
		String category = ""; 
		if (TextUtils.isEmpty(mCategoryName) == false) {
			category = mCategoryName + (TextUtils.isEmpty(mSubCategoryName) == false ? " : " + mSubCategoryName : "");
		}
		// write into text button category/subcategory
		if (TextUtils.isEmpty(category) == false)  {
			btnSelectCategory.setText(category);
		} else {
			btnSelectCategory.setText(getResources().getString(R.string.select_category));
		}
	}
	
	public void updateTransCode() {
 	   // check type of transaction
 	   if (mTransCode.equalsIgnoreCase("Transfer")) {
 		  linearPayee.setVisibility(View.GONE);
 		  linearToAccount.setVisibility(View.VISIBLE);
 		  txtAmount.setVisibility(View.VISIBLE);
 		  edtAmount.setVisibility(View.VISIBLE);
 	   } else {
			linearPayee.setVisibility(View.VISIBLE);
			linearToAccount.setVisibility(View.GONE);
			txtAmount.setVisibility(View.GONE);
			edtAmount.setVisibility(View.GONE);
 		  
 		   txtPayee.setText(mTransCode.equals(getString(R.string.withdrawal)) ? R.string.payee : R.string.from);
 		   mTextDefaultPayee = getResources().getString(mTransCode.equals(getString(R.string.withdrawal)) ? R.string.payee : R.string.from);
 	   }
	}
	/**
	 * populate the variabile of date 
	 */
	private String getDate() {
		// clear arraylist
		mDate.clear();
		// populate arraylist with part of date
		mDate.add(YEAR, dtpDate.getYear());
		mDate.add(MONTH, dtpDate.getMonth());
		mDate.add(DAY, dtpDate.getDayOfMonth());
		// return date formatted
		return new SimpleDateFormat("yyyy-MM-dd").format(new Date(dtpDate.getYear() - 1900, dtpDate.getMonth(), dtpDate.getDayOfMonth()));
	}
	private void setDate(int year, int month, int day) {
		// clear arraylist
		mDate.clear();
		// populate arraylist with part of date
		mDate.add(YEAR, year);
		mDate.add(MONTH, month);
		mDate.add(DAY, day);
	}
	/**
	 * getCategoryFromPayee set last category used from payee
	 * @param payeeId Identify of payee
	 * @return true if category set
	 */
	private boolean getCategoryFromPayee(int payeeId) {
		boolean ret = false;
		// take data of payee
		TablePayee payee = new TablePayee();
		Cursor curPayee = getContentResolver().query(payee.getUri(), payee.getAllColumns(), "PAYEEID=" + Integer.toString(payeeId), null, null);
		// check cursor is valid
		if ((curPayee != null) && (curPayee.moveToFirst())) {
			// chek if category is valid
			if (curPayee.getInt(curPayee.getColumnIndex(TablePayee.CATEGID)) != -1) {
				// prendo la categoria e la subcategorie
				mCategoryId = curPayee.getInt(curPayee.getColumnIndex(TablePayee.CATEGID));
				mSubCategoryId = curPayee.getInt(curPayee.getColumnIndex(TablePayee.SUBCATEGID));
				// create instance of query
				QueryCategorySubCategory category = new QueryCategorySubCategory();
				// compose selection
				String where = "CATEGID=" + Integer.toString(mCategoryId) + " AND SUBCATEGID=" + Integer.toString(mSubCategoryId); 
				Cursor curCategory = getContentResolver().query(category.getUri(), category.getAllColumns(), where, null, null);
				// check cursor is valid
				if ((curCategory != null) && (curCategory.moveToFirst())) {
					// take names of category and subcategory
					mCategoryName = curCategory.getString(curCategory.getColumnIndex(QueryCategorySubCategory.CATEGNAME));
					mSubCategoryName = curCategory.getString(curCategory.getColumnIndex(QueryCategorySubCategory.SUBCATEGNAME));
					// return true
					ret = true;
				}
			}
		}

		return ret;
	}
	/**
	 * query info payee
	 * @param payeeId id payee
	 * @return true if the data selected
	 */
	private boolean selectPayeeName(int payeeId) {
		TablePayee payee = new TablePayee();
		Cursor cursor = getContentResolver().query(payee.getUri(),
				payee.getAllColumns(),
				TablePayee.PAYEEID + "=?",
				new String[] { Integer.toString(payeeId) }, null);
		// check if cursor is valid and open
		if ((cursor == null) || (cursor.moveToFirst() == false)) {
			return false;
		}
		
		// set payeename
		mPayeeName = cursor.getString(cursor.getColumnIndex(TablePayee.PAYEENAME));
		
		return true;
	}
	/**
	 * query info payee
	 * @param payeeId id payee
	 * @return true if the data selected
	 */
	private boolean selectAccountName(int accountId) {
		TableAccountList account = new TableAccountList();
		Cursor cursor = getContentResolver().query(account.getUri(),
				account.getAllColumns(),
				TableAccountList.ACCOUNTID + "=?",
				new String[] { Integer.toString(accountId) }, null);
		// check if cursor is valid and open
		if ((cursor == null) || (cursor.moveToFirst() == false)) {
			return false;
		}
		
		// set payeename
		mToAccountName = cursor.getString(cursor.getColumnIndex(TableAccountList.ACCOUNTNAME));
		
		return true;
	}
	/**
	 * Query info of Category and Subcategory
	 * @param categoryId
	 * @param subCategoryId
	 * @return
	 */
	private boolean selectCategSubName(int categoryId, int subCategoryId) {
		TableCategory category = new TableCategory();
		TableSubCategory subCategory  = new TableSubCategory();
		Cursor cursor;
		// category
		cursor = getContentResolver().query(category.getUri(), category.getAllColumns(), TableCategory.CATEGID + "=?", new String[] {Integer.toString(categoryId)}, null);
		if ((cursor != null) && (cursor.moveToFirst())) {
			// set category name and sub category name
			mCategoryName = cursor.getString(cursor.getColumnIndex(TableCategory.CATEGNAME));
		} else {
			mCategoryName =  null;
		}
		// sub-category
		cursor = getContentResolver().query(subCategory.getUri(), subCategory.getAllColumns(), TableSubCategory.SUBCATEGID + "=?", new String[] {Integer.toString(subCategoryId)}, null);
		if ((cursor != null) && (cursor.moveToFirst())) {
			// set category name and sub category name
			mSubCategoryName = cursor.getString(cursor.getColumnIndex(TableSubCategory.SUBCATEGNAME));
		} else {
			mSubCategoryName =  null;
		}
		
		return true;
	}
	/**
	 * this method allows you to search the transaction data
	 * @param transId transaction id
	 * @return true if data selected, false nothing
	 */
	private boolean selectCheckingAccount(int transId) {
		Cursor cursor = getContentResolver().query(mCheckingAccount.getUri(),
				mCheckingAccount.getAllColumns(),
				TableCheckingAccount.TRANSID + "=?",
				new String[] { Integer.toString(transId) }, null);
		// check if cursor is valid and open
		if ((cursor == null) || (cursor.moveToFirst() == false)) {
			return false;
		}
		
		String[] datepart;
		// take a data
		mTransId = cursor.getInt(cursor.getColumnIndex(TableCheckingAccount.TRANSID));
		mAccountId = cursor.getInt(cursor.getColumnIndex(TableCheckingAccount.ACCOUNTID));
		mToAccountId = cursor.getInt(cursor.getColumnIndex(TableCheckingAccount.TOACCOUNTID));
		mTransCode = cursor.getString(cursor.getColumnIndex(TableCheckingAccount.TRANSCODE));
		mStatus = cursor.getString(cursor.getColumnIndex(TableCheckingAccount.STATUS));
		mAmount = (float) cursor.getDouble(cursor.getColumnIndex(TableCheckingAccount.TRANSAMOUNT));
		mTotAmount = (float) cursor.getDouble(cursor.getColumnIndex(TableCheckingAccount.TOTRANSAMOUNT));
		mPayeeId = cursor.getInt(cursor.getColumnIndex(TableCheckingAccount.PAYEEID));
		mCategoryId = cursor.getInt(cursor.getColumnIndex(TableCheckingAccount.CATEGID));
		mSubCategoryId = cursor.getInt(cursor.getColumnIndex(TableCheckingAccount.SUBCATEGID));
		mNotes = cursor.getString(cursor.getColumnIndex(TableCheckingAccount.NOTES));
		datepart = cursor.getString(cursor.getColumnIndex(TableCheckingAccount.TRANSDATE)).split("-");
		// set date
		setDate(Integer.parseInt(datepart[YEAR]), Integer.parseInt(datepart[MONTH]), Integer.parseInt(datepart[DAY]));
		
		selectAccountName(mToAccountId);
		selectPayeeName(mPayeeId);
		selectCategSubName(mCategoryId, mSubCategoryId);
		
		return true;
	}
	/**
	 * validate data insert in activity
	 * @return
	 */
	private boolean validateData() {
		if ((mTransCode.equals("Transfer")) && (mToAccountId == -1)) {
			Toast.makeText(this, R.string.error_toaccount_not_selected, Toast.LENGTH_LONG).show();
			return false;
		} else if ((mTransCode.equals("Transfer") == false) && (mPayeeId == -1)) {
			Toast.makeText(this, R.string.error_payee_not_selected, Toast.LENGTH_LONG).show();
			return false;
		}
		if (mCategoryId == -1) {
			Toast.makeText(this, R.string.error_category_not_selected, Toast.LENGTH_LONG).show();
			return false;
		}
		if (TextUtils.isEmpty(edtTotAmount.getText())) {
			if (TextUtils.isEmpty(edtAmount.getText())) {
				Toast.makeText(this, R.string.error_totamount_empty, Toast.LENGTH_LONG).show();
				return false;
			} else {
				edtTotAmount.setText(edtAmount.getText());
			}
		}
		return true;
	}
	/**
	 * update data into database
	 * @return true if update data successful 
	 */
	private boolean updateData() {
		if (validateData() == false) {
			return false;
		}
		// content value for insert or update data
		ContentValues values = new ContentValues();
		
		values.put(TableCheckingAccount.ACCOUNTID, mAccountId);
		if (mTransCode.equals("Transfer")) {
			values.put(TableCheckingAccount.TOACCOUNTID, mToAccountId);
			values.put(TableCheckingAccount.PAYEEID, -1);
		} else {
			values.put(TableCheckingAccount.PAYEEID, mPayeeId);
		}
		values.put(TableCheckingAccount.TRANSCODE, mTransCode);
		if (TextUtils.isEmpty(edtAmount.getText().toString()) || (!(mTransCode.equalsIgnoreCase("Transfer")))) {
			values.put(TableCheckingAccount.TRANSAMOUNT, Float.valueOf(edtTotAmount.getText().toString()));
		} else {
			values.put(TableCheckingAccount.TRANSAMOUNT, Float.valueOf(edtAmount.getText().toString()));
		}
		values.put(TableCheckingAccount.STATUS, mStatus);
		values.put(TableCheckingAccount.CATEGID, mCategoryId);
		values.put(TableCheckingAccount.SUBCATEGID, mSubCategoryId);
		values.put(TableCheckingAccount.TRANSDATE, this.getDate());
		values.put(TableCheckingAccount.FOLLOWUPID, -1);
		values.put(TableCheckingAccount.TOTRANSAMOUNT, Float.valueOf(edtTotAmount.getText().toString()));
		values.put(TableCheckingAccount.NOTES, edtNotes.getText().toString());
		
		// check whether the application should do the update or insert
		if (mIntentAction.equals(INTENT_ACTION_INSERT)) {
			// insert
			if (getContentResolver().insert(mCheckingAccount.getUri(), values) == null) {
				Toast.makeText(this, R.string.db_checking_insert_failed, Toast.LENGTH_SHORT).show();
				Log.w(LOGCAT, "Insert new transaction failed!");
				return false;
			}
		} else {
			// update
			if (getContentResolver().update(mCheckingAccount.getUri(), values, TableCheckingAccount.TRANSID + "=?", new String[] {Integer.toString(mTransId)}) <= 0) {
				Toast.makeText(this, R.string.db_checking_update_failed, Toast.LENGTH_SHORT).show();
				Log.w(LOGCAT, "Insert new transaction failed!");
				return false;
			}
		}
		// update category and subcategory payee
		if ((!(mTransCode.equalsIgnoreCase("Transfer"))) && (mPayeeId > 0)) {
			// clear content value for update categoryId, subCategoryId 
			values.clear();
			// set categoryId and subCategoryId
			values.put(TablePayee.CATEGID, mCategoryId);
			values.put(TablePayee.SUBCATEGID, mSubCategoryId);
			// create instance TablePayee for update
			TablePayee payee = new TablePayee();
			// update data
			if (getContentResolver().update(payee.getUri(), values, TablePayee.PAYEEID + "=" + Integer.toString(mPayeeId), null) <= 0) {
				Toast.makeText(this, R.string.db_payee_update_failed, Toast.LENGTH_SHORT).show();
				Log.w(LOGCAT, "Update Payee with Id=" + Integer.toString(mPayeeId) + " return <= 0");
			}
		}
		
		return true;
	}
}
