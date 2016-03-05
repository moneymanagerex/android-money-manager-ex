package com.money.manager.ex.datalayer;

import android.content.Context;

import com.money.manager.ex.database.DatasetType;
import com.money.manager.ex.domainmodel.Info;

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
        List<Info> results = this.query(Info.class,
            getAllColumns(),
            Info.INFONAME + "=?",
            new String[] { infoName },
            null);

        return results;
    }

    public int delete(long id) {
        String idString = String.valueOf(id);
        return this.delete(Info.INFOID + "=?", new String[] { idString });
    }
}
