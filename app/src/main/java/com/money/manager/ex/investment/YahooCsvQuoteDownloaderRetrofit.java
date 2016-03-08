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

import com.money.manager.ex.R;
import com.money.manager.ex.core.ExceptionHandler;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Implementation of the Yahoo CSV quote provider using Retrofit.
 */
public class YahooCsvQuoteDownloaderRetrofit
    implements ISecurityPriceUpdater {

    private Context mContext;
    private IYahooCsvService yahooCsvService;

    public YahooCsvQuoteDownloaderRetrofit(Context context) {
        this.mContext = context;
    }

    @Override
    public void downloadPrices(List<String> symbols) {
        if (symbols == null) return;

        IYahooCsvService service = getService();

        Callback<String> callback = new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                onContentDownloaded(response.body());
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                ExceptionHandler handler = new ExceptionHandler(mContext, this);
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

    private Context getContext() {
        return mContext;
    }

    private IYahooCsvService getService() {
        if (this.yahooCsvService == null) {
            this.yahooCsvService = YahooCsvService.getService();
        }
        return this.yahooCsvService;
    }

    private void onContentDownloaded(String content) {
        ExceptionHandler handler = new ExceptionHandler(getContext(), this);

        if (content == null) {
            handler.showMessage(getContext().getString(R.string.error_updating_rates));
            return;
        }

        getPriceFrom(content);

        // Notify user that all the prices have been downloaded.
        handler.showMessage(mContext.getString(R.string.download_complete));
    }

    private void getPriceFrom(String content){
        PriceCsvParser parser = new PriceCsvParser(getContext());
        parser.parse(content);
    }
}
