/*
 * Copyright (C) 2012-2024 The Android Money Manager Ex Project Team
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

/**
 * Budget period helper
 */
public class BudgetPeriods {

    /**
     * Returns proper enum for period name in the database
     * @param periodString
     * @return
     */
    public static BudgetPeriodEnum getEnum(String periodString) {
        return BudgetPeriodEnum.fromString(periodString);
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
     * @param periodEnum
     * @return translated string
     */
    public static String getPeriodTranslationForEnum(Context context, BudgetPeriodEnum periodEnum) {
        return BudgetPeriodEnum.getTranslation(context, periodEnum);
    }

    /**
     * Method to estimate monthly equivalent for a given period
     * @param periodEnum
     * @param amount
     * @return
     */
    public static double getMonthlyEstimate(BudgetPeriodEnum periodEnum, double amount) {
//        double daysInPeriod = periodEnum.getDaysInPeriod();
//        return (amount / daysInPeriod) * 30; // Simplified calculation for monthly estimate
        return ( getYearlyEstimate(periodEnum, amount) / 12) ; // compute 1/12 of annual value
    }

    /**
     * Method to estimate yearly equivalent for a given period
     * @param periodEnum
     * @param amount
     * @return
     */
    public static double getYearlyEstimate(BudgetPeriodEnum periodEnum, double amount) {
//        double daysInPeriod = periodEnum.getDaysInPeriod();
//        return (amount / daysInPeriod) * 365; // Simplified calculation for yearly estimate
        return ( amount * periodEnum.getOccursTimes() );
    }
}
