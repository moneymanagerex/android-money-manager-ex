package com.money.manager.ex.datalayer;

import android.content.Context;

import com.money.manager.ex.database.DatasetType;
import com.money.manager.ex.domainmodel.ShareInfo;

public class ShareInfoRepository
        extends RepositoryBase<ShareInfo>{
    private static final String TABLE_NAME = "shareinfo_v1";
    private static final String ID_COLUMN = ShareInfo.SHAREINFOID;
    private static final String NAME_COLUMN = ShareInfo.SHARENUMBER;

    public ShareInfoRepository(Context context) {
        super(context, TABLE_NAME,  DatasetType.TABLE, "shareinfo", ID_COLUMN, NAME_COLUMN);
    }

    @Override
    public ShareInfo createEntity() {
        return new ShareInfo();
    }

    @Override
    public String[] getAllColumns() {
        return new String[]{ID_COLUMN + " AS _id",
                ShareInfo.SHARENUMBER,
                ShareInfo.SHAREPRICE,
                ShareInfo.SHARECOMMISSION,
                ShareInfo.SHARELOT
        };
    }
}
