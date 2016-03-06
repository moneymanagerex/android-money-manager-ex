/*
 * Copyright (C) 2012-2016 The Android Money Manager Ex Project Team
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
package com.money.manager.ex.utils;

import android.content.Context;
import android.widget.DatePicker;

import com.money.manager.ex.Constants;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Utilities for DateTime (date4j). Deprecated. Use Joda Time.
 */
public class MyDateTimeUtils {

    public static DateTime today() {
        //return DateTime.today(TimeZone.getDefault());
        return DateTime.now();
    }

    public static DateTime from(String isoString) {
        DateTimeFormatter format = DateTimeFormat.forPattern(Constants.ISO_DATE_FORMAT);
        DateTime dateTime = format.parseDateTime(isoString);
        return dateTime;
    }

    public static DateTime from(String dateString, String pattern) {
        DateTimeFormatter format = DateTimeFormat.forPattern(pattern);
        DateTime dateTime = format.parseDateTime(dateString);
        return dateTime;
    }

    /**
     * Conversion factory.
     * @param calendar The date to use as the base.
     * @return The DateTime instance of the date.
     */
    public static DateTime from(Calendar calendar) {
        if (calendar == null) return null;

//        SimpleDateFormat sdf = new SimpleDateFormat(Constants.ISO_DATE_FORMAT);
//        String date = sdf.format(calendar.getTime());
//        return new DateTime(date);

        DateTime result = new DateTime(calendar.getTime());
//        DateTimeFormatter format = DateTimeFormat.forPattern(Constants.ISO_DATE_FORMAT);
        return result;
    }

    public static DateTime from(int year, int monthOfYear, int dayOfMonth) {
        return new DateTime(year, monthOfYear, dayOfMonth, 0, 0);
    }

    public static DateTime fromDatePicker(DatePicker datePicker) {
        int day = datePicker.getDayOfMonth();
        int month = datePicker.getMonth() + 1;
        int year = datePicker.getYear();

        return new DateTime(year, month, day, 0, 0, 0, 0);
    }

    public static String getDateStringFrom(DateTime dateTime, String pattern) {
        DateTimeFormatter format = DateTimeFormat.forPattern(pattern);
        String result = format.print(dateTime);
        return result;
    }

    public static String getIsoStringFrom(Date date) {
        DateTime dateTime = new DateTime(date);
        return getIsoStringFrom(dateTime);
    }

    public static String getIsoStringFrom(DateTime dateTime) {
        DateTimeFormatter format = DateTimeFormat.forPattern(Constants.ISO_DATE_FORMAT);
        return format.print(dateTime);
    }

    public static String getUserStringFromDateTime(Context ctx, DateTime date) {
        if (date == null) return "";

        String userDatePattern = DateUtils.getUserDatePattern(ctx);

        // Must convert to uppercase.
//        String dateFormat = userDatePattern.toUpperCase();

        DateTimeFormatter format = DateTimeFormat.forPattern(userDatePattern);

        //return date.format(dateFormat);
        return format.print(date);
    }

    public static void setDatePicker(DateTime date, DatePicker datePicker) {
//        datePicker.updateDate(date.getYear(), date.getMonth() - 1, date.getDay());
        datePicker.updateDate(date.getYear(), date.getMonthOfYear(), date.getDayOfMonth());
    }
}
