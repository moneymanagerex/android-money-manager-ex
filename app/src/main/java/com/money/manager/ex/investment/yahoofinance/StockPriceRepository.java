package com.money.manager.ex.investment.yahoofinance;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.money.manager.ex.datalayer.StockHistoryRepository;
import com.money.manager.ex.datalayer.StockRepository;
import com.money.manager.ex.investment.SecurityPriceModel;
import com.money.manager.ex.utils.MmxDate;

import java.util.Date;
import java.util.List;

import info.javaperformance.money.Money;
import info.javaperformance.money.MoneyFactory;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import timber.log.Timber;

public class StockPriceRepository {
    private final IYahooChartService yahooService;
    private final StockRepository stockRepository;
    private final StockHistoryRepository stockHistoryRepository;

    public StockPriceRepository(Application application) {
        this.yahooService = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("https://query2.finance.yahoo.com")
                .build()
                .create(IYahooChartService.class);
        this.stockRepository = new StockRepository(application);
        this.stockHistoryRepository = new StockHistoryRepository(application);
    }

    public LiveData<SecurityPriceModel> downloadPrice(String symbol) {
        MutableLiveData<SecurityPriceModel> liveData = new MutableLiveData<>();

        yahooService.getChartData(symbol, "1mo", "1d").enqueue(new Callback<YahooChartResponse>() {
            @Override
            public void onResponse(@NonNull Call<YahooChartResponse> call, @NonNull Response<YahooChartResponse> response) {
                if (response.body() != null && response.body().chart != null && response.body().chart.result != null) {
                    YahooChartResponse.Result result = response.body().chart.result.get(0);

                    if (result.timestamps == null || result.indicators == null ||
                            result.indicators.quote == null || result.indicators.quote.isEmpty() ||
                            result.indicators.quote.get(0).closePrices == null) {
                        Timber.e("Invalid stock price data for symbol: %s", symbol);
                        return;
                    }

                    List<Long> timestamps = result.timestamps;
                    List<Double> prices = result.indicators.quote.get(0).closePrices;

                    if (!timestamps.isEmpty() && !prices.isEmpty()) {
                        double latestPrice = prices.get(prices.size() - 1);
                        long latestTimestamp = timestamps.get(timestamps.size() - 1);
                        Date date = new MmxDate(latestTimestamp).toDate();
                        Money moneyPrice = MoneyFactory.fromDouble(latestPrice);

                        stockRepository.updateCurrentPrice(symbol, moneyPrice);
                        stockHistoryRepository.addStockHistoryRecord(symbol, moneyPrice, date);

                        SecurityPriceModel model = new SecurityPriceModel();
                        model.symbol = symbol;
                        model.price = moneyPrice;
                        model.date = date;
                        liveData.postValue(model);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<YahooChartResponse> call, @NonNull Throwable t) {
                Timber.e(t, "Error fetching stock prices");
            }
        });

        return liveData;
    }
}