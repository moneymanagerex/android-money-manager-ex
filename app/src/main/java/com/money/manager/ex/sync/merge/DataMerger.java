package com.money.manager.ex.sync.merge;

import android.content.Context;
import android.database.Cursor;

import com.money.manager.ex.Constants;
import com.money.manager.ex.MmexApplication;
import com.money.manager.ex.core.docstorage.FileStorageHelper;
import com.money.manager.ex.database.MmxOpenHelper;
import com.money.manager.ex.datalayer.AccountTransactionRepository;
import com.money.manager.ex.datalayer.IModificationTraceable;
import com.money.manager.ex.datalayer.PayeeRepository;
import com.money.manager.ex.datalayer.RepositoryBase;
import com.money.manager.ex.domainmodel.AccountTransaction;
import com.money.manager.ex.domainmodel.EntityBase;
import com.money.manager.ex.domainmodel.Info;
import com.money.manager.ex.home.DatabaseMetadata;
import com.money.manager.ex.utils.MmxDatabaseUtils;
import com.money.manager.ex.utils.MmxDate;

import java.util.List;

import androidx.sqlite.db.SupportSQLiteDatabase;
import timber.log.Timber;

public class DataMerger {

    public static final String REMOTE_ONLY_WAS_MODIFIED = "remote-only was modified:";
    public static final String OVERWRITE_LOCAL_CHANGES = "overwrite local changes:";

    public void merge(DatabaseMetadata metadata, FileStorageHelper storage) {
        // prepare remote db (readable) -> already downloaded in SyncService
        MmxOpenHelper tmpDBHelper = new MmxOpenHelper(storage.getContext(), metadata.localTmpPath);
        SupportSQLiteDatabase tmpDBReadable = tmpDBHelper.getReadableDatabase();
        // prepare local db (writeable)
        SupportSQLiteDatabase localDB = MmexApplication.getApp().openHelperAtomicReference.get().getWritableDatabase();
        MmxDate lastLocalSyncDate = getLastLocalSyncDate(storage.getContext());
        StringBuilder log = new StringBuilder();
        // merge payee
        PayeeRepository localRepo = new PayeeRepository(storage.getContext());
        mergeAll(tmpDBReadable, localRepo, lastLocalSyncDate, log);
        // merge transactions
        AccountTransactionRepository localAccTrans = new AccountTransactionRepository(storage.getContext());
        mergeAll(tmpDBReadable, localAccTrans, lastLocalSyncDate, log);
    }

    public /* for ut */ <T extends EntityBase> void mergeAll(SupportSQLiteDatabase tmpDBReadable, RepositoryBase<T> localRepo, MmxDate lastLocalSyncDate, StringBuilder log) {
        int updateCount = 0;
        // iterate through all entries from tmp file (remote copy)
        try (Cursor remoteCursor = tmpDBReadable.query("SELECT * from "+localRepo.getTableName()+" WHERE 1")) {
            while (remoteCursor.moveToNext()) {
                T remoteEntity = localRepo.createEntity();
                remoteEntity.loadFromCursor(remoteCursor);
                // test if local data is equal
                T localEntity = localRepo.load(remoteEntity.getId());
                updateCount += mergeEntity(localRepo, localEntity, remoteEntity, lastLocalSyncDate, log);
            }
        } catch (Exception e) {
            Timber.e(e);
        }
        log.append("Updated ").append(updateCount).append(" AccountTransaction");
    }

