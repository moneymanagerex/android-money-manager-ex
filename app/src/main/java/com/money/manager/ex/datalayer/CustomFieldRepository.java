package com.money.manager.ex.datalayer;

import android.content.Context;

import com.money.manager.ex.database.DatasetType;
import com.money.manager.ex.domainmodel.CustomField;

public class CustomFieldRepository extends RepositoryBase<CustomField> {

    private static final String TABLE_NAME = "customfield_v1";
    private static final String ID_COLUMN = CustomField.FIELDID;
    private static final String NAME_COLUMN = CustomField.DESCRIPTION;

    public CustomFieldRepository(Context context) {
        super(context, TABLE_NAME, DatasetType.TABLE, "customfield", ID_COLUMN, NAME_COLUMN);
    }

    @Override
    protected CustomField createEntity() {
        return new CustomField();
    }

    @Override
    public String[] getAllColumns() {
        return new String[] {
                ID_COLUMN + " AS _id",  // Mapping the FIELDID column as _id for SQLite database
                CustomField.FIELDID,
                CustomField.REFTYPE,
                CustomField.DESCRIPTION,
                CustomField.TYPE,
                CustomField.PROPERTIES
        };
    }
}