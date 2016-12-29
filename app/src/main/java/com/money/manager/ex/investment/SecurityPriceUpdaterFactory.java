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

package com.money.manager.ex.investment;

import android.content.Context;

import com.money.manager.ex.investment.morningstar.IMorningstarService;
import com.money.manager.ex.investment.morningstar.MorningstarPriceUpdater;
import com.money.manager.ex.investment.yahoocsv.IYahooCsvService;
import com.money.manager.ex.investment.yahoocsv.YahooCsvQuoteDownloaderRetrofit;
import com.money.manager.ex.investment.yql.IYqlService;
import com.money.manager.ex.investment.yql.YqlSecurityPriceUpdaterRetrofit;
import com.money.manager.ex.settings.InvestmentSettings;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

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
                //updater = new YqlSecurityPriceUpdater(context, feedback);
                updater = new YqlSecurityPriceUpdaterRetrofit(context);
                break;
            case YahooCsv:
//                updater = new YahooCsvSecurityPriceUpdater(context);
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
