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

import android.text.TextUtils;

import com.money.manager.ex.Constants;

import java.text.ParseException;
import java.text.SimpleDateFormat;
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
//    private MmxDate _utils;
    private Locale _locale = Locale.ENGLISH;

    @Inject
    public MmxDateTimeUtils() {

    }

    public MmxDateTimeUtils(Locale locale) {
        _locale = locale;
    }

    public String getIsoStringFrom(Date date) {
        if (date == null) return null;

        SimpleDateFormat format = new SimpleDateFormat(Constants.ISO_DATE_FORMAT);
        return format.format(date);
    }

    public Date from(String isoString) {
        if (TextUtils.isEmpty(isoString)) return null;

        String pattern = Constants.ISO_DATE_FORMAT;
        return from(isoString, pattern);
    }

    public Date from(String dateString, String pattern) {
        if (TextUtils.isEmpty(dateString)) return null;

        try {
            return getFormatterFor(pattern).parse(dateString);
        } catch (ParseException e) {
            Timber.e(e, "parsing date string");
            return null;
        }
    }

    public Date from(int year, int monthOfYear, int dayOfMonth) {
        return new MmxDate(year, monthOfYear, dayOfMonth).toDate();
    }

    public String format(Date date, String format) {
        return getFormatterFor(format).format(date);
    }

    public Date now() {
        return new MmxDate().toDate();
    }

    private SimpleDateFormat getFormatterFor(String format) {
        return new SimpleDateFormat(format, _locale);
    }
}
