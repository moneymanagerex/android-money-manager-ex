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
package com.money.manager.ex.investment;

import android.text.TextUtils;
import android.util.Log;

/**
 * Updates security prices from Yahoo Finance.
 */
public class YahooSecurityPriceUpdater
        implements ISecurityPriceUpdater, IDownloadAsyncTaskFeedback {

    private final String LOGCAT = this.getClass().getSimpleName();

    private String mUrlPrefix = "http://download.finance.yahoo.com/d/quotes.csv?s=";
    private String mUrlSuffix = "&f=l1&e=.csv";

    public void updatePrices() {
        // todo: implementation

        // iterate through list

        // download one by one.
//        foreach
//        String symbol = "";
//        updatePrice(symbol);
    }

    @Override
    public void updatePrice(String symbol) {
        // validation
        if (TextUtils.isEmpty(symbol)) {
            Log.w(LOGCAT, "updatePrice called with an empty symbol.");
            return;
        }

        // download individual price.
        String url = getPriceUrl(symbol);
//        new DownloadCsvTask().execute(url);
        new DownloadCsvToStringTask(this).execute(url);

        // Async call. The prices are updated in onCsvDownloaded.
    }

    private String getPriceUrl(String symbol) {
        StringBuilder builder = new StringBuilder();

        builder.append(mUrlPrefix);
        builder.append(symbol);
        builder.append(mUrlSuffix);

        return builder.toString();
    }

    /**
     * Called from CSV downloader on progress update.
     * @param progress
     */
    @Override
    public void onProgressUpdate(String progress) {
        // progress is a number, percentage probably.
        Log.d(LOGCAT, progress);
    }

    /**
     * Called from the CSV downloader when the file is downloaded and the contents read.
     * @param contents
     */
    @Override
    public void onCsvDownloaded(String contents) {
        Log.d(LOGCAT, contents);

        // todo: update the price in database.

        // todo: save price history record.

    }
}
