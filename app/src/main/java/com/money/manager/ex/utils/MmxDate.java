/*
 * Copyright (C) 2012-2019 The Android Money Manager Ex Project Team
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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import androidx.annotation.NonNull;
import timber.log.Timber;

/**
 * Expanded Date type, matching JodaTime and other APIs.
 */
public class MmxDate {

    /**
     * The expected format is full ISO format: 2016-10-22T02:36:46.000+0200
     * @param dateString The date string in ISO format.
     * @return MmxDate instance.
     */
    public static MmxDate fromIso8601(String dateString) {
        if (dateString.length() < 28) {
            // manually handle short time-zone offset, i.e. 2016-10-22T02:36:46.000+02
            if (dateString.charAt(23) == '+' || dateString.charAt(23) == '-') {
                // append two zeroes
                dateString = dateString.concat("00");
            }

            // handle invalid format 2016-10-21T18:42:18.000Z
            if (dateString.charAt(23) == 'Z') {
                dateString = dateString.substring(0, 23);
                // append the current time zone time
                DateFormat offsetFormat = new SimpleDateFormat("Z");
                String offsetString = offsetFormat.format(new MmxDate().toDate());
                dateString += offsetString;
            }
        }

        return new MmxDate(dateString, Constants.ISO_8601_FORMAT);
    }

    public static Date from(String dateString, String pattern) {
        if (TextUtils.isEmpty(dateString)) return null;

        try {
            return getFormatterFor(pattern).parse(dateString);
        } catch (ParseException e) {
            Timber.e(e, "parsing date string");
            return null;
        }
    }

    public static MmxDate newDate() {
        MmxDate result = new MmxDate()
                .setTimeToBeginningOfDay();
        return result;
    }

    /*
        Instance
     */

    /**
     * The default constructor uses the current time instance by default.
     */
    public MmxDate() {
        mCalendar = Calendar.getInstance();
    }

    public MmxDate(Calendar calendar) {
        mCalendar = calendar;
    }

    public MmxDate(Date date) {
        mCalendar = new GregorianCalendar();

        if (date != null) {
            mCalendar.setTime(date);
        }
    }

    /**
     * Creates a date/time object from an ISO date string.
     * @param isoString ISO date string
     */
    public MmxDate(@NonNull String isoString) {
        String pattern = Constants.ISO_DATE_FORMAT;
        Date date = from(isoString, pattern);

        mCalendar = new GregorianCalendar();
        mCalendar.setTime(date);
    }

    public MmxDate(String dateString, String pattern) {
        Date date = from(dateString, pattern);

        mCalendar = new GregorianCalendar();
        mCalendar.setTime(date);
    }

    public MmxDate(int year, int month, int day) {
        mCalendar = new GregorianCalendar(year, month, day);
    }

    public MmxDate(long ticks) {
        mCalendar = new GregorianCalendar();
        mCalendar.setTimeInMillis(ticks);
    }

    private Calendar mCalendar;

    public MmxDate addDays(int value) {
        mCalendar.add(Calendar.DATE, value);
        return this;
    }

    public MmxDate addMonth(int value) {
        mCalendar.add(Calendar.MONTH, value);

        return this;
    }

    public MmxDate addYear(int value) {
        mCalendar.add(Calendar.YEAR, value);
        return this;
    }

    /**
     * Sets the calendar to the first day of the month to which the calendar points to.
     * I.e. if the calendar is 2015-08-20, this will return 2015-08-01 00:00:00.000
     * @return The first day of the month in which the Calendar is set.
     */
    public MmxDate firstDayOfMonth() {
        mCalendar.set(Calendar.DAY_OF_MONTH, mCalendar.getActualMinimum(Calendar.DAY_OF_MONTH));
        return this;
    }

    public MmxDate firstMonthOfYear() {
        mCalendar.set(Calendar.MONTH, mCalendar.getActualMinimum(Calendar.MONTH));
        return this;
    }

    public Calendar getCalendar() {
        return mCalendar;
    }

    public int getDayOfMonth() {
        return mCalendar.get(Calendar.DAY_OF_MONTH);
    }

    public int getDayOfWeek() {
        return mCalendar.get(Calendar.DAY_OF_WEEK);
    }

    public int getHour() {
        return getHourOfDay();
    }

    public int getHourOfDay() {
        return mCalendar.get(Calendar.HOUR_OF_DAY);
    }

    public long getMillis() {
        return mCalendar.getTimeInMillis();
    }

    public int getMinute() {
        return getMinuteOfHour();
    }

    public int getMinuteOfHour() {
        return mCalendar.get(Calendar.MINUTE);
    }

