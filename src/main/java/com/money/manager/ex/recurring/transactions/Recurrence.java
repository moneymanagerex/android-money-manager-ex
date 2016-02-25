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

package com.money.manager.ex.recurring.transactions;

import java.util.HashMap;
import java.util.Map;

/**
 * Types of recurrence
 */
public enum Recurrence {
    ONCE (0),
    WEEKLY (1),
    BIWEEKLY (2),
    MONTHLY (3),
    BIMONTHLY (4),
    QUARTERLY (5),
    SEMIANNUALLY (6),
    ANNUALLY (7),
    FOUR_MONTHS (8),
    FOUR_WEEKS (9),
    DAILY (10),
    IN_X_DAYS (11),
    IN_X_MONTHS (12),
    EVERY_X_DAYS (13),
    EVERY_X_MONTHS (14),
    MONTHLY_LAST_DAY (15),
    MONTHLY_LAST_BUSINESS_DAY (16);

    Recurrence(int value) {
        mValue = value;
    }

    private int mValue;

//    private static Map<Integer, Recurrence> mMap = new HashMap<>();

//    static {
//        for (Recurrence item : Recurrence.values()) {
//            mMap.put(item.getValue(), item);
//        }
//    }

//    public static Recurrence valueOf(int value) {
//        return mMap.get(value);
//    }

    public static Recurrence valueOf(int value) {
        for (Recurrence item : Recurrence.values()) {
            if (item.getValue() == value) {
                return item;
            }
        }
        return null; 
    }

    public int getValue() {
        return mValue;
    }

//    public Recurrence get(int value) {
//        return mMap.get(value);
//    }

}
