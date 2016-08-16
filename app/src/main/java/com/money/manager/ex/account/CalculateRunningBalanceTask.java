///*
// * Copyright (C) 2012-2016 The Android Money Manager Ex Project Team
// *
// * This program is free software; you can redistribute it and/or
// * modify it under the terms of the GNU General Public License
// * as published by the Free Software Foundation; either version 3
// * of the License, or (at your option) any later version.
// *
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU General Public License for more details.
// *
// * You should have received a copy of the GNU General Public License
// * along with this program.  If not, see <http://www.gnu.org/licenses/>.
// */
//package com.money.manager.ex.account;
//
//import android.content.Context;
//import android.database.Cursor;
//import android.database.sqlite.SQLiteDiskIOException;
//import android.os.AsyncTask;
//import android.view.View;
//import android.widget.TextView;
//
//import com.money.manager.ex.database.ITransactionEntity;
//import com.money.manager.ex.servicelayer.AccountService;
//import com.money.manager.ex.core.TransactionTypes;
//import com.money.manager.ex.currency.CurrencyService;
//import com.money.manager.ex.datalayer.AccountTransactionRepository;
//import com.money.manager.ex.domainmodel.AccountTransaction;
//
//import info.javaperformance.money.Money;
//import info.javaperformance.money.MoneyFactory;
//import timber.log.Timber;
//
///**
// * NOT USED
// * Async task that calculates and updates the amount balance in the transaction list.
// * The idea here is to keep a reference to the view and calculate the balance for each
// * transaction. This might be slow and processor intensive.
// */
//public class CalculateRunningBalanceTask
//        extends AsyncTask<Void, Void, Boolean> {
//
//    private int mAccountId;
//    private int mCurrencyId = -1;
//    private int mTransId;
//    private String mDate;
//    private TextView mTextView;
//    private Context mContext;
//    private double total = 0;
//
//    @Override
//    protected Boolean doInBackground(Void... params) {
//        try {
//            return runTask();
//        } catch (IllegalStateException | SQLiteDiskIOException ex) {
//            Timber.e(ex, "balancing amount");
//        } catch (Exception e) {
//            throw new RuntimeException("Error in Balance Amount Task", e);
//        }
//        return false;
//    }
//
//    @Override
//    protected void onPostExecute(Boolean result) {
//        if (result && getTextView() != null) {
//            CurrencyService currencyService = new CurrencyService(getContext());
//
//            getTextView().setText(currencyService.getCurrencyFormatted(getCurrencyId(), MoneyFactory.fromDouble(total)));
//            if (getTextView().getVisibility() != View.VISIBLE) getTextView().setVisibility(View.VISIBLE);
//        }
//    }
//
//    private boolean runTask() {
//        String selection = "(" + ITransactionEntity.ACCOUNTID + "=" + Integer.toString(getAccountId()) +
//                " OR " + ITransactionEntity.TOACCOUNTID + "=" + Integer.toString(getAccountId()) + ") " +
//            "AND (" + ITransactionEntity.TRANSDATE + "<'" + getDate() +
//                "' OR (" + ITransactionEntity.TRANSDATE + "='" + getDate() +
//                    "' AND " + AccountTransaction.TRANSID + "<=" + Integer.toString(getTransId()) + ")) " +
//            "AND " + ITransactionEntity.STATUS + "<>'V'";
//
//        // sorting required for the correct balance calculation.
//        String sort = ITransactionEntity.TRANSDATE + " DESC, " +
//            ITransactionEntity.TRANSCODE + ", " +
//            AccountTransaction.TRANSID + " DESC";
//
//        AccountTransactionRepository repo = new AccountTransactionRepository(mContext);
//
//        Cursor cursor = mContext.getContentResolver().query(repo.getUri(),
//                repo.getAllColumns(),
//                selection, null,
//                sort);
//
//        if (cursor != null) {
//            while (cursor.moveToNext()) {
//                String transType = cursor.getString(cursor.getColumnIndex(ITransactionEntity.TRANSCODE));
//
//                // Some users have invalid Transaction Type. Should we check .contains()?
//
//                switch (TransactionTypes.valueOf(transType)) {
//                    case Withdrawal:
//                        total -= cursor.getDouble(cursor.getColumnIndex(ITransactionEntity.TRANSAMOUNT));
//                        break;
//                    case Deposit:
//                        total += cursor.getDouble(cursor.getColumnIndex(ITransactionEntity.TRANSAMOUNT));
//                        break;
//                    case Transfer:
//                        if (cursor.getInt(cursor.getColumnIndex(ITransactionEntity.ACCOUNTID)) == getAccountId()) {
//                            total -= cursor.getDouble(cursor.getColumnIndex(ITransactionEntity.TRANSAMOUNT));
//                        } else {
//                            total += cursor.getDouble(cursor.getColumnIndex(ITransactionEntity.TOTRANSAMOUNT));
//                        }
//                        break;
//                }
//            }
//
//            cursor.close();
//        }
//
//        // Retrieve initial balance.
//        AccountService accountService = new AccountService(mContext);
//        Money initialBalance = accountService.loadInitialBalance(getAccountId());
//        total += initialBalance.toDouble();
//
//        return true;
//    }
//
//    /**
//     * @return the accountId
//     */
//    public int getAccountId() {
//        return mAccountId;
//    }
//
//    /**
//     * @param mAccountId the accountId to set
//     */
//    public void setAccountId(int mAccountId) {
//        this.mAccountId = mAccountId;
//    }
//
//    /**
//     * @return the mDate
//     */
//    public String getDate() {
//        return mDate;
//    }
//
//    /**
//     * @param mDate the mDate to set
//     */
//    public void setDate(String mDate) {
//        this.mDate = mDate;
//    }
//
//    /**
//     * @return the mTextView
//     */
//    public TextView getTextView() {
//        return mTextView;
//    }
//
//    /**
//     * @param mTextView the mTextView to set
//     */
//    public void setTextView(TextView mTextView) {
//        this.mTextView = mTextView;
//    }
//
//    /**
//     * @return the context
//     */
////    @SuppressWarnings("unused")
//    public Context getContext() {
//        return mContext;
//    }
//
//    /**
//     * @param mContext the context to set
//     */
//    public void setContext(Context mContext) {
//        this.mContext = mContext;
//    }
//
//    /**
//     * @return the mTransId
//     */
//    public int getTransId() {
//        return mTransId;
//    }
//
//    /**
//     * @param mTransId the mTransId to set
//     */
//    public void setTransId(int mTransId) {
//        this.mTransId = mTransId;
//    }
//
//    public int getCurrencyId() {
//        return mCurrencyId;
//    }
//
//    public void setCurrencyId(int mCurrencyId) {
//        this.mCurrencyId = mCurrencyId;
//    }
//
//}
