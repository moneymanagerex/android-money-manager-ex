/*
 * Copyright (C) 2012-2019 The Android Money Manager Ex Project Team
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

import com.google.gson.JsonArray;
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
import retrofit2.converter.scalars.ScalarsConverterFactory;
import timber.log.Timber;

/**
 * Fixer.io service implementation.
 * Exchange rates provider.
 */

public class FixerService
    extends PriceUpdaterBase
    implements IExchangeRateUpdater {

    public FixerService(Context context) {
        super(context);
    }

    @Override
    public void downloadPrices(String baseCurrency, List<String> symbols) {
        if (symbols == null) return;
        int items = symbols.size();
        if (items == 0) return;

        showProgressDialog(items);

        IFixerService service = getService();

        Callback<JsonElement> callback = new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                onContentDownloaded(response.body());
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                closeProgressDialog();
                Timber.e(t, "fetching price");
            }
        };

        // parameters
        String symbolsString = new ListUtils().toCommaDelimitedString(symbols);

        try {
            service.getPrices(baseCurrency, symbolsString).enqueue(callback);
        } catch (Exception ex) {
            Timber.e(ex, "downloading quotes");
        }

    }

    private void onContentDownloaded(JsonElement response) {
        UIHelper uiHelper = new UIHelper(getContext());

        if (response == null) {
            uiHelper.showToast(R.string.error_updating_rates);
            closeProgressDialog();
            return;
        }

        List<SecurityPriceModel> pricesList = getPricesFromJson(response.getAsJsonObject());
        StringBuilder updatedCurrencies = new StringBuilder();
        if (pricesList == null) {
            uiHelper.showToast(R.string.error_no_price_found_for_symbol);
        } else {
            // Send the parsed price data to the listener(s).
            for (SecurityPriceModel model : pricesList) {
                updatedCurrencies.append(model.symbol);
                updatedCurrencies.append(",");

                // Notify the caller.
                EventBus.getDefault().post(new PriceDownloadedEvent(model.symbol, model.price, model.date));
            }
        }
        closeProgressDialog();

        // Notify the user of the prices that have been downloaded.
        String message = getContext().getString(R.string.download_complete) +
            " (" + updatedCurrencies.toString().substring(0, updatedCurrencies.toString().length() - 1) + ")";
        uiHelper.showToast(message, Toast.LENGTH_LONG);
    }

    private IFixerService getService() {
        String BASE_URL = "https://api.fixer.io";
        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(BASE_URL)
                .build();
        return retrofit.create(IFixerService.class);
    }

    private List<SecurityPriceModel> getPricesFromJson(JsonObject root) {
        ArrayList<SecurityPriceModel> result = new ArrayList<>();

        String dateString = root.get("date").getAsString();
        JsonObject rates = root.get("rates").getAsJsonObject();
        if (rates == null || rates.isJsonNull()) return null;

        // prices
        Set<Map.Entry<String, JsonElement>> entries = rates.entrySet();
        for (Map.Entry<String, JsonElement> entry : entries)
        {
            SecurityPriceModel priceModel = getSecurityPriceFor(entry, dateString);
            if (priceModel == null) continue;

            result.add(priceModel);
        }

        return result;
    }

    private SecurityPriceModel getSecurityPriceFor(Map.Entry<String, JsonElement> quote, String dateString) {
        SecurityPriceModel priceModel = new SecurityPriceModel();
        priceModel.symbol = quote.getKey();

        UIHelper ui = new UIHelper(getContext());

        // Price

        JsonElement priceElement = quote.getValue();
        if (priceElement == JsonNull.INSTANCE) {
            ui.showToast(getContext().getString(R.string.error_no_price_found_for_symbol) + " " + priceModel.symbol);
            return null;
        }
        String priceString = priceElement.getAsString();
        if (!NumericHelper.isNumeric(priceString)) {
            ui.showToast(getContext().getString(R.string.error_no_price_found_for_symbol) + " " + priceModel.symbol);
            return null;
        }

        Money retrievedPrice = MoneyFactory.fromString(priceString);
        // invert the price. Round to 10 decimals.
        BigDecimal invertedPrice = BigDecimal.ONE.divide(retrievedPrice.toBigDecimal(), 10, RoundingMode.HALF_EVEN);
        priceModel.price = MoneyFactory.fromBigDecimal(invertedPrice);

        // Date

        Date date = new MmxDate(dateString).toDate();
        priceModel.date = date;

        return priceModel;
    }
}
