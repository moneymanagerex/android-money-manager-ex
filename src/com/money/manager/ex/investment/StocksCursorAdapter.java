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
package com.money.manager.ex.investment;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
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
import com.money.manager.ex.R;
import com.money.manager.ex.businessobjects.StockRepository;
import com.money.manager.ex.database.QueryBillDeposits;
import com.money.manager.ex.database.TransactionStatus;
import com.money.manager.ex.utils.CurrencyUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

/**
 *
 */
public class StocksCursorAdapter
        extends CursorAdapter {

    public StocksCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, -1);

        this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mHeadersAccountIndex = new HashMap<>();
        mCheckedPosition = new SparseBooleanArray();
        mContext = context;
    }

    private SQLiteDatabase mDatabase;
    private LayoutInflater mInflater;
    private HashMap<Integer, Integer> mHeadersAccountIndex;
    private SparseBooleanArray mCheckedPosition;
    private int mAccountId = -1;

    private boolean mShowAccountName = false;
    private boolean mShowBalanceAmount = false;

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = mInflater.inflate(R.layout.item_alldata_account, parent, false);
        // holder
        StocksDataViewHolder holder = new StocksDataViewHolder();
        // take a pointer of object UI
        holder.linDate = (LinearLayout) view.findViewById(R.id.linearLayoutDate);
        holder.txtDay = (TextView) view.findViewById(R.id.textViewDay);
        holder.txtMonth = (TextView) view.findViewById(R.id.textViewMonth);
        holder.txtYear = (TextView) view.findViewById(R.id.textViewYear);
        holder.txtStatus = (TextView) view.findViewById(R.id.textViewStatus);
        holder.txtAmount = (TextView) view.findViewById(R.id.textViewAmount);
        holder.txtPayee = (TextView) view.findViewById(R.id.textViewPayee);
        holder.txtAccountName = (TextView) view.findViewById(R.id.textViewAccountName);
        holder.txtCategorySub = (TextView) view.findViewById(R.id.textViewCategorySub);
        holder.txtNotes = (TextView) view.findViewById(R.id.textViewNotes);
        holder.txtBalance = (TextView) view.findViewById(R.id.textViewBalance);
        // set holder to view
        view.setTag(holder);

        return view;    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // take a holder
        StocksDataViewHolder holder = (StocksDataViewHolder) view.getTag();

        // header index
        int accountId = cursor.getInt(cursor.getColumnIndex(StockRepository.HELDAT));
        if (!mHeadersAccountIndex.containsKey(accountId)) {
            mHeadersAccountIndex.put(accountId, cursor.getPosition());
        }

//        // write status
//        String status = cursor.getString(cursor.getColumnIndex(STATUS));
//        holder.txtStatus.setText(TransactionStatus.getStatusAsString(mContext, status));
//        // color status
//        int colorBackground = TransactionStatus.getBackgroundColorFromStatus(mContext, status);
//        holder.linDate.setBackgroundColor(colorBackground);
//        holder.txtStatus.setTextColor(Color.GRAY);

//        // date group
//        try {
//            Locale locale = mContext.getResources().getConfiguration().locale;
//
//            Date date = new SimpleDateFormat(Constants.PATTERN_DB_DATE, locale)
//                    .parse(cursor.getString(cursor.getColumnIndex(DATE)));
//            holder.txtMonth.setText(new SimpleDateFormat("MMM", locale).format(date));
//            holder.txtYear.setText(new SimpleDateFormat("yyyy", locale).format(date));
//            holder.txtDay.setText(new SimpleDateFormat("dd", locale).format(date));
//        } catch (ParseException e) {
//            Log.e(StocksCursorAdapter.class.getSimpleName(), e.getMessage());
//        }

        // take transaction amount
//        double amount = cursor.getDouble(cursor.getColumnIndex(AMOUNT));
//        // set currency id
//        setCurrencyId(cursor.getInt(cursor.getColumnIndex(CURRENCYID)));
//        // manage transfer and change amount sign
//        if ((cursor.getString(cursor.getColumnIndex(TRANSACTIONTYPE)) != null) &&
//                (Constants.TRANSACTION_TYPE_TRANSFER.equalsIgnoreCase(cursor.getString(cursor.getColumnIndex(TRANSACTIONTYPE))))) {
//            if (getAccountId() != cursor.getInt(cursor.getColumnIndex(TOACCOUNTID))) {
//                amount = -(amount); // -total
//            } else if (getAccountId() == cursor.getInt(cursor.getColumnIndex(TOACCOUNTID))) {
//                amount = cursor.getDouble(cursor.getColumnIndex(TOTRANSAMOUNT)); // to account = account
//                setCurrencyId(cursor.getInt(cursor.getColumnIndex(TOCURRENCYID)));
//            }
//        }
//        // check amount sign
//        CurrencyUtils currencyUtils = new CurrencyUtils(mContext);
//        holder.txtAmount.setText(currencyUtils.getCurrencyFormatted(getCurrencyId(), amount));
//        // text color amount
//        if (Constants.TRANSACTION_TYPE_TRANSFER.equalsIgnoreCase(cursor.getString(cursor.getColumnIndex(TRANSACTIONTYPE)))) {
//            holder.txtAmount.setTextColor(mContext.getResources().getColor(R.color.material_grey_700));
//        } else if (Constants.TRANSACTION_TYPE_DEPOSIT.equalsIgnoreCase(cursor.getString(cursor.getColumnIndex(TRANSACTIONTYPE)))) {
//            holder.txtAmount.setTextColor(mContext.getResources().getColor(R.color.material_green_700));
//        } else {
//            holder.txtAmount.setTextColor(mContext.getResources().getColor(R.color.material_red_700));
//        }

