/*******************************************************************************
 * Copyright (C) 2013 The Android Money Manager Ex Project
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
package com.money.manager.ex.fragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.MenuItem;
import com.money.manager.ex.AccountListActivity;
import com.money.manager.ex.CategorySubCategoryActivity;
import com.money.manager.ex.MoneyManagerApplication;
import com.money.manager.ex.PayeeActivity;
import com.money.manager.ex.R;
import com.money.manager.ex.SearchActivity;
import com.money.manager.ex.database.MoneyManagerOpenHelper;
import com.money.manager.ex.database.QueryAllData;
import com.money.manager.ex.database.TableAccountList;
import com.money.manager.ex.database.ViewAllData;

public class SearchFragment extends SherlockFragment {
	// LOGCAT
	private static final String LOGCAT = SearchFragment.class.getSimpleName(); 
	// ID REQUEST code
	private static final int REQUEST_PICK_PAYEE = 1;
	private static final int REQUEST_PICK_ACCOUNT = 2;
	private static final int REQUEST_PICK_CATEGORY = 3;
	// reference view into layout
	private Spinner spinAccount, spinStatus;
	private Button btnOk, btnCancel;
	private Button btnSelectPayee, btnSelectToAccount, btnSelectCategory;
	private ImageButton btnFromDate, btnToDate;
	private EditText edtToAmount, edtFromAmount, edtTransNumber, edtNotes, edtFromDate, edtToDate;
	private CheckBox cbxWithdrawal, cbxDeposit, cbxTransfer;
	// application
	private MoneyManagerApplication mApplication;
	// arrayslist accountname and accountid
	private ArrayList<String> mAccountNameList = new ArrayList<String>();
	private ArrayList<Integer> mAccountIdList = new ArrayList<Integer>();
	private List<TableAccountList> mAccountList;
	// status item and values
	private ArrayList<String> mStatusItems = new ArrayList<String>(), mStatusValues = new ArrayList<String>();
	// dual panel
	private boolean mDualPanel = false;
	
	private class CategorySub {
		public int categId;
		public String categName;
		public int subCategId;
		public String subCategName;
	}
	
	
	private class OnDateButtonClickListener implements OnClickListener {
		private EditText mEditText;
		
		public OnDateButtonClickListener(EditText editText) {
			super();
			mEditText = editText;
		}
		
		private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {

			@Override
			public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
				try {
					Date date = new SimpleDateFormat("yyyy-MM-dd").parse(Integer.toString(year) + "-" + Integer.toString(monthOfYear + 1) + "-" + Integer.toString(dayOfMonth));
					mEditText.setText(mApplication.getStringFromDate(date));
				} catch (Exception e) {
					Log.e(LOGCAT, e.getMessage());
				}
				
			}
		};
		
		@Override
		public void onClick(View v) {
			Calendar date = Calendar.getInstance();
			if (!TextUtils.isEmpty(mEditText.getText())) {
				date.setTime(mApplication.getDateFromString(mEditText.getText().toString()));
			}
			DatePickerDialog dialog = new DatePickerDialog(getActivity(), mDateSetListener, date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DATE));
			dialog.show();
		}
	}		
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mApplication = (MoneyManagerApplication)getActivity().getApplication();
		setHasOptionsMenu(true);
		AllDataFragment fragment;
		fragment = (AllDataFragment) getActivity().getSupportFragmentManager().findFragmentByTag(AllDataFragment.class.getSimpleName());
		if (fragment != null) {
			fragment.setSearResultFragmentLoaderCallbacks((SearchActivity)getActivity());
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (container == null) { return null; }
		//create view
		View view = (LinearLayout)inflater.inflate(R.layout.search_activity, container, false);
		//To Amount
		edtToAmount = (EditText)view.findViewById(R.id.editTextToAmount);
		//From Amount
		edtFromAmount = (EditText)view.findViewById(R.id.editTextFromAmount);
		// accountlist <> to populate the spin
		spinAccount = (Spinner)view.findViewById(R.id.spinnerAccount);
		if (mAccountList == null) {
			mAccountList = new MoneyManagerOpenHelper(getActivity()).getListAccounts(mApplication.getAccountsOpenVisible(), mApplication.getAccountFavoriteVisible());
			mAccountList.add(0, null);
			for(int i = 0; i <= mAccountList.size() - 1; i ++) {
				if (mAccountList.get(i) != null) {
					mAccountNameList.add(mAccountList.get(i).getAccountName());
					mAccountIdList.add(mAccountList.get(i).getAccountId());
				} else {
					mAccountNameList.add("");
					mAccountIdList.add(AdapterView.INVALID_POSITION);
				}
			}
		}
		// checkbox
		cbxDeposit = (CheckBox)view.findViewById(R.id.checkBoxDeposit);
		cbxTransfer = (CheckBox)view.findViewById(R.id.checkBoxTransfer);
		cbxWithdrawal = (CheckBox)view.findViewById(R.id.checkBoxWithdrawal);
		// create adapter for spinAccount
		ArrayAdapter<String> adapterAccount = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, mAccountNameList);
		adapterAccount.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinAccount.setAdapter(adapterAccount);
		//To Account
		btnSelectToAccount = (Button)view.findViewById(R.id.buttonSelectToAccount);
		btnSelectToAccount.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), AccountListActivity.class);
				intent.setAction(Intent.ACTION_PICK);
				startActivityForResult(intent, REQUEST_PICK_ACCOUNT);
			}
		});
		//Payee
		btnSelectPayee = (Button)view.findViewById(R.id.buttonSelectPayee);
		btnSelectPayee.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), PayeeActivity.class);
				intent.setAction(Intent.ACTION_PICK);
				startActivityForResult(intent, REQUEST_PICK_PAYEE);
			}
		});
		//Category
		btnSelectCategory = (Button)view.findViewById(R.id.buttonSelectCategory);
		btnSelectCategory.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), CategorySubCategoryActivity.class);
				intent.setAction(Intent.ACTION_PICK);
				startActivityForResult(intent, REQUEST_PICK_CATEGORY);
			}
		});
		if (mStatusItems.size() <= 0) {
			// arrays to manage Status
			mStatusItems.add(""); mStatusValues.add("");
			mStatusItems.addAll(Arrays.asList(getResources().getStringArray(R.array.status_items)));
			mStatusValues.addAll(Arrays.asList(getResources().getStringArray(R.array.status_values)));
		}
		// create adapter for spinnerStatus
		spinStatus = (Spinner)view.findViewById(R.id.spinnerStatus);
		ArrayAdapter<String> adapterStatus = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, mStatusItems);
		adapterStatus.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinStatus.setAdapter(adapterStatus);
		// from date
		edtFromDate = (EditText)view.findViewById(R.id.editFromDate);
		edtFromDate.setKeyListener(null);
		
		btnFromDate = (ImageButton)view.findViewById(R.id.buttonFromDate);
		btnFromDate.setOnClickListener(new OnDateButtonClickListener(edtFromDate));
		// to date
		edtToDate = (EditText)view.findViewById(R.id.editToDate);
		edtToDate.setKeyListener(null);

		btnToDate = (ImageButton)view.findViewById(R.id.buttonToDate);
		btnToDate.setOnClickListener(new OnDateButtonClickListener(edtToDate));
		// transaction number
		edtTransNumber = (EditText)view.findViewById(R.id.editTextTransNumber);
		// notes
		edtNotes = (EditText)view.findViewById(R.id.editTextNotes);
		
		//button ok and cancel
		btnCancel = (Button)view.findViewById(R.id.buttonCancel);
		btnCancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				getSherlockActivity().finish();
			}
		});
		btnOk = (Button)view.findViewById(R.id.buttonOk);
		btnOk.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				executeSearch();
			}
		});
		return view;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getSherlockActivity().getSupportActionBar().setSubtitle(null);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch(requestCode) {
		case REQUEST_PICK_PAYEE:
			if ((resultCode == Activity.RESULT_OK) && (data != null)) {
				btnSelectPayee.setTag(data.getIntExtra(PayeeActivity.INTENT_RESULT_PAYEEID, -1));
				btnSelectPayee.setText(data.getStringExtra(PayeeActivity.INTENT_RESULT_PAYEENAME));
			}
			break;
		case REQUEST_PICK_CATEGORY:
			if ((resultCode == Activity.RESULT_OK) && (data != null)) {
				//create class for store data
				CategorySub categorySub = new CategorySub();
				categorySub.categId = data.getIntExtra(CategorySubCategoryActivity.INTENT_RESULT_CATEGID, -1);
				categorySub.categName = data.getStringExtra(CategorySubCategoryActivity.INTENT_RESULT_CATEGNAME);
				categorySub.subCategId = data.getIntExtra(CategorySubCategoryActivity.INTENT_RESULT_SUBCATEGID, -1);
				categorySub.subCategName = data.getStringExtra(CategorySubCategoryActivity.INTENT_RESULT_SUBCATEGNAME);
				//save into button
				btnSelectCategory.setText(categorySub.categName + (!TextUtils.isEmpty(categorySub.subCategName) ? " : " + categorySub.subCategName : ""));
				btnSelectCategory.setTag(categorySub);
			}
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_search_transaction:
			executeSearch();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	/**
	 * Compose arguments and execute search
	 */
	private void executeSearch() {
		ArrayList<String> whereClause = new ArrayList<String>();
		//account
		if (spinAccount.getSelectedItemPosition() != AdapterView.INVALID_POSITION && mAccountIdList.get(spinAccount.getSelectedItemPosition()) != -1) {
			whereClause.add(ViewAllData.ACCOUNTID + "=" + mAccountIdList.get(spinAccount.getSelectedItemPosition()));
		}
		//checkbox
		if (cbxDeposit.isChecked() || cbxTransfer.isChecked() || cbxWithdrawal.isChecked()) {
			whereClause.add(ViewAllData.TransactionType + " IN (" + (cbxDeposit.isChecked() ? "'Deposit'" : "''") + "," + (cbxTransfer.isChecked() ? "'Transfer'" : "''")
					 + "," + (cbxWithdrawal.isChecked() ? "'Withdrawal'" : "''") + ")"); 
		}
		//status
		if (spinStatus.getSelectedItemPosition() != AdapterView.INVALID_POSITION && (!TextUtils.isEmpty(mStatusValues.get(spinStatus.getSelectedItemPosition())))) {
			whereClause.add(ViewAllData.Status + "='" + mStatusValues.get(spinStatus.getSelectedItemPosition()) + "'");
		}
		//from date
		if (!TextUtils.isEmpty(edtFromDate.getText())) {
			whereClause.add(ViewAllData.Date + ">='" + mApplication.getSQLiteStringDate(mApplication.getDateFromString(edtFromDate.getText().toString())) + "'");
		}
		//to date
		if (!TextUtils.isEmpty(edtToDate.getText())) {
			whereClause.add(ViewAllData.Date + "<='" + mApplication.getSQLiteStringDate(mApplication.getDateFromString(edtToDate.getText().toString())) + "'");
		}
		//payee
		if (btnSelectPayee.getTag() != null) {
			whereClause.add(ViewAllData.PayeeID + "=" + btnSelectPayee.getTag().toString());
		}
		//categories
		if (btnSelectCategory.getTag() != null) {
			CategorySub categorySub = (CategorySub)btnSelectCategory.getTag();
			whereClause.add(ViewAllData.CategID + "=" + categorySub.categId + " AND " + ViewAllData.SubcategID + "=" + categorySub.subCategId);
		}
		//from amount
		if (!TextUtils.isEmpty(edtFromAmount.getText())) {
			whereClause.add(ViewAllData.Amount + ">=" + edtFromAmount.getText());
		}
		//to amount
		if (!TextUtils.isEmpty(edtToAmount.getText())) {
			whereClause.add(ViewAllData.Amount + "<=" + edtToAmount.getText());
		}
		//transaction number
		if (!TextUtils.isEmpty(edtTransNumber.getText())) {
			whereClause.add(ViewAllData.TransactionNumber + " LIKE '" + edtTransNumber.getText() + "'");
		}
		//note
		if (!TextUtils.isEmpty(edtNotes.getText())) {
			whereClause.add(ViewAllData.Notes + " LIKE '" + edtNotes.getText() + "'");
		}
		//create a fragment search
		AllDataFragment fragment;
		fragment = (AllDataFragment) getActivity().getSupportFragmentManager().findFragmentByTag(AllDataFragment.class.getSimpleName());
		if (fragment != null) {
			getActivity().getSupportFragmentManager().beginTransaction().remove(fragment).commit();
		}
		fragment = AllDataFragment.newInstance(-1);
		//create bundle
		Bundle args = new Bundle();
		args.putStringArrayList(AllDataFragment.KEY_ARGUMENTS_WHERE, whereClause);
		args.putString(AllDataFragment.KEY_ARGUMENTS_SORT, QueryAllData.ACCOUNTID + ", " + QueryAllData.ID);
		//set arguments
		fragment.setArguments(args);
		fragment.setSearResultFragmentLoaderCallbacks((SearchActivity)getActivity());
		fragment.setShownHeader(true);
		//add fragment
		FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
		//animation
		transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out);
		// Replace whatever is in the fragment_container view with this fragment,
		// and add the transaction to the back stack
		if (isDualPanel()) {
			transaction.add(R.id.fragmentDetail, fragment, AllDataFragment.class.getSimpleName());
		} else {
			transaction.replace(R.id.fragmentContent, fragment, AllDataFragment.class.getSimpleName());
			transaction.addToBackStack(null);
		}
		// Commit the transaction
		transaction.commit();
	}

	/**
	 * @param mDualPanel the mDualPanel to set
	 */
	public void setDualPanel(boolean mDualPanel) {
		this.mDualPanel = mDualPanel;
	}

	/**
	 * @return the mDualPanel
	 */
	public boolean isDualPanel() {
		return mDualPanel;
	}
}
