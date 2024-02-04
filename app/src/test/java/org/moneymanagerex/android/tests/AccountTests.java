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
package org.moneymanagerex.android.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import android.content.Context;

import com.money.manager.ex.account.AccountStatuses;
import com.money.manager.ex.account.AccountTypes;
import com.money.manager.ex.datalayer.AccountRepository;
import com.money.manager.ex.domainmodel.Account;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.moneymanagerex.android.testhelpers.UnitTestHelper;
import org.robolectric.RobolectricTestRunner;

/**
 * Test Account model.
 */
@RunWith(RobolectricTestRunner.class)
public class AccountTests {

    private Account account;

    @Before
    public void setup() {
        account = new Account();
    }

    @Test
    public void testInstantiation() {
        final Account account = new Account();

        assertNotNull(account);
    }

    //@Test
    public void testPropertySetting() {
        final int id = 3;

        account.setId(id);

        final int actual = account.getId();

        assertEquals(id, actual);
    }

    //@Test
    public void testThrowException() {
        final Integer actual = account.getId();

        assertNull(actual);
    }

    //@Test
    public void canUpdateValueInDb() {
        // Given

        UnitTestHelper.setupContentProvider();
        final Context context = UnitTestHelper.getContext();
        final AccountRepository repo = new AccountRepository(context);
        final Account account = Account.create("first", AccountTypes.CHECKING, AccountStatuses.OPEN,
                true, 1);
        final String accountNumber = "blah blah";

        // When

        repo.save(account);
        final Integer id = account.getId();

        Account loaded = repo.load(id);
        loaded.setAccountNumber(accountNumber);
        repo.save(loaded);

        loaded = repo.load(id);
        final String actual = loaded.getAccountNumber();

        // Then
//        assertThat(id).isEqualTo(1);
//        assertThat(actual).isEqualTo(accountNumber);
    }
}
