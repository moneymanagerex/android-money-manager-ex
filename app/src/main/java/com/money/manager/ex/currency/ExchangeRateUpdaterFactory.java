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

package com.money.manager.ex.currency;

import android.content.Context;

import com.money.manager.ex.investment.ExchangeRateProviders;
import com.money.manager.ex.investment.prices.IExchangeRateUpdater;
import com.money.manager.ex.investment.prices.FreeCurrencyExchangeRateAPIService;

import com.money.manager.ex.settings.InvestmentSettings;

import java.util.Objects;

/**
 * Factory for exchange rate updater.
 * Set here when changing the updater.
 */
public class ExchangeRateUpdaterFactory {

    public static IExchangeRateUpdater getUpdaterInstance(Context context) {
        IExchangeRateUpdater updater;

        // check preferences to see which downloader to use.
        InvestmentSettings settings = new InvestmentSettings(context);
        ExchangeRateProviders provider = settings.getExchangeRateProvider();

        //            case Morningstar:
        //                updater = new MorningstarPriceUpdater(context);
        //                break;
        if (Objects.requireNonNull(provider) == ExchangeRateProviders.Fixer) {
            updater = new FreeCurrencyExchangeRateAPIService(context);
            //            case YahooYql:
//                updater = new YqlSecurityPriceUpdaterRetrofit(context);
//                break;
//            case YahooCsv:
//                updater = new YahooCsvQuoteDownloaderRetrofit(context);
//                break;
        } else {
            updater = new FreeCurrencyExchangeRateAPIService(context);
        }

        return updater;
    }
}
