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

/**
 * Contains the parameters for a query
 */
public class Query
    implements Parcelable {

    public final static Parcelable.Creator<Query> CREATOR = new Parcelable.Creator<Query>() {
        public Query createFromParcel(Parcel source) {
            Query record = new Query();
            record.readFromParcel(source);
            return record;
        }

        @Override
        public Query[] newArray(int size) {
            return new Query[size];
        }
    };

    public String[] projection;
    public String selection;
    public String[] arguments;
    public String sort;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(this.projection);
        dest.writeString(this.selection);
        dest.writeStringArray(this.arguments);
        dest.writeString(this.sort);
    }

    public void readFromParcel(Parcel source) {
        source.readStringArray(this.projection);
        this.selection = source.readString();
        source.readStringArray(this.arguments);
        this.sort = source.readString();
    }
}
