package com.money.manager.ex.investment.yahoofinance;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class YahooChartResponse {
    @SerializedName("chart")
    public Chart chart;

    public static class Chart {
        @SerializedName("result")
        public List<Result> result;
    }

    public static class Result {
        @SerializedName("timestamp")
        public List<Long> timestamps;

        @SerializedName("indicators")
        public Indicators indicators;

        @SerializedName("meta")
        public Meta meta;
    }

    public static class Indicators {
        @SerializedName("quote")
        public List<Quote> quote;
    }

    public static class Quote {
        @SerializedName("close")
        public List<Double> closePrices;
    }

    public static class Meta {
        @SerializedName("regularMarketPrice")
        public Double regularMarketPrice;

        @SerializedName("regularMarketTime")
        public Long regularMarketTime;
    }
}
