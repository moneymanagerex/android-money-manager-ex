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

import android.content.Context;

import com.money.manager.ex.R;

import java.util.HashMap;

/**
 * Budget period helper
 */
public class BudgetPeriods {

    private static HashMap<String, BudgetPeriodEnum> periodEnumLookup = null;
    private static HashMap<BudgetPeriodEnum, String> periodTranslationLookup = null;

    /**
     * Returns proper enum for period name in the database
     * @param periodString
     * @return
     */
    public static BudgetPeriodEnum getEnum(String periodString) {
        if (periodEnumLookup == null) {
            periodEnumLookup = new HashMap<>();
        }

        if (periodEnumLookup.size() == 0) {
            periodEnumLookup.put("None"       , BudgetPeriodEnum.NONE);
            periodEnumLookup.put("Weekly"     , BudgetPeriodEnum.WEEKLY);
            periodEnumLookup.put("Bi-Weekly"  , BudgetPeriodEnum.BI_WEEKLY);
            periodEnumLookup.put("Monthly"    , BudgetPeriodEnum.MONTHLY);
            periodEnumLookup.put("Bi-Monthly" , BudgetPeriodEnum.BI_MONTHLY);
            periodEnumLookup.put("Quarterly"  , BudgetPeriodEnum.QUARTERLY);
            periodEnumLookup.put("Half-Yearly", BudgetPeriodEnum.HALF_YEARLY);
            periodEnumLookup.put("Yearly"     , BudgetPeriodEnum.YEARLY);
            periodEnumLookup.put("Daily"      , BudgetPeriodEnum.DAILY);
        }

        return periodEnumLookup.containsKey(periodString) ? periodEnumLookup.get(periodString) : BudgetPeriodEnum.NONE;
    }

    /**
     * Trampoline for translation by enum
     * @param context
     * @param periodString
     * @return
     */
    public static String getPeriodTranslationForEnum(Context context, String periodString) {
        return getPeriodTranslationForEnum(context, getEnum(periodString));
    }

    /**
     * Helper function to translate the string literals of period in the database.
     * @param context
     * @param periodEnum string value from the database
     * @return translated string
     */
    public static String getPeriodTranslationForEnum(Context context, BudgetPeriodEnum periodEnum) {
        if (periodTranslationLookup == null) {
            periodTranslationLookup = new HashMap<>();
        }

        if (periodTranslationLookup.size() == 0) {
            periodTranslationLookup.put(BudgetPeriodEnum.NONE, context.getString(R.string.none));
            periodTranslationLookup.put(BudgetPeriodEnum.WEEKLY, context.getString(R.string.weekly));
            periodTranslationLookup.put(BudgetPeriodEnum.BI_WEEKLY, context.getString(R.string.bi_weekly));
            periodTranslationLookup.put(BudgetPeriodEnum.MONTHLY, context.getString(R.string.monthly));
            periodTranslationLookup.put(BudgetPeriodEnum.BI_MONTHLY, context.getString(R.string.bi_monthly));
            periodTranslationLookup.put(BudgetPeriodEnum.QUARTERLY, context.getString(R.string.quaterly));
            periodTranslationLookup.put(BudgetPeriodEnum.HALF_YEARLY, context.getString(R.string.half_year));
            periodTranslationLookup.put(BudgetPeriodEnum.YEARLY, context.getString(R.string.yearly));
            periodTranslationLookup.put(BudgetPeriodEnum.DAILY, context.getString(R.string.daily));
        }

        return periodTranslationLookup.containsKey(periodEnum)
                ? periodTranslationLookup.get(periodEnum)
                : context.getString(R.string.none);
    }

    public static double getMonthlyEstimate(BudgetPeriodEnum periodEnum, double amount) {
        double estimated = 0;
        int ndays = 365;
        if (periodEnum == BudgetPeriodEnum.MONTHLY) {
            estimated = amount;
        }
        else if (periodEnum == BudgetPeriodEnum.YEARLY) {
            estimated = amount / 12;
        }
        else if (periodEnum == BudgetPeriodEnum.WEEKLY) {
            estimated = ((amount / 7) * ndays) / 12;
        }
        else if (periodEnum == BudgetPeriodEnum.BI_WEEKLY) {
            estimated = ((amount / 14) * ndays) / 12;
        }
        else if (periodEnum == BudgetPeriodEnum.BI_MONTHLY) {
            estimated = amount / 2;
        }
        else if (periodEnum == BudgetPeriodEnum.QUARTERLY) {
            estimated = amount / 3;
        }
        else if (periodEnum == BudgetPeriodEnum.HALF_YEARLY) {
            estimated = (amount / 6);
        }
        else if (periodEnum == BudgetPeriodEnum.DAILY) {
            estimated = (amount * ndays) / 12;
        }

        return estimated;
    }

    public static double getYearlyEstimate(BudgetPeriodEnum periodEnum, double amount) {
        double estimated = 0;
        if (periodEnum == BudgetPeriodEnum.MONTHLY) {
            estimated = amount * 12;
        }
        else if (periodEnum == BudgetPeriodEnum.YEARLY) {
            estimated = amount;
        }
        else if (periodEnum == BudgetPeriodEnum.WEEKLY) {
            estimated = amount * 52;
        }
        else if (periodEnum == BudgetPeriodEnum.BI_WEEKLY) {
            estimated = amount * 26;
        }
        else if (periodEnum == BudgetPeriodEnum.BI_MONTHLY) {
            estimated = amount * 6;
        }
        else if (periodEnum == BudgetPeriodEnum.QUARTERLY) {
            estimated = amount * 4;
        }
        else if (periodEnum == BudgetPeriodEnum.HALF_YEARLY) {
            estimated = amount * 2;
        }
        else if (periodEnum == BudgetPeriodEnum.DAILY) {
            estimated = amount * 365;
        }

        return estimated;
    }
}
