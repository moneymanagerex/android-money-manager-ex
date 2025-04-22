package com.money.manager.ex.domainmodel;

import android.content.ContentValues;

public class CustomField extends EntityBase {

    // Column constants
    public static final String FIELDID = "FIELDID";
    public static final String REFTYPE = "REFTYPE";
    public static final String DESCRIPTION = "DESCRIPTION";
    public static final String TYPE = "TYPE";
    public static final String PROPERTIES = "PROPERTIES";

    // Constructor with default values
    public CustomField() {
        super();
    }

    // Constructor with ContentValues for initialization
    public CustomField(ContentValues contentValues) {
        super(contentValues);
    }

    // Override to return the primary key column name
    @Override
    public String getPrimaryKeyColumn() {
        return FIELDID;
    }

    // Getters and setters for each column
    public Long getFieldId() {
        return getLong(FIELDID);
    }

    public void setFieldId(Long fieldId) {
        setLong(FIELDID, fieldId);
    }

    public String getRefType() {
        return getString(REFTYPE);
    }

    public void setRefType(String refType) {
        setString(REFTYPE, refType);
    }

    public String getDescription() {
        return getString(DESCRIPTION);
    }

    public void setDescription(String description) {
        setString(DESCRIPTION, description);
    }

    public String getType() {
        return getString(TYPE);
    }

    public void setType(String type) {
        setString(TYPE, type);
    }

    public String getProperties() {
        return getString(PROPERTIES);
    }

    public void setProperties(String properties) {
        setString(PROPERTIES, properties);
    }
}