    public <T extends EntityBase> int mergeEntity(RepositoryBase<T> localRepo, T localEntity, T remoteEntity, MmxDate lastLocalSyncDate, StringBuilder log) {
        int updateCount = 0;
        if (localEntity == null) {
            // new entry from remote
            localRepo.add(remoteEntity);
            updateCount++;
        } else if (! localEntity.equals(remoteEntity)) {
            // not equal --> check what has changed
            boolean modifiedLocallyAfterSync = false;
            boolean modifiedRemoteAfterSync = false;
            MmxDate localChangeDate = lastLocalSyncDate;
            MmxDate remoteChangeDate = lastLocalSyncDate;
            if (localEntity instanceof IModificationTraceable) {
                localChangeDate = new MmxDate(((IModificationTraceable)localEntity).getLastUpdatedTime(), Constants.IOS_8601_COMBINED);
                modifiedLocallyAfterSync = localChangeDate.toDate().after(lastLocalSyncDate.toDate());
                remoteChangeDate = new MmxDate(((IModificationTraceable)remoteEntity).getLastUpdatedTime(), Constants.IOS_8601_COMBINED);
                modifiedRemoteAfterSync = remoteChangeDate.toDate().after(lastLocalSyncDate.toDate());
            }
            // both modified
            if (modifiedLocallyAfterSync && modifiedRemoteAfterSync) {
                // for now we take remote if newer  --> TODO ask user: manual merge decision
                if (localChangeDate.toDate().before(remoteChangeDate.toDate())) {
                    localRepo.save(remoteEntity);
                    updateCount++;
                    log.append(OVERWRITE_LOCAL_CHANGES).append("transaction ").append(localEntity.getId()).append(" ").append(localEntity.toString()).append("\n");
                }
            } else if (! modifiedLocallyAfterSync) {
                // import the remote data
                // this is the default merge action for all entities that do no implement IModificationTraceable
                // This also means that we import the
                localRepo.save(remoteEntity);
                updateCount++;
                log.append(REMOTE_ONLY_WAS_MODIFIED).append("transaction ").append(remoteEntity.getId()).append("\n");
            }
        }
        return updateCount;
    }


    /*
     * go through remote data and compare with local data
     */
   /* public void mergeAccountTransactions(SupportSQLiteDatabase tmpDBReadable, AccountTransactionRepository localAccTrans, MmxDate lastLocalSyncDate, StringBuilder log) {
        int updateCount = 0;
        // iterate through all entries from tmp file (remote copy)
        try (Cursor remoteCursor = tmpDBReadable.query("SELECT * from "+AccountTransactionRepository.TABLE_NAME+" WHERE 1")) {
            while (remoteCursor.moveToNext()) {
                AccountTransaction remoteEntity = new AccountTransaction();
                remoteEntity.loadFromCursor(remoteCursor);
                // test if local data is equal
                AccountTransaction localEntity = localAccTrans.load(remoteEntity.getId());
                updateCount += mergeAccountTransaction(localAccTrans, localEntity, remoteEntity, lastLocalSyncDate, log);
            }
        } catch (Exception e) {
            Timber.e(e);
        }
        log.append("Updated ").append(updateCount).append(" AccountTransaction");
    }*/

    /*public int mergeAccountTransaction(AccountTransactionRepository localAccTrans, AccountTransaction localEntity, AccountTransaction remoteEntity, MmxDate lastLocalSyncDate, StringBuilder log) {
        int updateCount = 0;
        if (localEntity == null) {
            // new entry from remote
            localAccTrans.insert(remoteEntity);
            updateCount++;
        } else if (! localEntity.equals(remoteEntity)) {
            // not equal --> check what has changed
            MmxDate localChangeDate = new MmxDate(localEntity.getLastUpdatedTime(), Constants.IOS_8601_COMBINED);
            boolean modifiedLocallyAfterSync = localChangeDate.toDate().after(lastLocalSyncDate.toDate());
            MmxDate remoteChangeDate = new MmxDate(remoteEntity.getLastUpdatedTime(), Constants.IOS_8601_COMBINED);
            boolean modifiedRemoteAfterSync = remoteChangeDate.toDate().after(lastLocalSyncDate.toDate());
            // both modified
            if (modifiedLocallyAfterSync && modifiedRemoteAfterSync) {
                // for now we take remote if newer  --> TODO ask user: manual merge decision
                if (localChangeDate.toDate().before(remoteChangeDate.toDate())) {
                    localAccTrans.update(remoteEntity);
                    updateCount++;
                    log.append(OVERWRITE_LOCAL_CHANGES).append("transaction ").append(localEntity.getId()).append(" ").append(localEntity.toString()).append("\n");
                }
            } else if (! modifiedLocallyAfterSync) {
                // import the remote data
                localAccTrans.update(remoteEntity);
                updateCount++;
                log.append(REMOTE_ONLY_WAS_MODIFIED).append("transaction ").append(remoteEntity.getId()).append("\n");
            }
        }
        return updateCount;
    }*/

    private MmxDate getLastLocalSyncDate(Context ctx) {
        MmxDatabaseUtils utils = new MmxDatabaseUtils(ctx);
        List<Info> infoList = utils.getLastSyncDate();
        if (infoList != null && ! infoList.isEmpty()) {
            return new MmxDate(infoList.get(0).getString(Info.INFOVALUE));
        }
        return new MmxDate("1900-01-01T00:00:00");
    }
}
