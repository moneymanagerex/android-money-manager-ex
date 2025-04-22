package com.money.manager.ex.domainmodel;

import android.content.ContentValues;

public class CurrencyHistory extends EntityBase {

    // Column constants
    public static final String CURRHISTID = "CURRHISTID";
    public static final String CURRENCYID = "CURRENCYID";
    public static final String CURRDATE = "CURRDATE";
    public static final String CURRVALUE = "CURRVALUE";
    public static final String CURRUPDTYPE = "CURRUPDTYPE";

    // Constructor with default values
    public CurrencyHistory() {
        super();
    }

    // Constructor with ContentValues for initialization
    public CurrencyHistory(ContentValues contentValues) {
        super(contentValues);
    }

    // Override to return the primary key column name
    @Override
    public String getPrimaryKeyColumn() {
        return CURRHISTID;
    }

    // Getters and setters for each column
    public Long getCurrHistId() {
        return getLong(CURRHISTID);
    }

    public void setCurrHistId(Long currHistId) {
        setLong(CURRHISTID, currHistId);
    }

    public Long getCurrencyId() {
        return getLong(CURRENCYID);
    }

    public void setCurrencyId(Long currencyId) {
        setLong(CURRENCYID, currencyId);
    }

    public String getCurrDate() {
        return getString(CURRDATE);
    }

    public void setCurrDate(String currDate) {
        setString(CURRDATE, currDate);
    }

    public Double getCurrValue() {
        return getDouble(CURRVALUE);
    }

    public void setCurrValue(Double currValue) {
        setDouble(CURRVALUE, currValue);
    }

    public Long getCurrUpdateType() {
        return getLong(CURRUPDTYPE);
    }

    public void setCurrUpdateType(Long currUpdateType) {
        setLong(CURRUPDTYPE, currUpdateType);
    }
}