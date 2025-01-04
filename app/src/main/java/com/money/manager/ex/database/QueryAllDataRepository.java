/*
 * Copyright (C) 2012-2024 The Android Money Manager Ex Project Team
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

package com.money.manager.ex.database;

import android.content.Context;
import android.database.Cursor;

import com.money.manager.ex.R;
import com.money.manager.ex.datalayer.RepositoryBase;
import com.money.manager.ex.utils.MmxFileUtils;
import com.money.manager.ex.viewmodels.AccountTransactionDisplay;

/**
 * Account Transaction repository.
 */
public class QueryAllDataRepository
    extends RepositoryBase {

    public QueryAllDataRepository(Context context) {
        super(context, MmxFileUtils.getRawAsString(context, R.raw.query_alldata), DatasetType.QUERY,
            "queryalldata");
    }

    @Override
    public String[] getAllColumns() {
        return new String[]{"ID AS _id", QueryAllData.ID, QueryAllData.TransactionType,
                QueryAllData.Date, QueryAllData.Year, QueryAllData.Month, QueryAllData.Day, QueryAllData.ATTACHMENTCOUNT,
                QueryAllData.Category, QueryAllData.Subcategory, QueryAllData.AMOUNT,
            QueryAllData.BaseConvRate, QueryAllData.CURRENCYID, QueryAllData.AccountName,
            QueryAllData.ACCOUNTID,
            QueryAllData.SPLITTED,
            QueryAllData.PAYEENAME, QueryAllData.PAYEEID, QueryAllData.TransactionNumber,
            QueryAllData.STATUS, QueryAllData.Notes, QueryAllData.ToAccountName,
            QueryAllData.TOACCOUNTID, QueryAllData.ToAmount, QueryAllData.ToCurrencyId,
            QueryAllData.currency,
            QueryAllData.TAGS};
    }

    public long add(AccountTransactionDisplay entity) {
        return insert(entity.contentValues);
    }

    public Cursor query(String selection, String sort) {
        return openCursor(null, selection, null, sort);
    }
}
