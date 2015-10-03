/*
 * Copyright (C) 2012-2015 The Android Money Manager Ex Project Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.money.manager.ex.datalayer;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;

import com.money.manager.ex.database.DatasetType;
import com.money.manager.ex.database.QueryAccountBills;
import com.money.manager.ex.database.TableAccountList;
import com.money.manager.ex.database.WhereStatementGenerator;
import com.money.manager.ex.domainmodel.Account;

/**
 * Repository for Accounts
 */
public class AccountRepository
    extends RepositoryBase {

    public AccountRepository(Context context) {
        super(context, "accountlist_v1", DatasetType.TABLE, "accountlist");

    }

    @Override
    public String[] getAllColumns() {
        return new String[] { "ACCOUNTID AS _id", Account.ACCOUNTID, Account.ACCOUNTNAME,
                Account.ACCOUNTTYPE, Account.ACCOUNTNUM, Account.STATUS, Account.NOTES,
                Account.HELDAT, Account.WEBSITE, Account.CONTACTINFO, Account.ACCESSINFO,
                Account.INITIALBAL, Account.FAVORITEACCT, Account.CURRENCYID };
    }

    public Account load(int accountId) {
        WhereStatementGenerator where = new WhereStatementGenerator();
        where.addStatement(Account.ACCOUNTID, "=", accountId);

        return query(where.getWhere());
    }

    public boolean delete(int accountId) {
        int result = delete(Account.ACCOUNTID + "=?", new String[] { Integer.toString(accountId)});
        return result > 0;
    }

    /**
     * Loads account data with balances.
     * @param accountId Id of the account to load.
     * @return QueryAccountBills entity.
     */
    public QueryAccountBills loadAccountBills(int accountId) {
        QueryAccountBills result = new QueryAccountBills(context);

        String selection = QueryAccountBills.ACCOUNTID + "=?";

        Cursor cursor = context.getContentResolver().query(
                result.getUri(),
                result.getAllColumns(),
                selection,
                new String[] { Integer.toString(accountId) },
                null);
        if (cursor == null) return null;

        if (cursor.moveToFirst()) {
            result.setValueFromCursor(cursor);
        }

        cursor.close();

        return result;
    }

    public int loadIdByName(String name) {
        int result = -1;

        if(TextUtils.isEmpty(name)) { return result; }

        String selection = Account.ACCOUNTNAME + "=?";

        Cursor cursor = context.getContentResolver().query(
                this.getUri(),
                new String[] { Account.ACCOUNTID },
                selection,
                new String[] { name },
                null);
        if (cursor == null) return result;

        if(cursor.moveToFirst()) {
            result = cursor.getInt(cursor.getColumnIndex(Account.ACCOUNTID));
        }

        cursor.close();

        return result;
    }

    public String loadName(int id) {
        String name = null;

        Cursor cursor = openCursor(new String[] { Account.ACCOUNTNAME },
                Account.ACCOUNTID + "=?",
                new String[]{Integer.toString(id)}
        );
        if (cursor == null) return null;

        if (cursor.moveToFirst()) {
            name = cursor.getString(cursor.getColumnIndex(Account.ACCOUNTNAME));
        }
        cursor.close();

        return name;
    }

    public Account query(String selection) {
        return query(null, selection, null);
    }

    public Account query(String[] projection, String selection, String[] args) {
        Cursor c = openCursor(projection, selection, args);

        if (c == null) return null;

        Account account = null;

        if (c.moveToNext()) {
            account = new Account();
            account.loadFromCursor(c);
        }

        c.close();

        return account;
    }
}
