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

package com.money.manager.ex.budget;

import android.content.Context;

import com.money.manager.ex.R;

/**
 * Budget period
 */

public enum BudgetPeriodEnum {
    NONE("None",  0),
    WEEKLY("Weekly",  52), // ok
    BI_WEEKLY("Fortnightly",  26), // mod
    MONTHLY("Monthly",  12), // ok
    BI_MONTHLY("Every 2 Months",  6), // mod
    QUARTERLY("Quarterly",  4), //ok
    HALF_YEARLY("Half-Yearly",  2), //ok
    YEARLY("Yearly",  1), // ok
    DAILY("Daily", 365); // ok

    private final String displayName;
    private final int occursTimes; // How many times event occurs in a year

    BudgetPeriodEnum(String displayName,  int occursTimes) {
        this.displayName = displayName;
        this.occursTimes = occursTimes;
    }

    public String getDisplayName() {
        return displayName;
    }

    // please use new getOccursTime that has better compute
    @Deprecated
    public int getDaysInPeriod() {
        return ( occursTimes == 0 ? 99999 : occursTimes ); // simulate infinity
    }

    public int getOccursTimes() { return occursTimes; }

    public static BudgetPeriodEnum fromString(String periodString) {
        for (BudgetPeriodEnum period : values()) {
            if (period.displayName.equalsIgnoreCase(periodString)) {
                return period;
            }
        }
        return NONE; // Default to NONE if no match is found
    }

    public static String getTranslation(Context context, BudgetPeriodEnum periodEnum) {
        switch (periodEnum) {
            case WEEKLY:
                return context.getString(R.string.weekly);
            case BI_WEEKLY:
                return context.getString(R.string.bi_weekly);
            case MONTHLY:
                return context.getString(R.string.monthly);
            case BI_MONTHLY:
                return context.getString(R.string.bi_monthly);
            case QUARTERLY:
                return context.getString(R.string.quarterly);
            case HALF_YEARLY:
                return context.getString(R.string.half_year);
            case YEARLY:
                return context.getString(R.string.yearly);
            case DAILY:
                return context.getString(R.string.daily);
            case NONE:
            default:
                return context.getString(R.string.none);
        }
    }
}