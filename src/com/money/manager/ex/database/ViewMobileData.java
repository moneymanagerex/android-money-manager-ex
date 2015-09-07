/*
 * Copyright (C) 2012-2015 The Android Money Manager Ex Project Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
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
package com.money.manager.ex.database;

import android.content.Context;

import com.money.manager.ex.R;
import com.money.manager.ex.utils.RawFileUtils;

import org.apache.commons.lang3.StringUtils;

/**
 * This has been migrated to QueryAllData.
 */
public class ViewMobileData
        extends Dataset {

	// FIELDS
	public static final String ID = "ID";
	public static final String TransactionType = "TransactionType";
	public static final String Date = "Date";
	public static final String UserDate = "UserDate";
	public static final String Year = "Year";
	public static final String Month = "Month";
	public static final String Day = "Day";
	public static final String Category = "Category";
	public static final String Subcategory = "Subcategory";
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
	public static final String SubcategID = "SubcategID";
	public static final String PAYEE = "PAYEE";
	public static final String PAYEEID = "PAYEEID";
	public static final String TransactionNumber = "TransactionNumber";
	public static final String Status = "Status";
	public static final String Notes = "Notes";
	public static final String currency = "currency";
	public static final String finyear = "finyear";
	public static final String AmountBaseConvRate = "AmountBaseConvRate";
	
	public ViewMobileData(Context context) {
		super("", DatasetType.VIEW, "mobiledata");

        this.mContext = context.getApplicationContext();

        initialize(mContext, null);
	}

    private Context mContext;

	@Override
	public String[] getAllColumns() {
		return new String[] {"ID AS _id", ID, TransactionType, Date, UserDate, Year, Month, Day,
				Category, Subcategory, Amount, BaseConvRate, CURRENCYID, AccountName, ACCOUNTID,
                ToAccountName, ToAccountID, TOTRANSAMOUNT, ToCurrencyID, Splitted , CATEGID,
                SubcategID, PAYEE, PAYEEID, TransactionNumber, Status, Notes, currency, finyear,
                AmountBaseConvRate};
	}

    public void setWhere(String where) {
        initialize(mContext, where);
    }

    private void initialize(Context context, String where) {
        String source = RawFileUtils.getRawAsString(context, R.raw.query_mobiledata);

        // insert WHERE statement, filter.
        if(!StringUtils.isEmpty(where)) {
            source += " WHERE ";
            source += where;
        }

        source = "(" + source + ") mobiledata";

        setSource(source);
    }
}
