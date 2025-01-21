/*
 * Copyright (C) 2012-2024 The Android Money Manager Ex Project Team
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

package com.money.manager.ex.sqlite3mc;

import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory;

public class SupportFactory implements SupportSQLiteOpenHelper.Factory {
    private final byte[] passphrase;

    public SupportFactory(byte[] passphrase) {
        this.passphrase = passphrase;
    }

    @Override
    public SupportSQLiteOpenHelper create(SupportSQLiteOpenHelper.Configuration configuration) {
        // Use the default FrameworkSQLiteOpenHelperFactory
        SupportSQLiteOpenHelper.Factory delegate = new FrameworkSQLiteOpenHelperFactory();

        SupportSQLiteOpenHelper helper = delegate.create(configuration);

        // Configure SQLite3MC by passing the key as PRAGMA
        SupportSQLiteDatabase db = helper.getWritableDatabase();
        db.execSQL("PRAGMA cipher = 'sqlcipher';");
        db.execSQL("PRAGMA legacy = 4;");
        db.execSQL("PRAGMA key = '" + new String(passphrase) + "';");

        return helper;
    }
}