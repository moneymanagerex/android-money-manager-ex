/*
 * Copyright (C) 2024 The Android Money Manager Ex Project Team
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

package com.money.manager.ex.investment.prices;

import android.content.Context;
import android.widget.Toast;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.money.manager.ex.R;
import com.money.manager.ex.core.NumericHelper;
import com.money.manager.ex.core.UIHelper;
import com.money.manager.ex.investment.SecurityPriceModel;
import com.money.manager.ex.investment.events.PriceDownloadedEvent;
import com.money.manager.ex.utils.ListUtils;
import com.money.manager.ex.utils.MmxDate;

import org.greenrobot.eventbus.EventBus;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import info.javaperformance.money.Money;
import info.javaperformance.money.MoneyFactory;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import timber.log.Timber;

/**
 * Free Currency Exchange Rates API service implementation.
 * Exchange rates provider.
 */

public class FreeCurrencyExchangeRateAPIService extends PriceUpdaterBase implements IExchangeRateUpdater {

    private final IFreeCurrencyExchangeRateAPIService service;
    private final UIHelper uiHelper;

    public FreeCurrencyExchangeRateAPIService(Context context) {
        super(context);
        this.service = createService();
        this.uiHelper = new UIHelper(context);
    }

    @Override
    public void downloadPrices(String baseCurrency, List<String> symbols) {
        if (symbols == null || symbols.isEmpty()) return;

        showProgressDialog(symbols.size());

        String symbolsString = String.join(",", symbols);

        service.getExchangeRates(baseCurrency).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    handleContentDownloaded(baseCurrency, response.body(), symbols);
                } else {
                    Timber.d(String.valueOf(response.code()));
                    closeProgressDialog();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                closeProgressDialog();
                Timber.e(t, "fetching price");
            }
        });
    }

    private void handleContentDownloaded(String baseCurrency, JsonObject response, List<String> symbols) {
        if (response == null) {
            uiHelper.showToast(R.string.error_updating_rates);
            closeProgressDialog();
            return;
        }

        List<SecurityPriceModel> pricesList = extractPricesFromJson(baseCurrency, response);

        if (pricesList == null) {
            uiHelper.showToast(R.string.error_no_price_found_for_symbol);
        } else {
            StringBuilder updatedCurrencies = new StringBuilder();

            for (SecurityPriceModel model : pricesList) {
                if (symbols.contains(model.symbol.toUpperCase())) {
                    updatedCurrencies.append(model.symbol).append(",");
                    EventBus.getDefault().post(new PriceDownloadedEvent(model.symbol, model.price, model.date));
                }
            }

            if (updatedCurrencies.length() > 0) {
                String message = getContext().getString(R.string.download_complete) +
                        " (" + updatedCurrencies.substring(0, updatedCurrencies.length() - 1) + ")";
                uiHelper.showToast(message, Toast.LENGTH_LONG);
            }
        }

        closeProgressDialog();
    }

    private IFreeCurrencyExchangeRateAPIService createService() {
        String BASE_URL = "https://cdn.jsdelivr.net/npm/@fawazahmed0/";
        return new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(BASE_URL)
                .build()
                .create(IFreeCurrencyExchangeRateAPIService.class);
    }

    private List<SecurityPriceModel> extractPricesFromJson(String baseCurrency, JsonObject root) {
        List<SecurityPriceModel> pricesList = new ArrayList<>();
        String dateString = root.get("date").getAsString();
        JsonObject rates = root.get(baseCurrency).getAsJsonObject();

        if (rates == null || rates.isJsonNull()) return null;

        for (Map.Entry<String, JsonElement> entry : rates.entrySet()) {
            SecurityPriceModel priceModel = createSecurityPriceModel(entry, dateString);
            if (priceModel != null) {
                pricesList.add(priceModel);
            }
        }

        return pricesList;
    }

    private SecurityPriceModel createSecurityPriceModel(Map.Entry<String, JsonElement> quote, String dateString) {
        SecurityPriceModel priceModel = new SecurityPriceModel();
        priceModel.symbol = quote.getKey();

        JsonElement priceElement = quote.getValue();
        if (priceElement == JsonNull.INSTANCE || !NumericHelper.isNumeric(priceElement.getAsString())) {
            uiHelper.showToast(getContext().getString(R.string.error_no_price_found_for_symbol) + " " + priceModel.symbol);
            return null;
        }

        BigDecimal invertedPrice = BigDecimal.ONE.divide(
                new BigDecimal(priceElement.getAsString()), 10, RoundingMode.HALF_EVEN);
        priceModel.price = MoneyFactory.fromBigDecimal(invertedPrice);
        priceModel.date = new MmxDate(dateString).toDate();

        return priceModel;
    }
}