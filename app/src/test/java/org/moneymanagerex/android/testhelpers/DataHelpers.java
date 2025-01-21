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
import com.money.manager.ex.datalayer.SplitCategoryRepository;
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
public class DataHelpers {
    public static void insertData() {
        Context context = UnitTestHelper.getContext();

        // add account

        AccountRepository accountRepository = new AccountRepository(context);
        // Bahraini dinar
        Account account = Account.create("cash", AccountTypes.CHECKING, AccountStatuses.OPEN,
                true, 17);
        long accountId = accountRepository.add(account);
        account.setId(accountId);
//        assertThat(accountId).isNotEqualTo(Constants.NOT_SET);

        // add payees

        PayeeRepository repo = new PayeeRepository(context);
        for (int i = 0; i < 3; i++) {
            Payee payee = new Payee();
            payee.setName("payee" + i);
            long payeeId = repo.add(payee);
//            assertThat(payeeId).isNotEqualTo(Constants.NOT_SET);
        }

        // add transactions
        for (int i = 0; i < 3; i++) {
            Money amount = MoneyFactory.fromString("-" + i);
            // this is semantically wrong as there is no category & subcategory!
            createTransaction(accountId, 1, TransactionTypes.Withdrawal, -1, amount);
        }
    }

    public static void createTransaction(long accountId, long payeeId, TransactionTypes type,
                                         long categoryId, Money amount) {
        AccountTransactionRepository txRepo = new AccountTransactionRepository(UnitTestHelper.getContext());

        AccountTransaction tx = AccountTransaction.create(accountId, payeeId, type,
                categoryId, amount);
        long txId = txRepo.add(tx);
//        assertThat(txId).isNotEqualTo(Constants.NOT_SET);

    }

    public static void createSplitTransaction() {
        Context context = UnitTestHelper.getContext();

        // currency
        CurrencyService currencyService = new CurrencyService(context);
        Currency euro = currencyService.getCurrency("EUR");
        // account
        AccountService accountService = new AccountService(context);
        Account account = accountService.createAccount("only", AccountTypes.CHECKING, AccountStatuses.OPEN,
            true, euro.getCurrencyId());
        // payee
        PayeeService payeeService = new PayeeService(context);
        Payee payee = payeeService.createNew("zdravko colic");
        // transaction
        Money amount = MoneyFactory.fromDouble(100);
        AccountTransactionRepository txRepo = new AccountTransactionRepository(context);
        AccountTransaction tx = AccountTransaction.create(account.getId(), payee.getId(),
            TransactionTypes.Withdrawal, 1, amount);
        txRepo.insert(tx);
        // split categories
        SplitCategoryRepository splitRepo = new SplitCategoryRepository(context);
        SplitCategory split1 = SplitCategory.create(tx.getId(), 1,
                tx.getTransactionType(), MoneyFactory.fromDouble(25), "Note 1");
        splitRepo.insert(split1);
        SplitCategory split2 = SplitCategory.create(tx.getId(), 1,
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
