package com.money.manager.ex.datalayer;

import android.content.ContentValues;
import android.content.Context;

import com.money.manager.ex.database.DatasetType;
import com.money.manager.ex.domainmodel.Info;
import com.money.manager.ex.servicelayer.InfoService;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository for InfoTable
 */
public class InfoRepository
    extends RepositoryBase<Info> {

    public InfoRepository(Context context) {
        super(context, "infotable_v1", DatasetType.TABLE, "infotable");
    }

    @Override
    public String[] getAllColumns() {
        return new String[] {"INFOID AS _id", Info.INFOID, Info.INFONAME, Info.INFOVALUE};
    }

    public List<Info> loadAll(String infoName) {
        ContentValues[] results = this.query(getAllColumns(),
            Info.INFONAME + "=?",
            new String[] { infoName },
            null);

        List<Info> result = new ArrayList<>();
        for (ContentValues record : results) {
            Info entity = new Info();
            entity.contentValues.putAll(record);
            result.add(entity);
        }

        return result;
    }

    public int delete(long id) {
        String idString = String.valueOf(id);
        return this.delete(Info.INFOID + "=?", new String[] { idString });
    }
}
