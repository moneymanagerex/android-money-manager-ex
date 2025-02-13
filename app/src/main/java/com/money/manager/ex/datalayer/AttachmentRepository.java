/*
 * Copyright (C) 2024 The Android Money Manager Ex Project Team
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
package com.money.manager.ex.datalayer;

import android.content.Context;
import android.database.Cursor;

import com.money.manager.ex.Constants;
import com.money.manager.ex.database.DatasetType;
import com.money.manager.ex.domainmodel.Attachment;
import com.money.manager.ex.utils.MmxDatabaseUtils;

import java.util.ArrayList;

/**
 * Attachment repository
 */
public class AttachmentRepository
    extends RepositoryBase<Attachment> {

    private static final String TABLE_NAME = "attachment_v1";
    private static final String ID_COLUMN = Attachment.ATTACHMENTID;

    public AttachmentRepository(Context context) {
        super(context, TABLE_NAME, DatasetType.TABLE, "attachment", ID_COLUMN);
    }

    @Override
    protected Attachment createEntity() {
        return new Attachment();
    }

    @Override
    public String[] getAllColumns() {
        return new String[] { "ATTACHMENTID AS _id",
            Attachment.ATTACHMENTID,
            Attachment.REFTYPE,
            Attachment.REFID,
            Attachment.DESCRIPTION,
            Attachment.FILENAME
        };
    }

    public ArrayList<Attachment> loadAttachmentsFor(long refId, String refType) {
        Cursor curAtt = getContext().getContentResolver().query(getUri(), null,
                Attachment.REFID + "=? AND " + Attachment.REFTYPE +  "=?",
                new String[] { Long.toString(refId),  refType},
                Attachment.ATTACHMENTID);
        if (curAtt == null) return null;

        ArrayList<Attachment> listAtts = new ArrayList<>();

        while (curAtt.moveToNext()) {
            Attachment attachment = new Attachment();
            attachment.loadFromCursor(curAtt);

            listAtts.add(attachment);
        }
        curAtt.close();

        return listAtts;
    }
}
