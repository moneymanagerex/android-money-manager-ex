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
package com.money.manager.ex.servicelayer;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.widget.Adapter;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

import com.money.manager.ex.Constants;
import com.money.manager.ex.R;
import com.money.manager.ex.account.AccountStatuses;
import com.money.manager.ex.account.AccountTypes;
import com.money.manager.ex.core.ToolbarSpinnerAdapter;
import com.money.manager.ex.core.TransactionStatuses;
import com.money.manager.ex.core.TransactionTypes;
import com.money.manager.ex.core.UIHelper;
import com.money.manager.ex.currency.CurrencyService;
import com.money.manager.ex.database.ITransactionEntity;
import com.money.manager.ex.database.QueryAccountBills;
import com.money.manager.ex.database.QueryAllData;
import com.money.manager.ex.database.WhereStatementGenerator;
import com.money.manager.ex.datalayer.AccountRepository;
import com.money.manager.ex.datalayer.AccountTransactionRepository;
import com.money.manager.ex.datalayer.StockFields;
import com.money.manager.ex.datalayer.StockRepository;
import com.money.manager.ex.domainmodel.Account;
import com.money.manager.ex.settings.AppSettings;
import com.money.manager.ex.settings.LookAndFeelSettings;
import com.money.manager.ex.viewmodels.AccountTransactionDisplay;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import info.javaperformance.money.Money;
import info.javaperformance.money.MoneyFactory;
import timber.log.Timber;

/**
 * Various business logic pieces related to Account(s).
 */
