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

import android.content.Context;
import android.text.TextUtils;

import com.money.manager.ex.Constants;
import com.money.manager.ex.currency.CurrencyService;
import com.money.manager.ex.domainmodel.Currency;

import info.javaperformance.money.Money;

/**
 * Various methods that help out working with numbers.
 */
public class NumericHelper {
    private CurrencyService mCurrencyService;

    public static boolean isNumeric(String str) {
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

    public static String toString(Integer value) {
        if (value != null) {
            return Integer.toString(value);
        } else {
            return null;
        }
    }

    public static int toInt(String value) {
        return Integer.parseInt(value);
    }

    public static Integer toInteger(String value) {
        Integer result;
        if (!TextUtils.isEmpty(value) && NumericHelper.isNumeric(value)) {
            result = Integer.parseInt(value);
        } else {
            result = null;
        }
        return result;
    }

    // Instance methods

    public NumericHelper(Context context) {
        mContext = context;
    }

    private Context mContext;

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

    /**
     * Truncate the amount to the currency precision setting.
     * @return Amount truncated to the currency precision.
     */
    public Money truncateToCurrency(Money amount, Currency currency) {
        int scale = currency.getScale();
        int precision = getNumberOfDecimals(scale);

        return amount.truncate(precision);
    }

    /**
     * Extracts the number of decimal places from scale/precision value.
     * @param scale Scale, usually from the currency entity.
     * @return Number of decimals to use (precision?).
     */
    public int getNumberOfDecimals(int scale) {
        double decimals = Math.log(scale) / Math.log(10.0);
        int result = (int) Math.round(decimals);
        return result;
    }

    public String removeBlanks(String input) {
        return input.replace(" ", "");
    }

    /**
     * Clean up the number based on the locale preferences for grouping and decimal separators.
     * @param numberString Formatted string
     * @return (English) number string that can be used for expression.
     */
    public String cleanUpNumberString(String numberString) {
        // replace any blanks
        numberString = removeBlanks(numberString);

        FormatUtilities format = new FormatUtilities(mContext);

        // Remove grouping separator(s)
        String groupingSeparator = format.getGroupingSeparatorForAppLocale();
        numberString = numberString.replace(groupingSeparator, "");

        // Replace the decimal separator with a dot.
        String decimalSeparator = format.getDecimalSeparatorForAppLocale();
        if (!decimalSeparator.equals(".")) {
            numberString = numberString.replace(decimalSeparator, ".");
        }

        return numberString;
    }

    public CurrencyService getCurrencyService() {
        if (mCurrencyService == null) {
            mCurrencyService = new CurrencyService(mContext);
        }
        return mCurrencyService;
    }
}
