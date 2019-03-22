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

package com.money.manager.ex.search;

import org.parceler.Parcel;

import java.util.Date;

import info.javaperformance.money.Money;

/**
 * Class that contains the search parameters.
 * Used as a DTO and to store the values.
 */
@Parcel
public class SearchParameters {

    public static final String STRING_NULL_VALUE = "null";

    public SearchParameters() {
        // explicitly set the null value
        this.status = STRING_NULL_VALUE;
    }

    // Account
    public Integer accountId;

    // Currency
    public Integer currencyId;

    // Transaction Type
    public boolean deposit;
    public boolean transfer;
    public boolean withdrawal;

    // Status
    public String status;

    // Amount
    public Money amountFrom;
    public Money amountTo;

    // Date
    public Date dateFrom;
    public Date dateTo;

    public Integer payeeId = null;
    public String payeeName;

    public CategorySub category;

    public String transactionNumber;
    public String notes;
}
