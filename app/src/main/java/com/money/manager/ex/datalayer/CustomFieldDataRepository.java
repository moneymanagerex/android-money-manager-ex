package com.money.manager.ex.datalayer;

import android.content.Context;

import com.money.manager.ex.database.DatasetType;
import com.money.manager.ex.domainmodel.CustomFieldData;

public class CustomFieldDataRepository extends RepositoryBase<CustomFieldData> {

    private static final String TABLE_NAME = "customfielddata_v1";
    private static final String ID_COLUMN = CustomFieldData.FIELDATADID;
    private static final String NAME_COLUMN = CustomFieldData.CONTENT;

    public CustomFieldDataRepository(Context context) {
        super(context, TABLE_NAME, DatasetType.TABLE, "customfielddata", ID_COLUMN, NAME_COLUMN);
    }

    @Override
    protected CustomFieldData createEntity() {
        return new CustomFieldData();
    }

    @Override
    public String[] getAllColumns() {
        return new String[] {
                ID_COLUMN + " AS _id",  // Mapping the FIELDATADID column as _id for SQLite database
                CustomFieldData.FIELDATADID,
                CustomFieldData.FIELDID,
                CustomFieldData.REFID,
                CustomFieldData.CONTENT
        };
    }
}
