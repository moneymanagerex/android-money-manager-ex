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
package com.money.manager.ex.core;

import android.text.TextUtils;

import com.money.manager.ex.Constants;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

/**
 * Various methods that help out working with numbers.
 */
public class NumericHelper {
    public static boolean isNumeric(String str)
    {
        try
        {
            double d = Double.parseDouble(str);
        }
        catch(NumberFormatException nfe)
        {
            return false;
        }
        return true;
    }

    public int tryParse(String value) {
        int result;
        try {
            result = Integer.parseInt(value);
        } catch (Exception ex) {
            // nothing
            result = Constants.NOT_SET;
        }
        return result;
    }

    public double roundNumber(double amount, int decimals) {
        BigDecimal x = new BigDecimal(amount).setScale(decimals, RoundingMode.HALF_EVEN);
        double result = x.doubleValue();
        return result;
    }

    public String getNumberFormatted(double value, int decimals, String decimalPoint, String groupSeparator) {
        value = roundNumber(value, decimals);

        // set format
        DecimalFormatSymbols formatSymbols = new DecimalFormatSymbols();
        // getDecimalPoint()
        if (!(TextUtils.isEmpty(decimalPoint))) {
            formatSymbols.setDecimalSeparator(decimalPoint.charAt(0));
        }
        // getGroupSeparator()
        if (!(TextUtils.isEmpty(groupSeparator))) {
            formatSymbols.setGroupingSeparator(groupSeparator.charAt(0));
        }

        DecimalFormat formatter = new DecimalFormat();
        // set which symbols to use
        formatter.setDecimalFormatSymbols(formatSymbols);

        formatter.setMaximumFractionDigits(decimals);
        formatter.setMinimumFractionDigits(decimals);

        String result = formatter.format(value);
        return result;
    }

    public String getNumberFormatted(double value, double scale, String decimalPoint, String groupSeparator) {
        // Round the number first.
        int decimals = getNumberOfDecimals(scale);

        return getNumberFormatted(value, decimals, decimalPoint, groupSeparator);
    }

    /**
     * Extracts the number of decimal places from scale/precision value.
     * @param scale
     * @return
     */
    public int getNumberOfDecimals(double scale) {
        double decimals = Math.log(scale) / Math.log(10.0);
        int result = (int) Math.round(decimals);
        return result;
    }

}
