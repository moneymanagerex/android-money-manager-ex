package com.money.manager.ex.datalayer;

import android.content.Context;

import com.money.manager.ex.database.DatasetType;
import com.money.manager.ex.domainmodel.Info;

public class InfoRepository extends RepositoryBase<Info> {


    private static final String TABLE_NAME = "infotable_v1";
    private static final String ID_COLUMN =  Info.INFOID;
    private static final String NAME_COLUMN = Info.INFONAME;

    public InfoRepository(Context context) {
        super(context, TABLE_NAME, DatasetType.TABLE, "infotable", ID_COLUMN, NAME_COLUMN);
    }

    @Override
    public Info createEntity() {
        return new Info();
    }

    @Override
    public String[] getAllColumns() {
        return new String[] {ID_COLUMN+" as _id",
                Info.INFONAME,
                Info.INFOVALUE
        };
    }
}
