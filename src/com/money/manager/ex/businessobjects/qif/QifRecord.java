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
package com.money.manager.ex.businessobjects.qif;

import android.database.Cursor;

import com.money.manager.ex.Constants;
import com.money.manager.ex.adapter.AllDataAdapter;
import com.money.manager.ex.database.QueryAllData;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * A .qif file record. Represents a single transaction or account header record.
 */
public class QifRecord {
    public QifRecord(Cursor cursor) {
        mCursor = cursor;
    }

    public String Date;
    public String Amount;
    public String Payee;
    public String Remarks;

    private Cursor mCursor;
//    private String Logcat;

    /**
     * Returns a string representing one QIF record.
     * @return
     */
    public String getString() throws ParseException {
        // todo: implement
        StringBuilder builder = new StringBuilder();

        // Date

        this.Date = parseDate(mCursor);
        builder.append("D");
        builder.append(this.Date);
        builder.append(System.lineSeparator());

        // Amount

        this.Amount = parseAmount(mCursor);
        builder.append("T");
        builder.append(this.Amount);
        builder.append(System.lineSeparator());

        // Payee

        this.Payee = mCursor.getString(mCursor.getColumnIndex(QueryAllData.Payee));
        builder.append("P");
        builder.append(this.Payee);
        builder.append(System.lineSeparator());

        return builder.toString();
    }

    private String parseDate(Cursor cursor) throws ParseException {
        String dateValue = cursor.getString(cursor.getColumnIndex(QueryAllData.Date));
        SimpleDateFormat savedFormat = new SimpleDateFormat(Constants.PATTERN_DB_DATE, Locale.US);
        java.util.Date date = savedFormat.parse(dateValue);
        // todo: get Quicken date format from settings.
        // ref: http://www.unicode.org/reports/tr35/tr35-dates.html#Date_Format_Patterns
        SimpleDateFormat qifFormat = new SimpleDateFormat("MM''dd/yy", Locale.US);
        return qifFormat.format(date);
    }

    private String parseAmount(Cursor cursor) {
        String amount = Double.toString(cursor.getDouble(cursor.getColumnIndex(QueryAllData.TOTRANSAMOUNT)));

        // append sign
        String type = cursor.getString(cursor.getColumnIndex(QueryAllData.TransactionType));
        switch (type) {
            case Constants.TRANSACTION_TYPE_WITHDRAWAL:
                amount = "-" + amount;
                break;
            case Constants.TRANSACTION_TYPE_DEPOSIT:
                break;
            case Constants.TRANSACTION_TYPE_TRANSFER:
                break;
        }
        return amount;
    }
}
