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
    NONE("None", 0),
    WEEKLY("Weekly", 7), // ok
    BI_WEEKLY("Fortnightly", 14), // mod
    MONTHLY("Monthly", 30), // ok
    BI_MONTHLY("Every 2 Months", 60), // mod
    QUARTERLY("Quarterly", 90), //ok
    HALF_YEARLY("Half-Yearly", 180), //ok
    YEARLY("Yearly", 365), // ok
    DAILY("Daily", 1); // ok

    private final String displayName;
    private final int daysInPeriod; // Approximate number of days for each period

    BudgetPeriodEnum(String displayName, int daysInPeriod) {
        this.displayName = displayName;
        this.daysInPeriod = daysInPeriod;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getDaysInPeriod() {
        return daysInPeriod;
    }

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