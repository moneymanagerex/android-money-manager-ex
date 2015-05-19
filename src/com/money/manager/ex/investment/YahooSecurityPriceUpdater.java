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

import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import au.com.bytecode.opencsv.CSVParser;

/**
 * Updates security prices from Yahoo Finance.
 * References:
 * http://www.jarloo.com/yahoo_finance/
 */
public class YahooSecurityPriceUpdater
        implements ISecurityPriceUpdater, IDownloadAsyncTaskFeedback {

    public YahooSecurityPriceUpdater(IPriceUpdaterFeedback feedback) {
        mFeedback = feedback;
    }

    private final String LOGCAT = this.getClass().getSimpleName();

    // "http://download.finance.yahoo.com/d/quotes.csv?s=", A2, "&f=l1d1&e=.csv"
    private String mUrlPrefix = "http://download.finance.yahoo.com/d/quotes.csv?s=";
    // get symbol, last trade price, last trade date
    private String mUrlSuffix = "&f=sl1d1&e=.csv";
    // "&f=l1&e=.csv";
    private IPriceUpdaterFeedback mFeedback;

    // All the symbols to be updated.
    private String[] mSymbolsToUpdate;
    // The update progress counter.
    private int mFetchedCount;

    /**
     * Update prices for all the symbols in the list.
     */
    public void updatePrices(String... symbols) {
        if (symbols == null) return;

        mSymbolsToUpdate = symbols;
        mFetchedCount = 0;

        YahooDownloadAllPricesTask downloader = new YahooDownloadAllPricesTask(
                mFeedback.getContext(), this);
        downloader.execute(symbols);
    }

    @Override
    public void updatePrice(String symbol) {
        // validation
        if (TextUtils.isEmpty(symbol)) {
            Log.w(LOGCAT, "updatePrice called with an empty symbol.");
            return;
        }

        mSymbolsToUpdate = new String[] { symbol };
        mFetchedCount = 0;

        // download individual price.
        String url = getPriceUrl(symbol);
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
     * @param csvContents retrieved price
     */
    @Override
    public void onCsvDownloaded(String csvContents) {
        // validation
        if (TextUtils.isEmpty(csvContents)) {
            throw new IllegalArgumentException("Downloaded CSV contents are empty");
        }

        // parse CSV contents to get proper fields that can be saved to the database.
        CSVParser csvParser = new CSVParser();
        String[] values;
        try {
            values = csvParser.parseLineMulti(csvContents);
        } catch (IOException e) {
            Log.e(LOGCAT, "Error parsing downloaded CSV contents.");
            e.printStackTrace();
            return;
        }

        // convert csv values to their original type.

        String symbol = values[0];
        BigDecimal price = new BigDecimal(values[1]);
        // date
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        Date date = null;
        try {
            date = dateFormat.parse(values[2]);
        } catch (ParseException e) {
            Log.e(LOGCAT, "Error parsing date from CSV.");
            e.printStackTrace();
        }

//        mFetchedCount += 1;
//        // Notify the parent (to update any lists, etc.) once all the prices
//        if (mFetchedCount == mSymbolsToUpdate.length) {
        mFeedback.onPriceDownloaded(symbol, price, date);
//        }
    }
}
