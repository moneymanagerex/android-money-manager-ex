package com.money.manager.ex.sync.merge;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.OperationCanceledException;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;

import com.money.manager.ex.MmexApplication;
import com.money.manager.ex.core.docstorage.FileStorageHelper;
import com.money.manager.ex.database.MmxOpenHelper;
import com.money.manager.ex.datalayer.AccountRepository;
import com.money.manager.ex.datalayer.AccountTransactionRepository;
import com.money.manager.ex.datalayer.AttachmentRepository;
import com.money.manager.ex.datalayer.BudgetEntryRepository;
import com.money.manager.ex.datalayer.BudgetRepository;
import com.money.manager.ex.datalayer.CategoryRepository;
import com.money.manager.ex.datalayer.IModificationTraceable;
import com.money.manager.ex.datalayer.PayeeRepository;
import com.money.manager.ex.datalayer.ReportRepository;
import com.money.manager.ex.datalayer.RepositoryBase;
import com.money.manager.ex.datalayer.ScheduledTransactionRepository;
import com.money.manager.ex.datalayer.SplitCategoryRepository;
import com.money.manager.ex.datalayer.SplitScheduledCategoryRepository;
import com.money.manager.ex.datalayer.StockHistoryRepository;
import com.money.manager.ex.datalayer.StockRepository;
import com.money.manager.ex.datalayer.TagRepository;
import com.money.manager.ex.datalayer.TaglinkRepository;
import com.money.manager.ex.domainmodel.EntityBase;
import com.money.manager.ex.home.DatabaseMetadata;
import com.money.manager.ex.sync.SyncServiceMessage;
import com.money.manager.ex.sync.SyncServiceMessageHandler;
import com.money.manager.ex.transactions.CheckingTransactionEditActivity;
import com.money.manager.ex.utils.MmxDatabaseUtils;
import com.money.manager.ex.utils.MmxDate;

import org.jetbrains.annotations.NotNull;

import androidx.annotation.NonNull;
import androidx.sqlite.db.SupportSQLiteDatabase;
import timber.log.Timber;

public class DataMerger {

    public static final String REMOTE_ONLY_WAS_MODIFIED = "remote-only was modified:";
    public static final String OVERWRITE_LOCAL_CHANGES = "overwrite local changes:";

    private final Messenger messenger;
    private MergeConflictResolution lastUserResponse;

    private final Object lock = new Object();

    public DataMerger(@NotNull Messenger messenger) {
        this.messenger = messenger;
    }

