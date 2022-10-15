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

package com.money.manager.ex.utils;

import android.content.Context;
import android.text.TextUtils;
import android.widget.DatePicker;

import com.money.manager.ex.Constants;
import com.money.manager.ex.MmexApplication;
import com.money.manager.ex.R;
import com.money.manager.ex.core.DateRange;
import com.money.manager.ex.core.InfoKeys;
import com.money.manager.ex.servicelayer.InfoService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;

import timber.log.Timber;

/**
 * Date/time utilities using Java standard Date classes.
 * Format patterns:
 * https://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html
 */

public class MmxDateTimeUtils {
    private Locale _locale = Locale.ENGLISH;

    @Inject
    public MmxDateTimeUtils() {

    }

    public MmxDateTimeUtils(Locale locale) {
        _locale = locale;
    }

    public Date from(int year, int monthOfYear, int dayOfMonth) {
        return new MmxDate(year, monthOfYear, dayOfMonth).toDate();
    }

    public Date from(DatePicker datePicker) {
        int day = datePicker.getDayOfMonth();
        int month = datePicker.getMonth();
        int year = datePicker.getYear();

        return new MmxDate(year, month, day).toDate();
    }

    public String format(Date date, String format) {
        return getFormatterFor(format).format(date);
    }

    /**
     *
     * @param resourceId String Id for name of the period.
     * @return Date range that matches the period selected.
     */
    public DateRange getDateRangeForPeriod(Context context, int resourceId) {
        String value = context.getString(resourceId);
        return getDateRangeForPeriod(context, value);
    }

    /**
     * Creates a date range from the period name. Used when selecting a date range from the
     * localized menus.
     * @param period Period name in local language.
     * @return Date Range object.
     */
    public DateRange getDateRangeForPeriod(Context context, String period) {
        if (TextUtils.isEmpty(period)) return null;

        MmxDate dateFrom = new MmxDate();
        MmxDate dateTo = new MmxDate();

        // we ignore the minutes at the moment, since the field in the db only stores the date value.

        if (period.equalsIgnoreCase(context.getString(R.string.all_transaction)) ||
                period.equalsIgnoreCase(context.getString(R.string.all_time))) {
            // All transactions.
            dateFrom = dateFrom.today().minusYears(1000);
            dateTo = dateTo.today().plusYears(1000);
        } else if (period.equalsIgnoreCase(context.getString(R.string.today))) {
            dateFrom = dateFrom.today();
            dateTo = dateTo.today();
        } else if (period.equalsIgnoreCase(context.getString(R.string.last7days))) {
            dateFrom = dateFrom.today().minusDays(7);
            dateTo = dateTo.today();
        } else if (period.equalsIgnoreCase(context.getString(R.string.last15days))) {
            dateFrom = dateFrom.today().minusDays(14);
            dateTo = dateTo.today();
        } else if (period.equalsIgnoreCase(context.getString(R.string.current_month))) {
            dateFrom = dateFrom.today().firstDayOfMonth();
            dateTo = dateTo.today().lastDayOfMonth();
        } else if (period.equalsIgnoreCase(context.getString(R.string.last30days))) {
            dateFrom = dateFrom.today().minusDays(30);
            dateTo = dateTo.today();
        } else if (period.equalsIgnoreCase(context.getString(R.string.last3months))) {
            dateFrom = dateFrom.today().minusMonths(3)
                    .firstDayOfMonth();
            dateTo = dateTo.today();
        } else if (period.equalsIgnoreCase(context.getString(R.string.last6months))) {
            dateFrom = dateFrom.today().minusMonths(6)
                    .firstDayOfMonth();
            dateTo = dateTo.today();
        } else if (period.equalsIgnoreCase(context.getString(R.string.current_year))) {
            dateFrom = dateFrom.today().firstMonthOfYear()
                    .firstDayOfMonth();
            dateTo = dateTo.today().lastMonthOfYear()
                    .lastDayOfMonth();
        } else if (period.equalsIgnoreCase(context.getString(R.string.future_transactions))) {
            // Future transactions
            dateFrom = dateFrom.today().plusDays(1);
            dateTo = dateTo.today().plusYears(1000);
        } else {
            dateFrom = null;
            dateTo = null;
        }

        DateRange result = new DateRange(dateFrom.toDate(), dateTo.toDate());
        return result;
    }

    public String getDateStringFrom(Date dateTime, String pattern) {
        SimpleDateFormat format = getFormatterFor(pattern);
//        DateTimeFormatter format = DateTimeFormat.forPattern(pattern);
//        String result = format.print(dateTime);
        return format.format(dateTime);
//        return result;
    }

    public int getFirstDayOfWeek() {
        Locale appLocale = MmexApplication.getApp().getAppLocale();
        Calendar cal = Calendar.getInstance(appLocale);
        return cal.getFirstDayOfWeek();
    }

    /**
     * Get pattern defined by the user.
     * @return pattern user define
     */
    public String getUserDatePattern(Context context) {
        InfoService service = new InfoService(context);
        String pattern = service.getInfoValue(InfoKeys.DATEFORMAT);

        if (!TextUtils.isEmpty(pattern)) {
            //replace part of pattern
            pattern = pattern.replace("%d", "dd").replace("%m", "MM")
                    .replace("%y", "yy").replace("%Y", "yyyy")
                    .replace("'", "''");
        }

        // && getContext().getResources().getStringArray(R.array.date_format_mask) != null
        String[] dateFormats = context.getResources().getStringArray(R.array.date_format_mask);
        if (TextUtils.isEmpty(pattern) && dateFormats.length > 0){
            pattern = dateFormats[0];
            pattern = pattern.replace("%d", "dd").replace("%m", "MM")
                    .replace("%y", "yy").replace("%Y", "yyyy")
                    .replace("'", "''");
        }

        return pattern;
    }

    public String getUserFormattedDate(Context context, Date date) {
        if (date == null) return "";

        MmxDate dateTime = new MmxDate(date);

        String userDatePattern = new MmxDateTimeUtils().getUserDatePattern(context);

        return dateTime.toString(userDatePattern);
    }

    public Date now() {
        return new MmxDate().toDate();
    }

    public void setDatePicker(Date date, DatePicker datePicker) {
        MmxDate dateTime = new MmxDate(date);
        datePicker.updateDate(dateTime.getYear(), dateTime.getMonthOfYear(), dateTime.getDayOfMonth());
    }

    /*
        Private
     */

    private SimpleDateFormat getFormatterFor(String format) {
        return new SimpleDateFormat(format, _locale);
    }
}
