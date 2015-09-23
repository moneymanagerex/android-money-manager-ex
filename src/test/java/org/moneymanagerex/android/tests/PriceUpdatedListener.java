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
package org.moneymanagerex.android.tests;

import com.money.manager.ex.investment.IPriceUpdaterFeedback;

import java.util.Date;

import info.javaperformance.money.Money;

/**
 * Used as a test helper to listen to the events from a tested object and provide
 * the result to the test.
 *
 * Created by Alen on 23/09/2015.
 */
public class PriceUpdatedListener
    implements IPriceUpdaterFeedback {

    public String symbol;
    public Money price;
    public Date date;

    @Override
    public void onPriceDownloaded(String symbol, Money price, Date date) {
        this.symbol = symbol;
        this.price = price;
        this.date = date;
    }
}
