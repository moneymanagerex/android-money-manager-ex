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
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.money.manager.ex.core;

import android.content.Context;
import android.util.Log;
import android.widget.TextView;

import com.money.manager.ex.R;
import com.money.manager.ex.currency.CurrencyService;
import com.money.manager.ex.database.TableCurrencyFormats;
import com.money.manager.ex.settings.AppSettings;

import org.apache.commons.lang3.StringUtils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import info.javaperformance.money.Money;

/**
 * Utilities to assist with formatting dates and numbers.
 * Created by Alen on 26/09/2015.
 */
public class FormatUtilities {

    private static final String LOGCAT = FormatUtilities.class.getSimpleName();

    /**
     * Method, which formats the default currency amount in TextView
     *
     * @param view   TextView to set the amount
     * @param amount to be formatted
     */
    public static void formatAmountTextView(Context context, TextView view, Money amount) {
        formatAmountTextView(context, view, amount, null);
    }

    /**
     * Method, which formats the default currency amount in TextView
     *
     * @param view       TextView to set the amount
     * @param amount     to be formatted
     * @param currencyId Id currency to be formatted
     */
    public static void formatAmountTextView(Context context, TextView view, Money amount,
                                            Integer currencyId) {
        if (amount == null) {
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
        this.settings = new AppSettings(context);
    }

    private Context context;
    private NumericHelper numericHelper;
    private AppSettings settings;

    public String formatWithLocale(Money amount) {
        // Use the number of decimals from the base currency.
        int decimals = this.getNumberOfDecimalsForBaseCurrency();
        // separators from the locale.
        String decimalSeparator = getDecimalSeparatorForAppLocale();
        String groupSeparator = getGroupingSeparatorForAppLocale();

        return this.numericHelper.getNumberFormatted(amount, decimals, decimalSeparator, groupSeparator);
    }

    public int getNumberOfDecimalsForBaseCurrency() {
        CurrencyService service = new CurrencyService(context);
        TableCurrencyFormats baseCurrency = service.getBaseCurrency();
        if (baseCurrency == null) {
            ExceptionHandler handler = new ExceptionHandler(this.context, this);
            handler.showMessage(context.getString(R.string.base_currency_not_set));
            // arbitrary setting
            return 2;
        }

        double scale = baseCurrency.getScale();
        int decimals = this.numericHelper.getNumberOfDecimals(scale);
        return decimals;
    }

    public String getDecimalSeparatorForAppLocale() {
        Locale locale = getAppLocale();

        DecimalFormat currencyFormatter = (DecimalFormat) NumberFormat.getInstance(locale);
        char decimalSeparator = currencyFormatter.getDecimalFormatSymbols().getDecimalSeparator();

        String separator = Character.toString(decimalSeparator);

        return separator;
    }

    public String getGroupingSeparatorForAppLocale() {
        Locale locale = getAppLocale();

        DecimalFormat currencyFormatter = (DecimalFormat) NumberFormat.getInstance(locale);
        char groupingSeparator = currencyFormatter.getDecimalFormatSymbols().getGroupingSeparator();

        String separator = Character.toString(groupingSeparator);

        return separator;
    }

    public Locale getAppLocale() {
        Locale locale = null;

        String language = this.settings.getGeneralSettings().getApplicationLanguage();

        if(StringUtils.isNotEmpty(language)) {
            try {
                locale = Locale.forLanguageTag(language);
            } catch (Exception e) {
                ExceptionHandler handler = new ExceptionHandler(this.context, this);
                handler.handle(e, "parsing locale: " + language);
            }
        }

        // in case the above failed
        if (locale == null) {
            // use the default locale.
            locale = this.context.getResources().getConfiguration().locale;
        }

        return locale;
    }
}
