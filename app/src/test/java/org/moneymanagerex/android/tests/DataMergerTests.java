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

import android.content.ContentProvider;
import android.os.Handler;
import android.os.Messenger;
import android.os.RemoteException;

import com.money.manager.ex.MmexApplication;
import com.money.manager.ex.core.TransactionTypes;
import com.money.manager.ex.datalayer.AccountTransactionRepository;
import com.money.manager.ex.domainmodel.AccountTransaction;
import com.money.manager.ex.home.DatabaseMetadataFactory;
import com.money.manager.ex.home.RecentDatabasesProvider;
import com.money.manager.ex.sync.merge.DataMerger;
import com.money.manager.ex.sync.merge.MergeConflictResolution;
import com.money.manager.ex.utils.MmxDate;

import org.javamoney.moneta.Money;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.moneymanagerex.android.testhelpers.DataHelpers;
import org.moneymanagerex.android.testhelpers.UnitTestHelper;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import info.javaperformance.money.MoneyFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test Account model.
 */
@RunWith(RobolectricTestRunner.class)
public class DataMergerTests {

    @Test
    public void testMergeAccountTransactionTakeoverFromRemote() {
        // prepare data

        // prepare mocks
        Messenger msger = new Messenger(mock(Handler.class));
        DataMerger testee = spy(new DataMerger(msger));
        AccountTransaction localEntity = null;
        AccountTransaction remoteEntity = new AccountTransaction();
        AccountTransactionRepository localAccTrans = mock(AccountTransactionRepository.class);
        MmxDate lastLocalSyncDate = new MmxDate();
        StringBuilder log = new StringBuilder();
        // test
        testee.mergeEntity(localAccTrans, localEntity, remoteEntity, lastLocalSyncDate, log);
        // verify
        verify(localAccTrans).add(remoteEntity);
        verify(localAccTrans, never()).save(remoteEntity);
    }

    @Test
    public void testAccountTransactionEquals() {
        AccountTransaction localEntity = new AccountTransaction();
        localEntity.setLastUpdatedTime(MmxDate.fromIso8601("2020-01-20T18:42:18.000Z").toIsoString());
        AccountTransaction remoteEntity = new AccountTransaction();
        remoteEntity.setLastUpdatedTime(MmxDate.fromIso8601("2020-01-22T18:42:18.000Z").toIsoString());
        assertNotEquals(localEntity, remoteEntity);

        remoteEntity.setLastUpdatedTime(localEntity.getLastUpdatedTime().toIsoString());
        assertEquals(localEntity, remoteEntity);
    }

    @Test
    public void testAccountTransactionEqualsNull1() {
        AccountTransaction localEntity = new AccountTransaction();
        localEntity.setNotes(null);
        AccountTransaction remoteEntity = new AccountTransaction();
        assertNotEquals(localEntity, remoteEntity);

        remoteEntity.setNotes(null);
        assertEquals(localEntity, remoteEntity);
    }

    @Test
    public void testAccountTransactionEqualsNull2() {
        AccountTransaction localEntity = new AccountTransaction();
        localEntity.setNotes("x");
        AccountTransaction remoteEntity = new AccountTransaction();
        remoteEntity.setNotes(null);
        assertNotEquals(localEntity, remoteEntity);
    }

    @Test
    public void testAccountTransactionEqualsNull3() {
        AccountTransaction localEntity = new AccountTransaction();
        localEntity.setAccountId(null);
        AccountTransaction remoteEntity = new AccountTransaction();
        remoteEntity.setAccountId(1L);
        assertNotEquals(localEntity, remoteEntity);
    }

    @Test
    public void testMergeAccountTransactionRemoteWasModifiedAfterSync() {
        // prepare mocks
        Messenger msger = new Messenger(mock(Handler.class));
        DataMerger testee = spy(new DataMerger(msger));
        doReturn(MergeConflictResolution.THEIRS).when(testee).conflictResolutionByUser(any(), any()); // user ansers "THEIRS"
        AccountTransaction localEntity = new AccountTransaction();
        localEntity.setLastUpdatedTime("2020-01-20T18:42:18.000Z");
        AccountTransaction remoteEntity = new AccountTransaction();
        remoteEntity.setLastUpdatedTime("2020-01-22T18:42:18.000Z");
        AccountTransactionRepository localAccTrans = mock(AccountTransactionRepository.class);
        MmxDate lastLocalSyncDate = MmxDate.fromIso8601("2020-01-21T18:42:18.000Z");
        StringBuilder log = new StringBuilder();
        // test
        testee.mergeEntity(localAccTrans, localEntity, remoteEntity, lastLocalSyncDate, log);
        // verify
        verify(localAccTrans).save(remoteEntity);
        verify(localAccTrans, never()).add(remoteEntity);
        assertTrue(log.toString().contains(DataMerger.REMOTE_ONLY_WAS_MODIFIED));
    }

