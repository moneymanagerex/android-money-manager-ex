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
package com.money.manager.ex.servicelayer;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.database.sqlite.SQLiteDiskIOException;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

import com.money.manager.ex.Constants;
import com.money.manager.ex.R;
import com.money.manager.ex.account.AccountStatuses;
import com.money.manager.ex.account.AccountTypes;
import com.money.manager.ex.core.ExceptionHandler;
import com.money.manager.ex.core.TransactionStatuses;
import com.money.manager.ex.core.TransactionTypes;
import com.money.manager.ex.currency.CurrencyService;
import com.money.manager.ex.database.ITransactionEntity;
import com.money.manager.ex.datalayer.AccountRepository;
import com.money.manager.ex.database.QueryAccountBills;
import com.money.manager.ex.database.QueryAllData;
import com.money.manager.ex.database.TableAccountList;
import com.money.manager.ex.database.WhereStatementGenerator;
import com.money.manager.ex.datalayer.AccountTransactionRepository;
import com.money.manager.ex.datalayer.StockRepository;
import com.money.manager.ex.domainmodel.Account;
import com.money.manager.ex.domainmodel.Stock;
import com.money.manager.ex.settings.AppSettings;
import com.money.manager.ex.settings.LookAndFeelSettings;
import com.money.manager.ex.viewmodels.AccountTransactionDisplay;

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
        this.mContext = context.getApplicationContext();
        this.accountRepository = new AccountRepository(context);
    }

    private Context mContext;
    private AccountRepository accountRepository;

    public Account createAccount(String name, AccountTypes accountType, AccountStatuses status,
                                 boolean favourite, int currencyId) {
        Account account = Account.create(name, accountType, status, favourite, currencyId);

        // save
        AccountRepository repo = new AccountRepository(getContext());
        int id = repo.insert(account);
        account.setId(id);

        return account;
    }

    /**
     * Loads account list, applying the current preferences for Open & Favourite accounts.
     * @return List of accounts
     */
    public List<Account> getAccountList() {
        AppSettings settings = new AppSettings(getContext());

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

    public Context getContext() {
        return mContext;
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
            ExceptionHandler handler = new ExceptionHandler(getContext(), this);
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

        WhereStatementGenerator where = new WhereStatementGenerator();
        // load all transactions on the account before and on given date.
        where.addStatement(
            where.concatenateOr(
                where.getStatement(ITransactionEntity.ACCOUNTID, "=", accountId),
                where.getStatement(ITransactionEntity.TOACCOUNTID, "=", accountId)
            )
        );

        where.addStatement(ITransactionEntity.TRANSDATE, "<=", isoDate);
        where.addStatement(ITransactionEntity.STATUS, "<>", TransactionStatuses.VOID.getCode());

        String selection = where.getWhere();

        AccountTransactionRepository repo = new AccountTransactionRepository(getContext());

        Cursor cursor = getContext().getContentResolver().query(repo.getUri(),
            null,
            selection,
            null,
            null);
        if (cursor == null) return total;

        AccountTransactionDisplay tx = new AccountTransactionDisplay();
        Money amount;

        // calculate balance.
        while (cursor.moveToNext()) {
            tx.contentValues.clear();
            String transType = cursor.getString(cursor.getColumnIndex(ITransactionEntity.TRANSCODE));

            // Some users have invalid Transaction Type. Should we check .contains()?

            switch (TransactionTypes.valueOf(transType)) {
                case Withdrawal:
//                    total -= cursor.getDouble(cursor.getColumnIndex(ITransactionEntity.TRANSAMOUNT));
                    DatabaseUtils.cursorDoubleToContentValues(cursor, ITransactionEntity.TRANSAMOUNT,
                            tx.contentValues, QueryAllData.Amount);
//                    total = total.subtract(BigDecimal.valueOf(
//                        cursor.getDouble(cursor.getColumnIndex(ITransactionEntity.TRANSAMOUNT))));
                    amount = tx.getAmount();
                    total = total.subtract(amount);
                    break;
                case Deposit:
                    DatabaseUtils.cursorDoubleToContentValues(cursor, ITransactionEntity.TRANSAMOUNT,
                            tx.contentValues, QueryAllData.Amount);
//                    total += cursor.getDouble(cursor.getColumnIndex(ITransactionEntity.TRANSAMOUNT));
//                    total = total.add(BigDecimal.valueOf(
//                        cursor.getDouble(cursor.getColumnIndex(ITransactionEntity.TRANSAMOUNT))));
                    amount = tx.getAmount();
                    total = total.add(amount);
                    break;
                case Transfer:
                    DatabaseUtils.cursorDoubleToContentValues(cursor, ITransactionEntity.ACCOUNTID,
                            tx.contentValues, QueryAllData.ACCOUNTID);

                    if (tx.getAccountId().equals(accountId)) {
                        DatabaseUtils.cursorDoubleToContentValues(cursor, ITransactionEntity.TRANSAMOUNT,
                                tx.contentValues, QueryAllData.Amount);
//                        total -= cursor.getDouble(cursor.getColumnIndex(ITransactionEntity.TRANSAMOUNT));
//                        total = total.subtract(BigDecimal.valueOf(
//                            cursor.getDouble(cursor.getColumnIndex(ITransactionEntity.TRANSAMOUNT))));
                        amount = tx.getAmount();
                        total = total.subtract(amount);
                    } else {
//                        total += cursor.getDouble(cursor.getColumnIndex(ITransactionEntity.TOTRANSAMOUNT));
                        DatabaseUtils.cursorDoubleToContentValues(cursor, ITransactionEntity.TOTRANSAMOUNT,
                                tx.contentValues, QueryAllData.Amount);
//                        total = total.add(BigDecimal.valueOf(
//                            cursor.getDouble(cursor.getColumnIndex(ITransactionEntity.TOTRANSAMOUNT))));
                        amount = tx.getAmount();
                        total = total.add(amount);
                    }
                    break;
            }
        }

        cursor.close();
        return total;
    }

    public String getAccountCurrencyCode(int accountId) {
        AccountRepository repo = new AccountRepository(getContext());
        Account account = repo.query(new String[] {Account.CURRENCYID},
            Account.ACCOUNTID + "=?",
            new String[] { Integer.toString(accountId)});
        int currencyId = account.getCurrencyId();

        CurrencyService currencyService = new CurrencyService(getContext());
        return currencyService.getCurrency(currencyId).getCode();
    }

    /**
     * Check if the account is used in any of the transactions.
     * @param accountId id of the account
     * @return a boolean indicating if there are any transactions using this account.
     */
    public boolean isAccountUsed(int accountId) {
        WhereStatementGenerator where = new WhereStatementGenerator();
        // transactional accounts
        where.addStatement(
            where.concatenateOr(
                where.getStatement(ITransactionEntity.ACCOUNTID, "=", accountId),
                where.getStatement(ITransactionEntity.TOACCOUNTID, "=", accountId)
            )
        );

        AccountTransactionRepository repo = new AccountTransactionRepository(getContext());
        int txCount = repo.count(where.getWhere(), null);

        // investment accounts
        StockRepository stockRepository = new StockRepository(getContext());
        where.clear();
        where.addStatement(Stock.HELDAT, "=", accountId);
        int investmentCount = stockRepository.count(where.getWhere(), null);

        return (txCount + investmentCount) > 0;
    }

    public void loadTransactionAccountsToSpinner(Spinner spinner) {
        if (spinner == null) return;

        LookAndFeelSettings settings = new AppSettings(getContext()).getLookAndFeelSettings();

        Cursor cursor = this.getCursor(settings.getViewOpenAccounts(),
            settings.getViewFavouriteAccounts(), this.getTransactionAccountTypeNames());

        int[] adapterRowViews = new int[] { android.R.id.text1 };

        SimpleCursorAdapter cursorAdapter = new SimpleCursorAdapter(getContext(),
            android.R.layout.simple_spinner_item,
            cursor,
            new String[] { Account.ACCOUNTNAME, Account.ACCOUNTID },
            adapterRowViews,
            SimpleCursorAdapter.NO_SELECTION);
//        cursorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cursorAdapter.setDropDownViewResource(R.layout.toolbar_spinner_item_dropdown);

        spinner.setAdapter(cursorAdapter);
    }

    public void loadInvestmentAccountsToSpinner(Spinner spinner) {
        if (spinner == null) return;

//        if (context == null) {
//            Log.e(this.getClass().getSimpleName(), "Context not sent when loading accounts");
//            return;
//        }

        AccountRepository repo = new AccountRepository(getContext());
        Cursor cursor = repo.getInvestmentAccountsCursor(true);

        // append All Accounts item
        MatrixCursor extras = new MatrixCursor(new String[] { "_id", Account.ACCOUNTID,
            Account.ACCOUNTNAME, Account.INITIALBAL });
        String title = getContext().getString(R.string.all_accounts);
        extras.addRow(new String[] { Integer.toString(Constants.NOT_SET),
            Integer.toString(Constants.NOT_SET), title, "0.0" });
        Cursor[] cursors = { extras, cursor };
        Cursor extendedCursor = new MergeCursor(cursors);


        int[] adapterRowViews = new int[] { android.R.id.text1 };

        SimpleCursorAdapter cursorAdapter = new SimpleCursorAdapter(getContext(),
            android.R.layout.simple_spinner_item,
            extendedCursor,
            new String[] { Account.ACCOUNTNAME, Account.ACCOUNTID },
            adapterRowViews,
            SimpleCursorAdapter.NO_SELECTION);
        cursorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(cursorAdapter);
    }

    public List<Account> loadAccounts(boolean openOnly, boolean favoriteOnly, List<String> accountTypes) {
        List<Account> result = new ArrayList<>();

        Cursor cursor = getCursor(openOnly, favoriteOnly, accountTypes);
        if (cursor == null) {
            ExceptionHandler handler = new ExceptionHandler(getContext(), this);
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

    public Money loadInitialBalance(int accountId) {
        AccountRepository repo = new AccountRepository(getContext());
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

        QueryAccountBills accountBills = new QueryAccountBills(getContext());
        Cursor cursor = getContext().getContentResolver().query(accountBills.getUri(),
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

    public Cursor getCursor(boolean open, boolean favorite, List<String> accountTypes) {
        try {
            return getCursorInternal(open, favorite, accountTypes);
        } catch (Exception ex) {
            ExceptionHandler handler = new ExceptionHandler(getContext(), this);
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

    public List<Account> getTransactionAccounts(boolean openOnly, boolean favoriteOnly) {
        List<String> accountTypeNames = getTransactionAccountTypeNames();

        List<Account> result = loadAccounts(openOnly, favoriteOnly, accountTypeNames);

        return result;
    }

    // Private

    private Cursor getCursorInternal(boolean openOnly, boolean favoriteOnly, List<String> accountTypes) {
        TableAccountList account = new TableAccountList();

        String where = getWhereFilterFor(openOnly, favoriteOnly);

        if (accountTypes != null && accountTypes.size() > 0) {
            where = DatabaseUtils.concatenateWhere(where, getWherePartFor(accountTypes));
        }

        Cursor cursor = getContext().getContentResolver().query(account.getUri(),
                account.getAllColumns(),
                where,
                null,
                "lower (" + Account.ACCOUNTNAME + ")"
        );
        return cursor;
    }

    private String getWhereFilterFor(boolean openOnly, boolean favoriteOnly) {
        StringBuilder where = new StringBuilder();

        if (openOnly) {
            where.append("LOWER(STATUS)='open'");
        }
        if (favoriteOnly) {
            if (openOnly) {
                where.append(" AND ");
            }
            where.append("LOWER(FAVORITEACCT)='true'");
        }

        return where.toString();
    }

    private String getWherePartFor(List<String> accountTypes) {
        StringBuilder where = new StringBuilder();
        where.append(Account.ACCOUNTTYPE);
        where.append(" IN (");
        for(String type : accountTypes) {
            if (accountTypes.indexOf(type) > 0) {
                // if not first, add comma before the type name
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
        String selection = Account.ACCOUNTID + "=?";

        Cursor cursor = getContext().getContentResolver().query(account.getUri(),
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
}
