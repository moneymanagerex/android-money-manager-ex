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
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDiskIOException;
import android.util.Log;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

import com.money.manager.ex.Constants;
import com.money.manager.ex.core.AccountTypes;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.core.ExceptionHandler;
import com.money.manager.ex.core.TransactionTypes;
import com.money.manager.ex.database.AccountRepository;
import com.money.manager.ex.database.ISplitTransactionsDataset;
import com.money.manager.ex.database.MoneyManagerOpenHelper;
import com.money.manager.ex.database.QueryAccountBills;
import com.money.manager.ex.database.TableAccountList;
import com.money.manager.ex.database.TableCheckingAccount;
import com.money.manager.ex.database.WhereClauseGenerator;
import com.money.manager.ex.settings.AppSettings;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
     * Loads account list, applying the current preferences for Open & Favourite accounts.
     * @return List of accounts
     */
    public List<TableAccountList> getAccountList() {
        AppSettings settings = new AppSettings(mContext);

        boolean favourite = settings.getLookAndFeelSettings().getViewFavouriteAccounts();
        boolean open = settings.getLookAndFeelSettings().getViewOpenAccounts();

        return getAccountList(open, favourite);
    }

    /**
     * Load account list with given parameters.
     * Includes all account types.
     * @param open     show open accounts
     * @param favorite show favorite account
     * @return List<TableAccountList> list of accounts selected
     */
    public List<TableAccountList> getAccountList(boolean open, boolean favorite) {
        // create a return list
        List<TableAccountList> listAccount = loadAccounts(open, favorite, null);
        return listAccount;
    }

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

    public List<TableAccountList> loadAccounts(boolean open, boolean favorite, List<String> accountTypes) {
        List<TableAccountList> result;
//        result = loadAccounts_sql(open, favorite, accountTypes);
        result = loadAccounts_content(open, favorite, accountTypes);

        return result;
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

    /**
     * Loads account details with balances.
     * Needs to be better organized to limit the where clause.
     * @param where selection criteria for Query Account Bills
     * @param args arguments for the selection criteria
     * @return current balance in the currency of the account.
     */
    public double loadBalance(String where, String[] args) {
        double curTotal = 0;

        QueryAccountBills accountBills = new QueryAccountBills(mContext);
        Cursor cursor = mContext.getContentResolver().query(accountBills.getUri(),
                null,
                where, args,
                null);
        if (cursor == null) return 0;

        // calculate summary
        while (cursor.moveToNext()) {
            curTotal = curTotal + cursor.getDouble(cursor.getColumnIndex(QueryAccountBills.TOTAL));
        }
        cursor.close();

        return curTotal;

    }

    public Cursor getCursor(boolean open, boolean favorite, List<String> accountTypes) {
        try {
            return getCursorInternal(open, favorite, accountTypes);
        } catch (Exception ex) {
            ExceptionHandler handler = new ExceptionHandler(mContext, this);
            handler.handle(ex, "getting cursor in account repository");
        }
        return null;
    }

    public List<String> getTransactionAccountTypeNames() {
        List<String> accountTypeNames = new ArrayList<>();
        List<AccountTypes> accountTypes = getTransactionAccountTypes();

        for (AccountTypes type : accountTypes) {
            accountTypeNames.add(type.toString());
        }

        return accountTypeNames;
    }

    public List<AccountTypes> getTransactionAccountTypes() {
        List<AccountTypes> list = new ArrayList<>();

        list.add(AccountTypes.CHECKING);
        list.add(AccountTypes.TERM);
        list.add(AccountTypes.CREDIT_CARD);

        return list;
    }

    public List<TableAccountList> getTransactionAccounts(boolean open, boolean favorite) {
        List<String> accountTypeNames = getTransactionAccountTypeNames();

        List<TableAccountList> result = loadAccounts(open, favorite, accountTypeNames);

        return result;
    }

    private Cursor getCursorInternal(boolean open, boolean favorite, List<String> accountTypes) {
        TableAccountList account = new TableAccountList();

        // compose where clause
        String where = getWhereFilterFor(open, favorite);
        // filter accounts.
        if (accountTypes != null && accountTypes.size() > 0) {
            where = DatabaseUtils.concatenateWhere(where, getWherePartFor(accountTypes));
        }

        // use context provider instead of direct SQLite Database access.
        Cursor cursor = mContext.getContentResolver().query(account.getUri(),
                account.getAllColumns(),
                where,
                null,
                TableAccountList.ACCOUNTNAME
        );
        return cursor;
    }

    private String getWhereFilterFor(boolean open, boolean favorite) {
        StringBuilder where = new StringBuilder();

        if (open) {
            where.append("LOWER(STATUS)='open'");
        }
        if (favorite) {
            if (open) {
                where.append(" AND ");
            }
            where.append("LOWER(FAVORITEACCT)='true'");
        }

        return where.toString();
    }

    private String getWherePartFor(List<String> accountTypes) {
        StringBuilder where = new StringBuilder();
        where.append(TableAccountList.ACCOUNTTYPE);
        where.append(" IN (");
        for(String type : accountTypes) {
            if (accountTypes.indexOf(type) > 0) {
                where.append(',');
            }

            where.append("'");
            where.append(type);
            where.append("'");
        }
        where.append(")");

        return where.toString();
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

    private List<TableAccountList> loadAccounts_content(boolean open, boolean favorite, List<String> accountTypes) {
        List<TableAccountList> result = new ArrayList<>();

        Cursor cursor = getCursor(open, favorite, accountTypes);
        if (cursor == null) return null;

//        for(cursor.moveToFirst(); cursor.isAfterLast(); cursor.moveToNext()) {
        while (cursor.moveToNext()) {
            TableAccountList account = new TableAccountList();
            account.setValueFromCursor(cursor);
            result.add(account);
        }
        cursor.close();

        return result;
    }

    /**
     * Load accounts with these filters.
     * Here the account types can be specified.
     * @param open
     * @param favorite
     * @param accountTypes
     * @return
     */
    private List<TableAccountList> loadAccounts_sql(boolean open, boolean favorite, List<String> accountTypes) {
        // create a return list
        List<TableAccountList> listAccount = new ArrayList<>();

        // compose where clause
        String where = getWhereFilterFor(open,favorite);
        // filter accounts.
        if (accountTypes != null && accountTypes.size() > 0) {
            where = DatabaseUtils.concatenateWhere(where, getWherePartFor(accountTypes));
        }

        // data cursor
        TableAccountList tAccountList = new TableAccountList();
        MoneyManagerOpenHelper helper = MoneyManagerOpenHelper.getInstance(mContext.getApplicationContext());
//        SQLiteDatabase db = helper.getReadableDatabase();
//        Cursor cursor = db.query(tAccountList.getSource(), tAccountList.getAllColumns(),
//                where, null, null, null,
//                TableAccountList.ACCOUNTNAME);
        Cursor cursor = mContext.getContentResolver().query(tAccountList.getUri(),
                tAccountList.getAllColumns(),
                where, null,
                TableAccountList.ACCOUNTNAME);
        // populate list from data cursor
        if (cursor != null) {
            while (cursor.moveToNext()) {
                TableAccountList account = new TableAccountList();
                account.setValueFromCursor(cursor);
                listAccount.add(account);
            }
            cursor.close();
        }

        return listAccount;
    }

}
