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
package com.money.manager.ex.adapter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v4.widget.CursorAdapter;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.money.manager.ex.Constants;
import com.money.manager.ex.MoneyManagerApplication;
import com.money.manager.ex.R;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.core.CurrencyUtils;
import com.money.manager.ex.database.QueryAllData;
import com.money.manager.ex.database.QueryBillDeposits;
import com.money.manager.ex.database.TableAccountList;
import com.money.manager.ex.database.TableCheckingAccount;

@SuppressLint("UseSparseArrays")
public class AllDataAdapter extends CursorAdapter {
	// source type: AllData or RepeatingTransaction
	public enum TypeCursor {ALLDATA, REPEATINGTRANSACTION};
	// type cursor
	private TypeCursor mTypeCursor = TypeCursor.ALLDATA;
	// define cursor field
	private String ID, DATE, ACCOUNTID, STATUS, AMOUNT, TRANSACTIONTYPE, TOACCOUNTID, TOTRANSAMOUNT, CURRENCYID, PAYEE,
			ACCOUNTNAME, TOACCOUNTNAME, CATEGORY, SUBCATEGORY, NOTES, TOCURRENCYID;
	
	private LayoutInflater mInflater;
	private MoneyManagerApplication mApplication;
	// hash map for group
	private HashMap<Integer, Integer> mHeadersAccountIndex;
	// private HashMap<Integer, Double> mBalanceTransactions;
	private SparseBooleanArray mCheckedPosition;
	// account and currency
	private int mAccountId = -1;
	private int mCurrencyId = -1;
	// show account name and show balance
	private boolean mShowAccountName = false;
	private boolean mShowBalanceAmount = false;
	// database for balance account
	private SQLiteDatabase mDatabase;
	// core and context
	private Context mContext;
	private Core mCore;
	
	public AllDataAdapter(Context context, Cursor c, TypeCursor typeCursor) {
		super(context, c, -1);
		this.mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.mApplication = (MoneyManagerApplication) context.getApplicationContext();
		// create hash map
		mHeadersAccountIndex = new HashMap<Integer, Integer>();
		// create sparse array boolean checked
		mCheckedPosition = new SparseBooleanArray();
		
		mTypeCursor = typeCursor;
		
		mCore = new Core(context);
		mContext = context;
		
		setFieldFromTypeCursor();
	}
	
