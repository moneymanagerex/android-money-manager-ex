/*
 * Copyright (C) 2012-2019 The Android Money Manager Ex Project Team
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
 */
public class PayeeService
    extends ServiceBase {

    public PayeeService(Context context) {
        super(context);

        this.payeeRepository = new PayeeRepository(context);
    }

    private PayeeRepository payeeRepository;

    public Payee loadByName(String name) {
        Payee payee = null;
        String selection = Payee.PAYEENAME + "='" + name + "'";

        Cursor cursor = getContext().getContentResolver().query(
                this.payeeRepository.getUri(),
                this.payeeRepository.getAllColumns(),
                selection,
                null,
                null);
        if (cursor == null) return null;

        if(cursor.moveToFirst()) {
            payee = new Payee();
            payee.loadFromCursor(cursor);
        }

        cursor.close();

        return payee;
    }

    public int loadIdByName(String name) {
        int result = Constants.NOT_SET;

        if(TextUtils.isEmpty(name)) return result;

        String selection = Payee.PAYEENAME + "=?";

        Cursor cursor = getContext().getContentResolver().query(
                payeeRepository.getUri(),
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

    public Payee createNew(String name) {
        if (TextUtils.isEmpty(name)) return null;

        name = name.trim();

        Payee payee = new Payee();
        payee.setName(name);
        payee.setCategoryId(-1);
        payee.setSubcategoryId(-1);

        int id = this.payeeRepository.add(payee);

        payee.setId(id);

        return payee;
    }

    public boolean exists(String name) {
        name = name.trim();

        Payee payee = loadByName(name);
        return (payee != null);
    }

    public boolean isPayeeUsed(int payeeId) {
        AccountTransactionRepository repo = new AccountTransactionRepository(getContext());
        int links = repo.count(ITransactionEntity.PAYEEID + "=?", new String[]{Integer.toString(payeeId)});
        return links > 0;
    }

    public int update(int id, String name) {
        if(TextUtils.isEmpty(name)) return Constants.NOT_SET;

        name = name.trim();

        ContentValues values = new ContentValues();
        values.put(Payee.PAYEENAME, name);

        return getContext().getContentResolver().update(payeeRepository.getUri(),
                values,
                Payee.PAYEEID + "=" + id,
                null);
    }
}
