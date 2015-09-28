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
 *
 */
package com.money.manager.ex.businessobjects;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDiskIOException;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteStatement;

import com.money.manager.ex.core.AccountTypes;
import com.money.manager.ex.core.ExceptionHandler;
import com.money.manager.ex.core.TransactionStatuses;
import com.money.manager.ex.core.TransactionTypes;
import com.money.manager.ex.database.AccountRepository;
import com.money.manager.ex.database.ISplitTransactionsDataset;
import com.money.manager.ex.database.QueryAccountBills;
import com.money.manager.ex.database.QueryAllData;
import com.money.manager.ex.database.TableAccountList;
import com.money.manager.ex.database.TableCheckingAccount;
import com.money.manager.ex.database.WhereStatementGenerator;
import com.money.manager.ex.domainmodel.Account;
import com.money.manager.ex.settings.AppSettings;
import com.money.manager.ex.viewmodels.AccountTransaction;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import info.javaperformance.money.Money;
import info.javaperformance.money.MoneyFactory;

/**
 * Various business logic pieces related to Account(s).
 */
public class AccountService {

    public AccountService(Context context) {
        mContext = context;
    }

    private Context mContext;

    /**
     * Loads account list, applying the current preferences for Open & Favourite accounts.
     * @return List of accounts
     */
    public List<Account> getAccountList() {
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
     * @return List<Account> list of accounts selected
     */
    public List<Account> getAccountList(boolean open, boolean favorite) {
        // create a return list
        return loadAccounts(open, favorite, null);
    }

    /**
     * @param id account id to be search
     * @return Account, return null if account id not find
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
    public Money calculateBalanceOn(int accountId, String isoDate) {
        Money total = MoneyFactory.fromBigDecimal(BigDecimal.ZERO);

        TableCheckingAccount tableCheckingAccount = new TableCheckingAccount();

        WhereStatementGenerator where = new WhereStatementGenerator();
        // load all transactions on the account before and on given date.
        where.addStatement(
            where.concatenateOr(
                where.getStatement(ISplitTransactionsDataset.ACCOUNTID, "=", accountId),
                where.getStatement(ISplitTransactionsDataset.TOACCOUNTID, "=", accountId)
            )
        );

        where.addStatement(ISplitTransactionsDataset.TRANSDATE, "<=", isoDate);
        where.addStatement(ISplitTransactionsDataset.STATUS, "<>", TransactionStatuses.VOID.getCode());

        String selection = where.getWhere();

        Cursor cursor = mContext.getContentResolver().query(tableCheckingAccount.getUri(),
            null,
            selection,
            null,
            null);
        if (cursor == null) return total;

        AccountTransaction tx = new AccountTransaction();
        Money amount;

        // calculate balance.
        while (cursor.moveToNext()) {
            tx.contentValues.clear();
            String transType = cursor.getString(cursor.getColumnIndex(ISplitTransactionsDataset.TRANSCODE));

            // Some users have invalid Transaction Type. Should we check .contains()?

            switch (TransactionTypes.valueOf(transType)) {
                case Withdrawal:
//                    total -= cursor.getDouble(cursor.getColumnIndex(ISplitTransactionsDataset.TRANSAMOUNT));
                    DatabaseUtils.cursorDoubleToContentValues(cursor, ISplitTransactionsDataset.TRANSAMOUNT,
                            tx.contentValues, QueryAllData.Amount);
//                    total = total.subtract(BigDecimal.valueOf(
//                        cursor.getDouble(cursor.getColumnIndex(ISplitTransactionsDataset.TRANSAMOUNT))));
                    amount = tx.getAmount();
                    total = total.subtract(amount);
                    break;
                case Deposit:
                    DatabaseUtils.cursorDoubleToContentValues(cursor, ISplitTransactionsDataset.TRANSAMOUNT,
                            tx.contentValues, QueryAllData.Amount);
//                    total += cursor.getDouble(cursor.getColumnIndex(ISplitTransactionsDataset.TRANSAMOUNT));
//                    total = total.add(BigDecimal.valueOf(
//                        cursor.getDouble(cursor.getColumnIndex(ISplitTransactionsDataset.TRANSAMOUNT))));
                    amount = tx.getAmount();
                    total = total.add(amount);
                    break;
                case Transfer:
                    DatabaseUtils.cursorDoubleToContentValues(cursor, ISplitTransactionsDataset.ACCOUNTID,
                            tx.contentValues, QueryAllData.ACCOUNTID);

                    if (tx.getAccountId().equals(accountId)) {
                        DatabaseUtils.cursorDoubleToContentValues(cursor, ISplitTransactionsDataset.TRANSAMOUNT,
                                tx.contentValues, QueryAllData.Amount);
//                        total -= cursor.getDouble(cursor.getColumnIndex(ISplitTransactionsDataset.TRANSAMOUNT));
//                        total = total.subtract(BigDecimal.valueOf(
//                            cursor.getDouble(cursor.getColumnIndex(ISplitTransactionsDataset.TRANSAMOUNT))));
                        amount = tx.getAmount();
                        total = total.subtract(amount);
                    } else {
//                        total += cursor.getDouble(cursor.getColumnIndex(ISplitTransactionsDataset.TOTRANSAMOUNT));
                        DatabaseUtils.cursorDoubleToContentValues(cursor, ISplitTransactionsDataset.TOTRANSAMOUNT,
                                tx.contentValues, QueryAllData.Amount);
//                        total = total.add(BigDecimal.valueOf(
//                            cursor.getDouble(cursor.getColumnIndex(ISplitTransactionsDataset.TOTRANSAMOUNT))));
                        amount = tx.getAmount();
                        total = total.add(amount);
                    }
                    break;
            }
        }

        cursor.close();
        return total;
    }

    public List<Account> loadAccounts(boolean open, boolean favorite, List<String> accountTypes) {
        return loadAccounts_content(open, favorite, accountTypes);
    }

    public Money loadInitialBalance(int accountId) {
        AccountRepository repo = new AccountRepository(mContext);
        Account account = repo.load(accountId);
        return account.getInitialBalance();
    }

    /**
     * Loads account details with balances.
     * Needs to be better organized to limit the where clause.
     * @param where selection criteria for Query Account Bills
     * @return current balance in the currency of the account.
     */
    public Money loadBalance(String where) {
        Money curTotal = MoneyFactory.fromString("0");

        QueryAccountBills accountBills = new QueryAccountBills(mContext);
        Cursor cursor = mContext.getContentResolver().query(accountBills.getUri(),
                null,
                where,
                null,
                null);
        if (cursor == null) return curTotal;

        // calculate summary
        while (cursor.moveToNext()) {
//            curTotal = curTotal.add(MoneyFactory.fromDouble(cursor.getDouble(cursor.getColumnIndex(QueryAccountBills.TOTAL))));
            curTotal = curTotal.add(MoneyFactory.fromString(cursor.getString(cursor.getColumnIndex(QueryAccountBills.TOTAL))));
        }
        cursor.close();

        return curTotal;
    }

    public Integer loadCurrencyId(int accountId) {
        WhereStatementGenerator where = new WhereStatementGenerator();
        where.addStatement(TableAccountList.ACCOUNTID, "=", accountId);

        AccountRepository repo = new AccountRepository(mContext);
        Account account = repo.query(new String[]{TableAccountList.CURRENCYID},
                where.getWhere(), null);
        return account.getCurrencyId();
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

    public List<Account> getTransactionAccounts(boolean open, boolean favorite) {
        List<String> accountTypeNames = getTransactionAccountTypeNames();

        List<Account> result = loadAccounts(open, favorite, accountTypeNames);

        return result;
    }

    private Cursor getCursorInternal(boolean open, boolean favorite, List<String> accountTypes) {
        TableAccountList account = new TableAccountList();

        String where = getWhereFilterFor(open, favorite);

        if (accountTypes != null && accountTypes.size() > 0) {
            where = DatabaseUtils.concatenateWhere(where, getWherePartFor(accountTypes));
        }

        Cursor cursor = mContext.getContentResolver().query(account.getUri(),
                account.getAllColumns(),
                where,
                null,
                "lower (" + TableAccountList.ACCOUNTNAME + ")"
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
                null);
        if (cursor == null) return null;

        if (cursor.moveToFirst()) {
            account = new TableAccountList();
            account.setValueFromCursor(cursor);

            cursor.close();
        }

        return account;
    }

    private List<Account> loadAccounts_content(boolean open, boolean favorite, List<String> accountTypes) {
        List<Account> result = new ArrayList<>();

        Cursor cursor = getCursor(open, favorite, accountTypes);
        if (cursor == null) {
            ExceptionHandler handler = new ExceptionHandler(mContext, this);
            handler.showMessage("Error reading accounts list!");
            return result;
        }

        while (cursor.moveToNext()) {
            Account account = new Account();
            account.loadFromCursor(cursor);
            result.add(account);
        }
        cursor.close();

        return result;
    }

}