    @Test
    public void testMergeAccountTransactionBothModifiedRemoteWasLaterModifiedUserChoiceTheirs() {
        // prepare mocks
        Messenger msger = new Messenger(mock(Handler.class));
        DataMerger testee = spy(new DataMerger(msger));
        doReturn(MergeConflictResolution.THEIRS).when(testee).conflictResolutionByUser(any(), any()); // user ansers "THEIRS"
        AccountTransaction localEntity = new AccountTransaction();
        localEntity.setLastUpdatedTime("2020-01-22T18:42:18.000Z");
        AccountTransaction remoteEntity = new AccountTransaction();
        remoteEntity.setLastUpdatedTime("2020-01-23T18:42:18.000Z");
        AccountTransactionRepository localAccTrans = mock(AccountTransactionRepository.class);
        MmxDate lastLocalSyncDate = MmxDate.fromIso8601("2020-01-21T18:42:18.000Z");
        StringBuilder log = new StringBuilder();
        // test
        testee.mergeEntity(localAccTrans, localEntity, remoteEntity, lastLocalSyncDate, log);
        // verify
        verify(localAccTrans).save(remoteEntity);
        verify(localAccTrans, never()).add(remoteEntity);
        assertTrue(log.toString().contains(DataMerger.OVERWRITE_LOCAL_CHANGES));
    }

    @Test
    public void testMergeAccountTransactionBothModifiedLocalWasLaterModifiedUserChoiceTheirs() {
        // prepare mocks
        Messenger msger = new Messenger(mock(Handler.class));
        DataMerger testee = spy(new DataMerger(msger));
        doReturn(MergeConflictResolution.THEIRS).when(testee).conflictResolutionByUser(any(), any()); // user ansers "THEIRS"
        AccountTransaction localEntity = new AccountTransaction();
        localEntity.setLastUpdatedTime("2020-01-23T18:42:18.000Z");
        AccountTransaction remoteEntity = new AccountTransaction();
        remoteEntity.setLastUpdatedTime("2020-01-22T18:42:18.000Z");
        AccountTransactionRepository localAccTrans = mock(AccountTransactionRepository.class);
        MmxDate lastLocalSyncDate = MmxDate.fromIso8601("2020-01-21T18:42:18.000Z");
        StringBuilder log = new StringBuilder();
        // test
        testee.mergeEntity(localAccTrans, localEntity, remoteEntity, lastLocalSyncDate, log);
        // verify
        verify(localAccTrans).save(remoteEntity);
        verify(localAccTrans, never()).add(remoteEntity);
    }

    @Test
    public void testMergeAccountTransactionBothModifiedRemoteWasLaterModifiedUserChoiceOurs() {
        // prepare mocks
        Messenger msger = new Messenger(mock(Handler.class));
        DataMerger testee = spy(new DataMerger(msger));
        doReturn(MergeConflictResolution.OURS).when(testee).conflictResolutionByUser(any(), any()); // user ansers "OURS"
        AccountTransaction localEntity = new AccountTransaction();
        localEntity.setLastUpdatedTime("2020-01-22T18:42:18.000Z");
        AccountTransaction remoteEntity = new AccountTransaction();
        remoteEntity.setLastUpdatedTime("2020-01-23T18:42:18.000Z");
        AccountTransactionRepository localAccTrans = mock(AccountTransactionRepository.class);
        MmxDate lastLocalSyncDate = MmxDate.fromIso8601("2020-01-21T18:42:18.000Z");
        StringBuilder log = new StringBuilder();
        // test
        testee.mergeEntity(localAccTrans, localEntity, remoteEntity, lastLocalSyncDate, log);
        // verify
        verify(localAccTrans, never()).save(any());
        verify(localAccTrans, never()).add(any());
        assertEquals(0, log.length());
    }

    @Test
    public void testMergeAccountTransactionBothModifiedLocalWasLaterModifiedUserChoiceOurs() throws RemoteException {
        // prepare mocks
        Messenger msger = mock(Messenger.class);
        final DataMerger testee = spy(new DataMerger(msger));
        doReturn(MergeConflictResolution.OURS).when(testee).conflictResolutionByUser(any(), any()); // user ansers "OURS"
        AccountTransaction localEntity = new AccountTransaction();
        localEntity.setLastUpdatedTime("2020-01-23T18:42:18.000Z");
        AccountTransaction remoteEntity = new AccountTransaction();
        remoteEntity.setLastUpdatedTime("2020-01-22T18:42:18.000Z");
        AccountTransactionRepository localAccTrans = mock(AccountTransactionRepository.class);
        MmxDate lastLocalSyncDate = MmxDate.fromIso8601("2020-01-21T18:42:18.000Z");
        StringBuilder log = new StringBuilder();
        // test
        testee.mergeEntity(localAccTrans, localEntity, remoteEntity, lastLocalSyncDate, log);
        // verify
        verify(localAccTrans, never()).save(any());
        verify(localAccTrans, never()).add(any());
        assertEquals(0, log.length());
    }
}