public class AccountService
        extends ServiceBase {

    public AccountService(final Context context) {
        super(context);

    }

    public Account createAccount(final String name, final AccountTypes accountType, final AccountStatuses status,
                                 final boolean favourite, final int currencyId) {
        final Account account = Account.create(name, accountType, status, favourite, currencyId);

        // update
        final AccountRepository repo = new AccountRepository(getContext());
        repo.save(account);

        return account;
    }

    /**
     * Loads account list, applying the current preferences for Open & Favourite accounts.
     *
     * @return List of accounts
     */
    public List<Account> getAccountList() {
        final AppSettings settings = new AppSettings(getContext());

        final boolean favourite = settings.getLookAndFeelSettings().getViewFavouriteAccounts();
        final boolean open = settings.getLookAndFeelSettings().getViewOpenAccounts();

        return getAccountList(open, favourite);
    }

    /**
     * Load account list with given parameters.
     * Includes all account types.
     *
     * @param open     show open accounts
     * @param favorite show favorite account
     * @return List<Account> list of accounts selected
     */
    public List<Account> getAccountList(final boolean open, final boolean favorite) {
        // create a return list
        return loadAccounts(open, favorite, null);
    }

    /**
     * Calculate simple balance by adding together all transactions before and on the
     * given date. To get the real balance, this amount should be subtracted from the
     * account initial balance.
     *
     * @param isoDate date in ISO format
     */
    public Money calculateBalanceOn(final int accountId, final String isoDate) {
        Money total = MoneyFactory.fromBigDecimal(BigDecimal.ZERO);

        final WhereStatementGenerator where = new WhereStatementGenerator();
        // load all transactions on the account before and on given date.
        where.addStatement(
                where.concatenateOr(
                        where.getStatement(ITransactionEntity.ACCOUNTID, "=", accountId),
                        where.getStatement(ITransactionEntity.TOACCOUNTID, "=", accountId)
                )
        );

        where.addStatement(ITransactionEntity.TRANSDATE, "<=", isoDate);
        where.addStatement(ITransactionEntity.STATUS, "<>", TransactionStatuses.VOID.getCode());

        final String selection = where.getWhere();

        final AccountTransactionRepository repo = new AccountTransactionRepository(getContext());

        final Cursor cursor = getContext().getContentResolver().query(repo.getUri(),
                null,
                selection,
                null,
                null);
        if (null == cursor) return total;

        final AccountTransactionDisplay tx = new AccountTransactionDisplay();
        Money amount;

        // calculate balance.
        while (cursor.moveToNext()) {
            tx.contentValues.clear();
            final String transType = cursor.getString(cursor.getColumnIndex(ITransactionEntity.TRANSCODE));

            // Some users have invalid Transaction Type. Should we check .contains()?

            switch (TransactionTypes.valueOf(transType)) {
                case Withdrawal:
                    DatabaseUtils.cursorDoubleToContentValues(cursor, ITransactionEntity.TRANSAMOUNT,
                            tx.contentValues, QueryAllData.Amount);
                    amount = tx.getAmount();
                    total = total.subtract(amount);
                    break;
                case Deposit:
                    DatabaseUtils.cursorDoubleToContentValues(cursor, ITransactionEntity.TRANSAMOUNT,
                            tx.contentValues, QueryAllData.Amount);
                    amount = tx.getAmount();
                    total = total.add(amount);
                    break;
                case Transfer:
                    DatabaseUtils.cursorDoubleToContentValues(cursor, ITransactionEntity.ACCOUNTID,
                            tx.contentValues, QueryAllData.ACCOUNTID);

                    if (tx.getAccountId().equals(accountId)) {
                        DatabaseUtils.cursorDoubleToContentValues(cursor, ITransactionEntity.TRANSAMOUNT,
                                tx.contentValues, QueryAllData.Amount);
                        amount = tx.getAmount();
                        total = total.subtract(amount);
                    } else {
                        DatabaseUtils.cursorDoubleToContentValues(cursor, ITransactionEntity.TOTRANSAMOUNT,
                                tx.contentValues, QueryAllData.Amount);
                        amount = tx.getAmount();
                        total = total.add(amount);
                    }
                    break;
            }
        }

        cursor.close();
        return total;
    }

    public String getAccountCurrencyCode(final int accountId) {
        final AccountRepository repo = new AccountRepository(getContext());
        final Account account = (Account) repo.first(Account.class,
                new String[]{Account.CURRENCYID},
                Account.ACCOUNTID + "=?",
                new String[]{Integer.toString(accountId)},
                null);
        final int currencyId = account.getCurrencyId();

        final CurrencyService currencyService = new CurrencyService(getContext());
        return currencyService.getCurrency(currencyId).getCode();
    }

    public Cursor getCursor(final boolean open, final boolean favorite, final List<String> accountTypes) {
        try {
            return getCursorInternal(open, favorite, accountTypes);
        } catch (final Exception ex) {
            Timber.e(ex, "getting cursor in account repository");
        }
        return null;
    }

    public List<String> getTransactionAccountTypeNames() {
        final List<String> accountTypeNames = new ArrayList<>();
        final List<AccountTypes> accountTypes = getTransactionAccountTypes();

        for (final AccountTypes type : accountTypes) {
            accountTypeNames.add(type.toString());
        }

        return accountTypeNames;
    }

    public List<AccountTypes> getTransactionAccountTypes() {
        final List<AccountTypes> list = new ArrayList<>();

        list.add(AccountTypes.CASH);
        list.add(AccountTypes.CHECKING);
        list.add(AccountTypes.TERM);
        list.add(AccountTypes.CREDIT_CARD);
        list.add(AccountTypes.LOAN);
        list.add(AccountTypes.SHARES);

        return list;
    }

    public List<Account> getTransactionAccounts(final boolean openOnly, final boolean favoriteOnly) {
        final List<String> accountTypeNames = getTransactionAccountTypeNames();

        final List<Account> result = loadAccounts(openOnly, favoriteOnly, accountTypeNames);

        return result;
    }

    /**
     * Check if the account is used in any of the transactions.
     *
     * @param accountId id of the account
     * @return a boolean indicating if there are any transactions using this account.
     */
    public boolean isAccountUsed(final int accountId) {
        final WhereStatementGenerator where = new WhereStatementGenerator();
        // transactional accounts
        where.addStatement(
                where.concatenateOr(
                        where.getStatement(ITransactionEntity.ACCOUNTID, "=", accountId),
                        where.getStatement(ITransactionEntity.TOACCOUNTID, "=", accountId)
                )
        );

        final AccountTransactionRepository repo = new AccountTransactionRepository(getContext());
        final int txCount = repo.count(where.getWhere(), null);

        // investment accounts
        final StockRepository stockRepository = new StockRepository(getContext());
        where.clear();
        where.addStatement(StockFields.HELDAT, "=", accountId);
        final int investmentCount = stockRepository.count(where.getWhere(), null);

        return 0 < (txCount + investmentCount);
    }

    public void loadTransactionAccountsToSpinner(final Spinner spinner) {
        if (null == spinner) return;

        final LookAndFeelSettings settings = new AppSettings(getContext()).getLookAndFeelSettings();

        final Cursor cursor = getCursor(settings.getViewOpenAccounts(),
                settings.getViewFavouriteAccounts(), getTransactionAccountTypeNames());

        final int[] adapterRowViews = {android.R.id.text1};

        final ToolbarSpinnerAdapter cursorAdapter = new ToolbarSpinnerAdapter(getContext(),
                android.R.layout.simple_spinner_item,
                cursor,
                new String[]{Account.ACCOUNTNAME, Account.ACCOUNTID},
                adapterRowViews,
                Adapter.NO_SELECTION);
//        cursorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cursorAdapter.setDropDownViewResource(R.layout.toolbar_spinner_item_dropdown);

        spinner.setAdapter(cursorAdapter);
    }

    public void loadInvestmentAccountsToSpinner(final Spinner spinner, final boolean showAllAccountsItem) {
        if (null == spinner) return;

        final AccountRepository repo = new AccountRepository(getContext());
        final Cursor cursor = repo.getInvestmentAccountsCursor(true);
        Cursor finalCursor = cursor;

        if (showAllAccountsItem) {
            // append All Accounts item
            final MatrixCursor extras = new MatrixCursor(new String[]{"_id", Account.ACCOUNTID,
                    Account.ACCOUNTNAME, Account.INITIALBAL});
            final String title = getContext().getString(R.string.all_accounts);
            extras.addRow(new String[]{Integer.toString(Constants.NOT_SET),
                    Integer.toString(Constants.NOT_SET), title, "0.0"});
            final Cursor[] cursors = {extras, cursor};
            finalCursor = new MergeCursor(cursors);
        }

        final int[] adapterRowViews = {android.R.id.text1};

        final ToolbarSpinnerAdapter cursorAdapter = new ToolbarSpinnerAdapter(getContext(),
                android.R.layout.simple_spinner_item,
                finalCursor,
                new String[]{Account.ACCOUNTNAME, Account.ACCOUNTID},
                adapterRowViews,
                Adapter.NO_SELECTION);
        cursorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(cursorAdapter);
    }

    public List<Account> loadAccounts(final boolean openOnly, final boolean favoriteOnly, final List<String> accountTypes) {
        final List<Account> result = new ArrayList<>();

        final Cursor cursor = getCursor(openOnly, favoriteOnly, accountTypes);
        if (null == cursor) {
            new UIHelper(getContext()).showToast("Error reading accounts list!");
            return result;
        }

        while (cursor.moveToNext()) {
            final Account account = new Account();
            account.loadFromCursor(cursor);
            result.add(account);
        }
        cursor.close();

        return result;
    }

    public Money loadInitialBalance(final int accountId) {
        final AccountRepository repo = new AccountRepository(getContext());
        final Account account = repo.load(accountId);
        return account.getInitialBalance();
    }

    /**
     * Loads account details with balances.
     * Needs to be better organized to limit the where clause.
     *
     * @param where selection criteria for Select Account Bills
     * @return current balance in the currency of the account.
     */
    public Money loadBalance(final String where) {
        Money curTotal = MoneyFactory.fromString("0");

        final QueryAccountBills accountBills = new QueryAccountBills(getContext());
        final Cursor cursor = getContext().getContentResolver().query(accountBills.getUri(),
                null,
                where,
                null,
                null);
        if (null == cursor) return curTotal;

        // calculate summary
        while (cursor.moveToNext()) {
//            curTotal = curTotal.add(MoneyFactory.fromDouble(cursor.getDouble(cursor.getColumnIndex(QueryAccountBills.TOTAL))));
            curTotal = curTotal.add(MoneyFactory.fromString(cursor.getString(cursor.getColumnIndex(QueryAccountBills.TOTAL))));
        }
        cursor.close();

        return curTotal;
    }

    // Private

    private Cursor getCursorInternal(final boolean openOnly, final boolean favoriteOnly, final List<String> accountTypes) {
        final AccountRepository repo = new AccountRepository(getContext());

        String where = getWhereFilterFor(openOnly, favoriteOnly);

        if (null != accountTypes && 0 < accountTypes.size()) {
            where = DatabaseUtils.concatenateWhere(where, getWherePartFor(accountTypes));
        }

        final Cursor cursor = getContext().getContentResolver().query(repo.getUri(),
                repo.getAllColumns(),
                where,
                null,
                "lower (" + Account.ACCOUNTNAME + ")"
        );
        return cursor;
    }

    private String getWhereFilterFor(final boolean openOnly, final boolean favoriteOnly) {
        final StringBuilder where = new StringBuilder();

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

    private String getWherePartFor(final List<String> accountTypes) {
        final StringBuilder where = new StringBuilder();
        where.append(Account.ACCOUNTTYPE);
        where.append(" IN (");
        for (final String type : accountTypes) {
            if (0 < accountTypes.indexOf(type)) {
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
}
