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
import android.text.TextUtils;

import com.money.manager.ex.Constants;
import com.money.manager.ex.database.QueryAllData;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * A .qif file record. Represents a single transaction or account header record.
 * References:
 * http://en.wikipedia.org/wiki/Quicken_Interchange_Format
 */
public class QifRecord {
    public QifRecord() {
    }

//    public String Date;
//    public String Amount;
//    public String Payee;
//    public String Category;
//    public String Memo;

//    private Cursor mCursor;
//    private String Logcat;

    /**
     * Returns a string representing one QIF record.
     * @return
     */
    public String parse(Cursor cursor) throws ParseException {
        StringBuilder builder = new StringBuilder();

        // Date
        String date = parseDate(cursor);
        builder.append("D");
        builder.append(date);
        builder.append(System.lineSeparator());

        // Amount
        String amount = parseAmount(cursor);
//        builder.append("U");
//        builder.append(this.Amount);
//        builder.append(System.lineSeparator());
        builder.append("T");
        builder.append(amount);
        builder.append(System.lineSeparator());

        // Cleared status
        String cleared = parseCleared(cursor);
        if (!TextUtils.isEmpty(cleared)) {
            builder.append("C");
            builder.append(cleared);
        }

        // Payee
        String payee = cursor.getString(cursor.getColumnIndex(QueryAllData.Payee));
        builder.append("P");
        builder.append(payee);
        builder.append(System.lineSeparator());

        // Category
        String category = parseCategory(cursor);
        builder.append("L");
        builder.append(category);
        builder.append(System.lineSeparator());

        // todo: handle splits
        // todo: handle transfers

        // Notes
        String memo = parseMemo(cursor);
        if (!TextUtils.isEmpty(memo)) {
            builder.append("M");
            builder.append(memo);
            builder.append(System.lineSeparator());
        }

        builder.append("^");
        builder.append(System.lineSeparator());

        return builder.toString();
    }

    public int getAccountId(Cursor cursor) {
        int accountId = cursor.getInt(cursor.getColumnIndex(QueryAllData.ACCOUNTID));
        return accountId;
    }

    private String parseCleared(Cursor cursor) {
        // todo: handle other statuses?
        // Cleared: * or c
        // Reconciled: X or R
        return cursor.getString(cursor.getColumnIndex(QueryAllData.Status));
    }

    private String parseDate(Cursor cursor) throws ParseException {
        String dateValue = cursor.getString(cursor.getColumnIndex(QueryAllData.Date));
        SimpleDateFormat savedFormat = new SimpleDateFormat(Constants.PATTERN_DB_DATE, Locale.US);
        java.util.Date date = savedFormat.parse(dateValue);
        // todo: get Quicken date format from settings.
        // ref: http://www.unicode.org/reports/tr35/tr35-dates.html#Date_Format_Patterns
        SimpleDateFormat qifFormat = new SimpleDateFormat("MM/dd''yy", Locale.US);
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

    private String parseCategory(Cursor cursor) {
        String category = cursor.getString(cursor.getColumnIndex(QueryAllData.Category));
        String subCategory = cursor.getString(cursor.getColumnIndex(QueryAllData.Subcategory));

        if (!TextUtils.isEmpty(subCategory)) {
            return subCategory;
        } else {
            return category;
        }
    }

    private String parseMemo(Cursor cursor) {
        return cursor.getString(cursor.getColumnIndex(QueryAllData.Notes));
    }
}
