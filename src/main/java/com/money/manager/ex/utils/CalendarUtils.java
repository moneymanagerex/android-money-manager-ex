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

import java.util.Calendar;
import java.util.Date;

/**
 * Various calendar utilities.
 * Created by Alen on 6/09/2015.
 */
public class CalendarUtils {
    public CalendarUtils() {
        mCalendar = Calendar.getInstance();
    }

    public CalendarUtils(Calendar calendar) {
        mCalendar = calendar;
    }

    private Calendar mCalendar;

    public CalendarUtils addDays(int value) {
        mCalendar.add(Calendar.DATE, value);
        return this;
    }

    public CalendarUtils addMonth(int value) {
        mCalendar.add(Calendar.MONTH, value);

        return this;
    }

    public CalendarUtils addYear(int value) {
        mCalendar.add(Calendar.YEAR, value);
        return this;
    }

    public Calendar getCalendar() {
        return mCalendar;
    }

    public Date getTime() {
        return mCalendar.getTime();
    }

    public int getHour() {
        return mCalendar.get(Calendar.HOUR_OF_DAY);
    }

    public int getMinute() {
        return mCalendar.get(Calendar.MINUTE);
    }

    public CalendarUtils setCalendar(Calendar calendar) {
        mCalendar = calendar;
        return this;
    }

    /**
     * Set the month for the current calendar.
     * @param month Month value i.e. Calendar.January. NOT ordinal, i.e. November != 11.
     * @return
     */
    public CalendarUtils setMonth(int month) {
        mCalendar.set(Calendar.MONTH, month);
        return this;
    }

    public CalendarUtils setYear(int year) {
        mCalendar.set(Calendar.YEAR, year);
        return this;
    }

    public CalendarUtils setNow() {
        mCalendar = Calendar.getInstance();

        return this;
    }

    /**
     * Sets the calendar to the first day of the month to which the calendar points to.
     * I.e. if the calendar is 2015-08-20, this will return 2015-08-01 00:00:00.000
     * @return The first day of the month in which the Calendar is set.
     */
    public CalendarUtils setFirstDayOfMonth() {
        mCalendar.set(Calendar.DAY_OF_MONTH, mCalendar.getActualMinimum(Calendar.DAY_OF_MONTH));
        return this;
    }

    public CalendarUtils setLastDayOfMonth() {
        mCalendar.set(Calendar.DAY_OF_MONTH,
            mCalendar.getActualMaximum(Calendar.DAY_OF_MONTH));

        return this;
    }

    public CalendarUtils setTimeToBeginningOfDay() {
        Calendar calendar = mCalendar;

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return this;
    }

    public CalendarUtils setTimeToEndOfDay() {
        Calendar calendar = mCalendar;

        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);

        return this;
    }

    public CalendarUtils setTime(Date date) {
        mCalendar.setTime(date);
        return this;
    }
}
