/*
 * Copyright (C) 2012-2016 The Android Money Manager Ex Project Team
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

import com.money.manager.ex.Constants;
import com.money.manager.ex.R;
import com.money.manager.ex.account.AccountStatuses;
import com.money.manager.ex.account.AccountTypes;
import com.money.manager.ex.database.DatasetType;
import com.money.manager.ex.database.QueryAccountBills;
import com.money.manager.ex.database.WhereStatementGenerator;
import com.money.manager.ex.domainmodel.Account;
import com.money.manager.ex.utils.MyDatabaseUtils;

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

    public int insert(Account entity) {
        return this.insert(entity.contentValues);
    }

    public Account load(int id) {
        if (id == Constants.NOT_SET) return null;

        WhereStatementGenerator where = new WhereStatementGenerator();
        where.addStatement(Account.ACCOUNTID, "=", id);

        return first(where.getWhere());
    }

    public boolean delete(int id) {
        int result = delete(Account.ACCOUNTID + "=?", new String[] { Integer.toString(id)});
        return result > 0;
    }

    /**
     * Loads account data with balances.
     * @param id Id of the account to load.
     * @return QueryAccountBills entity.
     */
    public QueryAccountBills loadAccountBills(int id) {
        QueryAccountBills result = new QueryAccountBills(getContext());

        String selection = QueryAccountBills.ACCOUNTID + "=?";

        Cursor cursor = getContext().getContentResolver().query(
                result.getUri(),
                result.getAllColumns(),
                selection,
                new String[] { Integer.toString(id) },
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

        Cursor cursor = getContext().getContentResolver().query(
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

    public int loadCurrencyIdFor(int id) {
        Account account = (Account) first(Account.class,
            new String[] { Account.CURRENCYID },
            Account.ACCOUNTID + "=?",
            MyDatabaseUtils.getArgsForId(id),
            null);

        if (account == null) {
            String message = this.getContext().getString(R.string.account_not_found) + " " + id;
            throw new IllegalArgumentException(message);
        }
        return account.getCurrencyId();
    }

    public String loadName(Integer id) {
        if (id == null) return null;

        String name = null;

        Cursor cursor = openCursor(new String[]{Account.ACCOUNTNAME},
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

    public Account first(String selection) {
        return (Account) first(Account.class, null, selection, null, null);
    }

    public boolean save(Account value) {
        int id = value.getId();

        WhereStatementGenerator generator = new WhereStatementGenerator();
        String where = generator.getStatement(Account.ACCOUNTID, "=", id);

        return update(value, where);
    }

    public Cursor getInvestmentAccountsCursor(boolean openOnly) {
        WhereStatementGenerator where = new WhereStatementGenerator();
        where.addStatement(Account.ACCOUNTTYPE, "=", AccountTypes.INVESTMENT.toString());
        if (openOnly) {
            where.addStatement(Account.STATUS, "=", AccountStatuses.OPEN.toString());
        }

        Cursor c = openCursor(this.getAllColumns(),
            where.getWhere(),
            null,
            "lower (" + Account.ACCOUNTNAME + ")");
        if (c == null) return null;

        return c;
    }

    public boolean anyAccountsUsingCurrency(int currencyId) {
        int links = count(Account.CURRENCYID + "=?",
                MyDatabaseUtils.getArgsForId(currencyId));
        return links > 0;
    }
}