    public void merge(DatabaseMetadata metadata, FileStorageHelper storage) {
        // prepare remote db (readable) -> already downloaded in SyncService
        MmxOpenHelper tmpDBHelper = new MmxOpenHelper(storage.getContext(), metadata.localTmpPath);
        SupportSQLiteDatabase tmpDBReadable = tmpDBHelper.getReadableDatabase();
        // prepare local db (writeable)
        MmxDate lastLocalSyncDate = getLastLocalSyncDate(storage.getContext());
        StringBuilder log = new StringBuilder();
        // merge table by table, start with the leafs in the structure
        // AccountList_v1
        AccountRepository localAccountRepo = new AccountRepository(storage.getContext());
        mergeAll(tmpDBReadable, localAccountRepo, lastLocalSyncDate, log);
        // ASSETS_V1
        // ??
        // ATTACHMENT_V1
        AttachmentRepository localAttachmentRepo = new AttachmentRepository(storage.getContext());
        mergeAll(tmpDBReadable, localAttachmentRepo, lastLocalSyncDate, log);
        // PAYEE
        PayeeRepository localPayeeRepo = new PayeeRepository(storage.getContext());
        mergeAll(tmpDBReadable, localPayeeRepo, lastLocalSyncDate, log);
        // TAG
        TagRepository localTagRepo = new TagRepository(storage.getContext());
        mergeAll(tmpDBReadable, localTagRepo, lastLocalSyncDate, log);
        // CURRENCYFORMATS
        // ??
        // CURRENCYHISTORY
        // ??
        // CUSTOMFIELDDATA
        // ??
        // CUSTOMFIELD
        // ??
        // INFOTABLE
        // ??
        // CATEGORY_V1
        CategoryRepository localCatRepo = new CategoryRepository(storage.getContext());
        mergeAll(tmpDBReadable, localCatRepo, lastLocalSyncDate, log);
        // BILLSDEPOSITS_V1
        ScheduledTransactionRepository localBillDepoRepo = new ScheduledTransactionRepository(storage.getContext());
        mergeAll(tmpDBReadable, localBillDepoRepo, lastLocalSyncDate, log);
        // BUDGETSPLITTRANSACTIONNS_V1
        SplitScheduledCategoryRepository localBudgetSplitTranRepo = new SplitScheduledCategoryRepository(storage.getContext());
        mergeAll(tmpDBReadable, localBudgetSplitTranRepo, lastLocalSyncDate, log);
        // BUDGETTABLE_V1
        BudgetEntryRepository localBudgetTableRepo = new BudgetEntryRepository(storage.getContext());
        mergeAll(tmpDBReadable, localBudgetTableRepo, lastLocalSyncDate, log);
        // BUDGETYEAR_V1
        BudgetRepository localBudgetRepo = new BudgetRepository(storage.getContext());
        mergeAll(tmpDBReadable, localBudgetRepo, lastLocalSyncDate, log);
        // CHECKINGACCOUNT
        // ??
        // REPORT
        ReportRepository localReportRepo = new ReportRepository(storage.getContext());
        mergeAll(tmpDBReadable, localReportRepo, lastLocalSyncDate, log);
        // SHAREINFO
        // ??
        // Transactions
        AccountTransactionRepository localAccTrans = new AccountTransactionRepository(storage.getContext());
        mergeAll(tmpDBReadable, localAccTrans, lastLocalSyncDate, log);
        // SPLITTRANSACTION
        SplitCategoryRepository localSplitTransRepo = new SplitCategoryRepository(storage.getContext());
        mergeAll(tmpDBReadable, localSplitTransRepo, lastLocalSyncDate, log);
        // STOCK
        StockRepository localStockRepo = new StockRepository(storage.getContext());
        mergeAll(tmpDBReadable, localStockRepo, lastLocalSyncDate, log);
        // STOCKHISTORY
        StockHistoryRepository localStockHistRepo = new StockHistoryRepository(storage.getContext());
        mergeAll(tmpDBReadable, localStockHistRepo, lastLocalSyncDate, log);
        // TAGLINK
        TaglinkRepository localTagLinkRepo = new TaglinkRepository(storage.getContext());
        mergeAll(tmpDBReadable, localTagLinkRepo, lastLocalSyncDate, log);
        //TRANSLINK
        // ??
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
                MmxDate localChangeDate = ((IModificationTraceable)localEntity).getLastUpdatedTime();
                modifiedLocallyAfterSync = localChangeDate.toDate().after(lastLocalSyncDate.toDate());
                MmxDate remoteChangeDate = ((IModificationTraceable)remoteEntity).getLastUpdatedTime();
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
        // send the question to user dialog
        Message msg = new Message();
        msg.what = SyncServiceMessage.USER_DIALOG_CONFLICT.code;
        msg.obj = localEntity.getDiffString(remoteEntity);
        msg.setAsynchronous(false);
        Handler handler = new ResponseHandler();
        msg.replyTo = new Messenger(handler);
        try {
            Timber.d("Asking user for conflict solution");
            messenger.send(msg);
        } catch (RemoteException e) {
            Timber.e(e);
            return MergeConflictResolution.ABORT;
        }
        // wait for response
        try {
            Timber.d("Waiting for user response");
            synchronized (lock) {
                lock.wait();
            }
            // return the last response and continue
            MergeConflictResolution resp = lastUserResponse;// received by {@link #handleMessage}
            lastUserResponse = null; // reset
            return resp;
        } catch (InterruptedException e) {
            Timber.e(e);
            return MergeConflictResolution.ABORT;
        } finally {
            Timber.d("Continue processing");
        }
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

    @SuppressLint("HandlerLeak")
    private class ResponseHandler extends Handler{
        public ResponseHandler() {
            super(Looper.getMainLooper());
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            Timber.d("Response received");
            lastUserResponse = MergeConflictResolution.values()[msg.what];
            synchronized (lock) {
                lock.notify();
            }
        }
    }

}
