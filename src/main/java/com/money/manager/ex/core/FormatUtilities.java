/*
 * Copyright (C) 2012-2015 The Android Money Manager Ex Project Team
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
import android.util.Log;
import android.widget.TextView;

import com.money.manager.ex.Constants;
import com.money.manager.ex.MoneyManagerApplication;
import com.money.manager.ex.R;
import com.money.manager.ex.currency.CurrencyService;
import com.money.manager.ex.database.TableCurrencyFormats;
import com.money.manager.ex.domainmodel.Currency;
import com.money.manager.ex.settings.AppSettings;

import org.apache.commons.lang3.StringUtils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

import info.javaperformance.money.Money;

/**
 * Utilities to assist with formatting dates and numbers.
 */
public class FormatUtilities {

    private static final String LOGCAT = FormatUtilities.class.getSimpleName();

    /**
     * Method, which formats the amount in TextView with the given currency settings.
     *
     * @param view       TextView to set the amount
     * @param amount     to be formatted
     * @param currencyId Id currency to be formatted
     */
    public static void formatAmountTextView(Context context, TextView view, Money amount,
                                            Integer currencyId) {
        if (amount == null) {
            ExceptionHandler handler = new ExceptionHandler(context, null);
            handler.showMessage("Amount for formatting is null.");
            Log.w(LOGCAT, "Amount for formatting is null.");
            return;
        }

        CurrencyService currencyService = new CurrencyService(context);

        if (currencyId == null) {
            view.setText(currencyService.getBaseCurrencyFormatted(amount));
        } else {
            view.setText(currencyService.getCurrencyFormatted(currencyId, amount));
        }

        view.setTag(amount);
    }

    public FormatUtilities(Context context) {
        this.context = context;
        this.numericHelper = new NumericHelper(context);
    }

    private Context context;
    private NumericHelper numericHelper;

    public String formatWithLocale(Money amount) {
        // Use the number of decimals from the base currency.
        int scale = this.getScaleForBaseCurrency();


        // separators from the locale.
        String decimalSeparator = getDecimalSeparatorForAppLocale();
        String groupSeparator = getGroupingSeparatorForAppLocale();

        return getNumberFormatted(amount, scale, decimalSeparator, groupSeparator);
    }

    public Context getContext() {
        return this.context;
    }

    public int getScaleForBaseCurrency() {
        CurrencyService service = new CurrencyService(context);
        Currency baseCurrency = service.getBaseCurrency();
        if (baseCurrency == null) {
            ExceptionHandler handler = new ExceptionHandler(this.context, this);
            handler.showMessage(context.getString(R.string.base_currency_not_set));

            return Constants.DEFAULT_PRECISION;
        }

        int scale = baseCurrency.getScale();
//        int decimals = this.numericHelper.getNumberOfDecimals(scale);
        return scale;
    }

    public String getDecimalSeparatorForAppLocale() {
        Locale locale = MoneyManagerApplication.getInstanceApp().getAppLocale();

        DecimalFormat currencyFormatter = (DecimalFormat) NumberFormat.getInstance(locale);
        char decimalSeparator = currencyFormatter.getDecimalFormatSymbols().getDecimalSeparator();

        String separator = Character.toString(decimalSeparator);

        return separator;
    }

    public String getGroupingSeparatorForAppLocale() {
        Locale locale = MoneyManagerApplication.getInstanceApp().getAppLocale();

        DecimalFormat currencyFormatter = (DecimalFormat) NumberFormat.getInstance(locale);
        char groupingSeparator = currencyFormatter.getDecimalFormatSymbols().getGroupingSeparator();

        String separator = Character.toString(groupingSeparator);

        return separator;
    }

    /**
     *
     * @param value value to format
     * @param showSymbols Whether to include the currency symbol in the output.
     * @return formatted value
     */
    public String getValueFormatted(Money value, boolean showSymbols, Currency currency) {
        String result = this.getNumberFormatted(value, currency.getScale(),
            currency.getDecimalPoint(), currency.getGroupSeparator());

        // check suffix
        if ((showSymbols) && (!TextUtils.isEmpty(currency.getSfxSymbol()))) {
            result = result + " " + currency.getSfxSymbol();
        }
        // check prefix
        if (((showSymbols) && !TextUtils.isEmpty(currency.getPfxSymbol()))) {
            result = currency.getPfxSymbol() + " " + result;
        }

        return result;
    }

    /**
     *
     * @param value value to format
     * @return value formatted
     */
    public String getValueFormatted(Money value, Currency currency) {
        return getValueFormatted(value, true, currency);
    }

    public String getNumberFormatted(Money value, int scale, String decimalSeparator, String groupSeparator) {
        int decimals = this.numericHelper.getNumberOfDecimals(scale);

        value = value.truncate(decimals);

        // set format
        DecimalFormatSymbols formatSymbols = new DecimalFormatSymbols();
        // getDecimalPoint()
        if (!(TextUtils.isEmpty(decimalSeparator))) {
            formatSymbols.setDecimalSeparator(decimalSeparator.charAt(0));
        }
        // getGroupSeparator()
        if (!(TextUtils.isEmpty(groupSeparator))) {
            formatSymbols.setGroupingSeparator(groupSeparator.charAt(0));
        }

        // All these use locale-dependent formatting.
//        DecimalFormat formatter = new DecimalFormat();
//        Locale appLocale = MoneyManagerApplication.getInstanceApp().getLocale();
//        DecimalFormat formatter = (DecimalFormat) NumberFormat.getNumberInstance(appLocale);
        String pattern = NumericPatternGenerator.getPattern(decimals);
        DecimalFormat formatter = new DecimalFormat(pattern);

        formatter.setGroupingSize(3);
        formatter.setDecimalFormatSymbols(formatSymbols);

        formatter.setMaximumFractionDigits(decimals);
        formatter.setMinimumFractionDigits(decimals);

        String result = formatter.format(value.toBigDecimal());
        return result;
    }

//    public String getNumberFormatted(Money value, double scale, String decimalPoint, String groupSeparator) {
//        // Round the number first.
//        int decimals = getNumberOfDecimals(scale);
//
//        return getNumberFormatted(value, decimals, decimalPoint, groupSeparator);
//    }
}
