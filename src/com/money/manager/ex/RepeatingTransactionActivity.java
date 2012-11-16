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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.DatePickerDialog;
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
import com.money.manager.ex.database.TableBillsDeposits;
import com.money.manager.ex.database.TableCategory;
import com.money.manager.ex.database.TableCheckingAccount;
import com.money.manager.ex.database.TablePayee;
import com.money.manager.ex.database.TableSubCategory;
import com.money.manager.ex.fragment.BaseFragmentActivity;
/**
 * 
 * @author Alessandro Lazzari (lazzari.ale@gmail.com)
 */
public class RepeatingTransactionActivity extends BaseFragmentActivity {
	private static final String LOGCAT = RepeatingTransactionActivity.class.getSimpleName();
	// ID REQUEST Data
	private static final int REQUEST_PICK_PAYEE = 1;
	private static final int REQUEST_PICK_ACCOUNT = 2;
	private static final int REQUEST_PICK_CATEGORY = 3;
	// Intent action
	public static final String INTENT_ACTION_EDIT = "android.intent.action.EDIT";
	public static final String INTENT_ACTION_INSERT = "android.intent.action.INSERT";
	// KEY INTENT per il passaggio dei dati
	public static final String KEY_BILL_DEPOSITS_ID = "RepeatingTransaction:BillDepositsId";
	public static final String KEY_ACCOUNT_ID = "RepeatingTransaction:AccountId";
	public static final String KEY_TO_ACCOUNT_ID = "RepeatingTransaction:ToAccountId";
	public static final String KEY_TO_ACCOUNT_NAME = "RepeatingTransaction:ToAccountName";
	public static final String KEY_TRANS_CODE = "RepeatingTransaction:TransCode";
	public static final String KEY_TRANS_STATUS = "RepeatingTransaction:TransStatus";
	public static final String KEY_TRANS_DATE = "RepeatingTransaction:TransDate";
	public static final String KEY_TRANS_AMOUNT = "RepeatingTransaction:TransAmount";
	public static final String KEY_TRANS_TOTAMOUNT = "RepeatingTransaction:TransTotAmount";
	public static final String KEY_PAYEE_ID = "RepeatingTransaction:PayeeId";
	public static final String KEY_PAYEE_NAME = "RepeatingTransaction:PayeeName";
	public static final String KEY_CATEGORY_ID = "RepeatingTransaction:CategoryId";
	public static final String KEY_CATEGORY_NAME = "RepeatingTransaction:CategoryName";
	public static final String KEY_SUBCATEGORY_ID = "RepeatingTransaction:SubCategoryId";
	public static final String KEY_SUBCATEGORY_NAME = "RepeatingTransaction:SubCategoryName";
	public static final String KEY_NOTES = "RepeatingTransaction:Notes";
	public static final String KEY_TRANS_NUMBER = "RepeatingTransaction:TransNumber";
	public static final String KEY_NEXT_OCCURRENCE = "RepeatingTransaction:NextOccurrence";
	public static final String KEY_REPEATS = "RepeatingTransaction:Repeats";
	public static final String KEY_NUM_OCCURRENCE = "RepeatingTransaction:NumOccurrence";
	public static final String KEY_ACTION = "RepeatingTransaction:Action";
	// object of the table
	TableBillsDeposits mRepeatingTransaction = new TableBillsDeposits();
	// action type intent
	private String mIntentAction;
	// id account from and id ToAccount
	private int mAccountId = -1, mToAccountId = -1;
	private List<TableAccountList> mAccountList;
	private String mToAccountName;
	private int mBillDepositsId = -1;
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
	// next occurrance
	private String mNextOccurrence = "";
	private int mFrequencies = 0;
	private int mNumOccurrence = -1;
	// transaction numbers
	private String mTransNumber = "";
	// application
	private MoneyManagerApplication mApplication;
	// reference view into layout
	private LinearLayout linearPayee, linearToAccount;
	private Spinner spinAccount, spinTransCode, spinStatus, spinFrequencies;
	private Button btnSelectPayee, btnSelectToAccount, btnSelectCategory, btnTransNumber, btnNextOccurrence,  
				   btnCancel, btnOk;
	private EditText edtTotAmount, edtAmount, edtTransNumber, edtNotes, edtNextOccurrence, edtTimesRepeated;
	private TextView txtPayee, txtAmount, txtRepeats, txtTimesRepeated;

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
				mCategoryId = curPayee.getInt(curPayee.getColumnIndex(TablePayee.CATEGID));
				mSubCategoryId = curPayee.getInt(curPayee.getColumnIndex(TablePayee.SUBCATEGID));
				// create instance of query
				QueryCategorySubCategory category = new QueryCategorySubCategory(this);
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
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch(requestCode) {
		case REQUEST_PICK_PAYEE:
			if ((resultCode == Activity.RESULT_OK) && (data != null)) {
				mPayeeId = data.getIntExtra(PayeeActivity.INTENT_RESULT_PAYEEID, -1);
				mPayeeName = data.getStringExtra(PayeeActivity.INTENT_RESULT_PAYEENAME);
				// select last category used from payee
				if (getCategoryFromPayee(mPayeeId)) {
					refreshCategoryName(); // refresh UI
				}
				// refresh UI
				refreshPayeeName();
			}
			break;
		case REQUEST_PICK_ACCOUNT:
			if ((resultCode == Activity.RESULT_OK) && (data != null)) {
				mToAccountId = data.getIntExtra(AccountListActivity.INTENT_RESULT_ACCOUNTID, -1);
				mToAccountName = data.getStringExtra(AccountListActivity.INTENT_RESULT_ACCOUNTNAME);
				// refresh UI account name
				refreshAccountName();
			}
			break;
		case REQUEST_PICK_CATEGORY:
			if ((resultCode == Activity.RESULT_OK) && (data != null)) {
				mCategoryId = data.getIntExtra(CategorySubCategoryActivity.INTENT_RESULT_CATEGID, -1);
				mCategoryName = data.getStringExtra(CategorySubCategoryActivity.INTENT_RESULT_CATEGNAME);
				mSubCategoryId = data.getIntExtra(CategorySubCategoryActivity.INTENT_RESULT_SUBCATEGID, -1);
				mSubCategoryName = data.getStringExtra(CategorySubCategoryActivity.INTENT_RESULT_SUBCATEGNAME);
				// refresh UI category
				refreshCategoryName();
			}
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		// take a reference of application
		mApplication = (MoneyManagerApplication)getApplication();
		// manage save instance
		if ((savedInstanceState != null)) {
			mBillDepositsId = savedInstanceState.getInt(KEY_BILL_DEPOSITS_ID);
			mAccountId = savedInstanceState.getInt(KEY_ACCOUNT_ID);
			mToAccountId = savedInstanceState.getInt(KEY_TO_ACCOUNT_ID);
			mToAccountName = savedInstanceState.getString(KEY_TO_ACCOUNT_NAME);
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
			mTransNumber = savedInstanceState.getString(KEY_TRANS_NUMBER);
			mNextOccurrence = savedInstanceState.getString(KEY_NEXT_OCCURRENCE);
			mFrequencies = savedInstanceState.getInt(KEY_REPEATS);
			mNumOccurrence = savedInstanceState.getInt(KEY_NUM_OCCURRENCE);
			// action
			mIntentAction = savedInstanceState.getString(KEY_ACTION);
		}
		// manage intent
		if (getIntent() != null) {
			if (savedInstanceState == null) {
				mAccountId = getIntent().getIntExtra(KEY_ACCOUNT_ID, -1);
				if (getIntent().getAction() != null && getIntent().getAction().equals(Intent.ACTION_EDIT)) {
					mBillDepositsId = getIntent().getIntExtra(KEY_BILL_DEPOSITS_ID, -1);
					// select data transaction
					selectRepeatingTransaction(mBillDepositsId);
				}
			}
			mIntentAction = getIntent().getAction();
		}
		// compose layout
		setContentView(R.layout.repeatingtransaction_activity);
		// take a reference view into layout
		linearPayee = (LinearLayout)findViewById(R.id.linearLayoutPayee);
		linearToAccount = (LinearLayout)findViewById(R.id.linearLayoutToAccount);

		txtPayee = (TextView)findViewById(R.id.textViewPayee);
		txtAmount = (TextView)findViewById(R.id.textViewTotAmount);
		txtRepeats = (TextView)findViewById(R.id.textViewRepeat);
		txtTimesRepeated = (TextView)findViewById(R.id.textViewTimesRepeated);

		btnSelectPayee = (Button)findViewById(R.id.buttonSelectPayee);
		btnSelectToAccount = (Button)findViewById(R.id.buttonSelectToAccount);
		btnSelectCategory = (Button)findViewById(R.id.buttonSelectCategory);

		spinAccount = (Spinner)findViewById(R.id.spinnerAccount);
		spinTransCode = (Spinner)findViewById(R.id.spinnerTransCode);
		spinStatus = (Spinner)findViewById(R.id.spinnerStatus);
		spinFrequencies =  (Spinner)findViewById(R.id.spinnerFrequencies);

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
					refreshTransCode();
				}

