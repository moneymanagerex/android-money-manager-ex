package com.money.manager.ex.datalayer;

import android.content.Context;
import android.database.Cursor;

import com.money.manager.ex.Constants;
import com.money.manager.ex.database.DatasetType;
import com.money.manager.ex.database.WhereStatementGenerator;
import com.money.manager.ex.domainmodel.Tag;
import com.money.manager.ex.settings.AppSettings;
import com.money.manager.ex.utils.MmxDatabaseUtils;

import java.util.ArrayList;

public class TagRepository extends  RepositoryBase {
    public TagRepository(Context context) {
        super(context, "TAG_V1", DatasetType.TABLE, "tag");
    }

    public static final int SORT_BY_NAME = 0;
    public static final int SORT_BY_FREQUENCY = 1;
    public static final int SORT_BY_RECENT = 2;
    private static final String ORDER_BY_NAME = "UPPER(" + Tag.TAGNAME + ")";
    private static final String ORDER_BY_USAGE = "(SELECT COUNT(*) FROM TAGLINK_V1 WHERE T.TAGID = TAGLINK_V1.TAGID ) DESC";
    private static final String ORDER_BY_RECENT =
            "( SELECT max( TRANSDATE ) \n" +
                    "FROM TAGLINK_V1\n" +
                    "INNER join CHECKINGACCOUNT_V1 on CHECKINGACCOUNT_V1.TRANSID = TAGLINK_V1.REFID\n" +
                    "WHERE T.TAGID = TAGLINK_V1.TAGID\n" +
                    ") DESC";


    @Override
    public String[] getAllColumns() {
        return new String[] {Tag.TAGID +" AS _id",
                Tag.TAGID,
                Tag.TAGNAME,
                Tag.ACTIVE
        };
    }

    public boolean delete(Long id) {
        if (id == Constants.NOT_SET) return false;
        // TODO: Tag has inactive flag: no delete, but set inactive
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
//                "lower (" + Tag.TAGNAME + ")");
                getOrderByFromCode());

        ArrayList<Tag> listEntity = new ArrayList<>();

        while (cursor.moveToNext()) {
            Tag entity = new Tag();
            entity.loadFromCursor(cursor);
            listEntity.add(entity);
        }
        cursor.close();

        return listEntity;
    }

    public String getOrderByFromCode(int sort) {
        switch(sort) {
            case SORT_BY_FREQUENCY:
                return ORDER_BY_USAGE;
            case SORT_BY_RECENT:
                return ORDER_BY_RECENT;
            default: // SORT_BY_NAME
                return ORDER_BY_NAME;
        }
    }

    public String getOrderByFromCode() {
        AppSettings settings = new AppSettings(getContext());
        return  getOrderByFromCode(settings.getTagSort());
    }


}
