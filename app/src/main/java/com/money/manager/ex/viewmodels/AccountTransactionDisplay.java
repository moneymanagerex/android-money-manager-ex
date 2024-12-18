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
package com.money.manager.ex.viewmodels;

import android.database.Cursor;
import android.database.DatabaseUtils;

import com.money.manager.ex.core.TransactionStatuses;
import com.money.manager.ex.core.TransactionTypes;
import com.money.manager.ex.database.QueryAllData;
import com.money.manager.ex.database.QueryMobileData;
import com.money.manager.ex.domainmodel.EntityBase;
import com.money.manager.ex.utils.MmxDate;

import java.util.Date;

import info.javaperformance.money.Money;

/**
 * Record from All Data query. Used for account transactions lists (search results, account
 * transactions).
 * Source is QueryAllData.
 * Note: This data is readonly! Records can not be created or updated.
 */
// TODO: Replace QueryAllData with QueryMobileData
public class AccountTransactionDisplay
    extends EntityBase {

    @Override
    public void loadFromCursor(Cursor c) {
        super.loadFromCursor(c);

        // Reload all money values.
        DatabaseUtils.cursorDoubleToCursorValues(c, QueryAllData.AMOUNT, this.contentValues);
        DatabaseUtils.cursorDoubleToCursorValues(c, QueryAllData.ToAmount, this.contentValues);
    }

    public Long getId() {
        return getLong(QueryAllData.ID);
    }

    public Long getAccountId() {
        return getLong(QueryAllData.ACCOUNTID);
    }

    public String getAccountName() {
        return getString(QueryAllData.AccountName);
    }

    public Money getAmount() {
        return getMoney(QueryAllData.AMOUNT);
    }

    public String getCategory() {
        return getString(QueryAllData.Category);
    }

    public String getDateString() {
        return getString(QueryAllData.Date);
    }

    public Date getDate() {
        String dateString = getDateString();

        Date dateTime = new MmxDate(dateString).toDate();

        return dateTime;
    }

    public String getNotes() {
        return getString(QueryAllData.Notes);
    }

    public boolean getIsSplit() {
        long split = getLong(QueryAllData.SPLITTED);
        return split > 0;
    }

    public String getPayee() {
        return getString(QueryAllData.PAYEENAME);
    }

    public String getStatusCode() {
        return getString(QueryAllData.STATUS);
    }

    public TransactionStatuses getStatus() {
        String code = getStatusCode();
        return TransactionStatuses.get(code);
    }

    public long getToAccountId() {
        return getLong(QueryAllData.TOACCOUNTID);
    }

    public Money getToAmount() {
        return getMoney(QueryAllData.ToAmount);
    }

    public String getTransactionTypeName() {
        return getString(QueryAllData.TransactionType);
    }

    public TransactionTypes getTransactionType() {
        String typeName = getTransactionTypeName();

        TransactionTypes transactionType = TransactionTypes.valueOf(typeName);
        return transactionType;
    }

    public String getTags() {
        return getString(QueryMobileData.TAGS);
    }
}
