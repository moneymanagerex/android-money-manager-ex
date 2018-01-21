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
package com.money.manager.ex.domainmodel;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.Parcel;

import java.util.Date;

/**
 * A stock history record.
 */
public class StockHistory
    extends EntityBase {

    public static final String HISTID = "HISTID";
    public static final String SYMBOL = "SYMBOL";
    public static final String DATE = "DATE";
    public static final String VALUE = "VALUE";
    public static final String UPDTYPE = "UPDTYPE";

    public StockHistory() {
        super();
    }

    @Override
    public void loadFromCursor(Cursor c) {
        super.loadFromCursor(c);

        // Reload all money values.
        DatabaseUtils.cursorDoubleToCursorValues(c, VALUE, this.contentValues);
    }

    protected StockHistory(Parcel in) {
        contentValues = in.readParcelable(ContentValues.class.getClassLoader());
    }

    public int getHistId() {
        return getInt(HISTID);
    }

    public Date getDate() {
        return getDate(DATE);
    }
}
