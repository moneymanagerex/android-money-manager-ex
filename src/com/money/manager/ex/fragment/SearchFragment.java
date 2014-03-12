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
import android.content.Context;
import android.content.Intent;
import android.os.Build;
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
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.money.manager.ex.CategorySubCategoryExpandableListActivity;
import com.money.manager.ex.MoneyManagerApplication;
import com.money.manager.ex.PayeeActivity;
import com.money.manager.ex.R;
import com.money.manager.ex.SearchActivity;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.database.MoneyManagerOpenHelper;
import com.money.manager.ex.database.QueryAllData;
import com.money.manager.ex.database.TableAccountList;
import com.money.manager.ex.database.ViewAllData;
import com.money.manager.ex.fragment.InputAmountDialog.InputAmountDialogListener;

public class SearchFragment extends SherlockFragment implements InputAmountDialogListener {
	// LOGCAT
	private static final String LOGCAT = SearchFragment.class.getSimpleName(); 
	// ID REQUEST code
	private static final int REQUEST_PICK_PAYEE = 1;
	private static final int REQUEST_PICK_CATEGORY = 3;
	// reference view into layout
	private Spinner spinAccount, spinStatus;
	private EditText edtTransNumber, edtNotes;
	private TextView txtToAmount, txtFromAmount, txtSelectCategory, txtSelectPayee, txtFromDate, txtToDate;
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
		private TextView mTextView;
		
		public OnDateButtonClickListener(TextView txtFromDate) {
			super();
			mTextView = txtFromDate;
		}
		
