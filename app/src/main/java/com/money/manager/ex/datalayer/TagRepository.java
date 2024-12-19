package com.money.manager.ex.datalayer;

import android.content.Context;
import android.database.Cursor;

import com.money.manager.ex.Constants;
import com.money.manager.ex.account.AccountStatuses;
import com.money.manager.ex.account.AccountTypes;
import com.money.manager.ex.database.DatasetType;
import com.money.manager.ex.database.WhereStatementGenerator;
import com.money.manager.ex.domainmodel.Account;
import com.money.manager.ex.domainmodel.Tag;
import com.money.manager.ex.domainmodel.Taglink;
import com.money.manager.ex.utils.MmxDatabaseUtils;

import java.util.ArrayList;

public class TagRepository extends  RepositoryBase {
    public TagRepository(Context context) {
        super(context, "TAG_V1", DatasetType.TABLE, "tag");
    }

    @Override
    public String[] getAllColumns() {
        return new String[] {Tag.TAGID +" AS _id",
                Tag.TAGID,
                Tag.TAGNAME,
                Tag.ACTIVE
        };
    }

    public long add(Tag entity) {
        return insert(entity.contentValues);
    }

    public boolean delete(Long id) {
        if (id == Constants.NOT_SET) return false;
        long result = delete(Tag.TAGID + "=?", MmxDatabaseUtils.getArgsForId(id));
        return result > 0;
    }

    public Tag load(Long id) {
        if (id == null || id == Constants.NOT_SET) return null;

        Tag entity = (Tag) super.first(Tag.class,
                getAllColumns(),
                Tag.TAGID + "=?", MmxDatabaseUtils.getArgsForId(id),
                null);

        return entity;
    }

    public boolean save(Tag entity) {
        long id = entity.getId();
        return super.update(entity, Tag.TAGID + "=" + id);
    }

    public ArrayList<Tag> getAllActiveTag() {
        WhereStatementGenerator where = new WhereStatementGenerator();
        where.addStatement(Tag.ACTIVE, "=", Tag.ACTIVE_TRUE);

        Cursor cursor = openCursor(this.getAllColumns(),
                where.getWhere(),
                null,
                "lower (" + Tag.TAGNAME + ")");

        ArrayList<Tag> listEntity = new ArrayList<>();

        while (cursor.moveToNext()) {
            Tag entity = new Tag();
            entity.loadFromCursor(cursor);
            listEntity.add(entity);
        }
        cursor.close();

        return listEntity;
    }

}
