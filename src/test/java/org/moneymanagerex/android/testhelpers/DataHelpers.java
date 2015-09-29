package org.moneymanagerex.android.testhelpers;

import android.content.Context;

import com.money.manager.ex.datalayer.AccountRepository;
import com.money.manager.ex.datalayer.AccountTransactionRepository;
import com.money.manager.ex.datalayer.PayeeRepository;
import com.money.manager.ex.domainmodel.Account;
import com.money.manager.ex.domainmodel.Payee;
import com.money.manager.ex.viewmodels.AccountTransaction;

import org.junit.Test;

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
        Account account = new Account();
        account.setName("cash");
        accountRepository.add(account);

        // add payees

        PayeeRepository repo = new PayeeRepository(context);
        for (int i = 0; i < 3; i++) {
            Payee payee = new Payee();
            payee.setName("payee" + i);
            repo.add(payee);
        }

        // add transactions

        AccountTransactionRepository txRepo = new AccountTransactionRepository(context);
        for (int i = 0; i < 3; i++) {
            AccountTransaction tx = new AccountTransaction();
            tx.setAccountId(account.getId());
            txRepo.add(tx);
        }
    }

}
