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
package com.money.manager.ex.core;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v4.widget.CursorAdapter;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.money.manager.ex.MoneyManagerApplication;
import com.money.manager.ex.R;
import com.money.manager.ex.database.QueryAllData;
import com.money.manager.ex.database.TableAccountList;
import com.money.manager.ex.database.TableCheckingAccount;

public class AllDataAdapter extends CursorAdapter {
	private LayoutInflater mInflater;
	private MoneyManagerApplication mApplication;
	// hash map for group
	private HashMap<Integer, Integer> mHeadersAccountIndex;
	private HashMap<String, Integer> mHeadersIndexAccountDate;
	// private HashMap<Integer, Float> mBalanceTransactions;
	// account and currency
	private int mAccountId = -1;
	private int mCurrencyId = -1;
	// show account name and show balance
	private boolean mShowAccountName = false;
	private boolean mShowBalanceAmount = false;
	// database for balance account
	private SQLiteDatabase mDatabase;
	
	public AllDataAdapter(Context context, Cursor c) {
		super(context, c, -1);
		this.mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.mApplication = (MoneyManagerApplication) context.getApplicationContext();
		// create hash map
		mHeadersAccountIndex = new HashMap<Integer, Integer>();
		mHeadersIndexAccountDate = new HashMap<String, Integer>();
		// mBalanceTransactions = new HashMap<Integer, Float>();
	}
	
	
	