               @Override
               public void onNothingSelected(AdapterView<?> parent) {       	   
               }
		});

		btnSelectToAccount.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(RepeatingTransactionActivity.this, AccountListActivity.class);
				intent.setAction(Intent.ACTION_PICK);
				startActivityForResult(intent, REQUEST_PICK_ACCOUNT);
			}
		});
		btnSelectPayee.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(RepeatingTransactionActivity.this, PayeeActivity.class);
				intent.setAction(Intent.ACTION_PICK);
				startActivityForResult(intent, REQUEST_PICK_PAYEE);
			}
		});
		btnSelectCategory.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(RepeatingTransactionActivity.this, CategorySubCategoryActivity.class);
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

		btnOk = (Button)findViewById(R.id.buttonOk);
		btnOk.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (updateData() == true) {
					// set result ok, send broadcast to update widgets and finish activity
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
		// transaction number
		edtTransNumber = (EditText)findViewById(R.id.editTextTransNumber);
		if (!TextUtils.isEmpty(mTransNumber)) {
			edtTransNumber.setText(mTransNumber);
		}
		btnTransNumber = (Button)findViewById(R.id.buttonTransNumber);
		btnTransNumber.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				MoneyManagerOpenHelper helper = new MoneyManagerOpenHelper(RepeatingTransactionActivity.this);
				String query = "SELECT MAX(" + TableCheckingAccount.TRANSACTIONNUMBER + ") FROM " + 
								new TableCheckingAccount().getSource() + " WHERE " + 
								TableCheckingAccount.ACCOUNTID + "=?";
				Cursor cursor = helper.getReadableDatabase().rawQuery(query, new String[] {Integer.toString(mAccountId)});
				if (cursor != null && cursor.moveToFirst()) {
					String transNumber = cursor.getString(0);
					if (TextUtils.isEmpty(transNumber)) {
						transNumber = "0";
					}
					if ((!TextUtils.isEmpty(transNumber)) && TextUtils.isDigitsOnly(transNumber)) {
						edtTransNumber.setText(Integer.toString(Integer.parseInt(transNumber) + 1));
					}
					cursor.close();
				}
				helper.close();
			}
		});
		// notes		
		edtNotes = (EditText)findViewById(R.id.editTextNotes);
		if (!(TextUtils.isEmpty(mNotes))) {
			edtNotes.setText(mNotes);
		}
		// next occurrence
		edtNextOccurrence = (EditText)findViewById(R.id.editTextNextOccurrence);
		if (!(TextUtils.isEmpty(mNextOccurrence))) {
			try {
				edtNextOccurrence.setText(mApplication.getStringFromDate(new SimpleDateFormat("yyyy-MM-dd").parse(mNextOccurrence)));
			} catch (ParseException e) {
				Log.e(LOGCAT, e.getMessage());
			}
		} else {
			edtNextOccurrence.setText(mApplication.getStringFromDate((Date)Calendar.getInstance().getTime()));
		}
			
		btnNextOccurrence = (Button)findViewById(R.id.buttonNextOccurrence);
		btnNextOccurrence.setOnClickListener(new OnClickListener() {
			private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {

				@Override
				public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
					try {
						Date date = new SimpleDateFormat("yyyy-MM-dd").parse(Integer.toString(year) + "-" + Integer.toString(monthOfYear + 1) + "-" + Integer.toString(dayOfMonth));
						edtNextOccurrence.setText(mApplication.getStringFromDate(date));
					} catch (Exception e) {
						Log.e(LOGCAT, e.getMessage());
					}
					
				}
			};
			@Override
			public void onClick(View v) {
				Calendar date = Calendar.getInstance();
				date.setTime(mApplication.getDateFromString(edtNextOccurrence.getText().toString()));
				DatePickerDialog dialog = new DatePickerDialog(RepeatingTransactionActivity.this, mDateSetListener, date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DATE));
				dialog.show();
			}
		});
		// times repeated
		edtTimesRepeated = (EditText)findViewById(R.id.editTextTimesRepeated);
		if (mNumOccurrence >= 0) {
			edtTimesRepeated.setText(Integer.toString(mNumOccurrence));
		}
		// frequencies
		if (mFrequencies >= 200) { mFrequencies = mFrequencies - 200; } // set auto execute without user acknowlegement
		if (mFrequencies >= 100) { mFrequencies = mFrequencies - 100; } // set auto execute on the next occurrence
		spinFrequencies.setSelection(mFrequencies, true);
		spinFrequencies.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				mFrequencies = position;
				refreshTimesRepeated();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				mFrequencies = 0;
				refreshTimesRepeated();
			}
		});
		// refresh user interface
		refreshTransCode();
		refreshPayeeName();
		refreshAccountName();
		refreshCategoryName();
		refreshTimesRepeated();
	}
	/**
	 * refersh UI control times repeated
	 */
	public void refreshTimesRepeated() {
		edtTimesRepeated.setVisibility(mFrequencies > 0 ? View.VISIBLE : View.GONE);
		txtRepeats.setText((mFrequencies == 11) || (mFrequencies == 12) ? R.string.activates : R.string.repeats);
		txtTimesRepeated.setVisibility(mFrequencies > 0 ? View.VISIBLE : View.GONE);
		txtTimesRepeated.setText(mFrequencies >= 11 ? R.string.activates : R.string.times_repeated);
		edtTimesRepeated.setHint(mFrequencies >= 11 ? R.string.activates : R.string.times_repeated);
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
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		// save the state interface
		outState.putInt(KEY_BILL_DEPOSITS_ID, mBillDepositsId);
		outState.putInt(KEY_ACCOUNT_ID, mAccountId);
		outState.putInt(KEY_TO_ACCOUNT_ID, mToAccountId);
		outState.putString(KEY_TO_ACCOUNT_NAME, mToAccountName);
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
		outState.putString(KEY_TRANS_NUMBER, edtTransNumber.getText().toString());
		outState.putString(KEY_NOTES, edtNotes.getText().toString());
		outState.putString(KEY_NEXT_OCCURRENCE, edtNextOccurrence.getText().toString());
		outState.putInt(KEY_REPEATS, mFrequencies);
		if (!TextUtils.isEmpty(edtTimesRepeated.getText())) {
			outState.putInt(KEY_NUM_OCCURRENCE, Integer.parseInt(edtTimesRepeated.getText().toString()));
		} else {
			outState.putInt(KEY_NUM_OCCURRENCE, -1);
		}
		outState.putString(KEY_ACTION, mIntentAction);
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
	 * this method allows you to search the transaction data
	 * @param billId transaction id
	 * @return true if data selected, false nothing
	 */
	private boolean selectRepeatingTransaction(int billId) {
		Cursor cursor = getContentResolver().query(mRepeatingTransaction.getUri(),
				mRepeatingTransaction.getAllColumns(),
				TableBillsDeposits.BDID + "=?",
				new String[] { Integer.toString(billId) }, null);
		// check if cursor is valid and open
		if ((cursor == null) || (cursor.moveToFirst() == false)) {
			return false;
		}

		// take a data
		mBillDepositsId = cursor.getInt(cursor.getColumnIndex(TableBillsDeposits.BDID));
		mAccountId = cursor.getInt(cursor.getColumnIndex(TableBillsDeposits.ACCOUNTID));
		mToAccountId = cursor.getInt(cursor.getColumnIndex(TableBillsDeposits.TOACCOUNTID));
		mTransCode = cursor.getString(cursor.getColumnIndex(TableBillsDeposits.TRANSCODE));
		mStatus = cursor.getString(cursor.getColumnIndex(TableBillsDeposits.STATUS));
		mAmount = (float) cursor.getDouble(cursor.getColumnIndex(TableBillsDeposits.TRANSAMOUNT));
		mTotAmount = (float) cursor.getDouble(cursor.getColumnIndex(TableBillsDeposits.TOTRANSAMOUNT));
		mPayeeId = cursor.getInt(cursor.getColumnIndex(TableBillsDeposits.PAYEEID));
		mCategoryId = cursor.getInt(cursor.getColumnIndex(TableBillsDeposits.CATEGID));
		mSubCategoryId = cursor.getInt(cursor.getColumnIndex(TableBillsDeposits.SUBCATEGID));
		mTransNumber = cursor.getString(cursor.getColumnIndex(TableBillsDeposits.TRANSACTIONNUMBER));
		mNotes = cursor.getString(cursor.getColumnIndex(TableBillsDeposits.NOTES));
		mNextOccurrence = cursor.getString(cursor.getColumnIndex(TableBillsDeposits.NEXTOCCURRENCEDATE));
		mFrequencies = cursor.getInt(cursor.getColumnIndex(TableBillsDeposits.REPEATS));
		mNumOccurrence = cursor.getInt(cursor.getColumnIndex(TableBillsDeposits.NUMOCCURRENCES));
		
		selectAccountName(mToAccountId);
		selectPayeeName(mPayeeId);
		selectCategSubName(mCategoryId, mSubCategoryId);
		
		return true;
	}
	public void refreshAccountName() {
		// write into text button account name
		btnSelectToAccount.setText(TextUtils.isEmpty(mToAccountName) == false ? mToAccountName : getResources().getString(R.string.select_to_account));
	}
	public void refreshCategoryName() {
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
	/**
	 * update data into database
	 * @return true if update data successful 
	 */
	private boolean updateData() {
		//TODO updatedata
		if (validateData() == false) {
			return false;
		}
		// content value for insert or update data
		ContentValues values = new ContentValues();
		
		values.put(TableBillsDeposits.ACCOUNTID, mAccountId);
		if (mTransCode.equals("Transfer")) {
			values.put(TableBillsDeposits.TOACCOUNTID, mToAccountId);
			values.put(TableBillsDeposits.PAYEEID, -1);
		} else {
			values.put(TableBillsDeposits.PAYEEID, mPayeeId);
		}
		values.put(TableBillsDeposits.TRANSCODE, mTransCode);
		if (TextUtils.isEmpty(edtAmount.getText().toString()) || (!(mTransCode.equalsIgnoreCase("Transfer")))) {
			values.put(TableBillsDeposits.TRANSAMOUNT, Float.valueOf(edtTotAmount.getText().toString()));
		} else {
			values.put(TableBillsDeposits.TRANSAMOUNT, Float.valueOf(edtAmount.getText().toString()));
		}
		values.put(TableBillsDeposits.STATUS, mStatus);
		values.put(TableBillsDeposits.CATEGID, mCategoryId);
		values.put(TableBillsDeposits.SUBCATEGID, mSubCategoryId);
		values.put(TableBillsDeposits.FOLLOWUPID, -1);
		values.put(TableBillsDeposits.TOTRANSAMOUNT, Float.valueOf(edtTotAmount.getText().toString()));
		values.put(TableBillsDeposits.TRANSACTIONNUMBER, edtTransNumber.getText().toString());
		values.put(TableBillsDeposits.NOTES, edtNotes.getText().toString());
		values.put(TableBillsDeposits.NEXTOCCURRENCEDATE, mApplication.getSQLiteStringDate(mApplication.getDateFromString(edtNextOccurrence.getText().toString())));
		values.put(TableBillsDeposits.TRANSDATE, mApplication.getSQLiteStringDate(mApplication.getDateFromString(edtNextOccurrence.getText().toString())));
		values.put(TableBillsDeposits.REPEATS, mFrequencies);
		values.put(TableBillsDeposits.NUMOCCURRENCES, mFrequencies > 0 ? edtTimesRepeated.getText().toString() : null);
		
		// check whether the application should do the update or insert
		if (mIntentAction.equals(INTENT_ACTION_INSERT)) {
			// insert
			if (getContentResolver().insert(mRepeatingTransaction.getUri(), values) == null) {
				Toast.makeText(this, R.string.db_checking_insert_failed, Toast.LENGTH_SHORT).show();
				Log.w(LOGCAT, "Insert new repeating transaction failed!");
				return false;
			}
		} else {
			// update
			if (getContentResolver().update(mRepeatingTransaction.getUri(), values, TableBillsDeposits.BDID + "=?", new String[] {Integer.toString(mBillDepositsId)}) <= 0) {
				Toast.makeText(this, R.string.db_checking_update_failed, Toast.LENGTH_SHORT).show();
				Log.w(LOGCAT, "Update repeating  transaction failed!");
				return false;
			}
		}
		
		return true;
	}
	/**
	 * update UI interface with PayeeName
	 */
	public void refreshPayeeName() {
		// write into text button payee name
		btnSelectPayee.setText(TextUtils.isEmpty(mPayeeName) == false ? mPayeeName : mTextDefaultPayee);
	}
	public void refreshTransCode() {
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
		if (TextUtils.isEmpty(edtNextOccurrence.getText().toString())) {
			Toast.makeText(this, R.string.error_next_occurrence_not_populate, Toast.LENGTH_LONG).show();
			return false;
		}
		return true;
	}
}