//        // compose payee description
//        holder.txtPayee.setText(cursor.getString(cursor.getColumnIndex(PAYEE)));
//        // compose account name
//        if (isShowAccountName()) {
//            if (mHeadersAccountIndex.containsValue(cursor.getPosition())) {
//                holder.txtAccountName.setText(cursor.getString(cursor.getColumnIndex(ACCOUNTNAME)));
//                holder.txtAccountName.setVisibility(View.VISIBLE);
//            } else {
//                holder.txtAccountName.setVisibility(View.GONE);
//            }
//        } else {
//            holder.txtAccountName.setVisibility(View.GONE);
//        }
//        // write ToAccountName
//        if ((!TextUtils.isEmpty(cursor.getString(cursor.getColumnIndex(TOACCOUNTNAME))))) {
//            if (getAccountId() != cursor.getInt(cursor.getColumnIndex(TOACCOUNTID)))
//                holder.txtPayee.setText(cursor.getString(cursor.getColumnIndex(TOACCOUNTNAME)));
//            else
//                holder.txtPayee.setText(cursor.getString(cursor.getColumnIndex(ACCOUNTNAME)));
//        }

//        // compose category description
//        String categorySub = cursor.getString(cursor.getColumnIndex(CATEGORY));
//        // check sub category
//        if (!(TextUtils.isEmpty(cursor.getString(cursor.getColumnIndex(SUBCATEGORY))))) {
//            categorySub += " : <i>" + cursor.getString(cursor.getColumnIndex(SUBCATEGORY)) + "</i>";
//        }
//        // write category/subcategory format html
//        if (!TextUtils.isEmpty(categorySub)) {
//            // Display category/sub-category.
//            holder.txtCategorySub.setText(Html.fromHtml(categorySub));
//        } else {
//            // Must be a split category.
//            holder.txtCategorySub.setText(R.string.split_category);
//        }

        // notes
//        if (!TextUtils.isEmpty(cursor.getString(cursor.getColumnIndex(NOTES)))) {
//            holder.txtNotes.setText(Html.fromHtml("<small>" + cursor.getString(cursor.getColumnIndex(NOTES)) + "</small>"));
//            holder.txtNotes.setVisibility(View.VISIBLE);
//        } else {
//            holder.txtNotes.setVisibility(View.GONE);
//        }
        // check if item is checked
        if (mCheckedPosition.get(cursor.getPosition(), false)) {
            view.setBackgroundResource(R.color.material_green_100);
        } else {
            view.setBackgroundResource(android.R.color.transparent);
        }
        // balance account
//        if (isShowBalanceAmount() && getDatabase() != null) {
//            int transId = cursor.getInt(cursor.getColumnIndex(ID));
//            // create thread for calculate balance amount
//            BalanceAmount balanceAmount = new BalanceAmount();
//            balanceAmount.setAccountId(getAccountId());
//            balanceAmount.setDate(cursor.getString(cursor.getColumnIndex(DATE)));
//            balanceAmount.setTextView(holder.txtBalance);
//            balanceAmount.setContext(mContext);
//            balanceAmount.setDatabase(getDatabase());
//            balanceAmount.setTransId(transId);
//            // execute thread
//            balanceAmount.execute();
//        } else {
//            holder.txtBalance.setVisibility(View.GONE);
//        }
    }

    public void setDatabase(SQLiteDatabase mDatabase) {
        this.mDatabase = mDatabase;
    }

    public void resetAccountHeaderIndexes() {
        if (mHeadersAccountIndex != null) mHeadersAccountIndex.clear();
    }

    public void setAccountId(int mAccountId) {
        this.mAccountId = mAccountId;
    }

    public void setShowAccountName(boolean showAccountName) {
        this.mShowAccountName = showAccountName;
    }

    public void setShowBalanceAmount(boolean mShowBalanceAmount) {
        this.mShowBalanceAmount = mShowBalanceAmount;
    }

}
