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

package com.money.manager.ex.account;

import android.os.Parcel;
import android.os.Parcelable;

import com.money.manager.ex.core.DateRange;
import com.money.manager.ex.core.DefinedDateRange;

/**
 * Contains the selected filters for the account transactions list.
 * Used to pass to the UI filter dialog, and to the adapter when querying the data.
 */
public class TransactionFilter
    implements Parcelable {

    public TransactionFilter() {

    }

    public TransactionFilter(Parcel in) {
        this.dateRange = in.readParcelable(DefinedDateRange.class.getClassLoader());
        this.transactionStatus = in.readParcelable(StatusFilter.class.getClassLoader());
    }

    public DateRange dateRange;
    public StatusFilter transactionStatus;


    public static final Creator<TransactionFilter> CREATOR = new Creator<TransactionFilter>() {
        @Override
        public TransactionFilter createFromParcel(Parcel in) {
            return new TransactionFilter(in);
        }

        @Override
        public TransactionFilter[] newArray(int size) {
            return new TransactionFilter[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.dateRange, 0);
        dest.writeParcelable(this.transactionStatus, 0);
    }
}
