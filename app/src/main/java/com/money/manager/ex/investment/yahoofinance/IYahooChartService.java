package com.money.manager.ex.investment.yahoofinance;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface IYahooChartService {
    @GET("/v8/finance/chart/{symbol}")
    Call<YahooChartResponse> getChartData(
            @Path("symbol") String symbol,
            @Query("range") String range,
            @Query("interval") String interval
    );
}