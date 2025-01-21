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
import android.text.TextUtils;

import com.money.manager.ex.R;
import com.money.manager.ex.utils.MmxFileUtils;

/**
 * This has been migrated to QueryAllData.
 */
public class QueryMobileData
	extends Dataset {

	// FIELDS
	// TODO rename all field in a standard form, ALL CAPS or LetterCaps, not a mixed
	public static final String ID = "ID";
	public static final String TransactionType = "TransactionType";
	public static final String Date = "Date";
	public static final String Year = "Year";
	public static final String Month = "Month";
	public static final String Day = "Day";
	public static final String Category = "Category";
	public static final String Amount = "Amount";
	public static final String BaseConvRate = "BaseConvRate";
	public static final String CURRENCYID = "CurrencyID";
	public static final String AccountName = "AccountName";
	public static final String ACCOUNTID = "AccountID";
	public static final String ToAccountName = "ToAccountName";
	public static final String ToAccountID = "ToAccountID";
	public static final String TOTRANSAMOUNT = "ToAmount";
	public static final String ToCurrencyID = "ToCurrencyID";
	public static final String Splitted  = "SPLITTED";
	public static final String CATEGID = "CATEGID";
	public static final String PAYEENAME = "PayeeName";
	public static final String PAYEEID = "PayeeID";
	public static final String TransactionNumber = "TransactionNumber";
	public static final String Status = "Status";
	public static final String Notes = "Notes";
	public static final String currency = "currency";
	public static final String AmountBaseConvRate = "AmountBaseConvRate";
	public static final String ATTACHMENTCOUNT = "ATTACHMENTCOUNT";
	public static final String TAGS = "TAGS";

	public QueryMobileData(Context context) {
		super("", DatasetType.VIEW, "mobiledata");

        this.mContext = context.getApplicationContext();

        initialize(mContext, null);
	}

    private final Context mContext;

	@Override
	public String[] getAllColumns() {
		return new String[] {"ID AS _id", ID, TransactionType, Date, Year, Month, Day, ATTACHMENTCOUNT,
				Category, Amount, BaseConvRate, CURRENCYID, AccountName, ACCOUNTID,
                ToAccountName, ToAccountID, TOTRANSAMOUNT, ToCurrencyID, Splitted , CATEGID,
                PAYEENAME, PAYEEID, TransactionNumber, Status, Notes, currency,
                AmountBaseConvRate, TAGS };
	}

    public void setWhere(String where) {
        initialize(mContext, where);
    }

    private void initialize(Context context, String where) {
        String source = MmxFileUtils.getRawAsString(context, R.raw.query_mobiledata);

        // insert WHERE statement, filter.
        if(!TextUtils.isEmpty(where)) {
            source += " WHERE ";
            source += where;
        }

        source = "(" + source + ") mobiledata";

        setSource(source);
    }
}
