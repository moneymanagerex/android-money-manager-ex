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
package com.money.manager.ex.businessobjects;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDiskIOException;

import com.money.manager.ex.core.ExceptionHandler;
import com.money.manager.ex.database.TableAccountList;

/**
 * Various business logic pieces related to Account(s).
 */
public class AccountService {

    public AccountService(Context context) {
        mContext = context;
    }

//    public static final int NO_ACCOUNT = -1;

    private Context mContext;

    /**
     * @param id account id to be search
     * @return TableAccountList, return null if account id not find
     */
    public TableAccountList getTableAccountList(int id) {
        TableAccountList account = null;
        try {
            account = loadAccount(id);
        } catch (SQLiteDiskIOException | IllegalStateException ex) {
            ExceptionHandler handler = new ExceptionHandler(mContext, this);
            handler.handle(ex, "loading account: " + Integer.toString(id));
        }
        return account;
    }

    private TableAccountList loadAccount(int id) {
        TableAccountList account = new TableAccountList();
        String selection = TableAccountList.ACCOUNTID + "=?";

        Cursor cursor = mContext.getContentResolver().query(account.getUri(),
                null,
                selection,
                new String[]{Integer.toString(id)},
                null);
        if (cursor == null) return null;

        // check if cursor is valid
        if (cursor.moveToFirst()) {
            account = new TableAccountList();
            account.setValueFromCursor(cursor);

            cursor.close();
        }

        return account;
    }
}
