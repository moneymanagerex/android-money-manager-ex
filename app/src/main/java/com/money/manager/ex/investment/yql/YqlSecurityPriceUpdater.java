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
package com.money.manager.ex.investment.yql;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import com.money.manager.ex.R;
import com.money.manager.ex.log.ExceptionHandler;
import com.money.manager.ex.core.NumericHelper;
import com.money.manager.ex.investment.IDownloadAsyncTaskFeedback;
import com.money.manager.ex.investment.ISecurityPriceUpdater;
import com.money.manager.ex.investment.SecurityPriceModel;
import com.money.manager.ex.investment.TextDownloaderTask;
import com.money.manager.ex.investment.events.PriceDownloadedEvent;

import org.greenrobot.eventbus.EventBus;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import info.javaperformance.money.Money;
import info.javaperformance.money.MoneyFactory;

/**
 * Updates security prices from Yahoo Finance using YQL.
 * The background task downloads the results.
 * References:
 * http://www.jarloo.com/yahoo_finance/
 */
public class YqlSecurityPriceUpdater
        implements ISecurityPriceUpdater, IDownloadAsyncTaskFeedback {

    public YqlSecurityPriceUpdater(Context context) {
        mContext = context;
    }

    public String response;

    private Context mContext;
    //
    private final String mBaseUri = "https://query.yahooapis.com/v1/public/yql";
    // https://query.yahooapis.com/v1/public/yql
    // ?q=... url escaped
    // &format=json
    // &diagnostics=true
    // &env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys
    // &callback=

    /**
     * Update prices for all the symbols in the list.
     */
    public void downloadPrices(List<String> symbols) {
        if (symbols == null) return;

        // Get varargs from list.
        //String[] symbolsArray = symbols.toArray(new String[symbols.size()]);
        String url = getPriceUrl(symbols);

        // Download prices
        TextDownloaderTask downloader = new TextDownloaderTask(mContext, this);
        downloader.execute(url);

        // todo: replace with Retrofit

        // Async call. The prices are updated in onContentDownloaded.
    }

    @Override
    public String getUrlForSymbol(String symbol) {
        String result = getPriceUrl(Collections.singletonList(symbol));
        return result;
    }

    /**
     * Called from CSV downloader on progress update.
     * @param progress progress
     */
    @Override
    public void onProgressUpdate(String progress) {
        // progress is a number, percentage probably.
//        Log.d(LOGCAT, progress);
    }

    /**
     * Called from the Text Downloader when the file is downloaded and the contents read.
     * Here we have all the prices.
     * @param content The content received from the given url.
     */
    @Override
    public void onContentDownloaded(String content) {
        // validation
        if (TextUtils.isEmpty(content)) {
            throw new IllegalArgumentException("Downloadeded contents are empty");
        }

        // parse Json results
        List<SecurityPriceModel> pricesList = new ArrayList<>();
        try {
            pricesList = parseDownloadedJson(content);
        } catch (JSONException e) {
            ExceptionHandler handler = new ExceptionHandler(mContext, this);
            handler.e(e, "parsing JSON");
        }

        for (SecurityPriceModel model : pricesList) {
            // Notify the interested parties via event bus.
            EventBus.getDefault().post(new PriceDownloadedEvent(model.symbol, model.price, model.date));
        }
    }

    private String getPriceUrl(List<String> symbols) {
        YqlQueryGenerator queryGenerator = new YqlQueryGenerator();
        String query = queryGenerator.getQueryFor(symbols);

        String uri = Uri.parse(mBaseUri)
                .buildUpon()
                .appendQueryParameter("q", query)
                .appendQueryParameter("format", "json")
                .appendQueryParameter("env", "store://datatables.org/alltableswithkeys")
                .build()
                .toString();

        return uri;
    }

    private List<SecurityPriceModel> parseDownloadedJson(String content) throws JSONException {
        ArrayList<SecurityPriceModel> result = new ArrayList<>();

        JSONObject root = new JSONObject(content);

        // check whether there is only one item or more
        Object quoteObject = root.getJSONObject("query").getJSONObject("results").get("quote");
        if (quoteObject instanceof JSONArray) {
            JSONArray quotes = root
                    .getJSONObject("query")
                    .getJSONObject("results")
                    .getJSONArray("quote");

            for (int i = 0; i < quotes.length(); i++) {
                JSONObject quote = quotes.optJSONObject(i);
                // process individual quote
                SecurityPriceModel priceModel = getSecurityPriceFor(quote);
                if (priceModel == null) continue;

                result.add(priceModel);
            }
        } else {
            // Single quote
            JSONObject quote = root
                    .getJSONObject("query")
                    .getJSONObject("results")
                    .getJSONObject("quote");

            SecurityPriceModel priceModel = getSecurityPriceFor(quote);
            if (priceModel != null) {
                result.add(priceModel);
            }
        }

        return result;
    }

    private SecurityPriceModel getSecurityPriceFor(JSONObject quote) throws JSONException {

        SecurityPriceModel priceModel = new SecurityPriceModel();
        priceModel.symbol = quote.getString("symbol");

        ExceptionHandler handler = new ExceptionHandler(mContext, this);

        // price
        String priceString = quote.getString("LastTradePriceOnly");
        if (!NumericHelper.isNumeric(priceString)) {
            handler.showMessage(mContext.getString(R.string.error_downloading_symbol) + " " +
                    priceModel.symbol);
            return null;
        }

        Money price = MoneyFactory.fromString(priceString);
        // LSE stocks are expressed in GBp (pence), not Pounds.
        // From stockspanel.cpp, line 785: if (StockQuoteCurrency == "GBp") dPrice = dPrice / 100;
        String currency = quote.getString("Currency");
        if (currency.equals("GBp")) {
            price = price.divide(100, MoneyFactory.MAX_ALLOWED_PRECISION);
        }
        priceModel.price = price;

        // date
        DateTimeFormatter format = DateTimeFormat.forPattern("MM/dd/yyyy");
        priceModel.date = format.parseDateTime(quote.getString("LastTradeDate"));

        return priceModel;
    }
}
