package com.money.manager.ex.domainmodel;

import android.content.ContentValues;

public class Asset extends EntityBase {

    // Column constants
    public static final String ASSETID = "ASSETID";
    public static final String STARTDATE = "STARTDATE";
    public static final String ASSETNAME = "ASSETNAME";
    public static final String ASSETSTATUS = "ASSETSTATUS";
    public static final String CURRENCYID = "CURRENCYID";
    public static final String VALUECHANGEMODE = "VALUECHANGEMODE";
    public static final String VALUE = "VALUE";
    public static final String VALUECHANGE = "VALUECHANGE";
    public static final String NOTES = "NOTES";
    public static final String VALUECHANGERATE = "VALUECHANGERATE";
    public static final String ASSETTYPE = "ASSETTYPE";

    // Constructor with default values
    public Asset() {
        super();
    }

    // Constructor with ContentValues for initialization
    public Asset(ContentValues contentValues) {
        super(contentValues);
    }

    // Override to return the primary key column name
    @Override
    public String getPrimaryKeyColumn() {
        return ASSETID;
    }

    // Getters and setters for each column
    public Long getAssetId() {
        return getLong(ASSETID);
    }

    public void setAssetId(Long assetId) {
        setLong(ASSETID, assetId);
    }

    public String getStartDate() {
        return getString(STARTDATE);
    }

    public void setStartDate(String startDate) {
        setString(STARTDATE, startDate);
    }

    public String getAssetName() {
        return getString(ASSETNAME);
    }

    public void setAssetName(String assetName) {
        setString(ASSETNAME, assetName);
    }

    public String getAssetStatus() {
        return getString(ASSETSTATUS);
    }

    public void setAssetStatus(String assetStatus) {
        setString(ASSETSTATUS, assetStatus);
    }

    public Long getCurrencyId() {
        return getLong(CURRENCYID);
    }

    public void setCurrencyId(Long currencyId) {
        setLong(CURRENCYID, currencyId);
    }

    public String getValueChangeMode() {
        return getString(VALUECHANGEMODE);
    }

    public void setValueChangeMode(String valueChangeMode) {
        setString(VALUECHANGEMODE, valueChangeMode);
    }

    public Double getValue() {
        return getDouble(VALUE);
    }

    public void setValue(Double value) {
        setDouble(VALUE, value);
    }

    public String getValueChange() {
        return getString(VALUECHANGE);
    }

    public void setValueChange(String valueChange) {
        setString(VALUECHANGE, valueChange);
    }

    public String getNotes() {
        return getString(NOTES);
    }

    public void setNotes(String notes) {
        setString(NOTES, notes);
    }

    public Double getValueChangeRate() {
        return getDouble(VALUECHANGERATE);
    }

    public void setValueChangeRate(Double valueChangeRate) {
        setDouble(VALUECHANGERATE, valueChangeRate);
    }

    public String getAssetType() {
        return getString(ASSETTYPE);
    }

    public void setAssetType(String assetType) {
        setString(ASSETTYPE, assetType);
    }
}