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
import android.util.Log;

import com.money.manager.ex.adapter.AllDataAdapter;
import com.money.manager.ex.database.QueryAllData;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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

        String dateValue = mCursor.getString(mCursor.getColumnIndex(QueryAllData.Date));
        SimpleDateFormat savedFormat = new SimpleDateFormat(AllDataAdapter.DATE_FORMAT, Locale.US);
        java.util.Date date = savedFormat.parse(dateValue);
        // todo: format date
        // todo: get Quicken date format from settings.
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM'dd/YY", Locale.US);
        this.Date = date.toString();
        builder.append(this.Date);

        // Amount

        this.Amount = Double.toString(mCursor.getDouble(mCursor.getColumnIndex(QueryAllData.TOTRANSAMOUNT)));

        // Payee

        this.Payee = mCursor.getString(mCursor.getColumnIndex(QueryAllData.Payee));

        return builder.toString();
    }
}
