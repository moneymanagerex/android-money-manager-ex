package org.moneymanagerex.android.tests;

import com.money.manager.ex.domainmodel.AccountTransaction;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import info.javaperformance.money.MoneyFactory;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
public class EntityBaseTests {

    @Test
    public void testAccountTransactionGetDiffString() {
        AccountTransaction at1 = new AccountTransaction();
        at1.setAccountId(5L);
        at1.setNotes("abc");
        at1.setAmount(MoneyFactory.fromDouble(5.55));

        AccountTransaction at2 = new AccountTransaction();
        at2.setAccountId(5L);
        at2.setNotes("abc123");
        at2.setAmount(MoneyFactory.fromDouble(5.55));

        String s = at1.getDiffString(at2);
        assertEquals("NOTES: abc | abc123\n", s);
    }
}
