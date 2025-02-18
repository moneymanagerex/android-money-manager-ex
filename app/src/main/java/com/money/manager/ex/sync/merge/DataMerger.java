package com.money.manager.ex.sync.merge;

import android.content.Context;
import android.database.Cursor;
import android.os.OperationCanceledException;

import com.money.manager.ex.core.docstorage.FileStorageHelper;
import com.money.manager.ex.database.MmxOpenHelper;
import com.money.manager.ex.datalayer.AccountTransactionRepository;
import com.money.manager.ex.datalayer.IModificationTraceable;
import com.money.manager.ex.datalayer.PayeeRepository;
import com.money.manager.ex.datalayer.RepositoryBase;
import com.money.manager.ex.domainmodel.EntityBase;
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
            boolean modifiedLocallyAfterSync = true;
            boolean modifiedRemoteAfterSync = true;
            if (localEntity instanceof IModificationTraceable) {
                // only for entities that implements IModificationTraceable we can determine if they are modified or not
                MmxDate localChangeDate = MmxDate.fromIso8601(((IModificationTraceable)localEntity).getLastUpdatedTime());
                modifiedLocallyAfterSync = localChangeDate.toDate().after(lastLocalSyncDate.toDate());
                MmxDate remoteChangeDate = MmxDate.fromIso8601(((IModificationTraceable)remoteEntity).getLastUpdatedTime());
                modifiedRemoteAfterSync = remoteChangeDate.toDate().after(lastLocalSyncDate.toDate());
            }

            if (! modifiedLocallyAfterSync && modifiedRemoteAfterSync) {
                // only remote modified
                localRepo.save(remoteEntity);
                updateCount++;
                log.append(REMOTE_ONLY_WAS_MODIFIED).append("transaction ").append(remoteEntity.getId()).append("\n");
            } else if (modifiedLocallyAfterSync && modifiedRemoteAfterSync) {
                // both modified -> ask user
                MergeConflictResolution resolution = conflictResolutionByUser(localEntity, remoteEntity);
                switch (resolution) {
                    case THEIRS:
                        localRepo.save(remoteEntity);
                        updateCount++;
                        log.append(OVERWRITE_LOCAL_CHANGES).append("transaction ").append(localEntity.getId()).append(" ").append(localEntity).append("\n");
                        break;
                    case OURS:
                       // do nothing, we use the local change
                        break;
                    case ABORT:
                        // fall-through
                    default:
                        // default:. abort
                        throw new OperationCanceledException("Merge databases canceled by user");
                }
            } // otherwise we don't have to do anything (local change only)
        }
        return updateCount;
    }

    public <T extends EntityBase> MergeConflictResolution conflictResolutionByUser(T localEntity, T remoteEntity) {
        // FIXME create user dialog
        return MergeConflictResolution.THEIRS;
    }

    private MmxDate getLastLocalSyncDate(Context ctx) {
        MmxDatabaseUtils utils = new MmxDatabaseUtils(ctx);
        MmxDate lastSyncDate = utils.getLastSyncDate();
        if (lastSyncDate != null) {
            return lastSyncDate;
        }
        // as a default (when the database has not yet an entry)
        return new MmxDate("1900-01-01T00:00:00.000+0200");
    }
}