	@SuppressWarnings({ })
	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		// take a pointer of object UI
		LinearLayout linDate = (LinearLayout)view.findViewById(R.id.linearLayoutDate);
		TextView txtDay = (TextView)view.findViewById(R.id.textViewDay);
		TextView txtMonth = (TextView)view.findViewById(R.id.textViewMonth);
		TextView txtYear = (TextView)view.findViewById(R.id.textViewYear);
		TextView txtStatus = (TextView)view.findViewById(R.id.textViewStatus);
		TextView txtAmount = (TextView)view.findViewById(R.id.textViewAmount);
		TextView txtPayee = (TextView)view.findViewById(R.id.textViewPayee);
		TextView txtAccountName = (TextView)view.findViewById(R.id.textViewAccountName);
		TextView txtCategorySub = (TextView)view.findViewById(R.id.textViewCategorySub);
		TextView txtNotes = (TextView)view.findViewById(R.id.textViewNotes);
		TextView txtBalance = (TextView)view.findViewById(R.id.textViewBalance);
		// header index
		if (!mHeadersAccountIndex.containsKey(cursor.getInt(cursor.getColumnIndex(ACCOUNTID)))) {
			mHeadersAccountIndex.put(cursor.getInt(cursor.getColumnIndex(ACCOUNTID)), cursor.getPosition());
		}
		// write status
		txtStatus.setText(mApplication.getStatusAsString(cursor.getString(cursor.getColumnIndex(STATUS))));
		// color status
		int colorBackground = getBackgroundColorFromStatus(cursor.getString(cursor.getColumnIndex(STATUS)));
		linDate.setBackgroundColor(colorBackground);
		txtStatus.setTextColor(Color.GRAY);
		// date group
		try {
			Date date = new SimpleDateFormat("yyyy-MM-dd").parse(cursor.getString(cursor.getColumnIndex(DATE)));
			txtMonth.setText(new SimpleDateFormat("MMM").format(date));
			txtYear.setText(new SimpleDateFormat("yyyy").format(date));
			txtDay.setText(new SimpleDateFormat("dd").format(date));
		} catch (ParseException e) {
			Log.e(AllDataAdapter.class.getSimpleName(), e.getMessage());
		}
		// take transaction amount
		double amount = cursor.getDouble(cursor.getColumnIndex(AMOUNT));
		// set currency id
		setCurrencyId(cursor.getInt(cursor.getColumnIndex(CURRENCYID)));
		// manage transfer and change amount sign
		if ((cursor.getString(cursor.getColumnIndex(TRANSACTIONTYPE)) != null) &&
			(Constants.TRANSACTION_TYPE_TRANSFER.equalsIgnoreCase(cursor.getString(cursor.getColumnIndex(TRANSACTIONTYPE)))))  {
			if (getAccountId() != cursor.getInt(cursor.getColumnIndex(TOACCOUNTID))) {
				amount = -(amount); // -total
			} else if (getAccountId() == cursor.getInt(cursor.getColumnIndex(TOACCOUNTID))) {
				amount = cursor.getDouble(cursor.getColumnIndex(TOTRANSAMOUNT)); // to account = account
				setCurrencyId(cursor.getInt(cursor.getColumnIndex(TOCURRENCYID)));
			}
		}
		// check amount sign
		CurrencyUtils currencyUtils = new CurrencyUtils(mContext);
		txtAmount.setText(currencyUtils.getCurrencyFormatted(getCurrencyId(), amount));
		// text color amount
		if (Constants.TRANSACTION_TYPE_TRANSFER.equalsIgnoreCase(cursor.getString(cursor.getColumnIndex(TRANSACTIONTYPE)))) {
			txtAmount.setTextColor(Color.GRAY);
		} else if (Constants.TRANSACTION_TYPE_DEPOSIT.equalsIgnoreCase(cursor.getString(cursor.getColumnIndex(TRANSACTIONTYPE)))) {
			txtAmount.setTextColor(mCore.resolveColorAttribute(R.attr.holo_green_color_theme));
		} else {
			txtAmount.setTextColor(mCore.resolveColorAttribute(R.attr.holo_red_color_theme));
		}
		// compose payee description
		txtPayee.setText(cursor.getString(cursor.getColumnIndex(PAYEE)));
		// compose account name
		if (isShowAccountName()) {
			if (mHeadersAccountIndex.containsValue(cursor.getPosition())) {
				txtAccountName.setText(cursor.getString(cursor.getColumnIndex(ACCOUNTNAME)));
				txtAccountName.setVisibility(View.VISIBLE);
			} else {
				txtAccountName.setVisibility(View.GONE);
			}
		} else {
			txtAccountName.setVisibility(View.GONE);
		}
		// write ToAccountName
		if ((!TextUtils.isEmpty(cursor.getString(cursor.getColumnIndex(TOACCOUNTNAME))))) {
			if (getAccountId() != cursor.getInt(cursor.getColumnIndex(TOACCOUNTID)))
				txtPayee.setText(cursor.getString(cursor.getColumnIndex(TOACCOUNTNAME)));
			else
				txtPayee.setText(cursor.getString(cursor.getColumnIndex(ACCOUNTNAME)));
		}
		// compose category description
		String categorySub = cursor.getString(cursor.getColumnIndex(CATEGORY));
		// check sub category
		if (!(TextUtils.isEmpty(cursor.getString(cursor.getColumnIndex(SUBCATEGORY))))) {
			categorySub += " : <i>" + cursor.getString(cursor.getColumnIndex(SUBCATEGORY)) + "</i>";
		}
		// write category/subcategory format html
		if (!TextUtils.isEmpty(categorySub)) {
			txtCategorySub.setText(Html.fromHtml(categorySub));
		} else {
			txtCategorySub.setText("");
		}
		// notes
		if (!TextUtils.isEmpty(cursor.getString(cursor.getColumnIndex(NOTES)))) {
			txtNotes.setText(Html.fromHtml("<small>" + cursor.getString(cursor.getColumnIndex(NOTES)) + "</small>"));
			txtNotes.setVisibility(View.VISIBLE);
		} else {
			txtNotes.setVisibility(View.GONE);
		}
		// check if item is checked
		if (mCheckedPosition.get(cursor.getPosition(), false)) {
			view.setBackgroundResource(R.color.holo_blue_light);
		} else {
			view.setBackgroundResource(android.R.color.transparent);
		}
		// balance account or days left
		if (mTypeCursor == TypeCursor.ALLDATA) {
			if (isShowBalanceAmount() && getDatabase() != null) {
				int transId = cursor.getInt(cursor.getColumnIndex(ID));
				// create thread for calculate balance amount
				BalanceAmount balanceAmount = new BalanceAmount();
				balanceAmount.setAccountId(getAccountId());
				balanceAmount.setDate(cursor.getString(cursor.getColumnIndex(DATE)));
				balanceAmount.setTextView(txtBalance);
				balanceAmount.setContext(mContext);
				balanceAmount.setDatabase(getDatabase());
				balanceAmount.setTransId(transId);
				// execute thread
				balanceAmount.execute();
			} else {
				txtBalance.setVisibility(View.GONE);
			}
		} else {
			int daysLeft = cursor.getInt(cursor.getColumnIndex(QueryBillDeposits.DAYSLEFT));
			if (daysLeft == 0) {
				txtBalance.setText(R.string.inactive);
			} else {
				txtBalance.setText(Integer.toString(Math.abs(daysLeft)) + " " + context.getString(daysLeft > 0 ? R.string.days_remaining : R.string.days_overdue));
			}
			txtBalance.setVisibility(View.VISIBLE);
		}
	}
	
	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		return mInflater.inflate(R.layout.item_alldata_account, parent, false);
	}
	
	public int getBackgroundColorFromStatus(String status) {
		if (Constants.TRANSACTION_STATUS_RECONCILED.equalsIgnoreCase(status)) {
			return mContext.getResources().getColor(R.color.holo_green_dark);
		} else if (Constants.TRANSACTION_STATUS_VOID.equalsIgnoreCase(status)) {
			return mContext.getResources().getColor(R.color.holo_red_dark);
		} else if (Constants.TRANSACTION_STATUS_FOLLOWUP.equalsIgnoreCase(status)) {
			return mContext.getResources().getColor(R.color.holo_orange_dark);
		} else if (Constants.TRANSACTION_STATUS_DUPLICATE.equalsIgnoreCase(status)) {
			return mContext.getResources().getColor(R.color.holo_blue_dark);
		} else {
			return Color.GRAY;
		}
	}
	
	public void clearPositionChecked() {
		mCheckedPosition.clear();
	}
	
	/**
	 * Set checked in position
	 * @param position
	 * @param checked
	 */
	public void setPositionChecked(int position, boolean checked) {
		mCheckedPosition.put(position, checked);
	}
	
	/**
	 * @return the mAccountId
	 */
	public int getAccountId() {
		return mAccountId;
	}

	/**
	 * @param mAccountId the mAccountId to set
	 */
	public void setAccountId(int mAccountId) {
		this.mAccountId = mAccountId;
	}

	/**
	 * @return the mCurrencyId
	 */
	public int getCurrencyId() {
		return mCurrencyId;
	}

	/**
	 * @param mCurrencyId the mCurrencyId to set
	 */
	public void setCurrencyId(int mCurrencyId) {
		this.mCurrencyId = mCurrencyId;
	}

	/**
	 * @return the mShowAccountName
	 */
	public boolean isShowAccountName() {
		return mShowAccountName;
	}

	/**
	 * @param mShowAccountName the mShowAccountName to set
	 */
	public void setShowAccountName(boolean mShowAccountName) {
		this.mShowAccountName = mShowAccountName;
	}

	/**
	 * @return the mDatabase
	 */
	public SQLiteDatabase getDatabase() {
		return mDatabase;
	}

	/**
	 * @param mDatabase the mDatabase to set
	 */
	public void setDatabase(SQLiteDatabase mDatabase) {
		this.mDatabase = mDatabase;
	}

	/**
	 * @return the mShowBalanceAmount
	 */
	public boolean isShowBalanceAmount() {
		return mShowBalanceAmount;
	}

	/**
	 * @param mShowBalanceAmount the mShowBalanceAmount to set
	 */
	public void setShowBalanceAmount(boolean mShowBalanceAmount) {
		this.mShowBalanceAmount = mShowBalanceAmount;
	}

	public void setFieldFromTypeCursor() {
		ID = mTypeCursor == TypeCursor.ALLDATA ? QueryAllData.ID : QueryBillDeposits.BDID;
		DATE = mTypeCursor == TypeCursor.ALLDATA ? QueryAllData.Date : QueryBillDeposits.NEXTOCCURRENCEDATE;
		ACCOUNTID = mTypeCursor == TypeCursor.ALLDATA ? QueryAllData.ACCOUNTID : QueryBillDeposits.ACCOUNTID;
		STATUS = mTypeCursor == TypeCursor.ALLDATA ? QueryAllData.Status : QueryBillDeposits.STATUS;
		AMOUNT = mTypeCursor == TypeCursor.ALLDATA ? QueryAllData.Amount : QueryBillDeposits.AMOUNT;
		TRANSACTIONTYPE = mTypeCursor == TypeCursor.ALLDATA ? QueryAllData.TransactionType : QueryBillDeposits.TRANSCODE;
		TOACCOUNTID = mTypeCursor == TypeCursor.ALLDATA ? QueryAllData.ToAccountID : QueryBillDeposits.TOACCOUNTID;
		TOTRANSAMOUNT = mTypeCursor == TypeCursor.ALLDATA ? QueryAllData.TOTRANSAMOUNT : QueryBillDeposits.TOTRANSAMOUNT;
		CURRENCYID = mTypeCursor == TypeCursor.ALLDATA ? QueryAllData.CURRENCYID : QueryBillDeposits.CURRENCYID;
		TOCURRENCYID = mTypeCursor == TypeCursor.ALLDATA ? QueryAllData.ToCurrencyID : QueryBillDeposits.CURRENCYID;
		PAYEE = mTypeCursor == TypeCursor.ALLDATA ? QueryAllData.Payee : QueryBillDeposits.PAYEENAME;
		ACCOUNTNAME = mTypeCursor == TypeCursor.ALLDATA ? QueryAllData.AccountName : QueryBillDeposits.ACCOUNTNAME;
		TOACCOUNTNAME = mTypeCursor == TypeCursor.ALLDATA ? QueryAllData.ToAccountName : QueryBillDeposits.TOACCOUNTNAME;
		CATEGORY = mTypeCursor == TypeCursor.ALLDATA ? QueryAllData.Category : QueryBillDeposits.CATEGNAME;
		SUBCATEGORY = mTypeCursor == TypeCursor.ALLDATA ? QueryAllData.Subcategory : QueryBillDeposits.SUBCATEGNAME;
		NOTES = mTypeCursor == TypeCursor.ALLDATA ? QueryAllData.Notes : QueryBillDeposits.NOTES;
	}
	
	
	private class BalanceAmount extends AsyncTask<Void, Void, Boolean> {
		private int mAccountId;
		private int mTransId;
		private String mDate;
		private TextView mTextView;
		private Context mContext;
		private double total = 0;
		private SQLiteDatabase mDatabase;
		
		@Override
		protected Boolean doInBackground(Void... params) {
			TableCheckingAccount checkingAccount = new TableCheckingAccount();
			String selection = "(" + TableCheckingAccount.ACCOUNTID + "=" + Integer.toString(getAccountId()) + " OR " + TableCheckingAccount.TOACCOUNTID + "=" + Integer.toString(getAccountId()) + ") " + "" +
					"AND (" + TableCheckingAccount.TRANSDATE + "<'" + getDate() + "' OR (" + TableCheckingAccount.TRANSDATE + "='" + getDate() + "' AND " + TableCheckingAccount.TRANSID + "<=" + Integer.toString(getTransId()) + ")) " +
					"AND " + TableCheckingAccount.STATUS + "<>'V'"; 
			Cursor cursor = getDatabase().query(checkingAccount.getSource(), checkingAccount.getAllColumns(), selection, null, null, null, null);
			if (cursor != null && cursor.moveToFirst()) {
				while (!cursor.isAfterLast()) {
					if (Constants.TRANSACTION_TYPE_WITHDRAWAL.equalsIgnoreCase(cursor.getString(cursor.getColumnIndex(TableCheckingAccount.TRANSCODE)))) {
						total -= cursor.getDouble(cursor.getColumnIndex(TableCheckingAccount.TRANSAMOUNT));
					}
					else if (Constants.TRANSACTION_TYPE_DEPOSIT.equalsIgnoreCase(cursor.getString(cursor.getColumnIndex(TableCheckingAccount.TRANSCODE)))) {
						total += cursor.getDouble(cursor.getColumnIndex(TableCheckingAccount.TRANSAMOUNT));
					} else {
						if (cursor.getInt(cursor.getColumnIndex(TableCheckingAccount.ACCOUNTID)) == getAccountId()) {
							total -= cursor.getDouble(cursor.getColumnIndex(TableCheckingAccount.TRANSAMOUNT));
						} else {
							total += cursor.getDouble(cursor.getColumnIndex(TableCheckingAccount.TOTRANSAMOUNT));
						}
					}
					cursor.moveToNext();
				}
			}
			cursor.close();
			// calculate initial bal
			TableAccountList accountList = new TableAccountList();
			cursor = getDatabase().query(accountList.getSource(), accountList.getAllColumns(), TableAccountList.ACCOUNTID + "=" + Integer.toString(getAccountId()), null, null, null, null);
			if (cursor != null && cursor.moveToFirst()) {
				total += cursor.getDouble(cursor.getColumnIndex(TableAccountList.INITIALBAL));
			}
			cursor.close();
			return true;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			if (result && getTextView() != null) {
				CurrencyUtils currencyUtils = new CurrencyUtils(mContext);
				
				getTextView().setText(currencyUtils.getCurrencyFormatted(getCurrencyId(), total));
				if (getTextView().getVisibility() != View.VISIBLE) getTextView().setVisibility(View.VISIBLE);
				// put in hash map balance total
				// mBalanceTransactions.put(getTransId(), total);
			}
		}

		/**
		 * @return the mAccountId
		 */
		public int getAccountId() {
			return mAccountId;
		}

		/**
		 * @param mAccountId the mAccountId to set
		 */
		public void setAccountId(int mAccountId) {
			this.mAccountId = mAccountId;
		}

		/**
		 * @return the mDate
		 */
		public String getDate() {
			return mDate;
		}

		/**
		 * @param mDate the mDate to set
		 */
		public void setDate(String mDate) {
			this.mDate = mDate;
		}

		/**
		 * @return the mTextView
		 */
		public TextView getTextView() {
			return mTextView;
		}

		/**
		 * @param mTextView the mTextView to set
		 */
		public void setTextView(TextView mTextView) {
			this.mTextView = mTextView;
		}

		/**
		 * @return the mContext
		 */
		@SuppressWarnings("unused")
		public Context getContext() {
			return mContext;
		}

		/**
		 * @param mContext the mContext to set
		 */
		public void setContext(Context mContext) {
			this.mContext = mContext;
		}

		/**
		 * @return the mTransId
		 */
		public int getTransId() {
			return mTransId;
		}

		/**
		 * @param mTransId the mTransId to set
		 */
		public void setTransId(int mTransId) {
			this.mTransId = mTransId;
		}

		/**
		 * @return the mDatabase
		 */
		public SQLiteDatabase getDatabase() {
			return mDatabase;
		}

		/**
		 * @param mDatabase the mDatabase to set
		 */
		public void setDatabase(SQLiteDatabase mDatabase) {
			this.mDatabase = mDatabase;
		}
		
	}
}
