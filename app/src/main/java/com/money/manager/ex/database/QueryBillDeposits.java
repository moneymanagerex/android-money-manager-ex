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
 * A dataset for the query that fetches recurring transactions.
 */
public class QueryBillDeposits
        extends Dataset {

    // constructor
    public QueryBillDeposits(Context context) {
        super(MmxFileUtils.getRawAsString(context, R.raw.query_billdeposits), DatasetType.QUERY,
                QueryBillDeposits.class.getSimpleName());
    }

    // fields
    public static String BDID = "BDID";
    public static String PAYEEID = "PAYEEID";
    public static String PAYEENAME = "PAYEENAME";
    public static String TOACCOUNTID = "TOACCOUNTID";
    public static String TOACCOUNTNAME = "TOACCOUNTNAME";
    public static String ACCOUNTID = "ACCOUNTID";
    public static String ACCOUNTNAME = "ACCOUNTNAME";
    public static String CURRENCYID = "CURRENCYID";
    public static String CATEGSUBCATEGNAME = "CATEGSUBCATEGNAME";
    public static String CATEGNAME = "CATEGNAME";
    public static String SUBCATEGNAME = "SUBCATEGNAME";
    public static String TRANSCODE = "TRANSCODE";
    public static String TRANSAMOUNT = "TRANSAMOUNT";
    public static String NEXTOCCURRENCEDATE = "NEXTOCCURRENCEDATE";
    public static String REPEATS = "REPEATS";
    public static String DAYSLEFT = "DAYSLEFT";
    public static String NOTES = "NOTES";
    public static String STATUS = "STATUS";
    public static String NUMOCCURRENCES = "NUMOCCURRENCES";
    public static String TOTRANSAMOUNT = "TOTRANSAMOUNT";
    public static String TRANSACTIONNUMBER = "TRANSACTIONNUMBER";
    public static String TRANSDATE = "TRANSDATE";
    public static String AMOUNT = "AMOUNT";
    public static String USERNEXTOCCURRENCEDATE = "USERNEXTOCCURRENCEDATE";

    // get all columns
    @Override
    public String[] getAllColumns() {
        return new String[]{BDID + " AS _id", BDID, PAYEEID, PAYEENAME, TOACCOUNTID, TOACCOUNTNAME,
                ACCOUNTID, ACCOUNTNAME, CURRENCYID, CATEGSUBCATEGNAME,
                CATEGNAME, SUBCATEGNAME, TRANSCODE, TRANSAMOUNT, NEXTOCCURRENCEDATE, REPEATS,
                DAYSLEFT, NOTES, STATUS, NUMOCCURRENCES, TOTRANSAMOUNT,
                TRANSACTIONNUMBER, TRANSDATE, AMOUNT, USERNEXTOCCURRENCEDATE};
    }
}
