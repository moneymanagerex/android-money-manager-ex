/*
 * Copyright (C) 2012-2016 The Android Money Manager Ex Project Team
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

package com.money.manager.ex.database;

import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteCursorDriver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQuery;
import android.util.Log;

/**
 * Closing the database connection after the operation is done.
 * Ref: http://stackoverflow.com/questions/4547461/closing-the-database-in-a-contentprovider
 */
public class LeaklessCursor extends SQLiteCursor {
    static final String TAG = "LeaklessCursor";

    public LeaklessCursor(SQLiteDatabase db, SQLiteCursorDriver driver,
                          String editTable, SQLiteQuery query) {
        super(db, driver, editTable, query);
    }

    @Override
    public void close() {
        final SQLiteDatabase db = getDatabase();
        super.close();
        if (db != null) {
            Log.d(TAG, "Closing LeaklessCursor: " + db.getPath());
            db.close();
        }
    }
}