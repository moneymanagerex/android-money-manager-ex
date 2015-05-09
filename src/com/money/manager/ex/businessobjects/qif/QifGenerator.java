/*
 * Copyright (C) 2012-2015 The Android Money Manager Ex Project Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.money.manager.ex.businessobjects.qif;

import android.database.Cursor;

import com.money.manager.ex.adapter.AllDataAdapter;

import java.text.ParseException;

/**
 * Generator of Qif file contents.
 * Parses the transactions and creates qif output.
 * References:
 * http://www.respmech.com/mym2qifw/qif_new.htm
 */
public class QifGenerator implements IQifGenerator {
    public String createFromAdapter(AllDataAdapter adapter)
            throws ParseException {
        StringBuilder builder = new StringBuilder();

        Cursor cursor = adapter.getCursor();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            String row = createRow(cursor);
            builder.append(row);
            cursor.moveToNext();
        }
        cursor.close();

        return builder.toString();
    }

    private String createRow(Cursor cursor)
            throws ParseException {
        QifRecord record = new QifRecord(cursor);
        return record.getString();
    }
}
