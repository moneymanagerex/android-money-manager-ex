/*
 * Copyright (C) 2012-2016 The Android Money Manager Ex Project Team
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

import android.os.Parcel;
import android.os.Parcelable;

import com.money.manager.ex.utils.DateUtils;
import com.money.manager.ex.utils.MyDateTimeUtils;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import java.util.Date;

import info.javaperformance.money.Money;
import info.javaperformance.money.MoneyFactory;

/**
 * Class that contains the search parameters.
 * Used as a DTO and to store the values.
 */
public class SearchParameters implements Parcelable {

    public static final String STRING_NULL_VALUE = "null";

    public SearchParameters() {
        // default constructor
        // explicitly set the null value
        this.status = STRING_NULL_VALUE;
    }

    // Account
    public Integer accountId;

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
    public DateTime dateFrom;
    public DateTime dateTo;

    public Integer payeeId = null;
    public String payeeName;

    public CategorySub category;

    public String transactionNumber;
    public String notes;

    protected SearchParameters(Parcel in) {
        this.accountId = (Integer) in.readValue(null);

        deposit = in.readByte() != 0;
        transfer = in.readByte() != 0;
        withdrawal = in.readByte() != 0;
        status = in.readString();

        String amountFromParcel = in.readString();
        if (StringUtils.isNotEmpty(amountFromParcel)) {
            amountFrom = MoneyFactory.fromString(amountFromParcel);
        }

        String amountToParcel = in.readString();
        if (StringUtils.isNotEmpty(amountToParcel)) {
            amountTo = MoneyFactory.fromString(amountToParcel);
        }

        String dateString = in.readString();
        if (!StringUtils.isEmpty(dateString)) {
            dateFrom = new DateTime(dateString);
        }

        dateString = in.readString();
        if (!StringUtils.isEmpty(dateString)) {
            dateTo = MyDateTimeUtils.from(dateString);
        }

        payeeId = (Integer) in.readValue(null);
        payeeName = in.readString();
        category = in.readParcelable(CategorySub.class.getClassLoader());
        transactionNumber = in.readString();
        notes = in.readString();
    }

    public static final Creator<SearchParameters> CREATOR = new Creator<SearchParameters>() {
        @Override
        public SearchParameters createFromParcel(Parcel in) {
            return new SearchParameters(in);
        }

        @Override
        public SearchParameters[] newArray(int size) {
            return new SearchParameters[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        // this is a sample on how to read/write nullable objects.
        parcel.writeValue(this.accountId);

        parcel.writeByte((byte) (this.deposit ? 1 : 0));
        parcel.writeByte((byte) (this.transfer ? 1 : 0));
        parcel.writeByte((byte) (this.withdrawal ? 1 : 0));

        parcel.writeString(this.status);

        parcel.writeString(amountFrom != null ? amountFrom.toString() : null);
        parcel.writeString(amountTo != null ? amountTo.toString() : null);

        String dateString = dateFrom != null ? dateFrom.toString() : "";
        parcel.writeString(dateString);
        parcel.writeString(MyDateTimeUtils.getIsoStringFrom(dateTo));

        parcel.writeValue(payeeId);
        parcel.writeString(payeeName);

        parcel.writeParcelable(category, i);

        parcel.writeString(transactionNumber);
        parcel.writeString(notes);
    }

    // Parcelable implementation
}
