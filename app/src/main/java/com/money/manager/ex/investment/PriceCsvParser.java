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
import android.text.TextUtils;

import com.money.manager.ex.core.ExceptionHandler;
import com.money.manager.ex.core.NumericHelper;
import com.money.manager.ex.investment.events.PriceDownloadedEvent;
import com.opencsv.CSVParser;

import org.greenrobot.eventbus.EventBus;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.IOException;

import info.javaperformance.money.Money;
import info.javaperformance.money.MoneyFactory;

/**
 * Parses CSV response into price information.
 */
public class PriceCsvParser {

    private Context mContext;

    public PriceCsvParser(Context context) {
        mContext = context;
    }

    public Context getContext() {
        return mContext;
    }

    /**
     * Parses CSV content and fires an PriceDownloadedEvent.
     * @param content CSV content to parse into price information.
     */
    public void parse(String content) {
        // validation
        if (TextUtils.isEmpty(content)) {
            throw new IllegalArgumentException("Downloaded CSV contents are empty");
        }

        // parse CSV contents to get proper fields that can be saved to the database.
        CSVParser csvParser = new CSVParser();
        String[] values;
        try {
            values = csvParser.parseLineMulti(content);
        } catch (IOException e) {
            ExceptionHandler handler = new ExceptionHandler(getContext(), this);
            handler.handle(e, "parsing downloaded CSV contents");
            return;
        }

        // convert csv values to their original type.

        String symbol = values[0];

        // price
        String priceString = values[1];
        if (!NumericHelper.isNumeric(priceString)) return;
        Money price = MoneyFactory.fromString(priceString);
        // LSE stocks are expressed in GBp (pence), not Pounds.
        // From stockspanel.cpp, line 785: if (StockQuoteCurrency == "GBp") dPrice = dPrice / 100;
        String currency = values[3];
        if (currency.equals("GBp")) {
            price = price.divide(100, MoneyFactory.MAX_ALLOWED_PRECISION);
        }

        // date
        DateTimeFormatter format = DateTimeFormat.forPattern("MM/dd/yyyy");
        DateTime date = format.parseDateTime(values[2]);

        // Notify the caller by invoking the interface method.
        EventBus.getDefault().post(new PriceDownloadedEvent(symbol, price, date));
    }
}
