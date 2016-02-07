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
import android.util.Log;
import android.widget.DatePicker;
import android.widget.TextView;

import com.money.manager.ex.Constants;
import com.money.manager.ex.MoneyManagerApplication;
import com.money.manager.ex.R;
import com.money.manager.ex.core.DateRange;
import com.money.manager.ex.core.ExceptionHandler;
import com.money.manager.ex.servicelayer.InfoService;

import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import hirondelle.date4j.DateTime;

/**
 * Date utilities
 */
public class DateUtils {
    private static final String LOGCAT = DateUtils.class.getSimpleName();

    public static Date getDateFromIsoString(String date) {
        return getDateFromString(date, Constants.PATTERN_DB_DATE);
    }

    public static Date getDateFromString(String date, String pattern) {
        try {
            return new SimpleDateFormat(pattern).parse(date);
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * Convert string date into date object using pattern defined by the user.
     *
     * @param ctx  context
     * @param date string to convert
     * @return date converted
     */
    public static Date getDateFromUserString(Context ctx, String date) {
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
            Locale locale = MoneyManagerApplication.getInstanceApp().getAppLocale();
            return new SimpleDateFormat(pattern, locale).parse(date);
        } catch (ParseException e) {
            ExceptionHandler handler = new ExceptionHandler(context, null);
            handler.handle(e, "parsing date");
        }
        return null;
    }

    public static String getStringFromDate(Date date, String pattern) {
        if (date == null) return null;

        return new SimpleDateFormat(pattern).format(date);
    }

    /**
     * @param date    object to convert in string
     * @param pattern pattern to use to convert
     * @return string representation of the date
     */
    public static String getStringFromDate(Context context, Date date, String pattern) {
        if (date == null) return null;

        Locale locale = context.getResources().getConfiguration().locale;
        return new SimpleDateFormat(pattern, locale).format(date);
    }

    /**
     * Convert date object in string SQLite date format
     *
     * @param date to convert
     * @return string formatted date SQLite
     */
    public static String getIsoStringDate(Date date) {
        return getStringFromDate(date, Constants.PATTERN_DB_DATE);
    }

    /**
     * Get pattern define from user
     *
     * @return pattern user define
     */
    public static String getUserDatePattern(Context context) {
        InfoService service = new InfoService(context);
        String pattern = service.getInfoValue("DATEFORMAT");

        if (!StringUtils.isEmpty(pattern)) {
            //replace part of pattern
            pattern = pattern.replace("%d", "dd").replace("%m", "MM")
                .replace("%y", "yy").replace("%Y", "yyyy")
                .replace("'", "''");
        }

        if (StringUtils.isEmpty(pattern)
                && context.getResources().getStringArray(R.array.date_format_mask) != null
                && context.getResources().getStringArray(R.array.date_format_mask).length > 0){
            pattern= context.getResources().getStringArray(R.array.date_format_mask)[0];
            pattern = pattern.replace("%d", "dd").replace("%m", "MM")
                .replace("%y", "yy").replace("%Y", "yyyy")
                .replace("'", "''");
        }

        return pattern;
    }

    /**
     * Convert date object to string using user's preferences for date format.
     *
     * @param date date value
     * @return string The date formatted according to user preferences.
     */
    public static String getUserStringFromDate(Context ctx, Date date) {
        return getStringFromDate(ctx, date, getUserDatePattern(ctx));
    }

    /**
     * This function from the date picker returns a date in java
     *
     * @param datePicker date picker control
     * @return java date
     */
    public static Date getDateFromDatePicker(DatePicker datePicker) {
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

    public static Date getToday() {
        Date today = new CalendarUtils().setNow()
                .setTimeToBeginningOfDay().getTime();
        return today;
    }

    public static String getYesterdayFrom(String isoDate) {
        String result = null;

        try {
            SimpleDateFormat sdf = new SimpleDateFormat(Constants.PATTERN_DB_DATE);
            Date givenDate = sdf.parse(isoDate);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(givenDate);
            calendar.add(Calendar.DATE, -1);
            Date yesterday = calendar.getTime();

            result = sdf.format(yesterday);
            return result;
        } catch (Exception e) {
            // ExceptionHandler handler = new ExceptionHandler()
            Log.e(LOGCAT, "Error parsing date");
        }
        return result;
    }

    private Context context;

    // Instance methods.

    public DateUtils(Context context) {
        this.context = context.getApplicationContext();
    }

    public void formatExtendedDate(TextView dateTextView, Date date) {
        try {
            Locale locale = MoneyManagerApplication.getInstanceApp().getAppLocale();
            SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.LONG_DATE_PATTERN, locale);
            // use a shorted, defined, format, i.e. Tue, 28 Aug 2015 for fixed width, if
            // the status selector is to switch to an icon.
//            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy", locale);

            dateTextView.setText(dateFormat.format(date));
        } catch (Exception e) {
            ExceptionHandler handler = new ExceptionHandler(context, this);
            handler.handle(e, "formatting extended date");
        }
    }

    /**
     *
     * @param resourceId String Id for name of the period.
     * @return Date range that matches the period selected.
     */
    public DateRange getDateRangeForPeriod(int resourceId) {
        String value = this.context.getString(resourceId);
        return getDateRangeForPeriod(value);
    }

    public DateRange getDateRangeForPeriod(String period) {
        if (StringUtils.isEmpty(period)) return null;

        Date dateFrom;
        Date dateTo;
        CalendarUtils cal = new CalendarUtils();

        // we ignore the minutes at the moment, since the field in the db only stores the date value.

        if (period.equalsIgnoreCase(this.context.getString(R.string.all_transaction)) ||
                period.equalsIgnoreCase(this.context.getString(R.string.all_time))) {
            // All transactions.
            dateFrom = cal.setNow().addYear(-1000).getTime();
            dateTo = cal.setNow().addYear(1000).getTime();
        } else if (period.equalsIgnoreCase(this.context.getString(R.string.today))) {
//            result.add("(julianday(date('now')) = julianday(" + QueryAllData.Date + "))");
            dateFrom = cal.setNow().getTime();
            dateTo = dateFrom;
        } else if (period.equalsIgnoreCase(this.context.getString(R.string.last7days))) {
//            result.add("(julianday(date('now')) - julianday(" + QueryAllData.Date + ") <= 7)");
            dateFrom = cal.setNow().addDays(-7).getTime();
            dateTo = cal.setNow().getTime();
        } else if (period.equalsIgnoreCase(this.context.getString(R.string.last15days))) {
//            result.add("(julianday(date('now')) - julianday(" + QueryAllData.Date + ") <= 14)");
            dateFrom = cal.setNow().addDays(-14).getTime();
            dateTo = cal.setNow().getTime();
        } else if (period.equalsIgnoreCase(this.context.getString(R.string.current_month))) {
//            result.add(QueryAllData.Month + "=" + Integer.toString(Calendar.getInstance().get(Calendar.MONTH) + 1));
//            result.add(QueryAllData.Year + "=" + Integer.toString(Calendar.getInstance().get(Calendar.YEAR)));
            dateFrom = cal.setNow().setFirstDayOfMonth().getTime();
            dateTo = cal.setLastDayOfMonth().getTime();
        } else if (period.equalsIgnoreCase(this.context.getString(R.string.last30days))) {
//            result.add("(julianday(date('now')) - julianday(" + QueryAllData.Date + ") <= 30)");
            dateFrom = cal.setNow().addDays(-30).getTime();
            dateTo = cal.setNow().getTime();
        } else if (period.equalsIgnoreCase(this.context.getString(R.string.last3months))) {
//            result.add("(julianday(date('now')) - julianday(" + QueryAllData.Date + ") <= 90)");
            dateFrom = cal.setNow().addMonth(-3).setFirstDayOfMonth().getTime();
            dateTo = cal.setNow().getTime();
        } else if (period.equalsIgnoreCase(this.context.getString(R.string.last6months))) {
//            result.add("(julianday(date('now')) - julianday(" + QueryAllData.Date + ") <= 180)");
            dateFrom = cal.setNow().addMonth(-6).setFirstDayOfMonth().getTime();
            dateTo = cal.setNow().getTime();
        } else if (period.equalsIgnoreCase(this.context.getString(R.string.current_year))) {
//            result.add(QueryAllData.Year + "=" + Integer.toString(Calendar.getInstance().get(Calendar.YEAR)));
            dateFrom = cal.setNow().setMonth(Calendar.JANUARY).setFirstDayOfMonth().getTime();
            dateTo = cal.setMonth(Calendar.DECEMBER).setLastDayOfMonth().getTime();
        } else if (period.equalsIgnoreCase(this.context.getString(R.string.future_transactions))) {
            // Future transactions
//            result.add("date(" + QueryAllData.Date + ") > date('now')");
            dateFrom = cal.setNow().addDays(1).getTime();
            dateTo = cal.addYear(1000).getTime();
        } else {
            dateFrom = null;
            dateTo = null;
        }

        DateRange result = new DateRange(dateFrom, dateTo);
        return result;
    }

}
