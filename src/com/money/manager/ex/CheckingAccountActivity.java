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
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.money.manager.ex.core.Core;
import com.money.manager.ex.database.MoneyManagerOpenHelper;
import com.money.manager.ex.database.QueryCategorySubCategory;
import com.money.manager.ex.database.TableAccountList;
import com.money.manager.ex.database.TableBillsDeposits;
import com.money.manager.ex.database.TableCategory;
import com.money.manager.ex.database.TableCheckingAccount;
import com.money.manager.ex.database.TablePayee;
import com.money.manager.ex.database.TableSplitTransactions;
import com.money.manager.ex.database.TableSubCategory;
import com.money.manager.ex.fragment.BaseFragmentActivity;
import com.money.manager.ex.fragment.InputAmountDialog;
import com.money.manager.ex.fragment.InputAmountDialog.InputAmountDialogListener;
import com.money.manager.ex.preferences.PreferencesConstant;
/**
 * 
 * @author Alessandro Lazzari (lazzari.ale@gmail.com)
 * @version 1.0.1
 */
public class CheckingAccountActivity extends BaseFragmentActivity implements InputAmountDialogListener {
	public static final String LOGCAT = CheckingAccountActivity.class.getSimpleName();
	// ID REQUEST Data
	public static final int REQUEST_PICK_PAYEE = 1;
	public static final int REQUEST_PICK_ACCOUNT = 2;
	public static final int REQUEST_PICK_CATEGORY = 3;
	public static final int REQUEST_PICK_SPLIT_TRANSACTION = 4;
	// KEY INTENT per il passaggio dei dati
	public static final String KEY_TRANS_ID = "AllDataActivity:TransId";
	public static final String KEY_BDID_ID = "AllDataActivity:bdId";
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
	public static final String KEY_TRANS_NUMBER = "AllDataActivity:TransNumber";
	public static final String KEY_NEXT_OCCURRENCE = "AllDataActivity:NextOccurrence";
	public static final String KEY_SPLIT_TRANSACTION = "AllDataActivity:SplitTransaction";
	public static final String KEY_SPLIT_TRANSACTION_DELETED = "AllDataActivity:SplitTransactionDeleted";
	public static final String KEY_ACTION = "AllDataActivity:Action";
	// object of the table
	TableCheckingAccount mCheckingAccount = new TableCheckingAccount();
	// action type intent
	public String mIntentAction;
	// id account from and id ToAccount
	public int mAccountId = -1, mToAccountId = -1;
	public List<TableAccountList> mAccountList;
	public String mToAccountName;
	public int mTransId = -1;
	public String mTransCode, mStatus;
	// info payee
	public int mPayeeId = -1;
	public String mPayeeName;
	// info category and subcategory
	public int mCategoryId = -1, mSubCategoryId = -1;
	public String mCategoryName, mSubCategoryName;
	// arrays to manage transcode and status 
	public String[] mTransCodeItems, mStatusItems;
	public String[] mTransCodeValues, mStatusValues;
	// arrayslist accountname and accountid
	public ArrayList<String> mAccountNameList = new ArrayList<String>();
	public ArrayList<Integer> mAccountIdList = new ArrayList<Integer>();
	// amount
	public float mTotAmount = 0, mAmount = 0;
	// notes
	public String mNotes = "";
	// transaction numbers
	public String mTransNumber = "";
	// bill deposits
	public int mBdId = -1;
	public String mNextOccurrence = null;
	// application
	public MoneyManagerApplication mApplication;
	// datepicker value
	public String mDate = "";
	// list split transactions
	ArrayList<TableSplitTransactions> mSplitTransaction = null;
	ArrayList<TableSplitTransactions> mSplitTransactionDeleted = null;
	// reference view into layout
	public Spinner spinAccount, spinToAccount, spinTransCode, spinStatus;
	public ImageButton btnTransNumber;
	public EditText  edtTransNumber, edtNotes;
	public CheckBox chbSplitTransaction;
	public TextView txtSelectDate, txtSelectPayee, txtSelectCategory, txtTotAmount, txtAmount;

