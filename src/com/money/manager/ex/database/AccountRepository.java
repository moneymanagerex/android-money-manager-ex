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
package com.money.manager.ex.database;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.text.TextUtils;

import com.money.manager.ex.core.AccountTypes;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository for Accounts
 */
public class AccountRepository {
    public AccountRepository(Context context) {
        mContext = context;
        mAccount = new TableAccountList();
    }

    private Context mContext;
    private TableAccountList mAccount;

    public TableAccountList load(int accountId) {
        TableAccountList result = new TableAccountList();

        String selection = TableAccountList.ACCOUNTID + "=?";

        Cursor cursor = mContext.getContentResolver().query(
                mAccount.getUri(),
                mAccount.getAllColumns(),
                selection,
                new String[] { Integer.toString(accountId) },
                null);

        if (cursor.moveToFirst()) {
            result.setValueFromCursor(cursor);
        }

        cursor.close();

        return result;
    }

    public int loadIdByName(String name) {
        int result = -1;

        if(TextUtils.isEmpty(name)) { return result; }

        String selection = TableAccountList.ACCOUNTNAME + "=?";

        Cursor cursor = mContext.getContentResolver().query(
                mAccount.getUri(),
                new String[] { TableAccountList.ACCOUNTID },
                selection,
                new String[] { name },
                null);

        if(cursor.moveToFirst()) {
            result = cursor.getInt(cursor.getColumnIndex(TableAccountList.ACCOUNTID));
        }

        cursor.close();

        return result;
    }

    public String loadName(int id) {
        String name = null;

        Cursor cursor = mContext.getContentResolver().query(mAccount.getUri(),
                new String[] { TableAccountList.ACCOUNTNAME },
                TableAccountList.ACCOUNTID + "=?",
                new String[]{Integer.toString(id)},
                null);

        if ((cursor != null) && cursor.moveToFirst()) {
            name = cursor.getString(cursor.getColumnIndex(TableAccountList.ACCOUNTNAME));
            cursor.close();
        }

        return name;
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

    public List<AccountTypes> getTransactionAccountTypes() {
        List<AccountTypes> list = new ArrayList<>();

        list.add(AccountTypes.CHECKING);
        list.add(AccountTypes.TERM);
        list.add(AccountTypes.CREDIT_CARD);

        return list;
    }

    public List<String> getTransactionAccountTypeNames() {
        List<String> accountTypeNames = new ArrayList<>();
        List<AccountTypes> accountTypes = getTransactionAccountTypes();

        for (AccountTypes type : accountTypes) {
            accountTypeNames.add(type.toString());
        }

        return accountTypeNames;
    }

    public List<TableAccountList> getTransactionAccounts(boolean open, boolean favorite) {
        List<String> accountTypeNames = getTransactionAccountTypeNames();

        List<TableAccountList> result = loadAccounts(open, favorite, accountTypeNames);

        return result;
    }

    public List<TableAccountList> loadAccounts(boolean open, boolean favorite, List<String> accountTypes) {
        List<TableAccountList> result;
//        result = loadAccounts_sql(open, favorite, accountTypes);
        result = loadAccounts_content(open, favorite, accountTypes);

        return result;
    }

    public Cursor getCursor(boolean open, boolean favorite, List<String> accountTypes) {
        // compose where clause
        String where = getWhereFilterFor(open, favorite);
        // filter accounts.
        if (accountTypes != null && accountTypes.size() > 0) {
            where = DatabaseUtils.concatenateWhere(where, getWherePartFor(accountTypes));
        }

        // use context provider instead of direct SQLite Database access.
        Cursor cursor = mContext.getContentResolver().query(mAccount.getUri(),
                mAccount.getAllColumns(),
                where,
                null,
                mAccount.ACCOUNTNAME
        );
        return cursor;
    }

    // Private section

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
        where.append(mAccount.ACCOUNTTYPE);
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
}
