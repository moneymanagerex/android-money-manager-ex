package com.money.manager.ex.investment.yahoofinance;

import android.content.Context;

import androidx.annotation.NonNull;

import com.money.manager.ex.R;
import com.money.manager.ex.core.UIHelper;
import com.money.manager.ex.investment.events.AllPricesDownloadedEvent;
import com.money.manager.ex.investment.events.PriceDownloadedEvent;
import com.money.manager.ex.investment.prices.ISecurityPriceUpdater;
import com.money.manager.ex.investment.prices.PriceUpdaterBase;
import com.money.manager.ex.utils.MmxDate;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import info.javaperformance.money.MoneyFactory;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import timber.log.Timber;

public class YahooChartDownloaderRetrofit
        extends PriceUpdaterBase
        implements ISecurityPriceUpdater {

    private int mCounter;
    private int mTotalRecords;

    public YahooChartDownloaderRetrofit(Context context) {
        super(context);
    }

    @Override
    public void downloadPrices(List<String> symbols) {
        if (symbols == null || symbols.isEmpty()) return;
        mTotalRecords = symbols.size();

        showProgressDialog(mTotalRecords);
        IYahooChartService service = getYahooService();

        for (String symbol : symbols) {
            service.getChartData(symbol, "1mo", "1d").enqueue(new Callback<>() {
                @Override
                public void onResponse(@NonNull Call<YahooChartResponse> call, @NonNull Response<YahooChartResponse> response) {
                    if (response.body() != null && response.body().chart.result != null) {
                        onContentDownloaded(symbol, response.body().chart.result.get(0));
                    } else {
                        Timber.e("Empty response from Yahoo Finance API");
                    }
                }

                @Override
                public void onFailure(@NonNull Call<YahooChartResponse> call, @NonNull Throwable t) {
                    mCounter++;
                    setProgress(mCounter);
                    finishIfAllDone();
                    Timber.e(t, "Error fetching stock prices");
                }
            });
        }
    }

    private synchronized void finishIfAllDone() {
        if (mCounter != mTotalRecords) return;

        closeProgressDialog();
        new UIHelper(getContext()).showToast(getContext().getString(R.string.download_complete));
        EventBus.getDefault().post(new AllPricesDownloadedEvent());
    }

    private void onContentDownloaded(String symbol, YahooChartResponse.Result result) {
        mCounter++;
        setProgress(mCounter);

        if (result == null || result.timestamps == null || result.indicators.quote == null) {
            Timber.e("Invalid chart data for %s", symbol);
            return;
        }

        List<Long> timestamps = result.timestamps;
        List<Double> prices = result.indicators.quote.get(0).closePrices;
        if (timestamps.isEmpty() || prices.isEmpty()) return;

        double latestPrice = prices.get(prices.size() - 1);
        long latestTimestamp = timestamps.get(timestamps.size() - 1);

        PriceDownloadedEvent event = new PriceDownloadedEvent(symbol, MoneyFactory.fromDouble(latestPrice), new MmxDate(latestTimestamp).toDate());
        EventBus.getDefault().post(event);

        finishIfAllDone();
    }

    public IYahooChartService getYahooService() {
        String BASE_URL = "https://query2.finance.yahoo.com";
        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(BASE_URL)
                .build();
        return retrofit.create(IYahooChartService.class);
    }
}

