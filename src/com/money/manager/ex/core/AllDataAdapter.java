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

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.money.manager.ex.MoneyManagerApplication;
import com.money.manager.ex.R;
import com.money.manager.ex.database.QueryAllData;

public class AllDataAdapter extends CursorAdapter {
	private LayoutInflater inflater;
	private MoneyManagerApplication application;
	private boolean showAccountName = false;
	
	public AllDataAdapter(Context context, Cursor c) {
		this(context, c, false);
	}
	
	@SuppressWarnings("deprecation")
	public AllDataAdapter(Context context, Cursor c, boolean showAccountName) {
		super(context, c);
		inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		application = (MoneyManagerApplication) context.getApplicationContext();
		this.showAccountName = showAccountName;
	}
	
	@SuppressWarnings({ })
	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		// take a pointer of object UI
		TextView txtDate = (TextView)view.findViewById(R.id.textViewDate);
		TextView txtStatus = (TextView)view.findViewById(R.id.textViewStatus);
		TextView txtDeposit = (TextView)view.findViewById(R.id.textViewDeposit);
		TextView txtTransfer = (TextView)view.findViewById(R.id.textViewTransfer);
		TextView txtPayee = (TextView)view.findViewById(R.id.textViewPayee);
		TextView txtAccountName = (TextView)view.findViewById(R.id.textViewAccountName);
		LinearLayout linearToAccount = (LinearLayout)view.findViewById(R.id.linearLayoutToAccount);
		TextView txtToAccountName = (TextView)view.findViewById(R.id.textViewToAccountName);
		TextView txtCategorySub = (TextView)view.findViewById(R.id.textViewCategorySub);
		// write status and date
		txtDate.setText(cursor.getString(cursor.getColumnIndex(QueryAllData.UserDate)));
		txtStatus.setText(application.getStatusAsString(cursor.getString(cursor.getColumnIndex(QueryAllData.Status))));
		// take transaction amount
		float amount = cursor.getFloat(cursor.getColumnIndex(QueryAllData.Amount));
		// manage transfer and change amount sign
		if ((cursor.getString(cursor.getColumnIndex(QueryAllData.TransactionType)) != null) &&
			(cursor.getString(cursor.getColumnIndex(QueryAllData.TransactionType)).equals("Transfer")))  {
			if (cursor.getInt(cursor.getColumnIndex(QueryAllData.ACCOUNTID)) != cursor.getInt(cursor.getColumnIndex(QueryAllData.ToAccountID))) {
				amount = -(amount); // -total
			} else if (cursor.getInt(cursor.getColumnIndex(QueryAllData.ACCOUNTID)) == cursor.getInt(cursor.getColumnIndex(QueryAllData.ToAccountID))) {
				amount = cursor.getFloat(cursor.getColumnIndex(QueryAllData.TOTRANSAMOUNT)); // to account = account
			}
		}
		// check amount sign
		if (amount > 0) {
			txtDeposit.setText(application.getCurrencyFormatted(cursor.getInt(cursor.getColumnIndex(QueryAllData.CURRENCYID)), amount));
			txtTransfer.setText(null);
		} else {
			txtTransfer.setText(application.getCurrencyFormatted(cursor.getInt(cursor.getColumnIndex(QueryAllData.CURRENCYID)), amount));
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
		if (showAccountName) {
			txtAccountName.setText(cursor.getString(cursor.getColumnIndex(QueryAllData.AccountName)));
			txtAccountName.setVisibility(View.VISIBLE);
		}
		// write ToAccountName
		if ((!TextUtils.isEmpty(cursor.getString(cursor.getColumnIndex(QueryAllData.ToAccountName))))) {
			txtToAccountName.setText(cursor.getString(cursor.getColumnIndex(QueryAllData.ToAccountName)));
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
	}
	
	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		return inflater.inflate(R.layout.item_alldata_account, parent, false);
	}
}
