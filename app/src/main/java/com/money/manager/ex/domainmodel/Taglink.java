package com.money.manager.ex.domainmodel;

import android.content.ContentValues;

public class Taglink extends EntityBase  {

    /* Table
    CREATE TABLE TAGLINK_V1(
        TAGLINKID INTEGER PRIMARY KEY
        , REFTYPE TEXT NOT NULL
        , REFID INTEGER NOT NULL
        , TAGID INTEGER NOT NULL
        , UNIQUE(REFTYPE, REFID, TAGID)
        )
     */
    public static final String REFTYPE_TRANSACTION = "Transaction";
    public static final String TAGLINKID = "TAGLINKID";
    public static final String REFTYPE = "REFTYPE";
    public static final String REFID = "REFID";
    public static final String TAGID = "TAGID";

    public Taglink() { super(); }
    public Taglink(ContentValues contentValues) {
        super(contentValues);
    }

    @Override
    public Long getId() { return getLong(TAGLINKID); }

    @Override
    public void setId(Long id) { setLong(TAGLINKID, id); }

    public String getRefType() { return getString(REFTYPE); }
    public void setRefType(String value) { setString(REFTYPE, value); }

    public Long getRefId() { return getLong(REFID); }
    public void setRefId(Long value) { setLong(REFID, value); }

    public long getTagId() { return getLong(TAGID); }
    public void setTagId(long value) { setLong(TAGID, value); }

}
