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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * Various calendar utilities.
 */
public class MmxDate {
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
        mCalendar.setTime(date);
    }

    public MmxDate(int year, int month, int day) {
        mCalendar = new GregorianCalendar(year, month, day);
    }

    private Calendar mCalendar;

    public MmxDate addDays(int value) {
        mCalendar.add(Calendar.DATE, value);
        return this;
    }

    public MmxDate minusDays(int value) {
        return addDays(-value);
    }

    public MmxDate plusDays(int value) {
        return addDays(value);
    }

    public MmxDate addMonth(int value) {
        mCalendar.add(Calendar.MONTH, value);

        return this;
    }

    public MmxDate addYear(int value) {
        mCalendar.add(Calendar.YEAR, value);
        return this;
    }

    public Calendar getCalendar() {
        return mCalendar;
    }

    public int getDayOfMonth() {
        return mCalendar.get(Calendar.DAY_OF_MONTH);
    }

    public int getHour() {
        return mCalendar.get(Calendar.HOUR_OF_DAY);
    }

    public int getMinute() {
        return mCalendar.get(Calendar.MINUTE);
    }

    public int getYear() { return mCalendar.get(Calendar.YEAR); }

    public int getMonth() {
        return mCalendar.get(Calendar.MONTH);
    }

    public MmxDate setCalendar(Calendar calendar) {
        mCalendar = calendar;
        return this;
    }

    public MmxDate setMinute(int minute) {
        mCalendar.set(Calendar.MINUTE, minute);
        return this;
    }

    public MmxDate setHour(int hour) {
        mCalendar.set(Calendar.HOUR_OF_DAY, hour);
        return this;
    }

    public MmxDate setDate(int dayOfMonth) {
        mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
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

    public MmxDate setNow() {
        mCalendar = Calendar.getInstance();
        return this;
    }

    /**
     * Sets the calendar to the first day of the month to which the calendar points to.
     * I.e. if the calendar is 2015-08-20, this will return 2015-08-01 00:00:00.000
     * @return The first day of the month in which the Calendar is set.
     */
    public MmxDate setFirstDayOfMonth() {
        mCalendar.set(Calendar.DAY_OF_MONTH, mCalendar.getActualMinimum(Calendar.DAY_OF_MONTH));
        return this;
    }

    public MmxDate setLastDayOfMonth() {
        mCalendar.set(Calendar.DAY_OF_MONTH,
            mCalendar.getActualMaximum(Calendar.DAY_OF_MONTH));

        return this;
    }

    public MmxDate setTimeToBeginningOfDay() {
        Calendar calendar = mCalendar;

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

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

    public Date toDate() {
        return mCalendar.getTime();
    }

    public String toString(String format) {
        return getFormatterFor(format).format(toDate());
    }

    private SimpleDateFormat getFormatterFor(String format) {
        return new SimpleDateFormat(format, Locale.ENGLISH);
    }

}
