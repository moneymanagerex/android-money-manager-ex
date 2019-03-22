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

package com.money.manager.ex.datalayer;

import android.content.Context;
import android.database.Cursor;

import com.money.manager.ex.R;
import com.money.manager.ex.database.DatasetType;
import com.money.manager.ex.database.QueryAllData;
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
            QueryAllData.Date, QueryAllData.UserDate, QueryAllData.Year, QueryAllData.Month,
            QueryAllData.Day, QueryAllData.Category, QueryAllData.Subcategory, QueryAllData.Amount,
            QueryAllData.BaseConvRate, QueryAllData.CURRENCYID, QueryAllData.AccountName,
            QueryAllData.ACCOUNTID,
//                FromAccountName, FromAccountId, FromAmount, FromCurrencyId,
            QueryAllData.SPLITTED, QueryAllData.CategID, QueryAllData.SubcategID,
            QueryAllData.Payee, QueryAllData.PayeeID, QueryAllData.TransactionNumber,
            QueryAllData.Status, QueryAllData.Notes, QueryAllData.ToAccountName,
            QueryAllData.TOACCOUNTID, QueryAllData.ToAmount, QueryAllData.ToCurrencyId,
            QueryAllData.currency, QueryAllData.finyear};
    }

    public int add(AccountTransactionDisplay entity) {
        return insert(entity.contentValues);
    }

    public Cursor query(String selection, String sort) {
        return openCursor(null, selection, null, sort);
    }

}
