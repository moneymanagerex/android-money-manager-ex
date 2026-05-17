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

                    // Try to read price/time series first
                    List<Long> timestamps = (result.timestamps == null) ? null : result.timestamps;
                    List<Double> prices = null;
                    if (result.indicators != null && result.indicators.quote != null && !result.indicators.quote.isEmpty()) {
                        prices = result.indicators.quote.get(0).closePrices;
                    }

                    // If series data is missing, try falling back to meta.regularMarketPrice/time
                    if ((timestamps == null || timestamps.isEmpty() || prices == null || prices.isEmpty())) {
                        if (result.meta != null && result.meta.regularMarketPrice != null && result.meta.regularMarketTime != null) {
                            try {
                                double latestPrice = result.meta.regularMarketPrice;
                                long latestTimestamp = result.meta.regularMarketTime;
                                Date date = new MmxDate(latestTimestamp * 1000L).toDate();
                                Money moneyPrice = MoneyFactory.fromDouble(latestPrice);

                                stockRepository.updateCurrentPrice(symbol, moneyPrice);
                                stockHistoryRepository.addStockHistoryRecord(symbol, moneyPrice, date);

                                SecurityPriceModel model = new SecurityPriceModel();
                                model.symbol = symbol;
                                model.price = moneyPrice;
                                model.date = date;
                                liveData.postValue(model);
                                return;
                            } catch (Exception e) {
                                Timber.e(e, "Error updating stock price from meta fallback");
                                liveData.postValue(null);
                                return;
                            }
                        }

                        Timber.e("Invalid stock price data for symbol: %s", symbol);
                        liveData.postValue(null);
                        return;
                    }

                    try {
                        if (!timestamps.isEmpty() && !prices.isEmpty()) {
                            double latestPrice = prices.get(prices.size() - 1);
                            long latestTimestamp = timestamps.get(timestamps.size() - 1);
                            Date date = new MmxDate(latestTimestamp * 1000L).toDate();
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
                    catch (Exception e) {
                        Timber.e(e, "Error updating stock price");
                        liveData.postValue(null);
                    }
                } else {
                    liveData.postValue(null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<YahooChartResponse> call, @NonNull Throwable t) {
                Timber.e(t, "Error fetching stock prices");
                liveData.postValue(null);
            }
        });

        return liveData;
    }

    public LiveData<Integer> downloadPriceHistory(String symbol, Date fromDate, Date toDate) {
        MutableLiveData<Integer> liveData = new MutableLiveData<>();

        long period1 = fromDate.getTime() / 1000;
        long period2 = toDate.getTime() / 1000;

        yahooService.getChartDataForPeriod(symbol, period1, period2, "1d").enqueue(new Callback<YahooChartResponse>() {
            @Override
            public void onResponse(@NonNull Call<YahooChartResponse> call, @NonNull Response<YahooChartResponse> response) {
                YahooChartResponse.Result result = getValidHistoryResult(response.body());
                if (result == null) {
                    liveData.postValue(0);
                    return;
                }

                try {
                    SaveHistoryResult saveResult = saveHistoryPrices(symbol, result.timestamps,
                            result.indicators.quote.get(0).closePrices);
                    if (saveResult.latestPrice != null) {
                        stockRepository.updateCurrentPrice(symbol, saveResult.latestPrice);
                    }

                    liveData.postValue(saveResult.savedCount);
                } catch (Exception e) {
                    Timber.e(e, "Error storing historical stock prices");
                    liveData.postValue(0);
                }
            }

            @Override
            public void onFailure(@NonNull Call<YahooChartResponse> call, @NonNull Throwable t) {
                Timber.e(t, "Error fetching historical stock prices");
                liveData.postValue(-1);
            }
        });

        return liveData;
    }

    private YahooChartResponse.Result getValidHistoryResult(YahooChartResponse response) {
        if (response == null || response.chart == null || response.chart.result == null
                || response.chart.result.isEmpty()) {
            return null;
        }

        YahooChartResponse.Result result = response.chart.result.get(0);
        if (result.timestamps == null || result.indicators == null
                || result.indicators.quote == null || result.indicators.quote.isEmpty()
                || result.indicators.quote.get(0).closePrices == null) {
            return null;
        }

        return result;
    }

    private SaveHistoryResult saveHistoryPrices(String symbol, List<Long> timestamps,
                                                List<Double> prices) {
        int savedCount = 0;
        Money latestPrice = null;

        int size = Math.min(timestamps.size(), prices.size());
        for (int i = 0; i < size; i++) {
            Double price = prices.get(i);
            if (price == null) {
                continue;
            }

            Date date = new MmxDate(timestamps.get(i) * 1000L).toDate();
            Money moneyPrice = MoneyFactory.fromDouble(price);
            stockHistoryRepository.addStockHistoryRecord(symbol, moneyPrice, date);
            latestPrice = moneyPrice;
            savedCount++;
        }

        return new SaveHistoryResult(savedCount, latestPrice);
    }

    private SaveHistoryResult saveHistoryPricesForDate(String symbol, List<Long> timestamps,
                                                       List<Double> prices, Date targetDate) {
        int savedCount = 0;
        Money latestPrice = null;

        // Convert targetDate to midnight for day-level comparison
        MmxDate targetDay = new MmxDate(targetDate);
        String targetDateString = targetDay.toIsoDateString();

        int size = Math.min(timestamps.size(), prices.size());
        for (int i = 0; i < size; i++) {
            Double price = prices.get(i);
            if (price == null) {
                continue;
            }

            Date date = new MmxDate(timestamps.get(i) * 1000L).toDate();
            String priceDay = new MmxDate(date).toIsoDateString();

            // Only save if the price date matches the target date
            if (priceDay.equals(targetDateString)) {
                Money moneyPrice = MoneyFactory.fromDouble(price);
                stockHistoryRepository.addStockHistoryRecord(symbol, moneyPrice, date);
                latestPrice = moneyPrice;
                savedCount++;
            }
        }

        return new SaveHistoryResult(savedCount, latestPrice);
    }

    public LiveData<Integer> downloadPriceForDate(String symbol, Date targetDate) {
        MutableLiveData<Integer> liveData = new MutableLiveData<>();

        // Download from target date to today to get the data
        long period1 = targetDate.getTime() / 1000;
        long period2 = new Date().getTime() / 1000;

        yahooService.getChartDataForPeriod(symbol, period1, period2, "1d").enqueue(new Callback<YahooChartResponse>() {
            @Override
            public void onResponse(@NonNull Call<YahooChartResponse> call, @NonNull Response<YahooChartResponse> response) {
                YahooChartResponse.Result result = getValidHistoryResult(response.body());
                if (result == null) {
                    liveData.postValue(0);
                    return;
                }

                try {
                    SaveHistoryResult saveResult = saveHistoryPricesForDate(symbol, result.timestamps,
                            result.indicators.quote.get(0).closePrices, targetDate);

                    if (saveResult.latestPrice != null && isToday(targetDate)) {
                        stockRepository.updateCurrentPrice(symbol, saveResult.latestPrice);
                    }

                    liveData.postValue(saveResult.savedCount);
                } catch (Exception e) {
                    Timber.e(e, "Error storing historical stock price");
                    liveData.postValue(0);
                }
            }

            @Override
            public void onFailure(@NonNull Call<YahooChartResponse> call, @NonNull Throwable t) {
                Timber.e(t, "Error fetching historical stock price");
                liveData.postValue(-1);
            }
        });

        return liveData;
    }

    private boolean isToday(Date date) {
        return new MmxDate(date).toIsoDateString().equals(new MmxDate().toIsoDateString());
    }

    private static final class SaveHistoryResult {
        private final int savedCount;
        private final Money latestPrice;

        private SaveHistoryResult(int savedCount, Money latestPrice) {
            this.savedCount = savedCount;
            this.latestPrice = latestPrice;
        }
    }
}
