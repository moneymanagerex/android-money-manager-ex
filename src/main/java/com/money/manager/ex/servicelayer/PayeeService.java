/*
 * Copyright (C) 2012-2015 The Android Money Manager Ex Project Team
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
import com.money.manager.ex.database.TablePayee;
import com.money.manager.ex.datalayer.PayeeRepository;
import com.money.manager.ex.domainmodel.Payee;

/**
 */
public class PayeeService {

    public PayeeService(Context context) {
        mContext = context;
        mPayee = new TablePayee();
        this.payeeRepository = new PayeeRepository(context);
    }

    private Context mContext;
    private TablePayee mPayee;
    private PayeeRepository payeeRepository;

    public TablePayee loadByName(String name) {
        String selection = Payee.PAYEENAME + "='" + name + "'";

        Cursor cursor = mContext.getContentResolver().query(
                mPayee.getUri(),
                mPayee.getAllColumns(),
                selection,
                null,
                null);

        if(cursor.moveToFirst()) {
            mPayee.setValueFromCursor(cursor);
        }

        cursor.close();

        return mPayee;
    }

    public int loadIdByName(String name) {
        int result = -1;

        if(TextUtils.isEmpty(name)) return result;

        String selection = Payee.PAYEENAME + "=?";

        Cursor cursor = mContext.getContentResolver().query(
                mPayee.getUri(),
                new String[]{ Payee.PAYEEID },
                selection,
                new String[] { name },
                null);
        if (cursor == null) return Constants.NOT_SET;

        if(cursor.moveToFirst()) {
            result = cursor.getInt(cursor.getColumnIndex(Payee.PAYEEID));
        }

        cursor.close();

        return result;
    }

    public int createNew(String name) {
        if (TextUtils.isEmpty(name)) return Constants.NOT_SET;

        name = name.trim();

        Payee payee = new Payee();
        payee.setName(name);

        return this.payeeRepository.add(payee);
    }

    public boolean exists(String name) {
        name = name.trim();

        TablePayee payee = loadByName(name);
        return (payee != null);
    }

    public int update(int id, String name) {
        if(TextUtils.isEmpty(name)) return Constants.NOT_SET;

        name = name.trim();

        ContentValues values = new ContentValues();
        values.put(Payee.PAYEENAME, name);

        int result = mContext.getContentResolver().update(mPayee.getUri(),
                values,
                Payee.PAYEEID + "=" + id,
                null);

        return result;
    }
}
