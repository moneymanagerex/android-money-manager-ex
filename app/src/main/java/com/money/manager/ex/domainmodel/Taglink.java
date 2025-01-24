package com.money.manager.ex.domainmodel;

import android.content.ContentValues;

import java.util.ArrayList;

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
    public static final String REFTYPE_RECURRING_TRANSACTION = "RecurringTransaction";
    public static final String REFTYPE_TRANSACTION_SPLIT = "TransactionSplit";
    public static final String REFTYPE_RECURRING_TRANSACTION_SPLIT = "RecurringTransactionSplit";

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

    public boolean inTaglinkList(ArrayList<Taglink> list ) {
        for( Taglink entity : list ) {
            if ( entity.getId() == getId() )
                return true;
        }
        return false;
    }

    public static ArrayList<Taglink> clearCrossReference(ArrayList<Taglink> list) {
        for (Taglink entity : list) {
          entity.setRefType(null);
          entity.setRefId(null);
          entity.setId(null);
        }
        return list;
    }
}
