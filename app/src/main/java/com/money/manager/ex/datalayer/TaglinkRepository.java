package com.money.manager.ex.datalayer;

import android.content.Context;
import android.database.Cursor;
import android.nfc.TagLostException;

import com.money.manager.ex.Constants;
import com.money.manager.ex.database.DatasetType;
import com.money.manager.ex.domainmodel.Tag;
import com.money.manager.ex.domainmodel.Taglink;
import com.money.manager.ex.utils.MmxDatabaseUtils;

import java.util.ArrayList;

public class TaglinkRepository extends  RepositoryBase {

    public TaglinkRepository(Context context) {
        super(context, "TAGLINK_V1", DatasetType.TABLE, "taglink");
    }

    @Override
    public String[] getAllColumns() {
        return new String[] { Taglink.TAGLINKID +" AS _id",
                Taglink.TAGLINKID,
                Taglink.REFTYPE,
                Taglink.REFID,
                Taglink.TAGID
        };
    }

    public long add(Taglink entity) {
        return insert(entity.contentValues);
    }

    public boolean delete(Long id) {
        if (id == Constants.NOT_SET) return false;
        long result = delete(Taglink.TAGLINKID + "=?", MmxDatabaseUtils.getArgsForId(id));
        return result > 0;
    }

    public boolean deleteForTransaction ( Long refId ) {
        return deleteForType(refId, Taglink.REFTYPE_TRANSACTION);
    }

    public boolean deleteForType( Long refId, String reftype ) {
        if (refId == Constants.NOT_SET) return false;
        long result = delete(Taglink.REFID + "=? AND " + Taglink.REFTYPE + "=?", new String[] { Long.toString(refId), reftype });
        return result > 0;
    }

    public Taglink load(Long id) {
        if (id == null || id == Constants.NOT_SET) return null;

        Taglink taglink = (Taglink) super.first(Taglink.class,
                getAllColumns(),
                Taglink.TAGLINKID + "=?", MmxDatabaseUtils.getArgsForId(id),
                null);

        return taglink;
    }

    public boolean save(Taglink taglink) {
        long id = taglink.getId();
        return super.update(taglink, Taglink.TAGLINKID + "=" + id);
    }

    public ArrayList<Taglink> loadTaglinkFor(long refId, String refType) {
        Cursor cursor = getContext().getContentResolver().query(getUri(), null,
                Taglink.REFID + "=? AND " + Taglink.REFTYPE +  "=?",
                new String[] { Long.toString(refId),  refType},
                Taglink.TAGLINKID);
        if (cursor == null) return null;

        ArrayList<Taglink> listEntity = new ArrayList<>();

        while (cursor.moveToNext()) {
            Taglink entity = new Taglink();
            entity.loadFromCursor(cursor);

            listEntity.add(entity);
        }
        cursor.close();

        return listEntity;
    }

    public String loadTagsfor(long refId, String refType) {
        ArrayList<Taglink> listEntity = loadTaglinkFor(refId, refType);
        if (listEntity == null) return "";
        String tags = "";
        TagRepository tagRepository = new TagRepository(getContext());
        for (Taglink entity : listEntity) {
            Tag tag = tagRepository.load(entity.getRefId());
            tags += tag.getName() + ",";
        }
        return tags;
    }

}
