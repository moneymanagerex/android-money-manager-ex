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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import hirondelle.date4j.DateTime;

/**
 * Utilities for DateTime (date4j).
 */
public class DateTimeUtils {

    public static DateTime today() {
        return DateTime.today(TimeZone.getDefault());
    }

    /**
     * Conversion factory.
     * @param calendar The date to use as the base.
     * @return The DateTime instance of the date.
     */
    public static DateTime from(Calendar calendar) {
        if (calendar == null) return null;

        SimpleDateFormat sdf = new SimpleDateFormat(Constants.PATTERN_DB_DATE);
        String date = sdf.format(calendar.getTime());
        return new DateTime(date);
    }

    public static DateTime fromDatePicker(DatePicker datePicker) {
        int day = datePicker.getDayOfMonth();
        int month = datePicker.getMonth() + 1;
        int year = datePicker.getYear();

        return new DateTime(year, month, day, 0, 0, 0, 0);
    }

    public static String getUserStringFromDateTime(Context ctx, DateTime date) {
        if (date == null) return "";

        String userDatePattern = DateUtils.getUserDatePattern(ctx);

        // Must convert to uppercase.
        String dateFormat = userDatePattern.toUpperCase();

        return date.format(dateFormat);
    }

    public static void setDatePicker(DateTime date, DatePicker datePicker) {
        datePicker.updateDate(date.getYear(), date.getMonth() - 1, date.getDay());
    }

}
