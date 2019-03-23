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
package com.money.manager.ex.database;

import android.content.Context;
import android.database.Cursor;

import com.money.manager.ex.R;
import com.money.manager.ex.utils.MmxFileUtils;

public class QueryCategorySubCategory extends Dataset {
    //definizione dei nomi dei campi
    public static final String ID = "_id";
    public static final String CATEGID = "CATEGID";
    public static final String CATEGNAME = "CATEGNAME";
    public static final String SUBCATEGID = "SUBCATEGID";
    public static final String SUBCATEGNAME = "SUBCATEGNAME";
    public static final String CATEGSUBNAME = "CATEGSUBNAME";
    //definizione dei campi
    private int categId;
    private CharSequence categName;
    private int subCategId;
    private CharSequence mSubcategoryName;
    private CharSequence categSubName;

    // definizione del costruttore
    public QueryCategorySubCategory(Context context) {
        super(MmxFileUtils.getRawAsString(context, R.raw.query_categorysubcategory), DatasetType.QUERY, "categorysubcategory");
    }

    @Override
    public String[] getAllColumns() {
        return new String[]{ID, CATEGID, CATEGNAME, SUBCATEGID, SUBCATEGNAME, CATEGSUBNAME};
    }

    public int getCategId() {
        return categId;
    }

    public void setCategId(int categId) {
        this.categId = categId;
    }

    public CharSequence getCategName() {
        return categName;
    }

    public void setCategName(CharSequence categName) {
        this.categName = categName;
    }

    public CharSequence getCategSubName() {
        return categSubName;
    }

    public void setCategSubName(CharSequence categSubName) {
        this.categSubName = categSubName;
    }

    public int getSubCategId() {
        return subCategId;
    }

    public void setSubCategId(int subCategId) {
        this.subCategId = subCategId;
    }

    public CharSequence getSubcategoryName() {
        return mSubcategoryName;
    }

    public void setSubcategoryName(CharSequence mSubcategoryName) {
        this.mSubcategoryName = mSubcategoryName;
    }

    @Override
    public void setValueFromCursor(Cursor c) {
        // controllo che non sia null il cursore
        if (c == null) {
            return;
        }
        // controllo che il numero di colonne siano le stesse
        if (!(c.getColumnCount() == this.getAllColumns().length)) {
            return;
        }
        // set dei valori
        this.setCategId(c.getInt(c.getColumnIndex(CATEGID)));
        this.setCategName(c.getString(c.getColumnIndex(CATEGNAME)));
        this.setSubCategId(c.getInt(c.getColumnIndex(SUBCATEGID)));
        this.setSubcategoryName(c.getString(c.getColumnIndex(SUBCATEGNAME)));
        this.setCategSubName(c.getString(c.getColumnIndex(CATEGSUBNAME)));
    }
}
