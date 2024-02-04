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
package org.moneymanagerex.android.testhelpers;

import android.content.Context;

import com.money.manager.ex.account.AccountStatuses;
import com.money.manager.ex.account.AccountTypes;
import com.money.manager.ex.core.TransactionTypes;
import com.money.manager.ex.currency.CurrencyService;
import com.money.manager.ex.datalayer.AccountRepository;
import com.money.manager.ex.datalayer.AccountTransactionRepository;
import com.money.manager.ex.datalayer.PayeeRepository;
import com.money.manager.ex.datalayer.SplitCategoriesRepository;
import com.money.manager.ex.domainmodel.Account;
import com.money.manager.ex.domainmodel.AccountTransaction;
import com.money.manager.ex.domainmodel.Currency;
import com.money.manager.ex.domainmodel.Payee;
import com.money.manager.ex.domainmodel.SplitCategory;
import com.money.manager.ex.servicelayer.AccountService;
import com.money.manager.ex.servicelayer.PayeeService;

import info.javaperformance.money.Money;
import info.javaperformance.money.MoneyFactory;

/**
 * Database manipulation. Used for test preparation.
 */
public enum DataHelpers {
    ;

    public static void insertData() {
        final Context context = UnitTestHelper.getContext();

        // add account

        final AccountRepository accountRepository = new AccountRepository(context);
        // Bahraini dinar
        final Account account = Account.create("cash", AccountTypes.CHECKING, AccountStatuses.OPEN,
                true, 17);
        final int accountId = accountRepository.add(account);
        account.setId(accountId);
//        assertThat(accountId).isNotEqualTo(Constants.NOT_SET);

        // add payees

        final PayeeRepository repo = new PayeeRepository(context);
        for (int i = 0; 3 > i; i++) {
            final Payee payee = new Payee();
            payee.setName("payee" + i);
            final int payeeId = repo.add(payee);
//            assertThat(payeeId).isNotEqualTo(Constants.NOT_SET);
        }

        // add transactions
        for (int i = 0; 3 > i; i++) {
            final Money amount = MoneyFactory.fromString("-" + i);
            // this is semantically wrong as there is no category & subcategory!
            createTransaction(accountId, 1, TransactionTypes.Withdrawal, -1, amount);
        }
    }

    public static void createTransaction(final int accountId, final int payeeId, final TransactionTypes type,
                                         final int categoryId, final Money amount) {
        final AccountTransactionRepository txRepo = new AccountTransactionRepository(UnitTestHelper.getContext());

        final AccountTransaction tx = AccountTransaction.create(accountId, payeeId, type,
                categoryId, amount);
        final int txId = txRepo.add(tx);
//        assertThat(txId).isNotEqualTo(Constants.NOT_SET);

    }

    public static void createSplitTransaction() {
        final Context context = UnitTestHelper.getContext();

        // currency
        final CurrencyService currencyService = new CurrencyService(context);
        final Currency euro = currencyService.getCurrency("EUR");
        // account
        final AccountService accountService = new AccountService(context);
        final Account account = accountService.createAccount("only", AccountTypes.CHECKING, AccountStatuses.OPEN,
                true, euro.getCurrencyId());
        // payee
        final PayeeService payeeService = new PayeeService(context);
        final Payee payee = payeeService.createNew("zdravko colic");
        // transaction
        final Money amount = MoneyFactory.fromDouble(100);
        final AccountTransactionRepository txRepo = new AccountTransactionRepository(context);
        final AccountTransaction tx = AccountTransaction.create(account.getId(), payee.getId(),
                TransactionTypes.Withdrawal, 1, amount);
        txRepo.insert(tx);
        // split categories
        final SplitCategoriesRepository splitRepo = new SplitCategoriesRepository(context);
        final SplitCategory split1 = SplitCategory.create(tx.getId(), 1, -1,
                tx.getTransactionType(), MoneyFactory.fromDouble(25), "Note 1");
        splitRepo.insert(split1);
        final SplitCategory split2 = SplitCategory.create(tx.getId(), 1, -1,
                tx.getTransactionType(), MoneyFactory.fromDouble(25), "Note 2");
        splitRepo.insert(split2);
    }

//    private static void setFakeCursor() {
//        ContentResolver resolver = UnitTestHelper.getContext().getContentResolver();
//        ShadowContentResolver shadow = shadowOf(resolver);
//
//        BaseCursor cursor = new AccountCursor();
//        shadow.setCursor(cursor);
//    }
}
