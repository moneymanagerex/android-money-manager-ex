/*
 * Copyright (C) 2012-2018 The Android Money Manager Ex Project Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.money.manager.ex.servicelayer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;

import com.money.manager.ex.Constants;
import com.money.manager.ex.database.ITransactionEntity;
import com.money.manager.ex.datalayer.AccountTransactionRepository;
import com.money.manager.ex.datalayer.TagRepository;
import com.money.manager.ex.datalayer.TaglinkRepository;
import com.money.manager.ex.domainmodel.Tag;

/**
 */
public class TagService
        extends ServiceBase {

    public TagService(Context context) {
        super(context);

        this.tagRepository = new TagRepository(context);
    }

    private final TagRepository tagRepository;

    public Tag loadByName(String name) {
        return tagRepository.loadByName(name);
    }

    public long loadIdByName(String name) {
        Tag tag = loadByName(name);
        if (tag == null)
            return Constants.NOT_SET;

        return tag.getId();
    }

    public Tag createNew(String name) {
        if (TextUtils.isEmpty(name)) return null;

        name = name.trim();

        Tag tag = new Tag();
        tag.setName(name);
        tag.setActive(true);
        long id = this.tagRepository.add(tag);
        tag.setId(id);

        return tag;
    }

    public boolean exists(String name) {
        name = name.trim();

        Tag tag = loadByName(name);
        return (tag != null);
    }

    public boolean isUsed(long tagId) {
        // todo non funziona
        TaglinkRepository repo = new TaglinkRepository(getContext());
        long links = repo.count( Tag.TAGID + "=?", new String[]{Long.toString(tagId)});
        return links > 0;
    }

    public long update(long id, String name) {
        if(TextUtils.isEmpty(name)) return Constants.NOT_SET;

        name = name.trim();

        ContentValues values = new ContentValues();
        values.put(Tag.TAGNAME, name);

        return getContext().getContentResolver().update(tagRepository.getUri(),
                values,
                Tag.TAGID + "=?",
                new String[]{Long.toString(id)});
    }

    public long update(Tag tag) {
        if (tag == null) return  Constants.NOT_SET;
        if(TextUtils.isEmpty(tag.getName())) return Constants.NOT_SET;
        if(tag.getId() == null) return Constants.NOT_SET;

        tag.setName(tag.getName().trim());
        ContentValues values = tag.getContentValues();
        values.remove("_id");

        return getContext().getContentResolver().update(tagRepository.getUri(),
                values,
                Tag.TAGID + "=?",
                new String[]{Long.toString(tag.getId())});
    }

}
