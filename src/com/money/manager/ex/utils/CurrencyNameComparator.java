/*
 * Copyright (C) 2012-2015 The Android Money Manager Ex Project Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package com.money.manager.ex.utils;

import com.money.manager.ex.database.TableCurrencyFormats;

import java.util.Comparator;

/**
 * Compare two Currencies by Name.
 * Created by Alen Siljak on 11/03/2015.
 */
public class CurrencyNameComparator implements Comparator<TableCurrencyFormats> {
    @Override
    public int compare(TableCurrencyFormats o1, TableCurrencyFormats o2) {
        return o1.getCurrencyName().compareTo(o2.getCurrencyName());
    }
}
