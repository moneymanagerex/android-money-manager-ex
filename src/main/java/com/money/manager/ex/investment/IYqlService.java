/*
 * Copyright (C) 2012-2015 The Android Money Manager Ex Project Team
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
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.money.manager.ex.investment;

import com.google.gson.JsonElement;
import com.squareup.okhttp.Response;

import java.util.List;

import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Service interface using Retrofit, to fetch data from YQL service.
 *
 * Created by Alen Siljak on 25/09/2015.
 */
public interface IYqlService {
    @GET("/v1/public/yql?format=json&env=store://datatables.org/alltableswithkeys")
    Call<JsonElement> getPrices(@Query("q") String query);

//    @GET("/v1/public/yql?q={query}&format=json&env=store://datatables.org/alltableswithkeys")
//    Call<List<SecurityPriceModel>> getPrices(@Query("query") String query, Callback<List<SecurityPriceModel>> callback);

}
