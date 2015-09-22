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
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.money.manager.ex.viewmodels;

import android.database.Cursor;
import android.database.DatabaseUtils;

import com.money.manager.ex.core.TransactionStatuses;
import com.money.manager.ex.database.QueryAllData;
import com.money.manager.ex.domainmodel.EntityBase;

import java.math.BigDecimal;

import info.javaperformance.money.Money;

/**
 * Record from All Data query. Used for account transactions lists.
 * Source is QueryAllData.
 */
public class AccountTransaction
    extends EntityBase {

    @Override
    public void loadFromCursor(Cursor c) {
        super.loadFromCursor(c);

        // Reload all money values.
        DatabaseUtils.cursorDoubleToCursorValues(c, QueryAllData.Amount, this.contentValues);
        DatabaseUtils.cursorDoubleToCursorValues(c, QueryAllData.ToAmount, this.contentValues);
    }

    public Integer getId() {
        return getInt(QueryAllData.ID);
    }

    public Integer getAccountId() {
        return getInt(QueryAllData.ACCOUNTID);
    }

    public Money getAmount() {
        return getMoney(QueryAllData.Amount);
    }

    public String getStatusCode() {
        return getString(QueryAllData.Status);
    }

    public TransactionStatuses getStatus() {
        String code = getStatusCode();
        return TransactionStatuses.get(code);
    }

    public Money getToAmount() {
        return getMoney(QueryAllData.ToAmount);
    }

    public String getTransactionType() {
        return getString(QueryAllData.TransactionType);
    }
}
