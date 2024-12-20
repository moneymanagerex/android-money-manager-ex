package com.money.manager.ex.domainmodel;

public class Tag extends EntityBase {
    /* Table
    CREATE TABLE TAG_V1(
        TAGID INTEGER PRIMARY KEY
        , TAGNAME TEXT COLLATE NOCASE NOT NULL UNIQUE
        , ACTIVE INTEGER
        )
     */

    public static final String ID = "TAGID";
    public static final String NAME = "TAGNAME";
//    public static final String ACTIVE = "ACTIVE";

    public Tag() {
        super(ID, NAME, ACTIVE);
     }

}
