package com.money.manager.ex.domainmodel;

import android.content.ContentValues;

public class CustomFieldData extends EntityBase {

    // Column constants
    public static final String FIELDATADID = "FIELDATADID";
    public static final String FIELDID = "FIELDID";
    public static final String REFID = "REFID";
    public static final String CONTENT = "CONTENT";

    // Constructor with default values
    public CustomFieldData() {
        super();
    }

    // Constructor with ContentValues for initialization
    public CustomFieldData(ContentValues contentValues) {
        super(contentValues);
    }

    // Override to return the primary key column name
    @Override
    public String getPrimaryKeyColumn() {
        return FIELDATADID;
    }

    // Getters and setters for each column
    public Long getFieldDataId() {
        return getLong(FIELDATADID);
    }

    public void setFieldDataId(Long fieldDataId) {
        setLong(FIELDATADID, fieldDataId);
    }

    public Long getFieldId() {
        return getLong(FIELDID);
    }

    public void setFieldId(Long fieldId) {
        setLong(FIELDID, fieldId);
    }

    public Long getRefId() {
        return getLong(REFID);
    }

    public void setRefId(Long refId) {
        setLong(REFID, refId);
    }

    public String getContent() {
        return getString(CONTENT);
    }

    public void setContent(String content) {
        setString(CONTENT, content);
    }
}