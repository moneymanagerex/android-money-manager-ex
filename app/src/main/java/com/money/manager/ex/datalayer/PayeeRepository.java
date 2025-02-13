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
package com.money.manager.ex.datalayer;

import android.content.Context;

import com.money.manager.ex.Constants;
import com.money.manager.ex.database.DatasetType;
import com.money.manager.ex.domainmodel.Payee;
import com.money.manager.ex.utils.MmxDatabaseUtils;

/**
 * Payee repository
 */
public class PayeeRepository
    extends RepositoryBase<Payee>{

    private static final String TABLE_NAME = "payee_v1";
    private static final String ID_COLUMN = Payee.PAYEEID;

    public PayeeRepository(Context context) {
        super(context, TABLE_NAME, DatasetType.TABLE, "payee", ID_COLUMN);
    }

    @Override
    protected Payee createEntity() {
        return new Payee();
    }

    @Override
    public String[] getAllColumns() {
        return new String[] { "PAYEEID AS _id",
                Payee.PAYEEID,
                Payee.PAYEENAME,
                Payee.CATEGID,
                Payee.NUMBER,
                Payee.ACTIVE
        };
    }
}