    public int getYear() { return mCalendar.get(Calendar.YEAR); }

    public int getMonth() {
        return mCalendar.get(Calendar.MONTH);
    }

    public int getMonthOfYear() {
        return getMonth();
    }

    /**
     * Converts the date/time value to the destination time zone.
     * @param timeZone The name of the time zone. I.e. "Europe/Berlin".
     * @return Date/Time value in the destination time zone.
     */
    public MmxDate inTimeZone(String timeZone) {
        // Keep the original value for conversion.
        long currentValue = mCalendar.getTimeInMillis();

        // now create the calendar in the destination time zone and convert the value.
        mCalendar = new GregorianCalendar(TimeZone.getTimeZone(timeZone));

        mCalendar.setTimeInMillis(currentValue);

        return this;
    }

    public MmxDate lastDayOfMonth() {
        mCalendar.set(Calendar.DAY_OF_MONTH,
                mCalendar.getActualMaximum(Calendar.DAY_OF_MONTH));

        return this;
    }

    public MmxDate lastMonthOfYear() {
        mCalendar.set(Calendar.MONTH, mCalendar.getActualMaximum(Calendar.MONTH));
        return this;
    }

    public MmxDate minusDays(int value) {
        return addDays(-value);
    }

    public MmxDate minusMonths(int value) {
        return addMonth(-value);
    }

    public MmxDate minusYears(int value) {
        return addYear(-value);
    }

    public MmxDate plusDays(int value) {
        return addDays(value);
    }

    public MmxDate plusMonths(int value) {
        return addMonth(value);
    }

    public MmxDate plusWeeks(int value) {
        mCalendar.add(Calendar.WEEK_OF_YEAR, value);
        return this;
    }

    public MmxDate plusYears(int value) {
        return addYear(value);
    }

    public MmxDate setCalendar(Calendar calendar) {
        mCalendar = calendar;
        return this;
    }

    public MmxDate setDate(int dayOfMonth) {
        mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        return this;
    }

    public MmxDate setHour(int hour) {
        mCalendar.set(Calendar.HOUR_OF_DAY, hour);
        return this;
    }

    public MmxDate setMinute(int minute) {
        mCalendar.set(Calendar.MINUTE, minute);
        return this;
    }

    public MmxDate setMilisecond(int milisecond) {
        mCalendar.set(Calendar.MILLISECOND, milisecond);
        return this;
    }

    /**
     * Set the month for the current calendar.
     * @param month Month value i.e. Calendar.January. NOT ordinal, i.e. November != 11.
     * @return
     */
    public MmxDate setMonth(int month) {
        mCalendar.set(Calendar.MONTH, month);
        return this;
    }

    public MmxDate setSecond(int second) {
        mCalendar.set(Calendar.SECOND, second);
        return this;
    }

    public MmxDate setYear(int year) {
        mCalendar.set(Calendar.YEAR, year);
        return this;
    }

//    public MmxDate setNow() {
//        mCalendar = Calendar.getInstance();
//        return this;
//    }

    public MmxDate setTimeToBeginningOfDay() {
        setHour(0);
        setMinute(0);
        setSecond(0);
        setMilisecond(0);

        return this;
    }

    public MmxDate setTimeToEndOfDay() {
        Calendar calendar = mCalendar;

        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);

        return this;
    }

    public MmxDate setTime(Date date) {
        mCalendar.setTime(date);
        return this;
    }

    public MmxDate setTimeZone(String timeZone) {
        mCalendar.setTimeZone(TimeZone.getTimeZone(timeZone));
        return this;
    }

    public Date toDate() {
        return mCalendar.getTime();
    }

    public MmxDate today() {
        return setTimeToBeginningOfDay();
    }

    public String toString(String format) {
        return getFormatterFor(format).format(toDate());
    }

    public String toIsoDateString() {
        SimpleDateFormat format = new SimpleDateFormat(Constants.ISO_DATE_FORMAT, Locale.ENGLISH);
        return format.format(toDate());
    }

    public String toIsoString() {
        SimpleDateFormat format = new SimpleDateFormat(Constants.ISO_8601_FORMAT, Locale.ENGLISH);
        return format.format(toDate());
    }

    public String toIsoDateShortTimeString() {
        SimpleDateFormat format = new SimpleDateFormat(Constants.ISO_DATE_SHORT_TIME_FORMAT, Locale.ENGLISH);
        return format.format(toDate());
    }

    /*
        Private
     */

    private static SimpleDateFormat getFormatterFor(String format) {
        return new SimpleDateFormat(format, Locale.ENGLISH);
    }
}
