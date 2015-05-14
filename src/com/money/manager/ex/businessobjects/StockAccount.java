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

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.money.manager.ex.database.MoneyManagerOpenHelper;
import com.money.manager.ex.database.TableAccountList;

/**
 * Stocks account
 */
public class StockAccount {
//    public void load(int accountId) {
//        String selection = TableAccountList.ACCOUNTID + "=?";
//        SQLiteDatabase database = MoneyManagerOpenHelper..getReadableDatabase();
//        if (database != null) {
//            Cursor cursor = database.query(new TableAccountList().getSource(), null, selection,
//                    new String[]{Integer.toString(id)}, null, null, null);
//            // check if cursor is valid
//            if (cursor != null && cursor.moveToFirst()) {
//                TableAccountList account = new TableAccountList();
//                account.setValueFromCursor(cursor);
//
//                cursor.close();
//                return account;
//            }
//            database.close();
//            //close();
//        }
//        // find is false then return null
//        return null;
//
//    }
}
