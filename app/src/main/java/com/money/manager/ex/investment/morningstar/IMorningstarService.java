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

package com.money.manager.ex.investment.morningstar;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Morningstar network service
 * http://quotes.morningstar.com
 * t=XPAR:BNP
 */
public interface IMorningstarService {
    @GET("/stockq/c-header")
    Observable<String> getPrice(@Query("t") String symbol);
}
