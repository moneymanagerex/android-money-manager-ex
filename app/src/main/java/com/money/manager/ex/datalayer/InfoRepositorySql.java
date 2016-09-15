package com.money.manager.ex.datalayer;

import android.database.Cursor;

import com.money.manager.ex.core.InfoKeys;
import com.money.manager.ex.domainmodel.Info;
import com.squareup.sqlbrite.BriteDatabase;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * Repository for InfoTable
 */
public class InfoRepositorySql
    extends SqlRepositoryBase<Info> {

    public static final String TABLE_NAME = "infotable_v1";

    @Inject
    public InfoRepositorySql(BriteDatabase db) {
        super(TABLE_NAME, db);
    }

//    @Override
//    public String[] getAllColumns() {
//        return new String[] {"INFOID AS _id", Info.INFOID, Info.INFONAME, Info.INFOVALUE};
//    }

    public List<Info> loadAll(String infoName) {
        String sql = new Query()
            .select()
            .where(Info.INFONAME + "=?", new String[] { infoName })
            .toString();

        Cursor c = this.query(sql);
        if (c == null) return null;

        List<Info> results = new ArrayList<>();
        while (c.moveToNext()) {
            Info entity = new Info();
            entity.loadFromCursor(c);
            results.add(entity);
        }

        return results;
    }

    public int delete(long id) {
        String idString = String.valueOf(id);
        return this.delete(Info.INFOID + "=?", idString);
    }

    public long insert(Info value) {
        return insert(value.contentValues);
    }

    public boolean update(Info entity) {
        return update(entity, Info.INFONAME + "=?", InfoKeys.PASSCODE);
    }
}
