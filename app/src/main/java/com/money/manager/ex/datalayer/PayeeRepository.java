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
import android.database.Cursor;
import android.text.TextUtils;

import androidx.loader.content.Loader;

import com.money.manager.ex.common.MmxCursorLoader;
import com.money.manager.ex.database.DatasetType;
import com.money.manager.ex.domainmodel.Payee;

/**
 * Payee repository
 */
public class PayeeRepository
    extends RepositoryBase<Payee>{

    private static final String TABLE_NAME = "payee_v1";
    private static final String ID_COLUMN = Payee.PAYEEID;
    private static final String NAME_COLUMN = Payee.PAYEENAME;

    // private static final int ORDER_BY_NAME = 0;
    private static final int ORDER_BY_USAGE = 1;
    private static final int ORDER_BY_RECENT = 2;

    private static final String SORT_BY_NAME = "UPPER(" + Payee.PAYEENAME + ")";
    private static final String SORT_BY_USAGE = "(SELECT COUNT(*) FROM CHECKINGACCOUNT_V1 WHERE T.PAYEEID = CHECKINGACCOUNT_V1.PAYEEID AND (CHECKINGACCOUNT_V1.DELETEDTIME IS NULL OR CHECKINGACCOUNT_V1.DELETEDTIME = '') ) DESC";
    private static final String SORT_BY_RECENT =
            "(SELECT max( TRANSDATE ) \n" +
                    " FROM CHECKINGACCOUNT_V1 \n" +
                    " WHERE T.PAYEEID = CHECKINGACCOUNT_V1.PAYEEID \n" +
                    "   AND (CHECKINGACCOUNT_V1.DELETEDTIME IS NULL OR CHECKINGACCOUNT_V1.DELETEDTIME = '') ) DESC";

    public PayeeRepository(Context context) {
        super(context, TABLE_NAME, DatasetType.TABLE, "payee", ID_COLUMN, NAME_COLUMN);
    }

    @Override
    protected Payee createEntity() {
        return new Payee();
    }

    @Override
    public String[] getAllColumns() {
        return new String[] { ID_COLUMN + " AS _id",
                Payee.PAYEEID,
                Payee.PAYEENAME,
                Payee.CATEGID,
                Payee.NUMBER,
                Payee.ACTIVE
        };
    }

    // custom func

    // New method to handle query logic
    public Loader<Cursor> getPayees(String filter, boolean showInactive, int sortOrder) {
        String whereClause = "";
        if (!showInactive) {
            whereClause = "ACTIVE <> 0";
        }

        String[] selectionArgs = null;
        if (!TextUtils.isEmpty(filter)) {
            if (!whereClause.isEmpty()) whereClause += " AND ";
            whereClause += Payee.PAYEENAME + " LIKE ?";
            selectionArgs = new String[]{filter + '%'};
        }

        String orderBy;
        switch (sortOrder) {
            case ORDER_BY_USAGE:
                orderBy = SORT_BY_USAGE;
                break;
            case ORDER_BY_RECENT:
                orderBy = SORT_BY_RECENT;
                break;
            default:
                orderBy = SORT_BY_NAME;
                break;
        }

        Select query = new Select(getAllColumns())
                .where(whereClause, selectionArgs)
                .orderBy(orderBy);

        return new MmxCursorLoader(getContext(), getUri(), query);
    }
}
