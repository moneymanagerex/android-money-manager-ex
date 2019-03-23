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

package com.money.manager.ex.investment;

import android.content.Context;

import com.money.manager.ex.investment.morningstar.MorningstarPriceUpdater;
import com.money.manager.ex.investment.prices.ISecurityPriceUpdater;
import com.money.manager.ex.investment.yahoocsv.YahooCsvQuoteDownloaderRetrofit;
import com.money.manager.ex.investment.yql.YqlSecurityPriceUpdaterRetrofit;
import com.money.manager.ex.settings.InvestmentSettings;

/**
 * Factory for security price updater.
 * Set here when changing the updater.
 */
public class SecurityPriceUpdaterFactory {
    public static ISecurityPriceUpdater getUpdaterInstance(Context context) {
        ISecurityPriceUpdater updater;

        // check preferences to see which downloader to use.
        InvestmentSettings settings = new InvestmentSettings(context);
        QuoteProviders provider = settings.getQuoteProvider();

        switch (provider) {
            case Morningstar:
                updater = new MorningstarPriceUpdater(context);
                break;
            case YahooYql:
                updater = new YqlSecurityPriceUpdaterRetrofit(context);
                break;
            case YahooCsv:
                updater = new YahooCsvQuoteDownloaderRetrofit(context);
                break;
            default:
                // yql
                updater = new YqlSecurityPriceUpdaterRetrofit(context);
                break;
        }

        return updater;
    }
}
