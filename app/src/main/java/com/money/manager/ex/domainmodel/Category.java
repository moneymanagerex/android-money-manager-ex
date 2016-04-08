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
package com.money.manager.ex.domainmodel;

import android.content.ContentValues;
import android.os.Parcel;

/**
 * Category
 */
public class Category
    extends EntityBase {

    public static final String CATEGID = "CATEGID";
    public static final String CATEGNAME = "CATEGNAME";

    public static final Creator<Category> CREATOR = new Creator<Category>() {
        @Override
        public Category createFromParcel(Parcel in) {
            return new Category(in);
        }

        @Override
        public Category[] newArray(int size) {
            return new Category[size];
        }
    };

    public Category() {
        super();
    }

    public Category(ContentValues contentValues) {
        super(contentValues);
    }

    protected Category(Parcel in) {
        contentValues = in.readParcelable(ContentValues.class.getClassLoader());
    }

    public String getName() {
        return getString(CATEGNAME);
    }
}
