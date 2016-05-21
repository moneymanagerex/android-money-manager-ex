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

import com.money.manager.ex.investment.events.PriceDownloadedEvent;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import info.javaperformance.money.Money;
import info.javaperformance.money.MoneyFactory;

/**
 * Updates security prices from Yahoo Finance.
 * References:
 * http://www.jarloo.com/yahoo_finance/
 */
public class YahooCsvSecurityPriceUpdater
    implements ISecurityPriceUpdater, IDownloadAsyncTaskFeedback {

    public YahooCsvSecurityPriceUpdater(Context context) {
        mContext = context;
    }

    private Context mContext;
    // "http://download.finance.yahoo.com/d/quotes.csv?s=", A2, "&f=l1d1&e=.csv"
    private String mUrlPrefix = "http://download.finance.yahoo.com/d/quotes.csv?s=";
    // get symbol, last trade price, last trade date
    private String mUrlSuffix = "&f=sl1d1c4&e=.csv";
    // "&f=l1&e=.csv";
    // c4 = currency

    /**
     * Update prices for all the symbols in the list.
     */
    public void downloadPrices(List<String> symbols) {
        if (symbols == null) return;

        String[] symbolsArray = symbols.toArray(new String[symbols.size()]);

        YahooDownloadAllPricesTask downloader = new YahooDownloadAllPricesTask(getContext(), this);
        downloader.execute(symbolsArray);

        // Async call. The prices are updated in onContentDownloaded.
    }

    @Override
    public String getUrlForSymbol(String symbol) {
        String result = getPriceUrl(symbol);
        return result;
    }

    /**
     * Called from CSV downloader on progress update.
     * @param progress
     */
    @Override
    public void onProgressUpdate(String progress) {
        // progress is a number, percentage probably.
//        Log.d(LOGCAT, progress);
    }

    /**
     * Called from the CSV downloader when the file is downloaded and the contents read.
     * @param content retrieved price
     */
    @Override
    public void onContentDownloaded(String content) {
        PriceCsvParser parser = new PriceCsvParser(getContext());
        PriceDownloadedEvent event = parser.parse(content);

        // Notify the caller by invoking the interface method.
        EventBus.getDefault().post(event);
    }

    private Context getContext() {
        return mContext;
    }

    private String getPriceUrl(String symbol) {
        StringBuilder builder = new StringBuilder();

        builder.append(mUrlPrefix);
        builder.append(symbol);
        builder.append(mUrlSuffix);

        return builder.toString();
    }

}
