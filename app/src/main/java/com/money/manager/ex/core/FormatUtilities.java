/*
 * Copyright (C) 2012-2018 The Android Money Manager Ex Project Team
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
import android.widget.TextView;

import com.money.manager.ex.Constants;
import com.money.manager.ex.MmexApplication;
import com.money.manager.ex.R;
import com.money.manager.ex.currency.CurrencyService;
import com.money.manager.ex.domainmodel.Currency;
import com.money.manager.ex.log.ExceptionHandler;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

import javax.inject.Inject;

import info.javaperformance.money.Money;
import timber.log.Timber;

/**
 * Utilities to assist with formatting dates and numbers.
 */
public class FormatUtilities {

    public FormatUtilities(Context context) {
        this.context = context;
        MmexApplication.getApp().iocComponent.inject(this);
    }

    @Inject
    public FormatUtilities(MmexApplication app) {
        this.context = app;
    }

    private Context context;
    @Inject CurrencyService currencyService;

    /**
     * Formats the amount in TextView with the given currency preferences.
     *
     * @param view       TextView to set the amount
     * @param amount     to be formatted
     * @param currencyId Id currency to be formatted
     */
    public void formatAmountTextView(TextView view, Money amount, Integer currencyId) {
        if (amount == null) {
            Timber.w("Amount for formatting is null.");
            return;
        }

        String displayText;

        CurrencyService currencyService = getCurrencyService();
        if (currencyId == null) {
            displayText = currencyService.getBaseCurrencyFormatted(amount);
        } else {
            displayText = currencyService.getCurrencyFormatted(currencyId, amount);
        }

        view.setText(displayText);
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

        return format(amount, scale, decimalSeparator, groupSeparator);
    }

    public Context getContext() {
        return this.context;
    }

    public int getScaleForBaseCurrency() {
        CurrencyService service = getCurrencyService();
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
        Locale locale = MmexApplication.getApp().getAppLocale();

        DecimalFormat currencyFormatter = (DecimalFormat) NumberFormat.getInstance(locale);
        char decimalSeparator = currencyFormatter.getDecimalFormatSymbols().getDecimalSeparator();

        String separator = Character.toString(decimalSeparator);

        return separator;
    }

    public String getGroupingSeparatorForAppLocale() {
        Locale locale = MmexApplication.getApp().getAppLocale();

        DecimalFormat currencyFormatter = (DecimalFormat) NumberFormat.getInstance(locale);
        char groupingSeparator = currencyFormatter.getDecimalFormatSymbols().getGroupingSeparator();

        String separator = Character.toString(groupingSeparator);

        return separator;
    }

    public String format(Money value, String numberFormat) {
//        DecimalFormat format = new DecimalFormat(numberFormat);
        DecimalFormat formatter = new DecimalFormat(numberFormat);
        return formatter.format(value.toDouble());
    }

    /**
     * Formats the amount with the currency scale, decimal & group separators, prefix and suffix.
     *
     * @param value value to format
     * @param showSymbols Whether to include the currency symbol in the output.
     * @return formatted value
     */
    public String format(Money value, boolean showSymbols, Currency currency) {
        if (currency == null) return "n/a";

        String result = this.format(value, currency.getScale(),
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
     * Formats the given value with the currency preferences.
     * @param value value to format
     * @return value formatted
     */
    public String format(Money value, Currency currency) {
        return format(value, true, currency);
    }

    public String format(Money value, int currencyId) {
        CurrencyService currencyService = getCurrencyService();
        Currency currency = currencyService.getCurrency(currencyId);
        
        return format(value, true, currency);
    }

    public String format(Money value, int scale, String decimalSeparator, String groupSeparator) {
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
//        Locale appLocale = MoneyManagerApplication.getInstance().getLocale();
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

        Currency currency = getCurrencyService().getCurrency(currencyId);

        // group & decimal symbols
        // currency symbol
        return this.formatNumber(amount, scale, currency.getDecimalSeparator(),
            currency.getGroupSeparator(), currency.getPfxSymbol(), currency.getSfxSymbol());
    }

    public CurrencyService getCurrencyService() {
        if (this.currencyService == null) {
            this.currencyService = new CurrencyService(getContext());
        }
        return this.currencyService;
    }

    public String getValueFormattedInBaseCurrency(Money value) {
        CurrencyService service = getCurrencyService();
        return format(value, service.getBaseCurrency());
    }

}