	/**
	 * getCategoryFromPayee set last category used from payee
	 * @param payeeId Identify of payee
	 * @return true if category set
	 */
	public boolean getCategoryFromPayee(int payeeId) {
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
	
	public ArrayList<TableSplitTransactions> getSplitTransaction(int transId) {
		ArrayList<TableSplitTransactions> listSplitTrans = null;
		
		TableSplitTransactions split = new TableSplitTransactions();
		Cursor curSplit = getContentResolver().query(split.getUri(), null, TableSplitTransactions.TRANSID + "=" + Integer.toString(transId), null, TableSplitTransactions.SPLITTRANSID);
		if (curSplit != null && curSplit.moveToFirst()) {
			listSplitTrans = new ArrayList<TableSplitTransactions>();
			while (!curSplit.isAfterLast()) {
				TableSplitTransactions obj = new TableSplitTransactions();
				obj.setValueFromCursor(curSplit);
				listSplitTrans.add(obj);
				curSplit.moveToNext();
			}
		}
		
		return listSplitTrans;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch(requestCode) {
		case REQUEST_PICK_PAYEE:
			if ((resultCode == Activity.RESULT_OK) && (data != null)) {
				mPayeeId = data.getIntExtra(PayeeActivity.INTENT_RESULT_PAYEEID, -1);
				mPayeeName = data.getStringExtra(PayeeActivity.INTENT_RESULT_PAYEENAME);
				// select last category used from payee
				if (!chbSplitTransaction.isChecked()) {
					if (getCategoryFromPayee(mPayeeId)) {
						refreshCategoryName(); // refresh UI
					}
				}
				// refresh UI
				refreshPayeeName();
			}
			break;
		case REQUEST_PICK_ACCOUNT:
			if ((resultCode == Activity.RESULT_OK) && (data != null)) {
				mToAccountId = data.getIntExtra(AccountListActivity.INTENT_RESULT_ACCOUNTID, -1);
				mToAccountName = data.getStringExtra(AccountListActivity.INTENT_RESULT_ACCOUNTNAME);
			}
			break;
		case REQUEST_PICK_CATEGORY:
			if ((resultCode == Activity.RESULT_OK) && (data != null)) {
				mCategoryId = data.getIntExtra(CategorySubCategoryExpandableListActivity.INTENT_RESULT_CATEGID, -1);
				mCategoryName = data.getStringExtra(CategorySubCategoryExpandableListActivity.INTENT_RESULT_CATEGNAME);
				mSubCategoryId = data.getIntExtra(CategorySubCategoryExpandableListActivity.INTENT_RESULT_SUBCATEGID, -1);
				mSubCategoryName = data.getStringExtra(CategorySubCategoryExpandableListActivity.INTENT_RESULT_SUBCATEGNAME);
				// refresh UI category
				refreshCategoryName();
			}
		case REQUEST_PICK_SPLIT_TRANSACTION:
			if ((resultCode == Activity.RESULT_OK) && (data != null)) {
				mSplitTransaction = data.getParcelableArrayListExtra(SplitTransactionsActivity.INTENT_RESULT_SPLIT_TRANSACTION);
				if (mSplitTransaction != null && mSplitTransaction.size() > 0) {
					float totAmount = 0;
					for (int i = 0; i < mSplitTransaction.size(); i ++) {
						totAmount += mSplitTransaction.get(i).getSplitTransAmount();
					}
					formatAmount(txtTotAmount, totAmount);
				}
				// deleted item
				if (data.getParcelableArrayListExtra(SplitTransactionsActivity.INTENT_RESULT_SPLIT_TRANSACTION_DELETED) != null) {
					mSplitTransactionDeleted = data.getParcelableArrayListExtra(SplitTransactionsActivity.INTENT_RESULT_SPLIT_TRANSACTION_DELETED);
				}
			}
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mApplication = (MoneyManagerApplication)getApplication();
		Core core = new Core(this);
		
		// set dialog mode
		setDialogMode(true);
		
		// manage save instance
		if ((savedInstanceState != null)) {
			mTransId = savedInstanceState.getInt(KEY_TRANS_ID);
			mAccountId = savedInstanceState.getInt(KEY_ACCOUNT_ID);
			mToAccountId = savedInstanceState.getInt(KEY_TO_ACCOUNT_ID);
			mToAccountName = savedInstanceState.getString(KEY_TO_ACCOUNT_NAME);
			mDate = savedInstanceState.getString(KEY_TRANS_DATE);
			mTransCode = savedInstanceState.getString(KEY_TRANS_CODE);
			mStatus = savedInstanceState.getString(KEY_TRANS_STATUS);
			mAmount = savedInstanceState.getFloat(KEY_TRANS_AMOUNT);
			mTotAmount = savedInstanceState.getFloat(KEY_TRANS_TOTAMOUNT);
			mPayeeId = savedInstanceState.getInt(KEY_PAYEE_ID);
			mPayeeName = savedInstanceState.getString(KEY_PAYEE_NAME);
			mCategoryId = savedInstanceState.getInt(KEY_CATEGORY_ID);
			mCategoryName = savedInstanceState.getString(KEY_CATEGORY_NAME);
			mSubCategoryId = savedInstanceState.getInt(KEY_SUBCATEGORY_ID);
			mSubCategoryName = savedInstanceState.getString(KEY_SUBCATEGORY_NAME);
			mNotes = savedInstanceState.getString(KEY_NOTES);
			mTransNumber = savedInstanceState.getString(KEY_TRANS_NUMBER);
			mSplitTransaction = savedInstanceState.getParcelableArrayList(KEY_SPLIT_TRANSACTION);
			mSplitTransactionDeleted = savedInstanceState.getParcelableArrayList(KEY_SPLIT_TRANSACTION_DELETED);
			mBdId = savedInstanceState.getInt(KEY_BDID_ID);
			mNextOccurrence = savedInstanceState.getString(KEY_NEXT_OCCURRENCE);
			// action
			mIntentAction = savedInstanceState.getString(KEY_ACTION);
		}
		// manage intent
		if (getIntent() != null) {
			if (savedInstanceState == null) {
				mAccountId = getIntent().getIntExtra(KEY_ACCOUNT_ID, -1);
				if (getIntent().getAction() != null && Intent.ACTION_EDIT.equals(getIntent().getAction())) {
					mTransId = getIntent().getIntExtra(KEY_TRANS_ID, -1);
					// select data transaction
					getCheckingAccount(mTransId);
				} else {
					if (getIntent().getIntExtra(KEY_BDID_ID, -1) > -1) {
						mBdId = getIntent().getIntExtra(KEY_BDID_ID, -1);
						mNextOccurrence = getIntent().getStringExtra(KEY_NEXT_OCCURRENCE);
						getRepeatingTransaction(mBdId);
					}
				}
			}
			mIntentAction = getIntent().getAction();
			if (Constants.INTENT_ACTION_INSERT.equals(mIntentAction)) {
				mStatus = PreferenceManager.getDefaultSharedPreferences(this).getString(PreferencesConstant.PREF_DEFAULT_STATUS, "");
				if ("L".equals(PreferenceManager.getDefaultSharedPreferences(this).getString(PreferencesConstant.PREF_DEFAULT_PAYEE, "N"))) {
					TablePayee payee = core.getLastPayeeUsed();
					if (payee != null) {
						// get id payee and category
						mPayeeId = payee.getPayeeId();
						mPayeeName = payee.getPayeeName();
						mCategoryId = payee.getCategId();
						mSubCategoryId = payee.getSubCategId();
						// load category and subcategory name
						getCategSubName(mCategoryId, mSubCategoryId);
					}
				}
			}
			// set title
			getSupportActionBar().setTitle(Constants.INTENT_ACTION_INSERT.equals(mIntentAction) ? R.string.new_transaction : R.string.edit_transaction);
		}
		// compose layout
		setContentView(R.layout.checkingaccount_activity);
		// take a reference view into layout
		// account
		spinAccount = (Spinner)findViewById(R.id.spinnerAccount);
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
		
		// account
		spinToAccount = (Spinner)findViewById(R.id.spinnerToAccount);
		adapterAccount.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinToAccount.setAdapter(adapterAccount);
		spinToAccount.setSelection(Spinner.INVALID_POSITION);

		spinToAccount.setOnItemSelectedListener(new OnItemSelectedListener() {
	           @Override
	           public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
					if ((position >= 0) && (position <= mAccountIdList.size())) {
						mToAccountId = mAccountIdList.get(position);
					}
				}

            @Override
            public void onNothingSelected(AdapterView<?> parent) {       	   
            }
		});
		
		// trans-code
		spinTransCode = (Spinner) findViewById(R.id.spinnerTransCode);
		// populate arrays TransCode
		mTransCodeItems = getResources().getStringArray(R.array.transcode_items);
		mTransCodeValues = getResources().getStringArray(R.array.transcode_values);
		// create adapter for TransCode
		ArrayAdapter<String> adapterTrans = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,
				mTransCodeItems);
		adapterTrans.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		
		spinTransCode.setAdapter(adapterTrans);
		// select a current value
		if (TextUtils.isEmpty(mTransCode) == false) {
			if (Arrays.asList(mTransCodeValues).indexOf(mTransCode) >= 0) {
				spinTransCode.setSelection(Arrays.asList(mTransCodeValues).indexOf(mTransCode), true);
			}
		} else {
			mTransCode = (String) spinTransCode.getSelectedItem();
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
		
		// status
		spinStatus = (Spinner)findViewById(R.id.spinnerStatus);
		// arrays to manage Status
		mStatusItems = getResources().getStringArray(R.array.status_items);
		mStatusValues = getResources().getStringArray(R.array.status_values);
		// create adapter for spinnerStatus
		ArrayAdapter<String> adapterStatus = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mStatusItems);
		adapterStatus.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		
		spinStatus.setAdapter(adapterStatus);
		// select current value
		if (!(TextUtils.isEmpty(mStatus))) {
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

		// date
		txtSelectDate = (TextView)findViewById(R.id.textViewDate);
		if (!(TextUtils.isEmpty(mDate))) {
			try {
				txtSelectDate.setTag(new SimpleDateFormat("yyyy-MM-dd").parse(mDate));
			} catch (ParseException e) {
				Log.e(LOGCAT, e.getMessage());
			}
		} else {
			txtSelectDate.setTag((Date)Calendar.getInstance().getTime());
		}
		formatExtendedDate(txtSelectDate);
		
		txtSelectDate.setOnClickListener(new OnClickListener() {
			public DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {

				@Override
				public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
					try {
						Date date = new SimpleDateFormat("yyyy-MM-dd").parse(Integer.toString(year) + "-" + Integer.toString(monthOfYear + 1) + "-" + Integer.toString(dayOfMonth));
						txtSelectDate.setTag(date);
						formatExtendedDate(txtSelectDate);
					} catch (Exception e) {
						Log.e(LOGCAT, e.getMessage());
					}
					
				}
			};
			@Override
			public void onClick(View v) {
				Calendar date = Calendar.getInstance();
				date.setTime((Date)txtSelectDate.getTag());
				DatePickerDialog dialog = new DatePickerDialog(CheckingAccountActivity.this, mDateSetListener, date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DATE));
				dialog.show();
			}
		});
		