	@SuppressWarnings({ })
	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		// take a pointer of object UI
		TextView txtDateGroup = (TextView)view.findViewById(R.id.textViewDateGroup);
		TextView txtStatus = (TextView)view.findViewById(R.id.textViewStatus);
		TextView txtDeposit = (TextView)view.findViewById(R.id.textViewDeposit);
		TextView txtTransfer = (TextView)view.findViewById(R.id.textViewTransfer);
		TextView txtPayee = (TextView)view.findViewById(R.id.textViewPayee);
		TextView txtAccountName = (TextView)view.findViewById(R.id.textViewAccountName);
		LinearLayout linearToAccount = (LinearLayout)view.findViewById(R.id.linearLayoutToAccount);
		TextView txtToAccountName = (TextView)view.findViewById(R.id.textViewToAccountName);
		TextView txtCategorySub = (TextView)view.findViewById(R.id.textViewCategorySub);
		ImageView imgFollowUp = (ImageView)view.findViewById(R.id.imageViewFollowUp);
		TextView txtNotes = (TextView)view.findViewById(R.id.textViewNotes);
		TextView txtBalance = (TextView)view.findViewById(R.id.textViewBalance);
		// header index
		if (!mHeadersAccountIndex.containsKey(cursor.getInt(cursor.getColumnIndex(QueryAllData.ACCOUNTID)))) {
			mHeadersAccountIndex.put(cursor.getInt(cursor.getColumnIndex(QueryAllData.ACCOUNTID)), cursor.getPosition());
		}
		// compose class 
		String group = Integer.toString(getAccountId() == -1 ? cursor.getInt(cursor.getColumnIndex(QueryAllData.ACCOUNTID)) : getAccountId()) + "#" + cursor.getString(cursor.getColumnIndex(QueryAllData.Date));
		if (!mHeadersIndexAccountDate.containsKey(group)) {
			mHeadersIndexAccountDate.put(group, cursor.getPosition());
		}
		// write status
		txtStatus.setText(mApplication.getStatusAsString(cursor.getString(cursor.getColumnIndex(QueryAllData.Status))));
		// show follow up icon
		if ("F".equalsIgnoreCase(cursor.getString(cursor.getColumnIndex(QueryAllData.Status)))) {
			imgFollowUp.setVisibility(View.VISIBLE);
		} else {
			imgFollowUp.setVisibility(View.GONE);
		}
		// take transaction amount
		float amount = cursor.getFloat(cursor.getColumnIndex(QueryAllData.Amount));
		// manage transfer and change amount sign
		if ((cursor.getString(cursor.getColumnIndex(QueryAllData.TransactionType)) != null) &&
			(cursor.getString(cursor.getColumnIndex(QueryAllData.TransactionType)).equals("Transfer")))  {
			if (getAccountId() != cursor.getInt(cursor.getColumnIndex(QueryAllData.ToAccountID))) {
				amount = -(amount); // -total
			} else if (getAccountId() == cursor.getInt(cursor.getColumnIndex(QueryAllData.ToAccountID))) {
				amount = cursor.getFloat(cursor.getColumnIndex(QueryAllData.TOTRANSAMOUNT)); // to account = account
			}
		}
		// set currency id
		setCurrencyId(cursor.getInt(cursor.getColumnIndex(QueryAllData.CURRENCYID)));
		// check amount sign
		if (amount > 0) {
			txtDeposit.setText(mApplication.getCurrencyFormatted(getCurrencyId(), amount));
			txtTransfer.setText(null);
		} else {
			txtTransfer.setText(mApplication.getCurrencyFormatted(getCurrencyId(), amount));
			txtDeposit.setText(null);
		}
		// compose payee description
		String payee = cursor.getString(cursor.getColumnIndex(QueryAllData.Payee));
		// write payee
		if ((!TextUtils.isEmpty(payee))) {
			txtPayee.setText(payee);
			txtPayee.setVisibility(View.VISIBLE);
		} else {
			txtPayee.setVisibility(View.GONE);
		}
		// compose account name
		if (isShowAccountName()) {
			if (mHeadersAccountIndex.containsValue(cursor.getPosition())) {
				txtAccountName.setText(cursor.getString(cursor.getColumnIndex(QueryAllData.AccountName)));
				txtAccountName.setVisibility(View.VISIBLE);
			} else {
				txtAccountName.setVisibility(View.GONE);
			}
		} else {
			txtAccountName.setVisibility(View.GONE);
		}
		// write ToAccountName
		if ((!TextUtils.isEmpty(cursor.getString(cursor.getColumnIndex(QueryAllData.ToAccountName))))) {
			if (getAccountId() != cursor.getInt(cursor.getColumnIndex(QueryAllData.ToAccountID)))
				txtToAccountName.setText(cursor.getString(cursor.getColumnIndex(QueryAllData.ToAccountName)));
			else
				txtToAccountName.setText(cursor.getString(cursor.getColumnIndex(QueryAllData.AccountName)));
			linearToAccount.setVisibility(View.VISIBLE);
		} else {
			linearToAccount.setVisibility(View.GONE);
		}
		// compose category description
		String categorySub = cursor.getString(cursor.getColumnIndex(QueryAllData.Category));
		// controllo se ho anche la subcategory
		if (!(TextUtils.isEmpty(cursor.getString(cursor.getColumnIndex(QueryAllData.Subcategory))))) {
			categorySub += " : <i>" + cursor.getString(cursor.getColumnIndex(QueryAllData.Subcategory)) + "</i>";
		}
		// write category/subcategory format html
		if (TextUtils.isEmpty(categorySub) == false) {
			txtCategorySub.setText(Html.fromHtml(categorySub));
		} else {
			txtCategorySub.setText("");
		}
		// notes
		if (!TextUtils.isEmpty(cursor.getString(cursor.getColumnIndex(QueryAllData.Notes)))) {
			txtNotes.setText(Html.fromHtml("<small>" + cursor.getString(cursor.getColumnIndex(QueryAllData.Notes)) + "</small>"));
			txtNotes.setVisibility(View.VISIBLE);
		} else {
			txtNotes.setVisibility(View.GONE);
		}
		// date group
		//txtDateGroup.setText(cursor.getString(cursor.getColumnIndex(QueryAllData.UserDate)));
		if (mHeadersIndexAccountDate.containsValue(cursor.getPosition())) {
			try {
				Date date = new SimpleDateFormat("yyyy-MM-dd").parse(cursor.getString(cursor.getColumnIndex(QueryAllData.Date)));
				txtDateGroup.setText(new SimpleDateFormat("EEEE dd MMMM yyyy").format(date));
			} catch (ParseException e) {
				txtDateGroup.setText(cursor.getString(cursor.getColumnIndex(QueryAllData.UserDate)));
			}
			txtDateGroup.setVisibility(View.VISIBLE);
		} else {
			txtDateGroup.setVisibility(View.GONE);
		}
		// balance account
		if (isShowBalanceAmount() && getDatabase() != null) {
			int transId = cursor.getInt(cursor.getColumnIndex(QueryAllData.ID));
			/*if (!mBalanceTransactions.containsKey(transId)) {
				// create thread for calculate balance amount
				BalanceAmount balanceAmount = new BalanceAmount();
				balanceAmount.setAccountId(getAccountId());
				balanceAmount.setDate(cursor.getString(cursor.getColumnIndex(QueryAllData.Date)));
				balanceAmount.setTextView(txtBalance);
				balanceAmount.setContext(mContext);
				balanceAmount.setDatabase(getDatabase());
				balanceAmount.setTransId(transId);
				// execute thread
				balanceAmount.execute();
			} else {
				txtBalance.setText(mApplication.getCurrencyFormatted(getCurrencyId(), mBalanceTransactions.get(transId)));
				if (txtBalance.getVisibility() != View.VISIBLE) txtBalance.setVisibility(View.VISIBLE); 
			}*/
			// create thread for calculate balance amount
			BalanceAmount balanceAmount = new BalanceAmount();
			balanceAmount.setAccountId(getAccountId());
			balanceAmount.setDate(cursor.getString(cursor.getColumnIndex(QueryAllData.Date)));
			balanceAmount.setTextView(txtBalance);
			balanceAmount.setContext(mContext);
			balanceAmount.setDatabase(getDatabase());
			balanceAmount.setTransId(transId);
			// execute thread
			balanceAmount.execute();
		} else {
			txtBalance.setVisibility(View.GONE);
		}
	}
	
	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		return mInflater.inflate(R.layout.item_alldata_account, parent, false);
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

	private class BalanceAmount extends AsyncTask<Void, Void, Boolean> {
		private int mAccountId;
		private int mTransId;
		private String mDate;
		private TextView mTextView;
		private Context mContext;
		private float total = 0;
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
					if (cursor.getString(cursor.getColumnIndex(TableCheckingAccount.TRANSCODE)).equalsIgnoreCase("Withdrawal")) {
						total -= cursor.getFloat(cursor.getColumnIndex(TableCheckingAccount.TRANSAMOUNT));
					}
					else if (cursor.getString(cursor.getColumnIndex(TableCheckingAccount.TRANSCODE)).equalsIgnoreCase("Deposit")) {
						total += cursor.getFloat(cursor.getColumnIndex(TableCheckingAccount.TRANSAMOUNT));
					} else {
						if (cursor.getInt(cursor.getColumnIndex(TableCheckingAccount.ACCOUNTID)) == getAccountId()) {
							total -= cursor.getFloat(cursor.getColumnIndex(TableCheckingAccount.TRANSAMOUNT));
						} else {
							total += cursor.getFloat(cursor.getColumnIndex(TableCheckingAccount.TOTRANSAMOUNT));
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
				total += cursor.getFloat(cursor.getColumnIndex(TableAccountList.INITIALBAL));
			}
			cursor.close();
			return true;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			if (result && getTextView() != null) {
				getTextView().setText(mApplication.getCurrencyFormatted(getCurrencyId(), total));
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
