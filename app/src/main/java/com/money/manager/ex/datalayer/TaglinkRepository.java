package com.money.manager.ex.datalayer;

import android.content.Context;

import com.money.manager.ex.Constants;
import com.money.manager.ex.database.DatasetType;
import com.money.manager.ex.domainmodel.RefType;
import com.money.manager.ex.domainmodel.Tag;
import com.money.manager.ex.domainmodel.TagLink;

import java.util.ArrayList;

public class TaglinkRepository extends RepositoryBase <TagLink> {

    private static final String TABLE_NAME = "TAGLINK_V1";
    private static final String ID_COLUMN = TagLink.TAGLINKID;
    private static final String NAME_COLUMN = "";

    public TaglinkRepository(Context context) {
        super(context, TABLE_NAME, DatasetType.TABLE, "taglink", ID_COLUMN, NAME_COLUMN);
    }

    @Override
    protected TagLink createEntity() {
        return new TagLink();
    }

    @Override
    public String[] getAllColumns() {
        return new String[] { ID_COLUMN +" AS _id",
                TagLink.TAGLINKID,
                TagLink.REFTYPE,
                TagLink.REFID,
                TagLink.TAGID
        };
    }

    public boolean deleteForType(Long refId, RefType refType) {
        if (refId == Constants.NOT_SET) return false;
        long result = delete(TagLink.REFID + "=? AND " + TagLink.REFTYPE + "=?", new String[] { Long.toString(refId), refType.getValue()});
        return result > 0;
    }

    public boolean save(ArrayList<TagLink> tagLinks) {
        for (TagLink taglink : tagLinks) {
            if ( ! save(taglink) )
                return false;
        }
        return true;
    }

    public boolean saveAllFor(RefType refType, long refId, ArrayList<TagLink> tagLinks) {
        if ( tagLinks == null || tagLinks.size() == 0 ) {
            deleteForType(refId, refType);
            return true;
        }
        ArrayList<TagLink> old = loadByRef(refId, refType);
        for (TagLink entity : old) {
            if (!entity.inTaglinkList(tagLinks))
                delete(entity.getId());
        }

        // be sure to set refid
        for (int i = 0; i < tagLinks.size(); i++ ) {
            tagLinks.get(i).setRefId(refId);
            tagLinks.get(i).setRefType(refType);
        }
        return save(tagLinks);
    }

    public ArrayList<TagLink> loadByRef(long refId, RefType refType) {

        return new ArrayList<>(query(new Select(getAllColumns())
                .where(TagLink.REFID + "= ? AND " + TagLink.REFTYPE +  "= ?"
                        , Long.toString(refId), refType.getValue())
                .orderBy(ID_COLUMN)));
    }

    public String loadTagsfor(ArrayList<TagLink> listEntity) {
        if (listEntity == null) return "";
        String tags = "";
        TagRepository tagRepository = new TagRepository(getContext());
        for (TagLink entity : listEntity) {
            Tag tag = tagRepository.load(entity.getTagId());
            if (tag != null ) {
                if ( tags.isEmpty() )
                    tags = tag.getName();
                else
                    tags += ", " + tag.getName();
            }
        }
        return tags;
    }


}
