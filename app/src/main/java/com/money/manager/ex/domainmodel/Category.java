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

import org.parceler.Parcel;

import java.util.Objects;

/**
 * Category
 */
@Parcel
public class Category
    extends EntityBase {

    public static final String CATEGID = "CATEGID";
    public static final String CATEGNAME = "CATEGNAME";
    public static final String ACTIVE = "ACTIVE";
    public static final String PARENTID = "PARENTID";

    // Wolfsolver One Category List
    public static final String BASECATEGNAME = "BASECATEGNAME";
    public static final String PARENTCATEGNAME = "PARENTCATEGNAME";

    public Category() { super(); }

    public Category(ContentValues contentValues) {
        super(contentValues);
    }

    public int getId() {
        return getInt(CATEGID);
    }

    public void setId(Integer value) {
        setInt(CATEGID, value);
    }

    public int getParentId() {
        return getInt(PARENTID);
    }

    public void setParentId(Integer value) {
        setInt(PARENTID, value);
    }

    public String getName() {
        return getString(CATEGNAME);
    }

    public void setName(String value) {
        setString(CATEGNAME, value);
    }

    public String getBaseCategName( ) { return ( getString(BASECATEGNAME) == null ? "" : getString(BASECATEGNAME)) ;}
    public void setBaseCategName( String value) { setString(BASECATEGNAME, value); }

    public String getParentCategName( ) { return ( getString(PARENTCATEGNAME) == null ? "" : getString(PARENTCATEGNAME)) ;}
    public void setParentCategName( String value) { setString(PARENTCATEGNAME, value); }

    public String getCategFullName ( ) {
        if ( getParentCategName().isEmpty() ) {
            return  getName();
        }
        return getParentCategName() + " : " +getName(); }
}
