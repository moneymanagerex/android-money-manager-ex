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

package com.money.manager.ex.utils;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.widget.DatePicker;

import com.money.manager.ex.Constants;
import com.money.manager.ex.R;
import com.money.manager.ex.database.MoneyManagerOpenHelper;
import com.money.manager.ex.database.TableInfoTable;

import org.apache.commons.lang.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Date utilities
 */
public class DateUtils {
    private static final String LOGCAT = DateUtils.class.getSimpleName();

    /**
     * Convert string date into date object using pattern define to user
     *
     * @param ctx  context
     * @param date string to convert
     * @return date converted
     */
    public static Date getDateFromString(Context ctx, String date) {
        return getDateFromString(ctx, date, getUserDatePattern(ctx));
    }

    /**
     * Convert string date into date object using pattern params
     *
     * @param date    string to convert
     * @param pattern to use for convert
     * @return date object converted
     */
    public static Date getDateFromString(Context context, String date, String pattern) {
        try {
            Locale locale = context.getResources().getConfiguration().locale;
            return new SimpleDateFormat(pattern, locale).parse(date);
        } catch (ParseException e) {
            Log.e(LOGCAT, e.getMessage());
        }
        return null;
    }

    /**
     * Convert date object to string from user pattern
     *
     * @param date date value
     * @return string date representation
     */
    public static String getStringFromDate(Context ctx, Date date) {
        return getStringFromDate(ctx, date, getUserDatePattern(ctx));
    }

    /**
     * @param date    object to convert in string
     * @param pattern pattern to use to convert
     * @return string representation of the date
     */
    public static String getStringFromDate(Context context, Date date, String pattern) {
        Locale locale = context.getResources().getConfiguration().locale;
        return new SimpleDateFormat(pattern, locale).format(date);
    }

    /**
     * Convert date object in string SQLite date format
     *
     * @param date to convert
     * @return string formatted date SQLite
     */
    public static String getSQLiteStringDate(Context context, Date date) {

        return getStringFromDate(context, date, Constants.PATTERN_DB_DATE);
    }

    /**
     * Get pattern define from user
     *
     * @return pattern user define
     */
    public static String getUserDatePattern(Context ctx) {
        TableInfoTable infoTable = new TableInfoTable();
        MoneyManagerOpenHelper helper = MoneyManagerOpenHelper.getInstance(ctx);
        Cursor cursor = helper.getReadableDatabase().query(infoTable.getSource(), null,
                TableInfoTable.INFONAME + "=?", new String[]{"DATEFORMAT"}, null, null, null);
        String pattern = null;
        if (cursor != null && cursor.moveToFirst()) {
            pattern = cursor.getString(cursor.getColumnIndex(TableInfoTable.INFOVALUE));
            //replace part of pattern
            pattern = pattern.replace("%d", "dd").replace("%m", "MM").replace("%y", "yy").replace("%Y", "yyyy").replace("'", "''");

            cursor.close();
        }

        if (StringUtils.isEmpty(pattern)
                && ctx.getResources().getStringArray(R.array.date_format_mask) != null
                && ctx.getResources().getStringArray(R.array.date_format_mask).length > 0){
            pattern= ctx.getResources().getStringArray(R.array.date_format_mask)[0];
            pattern = pattern.replace("%d", "dd").replace("%m", "MM").replace("%y", "yy").replace("%Y", "yyyy").replace("'", "''");
        }

        return pattern;
    }

    /**
     * @param date    to start calculate
     * @param repeats type of repeating transactions
     * @return next Date
     */
    public static Date getDateNextOccurrence(Date date, int repeats) {
        if (repeats >= 200) {
            repeats = repeats - 200;
        } // set auto execute without user acknowledgement
        if (repeats >= 100) {
            repeats = repeats - 100;
        } // set auto execute on the next occurrence
        // create object calendar
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        switch (repeats) {
            case 0: //none
                break;
            case 1: //weekly
                calendar.add(Calendar.DATE, 7);
                break;
            case 2: //bi_weekly
                calendar.add(Calendar.DATE, 14);
                break;
            case 3: //monthly
                calendar.add(Calendar.MONTH, 1);
                break;
            case 4: //bi_monthly
                calendar.add(Calendar.MONTH, 2);
                break;
            case 5: //quarterly
                calendar.add(Calendar.MONTH, 3);
                break;
            case 6: //half_year
                calendar.add(Calendar.MONTH, 6);
                break;
            case 7: //yearly
                calendar.add(Calendar.YEAR, 1);
                break;
            case 8: //four_months
                calendar.add(Calendar.MONTH, 4);
                break;
            case 9: //four_weeks
                calendar.add(Calendar.DATE, 28);
                break;
            case 10: //daily
                calendar.add(Calendar.DATE, 1);
                break;
            case 11: //in_x_days
            case 12: //in_x_months
            case 13: //every_x_days
            case 14: //every_x_months
                break;
            case 15: //month (last day)
                calendar.add(Calendar.MONTH, 1);
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                calendar.add(Calendar.DATE, -1);
                break;
            case 16: //month (last business day)
                break;
        }
        return calendar.getTime();
    }

    /**
     * This function from the date picker returns a date in java
     *
     * @param datePicker date picker control
     * @return java date
     */
    public static java.util.Date getDateFromDatePicker(DatePicker datePicker) {
        int day = datePicker.getDayOfMonth();
        int month = datePicker.getMonth();
        int year = datePicker.getYear();

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);

        return calendar.getTime();
    }

    public static void setDateToDatePicker(Date date, DatePicker datePicker) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        datePicker.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
    }
}
