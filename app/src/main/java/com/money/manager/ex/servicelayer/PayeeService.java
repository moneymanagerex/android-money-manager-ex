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
package com.money.manager.ex.servicelayer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;

import com.money.manager.ex.Constants;
import com.money.manager.ex.database.ITransactionEntity;
import com.money.manager.ex.datalayer.AccountTransactionRepository;
import com.money.manager.ex.datalayer.PayeeRepository;
import com.money.manager.ex.domainmodel.Payee;

/**
 *
 */
public class PayeeService
        extends ServiceBase {

    private final PayeeRepository payeeRepository;

    public PayeeService(final Context context) {
        super(context);

        payeeRepository = new PayeeRepository(context);
    }

    public Payee loadByName(final String name) {
        Payee payee = null;
        final String selection = Payee.PAYEENAME + "='" + name + "'";

        final Cursor cursor = getContext().getContentResolver().query(
                payeeRepository.getUri(),
                payeeRepository.getAllColumns(),
                selection,
                null,
                null);
        if (null == cursor) return null;

        if (cursor.moveToFirst()) {
            payee = new Payee();
            payee.loadFromCursor(cursor);
        }

        cursor.close();

        return payee;
    }

    public int loadIdByName(final String name) {
        int result = Constants.NOT_SET;

        if (TextUtils.isEmpty(name)) return result;

        final String selection = Payee.PAYEENAME + "=?";

        final Cursor cursor = getContext().getContentResolver().query(
                payeeRepository.getUri(),
                new String[]{Payee.PAYEEID},
                selection,
                new String[]{name},
                null);
        if (null == cursor) return Constants.NOT_SET;

        if (cursor.moveToFirst()) {
            result = cursor.getInt(cursor.getColumnIndex(Payee.PAYEEID));
        }

        cursor.close();

        return result;
    }

    public Payee createNew(String name) {
        if (TextUtils.isEmpty(name)) return null;

        name = name.trim();

        final Payee payee = new Payee();
        payee.setName(name);
        payee.setCategoryId(-1);

        final int id = payeeRepository.add(payee);

        payee.setId(id);

        return payee;
    }

    public boolean exists(String name) {
        name = name.trim();

        final Payee payee = loadByName(name);
        return (null != payee);
    }

    public boolean isPayeeUsed(final int payeeId) {
        final AccountTransactionRepository repo = new AccountTransactionRepository(getContext());
        final int links = repo.count(ITransactionEntity.PAYEEID + "=?", new String[]{Integer.toString(payeeId)});
        return 0 < links;
    }

    public int update(final int id, String name) {
        if (TextUtils.isEmpty(name)) return Constants.NOT_SET;

        name = name.trim();

        final ContentValues values = new ContentValues();
        values.put(Payee.PAYEENAME, name);

        return getContext().getContentResolver().update(payeeRepository.getUri(),
                values,
                Payee.PAYEEID + "=" + id,
                null);
    }
}
