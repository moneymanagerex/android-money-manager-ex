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
import com.money.manager.ex.R;
import com.money.manager.ex.core.DateRange;

import org.apache.commons.lang3.StringUtils;
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
        return DateTime.now()
                .withHourOfDay(0)
                .withMinuteOfHour(0)
                .withSecondOfMinute(0)
                .withMillisOfSecond(0);
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

        DateTime result = new DateTime(calendar.getTime());
        return result;
    }

    public static DateTime from(int year, int monthOfYear, int dayOfMonth) {
        return new DateTime(year, monthOfYear, dayOfMonth, 0, 0);
    }

    public static DateTime from(DatePicker datePicker) {
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

    public static String getIsoStringFrom(DateTime dateTime) {
        if (dateTime == null) return null;

        return dateTime.toString(Constants.ISO_DATE_FORMAT);
    }

    public static String getUserStringFromDateTime(Context ctx, DateTime dateTime) {
        if (dateTime == null) return "";

        String userDatePattern = DateUtils.getUserDatePattern(ctx);

        return dateTime.toString(userDatePattern);
    }

    public static void setDatePicker(DateTime date, DatePicker datePicker) {
        datePicker.updateDate(date.getYear(), date.getMonthOfYear() - 1, date.getDayOfMonth());
    }

    /**
     *
     * @param resourceId String Id for name of the period.
     * @return Date range that matches the period selected.
     */
    public static DateRange getDateRangeForPeriod(Context context, int resourceId) {
        String value = context.getString(resourceId);
        return getDateRangeForPeriod(context, value);
    }

    /**
     * Creates a date range from the period name. Used when selecting a date range from the
     * localized menus.
     * @param period Period name in local language.
     * @return Date Range object.
     */
    public static DateRange getDateRangeForPeriod(Context context, String period) {
        if (StringUtils.isEmpty(period)) return null;

        DateTime dateFrom;
        DateTime dateTo;

        // we ignore the minutes at the moment, since the field in the db only stores the date value.

        if (period.equalsIgnoreCase(context.getString(R.string.all_transaction)) ||
            period.equalsIgnoreCase(context.getString(R.string.all_time))) {
            // All transactions.
            dateFrom = MyDateTimeUtils.today().minusYears(1000);
            dateTo = MyDateTimeUtils.today().plusYears(1000);
        } else if (period.equalsIgnoreCase(context.getString(R.string.today))) {
            dateFrom = MyDateTimeUtils.today();
            dateTo = MyDateTimeUtils.today();
        } else if (period.equalsIgnoreCase(context.getString(R.string.last7days))) {
            dateFrom = MyDateTimeUtils.today().minusDays(7);
            dateTo = MyDateTimeUtils.today();
        } else if (period.equalsIgnoreCase(context.getString(R.string.last15days))) {
            dateFrom = MyDateTimeUtils.today().minusDays(14);
            dateTo = MyDateTimeUtils.today();
        } else if (period.equalsIgnoreCase(context.getString(R.string.current_month))) {
            dateFrom = MyDateTimeUtils.today().dayOfMonth().withMinimumValue();
            dateTo = MyDateTimeUtils.today().dayOfMonth().withMaximumValue();
        } else if (period.equalsIgnoreCase(context.getString(R.string.last30days))) {
            dateFrom = MyDateTimeUtils.today().minusDays(30);
            dateTo = MyDateTimeUtils.today();
        } else if (period.equalsIgnoreCase(context.getString(R.string.last3months))) {
            dateFrom = MyDateTimeUtils.today().minusMonths(3)
                .dayOfMonth().withMinimumValue();
            dateTo = MyDateTimeUtils.today();
        } else if (period.equalsIgnoreCase(context.getString(R.string.last6months))) {
            dateFrom = MyDateTimeUtils.today().minusMonths(6)
                .dayOfMonth().withMinimumValue();
            dateTo = MyDateTimeUtils.today();
        } else if (period.equalsIgnoreCase(context.getString(R.string.current_year))) {
            dateFrom = MyDateTimeUtils.today().monthOfYear().withMinimumValue()
                .dayOfMonth().withMinimumValue();
            dateTo = MyDateTimeUtils.today().monthOfYear().withMaximumValue()
                .dayOfMonth().withMaximumValue();
        } else if (period.equalsIgnoreCase(context.getString(R.string.future_transactions))) {
            // Future transactions
            dateFrom = MyDateTimeUtils.today().plusDays(1);
            dateTo = MyDateTimeUtils.today().plusYears(1000);
        } else {
            dateFrom = null;
            dateTo = null;
        }

        DateRange result = new DateRange(dateFrom, dateTo);
        return result;
    }

}
