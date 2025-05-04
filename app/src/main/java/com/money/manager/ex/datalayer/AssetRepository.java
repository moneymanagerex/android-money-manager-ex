package com.money.manager.ex.datalayer;

import android.content.Context;

import com.money.manager.ex.database.DatasetType;
import com.money.manager.ex.domainmodel.Asset;

public class AssetRepository extends RepositoryBase<Asset> {

    private static final String TABLE_NAME = "assets_v1";
    private static final String ID_COLUMN = Asset.ASSETID;
    private static final String NAME_COLUMN = Asset.ASSETNAME;

    public AssetRepository(Context context) {
        super(context, TABLE_NAME, DatasetType.TABLE, "asset", ID_COLUMN, NAME_COLUMN);
    }

    @Override
    public Asset createEntity() {
        return new Asset();
    }

    @Override
    public String[] getAllColumns() {
        return new String[] {
                ID_COLUMN + " AS _id",  // Mapping the ASSETID column as _id for SQLite database
                Asset.ASSETID,
                Asset.STARTDATE,
                Asset.ASSETNAME,
                Asset.ASSETSTATUS,
                Asset.CURRENCYID,
                Asset.VALUECHANGEMODE,
                Asset.VALUE,
                Asset.VALUECHANGE,
                Asset.NOTES,
                Asset.VALUECHANGERATE,
                Asset.ASSETTYPE
        };
    }
}