		// payee
		txtSelectPayee = (TextView)findViewById(R.id.textViewPayee);
		txtSelectPayee.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(CheckingAccountActivity.this, PayeeActivity.class);
				intent.setAction(Intent.ACTION_PICK);
				startActivityForResult(intent, REQUEST_PICK_PAYEE);
			}
		});
		
		// select category
		txtSelectCategory = (TextView)findViewById(R.id.textViewCategory);
		txtSelectCategory.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!chbSplitTransaction.isChecked()) {
					Intent intent = new Intent(CheckingAccountActivity.this, CategorySubCategoryExpandableListActivity.class);
					intent.setAction(Intent.ACTION_PICK);
					startActivityForResult(intent, REQUEST_PICK_CATEGORY);
				} else {
					Intent intent = new Intent(CheckingAccountActivity.this, SplitTransactionsActivity.class);
					intent.putParcelableArrayListExtra(SplitTransactionsActivity.KEY_SPLIT_TRANSACTION, mSplitTransaction);
					intent.putParcelableArrayListExtra(SplitTransactionsActivity.KEY_SPLIT_TRANSACTION_DELETED, mSplitTransactionDeleted);
					startActivityForResult(intent, REQUEST_PICK_SPLIT_TRANSACTION);
				}
			}
		});
		
		// split transaction
		chbSplitTransaction = (CheckBox)findViewById(R.id.checkBoxSplitTransaction);
		chbSplitTransaction.setChecked(mSplitTransaction != null && mSplitTransaction.size() >= 0);
		chbSplitTransaction.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				CheckingAccountActivity.this.refreshCategoryName();
			}
		});
		
		// amount and tot amount
		// listener on dialog amount edittext
		OnClickListener onClickAmount = new OnClickListener() {

			@Override
			public void onClick(View v) {
				Integer currencyId = null;
				if (spinAccount.getSelectedItemPosition() >= 0
						&& spinAccount.getSelectedItemPosition() < mAccountList.size()) {
					currencyId = mAccountList.get(spinAccount.getSelectedItemPosition()).getCurrencyId();
				}
				float amount = (Float)((TextView) v).getTag();
				InputAmountDialog dialog = InputAmountDialog.getInstance(v.getId(), amount, currencyId);
				dialog.show(getSupportFragmentManager(), dialog.getClass().getSimpleName());
			}
		};
		
		// total amount
		txtTotAmount = (TextView)findViewById(R.id.textViewTotAmount);
		formatAmount(txtTotAmount, mTotAmount);

		// on click open dialog
		txtTotAmount.setOnClickListener(onClickAmount);
		
		// amount
		txtAmount = (TextView)findViewById(R.id.textViewAmount);
		formatAmount(txtAmount, mAmount);

		// on click open dialog
		txtAmount.setOnClickListener(onClickAmount);

		// transaction number
		edtTransNumber = (EditText)findViewById(R.id.editTextTransNumber);
		if (!TextUtils.isEmpty(mTransNumber)) {
			edtTransNumber.setText(mTransNumber);
		}
		btnTransNumber = (ImageButton)findViewById(R.id.buttonTransNumber);
		btnTransNumber.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				MoneyManagerOpenHelper helper = new MoneyManagerOpenHelper(CheckingAccountActivity.this);
				String query = "SELECT MAX(CAST(" + TableCheckingAccount.TRANSACTIONNUMBER + " AS INTEGER)) FROM " + 
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
		
		// refresh user interface
		refreshTransCode();
		refreshPayeeName();
		refreshCategoryName();
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		// save the state interface
		outState.putInt(KEY_TRANS_ID, mTransId);
		outState.putInt(KEY_ACCOUNT_ID, mAccountId);
		outState.putInt(KEY_TO_ACCOUNT_ID, mToAccountId);
		outState.putString(KEY_TO_ACCOUNT_NAME, mToAccountName);
		outState.putString(KEY_TRANS_DATE, new SimpleDateFormat("yyyy-MM-dd").format(txtSelectDate.getTag()));
		outState.putString(KEY_TRANS_CODE, mTransCode);
		outState.putString(KEY_TRANS_STATUS, mStatus);
		outState.putFloat(KEY_TRANS_TOTAMOUNT, (Float)txtTotAmount.getTag());
		outState.putFloat(KEY_TRANS_AMOUNT, (Float)txtAmount.getTag());
		outState.putInt(KEY_PAYEE_ID, mPayeeId);
		outState.putString(KEY_PAYEE_NAME, mPayeeName);
		outState.putInt(KEY_CATEGORY_ID, mCategoryId);
		outState.putString(KEY_CATEGORY_NAME, mCategoryName);
		outState.putInt(KEY_SUBCATEGORY_ID, mSubCategoryId);
		outState.putString(KEY_SUBCATEGORY_NAME, mSubCategoryName);
		outState.putString(KEY_TRANS_NUMBER, edtTransNumber.getText().toString());
		outState.putParcelableArrayList(KEY_SPLIT_TRANSACTION, mSplitTransaction);
		outState.putParcelableArrayList(KEY_SPLIT_TRANSACTION_DELETED, mSplitTransactionDeleted);
		outState.putString(KEY_NOTES, edtNotes.getText().toString());
		// bill deposits
		outState.putInt(KEY_BDID_ID, mBdId);
		outState.putString(KEY_NEXT_OCCURRENCE, mNextOccurrence);
		
		outState.putString(KEY_ACTION, mIntentAction);
	}
	
	@Override
	public void onFinishedInputAmountDialog(int id, Float amount) {
		View view = findViewById(id);
		if (view != null && view instanceof TextView)
			formatAmount(((TextView)view), amount);
	}
	
	@Override
	public boolean onActionCancelClick() {
		finish();
		return super.onActionCancelClick();
	}

	@Override
	public boolean onActionDoneClick() {
		if (updateData()) {
			// set result ok and finish activity
			setResult(RESULT_OK);
			finish();
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * query info payee
	 * @param payeeId id payee
	 * @return true if the data selected
	 */
	public boolean getAccountName(int accountId) {
		TableAccountList account = new TableAccountList();
		Cursor cursor = getContentResolver().query(account.getUri(), account.getAllColumns(), TableAccountList.ACCOUNTID + "=?",
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
	public boolean getCategSubName(int categoryId, int subCategoryId) {
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
	public boolean getCheckingAccount(int transId) {
		Cursor cursor = getContentResolver().query(mCheckingAccount.getUri(),
				mCheckingAccount.getAllColumns(),
				TableCheckingAccount.TRANSID + "=?",
				new String[] { Integer.toString(transId) }, null);
		// check if cursor is valid and open
		if ((cursor == null) || (cursor.moveToFirst() == false)) {
			return false;
		}

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
		mTransNumber = cursor.getString(cursor.getColumnIndex(TableCheckingAccount.TRANSACTIONNUMBER));
		mNotes = cursor.getString(cursor.getColumnIndex(TableCheckingAccount.NOTES));
		mDate = cursor.getString(cursor.getColumnIndex(TableCheckingAccount.TRANSDATE));
		
		if (mSplitTransaction == null) {
			mSplitTransaction = getSplitTransaction(transId);
		}
		
		// convert status in uppercase string
		if (!TextUtils.isEmpty(mStatus))
			mStatus = mStatus.toUpperCase();		
		
		getAccountName(mToAccountId);
		getPayeeName(mPayeeId);
		getCategSubName(mCategoryId, mSubCategoryId);
		
		return true;
	}
	/**
	 * query info payee
	 * @param payeeId id payee
	 * @return true if the data selected
	 */
	public boolean getPayeeName(int payeeId) {
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
	
	public boolean getRepeatingTransaction(int billId) {
		TableBillsDeposits billDeposits = new TableBillsDeposits();
		Cursor cursor = getContentResolver().query(billDeposits.getUri(),
				billDeposits.getAllColumns(),
				TableBillsDeposits.BDID + "=?",
				new String[] { Integer.toString(billId) }, null);
		// check if cursor is valid and open
		if ((cursor == null) || (cursor.moveToFirst() == false)) {
			return false;
		}

		// take a data
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
		mDate = cursor.getString(cursor.getColumnIndex(TableBillsDeposits.NEXTOCCURRENCEDATE));
		
		getAccountName(mToAccountId);
		getPayeeName(mPayeeId);
		getCategSubName(mCategoryId, mSubCategoryId);
		
		return true;
	}
	
	public void formatExtendedDate(TextView dateTextView) {
		try { 
			dateTextView.setText(new SimpleDateFormat("EEEE dd MMMM yyyy").format((Date)dateTextView.getTag()));
		} catch (Exception e){
			Log.e(LOGCAT, e.getMessage());
		}
	}

	public void formatAmount(TextView view, float amount) {
		// take currency id
		Integer currencyId = null;
		if (spinAccount.getSelectedItemPosition() >= 0 && spinAccount.getSelectedItemPosition() < mAccountList.size()) {
			currencyId = mAccountList.get(spinAccount.getSelectedItemPosition()).getCurrencyId();
		}
		
		if (currencyId == null) {
			view.setText(mApplication.getBaseCurrencyFormatted(amount));
		} else {
			view.setText(mApplication.getCurrencyFormatted(currencyId, amount));
		}
		view.setTag(amount);
	}

	public void refreshCategoryName() {
		txtSelectCategory.setText("");
		
		if (!TextUtils.isEmpty(mCategoryName)) {
			txtSelectCategory.setText(mCategoryName);
			if (!TextUtils.isEmpty(mSubCategoryName)) {
				txtSelectCategory.setText(Html.fromHtml(txtSelectCategory.getText() + " : <i>" + mSubCategoryName + "</i>"));
			}
		}
	}
	
	/**
	 * update UI interface with PayeeName
	 */
	public void refreshPayeeName() {
		// write into text button payee name
		txtSelectPayee.setText(mPayeeName);
	}

	public void refreshTransCode() {
		// check type of transaction
		TextView txtFromAccount = (TextView)findViewById(R.id.textViewFromAccount);
		TextView txtToAccount = (TextView)findViewById(R.id.textViewToAccount);
		ViewGroup tableRowPayee = (ViewGroup)findViewById(R.id.tableRowPayee);
		ViewGroup tableRowAmmount = (ViewGroup)findViewById(R.id.tableRowAmount);
		// hide and show
		txtFromAccount.setText(Constants.TRANSACTION_TYPE_TRANSFER.equalsIgnoreCase(mTransCode) ? R.string.from_account : R.string.account);
		txtToAccount.setVisibility(Constants.TRANSACTION_TYPE_TRANSFER.equalsIgnoreCase(mTransCode) ? View.VISIBLE : View.GONE);
		tableRowPayee.setVisibility(!(Constants.TRANSACTION_TYPE_TRANSFER.equalsIgnoreCase(mTransCode)) ? View.VISIBLE : View.GONE);
		tableRowAmmount.setVisibility(Constants.TRANSACTION_TYPE_TRANSFER.equalsIgnoreCase(mTransCode) ? View.VISIBLE : View.GONE);
		spinToAccount.setVisibility(Constants.TRANSACTION_TYPE_TRANSFER.equalsIgnoreCase(mTransCode) ? View.VISIBLE : View.GONE);
	}

	/**
	 * validate data insert in activity
	 * @return
	 */
	public boolean validateData() {
		if ((Constants.TRANSACTION_TYPE_TRANSFER.equalsIgnoreCase(mTransCode)) && (mToAccountId == -1)) {
			Core.alertDialog(this, R.string.error_toaccount_not_selected).show();
			return false;
		} else if ((!Constants.TRANSACTION_TYPE_TRANSFER.equalsIgnoreCase(mTransCode)) && (mPayeeId == -1)) {
			Core.alertDialog(this, R.string.error_payee_not_selected).show();
			return false;
		}
		if (mCategoryId == -1 && (!chbSplitTransaction.isChecked())) {
			Core.alertDialog(this, R.string.error_category_not_selected).show();
			return false;
		}
		if (chbSplitTransaction.isChecked() && (mSplitTransaction == null || mSplitTransaction.size() <= 0)) {
			Core.alertDialog(this, R.string.error_split_transaction_empty).show();
			return false;
		}
		if ((Float)txtTotAmount.getTag() == 0) {
			if ((Float)txtAmount.getTag() == 0) {
				Core.alertDialog(this, R.string.error_totamount_empty).show();
				return false;
			} else {
				txtTotAmount.setTag(txtAmount.getTag());
			}
		}
		return true;
	}

	/**
	 * update data into database
	 * @return true if update data successful 
	 */
	public boolean updateData() {
		if (validateData() == false) {
			return false;
		}
		// content value for insert or update data
		ContentValues values = new ContentValues();
		
		values.put(TableCheckingAccount.ACCOUNTID, mAccountId);
		if (Constants.TRANSACTION_TYPE_TRANSFER.equalsIgnoreCase(mTransCode)) {
			values.put(TableCheckingAccount.TOACCOUNTID, mToAccountId);
			values.put(TableCheckingAccount.PAYEEID, -1);
		} else {
			values.put(TableCheckingAccount.PAYEEID, mPayeeId);
		}
		values.put(TableCheckingAccount.TRANSCODE, mTransCode);
		if (TextUtils.isEmpty(txtAmount.getText().toString()) || (!(Constants.TRANSACTION_TYPE_TRANSFER.equalsIgnoreCase(mTransCode)))) {
			values.put(TableCheckingAccount.TRANSAMOUNT, (Float)txtTotAmount.getTag());
		} else {
			values.put(TableCheckingAccount.TRANSAMOUNT, (Float)txtAmount.getTag());
		}
		values.put(TableCheckingAccount.STATUS, mStatus);
		values.put(TableCheckingAccount.CATEGID, !chbSplitTransaction.isChecked() ? mCategoryId : -1);
		values.put(TableCheckingAccount.SUBCATEGID, !chbSplitTransaction.isChecked() ? mSubCategoryId : -1);
		values.put(TableCheckingAccount.TRANSDATE, mApplication.getSQLiteStringDate((Date)txtSelectDate.getTag()));
		values.put(TableCheckingAccount.FOLLOWUPID, -1);
		values.put(TableCheckingAccount.TOTRANSAMOUNT, (Float)txtTotAmount.getTag());
		values.put(TableCheckingAccount.TRANSACTIONNUMBER, edtTransNumber.getText().toString());
		values.put(TableCheckingAccount.NOTES, edtNotes.getText().toString());
		
		// check whether the application should do the update or insert
		if (Constants.INTENT_ACTION_INSERT.equals(mIntentAction)) {
			// insert
			Uri insert = getContentResolver().insert(mCheckingAccount.getUri(), values);
			if (insert == null) {
				Toast.makeText(this, R.string.db_checking_insert_failed, Toast.LENGTH_SHORT).show();
				Log.w(LOGCAT, "Insert new transaction failed!");
				return false;
			}
			mTransId = Integer.parseInt(insert.getPathSegments().get(1));
		} else {
			// update
			if (getContentResolver().update(mCheckingAccount.getUri(), values, TableCheckingAccount.TRANSID + "=?", new String[] {Integer.toString(mTransId)}) <= 0) {
				Toast.makeText(this, R.string.db_checking_update_failed, Toast.LENGTH_SHORT).show();
				Log.w(LOGCAT, "Update transaction failed!");
				return false;
			}
		}
		// has split transaction
		boolean hasSplitTransaction = mSplitTransaction != null && mSplitTransaction.size() > 0;
		// update split transaction
		if (hasSplitTransaction) {
			for(int i = 0; i < mSplitTransaction.size(); i ++) {
				values.clear();
				//put value
				values.put(TableSplitTransactions.CATEGID, mSplitTransaction.get(i).getCategId());
				values.put(TableSplitTransactions.SUBCATEGID, mSplitTransaction.get(i).getSubCategId());
				values.put(TableSplitTransactions.SPLITTRANSAMOUNT, mSplitTransaction.get(i).getSplitTransAmount());
				values.put(TableSplitTransactions.TRANSID, mTransId);
				
				if (mSplitTransaction.get(i).getSplitTransId() == -1) {
					// insert data
					if (getContentResolver().insert(mSplitTransaction.get(i).getUri(), values) == null) {
						Toast.makeText(this, R.string.db_checking_insert_failed, Toast.LENGTH_SHORT).show();
						Log.w(LOGCAT, "Insert new split transaction failed!");
						return false;
					}
				} else {
					// update data
					if (getContentResolver().update(mSplitTransaction.get(i).getUri(), values, TableSplitTransactions.SPLITTRANSID + "=?", new String[] {Integer.toString(mSplitTransaction.get(i).getSplitTransId())}) <= 0) {
						Toast.makeText(this, R.string.db_checking_update_failed, Toast.LENGTH_SHORT).show();
						Log.w(LOGCAT, "Update split transaction failed!");
						return false;
					}
				}
			}
		}
		// deleted old split transaction
		if (mSplitTransactionDeleted != null && mSplitTransactionDeleted.size() > 0) {
			for(int i = 0; i < mSplitTransactionDeleted.size(); i ++) {
				values.clear();
				//put value
				values.put(TableSplitTransactions.SPLITTRANSAMOUNT, mSplitTransactionDeleted.get(i).getSplitTransAmount());
				
				// update data
				if (getContentResolver().delete(mSplitTransactionDeleted.get(i).getUri(), TableSplitTransactions.SPLITTRANSID + "=?", new String[] {Integer.toString(mSplitTransactionDeleted.get(i).getSplitTransId())}) <= 0) {
					Toast.makeText(this, R.string.db_checking_update_failed, Toast.LENGTH_SHORT).show();
					Log.w(LOGCAT, "Delete split transaction failed!");
					return false;
				}
			}
		}
		// update category and subcategory payee
		if ((!(Constants.TRANSACTION_TYPE_TRANSFER.equalsIgnoreCase(mTransCode))) && (mPayeeId > 0) && (!hasSplitTransaction)) {
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
		//update bill deposits
		if (mBdId > -1 && !(TextUtils.isEmpty(mNextOccurrence))) {
			values.clear();
			values.put(TableBillsDeposits.NEXTOCCURRENCEDATE, mNextOccurrence);
			// update date
			if (getContentResolver().update(new TableBillsDeposits().getUri(), values, TableBillsDeposits.BDID + "=?", new String[] {Integer.toString(mBdId)}) > 0) {
			} else {
				Toast.makeText(this, R.string.db_update_failed, Toast.LENGTH_SHORT).show();
				Log.w(LOGCAT, "Update Bill Deposits with Id=" + Integer.toString(mBdId) + " return <= 0");
			}
		}
		return true;
	}
}
