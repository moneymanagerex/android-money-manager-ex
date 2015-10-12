/*
 * Copyright (C) 2012-2015 The Android Money Manager Ex Project Team
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
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.money.manager.ex.Constants;
import com.money.manager.ex.core.ExceptionHandler;
import com.money.manager.ex.utils.DateUtils;

import org.javamoney.moneta.FastMoney;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.money.MonetaryAmount;

import info.javaperformance.money.Money;
import info.javaperformance.money.MoneyFactory;

/**
 * Base for the model entities. Keeps a reference to a cursor that contains the underlying data.
 * Created by Alen Siljak on 5/09/2015.
 */
public class EntityBase
    implements Parcelable {

    /**
     * Default constructor.
     */
    protected EntityBase() {
        contentValues = new ContentValues();
    }

    public ContentValues contentValues;

    /**
     * Contains the pointer to the actual data when loading from content provider.
     */
    protected Cursor mCursor;

    protected EntityBase(Parcel in) {
        contentValues = in.readParcelable(ContentValues.class.getClassLoader());
    }

    public static final Creator<EntityBase> CREATOR = new Creator<EntityBase>() {
        @Override
        public EntityBase createFromParcel(Parcel in) {
            return new EntityBase(in);
        }

        @Override
        public EntityBase[] newArray(int size) {
            return new EntityBase[size];
        }
    };

    public void loadFromCursor(Cursor c) {
        this.contentValues.clear();

        DatabaseUtils.cursorRowToContentValues(c, contentValues);
    }

    public void setCursor(Cursor c) {
        this.mCursor = c;
    }

    protected Boolean getBoolean(String column) {
        return contentValues.getAsBoolean(column);
    }

    protected void setBoolean(String column, Boolean value) {
        contentValues.put(column, value);
    }

    protected MonetaryAmount getMoneta(String fieldName, String currencyCode) {
        Double d = contentValues.getAsDouble(fieldName);

        MonetaryAmount dAmount = FastMoney.of(d, currencyCode);
        return dAmount;
    }

    protected Money getMoney(String fieldName) {
        String value = contentValues.getAsString(fieldName);
        if (value == null || TextUtils.isEmpty(value)) return null;

        Money result = MoneyFactory.fromString(value).truncate(Constants.DEFAULT_PRECISION);
        return result;
    }

    protected void setMoney(String fieldName, Money value) {
        contentValues.put(fieldName, value.toString());
    }

    protected Date getDate(String fieldName) {
        String dateString = getString(fieldName);
        SimpleDateFormat format = new SimpleDateFormat(Constants.PATTERN_DB_DATE);
        Date date = null;
        try {
            date = format.parse(dateString);
        } catch (ParseException pex) {
            ExceptionHandler handler = new ExceptionHandler(null, this);
            handler.handle(pex, "parsing the date from " + fieldName);
        }
        return date;
    }

    protected void setDate(String fieldName, Date value) {
        String dateString = DateUtils.getIsoStringDate(value);
        contentValues.put(fieldName, dateString);
    }

    protected Integer getInt(String column) {
        return contentValues.getAsInteger(column);
    }

    protected void setInt(String fieldName, Integer value) {
        contentValues.put(fieldName, value);
    }

    protected String getString(String fieldName) {
        return contentValues.getAsString(fieldName);
    }

    protected void setString(String fieldName, String value) {
        contentValues.put(fieldName, value);
    }

    protected Double getDouble(String column) {
        return contentValues.getAsDouble(column);
    }

    protected void setDouble(String column, Double value) {
        contentValues.put(column, value);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        this.contentValues.writeToParcel(dest, flags);
    }
}
