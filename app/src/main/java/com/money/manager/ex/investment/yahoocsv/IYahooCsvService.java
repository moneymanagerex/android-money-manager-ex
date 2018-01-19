/*
 * Copyright (C) 2012-2018 The Android Money Manager Ex Project Team
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

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Service interface using Retrofit, to fetch quotes from Yahoo, using CSV.
 */
public interface IYahooCsvService {
//    @GET("/v1/public/yql?format=json&env=store://datatables.org/alltableswithkeys")
//    Call<YqlStockPriceResponse> getPrices(@Select("q") String query);

//    @GET("/v1/public/yql?q={query}&format=json&env=store://datatables.org/alltableswithkeys")
//    Call<List<SecurityPriceModel>> getPrices(@Select("query") String query, Callback<List<SecurityPriceModel>> callback);

    /**
     * "http://download.finance.yahoo.com/d/quotes.csv?f=sl1d1c4&e=.csv"
     * @param symbol Yahoo Finance symbol to update
     * //@return Contents of the CSV result from Yahoo
     */
    @GET("/d/quotes.csv?f=sl1d1c4&e=.csv")
    Call<String> getPrice(@Query("s") String symbol);
}
