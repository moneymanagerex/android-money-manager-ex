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
package com.money.manager.ex.businessobjects;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDiskIOException;

import com.money.manager.ex.Constants;
import com.money.manager.ex.core.ExceptionHandler;
import com.money.manager.ex.core.TransactionTypes;
import com.money.manager.ex.database.ISplitTransactionsDataset;
import com.money.manager.ex.database.TableAccountList;
import com.money.manager.ex.database.TableCheckingAccount;
import com.money.manager.ex.database.WhereClauseGenerator;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Various business logic pieces related to Account(s).
 */
public class AccountService {

    public AccountService(Context context) {
        mContext = context;
    }

//    public static final int NO_ACCOUNT = -1;

    private Context mContext;

    /**
     * @param id account id to be search
     * @return TableAccountList, return null if account id not find
     */
    public TableAccountList getTableAccountList(int id) {
        TableAccountList account = null;
        try {
            account = loadAccount(id);
        } catch (SQLiteDiskIOException | IllegalStateException ex) {
            ExceptionHandler handler = new ExceptionHandler(mContext, this);
            handler.handle(ex, "loading account: " + Integer.toString(id));
        }
        return account;
    }

    /**
     * Calculate simple balance by adding together all transactions before and on the
     * given date. To get the real balance, this amount should be subtracted from the
     * account initial balance.
     * @param isoDate date in ISO format
     */
    public double calculateBalanceOn(int accountId, String isoDate) {
        double total = 0;

        TableCheckingAccount entity = new TableCheckingAccount();
        WhereClauseGenerator generator = new WhereClauseGenerator(mContext);

        // load all transactions on the account before and on given date.

        generator.addSelection(ISplitTransactionsDataset.ACCOUNTID, "=", Integer.toString(accountId));
//        SimpleDateFormat isoDate = new SimpleDateFormat(Constants.PATTERN_DB_DATE);
        generator.addSelection(ISplitTransactionsDataset.TRANSDATE, "<=", isoDate);

        String selection = generator.getSelectionStatements();
        String[] args = generator.getSelectionArguments();

        Cursor cursor = mContext.getContentResolver().query(entity.getUri(),
                null,
                selection,
                args,
                null, null);
        if (cursor == null) return total;

        // calculate balance.
        while (cursor.moveToNext()) {
            String transType = cursor.getString(cursor.getColumnIndex(ISplitTransactionsDataset.TRANSCODE));

            // Some users have invalid Transaction Type. Should we check .contains()?

            switch (TransactionTypes.valueOf(transType)) {
                case Withdrawal:
                    total -= cursor.getDouble(cursor.getColumnIndex(ISplitTransactionsDataset.TRANSAMOUNT));
                    break;
                case Deposit:
                    total += cursor.getDouble(cursor.getColumnIndex(ISplitTransactionsDataset.TRANSAMOUNT));
                    break;
                case Transfer:
                    if (cursor.getInt(cursor.getColumnIndex(ISplitTransactionsDataset.ACCOUNTID)) == accountId) {
                        total -= cursor.getDouble(cursor.getColumnIndex(ISplitTransactionsDataset.TRANSAMOUNT));
                    } else {
                        total += cursor.getDouble(cursor.getColumnIndex(ISplitTransactionsDataset.TOTRANSAMOUNT));
                    }
                    break;
            }
        }

        cursor.close();
        return total;
    }

    public double loadInitialBalance(int accountId) {
        double initialBalance = 0;

        TableAccountList accountList = new TableAccountList();

        Cursor cursor = mContext.getContentResolver().query(accountList.getUri(),
                accountList.getAllColumns(),
                TableAccountList.ACCOUNTID + "=?",
                new String[] { Integer.toString(accountId) },
                null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                initialBalance = cursor.getDouble(cursor.getColumnIndex(TableAccountList.INITIALBAL));
            }
            cursor.close();
        }

        return initialBalance;
    }

    private TableAccountList loadAccount(int id) {
        TableAccountList account = new TableAccountList();
        String selection = TableAccountList.ACCOUNTID + "=?";

        Cursor cursor = mContext.getContentResolver().query(account.getUri(),
                null,
                selection,
                new String[]{Integer.toString(id)},
                null, null);
        if (cursor == null) return null;

        // check if cursor is valid
        if (cursor.moveToFirst()) {
            account = new TableAccountList();
            account.setValueFromCursor(cursor);

            cursor.close();
        }

        return account;
    }
}
