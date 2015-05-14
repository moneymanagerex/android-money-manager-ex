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
import android.support.v4.content.CursorLoader;

import com.money.manager.ex.database.TableBudgetSplitTransactions;
import com.money.manager.ex.database.TablePayee;

/**
 *
 */
public class Payee {
    public Payee(Context context) {
        mContext = context;
        mPayee = new TablePayee();
    }

    private Context mContext;
    private TablePayee mPayee;

    public TablePayee loadByName(String name) {
//        String whereClause = TablePayee.PAYEENAME + " LIKE ?";// + mCurFilter + "%'";
//        String [] selectionArgs = new String[]{ "%" };
//        CursorLoader loader = new CursorLoader(mContext, mPayee.getUri(), mPayee.getAllColumns(),
//                whereClause, selectionArgs, mSort == 1
//                ? SORT_BY_USAGE : SORT_BY_NAME);

        String selection = TablePayee.PAYEENAME + "='" + name + "'";

        Cursor cursor = mContext.getContentResolver().query(
                mPayee.getUri(),
                mPayee.getAllColumns(),
                selection,
                null,
                null);

        if(!cursor.moveToFirst()) return null;

        //TablePayee.PAYEEID + "=" + Integer.toString(this.RecurringTransactionId),
        mPayee.setValueFromCursor(cursor);

        cursor.close();

        return mPayee;
    }

    public int getId() {
        return mPayee.getPayeeId();
    }

    public int loadIdByName(String name) {
        String selection = TablePayee.PAYEENAME + "=?";

        Cursor cursor = mContext.getContentResolver().query(
                mPayee.getUri(),
                new String[]{TablePayee.PAYEEID},
                selection,
                new String[] { name },
                null);

        if(!cursor.moveToFirst()) return -1;

        int id = cursor.getInt(cursor.getColumnIndex(TablePayee.PAYEEID));

        cursor.close();

        return id;
    }
}
