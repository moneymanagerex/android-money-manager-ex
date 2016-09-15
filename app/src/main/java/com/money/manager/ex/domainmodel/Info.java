package com.money.manager.ex.domainmodel;

/**
 * InfoTable entity
 */
public class Info
    extends EntityBase {

    public static final String INFOID = "INFOID";
    public static final String INFONAME = "INFONAME";
    public static final String INFOVALUE = "INFOVALUE";

    public static Info create(String key, String value) {
        Info entity = new Info();
        entity.setName(key);
        entity.setValue(value);
        return entity;
    }

    public int getId() {
        return getInt(INFOID);
    }

    public String getName() {
        return getString(INFONAME);
    }

    public void setName(String name) {
        setString(INFONAME, name);
    }

    public void setValue(String value) {
        setString(INFOVALUE, value);
    }
}
