/*
 * Copyright (C) 2012-2018 The Android Money Manager Ex Project Team
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

import com.money.manager.ex.account.AccountTypes;
import com.money.manager.ex.database.DatasetType;
import com.money.manager.ex.database.QueryAccountBills;
import com.money.manager.ex.domainmodel.Account;
import com.money.manager.ex.utils.MmxDatabaseUtils;

import java.util.List;

/**
 * Repository for Accounts
 */
public class AccountRepository
    extends RepositoryBase<Account> {

    private static final String TABLE_NAME = "accountlist_v1";
    private static final String ID_COLUMN = Account.ACCOUNTID;
    private static final String NAME_COLUMN = Account.ACCOUNTNAME;

    public AccountRepository(Context context) {
        super(context, TABLE_NAME, DatasetType.TABLE, "accountlist", ID_COLUMN, NAME_COLUMN);
    }

    @Override
    public Account createEntity() {
        return new Account();
    }

    @Override
    public String[] getAllColumns() {
        return new String[] { ID_COLUMN + " AS _id", Account.ACCOUNTID, Account.ACCOUNTNAME,
                Account.ACCOUNTTYPE, Account.ACCOUNTNUM, Account.STATUS, Account.NOTES,
                Account.HELDAT, Account.WEBSITE, Account.CONTACTINFO, Account.ACCESSINFO,
                Account.INITIALBAL, Account.FAVORITEACCT, Account.CURRENCYID ,
                Account.INITIALDATE     // issue #2800 missing initial date in read
        };
    }

    public String getTableName() {
        return TABLE_NAME;
    }

    /**
     * Loads account data with balances.
     * @param id Id of the account to load.
     * @return QueryAccountBills entity.
     */
    public QueryAccountBills loadAccountBills(long id) {
        QueryAccountBills result = new QueryAccountBills(getContext());

        String selection = QueryAccountBills.ACCOUNTID + "=?";

        Cursor cursor = getContext().getContentResolver().query(
                result.getUri(),
                result.getAllColumns(),
                selection,
                new String[] { Long.toString(id) },
                null);
        if (cursor == null) return null;

        if (cursor.moveToFirst()) {
            result.setValueFromCursor(cursor);
        }

        cursor.close();

        return result;
    }

    public Long loadCurrencyIdFor(long id) {
        Account account = load(id);

        if (account == null) {
            return null;
        }
        return account.getCurrencyId();
    }

    public String loadName(Long id) {
        Account account = load(id);
        if (account == null) {
            return null;
        }
        return account.getName();
    }

    public boolean anyAccountsUsingCurrency(long currencyId) {
        long links = count(Account.CURRENCYID + "=?",
                MmxDatabaseUtils.getArgsForId(currencyId));
        return links > 0;
    }

    public List<Account> loadByType(AccountTypes type) {
        return query(new Select(getAllColumns())
            .where(Account.ACCOUNTTYPE + " = ?", type.toString())
            .orderBy("lower(" + Account.ACCOUNTNAME + ")"));
    }
}
