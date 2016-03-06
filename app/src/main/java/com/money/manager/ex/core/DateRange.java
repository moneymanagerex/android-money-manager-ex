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
package com.money.manager.ex.core;

import android.os.Parcel;
import android.os.Parcelable;

import com.money.manager.ex.utils.DateUtils;
import com.money.manager.ex.utils.MyDateTimeUtils;

import java.util.Date;

/**
 * Represents a date range.
 */
public class DateRange
    implements Parcelable {

    public DateRange(Date dateFrom, Date dateTo) {
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
    }

    public Date dateFrom;
    public Date dateTo;

    protected DateRange(Parcel in) {
        this.dateFrom = DateUtils.getDateFromIsoString(in.readString());
        this.dateTo = DateUtils.getDateFromIsoString(in.readString());
    }

    public static final Creator<DateRange> CREATOR = new Creator<DateRange>() {
        @Override
        public DateRange createFromParcel(Parcel in) {
            return new DateRange(in);
        }

        @Override
        public DateRange[] newArray(int size) {
            return new DateRange[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        String from = MyDateTimeUtils.getIsoStringFrom(this.dateFrom);
        dest.writeString(from);

        String to = MyDateTimeUtils.getIsoStringFrom(this.dateTo);
        dest.writeString(to);
    }
}
