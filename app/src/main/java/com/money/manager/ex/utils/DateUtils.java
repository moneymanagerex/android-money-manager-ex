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
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Date utilities
 */
public class DateUtils {
    public static Date getDateFromString(String date, String pattern) {
        DateTimeFormatter format = DateTimeFormat.forPattern(pattern);
        return format.parseDateTime(date).toDate();
    }

    /**
     * Convert string date into date object using pattern defined by the user.
     *
     * @param ctx  context
     * @param date string to convert
     * @return date converted
     */
    public static Date getDateFromUserString(Context ctx, String date) {
        return getDateFromString(date, getUserDatePattern(ctx));
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
     * Get pattern defined by the user.
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

    public static Date getToday() {
        Date today = new CalendarUtils().setNow()
                .setTimeToBeginningOfDay().getTime();
        return today;
    }

    private Context context;

    // Instance methods.

    public DateUtils(Context context) {
        this.context = context.getApplicationContext();
    }

    public void formatExtendedDate(TextView dateTextView, DateTime date) {
        String displayValue = MyDateTimeUtils.getDateStringFrom(date, Constants.LONG_DATE_PATTERN);
        dateTextView.setText(displayValue);
    }
}
