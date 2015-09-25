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
package com.money.manager.ex.investment;

import android.content.Context;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.money.manager.ex.R;
import com.money.manager.ex.core.ExceptionHandler;
import com.money.manager.ex.core.NumericHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import info.javaperformance.money.Money;
import info.javaperformance.money.MoneyFactory;
import retrofit.Callback;
import retrofit.Response;

/**
 * Updates security prices from Yahoo Finance using YQL. Using Retrofit for network access.
 */
public class YqlSecurityPriceUpdaterRetrofit
        implements ISecurityPriceUpdater {

    /**
     *
     * @param context Executing context
     * @param feedback The object that will receive the notification after the prices are
     *                 loaded asynchronously.
     */
    public YqlSecurityPriceUpdaterRetrofit(Context context, IPriceUpdaterFeedback feedback) {
        mContext = context;
        mFeedback = feedback;
    }

    private Context mContext;
    private IPriceUpdaterFeedback mFeedback;

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
        if (symbols.size() == 0) return;

        YqlQueryGenerator queryGenerator = new YqlQueryGenerator();
        String query = queryGenerator.getQueryFor(symbols);

        IYqlService yql = YqlService.getService();

        // Async response handler.
        Callback<JsonElement> callback = new Callback<JsonElement>() {
            @Override
            public void onResponse(Response<JsonElement> response) {
                onContentDownloaded(response.body());
            }

            @Override
            public void onFailure(Throwable t) {
                ExceptionHandler handler = new ExceptionHandler(mContext, this);
                handler.handle(t, "fetching price");
            }
        };

        try {
            // This would be the synchronous call.
//            prices = yql.getPrices(query).execute().body();

            yql.getPrices(query).enqueue(callback);
        } catch (Exception e) {
            ExceptionHandler handler = new ExceptionHandler(mContext, this);
            handler.handle(e, "fetching prices");
        }
    }

    /**
     * Called when the file is downloaded and the contents read.
     * Here we have all the prices.
     */
    public void onContentDownloaded(JsonElement response) {
        // parse Json results
        List<SecurityPriceModel> pricesList = getPricesFromJson(response.getAsJsonObject());

        for (SecurityPriceModel model : pricesList) {
            // Notify the caller by invoking the interface method.
            mFeedback.onPriceDownloaded(model.symbol, model.price, model.date);
        }

        ExceptionHandler handler = new ExceptionHandler(mContext, this);
        handler.showMessage(mContext.getString(R.string.all_prices_updated));
    }

    private List<SecurityPriceModel> getPricesFromJson(JsonObject root) {
        ArrayList<SecurityPriceModel> result = new ArrayList<>();

        // check whether there is only one item or more
        JsonElement quoteElement = root.get("query").getAsJsonObject()
            .get("results").getAsJsonObject()
            .get("quote");
        if (quoteElement instanceof JsonArray) {
            JsonArray quotes = quoteElement.getAsJsonArray();

            for (int i = 0; i < quotes.size(); i++) {
                JsonObject quote = quotes.get(i).getAsJsonObject();
                // process individual quote
                SecurityPriceModel priceModel = getSecurityPriceFor(quote);
                if (priceModel == null) continue;

                result.add(priceModel);
            }
        } else {
            // Single quote
            JsonObject quote = quoteElement.getAsJsonObject();

            SecurityPriceModel priceModel = getSecurityPriceFor(quote);
            if (priceModel != null) {
                result.add(priceModel);
            }
        }

        return result;
    }

    private SecurityPriceModel getSecurityPriceFor(JsonObject quote) {
        SecurityPriceModel priceModel = new SecurityPriceModel();
        priceModel.symbol = quote.get("symbol").getAsString();

        ExceptionHandler handler = new ExceptionHandler(mContext, this);

        // price
        String priceString = quote.get("LastTradePriceOnly").getAsString();
        if (!NumericHelper.isNumeric(priceString)) {
            handler.showMessage(mContext.getString(R.string.error_downloading_symbol) + " " +
                    priceModel.symbol);
            return null;
        }

        Money price = MoneyFactory.fromString(priceString);
        // LSE stocks are expressed in GBp (pence), not Pounds.
        // From stockspanel.cpp, line 785: if (StockQuoteCurrency == "GBp") dPrice = dPrice / 100;
        String currency = quote.get("Currency").getAsString();
        if (currency.equals("GBp")) {
            price = price.divide(100, MoneyFactory.MAX_ALLOWED_PRECISION);
        }
        priceModel.price = price;

        // date
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        Date date = null;
        try {
            date = dateFormat.parse(quote.get("LastTradeDate").getAsString());
        } catch (ParseException e) {
            handler.handle(e, "parsing date from CSV");
        }
        priceModel.date = date;

        return priceModel;
    }
}
