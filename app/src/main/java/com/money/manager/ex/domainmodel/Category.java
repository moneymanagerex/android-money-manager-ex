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
package com.money.manager.ex.domainmodel;

import android.content.ContentValues;
import android.os.Parcelable;

import org.parceler.Parcel;

/**
 * Category
 */
@Parcel
public class Category
    extends EntityBase
        implements Parcelable {

    public static final String ID = "CATEGID";
    public static final String NAME = "CATEGNAME";
    public static final String PARENTID = "PARENTID";
    public static final String BASENAME = "BASENAME";
    public static final String ACTIVE = "ACTIVE";

    public Category() {
        super();
    }

    public Category(ContentValues contentValues) {
        super(contentValues);
    }

    protected Category(android.os.Parcel in) {
        setId(in.readLong());
        setName( in.readString() );
        setParentId( in.readLong() );
        setBasename( in.readString() );
        setActive( in.readBoolean() );
    }

    public static final Creator<Category> CREATOR = new Creator<Category>() {
        @Override
        public Category createFromParcel(android.os.Parcel in) {
            return new Category(in);
        }

        @Override
        public Category[] newArray(int size) {
            return new Category[size];
        }
    };

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(android.os.Parcel parcel, int i) {
        parcel.writeLong(getId());
        parcel.writeString(getName());
        parcel.writeLong(getParentId());
        parcel.writeString(getBasename());
        parcel.writeBoolean(getActive());
    }

    public Long getId() {
        return getLong(ID);
    }
    public void setId(Long value) {
        setLong(ID, value);
    }

    public String getName() {
        return getString(NAME);
    }
    public void setName(String value) {
        setString(NAME, value);
    }

    public long getParentId() {
        return getLong(PARENTID);
    }
    public void setParentId(Long value) {
        setLong(PARENTID, value);
    }

    public String getBasename() { return getString(BASENAME);}
    public void setBasename(String value) { setString(BASENAME, value);}

    public boolean getActive() { return getBoolean(ACTIVE);}
    public void setActive(boolean value) { setBoolean(ACTIVE, value);}
}


