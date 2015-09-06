/*
 * Copyright (C) 2012-2015 The Android Money Manager Ex Project Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
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
package com.money.manager.ex.search;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Used for search criteria.
 * Created by Alen on 13/07/2015.
 */
public class CategorySub implements Parcelable {

    public static CategorySub getInstance(int categoryId, int subCategoryId) {
        CategorySub object = new CategorySub();
        object.categId = categoryId;
        object.subCategId = subCategoryId;
        return object;
    }

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
