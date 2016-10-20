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

import com.money.manager.ex.Constants;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Date/time utilities using Java standard Date classes.
 */

public class MmxDateTimeUtils {
    private CalendarUtils _utils;

    public static String getIsoStringFrom(Date date) {
        if (date == null) return null;

        SimpleDateFormat format = new SimpleDateFormat(Constants.ISO_DATE_FORMAT);
        return format.format(date);

//        return dateTime.toString(Constants.ISO_DATE_FORMAT);
    }
}
