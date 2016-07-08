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

package com.money.manager.ex.investment.yahoocsv;

import android.content.Context;

import com.money.manager.ex.R;
import com.money.manager.ex.core.ExceptionHandler;
import com.money.manager.ex.investment.ISecurityPriceUpdater;
import com.money.manager.ex.investment.PriceCsvParser;
import com.money.manager.ex.investment.PriceUpdaterBase;
import com.money.manager.ex.investment.SecurityPriceUpdaterFactory;
import com.money.manager.ex.investment.events.AllPricesDownloadedEvent;
import com.money.manager.ex.investment.events.PriceDownloadedEvent;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Implementation of the Yahoo CSV quote provider using Retrofit.
 */
public class YahooCsvQuoteDownloaderRetrofit
    extends PriceUpdaterBase
    implements ISecurityPriceUpdater {

    /**
     * Tracks the number of records to update. Used to close progress dialog when all done.
     */
    private int mCounter;
    private int mTotalRecords;

    public YahooCsvQuoteDownloaderRetrofit(Context context) {
        super(context);
    }

    @Override
    public void downloadPrices(List<String> symbols) {
        if (symbols == null || symbols.isEmpty()) return;
        mTotalRecords = symbols.size();
        if (mTotalRecords == 0) return;

        showProgressDialog(mTotalRecords);

        IYahooCsvService service = SecurityPriceUpdaterFactory.getYahooCsvService();

        Callback<String> callback = new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                onContentDownloaded(response.body());
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                closeProgressDialog();

                ExceptionHandler handler = new ExceptionHandler(getContext(), this);
                handler.handle(t, "fetching price");
            }
        };

        for (String symbol : symbols) {
            try {
                service.getPrice(symbol).enqueue(callback);
            } catch (Exception ex) {
                ExceptionHandler handler = new ExceptionHandler(getContext());
                handler.handle(ex, "downloading quotes");
            }
        }
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

    private void onContentDownloaded(String content) {
        mCounter++;
        setProgress(mCounter);

        if (content == null) {
            ExceptionHandler handler = new ExceptionHandler(getContext(), this);
            handler.showMessage(getContext().getString(R.string.error_updating_rates));
//            closeProgressDialog();
            return;
        }

        PriceCsvParser parser = new PriceCsvParser(getContext());
        PriceDownloadedEvent event = parser.parse(content);

        // Notify the caller by invoking the interface method.
        if (event != null) {
            EventBus.getDefault().post(event);
        }

        finishIfAllDone();
    }

}
