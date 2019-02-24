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

package com.money.manager.ex.core;

/**
 * Common request codes for activity results.
 * The largest number is always to be kept at the bottom.
 */

public class RequestCodes {
    public static final int ACCOUNT = 12;
    public static final int ALLOCATION = 1;
    public static final int AMOUNT = 2;
    public static final int AMOUNT_FROM = 3;
    public static final int AMOUNT_TO = 4;
    public static final int ASSET_CLASS = 5;
    public static final int CATEGORY = 10;
    public static final int CURRENCY = 11;
    public static final int PASSCODE = 7;
    public static final int PAYEE = 8;
    public static final int PRICE = 9;
    /**
     * Used when selecting a database from a storage access framework.
     */
    public static final int SELECT_DOCUMENT = 15;
    public static final int SELECT_FILE = 6;
    public static final int SORT_ORDER = 13;
    public static final int SPLIT_TX = 14;
}
