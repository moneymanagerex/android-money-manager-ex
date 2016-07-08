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

package com.money.manager.ex.investment.morningstar;

import android.content.Context;

import com.money.manager.ex.R;
import com.money.manager.ex.core.ExceptionHandler;
import com.money.manager.ex.investment.ISecurityPriceUpdater;
import com.money.manager.ex.investment.PriceUpdaterBase;
import com.money.manager.ex.investment.SecurityPriceUpdaterFactory;
import com.money.manager.ex.investment.events.AllPricesDownloadedEvent;
import com.money.manager.ex.investment.events.PriceDownloadedEvent;

import org.greenrobot.eventbus.EventBus;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.List;

import info.javaperformance.money.Money;
import info.javaperformance.money.MoneyFactory;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Quote provider: Morningstar
 */
public class MorningstarPriceUpdater
    extends PriceUpdaterBase
    implements ISecurityPriceUpdater {

    public MorningstarPriceUpdater(Context context) {
        super(context);
    }

    /**
     * Tracks the number of records to update. Used to close progress dialog when all done.
     */
    private int mCounter;
    private int mTotalRecords;

    @Override
    public void downloadPrices(List<String> symbols) {
        if (symbols == null || symbols.isEmpty()) return;
        mTotalRecords = symbols.size();
        if (mTotalRecords == 0) return;

        showProgressDialog(mTotalRecords);

        IMorningstarService service = SecurityPriceUpdaterFactory.getMorningstarService();

        Callback<String> callback = new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                String symbol = call.request().url().queryParameter("t");
                onContentDownloaded(symbol, response.body());
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                closeProgressDialog();

                ExceptionHandler handler = new ExceptionHandler(getContext(), this);
                handler.handle(t, "fetching price");
            }
        };

        SymbolConverter converter = new SymbolConverter();

        for (String symbol : symbols) {
            try {
                String morningstarSymbol = converter.convert(symbol);

                service.getPrice(morningstarSymbol).enqueue(callback);
            } catch (Exception ex) {
                closeProgressDialog();

                ExceptionHandler handler = new ExceptionHandler(getContext());
                handler.handle(ex, "downloading quotes");
            }
        }
    }

    private void onContentDownloaded(String symbol, String content) {
        mCounter++;
        setProgress(mCounter);

        if (content == null) {
            ExceptionHandler handler = new ExceptionHandler(getContext(), this);
            handler.showMessage(getContext().getString(R.string.error_updating_rates));
//            closeProgressDialog();
            return;
        }

        // Parse results
        PriceDownloadedEvent event = null;
        try {
            event = parse(symbol, content);
        } catch (Exception e) {
            ExceptionHandler handler = new ExceptionHandler(getContext());
            handler.handle(e, "parsing downloaded data");
        }

        // Notify the caller by invoking the interface method.
        if (event != null) {
            EventBus.getDefault().post(event);
        }

        finishIfAllDone();
    }

    private synchronized void finishIfAllDone() {
        if (mCounter != mTotalRecords) return;

        closeProgressDialog();

        // Notify user that all the prices have been downloaded.
        ExceptionHandler handler = new ExceptionHandler(getContext(), this);
        handler.showMessage(getContext().getString(R.string.download_complete));

        // fire an event so that the data can be reloaded.
        EventBus.getDefault().post(new AllPricesDownloadedEvent());
    }

    private PriceDownloadedEvent parse(String symbol, String html) {
        Document doc = Jsoup.parse(html);

        // symbol
        String yahooSymbol = new SymbolConverter().getYahooSymbol(symbol);

        // price
        String priceString = doc.body().getElementById("last-price-value").text();
        Money price = MoneyFactory.fromString(priceString);

        // date
        String dateString = doc.body().getElementById("asOfDate").text();
        DateTimeFormatter formatter = DateTimeFormat.forPattern("MM/dd/YYYY HH:mm:ss");
        // the time zone is EST
        DateTime date = formatter.withZone(DateTimeZone.forID("America/New_York"))
                .parseDateTime(dateString)
                .withZone(DateTimeZone.forID("Europe/Vienna"));

        // todo: should this be converted to the exchange time?

        return new PriceDownloadedEvent(yahooSymbol, price, date);
    }
}
