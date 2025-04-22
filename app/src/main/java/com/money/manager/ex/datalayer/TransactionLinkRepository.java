package com.money.manager.ex.datalayer;

import android.content.Context;

import com.money.manager.ex.database.DatasetType;
import com.money.manager.ex.domainmodel.TransactionLink;

public class TransactionLinkRepository extends RepositoryBase<TransactionLink> {

    private static final String TABLE_NAME = "translink_v1";
    private static final String ID_COLUMN = TransactionLink.TRANSLINKID;
    private static final String NAME_COLUMN = TransactionLink.LINKTYPE;

    public TransactionLinkRepository(Context context) {
        super(context, TABLE_NAME, DatasetType.TABLE, "translink", ID_COLUMN, NAME_COLUMN);
    }

    @Override
    protected TransactionLink createEntity() {
        return new TransactionLink();
    }

    @Override
    public String[] getAllColumns() {
        return new String[] {
                ID_COLUMN + " AS _id",  // Mapping the TRANSLINKID column as _id for SQLite database
                TransactionLink.TRANSLINKID,
                TransactionLink.CHECKINGACCOUNTID,
                TransactionLink.LINKTYPE,
                TransactionLink.LINKRECORDID
        };
    }
}