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

public class TagRepository extends RepositoryBase <Tag> {

    private static final String TABLE_NAME = "TAG_V1";
    private static final String ID_COLUMN = Tag.TAGID;

    public TagRepository(Context context) {
        super(context, TABLE_NAME, DatasetType.TABLE, "tag", ID_COLUMN);
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
    protected Tag createEntity() {
        return new Tag();
    }

    @Override
    public String[] getAllColumns() {
        return new String[] {ID_COLUMN +" AS _id",
                Tag.TAGID,
                Tag.TAGNAME,
                Tag.ACTIVE
        };
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
