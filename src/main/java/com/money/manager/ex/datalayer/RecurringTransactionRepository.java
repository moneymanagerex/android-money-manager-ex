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

package com.money.manager.ex.datalayer;

import android.content.Context;
import android.database.Cursor;

import com.money.manager.ex.database.TableBillsDeposits;

/**
 * Recurring transaction repository.
 * Created by Alen Siljak on 13/08/2015.
 */
public class RecurringTransactionRepository {

    public RecurringTransactionRepository(Context context) {
        this.context = context;
    }

    public Context context;

    public TableBillsDeposits load(int id) {
        TableBillsDeposits tx = new TableBillsDeposits();

        Cursor cursor = this.context.getContentResolver().query(
                tx.getUri(),
                tx.getAllColumns(),
                TableBillsDeposits.BDID + "=?",
                new String[] { Integer.toString(id) },
                null);
        if (cursor == null) return null;
        if (!cursor.moveToFirst()) return null;

        tx.setValueFromCursor(cursor);

        cursor.close();

        return tx;
    }
}
