/*
 * Copyright (C) 2012-2019 The Android Money Manager Ex Project Team
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

import android.content.Context;

import com.money.manager.ex.BuildConfig;
import com.money.manager.ex.account.AccountStatuses;
import com.money.manager.ex.account.AccountTypes;
import com.money.manager.ex.datalayer.AccountRepository;
import com.money.manager.ex.domainmodel.Account;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.moneymanagerex.android.testhelpers.TestApplication;
import org.moneymanagerex.android.testhelpers.UnitTestHelper;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

/**
 * Test Account model.
 */
@RunWith(RobolectricTestRunner.class)
public class AccountTests {

    private Account account;

    @Before
    public void setup() {
        this.account = new Account();
    }

    @Test
    public void testInstantiation() {
        Account account = new Account();

        assertTrue(account != null);
    }

    //@Test
    public void testPropertySetting() {
        final int id = 3;

        this.account.setId(id);

        int actual = this.account.getId();

        assertEquals(id, actual);
    }

    //@Test
    public void testThrowException() {
        Integer actual = this.account.getId();

        assertNull(actual);
    }

    //@Test
    public void canUpdateValueInDb() {
        // Given

        UnitTestHelper.setupContentProvider();
        Context context = UnitTestHelper.getContext();
        AccountRepository repo = new AccountRepository(context);
        Account account = Account.create("first", AccountTypes.CHECKING, AccountStatuses.OPEN,
            true, 1);
        String accountNumber = "blah blah";

        // When

        repo.save(account);
        Integer id = account.getId();

        Account loaded = repo.load(id);
        loaded.setAccountNumber(accountNumber);
        repo.save(loaded);

        loaded = repo.load(id);
        String actual = loaded.getAccountNumber();

        // Then
//        assertThat(id).isEqualTo(1);
//        assertThat(actual).isEqualTo(accountNumber);
    }
}