		private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {

			@Override
			public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
				try {
					Date date = new SimpleDateFormat("yyyy-MM-dd").parse(Integer.toString(year) + "-" + Integer.toString(monthOfYear + 1) + "-" + Integer.toString(dayOfMonth));
					mTextView.setText(mApplication.getStringFromDate(date));
				} catch (Exception e) {
					Log.e(LOGCAT, e.getMessage());
				}
				
			}
		};
		
		@Override
		public void onClick(View v) {
			Calendar date = Calendar.getInstance();
			if (!TextUtils.isEmpty(mTextView.getText())) {
				date.setTime(mApplication.getDateFromString(mTextView.getText().toString()));
			}
			DatePickerDialog dialog = new DatePickerDialog(getSherlockActivity(), mDateSetListener, date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DATE));
			dialog.show();
		}
	}		
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mApplication = (MoneyManagerApplication)getSherlockActivity().getApplication();
		setHasOptionsMenu(true);
		AllDataFragment fragment;
		fragment = (AllDataFragment) getSherlockActivity().getSupportFragmentManager().findFragmentByTag(AllDataFragment.class.getSimpleName());
		if (fragment != null) {
			fragment.setSearResultFragmentLoaderCallbacks((SearchActivity)getSherlockActivity());
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (container == null) return null;	
		//create view
		View view = (LinearLayout)inflater.inflate(R.layout.search_activity, container, false);
		//create listener amount
		OnClickListener onClickAmount = new OnClickListener() {

			@Override
			public void onClick(View v) {
				float amount = 0;
				if (v.getTag() != null && v.getTag() instanceof Float) {
					amount = (Float)((TextView) v).getTag();
				}
				InputAmountDialog dialog = InputAmountDialog.getInstance(v.getId(), amount);
				dialog.show(getSherlockActivity().getSupportFragmentManager(), dialog.getClass().getSimpleName());
			}
		};
		//To Amount
		txtToAmount = (TextView)view.findViewById(R.id.textViewFromAmount);
		txtToAmount.setOnClickListener(onClickAmount);
		//From Amount
		txtFromAmount = (TextView)view.findViewById(R.id.textViewToAmount);
		txtFromAmount.setOnClickListener(onClickAmount);
		
		// accountlist <> to populate the spin
		spinAccount = (Spinner)view.findViewById(R.id.spinnerAccount);
		if (mAccountList == null) {
			mAccountList = new MoneyManagerOpenHelper(getSherlockActivity()).getListAccounts(mApplication.getAccountsOpenVisible(), mApplication.getAccountFavoriteVisible());
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
		ArrayAdapter<String> adapterAccount = new ArrayAdapter<String>(getSherlockActivity(), R.layout.sherlock_spinner_item, mAccountNameList);
		adapterAccount.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinAccount.setAdapter(adapterAccount);
		//Payee
		txtSelectPayee = (TextView)view.findViewById(R.id.textViewSelectPayee);
		txtSelectPayee.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getSherlockActivity(), PayeeActivity.class);
				intent.setAction(Intent.ACTION_PICK);
				startActivityForResult(intent, REQUEST_PICK_PAYEE);
			}
		});
		//Category
		txtSelectCategory = (TextView) view.findViewById(R.id.textViewSelectCategory);
		txtSelectCategory.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getSherlockActivity(), CategorySubCategoryExpandableListActivity.class);
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
		ArrayAdapter<String> adapterStatus = new ArrayAdapter<String>(getSherlockActivity(), R.layout.sherlock_spinner_item, mStatusItems);
		adapterStatus.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinStatus.setAdapter(adapterStatus);
		// from date
		txtFromDate = (TextView)view.findViewById(R.id.textViewFromDate);
		txtFromDate.setOnClickListener(new OnDateButtonClickListener(txtFromDate));
		// to date
		txtToDate = (TextView)view.findViewById(R.id.textViewToDate);
		txtToDate.setOnClickListener(new OnDateButtonClickListener(txtToDate));
		// transaction number
		edtTransNumber = (EditText)view.findViewById(R.id.editTextTransNumber);
		// notes
		edtNotes = (EditText)view.findViewById(R.id.editTextNotes);

		return view;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Core core = new Core(getSherlockActivity());
		// ****** action bar *****
		getSherlockActivity().getSupportActionBar().setSubtitle(null);
		if (!(core.isTablet() || Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH)) {
			getSherlockActivity().getSupportActionBar().setDisplayOptions(
					ActionBar.DISPLAY_SHOW_CUSTOM,
					ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE
							| ActionBar.DISPLAY_SHOW_CUSTOM);
			
			LayoutInflater inflater = (LayoutInflater)getSherlockActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			
	        View actionBarButtons = inflater.inflate(R.layout.actionbar_button_cancel_done, new LinearLayout(getSherlockActivity()), false);
	        View cancelActionView = actionBarButtons.findViewById(R.id.action_cancel);
	        cancelActionView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					onDoneClick();
				}
			});
	        View doneActionView = actionBarButtons.findViewById(R.id.action_done);
	        ImageView doneImageView = (ImageView) doneActionView.findViewById(R.id.image_done);
	        doneImageView.setImageDrawable(getSherlockActivity().getResources().getDrawable(core.resolveIdAttribute(R.attr.ic_action_search)));
	        TextView doneTextView = (TextView) doneActionView.findViewById(R.id.text_done);
	        doneTextView.setText(R.string.search);
	        
	        doneActionView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					onSearchClick();
				}
			});
	        getSherlockActivity().getSupportActionBar().setCustomView(actionBarButtons);
		}
		// ****** action bar *****
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch(requestCode) {
		case REQUEST_PICK_PAYEE:
			if ((resultCode == Activity.RESULT_OK) && (data != null)) {
				txtSelectPayee.setTag(data.getIntExtra(PayeeActivity.INTENT_RESULT_PAYEEID, -1));
				txtSelectPayee.setText(data.getStringExtra(PayeeActivity.INTENT_RESULT_PAYEENAME));
			}
			break;
		case REQUEST_PICK_CATEGORY:
			if ((resultCode == Activity.RESULT_OK) && (data != null)) {
				//create class for store data
				CategorySub categorySub = new CategorySub();
				categorySub.categId = data.getIntExtra(CategorySubCategoryExpandableListActivity.INTENT_RESULT_CATEGID, -1);
				categorySub.categName = data.getStringExtra(CategorySubCategoryExpandableListActivity.INTENT_RESULT_CATEGNAME);
				categorySub.subCategId = data.getIntExtra(CategorySubCategoryExpandableListActivity.INTENT_RESULT_SUBCATEGID, -1);
				categorySub.subCategName = data.getStringExtra(CategorySubCategoryExpandableListActivity.INTENT_RESULT_SUBCATEGNAME);
				//save into button
				txtSelectCategory.setText(categorySub.categName + (!TextUtils.isEmpty(categorySub.subCategName) ? " : " + categorySub.subCategName : ""));
				txtSelectCategory.setTag(categorySub);
			}
		}
	}
	
	public void onDoneClick() {
		getSherlockActivity().finish();
	}
	
	public void onSearchClick() {
		executeSearch();
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		Core core = new Core(getSherlockActivity());
		if (core.isTablet() || Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			inflater.inflate(R.menu.menu_button_cancel_done, menu);
			// change item ok in search
			MenuItem doneItem = menu.findItem(R.id.menu_done);
			if (doneItem != null) {
				doneItem.setIcon(core.resolveIdAttribute(R.attr.ic_action_search));
				doneItem.setTitle(R.string.search);
			}
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_cancel:
			getSherlockActivity().finish();
			return true;
		case R.id.menu_done:
		case R.id.menu_search_transaction:
			executeSearch();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	/**
	 * Compose arguments and execute search
	 */
	public void executeSearch() {
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
		if (spinStatus.getSelectedItemPosition() > 0) {
			whereClause.add(ViewAllData.Status + "='" + mStatusValues.get(spinStatus.getSelectedItemPosition()) + "'");
		}
		//from date
		if (!TextUtils.isEmpty(txtFromDate.getText())) {
			whereClause.add(ViewAllData.Date + ">='" + mApplication.getSQLiteStringDate(mApplication.getDateFromString(String.valueOf(txtFromDate.getText()))) + "'");
		}
		//to date
		if (!TextUtils.isEmpty(txtToDate.getText())) {
			whereClause.add(ViewAllData.Date + "<='" + mApplication.getSQLiteStringDate(mApplication.getDateFromString(String.valueOf(txtToDate.getText()))) + "'");
		}
		//payee
		if (txtSelectPayee.getTag() != null) {
			whereClause.add(ViewAllData.PayeeID + "=" + String.valueOf(txtSelectPayee.getTag()));
		}
		//categories
		if (txtSelectCategory.getTag() != null) {
			CategorySub categorySub = (CategorySub)txtSelectCategory.getTag();
			whereClause.add(ViewAllData.CategID + "=" + categorySub.categId);
			if (categorySub.subCategId != -1)
				whereClause.add(ViewAllData.SubcategID + "=" + categorySub.subCategId);
		}
		//from amount
		if (txtFromAmount.getTag() != null) {
			whereClause.add(ViewAllData.Amount + ">=" + String.valueOf(txtFromAmount.getTag()));
		}
		//to amount
		if (txtToAmount.getTag() != null) {
			whereClause.add(ViewAllData.Amount + "<=" + String.valueOf(txtToAmount.getTag()));
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
		fragment = (AllDataFragment) getSherlockActivity().getSupportFragmentManager().findFragmentByTag(AllDataFragment.class.getSimpleName());
		if (fragment != null) {
			getSherlockActivity().getSupportFragmentManager().beginTransaction().remove(fragment).commit();
		}
		fragment = AllDataFragment.newInstance(-1);
		//create bundle
		Bundle args = new Bundle();
		args.putStringArrayList(AllDataFragment.KEY_ARGUMENTS_WHERE, whereClause);
		args.putString(AllDataFragment.KEY_ARGUMENTS_SORT, QueryAllData.ACCOUNTID + ", " + QueryAllData.ID);
		//set arguments
		fragment.setArguments(args);
		fragment.setSearResultFragmentLoaderCallbacks((SearchActivity)getSherlockActivity());
		fragment.setShownHeader(true);
		//add fragment
		FragmentTransaction transaction = getSherlockActivity().getSupportFragmentManager().beginTransaction();
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

	@Override
	public void onFinishedInputAmountDialog(int id, Float amount) {
		Core core = new Core(getSherlockActivity());

		View view = getView().findViewById(id);
		if (view != null && view instanceof TextView)
			core.formatAmountTextView(((TextView) view), amount);
	}
}
