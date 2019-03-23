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

package com.money.manager.ex.budget;

import android.text.TextUtils;

import java.util.Locale;

/**
 * Parses budget name into year/month combination and vice versa.
 */
public class BudgetNameParser {

    private static final String SEPARATOR = "-";

    public int getYear(String name) {
        if (TextUtils.isEmpty(name)) {
            return 0;
        }

        if (name.contains(SEPARATOR)) {
            return getYearFromYearMonth(name);
        } else {
            return Integer.parseInt(name);
        }
    }

    public int getMonth(String name) {
        if (TextUtils.isEmpty(name)) {
            return 0;
        }

        if (name.contains(SEPARATOR)) {
            return getMonthFromYearMonth(name);
        } else {
            return Integer.parseInt(name);
        }
    }

    public String getName(int year, int month) {
        if (month == 0) {
            return Integer.toString(year);
        } else {
            return String.format(Locale.getDefault(), "%04d-%02d", year, month);
        }
    }

    private int getYearFromYearMonth(String yearMonthString) {
        int position = yearMonthString.indexOf(SEPARATOR);
        String valueString = yearMonthString.substring(0, position);
        return Integer.parseInt(valueString);
    }

    private int getMonthFromYearMonth(String yearMonthString) {
        int position = yearMonthString.indexOf(SEPARATOR);
        String valueString = yearMonthString.substring(position + 1);
        return Integer.parseInt(valueString);
    }
}
