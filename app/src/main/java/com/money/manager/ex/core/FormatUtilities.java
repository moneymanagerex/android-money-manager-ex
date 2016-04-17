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
package com.money.manager.ex.core;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import com.money.manager.ex.Constants;
import com.money.manager.ex.MoneyManagerApplication;
import com.money.manager.ex.R;
import com.money.manager.ex.currency.CurrencyRepository;
import com.money.manager.ex.currency.CurrencyService;
import com.money.manager.ex.domainmodel.Currency;

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
     * Compatibility wrapper only.
     */
    public static void formatAmountTextView(Context context, TextView view, Money amount,
                                            Integer currencyId) {
        FormatUtilities formatter = new FormatUtilities(context);
        formatter.formatAmountTextView(view, amount, currencyId);
    }

    public static String getIsoDateStringFrom(int year, int month, int day) {
        return String.format("%04d-%02d-%02d", year, month, day);
    }

    public FormatUtilities(Context context) {
        this.context = context;
    }

    private Context context;

    /**
     * Formats the amount in TextView with the given currency settings.
     *
     * @param view       TextView to set the amount
     * @param amount     to be formatted
     * @param currencyId Id currency to be formatted
     */
    public void formatAmountTextView(TextView view, Money amount,
                                     Integer currencyId) {
        if (amount == null) {
            ExceptionHandler handler = new ExceptionHandler(getContext(), null);
            handler.showMessage("Amount for formatting is null.");
            Log.w(LOGCAT, "Amount for formatting is null.");
            return;
        }

        CurrencyService currencyService = new CurrencyService(getContext());

        if (currencyId == null) {
            view.setText(currencyService.getBaseCurrencyFormatted(amount));
        } else {
            view.setText(currencyService.getCurrencyFormatted(currencyId, amount));
        }

        view.setTag(amount);
    }

    /**
     * Uses the number of decimals from the base currency, separators from the app locale.
     *
     * @param amount Amount to be formatted.
     * @return  String representation of the formatted number.
     */
    public String formatWithLocale(Money amount) {
        // Use the number of decimals from the base currency.
        int scale = this.getScaleForBaseCurrency();


        // separators from the locale.
        String decimalSeparator = getDecimalSeparatorForAppLocale();
        String groupSeparator = getGroupingSeparatorForAppLocale();

        return getValueFormatted(amount, scale, decimalSeparator, groupSeparator);
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
     * Formats the amount with the currency scale, decimal & group separators, prefix and suffix.
     *
     * @param value value to format
     * @param showSymbols Whether to include the currency symbol in the output.
     * @return formatted value
     */
    public String getValueFormatted(Money value, boolean showSymbols, Currency currency) {
        String result = this.getValueFormatted(value, currency.getScale(),
            currency.getDecimalSeparator(), currency.getGroupSeparator());

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

    public String getValueFormatted(Money value, int currencyId) {
        CurrencyService currencyService = new CurrencyService(getContext());
        Currency currency = currencyService.getCurrency(currencyId);
        
        return getValueFormatted(value, true, currency);
    }

    public String getValueFormatted(Money value, int scale, String decimalSeparator, String groupSeparator) {
        int decimals = new NumericHelper(getContext()).getNumberOfDecimals(scale);

        value = value.truncate(decimals);

        // set format
        DecimalFormatSymbols formatSymbols = new DecimalFormatSymbols();
        // getDecimalSeparator()
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

        formatter.setMaximumFractionDigits(decimals);
        formatter.setMinimumFractionDigits(decimals);

        formatter.setGroupingSize(3);
        formatter.setDecimalFormatSymbols(formatSymbols);

        String result = formatter.format(value.toDouble());
        return result;
    }

    /**
     * Ultimately, all the methods should converge to this one. Provides customization options for
     * the amount.
     *
     * @param amount Amount to be formatted
     * @param decimals Number of decimals to use.
     * @param decimalSeparator Decimal separator character.
     * @param groupSeparator Group separator character.
     * @param prefix the prefix to attach.
     * @param suffix the suffix to attach.
     * @return Number formatted.
     */
    public String formatNumber(Money amount, int decimals, String decimalSeparator, String groupSeparator,
                               String prefix, String suffix) {
        // Decimals

        String pattern = NumericPatternGenerator.getPattern(decimals);
        DecimalFormat formatter = new DecimalFormat(pattern);
        formatter.setMaximumFractionDigits(decimals);
        formatter.setMinimumFractionDigits(decimals);

        // Separators

        DecimalFormatSymbols formatSymbols = new DecimalFormatSymbols();
        if (!(TextUtils.isEmpty(decimalSeparator))) {
            formatSymbols.setDecimalSeparator(decimalSeparator.charAt(0));
        }
        if (!(TextUtils.isEmpty(groupSeparator))) {
            formatSymbols.setGroupingSeparator(groupSeparator.charAt(0));
        }
        // Group size
        formatter.setGroupingSize(3);

        formatter.setDecimalFormatSymbols(formatSymbols);

        String result = formatter.format(amount.toDouble());

        // Currency symbol, prefix/suffix

        if (prefix != null) {
            result = prefix + result;
        }
        if (suffix != null) {
            result += suffix;
        }

        return result;
    }

    /**
     * Formats the number by ignoring the decimal count.
     * Group & decimal symbols and currency prefix/suffix are used.
     *
     * @param amount
     * @param currencyId
     * @return
     */
    public String formatNumberIgnoreDecimalCount(Money amount, int currencyId) {
        if (currencyId == Constants.NOT_SET) {
            // use locale values?
            return formatWithLocale(amount);
        }

        // number of decimals - do not modify
        int scale = Constants.DEFAULT_PRECISION;

        CurrencyRepository repo = new CurrencyRepository(getContext());
        Currency currency = repo.loadCurrency(currencyId);

        // group & decimal symbols
        // currency symbol
        return this.formatNumber(amount, scale, currency.getDecimalSeparator(),
            currency.getGroupSeparator(), currency.getPfxSymbol(), currency.getSfxSymbol());
    }

    public String getValueFormattedInBaseCurrency(Money value) {
        CurrencyService service = new CurrencyService(getContext());
        return getValueFormatted(value, service.getBaseCurrency());
    }

}
