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
package com.money.manager.ex.search;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Used for search criteria.
 * Created by Alen on 13/07/2015.
 */
public class CategorySub implements Parcelable {

    public CategorySub() {
        // default constructor
    }

    public int categId;
    public String categName;
    public int subCategId;
    public String subCategName;

    protected CategorySub(Parcel in) {
        categId = in.readInt();
        categName = in.readString();
        subCategId = in.readInt();
        subCategName = in.readString();
    }

    public static final Creator<CategorySub> CREATOR = new Creator<CategorySub>() {
        @Override
        public CategorySub createFromParcel(Parcel in) {
            return new CategorySub(in);
        }

        @Override
        public CategorySub[] newArray(int size) {
            return new CategorySub[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(categId);
        parcel.writeString(categName);

        parcel.writeInt(subCategId);
        parcel.writeString(subCategName);
    }
}
