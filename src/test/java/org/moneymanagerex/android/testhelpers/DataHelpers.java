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

import android.content.ContentResolver;
import android.content.Context;

import com.money.manager.ex.Constants;
import com.money.manager.ex.account.AccountStatuses;
import com.money.manager.ex.account.AccountTypes;
import com.money.manager.ex.core.TransactionTypes;
import com.money.manager.ex.datalayer.AccountRepository;
import com.money.manager.ex.datalayer.AccountTransactionRepository;
import com.money.manager.ex.datalayer.QueryAllDataRepository;
import com.money.manager.ex.datalayer.PayeeRepository;
import com.money.manager.ex.domainmodel.Account;
import com.money.manager.ex.domainmodel.AccountTransaction;
import com.money.manager.ex.domainmodel.Payee;

import org.robolectric.fakes.BaseCursor;
import org.robolectric.shadows.ShadowContentResolver;

import info.javaperformance.money.Money;
import info.javaperformance.money.MoneyFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.Shadows.shadowOf;

/**
 * Database manipulation. Used for test preparation.
 *
 * Created by Alen Siljak on 29/09/2015.
 */
public class DataHelpers {
    public static void insertData() {
        Context context = UnitTestHelper.getContext();

        // add account

        AccountRepository accountRepository = new AccountRepository(context);
        // Bahraini dinar
        Account account = Account.create("cash", AccountTypes.CHECKING, AccountStatuses.OPEN,
                true, 17);
        int accountId = accountRepository.add(account);
        account.setId(accountId);
        assertThat(accountId).isNotEqualTo(Constants.NOT_SET);

        // add payees

        PayeeRepository repo = new PayeeRepository(context);
        for (int i = 0; i < 3; i++) {
            Payee payee = new Payee();
            payee.setName("payee" + i);
            int payeeId = repo.add(payee);
            assertThat(payeeId).isNotEqualTo(Constants.NOT_SET);
        }

        // add transactions

        AccountTransactionRepository txRepo = new AccountTransactionRepository(context);
        for (int i = 0; i < 3; i++) {
            Money amount = MoneyFactory.fromString("-" + i);
            AccountTransaction tx = AccountTransaction.create(accountId, 1, TransactionTypes.Withdrawal,
                    amount);
            int txId = txRepo.add(tx);
            assertThat(txId).isNotEqualTo(Constants.NOT_SET);
        }
    }

    public static void setFakeCursor() {
        ContentResolver resolver = UnitTestHelper.getContext().getContentResolver();
        ShadowContentResolver shadow = shadowOf(resolver);

        BaseCursor cursor = new AccountCursor();
        shadow.setCursor(cursor);
    }
}
