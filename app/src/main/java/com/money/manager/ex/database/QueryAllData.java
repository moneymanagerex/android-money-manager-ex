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
package com.money.manager.ex.database;

import android.content.Context;

import com.money.manager.ex.R;
import com.money.manager.ex.utils.MmxFileUtils;

/**
 */
public class QueryAllData
    extends Dataset {

    // FIELDS
    public static final String ID = "ID";
    public static final String TransactionType = "TransactionType";
    public static final String Date = "Date";
    public static final String Year = "Year";
    public static final String Month = "Month";
    public static final String Day = "Day";
    public static final String Category = "Category";
//    public static final String Subcategory = "Subcategory";
    public static final String AMOUNT = "Amount";
    public static final String BaseConvRate = "BaseConvRate";
    public static final String CURRENCYID = "CurrencyID";
    public static final String AccountName = "AccountName";
    public static final String ACCOUNTID = "AccountID";
    public static final String SPLITTED = "SPLITTED";
    public static final String CATEGID = "CategID";
    public static final String PAYEENAME = "PayeeName";
    public static final String PAYEEID = "PayeeID";
    public static final String ToAccountName = "ToAccountName";
    public static final String TOACCOUNTID = "ToAccountID";
    public static final String ToAmount = "ToAmount";
    public static final String ToCurrencyId = "ToCurrencyID";
    public static final String TransactionNumber = "TransactionNumber";
    public static final String STATUS = "Status";
    public static final String Notes = "Notes";
    public static final String currency = "currency";
    public static final String ATTACHMENTCOUNT = "ATTACHMENTCOUNT";
    public static final String TAGS = "TAGS";

    public QueryAllData(Context context) {
        super(MmxFileUtils.getRawAsString(context, R.raw.query_alldata), DatasetType.QUERY, "queryalldata");
        this.mContext = context.getApplicationContext();
    }

    private final Context mContext;

    @Override
    public String[] getAllColumns() {
        return new String[]{"ID AS _id", ID, TransactionType, Date, Year, Month, Day, ATTACHMENTCOUNT,
                Category, AMOUNT, BaseConvRate, CURRENCYID, AccountName, ACCOUNTID,
                SPLITTED, CATEGID, PAYEENAME, PAYEEID, TransactionNumber, STATUS, Notes,
                ToAccountName, TOACCOUNTID, ToAmount, ToCurrencyId,
                currency, TAGS};
    }
}
