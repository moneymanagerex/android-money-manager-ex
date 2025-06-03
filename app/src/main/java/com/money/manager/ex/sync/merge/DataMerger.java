package com.money.manager.ex.sync.merge;

import com.money.manager.ex.R;
import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.OperationCanceledException;
import android.os.RemoteException;

import com.money.manager.ex.MmxContentProvider;
import com.money.manager.ex.core.docstorage.FileStorageHelper;
import com.money.manager.ex.database.MmxOpenHelper;
import com.money.manager.ex.datalayer.IModificationTraceable;
import com.money.manager.ex.datalayer.RepositoryBase;
import com.money.manager.ex.domainmodel.EntityBase;
import com.money.manager.ex.home.DatabaseMetadata;
import com.money.manager.ex.sync.SyncServiceMessage;
import com.money.manager.ex.utils.MmxDatabaseUtils;
import com.money.manager.ex.utils.MmxDate;

import org.jetbrains.annotations.NotNull;

import androidx.annotation.NonNull;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.List;

import timber.log.Timber;

public class DataMerger {

    public static final String REMOTE_ONLY_WAS_MODIFIED = "remote-only was modified:";
    public static final String OVERWRITE_LOCAL_CHANGES = "overwrite local changes:";

    private final Messenger messenger;
    private MergeConflictResolution lastUserResponse;

    private final Object lock = new Object();

    private Context context;

    public DataMerger(@NotNull Messenger messenger, Context context) {
        this.context = context;
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

        List<RepositoryBase> l = MmxContentProvider.getRegisterDataSetForTables();
        for (RepositoryBase d : l) {
            mergeAll(tmpDBReadable, d, lastLocalSyncDate, log);
        }

/*
        // AccountList_v1
        AccountRepository localAccountRepo = new AccountRepository(storage.getContext());
        mergeAll(tmpDBReadable, localAccountRepo, lastLocalSyncDate, log);
        // ASSETS_V1
        AssetRepository localAssetsRepo = new AssetRepository(storage.getContext());
        mergeAll(tmpDBReadable, localAssetsRepo, lastLocalSyncDate, log);
        // ATTACHMENT_V1
        AttachmentRepository localAttachmentRepo = new AttachmentRepository(storage.getContext());
        mergeAll(tmpDBReadable, localAttachmentRepo, lastLocalSyncDate, log);
        // PAYEE
        PayeeRepository localPayeeRepo = new PayeeRepository(storage.getContext());
        mergeAll(tmpDBReadable, localPayeeRepo, lastLocalSyncDate, log);
        // TAG
        TagRepository localTagRepo = new TagRepository(storage.getContext());
        mergeAll(tmpDBReadable, localTagRepo, lastLocalSyncDate, log);
        // CURRENCYFORMATS - Currency
        CurrencyRepository localCurrencyRepo = new CurrencyRepository(storage.getContext());
        mergeAll(tmpDBReadable, localCurrencyRepo, lastLocalSyncDate, log);
        // CURRENCYHISTORY
        CurrencyHistoryRepository localCurrHistRepo = new CurrencyHistoryRepository(storage.getContext());
        mergeAll(tmpDBReadable, localCurrHistRepo, lastLocalSyncDate, log);
        // CUSTOMFIELDDATA
        CustomFieldDataRepository localCustFieldDataRepo = new CustomFieldDataRepository(storage.getContext());
        mergeAll(tmpDBReadable, localCustFieldDataRepo, lastLocalSyncDate, log);
        // CUSTOMFIELD
        CustomFieldRepository localCustFieldRepo = new CustomFieldRepository(storage.getContext());
        mergeAll(tmpDBReadable, localCustFieldRepo, lastLocalSyncDate, log);
        // INFOTABLE
        InfoRepository localInfoRepo = new InfoRepository(storage.getContext());
        mergeAll(tmpDBReadable, localInfoRepo, lastLocalSyncDate, log);
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
        // REPORT
        ReportRepository localReportRepo = new ReportRepository(storage.getContext());
        mergeAll(tmpDBReadable, localReportRepo, lastLocalSyncDate, log);
        // SHAREINFO
        ShareInfoRepository localShareInfo = new ShareInfoRepository(storage.getContext());
        mergeAll(tmpDBReadable, localShareInfo, lastLocalSyncDate, log);
        // Transactions CHECKINGACCOUNT --> accountTransaction
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
        TransactionLinkRepository localTransLinkRepo = new TransactionLinkRepository(storage.getContext());
        mergeAll(tmpDBReadable, localTransLinkRepo, lastLocalSyncDate, log);
*/
    }

    public /* for ut */ <T extends EntityBase> void mergeAll(SupportSQLiteDatabase tmpDBReadable, RepositoryBase<T> localRepo, MmxDate lastLocalSyncDate, StringBuilder log) {
        String msg  = context.getResources().getString(R.string.merge_table) + localRepo.getTableName();
        Timber.d(msg);
        pingMessage(localRepo.getTableName(), 0, 0);
        int updateCount = 0;
        // iterate through all entries from tmp file (remote copy)
        try (Cursor remoteCursor = tmpDBReadable.query("SELECT "+String.join(",", localRepo.getAllColumns())+" from "+localRepo.getTableName()+" WHERE 1")) {
            while (remoteCursor.moveToNext()) {
                pingMessage(localRepo.getTableName(), remoteCursor.getPosition(), remoteCursor.getCount());
                T remoteEntity = localRepo.createEntity();
                remoteEntity.loadFromCursor(remoteCursor);
                // test if local data is equal
                T localEntity = localRepo.load(remoteEntity.getId());
                updateCount += mergeEntity(localRepo, localEntity, remoteEntity, lastLocalSyncDate, log);
            }
        } catch (Exception e) {
            Timber.e(e);
        }
        log.append("Updated ").append(updateCount).append(localRepo.getTableName());
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

    String oldMsg = "";
    int old_act;
    int old_tot;
    void pingMessage(String message, int act, int tot) {
        if ( message.equals(oldMsg) &&
                ( act < ( old_act + 100 ) ) &&
                ( tot == old_tot ) ) {
            return;
        }
        oldMsg = message;
        old_act = act;
        old_tot = tot;

        Message msg = new Message();
        msg.what = SyncServiceMessage.USER_DIALOD_NOTIF.code;
        if ( tot == 0 ) {
            msg.obj = new String[] {message, ""};
        } else {
            msg.obj = new String[] {message, " [" + Integer.toString(act * 100 / tot) + "%]"};
        }
        msg.setAsynchronous(false);
        try {
            messenger.send(msg);
        } catch (Exception e) {
        }
    }

}
