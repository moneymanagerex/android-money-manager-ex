package com.money.manager.ex.domainmodel;

import android.content.ContentValues;

public class Tag extends EntityBase {
    /* Table
    CREATE TABLE TAG_V1(
        TAGID INTEGER PRIMARY KEY
        , TAGNAME TEXT COLLATE NOCASE NOT NULL UNIQUE
        , ACTIVE INTEGER
        )
     */

    public static final String TAGID = "TAGID";
    public static final String TAGNAME = "TAGNAME";
    public static final String ACTIVE = "ACTIVE";

    public static final Long ACTIVE_TRUE = 1L;

    public Tag() { super(); }
    public Tag(ContentValues contentValues) {
        super(contentValues);
    }

    @Override
    public Long getId() { return getLong(TAGID); }
    @Override
    public void setId(Long id) { setLong(TAGID, id); }

    public String getName() { return getString(TAGNAME); }
    public void setTagname(String value) { setString(TAGNAME, value); }

    public Boolean getActive() { return getLong(ACTIVE)==1L; }
    public void setActive(Boolean value) { setLong(ACTIVE, value ? 1L : 0L); }

}
